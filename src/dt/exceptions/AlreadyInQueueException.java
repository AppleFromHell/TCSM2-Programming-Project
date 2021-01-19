package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class AlreadyInQueueException extends Throwable {
    public AlreadyInQueueException(String yer_already_in_a_queue) {
        super(yer_already_in_a_queue);
    }
}
