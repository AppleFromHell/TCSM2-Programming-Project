package dt.exceptions;

public class NotYourTurnException extends Exception {
    public NotYourTurnException(String not_your_turn) {
        super(not_your_turn);
    }

    public NotYourTurnException(){
    }
}
