package dt.collectoClient;

import java.io.*;

import dt.exceptions.InvalidMoveException;
import dt.exceptions.ServerUnavailableException;
import dt.model.board.ClientBoard;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;

import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
    private ClientStates state;

    private static final String PROTOCOLEXCEPTIONMESSAGE = "Server list response not according to protocol. Response: ";

    public CollectoClient() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard();
        this.userName = null;
        this.port = null;
        this.chatEnabled = false;
        this.rankEnabled = false;
    }

    public void start() {
        this.state = ClientStates.STARTINGUP;
        new Thread(clientView).start();
    }

    private String[] splitResponse(String in) {
        return in.split(ProtocolMessages.delimiter);
    }

    @Override
    public String doHello() throws ServerUnavailableException, ProtocolException {
        write(ClientMessages.HELLO.constructMessage(CLIENTDESCRIPTION + this.userName));
        String rawResponse = readLineFromServer();
        String[] response = splitResponse(rawResponse);
        if(response[0].equals(ServerMessages.HELLO.toString())){
            switch (response.length){
                case 2:
                    this.chatEnabled = response[1].equals(ProtocolMessages.Messages.CHAT.toString());
                    this.rankEnabled =  response[1].equals(ProtocolMessages.Messages.RANK.toString());
                    break;
                case 3:
                    this.chatEnabled = response[1].equals(ProtocolMessages.Messages.CHAT.toString()) ||
                            response[2].equals(ProtocolMessages.Messages.CHAT.toString());
                    this.rankEnabled = response[1].equals(ProtocolMessages.Messages.RANK.toString()) ||
                            response[2].equals(ProtocolMessages.Messages.RANK.toString());
                    break;
            }
            return response[1];
        }
        throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + rawResponse);
    }

    @Override
    public boolean doLogin(String username) throws ServerUnavailableException, ProtocolException {
        write(ClientMessages.LOGIN.constructMessage(username));
        String response = readLineFromServer();
        if(!response.equals(ServerMessages.LOGIN.toString()) ||
                !response.equals(ServerMessages.ALREADYLOGGEDIN.toString())) {
            throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + response);
        }
        this.state = ClientStates.LOGGEDIN;
        return response.equals(ServerMessages.LOGIN.toString());
    }

    @Override
    public String doGetList() throws ServerUnavailableException, ProtocolException {
        String ret = "List of logged in users: \n";
        write(ClientMessages.LIST.constructMessage());
        String rawResponse = readLineFromServer();
        String[] response = splitResponse(rawResponse);

        if(!response[0].equals(ServerMessages.LIST.toString())) {
            throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + rawResponse);
        }

        for(int i = 1; i < response.length; i++) {
            ret = ret.concat(response[i] + '\n');
        }
        return ret;
    }

    @Override
    public String doMove(int move) throws ServerUnavailableException, ProtocolException, InvalidMoveException {
        board.makeMove(move);
        String ourMove = ClientMessages.MOVE.constructMessage(String.valueOf(move));
        return writeAndMakeResponseMove(ourMove);
    }


    @Override
    public String doMove(int move, int move2) throws ServerUnavailableException, ProtocolException, InvalidMoveException {
        board.makeMove(move, move2);
        String ourMove = ClientMessages.MOVE.constructMessage(String.valueOf(move), String.valueOf(move2));
        return writeAndMakeResponseMove(ourMove);
    }

    private String writeAndMakeResponseMove(String move) throws ServerUnavailableException, ProtocolException, InvalidMoveException {
        write(move);
        String rawOurMoveResponse = readLineFromServer();
        String[] ourMoveResponse = splitResponse(rawOurMoveResponse);
        String rawTheirMove = readLineFromServer();
        String[] theirMove = splitResponse(rawTheirMove);

        if(!ourMoveResponse[0].equals(ServerMessages.MOVE.toString())) {
            throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + rawOurMoveResponse);
        }
        if(!theirMove[0].equals(ServerMessages.MOVE.toString())) {
            throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + rawTheirMove);
        }
        if(!rawOurMoveResponse.equals(move)) {
            throw new ProtocolException("Move response not the same as ours. " +
                    "Our move: " + move +
                    " Their response: " + rawOurMoveResponse);
        }

        int theirMove1 = -1;
        int theirMove2 = -1;
        try {
            theirMove1 = Integer.parseInt(theirMove[1]);
            if(theirMove.length == 3) {
                theirMove2 = Integer.parseInt(theirMove[2]);
            }
        } catch (NumberFormatException e) {
            throw new ProtocolException("Their move was not an int. Response : "+ rawTheirMove);
        }

        if(theirMove2 == -1) {
            board.makeMove(theirMove1);
        } else {
            board.makeMove(theirMove1, theirMove2);
        }
        return board.getPrettyBoardState();
    }

    @Override
    public String doEnterQueue() throws ServerUnavailableException, ProtocolException {
        write(ClientMessages.QUEUE.constructMessage());
        String rawResponse = readLineFromServer();
        String[] response = splitResponse(rawResponse);
        if(!response[0].equals(ServerMessages.NEWGAME.toString())) {
            throw new ProtocolException(PROTOCOLEXCEPTIONMESSAGE + rawResponse);
        }
        return "Entered queue";
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
        try {
            clientView.showMessage(doHello());
        } catch (Exception e ) {
            clientView.showMessage("Shit broke");
        }
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
