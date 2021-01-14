package dt.collectoClient;

import java.io.*;

import dt.exceptions.InvalidMoveException;
import dt.exceptions.UnexpectedResponseException;
import dt.exceptions.UserExit;
import dt.model.board.ClientBoard;
import dt.peer.ConnectionHandler;
import dt.peer.NetworkEntity;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.util.Move;

import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;

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
    private boolean cryptEnabled;
    private boolean authEnabled;
    private ClientStates state;
    private Move ourLastMove;


    public Client() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard();
        this.userName = null;
        this.port = null;
        this.chatEnabled = false;
        this.rankEnabled = false;
        this.cryptEnabled = false;
        this.authEnabled = false;
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
                        this.clientView.displayList(parseListResponse(arguments));
                    }
                    synchronized (clientView) {
                        this.clientView.notify();
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
                    this.makeTheirMove(arguments);

                    if (this.state == ClientStates.AWAITMOVERESPONSE) {
                        this.checkMoveResponse(arguments);
                    } else if (this.state == ClientStates.AWAITNGTHEIRMOVE) {
                        try {
                            this.makeTheirMove(arguments);
                        } catch (InvalidMoveException e) {
                            throw new InvalidMoveException("The server move was invalid");
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case GAMEOVER:
                    this.clientView.showMessage(this.handleGameOver(arguments));
                    break;
                case ERROR:
                    clientView.showMessage("Server threw error: " + msg);
                    break;

            }
        }catch (UnexpectedResponseException e) {
            clientView.showMessage("Unexpected response: " + msg);
        } catch (NumberFormatException  | ProtocolException e) {
            clientView.showMessage("Invalid response from server. Response: " + msg);
            if (!e.getMessage().equals("")) clientView.showMessage("Reason: " + e.getMessage());
        }catch (IllegalArgumentException e) {
                clientView.showMessage("Unkown command from server. Response: " + msg);
        } catch (InvalidMoveException e) {
            clientView.showMessage(e.getMessage());
        }
    }

    //Outgoing messages. Updates state
    @Override
    public void doHello() {
        connection.write(ClientMessages.HELLO.constructMessage(CLIENTDESCRIPTION));
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
        try {
            clientView.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void doMove(Move move) throws InvalidMoveException {
            makeMove(move);
            connection.write(ClientMessages.MOVE.constructMessage(move));
            this.ourLastMove = move;
            this.state = ClientStates.AWAITMOVERESPONSE;
    }


    @Override
    public void doEnterQueue()  {
        connection.write(ClientMessages.QUEUE.constructMessage());
        this.state = ClientStates.INQUEUE;
    }

    //Parsing response
    //HELLO
    private void handleHello(String[] arguments)  {

        for(int i = 1; i < arguments.length; i++) {
            String arg = arguments[i];
            chatEnabled = arg.equals(ProtocolMessages.Messages.CHAT.toString());
            rankEnabled = arg.equals(ProtocolMessages.Messages.RANK.toString());
            cryptEnabled = arg.equals(ProtocolMessages.Messages.CRYPT.toString());
            authEnabled = arg.equals(ProtocolMessages.Messages.AUTH.toString());
        }
    }

    //LIST
    private String[] parseListResponse(String[] arguments) {
        String[] ret = new String[arguments.length-1];
        for(int i = 1; i < arguments.length; i++) {
            ret[i-1] =arguments[i];
        }
        return ret;
    }

    //NEWGAME
    private void createNewBoard(String[] arguments) throws NumberFormatException {
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
                if(ourLastMove.equals(new Move(Integer.parseInt(arguments[1])))) {
                    throw new ProtocolException("Move mismatch. Our move was: " + ourLastMove.toString());
                }
                break;
            case 3:
                if(ourLastMove.equals(new Move(Integer.parseInt(arguments[1], Integer.parseInt(arguments[2]))))) {
                    throw new ProtocolException("Move mismatch. Our move was: " + ourLastMove.toString());
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
                makeMove(new Move(Integer.parseInt(arguments[1])));
                break;
            case 3:
                makeMove(new Move(Integer.parseInt(arguments[1]),Integer.parseInt(arguments[1])));
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

        this.connection = new ConnectionHandler(this, serverSocket);
        (new Thread(connection)).start();

        clientView.showMessage("Connected to server!");
        doHello();
    }

    private void makeMove(Move move) throws InvalidMoveException {
        //board.makeMove(move);
    }

    @Override
    public void handlePeerShutdown() {
        this.clientView.showMessage("Server shutdown");
        try {
            this.serverSocket.close();
            this.clientView.reconnect();
        } catch (UserExit | IOException e) {
            this.shutDown();
        }
    }


    @Override
    public void shutDown() {
        clearConnection();
        connection.shutDown();
        clientView.showMessage("See you next time!");
        System.exit(0);
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
