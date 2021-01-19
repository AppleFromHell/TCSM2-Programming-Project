package dt.exceptions;

public class ClientHandlerNotFoundException extends Exception {
    public ClientHandlerNotFoundException(String maybeClientIsSnake) {
        super(maybeClientIsSnake);
    }
}
