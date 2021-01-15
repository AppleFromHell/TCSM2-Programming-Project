package dt.protocol;

import dt.model.Game;
import dt.server.ClientHandler;

import java.net.ProtocolException;

public interface ServerProtocol {

    void handleHello(String[] arguments) throws ProtocolException;
    void handleLogin(String[] arguments) throws ProtocolException;
    void handleList();
    void handleQueue();
    void startGame(boolean startsFirst, ClientHandler opponent, Game game);
}
