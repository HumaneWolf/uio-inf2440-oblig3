/**
 * This class exist purely so that I can differentiate it from InvalidArguementException.
 */
public class NoMorePrimesException extends Exception {
    public NoMorePrimesException(String ex) {
        super(ex);
    }
}
