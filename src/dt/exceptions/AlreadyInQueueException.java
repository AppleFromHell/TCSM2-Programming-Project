package dt.exceptions;

/**
 * Thrown when a user tries to enter the queue while they are already in the queue
 * @author Emiel Rous and Wouter Koning */
public class AlreadyInQueueException extends Throwable {
    public AlreadyInQueueException(String yer_already_in_a_queue) {
        super(yer_already_in_a_queue);
    }
}
