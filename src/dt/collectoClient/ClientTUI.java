package dt.collectoClient;

import dt.server.ServerTUI;
import dt.server.SimpleTUI;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientTUI extends SimpleTUI implements ClientView, Runnable  {

    private CollectoClient collectoClient;
    private boolean autoStartup = true;

    ClientTUI(CollectoClient collectoClient) {
        this.collectoClient = collectoClient;
    }

    @Override
    public void start() {
        if(!autoStartup) {
            while (collectoClient.getIp() == null) {
                this.collectoClient.setIp(getIp());
            }
            while (collectoClient.getPort() == null) {
                this.collectoClient.setPort(getPort());
            }
        } else {
            this.collectoClient.setPort(6969);
            try {
                this.collectoClient.setIp(InetAddress.getByName("localhost"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        collectoClient.createConnection();

        while(!exit) {
            try {
                String input = getString("What would you like to do?");
                handleUserInput(input);
            } catch (IOException e) {
                showMessage("Shit broke");
            }
        }
    }




    private void handleUserInput(String input) throws IOException {

    }

    public String getUsername() {
        return getString("What username would you like to have?");
    }

    public InetAddress getIp() {
        while(!exit) {
            try {
                return InetAddress.getByName(getString("What IP address is the server running on (format: x.x.x.x)"));
            } catch (UnknownHostException e) {
                showMessage("Invalid IP, try again. Format: x.x.x.x where x stands for 1-3 integers");
            }
        }
        return null;
    }
}
