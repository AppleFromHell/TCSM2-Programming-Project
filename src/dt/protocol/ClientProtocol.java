package dt.protocol;

import dt.exceptions.InvalidMoveException;
import dt.util.Move;

public interface ClientProtocol {

    void doHello();
    void doLogin(String username);
    void doGetList();
    void doMove(Move move) throws InvalidMoveException;
    void doEnterQueue();
}
