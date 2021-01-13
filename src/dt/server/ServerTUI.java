package dt.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerTUI extends SimpleTUI  {
    private Server server;

    public ServerTUI(Server server) {
        this.server = server;
    }
    public void start() {
        while (this.server.getPort() == null) {
            this.server.setPort(getPort());
        }
    }
}
