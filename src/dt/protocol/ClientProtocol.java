package dt.protocol;

import dt.exceptions.InvalidMoveException;
import dt.util.Move;

import java.net.ProtocolException;

/** @author Emiel Rous and Wouter Koning */
public interface ClientProtocol {

    void doHello();
    void doLogin(String username);
    void doGetList();
    void doMove(Move move) throws InvalidMoveException, ProtocolException;
    void doEnterQueue();
}
