package dt.collectoClient.GUI;

import javax.swing.*;

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
    private JLabel usernameLabel;
    private JLabel servernameLabel;
    private JLabel turnLabel;
    private ClientGUI view;
    private String username;
    private String serverName;

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

    public void setUsername(String username) {
        this.username = username;
        this.usernameLabel.setText(this.usernameLabel.getText() + username);
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
        this.servernameLabel.setText(this.servernameLabel.getText() + serverName);
    }

    public void setOurTurn(boolean ourTurn) {
        this.turnLabel.setText(turnLabel.getText() + (ourTurn? "Ours": "Theirs"));
    }

    public void emptyBoard() {
        this.turnLabel.setText("Turn:");
        ((GameDisplay)this.gameDisplay).setEmptyBoard();
    }
}
