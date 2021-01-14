package dt.server;

import dt.exceptions.CommandException;
import dt.exceptions.UserExit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerTUI extends SimpleTUI  {
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
