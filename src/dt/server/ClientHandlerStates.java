package dt.server;

/**
 * @author Emiel Rous and Wouter Koning
 * The various states the ClientHandler can be in.
 */
public enum ClientHandlerStates {
    LOGGEDIN,
    INQUEUE,
    INGAME,
    IDLE
}
