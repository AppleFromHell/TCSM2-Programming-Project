package dt.exceptions;

/**
 * Thrown when a {@link dt.collectoClient.Client} or {@link dt.server.ClientHandler} tries to make a move while it's not their turn
 *
 * @author Emiel Rous and Wouter Koning
 */
public class NotYourTurnException extends Exception {
    public NotYourTurnException(String not_your_turn) {
        super(not_your_turn);
    }

}
