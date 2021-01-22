package dt.collectoClient;

import dt.ai.AI;
import dt.ai.AITypes;
import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UserExit;
import dt.model.ClientBoard;
import dt.util.SimpleTUI;
import dt.util.Move;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/** @author Emiel Rous and Wouter Koning */
public class ClientTUI extends SimpleTUI implements ClientView {
    //TODO add an AI (with adjustable difficulty level)

    //TODO The user should decide at the start of the game who decides on the moves (AI or human)

    private boolean interrupted =false;
    private Client client;

    ClientTUI(Client client) {
        this.client = client;
    }

    @Override
    public void start() {
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
                    synchronized (this) {
                        this.wait();
                    }
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
                        if(this.client.getAi() == null){ // If the human has decided to play for themselves.
                            input = getString("Next Move:");

                        }
                    } else {

                        try {
                            synchronized (this) {
                                if(interrupted) this.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                            input = getString(); //Wait for user input
                    }
                    if(input != null) {
                        handleUserInput(input);
                    }
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
                throw new CommandException(String.format(UNKOWNCOMMAND, arguments[0]));
            switch (cmd) {
                case LIST:
                    this.client.doGetList();
                    break;
                case QUEUE:
                    this.client.doEnterQueue();
                    break;
                case MOVE:
                    if(client.getAi() == null) {
                        this.client.doMove(parseMove(arguments));
                    } else {
                        client.getAi().findBestMove(this.client.getBoard());
                    }
                    break;
                case HINT:
                    this.client.provideHint();
                    break;
                case HELP:
                    printHelpMenu();
                    break;
                case CHAT:
                    String[] splitChat = input.split(UserCmds.separators, 2);
                    this.client.doSendChat(splitChat[1]);
                    this.showMessage(client.getUserName()+":"+splitChat[1]);
                    break;
                case WHISPER:
                    String[] splitWhisper = input.split(UserCmds.separators, 3);
                    String receiver = splitWhisper[1];
                    String whisperMessage = splitWhisper[2];
                    this.client.doSendWhisper(receiver, whisperMessage);
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandException("Invalid number of arguments give");
        } catch (NumberFormatException e) {
            throw new CommandException(NOTINTEGERMOVE);
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

    public void displayList(String[] list) {
        this.showMessage("List of logged in users");
        for(int i = 0; i < list.length; i++) {
            this.showMessage(list[i]);
        }
    }

    /**
     *
     * @return A new instance of an AI type. If the return value is null, the person has chosen for manual playing.
     * @throws UserExit if the user decides to exit the program.
     */
    public synchronized AI getClientAI() throws UserExit {
        this.interrupted = true;
        boolean aiEnabled = getBoolean("Would you like to play this game with an AI?"); //TODO Sometimes you have to answer this question twice?
        if(aiEnabled){

            String question = "What AI difficulty would you like to use for this game? Choose from:"
                    .concat(System.lineSeparator())
                    .concat(AITypes.allToString());
            String aiString = getString(question);

            while(true) {
                try {
                    AITypes aiType = AITypes.valueOf(aiString.toUpperCase());
                    this.notify();
                    return aiType.getAIClass();
                } catch (IllegalArgumentException e) {
                    getString(aiString + " is not a valid AI type. Choose one of the following AI Types: "
                            .concat(System.lineSeparator())
                            .concat(AITypes.allToString()));
                }
            }
        }
        this.notify();
        return null;
    }

    @Override
    public void clearBoard(){

    }
    @Override
    public void showBoard(ClientBoard board) {
        this.showMessage(board.getPrettyBoardState());
        this.showMessage("############################################");

    }

    @Override
    public void displayChatMessage(String msg) {
        showMessage(msg);
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
