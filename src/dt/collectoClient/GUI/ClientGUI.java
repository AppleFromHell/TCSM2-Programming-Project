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
import java.awt.event.*;
import java.io.IOException;
import java.net.ProtocolException;
//TODO hint doen
/** @author Emiel Rous and Wouter Koning */
public class ClientGUI extends JFrame implements ClientView {
    private final Client client;
    private MainDisplay display;
    public ClientGUI(Client client) {
        super("Collecto Client");
        this.client = client;
    }


    public void start() {

        while(true) {
            new Server_prompt(this.client);
            try {
                this.client.createConnection();
                break;
            } catch (IOException e) {
                showErrorPopup("Couldn't connect to server, try again");
            }
        }

        while(true) {
            try {
                String userName = this.userNamePrompt();
                if (userName == null) {
                    client.shutDown();
                }
                this.client.doLogin(userName);
                this.client.setUsername(userName);
                synchronized (this) {
                    this.wait();
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
        display = new MainDisplay(this, this.client);
        display.setUsername('\n'+client.getUserName());
        display.setServerName('\n'+client.getServerName());
        this.setContentPane(display);
        this.pack();
        this.setVisible(true);
    }

    private String userNamePrompt() {
        return JOptionPane.showInputDialog("Connection Successful!\nEnter Username to login");
    }

    public void showErrorPopup(String err) {
        JOptionPane.showConfirmDialog(
                this,
                err,
                "ERROR",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);
    }

    public void run() {
        int delay = 5000; //milliseconds
        ActionListener taskPerformer = evt -> {
            if(this.display != null) {
                client.doGetRanking();
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
    public void gameOver(String msg) {
        showErrorPopup(msg);
        this.display.enableQueue();
        this.display.emptyBoard();
    }
    @Override
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void displayList(String[] list) {
        this.display.updateUserList(list);
    }


    @Override
    public boolean reconnect() throws UserExit {
        showErrorPopup("Server Disconnected");
        return true;
    }
    @Override
    public void showHint(String hint) {
        showErrorPopup("Hint: " + hint);
    }

    @Override
    public void showRank(String rank) {
        this.display.updateRankingList(rank);
    }

    @Override
    public void clearBoard() {
        this.display.emptyBoard();
    }

    @Override
    public void showBoard(ClientBoard board) {
        this.display.setOurTurn(client.isOurTurn());
        this.display.showBoard(board.getBoardState());
    }

    @Override
    public void displayChatMessage(String msg) {
        this.display.displayMessage(msg);
    }

    @Override
    public void setClientAI(AITypes types) {
        this.client.setAI(types);
    }


    public void makeMove(String move) {
        String[] arguments = move.split(UserCmds.separators);

        try {
            if(client.getAi() == null) {
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

    public void sendMessage(String text) {
        client.doSendChat(text);
    }

    public void enterQueue() {
        client.doEnterQueue();
    }
}
