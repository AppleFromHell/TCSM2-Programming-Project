package dt.protocol;

import dt.model.Game;
import dt.server.ClientHandler;

import java.net.ProtocolException;

/** @author Emiel Rous and Wouter Koning */
public interface ServerProtocol {

    void handleHello(String[] arguments) throws ProtocolException;
    void handleLogin(String[] arguments) throws ProtocolException;
    void handleChat(String msg) throws ProtocolException;
    void handleWhisper(String msg) throws ProtocolException;
    void handleList();
    void handleQueue();

    void startGame(boolean startsFirst, ClientHandler opponent, Game game);
}
