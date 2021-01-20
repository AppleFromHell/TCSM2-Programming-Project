package dt.collectoClient;

import dt.exceptions.InvalidMoveException;
import dt.exceptions.UserExit;
import dt.util.Move;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Time;

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
            new Server_prompt();
            try {
                this.client.createConnection();
                break;
            } catch (IOException e) {
                shorErrorPopup("Couldn't connect to server, try again");
            }
        }

        while(true) {
            try {
                String userName = this.userNamePrompt();
                if (userName == null) {
                    client.shutDown();
                }
                this.client.doLogin(userName);
                synchronized (this) {
                    this.wait();
                }
                if (this.client.getState() == ClientStates.PENDINGLOGIN) {
                    shorErrorPopup("Username already logged in, try again");
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                shorErrorPopup("Something went wrong, try again");
            }
        }
        display = new MainDisplay(this);
        this.setContentPane(display);
        this.pack();
        this.setVisible(true);
    }

    private String userNamePrompt() {
        return JOptionPane.showInputDialog("Connection Successful!\nEnter Username to login");
    }

    public void shorErrorPopup(String err) {
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
                client.doGetList();
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

    @Override
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void displayList(String[] list) {
        this.display.updateUserList(list);
    }

    @Override
    public void reconnect() throws UserExit {

    }

    @Override
    public void displayChatMessage(String msg) {
        this.display.displayMessage(msg);
    }


    public void makeMove(int move) {
    }

    public void sendMessage(String text) {
        client.doSendChat(text);
    }
}
