package dt.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerTUI extends SimpleTUI  {
    private Server server;
    boolean autoStartup = true;

    public ServerTUI(Server server) {
        this.server = server;
    }
    public void start() {
        if (!autoStartup) {
            while (this.server.getPort() == null) {
                this.server.setPort(getPort());
            }
        } else {
            this.server.setPort(6969);
        }
    }
}
