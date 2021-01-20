package dt.server;

import dt.exceptions.CommandException;
import dt.exceptions.UserExit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/** @author Emiel Rous and Wouter Koning */
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
