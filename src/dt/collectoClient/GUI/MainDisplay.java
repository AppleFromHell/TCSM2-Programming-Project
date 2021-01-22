package dt.collectoClient.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JButton queueButton;
    private ClientGUI view;

    public MainDisplay(ClientGUI view) {
        this.add(mainPanel);
        this.view = view;
        send.addActionListener(e -> sendMessage());
        chatFIeld.addActionListener(e -> sendMessage());
        confirmButton.addActionListener(e -> makeMove());
        moveField.addActionListener(e -> makeMove());
        queueButton.addActionListener(e -> enterQueue());
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
            view.makeMove(moveField.getText());
        } catch (NumberFormatException ex) {
            view.showErrorPopup("Enter an integer");
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
    private void enterQueue() {
        this.queueButton.setEnabled(false);
        view.enterQueue();
    }

    private void createUIComponents() {
        this.gameDisplay = new GameDisplay();
    }

    public void showBoard(int[] boardState) {
        ((GameDisplay) gameDisplay).setGameState(boardState);
    }
}
