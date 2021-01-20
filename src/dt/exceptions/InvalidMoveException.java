package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String msg){
        super(msg);
    }
    public InvalidMoveException(){}
}
