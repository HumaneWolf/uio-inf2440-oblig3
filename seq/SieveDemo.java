import java.util.LinkedList;

/**
 * Assignment 3 base
 *
 * @author Magnus Espeland <magnuesp@ifi.uio.no>
 *
 * Written for INF2440 - Spring 2018
 * Presented in the group session 2018.02.26 10:15
 *
 * Assumptions:
 *
 * 1. We don't need to consider even numbers.
 * There is only one prime among them, namely 2, which we hard code
 *
 * 2. If we start at an odd number we can skip ahead two times the prime
 * when we traverse the array (since odd number + odd number = even number,
 * and we don't need to consider even numbers)
 *
 * 3. We don't need to start flipping numbers before prime²
 * Example: 3 flips every non-prime below 25, so when we traverse with 5
 * we can start directly at 25.
 *
 *
 * This code is proof of concept. I has no error checks and safeguards,
 * but your code should!
 *
 * Please ask questions in Piazza :)
 *
 */


public class SieveDemo {

    byte[] theArray;

    int n = 10000;

    LinkedList<Integer> primes = new LinkedList<Integer>();


    public static void main(String[] args) {
        SieveDemo sd = new SieveDemo();

        sd.findFirst20();
    }


    public SieveDemo() {

        // There are room for 8 bits in every cell, and we don't need
        // to consider even numbers. Add one since it's integer division.
        int cells = n / 16 + 1;

        theArray = new byte[cells];

    }

    /**
     * The implementation of the algorithm "Sieve of Eratosthenes"
     *
     *
     */

    public void findFirst20() {

        primes.add(2); // These are the only primes we hard code
        primes.add(3);

        int currentPrime = 3;

        do {
            System.out.printf("Current prime is %d\n",currentPrime);

            // Go thru all the numbers
            traverse(currentPrime);

            // Get the next prime
            // Start at the next odd number from the current prime
            currentPrime = findNextPrime(currentPrime+2);
            primes.add(currentPrime);

        } while(primes.size() < 20); // Do this until we have 20 primes


        System.out.printf("First 20 primes:\n");
        int k=1;
        for(Integer p : primes)
            System.out.printf("%02d %02d\n", k++, p);
    }

    /**
     *
     * Go thru the numbers flipping each one that is divisible by the
     * current prime
     *
     * @param p The current prime
     */

    public void traverse(int p) {
        // According to assumption #3 we can start at prime²
        // According to assumption #2 we only consider odd numbers
        // (hence we skip ahead two times the prime on every iteration)
        for(int i = p*p; i < n; i += p * 2) {
            //System.out.printf("Prime %d Flipping %d\n",p, i);

            flip(i);
        }
    }

    /**
     * We mark/flip the bit corresponding to this integer in the byte array
     *
     * @param i Integer to flip
     */

    public void flip(int i) {

        // What if something goes wrong and this method is given an even number?

        // Again not considering even numbers, and 8 bits per byte
        int byteCell    = i / 16;
        int bit         = (i/2) % 8;

        //System.out.printf("%d means byte %d and bit %d\n", i, byteCell, bit);

        // | is bitwise OR
        // |= is the same as += just with bitwise OR instead of addition

        // 1 << bit shifts a binary 1 to the right position in the byte
        // Example: 1 << 2 becomes 00000100 in binary

        theArray[byteCell] |= (1 << bit);

        //System.out.printf("Cell %d is now %d or in binary: %8s\n\n",
        //    byteCell, theArray[byteCell], bitString(theArray[byteCell]));

    }

    /**
     * Check if this integer has been flipped. If it hasn't, it is not divisible by any
     * of the previous primes, and hence a prime itself.
     *
     * @param i Integer to check, only odd numbers!
     * @return
     */

    public boolean isPrime(int i) {

        // What if something goes wrong and this method is given an even number?

        int byteCell    = i / 16;
        int bit         = (i/2) % 8;

        // See the previous method
        // If this bit hasn't been flipped the & operation (bitwise AND) will result
        // in the bits 00000000 which equals 0

        return (theArray[byteCell] & (1 << bit)) == 0;

    }

    /**
     * Iterate over the numbers, and return the next prime
     *
     * @param startAt Where to start looking.
     * @return The next prime
     */

    public int findNextPrime(int startAt) {

        // Assumption #1 - Only consider odd numbers
        for(int i = startAt; i < n; i += 2) {
            if(isPrime(i))
                return i;
        }

        return 0; // Error prone

    }


    /**
     * Return a 8 character string with 0 and 1 representing the bits
     * of the byte given
     *
     * @see https://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
     *
     * @param b A byte
     * @return String
     */

    public String bitString(byte b) {
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }

}