package dt.exceptions;

/**
 * Thrown when the {@link dt.util.Move} that was tried is not valid
 *
 * @author Emiel Rous and Wouter Koning
 */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String msg) {
        super(msg);
    }

    public InvalidMoveException() {
    }
}
