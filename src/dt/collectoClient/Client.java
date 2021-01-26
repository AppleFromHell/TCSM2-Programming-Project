package dt.collectoClient;

import java.io.*;

import dt.ai.AI;
import dt.ai.AITypes;
import dt.collectoClient.GUI.ClientGUI;
import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UnexpectedResponseException;
import dt.exceptions.UserExit;
import dt.model.Board;
import dt.model.ClientBoard;
import dt.peer.SocketHandler;
import dt.peer.NetworkEntity;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.util.Move;

import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class Client implements ClientProtocol, NetworkEntity {
    //TODO client states doesn't work perfectly

    private Socket serverSocket;
    private SocketHandler socketHandler;
    private String userName;
    private Integer port;
    private ClientView clientView;
    private ClientBoard board;
    private InetAddress ip;
    private final String CLIENTDESCRIPTION = "Client By: Emiel";
    private boolean chatEnabled;
    private boolean rankEnabled;
    private boolean cryptEnabled;
    private boolean authEnabled;
    private ClientStates state;
    private Move ourLastMove;
    private boolean moveConfirmed;
    private boolean debug;

    private AI ai;

    private String serverName;
    private boolean myTurn;

    public Client() {
        this.clientView = new ClientTUI(this);
        this.board = new ClientBoard();
        this.userName = null;
        this.ip = null;
        this.port = null;
        this.chatEnabled = true;
        this.rankEnabled = false;
        this.cryptEnabled = false;
        this.authEnabled = false;
        this.debug = true;
    }

    public static void main(String[] args) {
        Client client = new Client();
        if(args.length > 1) {
            if(Arrays.asList(args).contains("gui")) {
                client.clientView = new ClientGUI(client);
            }
            if(Arrays.asList(args).contains("debug")) {
                client.setDebug(true);
            }
            try {
                client.ip = InetAddress.getByName(args[0]);
                client.port = Integer.parseInt(args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(args.length > 2) {

        }
        client.start();
    }


    public void start() {
        this.state = ClientStates.STARTINGUP;
        new Thread(clientView).start();
    }

    public void setDebug(Boolean state){
        this.debug = state;
    }

    public void writeMessage(String msg) {
        socketHandler.write(msg);
    }

    @Override
    public synchronized void handleMessage(String msg) {
        String[] arguments = msg.split(ProtocolMessages.delimiter);
        String keyWord = arguments[0];
        try {
            switch (ServerMessages.valueOf(keyWord)) {
                case HELLO:
                    if (this.state == ClientStates.PENDINGHELLO) {
                        this.handleHello(arguments);
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
                        this.clientView.displayList(parseListResponse(arguments));
                    break;
                case NEWGAME:
                    if(this.state == ClientStates.INQUEUE) { //TODO Sometimes when creating a new game, it doesn't expect the NEWGAME response from the server
                        this.createNewBoard(arguments);
                    }   else {
                        throw new UnexpectedResponseException();
                    }
                    this.clientView.showBoard(this.board);
                    break;
                case MOVE:
                    if (this.state == ClientStates.WAITVERIFYMOVE) {
                        if(this.verifyOurMove(this.createMove(arguments))) {
                            this.moveConfirmed = true;
                            this.notify(); // Notify client that move was verified
                        } else {
                            throw new ProtocolException("Our move could not be verfied. Our last move: " + this.ourLastMove + "server: "  + this.createMove(arguments));
                        }
                    } else if (this.state == ClientStates.WAITTHEIRMOVE) {
                            try {
                                this.makeTheirMove(this.createMove(arguments));
                            } catch (InvalidMoveException e ){
                                throw new ProtocolException("Server move was invalid. Our board said: "+ e.getMessage());
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case GAMEOVER:
                    this.clientView.showMessage(this.handleGameOver(arguments)); //TODO method maken voor gui met deze dingen
                    break;
                case ERROR:
                    clientView.showMessage("Server threw error: " + msg);
                    switch (this.state) {
                        case WAITOURMOVE:
                            break;
                        case WAITVERIFYMOVE:
                            this.notify();
                            this.state = ClientStates.WAITOURMOVE;
                            this.myTurn = true;
                            break;
                        case WAITTHEIRMOVE:
                            break;
                    }
                    break;
                case CHAT:
                    String[] splitChat = msg.split(ProtocolMessages.delimiter, 3);
                    clientView.displayChatMessage(splitChat[1] + ": " + splitChat[2]);
                    break;
                case WHISPER:
                    String[] splitWhisper = msg.split(ProtocolMessages.delimiter, 3);
                    clientView.displayChatMessage(splitWhisper[1] + " whispers: " + splitWhisper[2]);
                    break;
                case CANNOTWHISPER:
                    throw new CommandException(arguments[1] + "Cannot receive whispers");
            }
        } catch (UnexpectedResponseException e) {
            clientView.showMessage("Unexpected response: " + msg);
        } catch (NumberFormatException  | ProtocolException e) {
            clientView.showMessage("Invalid response from server. Response: " + msg);
            if (!e.getMessage().equals("")) clientView.showMessage("Reason: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            clientView.showMessage("Unkown command from server. Response: " + msg);
        } catch ( CommandException e) {
            clientView.showMessage(e.getMessage());
        }
    }

    //Outgoing messages. Updates state
    @Override
    public void doHello() {
        List<String> extensions = new ArrayList<>();
        extensions.add(userName);
        if(this.chatEnabled) extensions.add(ProtocolMessages.Messages.CHAT.name());
        if(this.authEnabled) extensions.add(ProtocolMessages.Messages.AUTH.name());
        if(this.cryptEnabled) extensions.add(ProtocolMessages.Messages.CRYPT.name());
        if(this.rankEnabled) extensions.add(ProtocolMessages.Messages.RANK.name());

        socketHandler.write(ClientMessages.HELLO.constructMessage(extensions));
        this.state = ClientStates.PENDINGHELLO;
    }


    @Override
    public void doLogin(String username) {
        socketHandler.write(ClientMessages.LOGIN.constructMessage(username));
        this.state = ClientStates.PENDINGLOGIN;
    }

    @Override
    public void doGetList() {
        socketHandler.write(ClientMessages.LIST.constructMessage());
    }

    public void doSendChat(String message){
        socketHandler.write(ClientMessages.CHAT.constructMessage(message));
    }

    public void doSendWhisper(String recipient, String message){
        socketHandler.write(ClientMessages.WHISPER.constructMessage(recipient, message));
    }
    @Override
    public void doEnterQueue()  {
        socketHandler.write(ClientMessages.QUEUE.constructMessage());
        clientView.showMessage("Entered queue");
        this.state = ClientStates.INQUEUE;
    }

    //Parsing response
    //HELLO
    private void handleHello(String[] arguments) throws ProtocolException {
        if(arguments.length == 1) throw new ProtocolException("Not enough arguments");
        for(int i = 1; i < arguments.length; i++) {
            String arg = arguments[i];
            chatEnabled = arg.equals(ProtocolMessages.Messages.CHAT.toString());
            rankEnabled = arg.equals(ProtocolMessages.Messages.RANK.toString());
            cryptEnabled = arg.equals(ProtocolMessages.Messages.CRYPT.toString());
            authEnabled = arg.equals(ProtocolMessages.Messages.AUTH.toString());
        }
        this.serverName =  arguments[1];
        this.clientView.showMessage("Handshake successful! Connected to server: " +serverName);
        this.state = ClientStates.HELLOED;
    }

    //LIST
    private String[] parseListResponse(String[] arguments) {
        String[] ret = new String[arguments.length-1];
        if (arguments.length - 1 >= 0) {
            System.arraycopy(arguments, 1, ret, 0, arguments.length - 1);
        }
        return ret;
    }

    //NEWGAME
    private synchronized void createNewBoard(String[] arguments) throws NumberFormatException {
        int[] boardState = new int[arguments.length-3];
        for(int i = 1; i < arguments.length-2; i++) {
            boardState[i-1] = Integer.parseInt(arguments[i]);
        }
        String player1 = arguments[arguments.length - 2];
        String player2 = arguments[arguments.length - 1];
        if(this.userName.equals(player1)){
            this.state = ClientStates.WAITOURMOVE;
            if(debug) this.clientView.showMessage("createNewBoard()/if: " + this.state);
            this.myTurn = true;
            clientView.showMessage("Playing against: " + player2);
            clientView.showMessage("You start");
            if(this.ai != null)  clientView.showMessage("Type: m to start");
        } else {
            this.state = ClientStates.WAITTHEIRMOVE;
            if(debug) this.clientView.showMessage("createNewBoard()/else: " + this.state);
            this.myTurn = false;
            clientView.showMessage("Playing against: " + player1);
            clientView.showMessage("Waiting on their move");
        }
        this.board = new ClientBoard(boardState);
    }

    public synchronized Move createMove(String[] arguments) throws NumberFormatException, ProtocolException {
        Move move = null;
        switch (arguments.length) {
            case 1:
                throw new ProtocolException("Not enough arguments");
            case 2:
                move = new Move(Integer.parseInt(arguments[1]));
                break;
            case 3:
                move = new Move(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[1]));
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
        return move;
    }

    public synchronized boolean verifyOurMove(Move move) {
        return this.ourLastMove.equals(move);
    }

    public synchronized void doAIMove() throws InvalidMoveException, ProtocolException {
        this.doMove(this.ai.findBestMove(this.board));
    }

    @Override
    public synchronized void doMove(Move move) throws InvalidMoveException, ProtocolException {
        if(!this.board.isValidMove(move)) throw new InvalidMoveException("Yer move invalid dipshit");
        this.socketHandler.write(ClientMessages.MOVE.constructMessage(move));
        this.ourLastMove = move;
        this.state = ClientStates.WAITVERIFYMOVE;
        if(debug) this.clientView.showMessage("doMove(): " + this.state);
        this.moveConfirmed = false;

        try {
            this.wait(); //Wait for server to verify and confirm the move
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(moveConfirmed) {
            this.makeMove(move);
            clientView.showMessage("Your move on board was: " + move);
            this.myTurn = false;
            this.state = ClientStates.WAITTHEIRMOVE;
            if(debug) this.clientView.showMessage("doMove()/if: " + this.state);
        } else {
            this.myTurn = true;
            this.state = ClientStates.WAITOURMOVE;
            if(debug) this.clientView.showMessage("doMove()/else: " + this.state);
            throw new ProtocolException("Our move could not be verified. Our move was: " + this.ourLastMove);
        }
    }

    public synchronized void makeTheirMove(Move move) throws InvalidMoveException {
        this.makeMove(move);
        this.clientView.showMessage("Their move on board was: "+ move);
        this.state = ClientStates.WAITOURMOVE;
        if(debug) this.clientView.showMessage("makeTheirMove(): " + this.state);
        this.myTurn = true;
    }

    private synchronized void makeMove(Move move) throws InvalidMoveException {
        board.makeMove(move);
        this.clientView.showBoard(this.board);
    }

    private synchronized String handleGameOver(String[] arguments) throws IllegalArgumentException, ProtocolException {
        String ret = "The game is over. \nReason: ";
        if(arguments.length != 3) throw new ProtocolException("Invalid number of arguments");
        switch (ServerMessages.GameOverReasons.valueOf(arguments[1])) {
            case VICTORY:
                ret = ret.concat("VICTORY ");
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
                ret = ret.concat("Your opponent left because he/she could not stand your face ;) (but you won so that's nice)");
                break;
        }

        this.clientView.clearBoard();
        this.board = null;
        this.state = ClientStates.GAMEOVER;
        if(debug) this.clientView.showMessage("gameOver(): " + this.state);
        return ret;
    }

    public void provideHint() {
        clientView.showMessage(this.board.getAHint().toString());
    }



    public void createConnection() throws IOException {
        createConnection(this.ip, this.port, this.userName);
    }

    private void createConnection(InetAddress ip, Integer port, String userName) throws IOException {
        clearConnection();

        while (serverSocket == null) {
            serverSocket = new Socket(ip, port);
        }

        this.socketHandler = new SocketHandler(this, serverSocket, this.CLIENTDESCRIPTION);
        socketHandler.setDebug(this.debug);

        new Thread(socketHandler).start();

        clientView.showMessage("Connected to server!");
        this.chatEnabled = true;
        doHello();
    }

    @Override
    public void handlePeerShutdown(boolean clientShutdown) {
        if(!clientShutdown) {
            this.clientView.showMessage("Server shutdown");

            try {
                this.clientView.reconnect();

            } catch (UserExit e) {
                this.shutDown();
            }
        } else {
            this.shutDown();
        }
    }

    @Override
    public void shutDown() {
        clearConnection();
        if(socketHandler != null) socketHandler.shutDown();
        clientView.showMessage("See you next time!");
        System.exit(69);
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
    public Board getBoard() {
        return this.board;
    }
    public String getServerName() {
        return serverName;
    }
    public boolean isOurTurn() {
        return this.myTurn;
    }

    public AI getAi() {
        return ai;
    }
    public void setAI(AITypes type) {
        this.ai = type.getAIClass();
    }
}
