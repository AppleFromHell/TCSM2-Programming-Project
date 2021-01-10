package dt.collectoClient;

import java.io.*;
import dt.model.board.ClientBoard;
import java.net.InetAddress;
import java.net.Socket;

public class CollectoClient {
    private static final int BOARDSIZE = 7;

    private Socket serverSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String userName;
    private Integer port;
    private ClientView clientView;
    private ClientBoard board;
    private InetAddress ip;

    public CollectoClient() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard(BOARDSIZE);
        this.userName = null;
        this.port = null;
    }

    public void start() {
        new Thread(clientView).start();
    }
    public void write(String input) throws IOException {
        out.write(input);
        out.newLine();
        out.flush();
    }

    public void createConnection() {
        createConnection(this.ip, this.port, this.userName);
    }

    private void createConnection(InetAddress ip, Integer port, String userName) {
        clearConnection();

        while (serverSocket == null) {

            // try to open a Socket to the server
            try {
                InetAddress addr = ip;
                System.out.println("Attempting to connect to " + addr + ":"
                        + port + "...");
                serverSocket = new Socket(addr, port);
                in = new BufferedReader(new InputStreamReader(
                        serverSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(
                        serverSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("ERROR: could not create a socket on "
                        + ip + " and port " + port + ".");
            }
        }
        clientView.showMessage("Connected to server!");
    }
    public void clearConnection() {
        serverSocket = null;
        in = null;
        out = null;
    }
    public String getUserName() {
        return this.userName;
    }
    public String setUsername(String userName) {
        return this.userName = userName;
    }

    public InetAddress getIp() {
        return this.ip;
    }
    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public static void main(String[] args) {
        CollectoClient collectoClient = new CollectoClient();
        collectoClient.start();
    }
}
