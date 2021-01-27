package dt.exceptions;

/**
 * Thrown when the clientHandler could not be found in a game
 * @author Emiel Rous and Wouter Koning
 */
public class ClientHandlerNotFoundException extends Exception {
    public ClientHandlerNotFoundException(String maybeClientIsSnake) {
        super(maybeClientIsSnake);
    }
}
