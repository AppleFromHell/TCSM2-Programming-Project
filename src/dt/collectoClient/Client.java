package dt.collectoClient;

import java.io.*;

import com.sun.source.tree.PackageTree;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UnexpectedResponseException;
import dt.model.board.ClientBoard;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;

import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Arrays;

public class Client implements ClientProtocol, NetworkEntity {

    private Socket serverSocket;
    private ConnectionHandler connection;
    private String userName;
    private Integer port;
    private final ClientView clientView;
    private ClientBoard board;
    private InetAddress ip;
    private final String CLIENTDESCRIPTION = "Client By: Emiel";
    private boolean chatEnabled;
    private boolean rankEnabled;
    private ClientStates state;
    private int[] ourLastMove;


    public Client() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard();
        this.userName = null;
        this.port = null;
        this.chatEnabled = false;
        this.rankEnabled = false;
    }
    public static void main(String[] args) {
        Client client = new Client();
        if(args.length > 1) {
            try {
                client.ip = InetAddress.getByName(args[0]);
                client.port = Integer.parseInt(args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.start();
    }

    public void start() {
        this.state = ClientStates.STARTINGUP;
        new Thread(clientView).start();
    }
    public void writeMessage(String msg) {
        connection.write(msg);
    }
    @Override
    public void handleMessage(String msg) {
        String[] arguments = msg.split(ProtocolMessages.delimiter);
        String keyWord = arguments[0];

        try {
            switch (ServerMessages.valueOf(keyWord)) {
                case HELLO:
                    if (this.state == ClientStates.PENDINGHELLO) {
                        this.state = ClientStates.HELLOED;
                        this.handleHello(arguments);
                        this.clientView.showMessage("Handshake successful! Connected to server: " + arguments[1]);
                        synchronized (clientView) {
                            this.clientView.notify();
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case LOGIN:
                    if (this.state == ClientStates.PENDINGLOGIN) {
                        this.state = ClientStates.LOGGEDIN;
                        this.clientView.showMessage("Successfully logged in!");
                        synchronized (clientView) {
                            this.clientView.notifyAll();
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case ALREADYLOGGEDIN:
                    if (this.state == ClientStates.PENDINGLOGIN) {
                        this.clientView.showMessage("Username already logged in, try again");
                        synchronized (clientView) {
                            this.clientView.notifyAll();
                        }
                    }else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case LIST:
                    if (this.state == ClientStates.WAITINGONLIST) {
                        this.clientView.showMessage(parseListResponse(arguments));
                    }
                    break;
                case NEWGAME:
                    if(this.state == ClientStates.INQUEUE) {
                        this.createNewBoard(arguments);
                    }   else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case MOVE:
                    if (this.state == ClientStates.AWAITMOVERESPONSE) {
                        this.checkMoveResponse(arguments);
                    }
                    if (this.state == ClientStates.AWAITNGTHEIRMOVE) {
                        try {
                            this.makeTheirMove(arguments);
                        } catch (InvalidMoveException e) {
                            throw new InvalidMoveException("The server move was invalid");
                        }
                    }
                case GAMEOVER:
                    this.clientView.showMessage(this.handleGameOver(arguments));
                    break;
                case ERROR:

            }
        }catch (UnexpectedResponseException e) {
            clientView.showMessage("Unexpected response: " + msg);
        } catch (IllegalArgumentException | ProtocolException e) {
            clientView.showMessage("Invalid response from server. Response: " + msg);
            if(!e.getMessage().equals("")) clientView.showMessage("Reason: " + e.getMessage());
        } catch (InvalidMoveException e) {
            clientView.showMessage(e.getMessage());
        }
    }

    //Outgoing messages. Updates state
    @Override
    public void doHello() {
        connection.write(ClientMessages.HELLO.constructMessage(CLIENTDESCRIPTION + this.userName));
        this.state = ClientStates.PENDINGHELLO;
    }


    @Override
    public void doLogin(String username) {
        connection.write(ClientMessages.LOGIN.constructMessage(username));
        this.state = ClientStates.PENDINGLOGIN;
    }

    @Override
    public void doGetList() {
        connection.write(ClientMessages.LIST.constructMessage());
        this.state = ClientStates.WAITINGONLIST;
    }


    @Override
    public void doMove(int move) throws InvalidMoveException {
            makeMove(move);
            connection.write(ClientMessages.MOVE.constructMessage(String.valueOf(move)));
            this.ourLastMove = new int[]{move};
            this.state = ClientStates.AWAITMOVERESPONSE;
    }


    @Override
    public void doMove(int move, int move2) throws InvalidMoveException {
            makeMove(move, move2);
            connection.write(ClientMessages.MOVE.constructMessage(String.valueOf(move), String.valueOf(move2)));
            this.ourLastMove = new int[]{move, move2};
            this.state = ClientStates.AWAITMOVERESPONSE;
    }

    @Override
    public void doEnterQueue()  {
        connection.write(ClientMessages.QUEUE.constructMessage());
        this.state = ClientStates.INQUEUE;
    }

    //Parsing response
    //HELLO
    private void handleHello(String[] aguments) throws ProtocolException {
        switch (aguments.length) {
            case 1:
                throw new ProtocolException("No server name given");
            case 2:
                this.chatEnabled = aguments[1].equals(ProtocolMessages.Messages.CHAT.toString());
                this.rankEnabled = aguments[1].equals(ProtocolMessages.Messages.RANK.toString());
                break;
            case 3:
                this.chatEnabled = aguments[1].equals(ProtocolMessages.Messages.CHAT.toString()) ||
                        aguments[2].equals(ProtocolMessages.Messages.CHAT.toString());
                this.rankEnabled = aguments[1].equals(ProtocolMessages.Messages.RANK.toString()) ||
                        aguments[2].equals(ProtocolMessages.Messages.RANK.toString());
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
    }

    //LIST
    private String parseListResponse(String[] arguments) throws ProtocolException {
        String ret = "List of logged in users: \n";
        for(int i = 1; i < arguments.length; i++) {
            ret = ret.concat(arguments[i] + '\n');
        }
        return ret;
    }

    //NEWGAME
    private void createNewBoard(String[] arguments) throws NumberFormatException{
        int[] boardState = new int[arguments.length-1]; //TODO add check on valid number of squares
        for(int i = 1; i < arguments.length; i++) {
            boardState[i-1] = Integer.parseInt(arguments[i]);
        }
        this.board = new ClientBoard(boardState);
    }

    //MOVE (1st)
    private void checkMoveResponse(String[] arguments) throws NumberFormatException, ProtocolException {
        switch (arguments.length) {
            case 1:
                throw new ProtocolException("No move in response");
            case 2:
                if(!Arrays.equals(ourLastMove, new int[]{Integer.parseInt(arguments[1])})) {
                    throw new ProtocolException("Move mismatch. Our move was: " + Arrays.toString(ourLastMove));
                }
                break;
            case 3:
                if(!Arrays.equals(ourLastMove, new int[]{Integer.parseInt(arguments[1], Integer.parseInt(arguments[2]))})) {
                    throw new ProtocolException("Move mismatch. Our move was: " + Arrays.toString(ourLastMove));
                }
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
        this.state = ClientStates.AWAITNGTHEIRMOVE;
    }

    //MOVE (2nd)
    private void makeTheirMove(String[] arguments) throws NumberFormatException, ProtocolException, InvalidMoveException {
        switch (arguments.length) {
            case 1:
                throw new ProtocolException("No move in response of their move");
            case 2:
                board.makeMove(Integer.parseInt(arguments[1]));
                break;
            case 3:
                board.makeMove(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
        this.state = ClientStates.INGAME;
    }

    private String handleGameOver(String[] arguments) throws IllegalArgumentException, ProtocolException {
        String ret = "The game is over. \nReason: ";
        if(arguments.length != 3) throw new ProtocolException("Invalid number of arguments");
        switch (ServerMessages.GameOverReasons.valueOf(arguments[1])) {
            case VICTORY:
                ret = ret.concat("VICTORY");
                if(arguments[2].equals(this.userName)) {
                    ret += "Congratulations YOU WON! :)";
                } else {
                    ret = ret.concat("You lost. You donkey. I will disown you. You are not worthy >:(");
                }
                break;
            case DRAW:
                ret = ret.concat("DRAW \nGood game though! :|");
                break;
            case DISCONNECT:
                ret = ret.concat("Your opponent left because he/she could not stand your face ;)");
                break;
        }
        this.state = ClientStates.IDLE;
        return ret;
    }


    public void createConnection() throws IOException {
        createConnection(this.ip, this.port, this.userName);
    }

    private void createConnection(InetAddress ip, Integer port, String userName) throws IOException {
        clearConnection();

        while (serverSocket == null) {
            serverSocket = new Socket(ip, port);
        }

        connection = new ConnectionHandler(this, serverSocket);
        (new Thread(connection)).start();

        clientView.showMessage("Connected to server!");
        doHello();
    }

    private void makeMove(int move) throws InvalidMoveException {
        board.makeMove(move);
    }

    private void makeMove(int move, int move2) throws InvalidMoveException {
        board.makeMove(move, move2);
    }

    @Override
    public void handleShutdown() {
        clientView.showMessage("Server shutdown"); //TODO dit mooi maken
    }

    public void clearConnection() {
        serverSocket = null;
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


    public ClientStates getState() {
        return this.state;
    }
}
