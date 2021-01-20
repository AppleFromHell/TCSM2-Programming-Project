package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class ServerUnavailableException extends Exception {
    public ServerUnavailableException(String msg) {
        super(msg);
    }
    public ServerUnavailableException(){}
}
