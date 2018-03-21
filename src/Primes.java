import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Primes {

    private static long n;
    private static int k;

    private static final int runs = 7;
    private static final int medianIndex = 4;
    private static double[] seqTiming = new double[runs];
    private static double[] parTiming = new double[runs];

    private long globalRemain;

    /**
     * Main.
     * @param args The program arguments.
     *             0 = N - The number to find primes below.
     *             1 = K - The number of threads to use to do the work.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Primes [Ceiling, highest number to check for] [Number of threads to use, 0 to use number of cores the machine has]");
            return;
        }
        n = Long.parseLong(args[0]);
        k = Integer.parseInt(args[1]);
        if (k == 0) k = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < runs; i++) {
            new Primes(i);
        }

        Arrays.sort(seqTiming);
        Arrays.sort(parTiming);

        System.out.printf("Sequential median  : %.3f\n", seqTiming[medianIndex]);
        System.out.printf(
                "Parallel median    : %.3f    Speedup from sequential: %.3f\n",
                parTiming[medianIndex], (seqTiming[medianIndex] / parTiming[medianIndex])
        );
        System.out.println("\nn = " + n);
    }

    /**
     * Constructor for the class.
     * @param run The run number, used to store timings.
     */
    private Primes(int run) {
        long startTime;
        int cells = (int)(n / 16 + 1);

        // Do sequential tests
        System.out.println("Starting sequential");
        startTime = System.nanoTime();
        byte[] seqArray = new byte[cells];
        LinkedList<Long>[] seqFactors = new LinkedList[100];
        for (int i = 0; i < 100; i++) seqFactors[i] = new LinkedList<Long>();
        seq(seqArray, seqFactors);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");

        /* Sequential printing of factors, used for testing purposes.
        FactorPrintOut fpo = new FactorPrintOut("krishto", n);
        for (int i = 0; i < seqFactors.length; i++) {
            Integer[] arr = seqFactors[i].toArray(new Integer[seqFactors[i].size()]);
            for (int j : arr) {
                fpo.addFactor((n * n) - 1 - i, j);
            }
        }
        fpo.writeFactors();
        */

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        byte[] parArray = new byte[cells];
        LinkedList<Long>[] parFactors = new LinkedList[100];
        for (int i = 0; i < 100; i++) parFactors[i] = new LinkedList<Long>();
        par(parArray, parFactors);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");

        // Print parallel factoring results.
        FactorPrintOut fpo = new FactorPrintOut("krishto", (int)n);
        for (int i = 0; i < parFactors.length; i++) {
            Long[] arr = parFactors[i].toArray(new Long[parFactors[i].size()]);
            Arrays.sort(arr);
            for (long j : arr) {
                fpo.addFactor((n * n) - 1 - i, j);
            }
        }
        fpo.writeFactors();

        // Check if it is correct.
        for (int i = 0; i < seqArray.length; i++) { // Primes
            if (seqArray[i] != parArray[i]) {
                System.out.printf(
                        "[PRIMES] Mismatch at index %d\n\t%s and %s.\n",
                        i, bitString(seqArray[i]), bitString(parArray[i])
                );
            }
        }

        // Check if factors match.
        for (int i = 0; i < seqFactors.length; i++) {
            Long[] parArr = parFactors[i].toArray(new Long[parFactors[i].size()]);
            Arrays.sort(parArr);
            Long[] seqArr = seqFactors[i].toArray(new Long[seqFactors[i].size()]);
            Arrays.sort(seqArr);

            for (int j = 0; j < seqArr.length; j++) {
               if (!parArr[j].equals(seqArr[j])) {
                   System.out.printf(
                           "[FACTORS] Mismatch at index %d-%d\n\ts:%d and p:%d.\n",
                           i, j, seqArr[j], parArr[j]
                   );
               }
            }
        }
    }

    /**
     * Do the algorithm sequentially.
     * @param array The byte array to work with.
     */
    private void seq(byte[] array, LinkedList<Long>[] factors) {
        long currentPrime = 3; // 2 is marked by default, because we skip even nums.

        while (currentPrime*currentPrime <= n) {
            //System.out.println("Prime found: " + currentPrime);

            flipInRange(array, currentPrime, currentPrime*currentPrime, n);
            try {
                currentPrime = findNextPrime(array, currentPrime + 2);
            } catch (NoMorePrimesException e) {
                break;
            }
        }

        // Factorize
        long num;
        long remain;
        for (int i = 0; i < 100; i++) {
            num = (n * n) - 1 - i;
            remain = num;

            // 2 is an egde case
            while (remain % 2 == 0) {
                factors[i].push(2L);
                remain = remain / 2;
            }

            // General
            long j = 3;
            while ((j * j) < num && remain != 1) {
                if (remain % j == 0) {
                    factors[i].push(j);
                    remain = remain / j;
                } else {
                    try {
                        j = findNextPrime(array, j + 2);
                    } catch (NoMorePrimesException e) {
                        if (remain != 1) {
                            factors[i].push(remain);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Do the algorithm in parallel.
     * @param array The byte array to work with.
     */
    private void par(byte[] array, LinkedList<Long>[] factors) {
        // Sequential start.
        long currentPrime = 3; // 2 is marked by default, because we skip even nums.

        int sqrtN = (int)Math.sqrt(n);

        // Find all primes in the square root of n, using only the square root of those numbers to generate it..
        while (currentPrime*currentPrime <= sqrtN) {
            //System.out.println("Prime found: " + currentPrime);

            flipInRange(array, currentPrime, currentPrime*currentPrime, sqrtN);
            try {
                currentPrime = findNextPrime(array, currentPrime + 2);
            } catch (NoMorePrimesException e) {
                break;
            }
        }

        // Threads doing more flipping work.
        CyclicBarrier cb = new CyclicBarrier(k); // Main will not use it.
        Lock lock = new ReentrantLock();

        Thread[] threads = new Thread[k];
        int segmentSize = (int)((n - sqrtN) / 16) / k; // (Number of bits to check / nums per byte) / threads
        // Will need to multiply by 16 before sending as argument, since arguments take bits as input, not byte.

        for (int i = 0; i < k; i++) {
            int start = sqrtN + ((i * segmentSize) * 16);
            int stop = start + (segmentSize * 16);
            stop = (i == (k - 1)) ? (int)n : stop;

            threads[i] = new Thread(new Worker(i, array, start, stop, factors, cb, lock));
            threads[i].start();
        }

        for (int i = 0; i < k; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * Worker class for parallel solution.
     */
    private class Worker implements Runnable {
        byte[] array;
        int id, start, stop;

        LinkedList<Long>[] factors;
        CyclicBarrier cb;
        Lock lock;

        Worker(int id, byte[] array, int start, int stop, LinkedList<Long>[] factors, CyclicBarrier cb, Lock lock) {
            this.id = id;

            this.array = array;
            this.start = start;
            this.stop = stop;

            this.factors = factors;
            this.cb = cb;
            this.lock = lock;
        }

        @Override
        public void run() {
            // Find primes.
            long currentPrime = 3;

            while (currentPrime*currentPrime <= n && currentPrime*currentPrime <= stop) {
                flipInRange(
                        array,
                        currentPrime,
                        currentPrime*currentPrime > start ? currentPrime*currentPrime : start, // Highest one
                        stop
                );
                try {
                    currentPrime = findNextPrime(array, currentPrime + 2);
                } catch (NoMorePrimesException e) {
                    break;
                }
            }

            // Wait for all to finish.
            try {
                cb.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            // Factorize.
            long num;
            long localRemain;
            for (int i = 0; i < 100; i++) {
                num = ((n * n) - 1 - i);
                if (id == 0) {
                    globalRemain = num;
                }
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                // Safe because we're between two barriers where nobody writes.
                localRemain = globalRemain;

                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                // 2 is still an edge case
                if (id == 0) {
                    if (localRemain % 2 == 0) {
                        lock.lock();
                        try {
                            while (localRemain % 2 == 0) {
                                factors[i].push(2L);
                                globalRemain = globalRemain / 2;
                                localRemain = globalRemain;
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }

                // General case
                long j = 3;
                boolean run = true; // If no primes for this thread and this number, don't run.
                for (int l = 0; l < id; l++) { // "Push" the starting point for each thread to an appropriate value.
                    try {
                        j = findNextPrime(array, j + 2);
                    } catch (NoMorePrimesException e) {
                        run = false;
                    }
                }
                while ((j * j) < num && localRemain != 1 && run) {
                    if (localRemain != globalRemain) {
                        lock.lock();
                        try {
                            localRemain = globalRemain;
                        } finally {
                            lock.unlock();
                        }
                    }

                    if (localRemain % j == 0) {
                        lock.lock();
                        try {
                            factors[i].push(j);
                            globalRemain = globalRemain / j;
                            localRemain = globalRemain;
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        try {
                            // Skip until the next number to process.
                            for (int l = 0; l < (k - 1); l++) {
                                j = findNextPrime(array, j + 2);
                            }
                            j = findNextPrime(array, j + 2);
                        } catch (NoMorePrimesException e) {
                            break;
                        }

                    }
                }

                // Wait for all to finish
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                // Add last factor if there is one more.
                // Because only 1 thread does this and it's between two barriers, this is safe.
                if (globalRemain != 1 && id == 0) {
                    factors[i].push(globalRemain);
                }

                // Wait before starting to process the next num.
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Assuming prime is a prime, flips all numbers divisible by prime between start and stop.
     * Start and stop values should reflect the assumptions.
     * @param array The bit array to work with.
     * @param prime The prime to process.
     * @param start Start point, inclusive.
     * @param stop Stop point, exclusive.
     */
    private void flipInRange(byte[] array, long prime, long start, long stop) {
        //System.out.println("flipinrange(" + prime + ", " + start + " to " + stop + ")");
        if ((prime & 1) == 0) {
            throw new IllegalArgumentException("Can not have an even prime.");
        }

        long rest = start % prime;
        if (rest != 0) {
            start = start + prime - rest;
        }
        if ((start & 1) == 0) {
            start = start + prime;
        }

        for (long i = start; i < stop; i += prime*2) {
            flipBit(array, i);
        }
    }

    /**
     * Flip the bit representing i in the array. Basically the same as the demo code.
     * @param array The array storing all the bytes.
     * @param i The number, flips the bit representing this number.
     * @throws IllegalArgumentException If i is not an odd number.
     */
    private void flipBit(byte[] array, long i) throws IllegalArgumentException {
        if ((i & 1) == 0) {
            throw new IllegalArgumentException("Can not flip an even bit.");
        }

        int cell = (int)(i / 16);
        int bit = (int)((i/2) % 8);

        //System.out.println("Flipped " + i);

        array[cell] |= (1 << bit);
    }

    /**
     * Check if the number is a prime by checking if its bit has been flipped.
     * @param array The array to work with.
     * @param i The number to check.
     * @return True if it is a prime number, false otherwise.
     * @throws IllegalArgumentException If i is not an odd number.
     */
    private boolean isPrime(byte[] array, long i) throws IllegalArgumentException {
        if ((i & 1) == 0) {
            throw new IllegalArgumentException("Can not check an even bit.");
        }

        int cell = (int)(i / 16);
        int bit = (int)((i/2) % 8);

        return (array[cell] & (1 << bit)) == 0;
    }

    /**
     * Find the next prime after, and including, startAt.
     * @param array The byte array to use in the search.
     * @param startAt The number to start at.
     * @return The next prime number below n.
     * @throws IllegalArgumentException If startAt is not an odd number.
     * @throws NoMorePrimesException If there are no more primes from startAt to n.
     */
    private long findNextPrime(byte[] array, long startAt) throws IllegalArgumentException, NoMorePrimesException {
        if ((startAt & 1) == 0) {
            throw new IllegalArgumentException("startAt can not be an even number.");
        }

        for (long i = startAt; i < n; i +=2) { // Check only the odd numbers.
            if (isPrime(array, i)) {
                return i;
            }
        }

        throw new NoMorePrimesException("No more primes.");
    }

    /**
     * Generate a string of 8 characters representing the content of a byte. Same as in sequential course code.
     * @param b The byte to print.
     * @return A string object of 8 characters, all of which are 0 or 1.
     */
    private String bitString(byte b) {
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }
}
