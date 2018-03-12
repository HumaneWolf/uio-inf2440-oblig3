import java.util.TreeMap;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Magnus Espeland <magnuesp@ifi.uio.no>
 * @changed 2018.03.02
 *
 * Class for ensuring unified output from Oblig 3, INF2440 - Spring 2018
 *
 *
 * Usage:
 * --
 * FactorPrintOut out = new FactorPrintOut("username", n);
 *
 * <Do this in a loop fitting your program and how you store the results>
 * out.addFactor(3999999999999999999L, 3);
 * out.addFactor(3999999999999999999L, 31);
 * out.addFactor(3999999999999999999L, 31);
 * out.addFactor(3999999999999999999L, 64516129);
 * out.addFactor(3999999999999999999L, 666666667);
 *
 * out.writeFactors();
 * --
 *
 * Note: This is meant to be run at the end of your program, 
 * when the results are ready
 *
 * It is NOT thread safe and NOT efficient
 *
 * The only reason for this program is to make the correcting of the oblig
 * easier for the TAs.
 *
 * Please ask questions in Piazza.
 *
 */

public class FactorPrintOut {

    String username;
    int n;

    TreeMap<Long, LinkedList<Long>> factors = new TreeMap<Long, LinkedList<Long>>();

    /**
     * Create an object for unified factor printing
     *
     *
     * @param username Students username
     * @param n The n given at startup
     */

    public FactorPrintOut(String username, int n) {
        this.username = username;
        this.n = n;

    }

    /**
     * Add a factor to a number
     *
     *
     * @param base This is the number you started to factorize (ie 3999999999999999999)
     * @param factor This is the factor you have found
     */

    public void addFactor(long base, long factor) {

        Long longObj = new Long(base);

        if(!factors.containsKey(longObj))
            factors.put(longObj, new LinkedList<Long>());

        //System.out.printf("Adding %d to %d\n",factor, base);

        factors.get(longObj).add(factor);

    }

    /**
     * Writes the factors you found to a file named "username_n.txt"
     *
     */

    public void writeFactors() {
        String filename = username + "_" + n + ".txt";

        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.printf("Factors for n=%d\n", n);

            for(Map.Entry<Long, LinkedList<Long>> entry : factors.entrySet()) {

                // Starting a new line with the base
                writer.print(entry.getKey() + " : ");


                // Sort the factors
                Collections.sort(entry.getValue());

                // Then print the factors
                String out = "";
                for(Long l : entry.getValue())
                    out += l + "*";

                // Removing the trailing '*'
                writer.println(out.substring(0, out.length()-1));


            }

            writer.flush();
            writer.close();

        } catch(Exception e) {
            System.out.printf("Got exception when trying to write file %s : ",filename, e.getMessage());
        }



    }

}