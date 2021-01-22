package dt.collectoClient.GUI;

import dt.collectoClient.Client;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server_prompt extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JCheckBox useDefaultCheckBox;
    private Client client;

    public Server_prompt(Client client) {
        this.client = client;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });



        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        useDefaultCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    textField1.setText("localhost");

                    textField1.setEditable(false);
                    textField2.setText("6969");
                    textField2.setEditable(false);
                } else {
                    textField1.setEditable(true);
                    textField2.setEditable(true);
                }

            }
        });
        this.pack();
        this.setVisible(true);
    }

    private void onOK() {
        System.out.println(textField1.getText());
        try {
            this.client.setIp(InetAddress.getByName(textField1.getText()));
        } catch (UnknownHostException e) {
e.printStackTrace();        }
        this.client.setPort(Integer.parseInt(textField2.getText()));
        dispose();

    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        System.exit(0);
    }
}
