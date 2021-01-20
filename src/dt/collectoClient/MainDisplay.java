package dt.collectoClient;

import dt.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class MainDisplay extends JPanel{
    private JPanel mainPanel;
    private JPanel gameDisplay;
    private JTextPane userList;
    private JPanel chatAndUsers;
    private JButton send;
    private JTextField chatFIeld;
    private JLabel usersLabel;
    private JPanel textAndSend;
    private JTextPane chatbox;
    private JTextField moveField;
    private JButton confirmButton;
    private JLabel moveLabel;
    private ClientGUI view;

    public MainDisplay(ClientGUI view) {
        this.add(mainPanel);
        this.view = view;
        send.addActionListener(e -> sendMessage());
        chatFIeld.addActionListener(e -> sendMessage());
        confirmButton.addActionListener(e -> makeMove());
        moveField.addActionListener(e -> makeMove());
    }

    public void updateUserList(String[] list) {
        StringBuilder users = new StringBuilder();
        for(String u : list) {
           users.append(u).append('\n');
        }
        userList.setText(users.toString());
    }

    private void makeMove() {
        try {
            view.makeMove(Integer.parseInt(moveField.getText()));
        } catch (NumberFormatException ex) {
            view.shorErrorPopup("Enter an integer");
            moveField.setText("");
        }
    }

    public void displayMessage(String msg) {
        this.chatbox.setText(this.chatbox.getText() + '\n' + msg);
    }

    private void sendMessage() {
        view.sendMessage(chatFIeld.getText());
        chatFIeld.setText("");
    }

    public static void main(String[] args) {
        MainDisplay display = new MainDisplay(null);
        JFrame frame = new JFrame("HI");
        frame.setContentPane(display);
        frame.pack();
        frame.setVisible(true);

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        this.gameDisplay = new GameDisplay(new int[]{1,2,4,5,2,4,3,2,2,6,6,4,3,3,2,3,4,0,5,3,5,4,4,3,2,4,5,3,2,3,4,4,3,2,1,1,2,3,4,5,4,3,2,2,3,4,5,2,2});
    }
}
