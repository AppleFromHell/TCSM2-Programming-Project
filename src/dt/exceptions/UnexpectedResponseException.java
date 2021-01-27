package dt.exceptions;

/**
 * Thrown when the message that was received was not expected according to the protocol
 *
 * @author Emiel Rous and Wouter Koning
 */
public class UnexpectedResponseException extends Exception {
    public UnexpectedResponseException(String msg) {
        super(msg);
    }

    public UnexpectedResponseException() {
    }
}
