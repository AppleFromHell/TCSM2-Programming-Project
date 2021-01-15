package dt.collectoClient;

import dt.exceptions.UserExit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGUI extends JFrame implements ClientView, ActionListener {
    private Client client;
    private JButton bConnect;
    private JTextField tfPort;
    private JTextArea taMessages;

    public ClientGUI(Client client) {
        super("Collecto Client");
        this.client = client;
    }


    public void start() {
        setSize(700, 400);

        // Panel panel1 - Listen

        JPanel panel1 = new JPanel(new FlowLayout());
        JPanel pp = new JPanel(new GridLayout(2, 2));

        JLabel lbAddress = new JLabel("Address: ");
        JTextField tfAddress = new JTextField("getHostAddress()", 12);
        tfAddress.setEditable(false);

        JLabel lbPort = new JLabel("Port:");
        tfPort = new JTextField("2727", 5);

        pp.add(lbAddress);
        pp.add(tfAddress);
        pp.add(lbPort);
        pp.add(tfPort);

        bConnect = new JButton("Start Listening");
        bConnect.addActionListener(this);

        panel1.add(pp, BorderLayout.WEST);
        panel1.add(bConnect, BorderLayout.EAST);

        // Panel p2 - Messages

        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());

        JLabel lbMessages = new JLabel("Messages:");
        taMessages = new JTextArea("", 15, 50);
        taMessages.setEditable(false);
        p2.add(lbMessages);
        p2.add(taMessages, BorderLayout.SOUTH);

        Container cc = getContentPane();
        cc.setLayout(new FlowLayout());
        cc.add(panel1);
        cc.add(p2);
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
