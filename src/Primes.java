import java.util.Arrays;

public class Primes {

    private static int n;
    private static int k;

    private static final int runs = 7;
    private static final int medianIndex = 4;
    private static double[] seqTiming = new double[runs];
    private static double[] parTiming = new double[runs];

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
        n = Integer.parseInt(args[0]);
        k = Integer.parseInt(args[1]);
        if (k == 0) k = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < runs; i++) {
            new Primes(i);

            if (true) return;
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
        int cells = n / 16 + 1;

        // Do sequential tests
        System.out.println("Starting sequential");
        startTime = System.nanoTime();
        byte[] seqArray = new byte[cells];
        seq(seqArray);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");

        if (true) return;

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        byte[] parArray = new byte[cells];
        par(parArray);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");

        // Check if it is correct.
        for (int i = 0; i < seqArray.length; i++) {
            if (seqArray[i] != parArray[i]) {
                System.out.printf(
                        "Mismatch at index %d\n\t%s and %s.\n",
                        i, bitString(seqArray[i]), bitString(parArray[i])
                );
            }
        }
    }

    /**
     * Do the algorithm sequentially.
     * @param array The byte array to work with.
     */
    private void seq(byte[] array) {
        int currentPrime = 3; // 2 is marked by default, because we skip even nums.

        double sqrtN = Math.sqrt(n);

        while (currentPrime <= sqrtN) {
            //System.out.println("Prime found: " + currentPrime);

            flipInRange(array, currentPrime, currentPrime*currentPrime, n);
            try {
                currentPrime = findNextPrime(array, currentPrime + 2);
            } catch (NoMorePrimesException e) {
                break;
            }
        }
    }

    /**
     * Do the algorithm in parallel.
     * @param array The byte array to work with.
     */
    private void par(byte[] array) {

    }

    /**
     * Assuming prime is a prime, flips all numbers divisible by prime between start and stop.
     * Start and stop values should reflect the assumptions.
     * @param array The bit array to work with.
     * @param prime The prime to process.
     * @param start Start point, inclusive.
     * @param stop Stop point, exclusive.
     */
    public void flipInRange(byte[] array, int prime, int start, int stop) {
        //System.out.println("flipinrange(" + prime + ", " + start + " to " + stop + ")");
        if ((prime & 1) == 0) {
            throw new IllegalArgumentException("Can not have an even prime.");
        }

        int rest = start % prime;
        if (rest != 0) {
            start = start + prime - rest;
        }
        if ((start & 1) == 0) {
            start = start + prime;
        }

        for (int i = start; i < stop; i += prime*2) {
            flipBit(array, i);
        }
    }

    /**
     * Flip the bit representing i in the array. Basically the same as the demo code.
     * @param array The array storing all the bytes.
     * @param i The number, flips the bit representing this number.
     * @throws IllegalArgumentException If i is not an odd number.
     */
    public void flipBit(byte[] array, int i) throws IllegalArgumentException {
        if ((i & 1) == 0) {
            throw new IllegalArgumentException("Can not flip an even bit.");
        }

        int cell = i / 16;
        int bit = (i/2) % 8;

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
    public boolean isPrime(byte[] array, int i) throws IllegalArgumentException {
        if ((i & 1) == 0) {
            throw new IllegalArgumentException("Can not check an even bit.");
        }

        int cell = i / 16;
        int bit = (i/2) % 8;

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
    public int findNextPrime(byte[] array, int startAt) throws IllegalArgumentException, NoMorePrimesException {
        if ((startAt & 1) == 0) {
            throw new IllegalArgumentException("startAt can not be an even number.");
        }

        for (int i = startAt; i < n; i +=2) { // Check only the odd numbers.
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
