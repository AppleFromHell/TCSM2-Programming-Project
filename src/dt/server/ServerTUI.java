package dt.server;

import dt.exceptions.UserExit;
import dt.util.SimpleTUI;

/** @author Emiel Rous and Wouter Koning */
public class ServerTUI extends SimpleTUI {
    private Server server;

    public ServerTUI(Server server) {
        this.server = server;
    }


    @Override
    public void start() {
        try {
            while (this.server.getPort() == null) {
                this.server.setPort(getPort());
            }
            while (true) {
                getString("");
            }
        } catch (UserExit e) {
            server.shutDown();
        }
    }
}
