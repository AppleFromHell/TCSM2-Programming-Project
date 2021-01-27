package dt.collectoClient.GUI;

import dt.collectoClient.Client;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The popup for the server Ip and port
 *
 * @author Wouter Koning and Emiel Rous
 */
public class Server_prompt extends JDialog {
    private final Client client;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JCheckBox useDefaultCheckBox;

    public Server_prompt(Client client) {
        this.client = client;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        useDefaultCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                textField1.setText("localhost");
                textField1.setEditable(false);
                textField2.setText("6969");
                textField2.setEditable(false);
            } else {
                textField1.setEditable(true);
                textField2.setEditable(true);
            }

        });
        this.pack();
        this.setVisible(true);
    }

    /**
     * Set the ip and the port of the client
     *
     * @ensures dialog is closed
     */
    private void onOK() {
        try {
            this.client.setIp(InetAddress.getByName(textField1.getText()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.client.setPort(Integer.parseInt(textField2.getText()));
        dispose();
    }

    /**
     * @ensures dialog is closed
     */
    private void onCancel() {
        dispose();
        this.client.shutDown();
    }
}
