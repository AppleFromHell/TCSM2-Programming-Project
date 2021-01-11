package dt.collectoClient;

import java.io.*;

import dt.exceptions.ServerUnavailableException;
import dt.model.board.ClientBoard;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;

import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;

public class CollectoClient implements ClientProtocol {

    private Socket serverSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String userName;
    private Integer port;
    private ClientView clientView;
    private ClientBoard board;
    private InetAddress ip;
    private final String CLIENTDESCRIPTION = "Client By: ";
    private boolean chatEnabled;
    private boolean rankEnabled;

    public CollectoClient() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard();
        this.userName = null;
        this.port = null;
        this.chatEnabled = false;
        this.rankEnabled = false;
    }

    public void start() {
        new Thread(clientView).start();
    }
    private String[] splitResponse(String in) {
        return in.split(ProtocolMessages.delimiter);

    }
    @Override
    public String doHello() throws ServerUnavailableException, ProtocolException {
        write(ClientMessages.HELLO.constructMessage(CLIENTDESCRIPTION + this.userName));
        String[] response = splitResponse(readLineFromServer());
        if(response[0].equals(ServerMessages.HELLO.toString())){
            switch (response.length){
                case 2:
                    chatEnabled = response[1].equals(ProtocolMessages.Messages.CHAT.toString());
                    rankEnabled = response[1].equals(ProtocolMessages.Messages.RANK.toString());
                    break;
                case 3:
                    chatEnabled = response[1].equals(ProtocolMessages.Messages.CHAT.toString()) ||
                            response[2].equals(ProtocolMessages.Messages.CHAT.toString());
                    rankEnabled = response[1].equals(ProtocolMessages.Messages.RANK.toString()) ||
                            response[2].equals(ProtocolMessages.Messages.RANK.toString());
                    break;
            }
            return response[1];
        }
        throw new ProtocolException("Handshake unsuccessful");
    }

    @Override
    public String doLogin(String username) {
        return null;
    }

    @Override
    public String doGetList() {
        return null;
    }

    @Override
    public String doMove(int move) {
        return null;
    }

    @Override
    public String doEnterQueue() {
        return null;
    }
    private synchronized void write(String input) throws ServerUnavailableException {
        if(out != null) {
            try {
                out.write(input);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new ServerUnavailableException("Could not write to server");
            }
        } else {
            throw new ServerUnavailableException("Socket is not available");
        }
    }

    private String readLineFromServer() throws ServerUnavailableException {
        if (in != null) {
            try {
                // Read and return answer from Server
                String answer = in.readLine();
                if (answer == null) {
                    throw new ServerUnavailableException("Could not read "
                            + "from server.");
                }
                return answer;
            } catch (IOException e) {
                throw new ServerUnavailableException("Could not read "
                        + "from server.");
            }
        } else {
            throw new ServerUnavailableException("Could not read "
                    + "from server.");
        }
    }

    public void createConnection() {
        createConnection(this.ip, this.port, this.userName);
    }

    private void createConnection(InetAddress ip, Integer port, String userName) {
        clearConnection();

        while (serverSocket == null) {

            // try to open a Socket to the server
            try {
                System.out.println("Attempting to connect to " + ip + ":"
                        + port + "...");
                serverSocket = new Socket(ip, port);
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
