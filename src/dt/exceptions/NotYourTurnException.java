package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class NotYourTurnException extends Exception {
    public NotYourTurnException(String not_your_turn) {
        super(not_your_turn);
    }

    public NotYourTurnException(){
    }
}
