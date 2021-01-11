package dt.collectoClient;

import java.net.InetAddress;

public interface ClientView extends Runnable {

    InetAddress ip = null;


    void start();
    void run();
    void showMessage(String msg);
}
