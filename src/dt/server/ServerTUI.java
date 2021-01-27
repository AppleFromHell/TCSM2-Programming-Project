package dt.server;

import dt.exceptions.UserExit;
import dt.util.SimpleTUI;

/** @author Emiel Rous and Wouter Koning */
public class ServerTUI extends SimpleTUI {
    private final Server server;

    public ServerTUI(Server server) {
        this.server = server;
    }

    /**
     * Start the ServerTUI in a separate Thread.
     */
    @Override
    public void start() {
        try {
            while (this.server.getPort() == null) {
                this.server.setPort(getPort());
                //Signaling that the port is filled
                synchronized (server) {
                    server.notify();
                }
                //Waiting on response
                synchronized (this) {
                    this.wait();
                }
            }

            while (true) {
                getString("");
            }
        } catch (UserExit | InterruptedException e) {
            server.shutDown();
        }
    }
}
