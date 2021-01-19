package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class UnexpectedResponseException extends Exception{
    public UnexpectedResponseException(String msg) {
        super(msg);
    }
    public UnexpectedResponseException(){}
}
