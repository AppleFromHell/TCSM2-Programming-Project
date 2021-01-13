package dt.protocol;

import dt.exceptions.InvalidMoveException;
import dt.exceptions.ServerUnavailableException;

import java.net.ProtocolException;

public interface ClientProtocol {

    void doHello();
    void doLogin(String username);
    void doGetList();
    void doMove(int move) throws InvalidMoveException;
    void doMove(int move, int move2) throws InvalidMoveException;
    void doEnterQueue();
}
