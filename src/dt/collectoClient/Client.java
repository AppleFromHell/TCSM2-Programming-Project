package dt.collectoClient;

import dt.ai.AI;
import dt.ai.AITypes;
import dt.collectoClient.GUI.ClientGUI;
import dt.exceptions.CommandException;
import dt.exceptions.InvalidMoveException;
import dt.exceptions.UnexpectedResponseException;
import dt.exceptions.UserExit;
import dt.model.Board;
import dt.model.ClientBoard;
import dt.peer.NetworkEntity;
import dt.peer.SocketHandler;
import dt.protocol.ClientMessages;
import dt.protocol.ClientProtocol;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.util.Move;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the interaction with the {@link ClientBoard}, the {@link ClientView} and the {@link SocketHandler}.
 * It is an endpoint for the messages from the server.
 * The view is also started from this class.
 * This class is passive; it waits for input from either the {@link SocketHandler} or the {@link ClientView}
 *
 * @author Emiel Rous and Wouter Koning
 */
public class Client implements ClientProtocol, NetworkEntity {

    private final String CLIENTDESCRIPTION = "Client By: Emiel";
    private Socket serverSocket;
    private SocketHandler socketHandler;
    private String userName;
    private Integer port;
    private ClientView clientView;
    private ClientBoard board;
    private InetAddress ip;
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
        this.rankEnabled = true;
        this.cryptEnabled = false;
        this.authEnabled = false;
        this.debug = true;
    }

    /**
     * @param args can be: ( ip + port) (+ gui) (+debug)
     */
    public static void main(String[] args) {
        Client client = new Client();
        if (args.length > 1) {
            if (Arrays.asList(args).contains("gui")) {
                client.clientView = new ClientGUI(client);
            }
            if (Arrays.asList(args).contains("debug")) {
                client.setDebug(true);
            }
            try {
                client.ip = InetAddress.getByName(args[0]);
                client.port = Integer.parseInt(args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.start();
    }

    /**
     * Starts the view in a new {@link Thread}.
     * The actions initiated from the main thread stop here.
     * The rest is handeld by the {@link SocketHandler} and the {@link ClientView} {@link Thread}.
     *
     * @requires {@link ClientView} should be initiated
     * @ensures {@link ClientView} is started in a separate thread
     */
    public void start() {
        this.state = ClientStates.STARTINGUP;
        new Thread(clientView).start();
    }

    /**
     * This is the main endpoint for the {@link SocketHandler}. Any message from the server passes through this method.
     * From here methods from the {@link ClientView} and the {@link ClientBoard} are called
     *
     * @param msg the raw message from the server
     * @requires message should not be null
     * @ensures The state is changed for the following {@link ServerMessages},{@link ServerMessages#HELLO},{@link ServerMessages#LOGIN},{@link ServerMessages#NEWGAME},{@link ServerMessages#MOVE},{@link ServerMessages#GAMEOVER}
     */
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
                            this.clientView.notify(); //The clientView can prompt username
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case LOGIN:
                    if (this.state == ClientStates.PENDINGLOGIN) {
                        this.state = ClientStates.LOGGEDIN;
                        this.clientView.showMessage("Successfully logged in!");
                        this.clientView.showMessage("Main menu. Enter a command.");
                        synchronized (clientView) {
                            this.clientView.notifyAll();//The ClientView can continue to main menu
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case ALREADYLOGGEDIN:
                    if (this.state == ClientStates.PENDINGLOGIN) {
                        this.clientView.showMessage("Username already logged in, try again");
                        synchronized (clientView) {
                            this.clientView.notifyAll();//THe clientView has to ask for username again
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case LIST:
                    this.clientView.displayList(parseListResponse(arguments));
                    break;
                case NEWGAME:
                    if (this.state == ClientStates.INQUEUE) {
                        this.createNewBoard(arguments);
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    this.clientView.showBoard(this.board);
                    this.clientView.showMessage("Your move: ");

                    break;
                case MOVE:
                    if (this.state == ClientStates.WAITVERIFYMOVE) {
                        if (this.verifyOurMove(this.createMove(arguments))) {
                            this.moveConfirmed = true;
                            this.notify(); // Notify client that move was verified
                        } else {
                            throw new ProtocolException("Our move could not be verfied. Our last move: " + this.ourLastMove + "server: " + this.createMove(arguments));
                        }
                    } else if (this.state == ClientStates.WAITTHEIRMOVE) {
                        try {
                            this.makeTheirMove(this.createMove(arguments));
                        } catch (InvalidMoveException e) {
                            throw new ProtocolException("Server move was invalid. Our board said: " + e.getMessage());
                        }
                    } else {
                        throw new UnexpectedResponseException();
                    }
                    break;
                case GAMEOVER:
                    this.clientView.showMessage(this.handleGameOver(arguments));
                    this.clientView.showMessage("Main menu. Enter a command.");
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
                case RANK:
                    this.handleRanking(arguments);
                    break;
            }
        } catch (UnexpectedResponseException e) {
            clientView.showMessage("Unexpected response: " + msg);
        } catch (NumberFormatException | ProtocolException e) {
            clientView.showMessage("Invalid response from server. Response: " + msg);
            if (!e.getMessage().equals("")) clientView.showMessage("Reason: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            clientView.showMessage("Unkown command from server. Response: " + msg);
        } catch (CommandException e) {
            clientView.showMessage(e.getMessage());
        }
    }

    /**
     * Sends a hello to the server.
     *
     * @requires the state to be {@link ClientStates#STARTINGUP}
     * @ensures the state to become {@link ClientStates#PENDINGHELLO}
     */
    @Override
    public void doHello() {
        List<String> extensions = new ArrayList<>();
        extensions.add(userName);
        if (this.chatEnabled) extensions.add(ProtocolMessages.Messages.CHAT.name());
        if (this.authEnabled) extensions.add(ProtocolMessages.Messages.AUTH.name());
        if (this.cryptEnabled) extensions.add(ProtocolMessages.Messages.CRYPT.name());
        if (this.rankEnabled) extensions.add(ProtocolMessages.Messages.RANK.name());

        socketHandler.write(ClientMessages.HELLO.constructMessage(extensions));
        this.state = ClientStates.PENDINGHELLO;
    }

    /**
     * Sends a login request to the server
     *
     * @param username
     * @requires the state to be {@link ClientStates#HELLOED}
     * @requires username should not be null
     * @ensures the state to become {@link ClientStates#PENDINGLOGIN}
     */
    @Override
    public void doLogin(String username) {
        socketHandler.write(ClientMessages.LOGIN.constructMessage(username));
        this.state = ClientStates.PENDINGLOGIN;
    }

    /**
     * Request a list of users from the server
     */
    @Override
    public void doGetList() {
        socketHandler.write(ClientMessages.LIST.constructMessage());
    }

    /**
     * Send a chat message to the server
     *
     * @param message
     */
    @Override
    public void doSendChat(String message) {
        socketHandler.write(ClientMessages.CHAT.constructMessage(message));
    }

    /**
     * Send a whisper to a player in the server
     *
     * @param recipient
     * @param message
     */
    @Override
    public void doSendWhisper(String recipient, String message) {
        socketHandler.write(ClientMessages.WHISPER.constructMessage(recipient, message));
    }

    /**
     * Enter the server queue
     *
     * @requires the user should be in the main menu
     * @ensures the state is changed to {@link ClientStates#INQUEUE}
     */
    @Override
    public void doEnterQueue() {
        socketHandler.write(ClientMessages.QUEUE.constructMessage());
        clientView.showMessage("Entered queue");
        this.state = ClientStates.INQUEUE;
    }

    /**
     * Request a ranking from the server
     */
    @Override
    public void doGetRanking() {
        socketHandler.write(ClientMessages.RANK.constructMessage());
    }

    /**
     * Handle the response of the ranking. Parses the ranking to a string and passes it to the {@link ClientView}
     *
     * @param arguments split message from the server
     * @throws ArrayIndexOutOfBoundsException when the arguments length is 0, which is an invalid response from the server
     * @requires the arguments to be the raw split response from the server
     */
    public void handleRanking(String[] arguments) throws ArrayIndexOutOfBoundsException {
        StringBuilder rank = new StringBuilder("Ranking: \nName:            Score:\n");
        for (int i = 1; i < arguments.length; i++) {
            String[] list = arguments[i].split(" ");
            rank.append(String.format("%-20s %20s", list[0], list[1])).append('\n');
        }
        clientView.showRank(rank.toString());
    }

    /**
     * Handles the {@link ServerMessages#HELLO} from the server.
     *
     * @param arguments
     * @throws ProtocolException
     * @requires the arguments to be the raw split arguments from the server
     * @ensures the state to be changed to {@link ClientStates#HELLOED}
     */
    private void handleHello(String[] arguments) throws ProtocolException {
        if (arguments.length == 1) throw new ProtocolException("Not enough arguments");
        for (int i = 1; i < arguments.length; i++) {
            String arg = arguments[i];
            chatEnabled = arg.equals(ProtocolMessages.Messages.CHAT.toString());
            rankEnabled = arg.equals(ProtocolMessages.Messages.RANK.toString());
            cryptEnabled = arg.equals(ProtocolMessages.Messages.CRYPT.toString());
            authEnabled = arg.equals(ProtocolMessages.Messages.AUTH.toString());
        }
        this.serverName = arguments[1];
        this.clientView.showMessage("Handshake successful! Connected to server: " + serverName);
        this.state = ClientStates.HELLOED;
    }

    /**
     * Parses the {@link ServerMessages#LIST} returned by the server to a list without the {@link ServerMessages}
     *
     * @param arguments
     * @return
     * @requires the arguments to be the raw split arguments from the server
     * @ensures that the list only contains the arguments from the server
     */
    private String[] parseListResponse(String[] arguments) {
        String[] ret = new String[arguments.length - 1];
        if (arguments.length - 1 >= 0) {
            System.arraycopy(arguments, 1, ret, 0, arguments.length - 1);
        }
        return ret;
    }

    /**
     * Creates a new board based on arguments and sets the turn of the right player
     *
     * @param arguments
     * @throws NumberFormatException
     * @requires the arguments to be a valid boardState
     * @requires the arguments to be the raw split arguments from the server
     * @ensures the {@link ClientBoard} is filled correctly
     * @ensures the state to be {@link ClientStates#WAITOURMOVE} if we start
     * @ensures the state to be {@link ClientStates#WAITTHEIRMOVE} if they start
     */
    private synchronized void createNewBoard(String[] arguments) throws NumberFormatException {
        int[] boardState = new int[arguments.length - 3];
        for (int i = 1; i < arguments.length - 2; i++) {
            boardState[i - 1] = Integer.parseInt(arguments[i]);
        }
        String player1 = arguments[arguments.length - 2];
        String player2 = arguments[arguments.length - 1];
        if (this.userName.equals(player1)) {
            this.state = ClientStates.WAITOURMOVE;
            if (debug) this.clientView.showMessage("createNewBoard()/if: " + this.state);
            this.myTurn = true;
            clientView.showMessage("Playing against: " + player2);
            clientView.showMessage("You start");
            if (this.ai != null) clientView.showMessage("Type: m to start");
        } else {
            this.state = ClientStates.WAITTHEIRMOVE;
            if (debug) this.clientView.showMessage("createNewBoard()/else: " + this.state);
            this.myTurn = false;
            clientView.showMessage("Playing against: " + player1);
            clientView.showMessage("Waiting on their move");
        }
        this.board = new ClientBoard(boardState);
    }

    /**
     * Parses the raw arguments from the server into a {@link Move}
     *
     * @param arguments
     * @return A move based on the arguments
     * @throws NumberFormatException
     * @throws ProtocolException
     * @requires the arguments to be the raw split arguments from the server
     */
    public synchronized Move createMove(String[] arguments) throws NumberFormatException, ProtocolException {
        Move move = null;
        switch (arguments.length) {
            case 1:
                throw new ProtocolException("Not enough arguments");
            case 2:
                move = new Move(Integer.parseInt(arguments[1]));
                break;
            case 3:
                move = new Move(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
        return move;
    }

    /**
     * Checks if the move returned by the server is the same as our last {@link Move}
     *
     * @param move
     * @return
     * @requires Our last {@link Move} should not be null
     */
    public synchronized boolean verifyOurMove(Move move) {
        return this.ourLastMove.equals(move);
    }

    /**
     * Finds a move and plays it on the board
     *
     * @throws InvalidMoveException
     * @throws ProtocolException
     * @requires ai should not be null
     */
    public synchronized void doAIMove() throws InvalidMoveException, ProtocolException {
        this.doMove(this.ai.findBestMove(this.board));
    }

    /**
     * Send a {@link Move} to the server.
     * Wait for a response, then place it on the board
     *
     * @param move
     * @throws InvalidMoveException
     * @throws ProtocolException
     * @requires {@link Move} to be valid
     * @requires the server to respond with a move at some point
     * @ensures the state to stay the same if the move is invalid
     * @ensures the state to be changed to {@link ClientStates#WAITTHEIRMOVE} if the move is confirmed
     * @ensures the state to be changed to {@link ClientStates#WAITOURMOVE} if the move could not be confirmed
     * @ensures the {@link} to be played on the board if it is valid and it's verified and myTurn to be true
     * @ensures myTurn to be true if the move could not be confirmed
     */
    @Override
    public synchronized void doMove(Move move) throws InvalidMoveException, ProtocolException {
        if (!this.board.isValidMove(move)) throw new InvalidMoveException("Yer move invalid dipshit");
        this.socketHandler.write(ClientMessages.MOVE.constructMessage(move));
        this.ourLastMove = move;
        this.state = ClientStates.WAITVERIFYMOVE;
        if (debug) this.clientView.showMessage("doMove(): " + this.state);
        this.moveConfirmed = false;

        try {
            this.wait(); //Wait for server to verify and confirm the move
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (moveConfirmed) {
            this.makeMove(move);
            clientView.showMessage("Your move on board was: " + move);
            this.myTurn = false;
            this.state = ClientStates.WAITTHEIRMOVE;
            if (debug) this.clientView.showMessage("doMove()/if: " + this.state);
        } else {
            this.myTurn = true;
            this.state = ClientStates.WAITOURMOVE;
            if (debug) this.clientView.showMessage("doMove()/else: " + this.state);
            throw new ProtocolException("Our move could not be verified. Our move was: " + this.ourLastMove);
        }
    }

    /**
     * Make the {@link Move} that the opponnent reponsds with
     *
     * @param move
     * @throws InvalidMoveException
     * @requires {@link Move} their move to be valid
     * @ensures the state to be changed to {@link ClientStates#WAITOURMOVE}
     * @ensures myTurn to be true
     */
    public synchronized void makeTheirMove(Move move) throws InvalidMoveException {
        this.makeMove(move);
        this.clientView.showMessage("Their move on board was: " + move);
        this.clientView.showMessage("Your move: ");
        this.state = ClientStates.WAITOURMOVE;
        if (debug) this.clientView.showMessage("makeTheirMove(): " + this.state);
        this.myTurn = true;
    }

    /**
     * Do a move on the board
     *
     * @param move
     * @throws InvalidMoveException
     * @requires {@link Move} should be valid
     */
    private synchronized void makeMove(Move move) throws InvalidMoveException {
        board.makeMove(move);
        this.clientView.showBoard(this.board);
    }

    /**
     * Handles a {@link ServerMessages#GAMEOVER} response from the server
     *
     * @param arguments
     * @return
     * @throws IllegalArgumentException
     * @throws ProtocolException
     * @requires the arguments to be the raw split response from the server
     * @ensures the game board is cleared
     * @ensures the board will be null
     * @ensures the state to be changed to {@link ClientStates#GAMEOVER}
     */
    private synchronized String handleGameOver(String[] arguments) throws IllegalArgumentException, ProtocolException {
        String ret = "The game is over. \nReason: ";
        if (arguments.length != 3) throw new ProtocolException("Invalid number of arguments");
        switch (ServerMessages.GameOverReasons.valueOf(arguments[1])) {
            case VICTORY:
                ret = ret.concat("VICTORY ");
                if (arguments[2].equals(this.userName)) {
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

        if (this.clientView instanceof ClientGUI) {
            ClientGUI gui = (ClientGUI) clientView;
            gui.gameOver(ret);
        }


        this.board = null;
        this.state = ClientStates.GAMEOVER;
        if (debug) this.clientView.showMessage("gameOver(): " + this.state);
        return ret;
    }

    /**
     * Show a hint in the {@link ClientView} based on a random hint provied by the {@link ClientBoard}
     */
    public void provideHint() {
        clientView.showHint(this.board.getAHint().toString());
    }


    /**
     * Public create connection method
     *
     * @requires ip and port should not be null
     */
    public void createConnection() throws IOException {
        createConnection(this.ip, this.port);
    }

    /**
     * Create a connection to a server given the arguments.
     *
     * @param ip
     * @param port
     * @throws IOException thrown if the port or ip is invalid
     * @requires ip and port should not be null
     * @ensures a connection is made if the ip and port are valid
     */
    private void createConnection(InetAddress ip, Integer port) throws IOException {
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


    /**
     * Handles the shutdown if the connection between the client and the server is disconnected
     *
     * @param clientShutdown is true the disconnect happened on our side
     * @ensures the user can get back into the beginning flow of starting a connection
     */
    @Override
    public void handlePeerShutdown(boolean clientShutdown) {
        if (!clientShutdown) {
            this.clientView.showMessage("Server shutdown");

            try {
                if (this.clientView.reconnect()) {
                    new Thread(clientView).start();
                }

            } catch (UserExit e) {
                this.shutDown();
            }
        } else {
            this.shutDown();
        }
    }

    /**
     * Shutdown the client neatly
     */
    @Override
    public void shutDown() {
        clearConnection();
        if (socketHandler != null) socketHandler.shutDown();
        clientView.showMessage("See you next time!");
        System.exit(69);
    }

    /**
     * Clears the connection
     */
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

    public void setDebug(Boolean state) {
        this.debug = state;
    }

    public AI getAi() {
        return ai;
    }

    public void setAI(AITypes type) {
        this.ai = type.getAIClass();
    }
}
