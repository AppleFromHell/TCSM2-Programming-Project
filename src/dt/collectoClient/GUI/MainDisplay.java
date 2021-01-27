package dt.collectoClient.GUI;

import dt.ai.AITypes;
import dt.collectoClient.Client;

import javax.swing.*;
import java.util.Arrays;

public class MainDisplay extends JPanel{
    private JPanel mainPanel;
    private JPanel gameDisplay;
    private JTextPane userList;
    private JPanel chatAndUsers;
    private JButton send;
    private JTextField chatField;
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
    private JButton hintButton;
    private JComboBox selectAi;
    private ClientGUI view;
    private String username;
    private String serverName;
    private Client client;

    public MainDisplay(ClientGUI view, Client client) {
        this.add(mainPanel);
        this.view = view;
        send.addActionListener(e -> sendMessage());
        chatField.addActionListener(e -> sendMessage());
        confirmButton.addActionListener(e -> makeMove());
        moveField.addActionListener(e -> makeMove());
        queueButton.addActionListener(e -> enterQueue());
        hintButton.addActionListener(e ->  getHint());
        this.client = client;
        selectAi.addActionListener(e -> setAi());
        Arrays.stream(AITypes.values()).map(AITypes::toString).forEach(selectAi::addItem);
    }

    private void setAi() {
        this.view.setClientAI(AITypes.valueOf((String)selectAi.getSelectedItem()));
    }

    private void getHint() {
        this.client.provideHint();
    }
    public void updateRankingList(String ranking) {
        userList.setText(ranking);
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
        }
        moveField.setText("");
    }

    public void displayMessage(String msg) {
        this.chatbox.setText(this.chatbox.getText() + '\n' + msg);
    }

    private void sendMessage() {
        view.sendMessage(chatField.getText());
        chatField.setText("");
    }

    public static void main(String[] args) {
        MainDisplay display = new MainDisplay(null, new Client());
        JFrame frame = new JFrame("HI");
        frame.setContentPane(display);
        frame.pack();
        frame.setVisible(true);

    }
    private void enterQueue() {
        this.queueButton.setEnabled(false);
        view.enterQueue();
    }

    public void enableQueue() {
        this.queueButton.setEnabled(true);
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
        this.turnLabel.setText("Turn: " + (ourTurn? "Ours": "Theirs"));
    }

    public void emptyBoard() {
        this.turnLabel.setText("Turn: ");
        ((GameDisplay)this.gameDisplay).setEmptyBoard();
        ((GameDisplay)this.gameDisplay).paintComponent(this.gameDisplay.getGraphics());
    }
}
