package dt.collectoClient.GUI;

import dt.ai.AITypes;
import dt.collectoClient.Client;
import dt.collectoClient.ClientStates;
import dt.collectoClient.ClientView;
import dt.collectoClient.UserCmds;
import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UserExit;
import dt.model.ClientBoard;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ProtocolException;

/**
 * The gui interface for the client
 *
 * @author Emiel Rous and Wouter Koning
 */
public class ClientGUI extends JFrame implements ClientView {
    private final Client client;
    private MainDisplay display;

    public ClientGUI(Client client) {
        super("Collecto Client");
        this.client = client;
    }

    /**
     * Start the main flow. First ip and port are prompted. Then username, once verified start main display
     *
     * @ensures the client is connected if ip and port are valid
     * @ensures the user can exit
     */
    public void start() {

        while (true) {
            new Server_prompt(this.client); //Prompt the user for an ip and port
            try {
                this.client.createConnection();
                break;
            } catch (IOException e) {
                showErrorPopup("Couldn't connect to server, try again");
            }
        }

        while (true) {
            try {
                String userName = this.userNamePrompt();
                if (userName == null) {
                    client.shutDown();
                }
                this.client.doLogin(userName);
                this.client.setUsername(userName);
                synchronized (this) {
                    this.wait(); //Wait for the client to verify login name
                }
                if (this.client.getState() == ClientStates.PENDINGLOGIN) {
                    showErrorPopup("Username already logged in, try again");
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                showErrorPopup("Something went wrong, try again");
            }
        }
        display = new MainDisplay(this, this.client); //Start main display
        display.setUsername('\n' + client.getUserName());
        display.setServerName('\n' + client.getServerName());
        this.setContentPane(display);
        this.pack();
        this.setVisible(true);
    }

    /**
     * Show a popup with a username field
     *
     * @return the username the user inputs
     */
    private String userNamePrompt() {
        return JOptionPane.showInputDialog("Connection Successful!\nEnter Username to login");
    }

    /**
     * Display an error popup
     *
     * @param err
     */
    public void showErrorPopup(String err) {
        JOptionPane.showConfirmDialog(
                this,
                err,
                "ERROR",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an info popup
     *
     * @param err
     */
    public void showInfoPopup(String err) {
        JOptionPane.showConfirmDialog(
                this,
                err,
                "INFO",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Starts gui thread.
     * A timer is added so that the list is updated every 5 seconds. Sets window closing action.
     * Sets main theme of GUI
     *
     * @ensures the list is updated every 5 seconds
     */
    public void run() {
        int delay = 5000; //milliseconds
        ActionListener taskPerformer = evt -> {
            if (this.display != null) {
                client.doGetList(); //Update the list every 5 seconds
            }
        };
        new Timer(delay, taskPerformer).start();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                client.shutDown();
            }
        });
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        start();
    }

    /**
     * Handle a game over
     *
     * @param msg
     * @ensures the queue button is enabled again
     * @ensures the board is empty
     */
    public void gameOver(String msg) {
        showInfoPopup(msg);
        this.display.enableQueue();
        this.display.emptyBoard();
    }

    @Override
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    /**
     * Updates the list on the main display
     *
     * @param list
     */
    @Override
    public void displayList(String[] list) {
        this.display.updateUserList(list);
    }

    /**
     * Prompt the user for a reconnect
     *
     * @return
     * @throws UserExit
     * @ensures the user can exit
     */
    @Override
    public boolean reconnect() throws UserExit {
        showErrorPopup("Server Disconnected");
        return true;
    }


    @Override
    public void showHint(String hint) {
        showInfoPopup("Hint: " + hint);
    }

    @Override
    public void showRank(String rank) {
        this.display.updateRankingList(rank);
    }

    /**
     * Updates the board state in the GUI
     *
     * @param board
     */
    @Override
    public void showBoard(ClientBoard board) {
        this.display.setOurTurn(client.isOurTurn());
        this.display.showBoard(board.getBoardState());
    }

    /**
     * Add a new chatmessage to the chat window
     *
     * @param msg
     */
    @Override
    public void displayChatMessage(String msg) {
        if (this.display != null) {
            this.display.displayMessage(msg);
        }
    }

    @Override
    public void setClientAI(AITypes types) {
        this.client.setAI(types);
    }

    /**
     * Makes a move. If the client is not AI, the move arguments are checked
     *
     * @param move
     */
    public void makeMove(String move) {
        String[] arguments = move.split(UserCmds.separators);

        try {
            if (client.getAi() == null) {
                this.client.doMove(parseMove(arguments));
            } else {
                this.client.doAIMove();
            }
        } catch (CommandException e) {
            showErrorPopup(String.format(UNKOWNCOMMAND, arguments[0]));
        } catch (InvalidMoveException | ProtocolException e) {
            showErrorPopup(e.getMessage());
        }
    }

    /**
     * Send a message to the client and display it in the chatbox
     *
     * @param text
     */
    public void sendMessage(String text) {
        this.displayChatMessage(this.client.getUserName() + ": " + text);
        client.doSendChat(text);
    }

    public void enterQueue() {
        client.doEnterQueue();
    }
}
