package dt.collectoClient;

import dt.exceptions.UserExit;

import java.net.InetAddress;

public interface ClientView extends Runnable {

    void start();

    void run();

    void showMessage(String msg);

    void displayList(String[] list);

    void reconnect() throws UserExit;

    void displayChatMessage(String msg);
}
