package dt.protocol;

import dt.exceptions.ServerUnavailableException;

import java.net.ProtocolException;

public interface ClientProtocol {

    String doHello() throws ServerUnavailableException, ProtocolException;
    String doLogin(String username);
    String doGetList();
    String doMove(int move);
    String doEnterQueue();
}
