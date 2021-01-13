package dt.collectoClient;

import dt.protocol.ClientMessages;
import dt.server.SimpleTUI;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTUI extends SimpleTUI implements ClientView, Runnable  {

    private Client client;
    private boolean autoStartup = true;

    ClientTUI(Client client) {
        this.client = client;
    }

    @Override
    public synchronized void start() {
        while (client.getIp() == null) {
            this.client.setIp(getIp());
        }
        while (client.getPort() == null) {
            this.client.setPort(getPort());
        }

        while(!exit) {
            try {
                client.createConnection();
                break;
            } catch (Exception e) {
                showMessage("Server not availabe. Reason: " + e.getMessage());
                exit = !getBoolean("Try again? (y/n)");
            }
        }

        String username = "Somethin wong";
        while(!exit) {
            try{
                this.wait();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(client.getState() == ClientStates.LOGGEDIN) break;
            username = getUsername();
            client.doLogin(username);
        }
        this.client.setUsername(username);

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
        try {
            switch (ClientMessages.valueOf(input)) {
                case LIST:
                    client.doGetList();
                    break;
                default:
                    client.writeMessage(input);
                    break;
            }
            ;
        } catch (IllegalArgumentException e) {
            client.writeMessage(input);
        }
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
