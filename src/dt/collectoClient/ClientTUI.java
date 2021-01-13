package dt.collectoClient;

import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.server.SimpleTUI;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientTUI extends SimpleTUI implements ClientView, Runnable  {

    private Client client;

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
            } catch (CommandException e) {
                this.showMessage(e.getMessage());
        }
        }
    }



    private void handleUserInput(String input) throws CommandException {
        try {
            String[] arguments = input.split(" ");
            UserCmds cmd = UserCmds.getUserCmd(arguments[0]);
            if(cmd == null) throw new CommandException("Unkown command: " + arguments[0]+ "For a list of valid commands type h");
            switch (cmd) {
                case LIST:
                    this.client.doGetList();
                    break;
                case QUEUE:
                    this.client.doEnterQueue();
                    break;
                case MOVE:
                    if(arguments.length == 2) {
                        this.client.doMove(Integer.parseInt(arguments[1]));
                    }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandException("Invalid number of arguments give");
        } catch (NumberFormatException e) {
            throw new CommandException("Move was not an integer");
        } catch (InvalidMoveException e) {
            throw new CommandException("Move was not valid");
        }

    }

    public String getUsername() {
        return getString("What username would you like to have?");
    }

    public synchronized void displayList(String[] list) { //TODO checken wat synchronized moet zijn
        this.showMessage("List of logged in users");
        for(int i = 0; i < list.length; i++) {
            this.showMessage(list[i]);
        }
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
