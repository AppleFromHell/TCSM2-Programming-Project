package dt.exceptions;

public class UnexpectedResponseException extends Exception{
    public UnexpectedResponseException(String msg) {
        super(msg);
    }
    public UnexpectedResponseException(){}
}
