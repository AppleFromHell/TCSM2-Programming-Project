package dt.collectoClient;

import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UserExit;
import dt.server.SimpleTUI;
import dt.util.Move;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientTUI extends SimpleTUI implements ClientView {

    private Client client;

    ClientTUI(Client client) {
        this.client = client;
    }

    @Override
    public synchronized void start() {
        try {
            while (client.getIp() == null) {
                this.client.setIp(getIp());
            }
            while (client.getPort() == null) {
                this.client.setPort(getPort());
            }

            this.createConnection();

            String username = "Somethin wong";
            while (true) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (client.getState() == ClientStates.LOGGEDIN) break;
                username = getUsername();
                client.doLogin(username);
            }
            this.client.setUsername(username);

            while (true) {
                try {
                    String input = "";
                    if (this.client.getState() == ClientStates.INGAME) {
                        input = getString("Next Move:");
                    } else {
                         input = getString("What would you like to do?");
                    }
                    handleUserInput(input);
                } catch (CommandException e) {
                    this.showMessage(e.getMessage());
                }
            }
        } catch (UserExit e) {
            client.shutDown();

        }
    }


    private void handleUserInput(String input) throws CommandException {
        try {
            String[] arguments = input.split(UserCmds.separators);
            UserCmds cmd = UserCmds.getUserCmd(arguments[0]);
            if (cmd == null)
                throw new CommandException("Unkown command: " + arguments[0] + "For a list of valid commands type h");
            switch (cmd) {
                case LIST:
                    this.client.doGetList();
                    break;
                case QUEUE:
                    this.client.doEnterQueue();
                    break;
                case MOVE:
                    if (arguments.length == 2) {
                        this.client.doMove(new Move(Integer.parseInt(arguments[1])));
                    } else if (arguments.length == 3) {
                        this.client.doMove(new Move(Integer.parseInt(arguments[1], Integer.parseInt(arguments[2]))));
                    } else {
                        throw new CommandException("Too many moves");
                    }
                    break;
                case HELP:
                    printHelpMenu();
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandException("Invalid number of arguments give");
        } catch (NumberFormatException e) {
            throw new CommandException("Move was not an integer");
        } catch (InvalidMoveException e) {
            throw new CommandException(e.getMessage());
        }

    }

    public String getUsername() throws UserExit {
        return getString("What username would you like to have?");
    }

    public void reconnect() throws UserExit {

        if(getBoolean("Reconnect to server? (y/n)")) {
            createConnection();
        } else {
            throw new UserExit();
        }
    }

    private void createConnection() throws UserExit{
            while (true) {
                try {
                    client.createConnection();
                    break;
                } catch (Exception e) {
                    this.showMessage("Server not availabe. Reason: " + e.getMessage());
                    if (!getBoolean("Try again? (y/n)")) {
                        throw new UserExit();
                    }
                }
            }
    }


    private void printHelpMenu() {
        String ret = "Here is the list of commands:\n";
        ret += UserCmds.getPrettyCommands();
        this.showMessage(ret);
    }

    public synchronized void displayList(String[] list) { //TODO checken wat synchronized moet zijn
        this.showMessage("List of logged in users");
        for(int i = 0; i < list.length; i++) {
            this.showMessage(list[i]);
        }
    }

    public InetAddress getIp() throws UserExit {
        try {
            return InetAddress.getByName(getString("What IP address is the server running on (format: x.x.x.x)"));
        } catch (UnknownHostException e) {
            showMessage("Invalid IP, try again. Format: x.x.x.x where x stands for 1-3 integers");
        }

        return null;
    }
}
