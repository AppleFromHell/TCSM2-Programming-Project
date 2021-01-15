package dt.collectoClient;

import dt.exceptions.UserExit;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientGUI extends JFrame implements ClientView, ActionListener {
    private final Client client;
    private JButton bConnect;
    private JTextField tfPort;
    private JTextArea taMessages;

    public ClientGUI(Client client) {
        super("Collecto Client");
        this.client = client;
    }


    public void start() {

        this.serverAddressPrompt();
        try {
            this.client.createConnection();
        } catch (IOException e) {
            shorErrorPopup("Couldn't connect to server, try again");
            serverAddressPrompt();
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
    }

    private String userNamePrompt() {
        return JOptionPane.showInputDialog("Connection Successful!\nEnter Username to login");
    }
    private void serverAddressPrompt() {
        JPanel panel = new JPanel();
        JTextField ip = new JTextField(10);
        JTextField port = new JTextField(4);
        ip.setEditable(false);
        ip.setText("localhost");
        port.setEditable(false);
        port.setText("6969");
        JLabel server = new JLabel("Server:");
        JCheckBox checkBox = new JCheckBox("Default", true);
        checkBox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                ip.setEditable(false);
                ip.setText("localhost");
                port.setEditable(false);
                port.setText("6969");
            } else {
                ip.setEditable(true);
                port.setEditable(true);
            }
        });
        panel.add(server);
        panel.add(ip);
        panel.add(port);
        panel.add(checkBox);
        Object[] options = {"Connect", "Exit"};
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_CANCEL_OPTION,
                null,
                options);
        JDialog dialog = new JDialog(this, "Server address", true);
        dialog.setSize(300, 300);

        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(
                e -> {
                    String prop = e.getPropertyName();
                    if (dialog.isVisible()
                            && (e.getSource() == optionPane)
                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                });
        dialog.pack();
        dialog.setVisible(true);
        String value = (String) optionPane.getValue();
        if (value == options[0]) {
            try {
                this.client.setIp(InetAddress.getByName(ip.getText()));
            } catch (UnknownHostException e) {
                shorErrorPopup("Enter a valid IP address");
                serverAddressPrompt();
            }
        } else if (value == options[1]) {
            client.shutDown();
        } else {
            client.shutDown();
        }
    }
    private void shorErrorPopup(String err) {
        JOptionPane.showConfirmDialog(
                this,
                err,
                "ERROR",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE);
    }

    public void run() {
        start();
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    @Override
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void displayList(String[] list) {

    }

    @Override
    public void reconnect() throws UserExit {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == bConnect) {
            taMessages.append("YEET");
        }
    }
}
