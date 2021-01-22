package dt.server;

import dt.exceptions.*;
import dt.peer.NetworkEntity;
import dt.peer.SocketHandler;
import dt.protocol.ClientMessages;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.protocol.ServerProtocol;
import dt.util.Move;

import javax.security.auth.login.LoginException;
import java.net.ProtocolException;
import java.net.Socket;

public class ClientHandler implements NetworkEntity, ServerProtocol {
    private final Server server;
    private final GameManager gameManager;
    private Game game;

    private final SocketHandler socketHandler;
    private ClientHandler opponent;
    private final ServerTUI view;

    private String name;
    private String userName;

    private ClientHandlerStates state;

    private boolean myTurn;
    private boolean chatEnabled;
    private boolean rankEnabled;
    private boolean cryptEnabled;
    private boolean authEnabled;

    ClientHandler(Server server, GameManager gameManager, ServerTUI view, Socket socket) {
        this.server = server;
        this.gameManager = gameManager;
        this.socketHandler = new SocketHandler(this, socket, "");
        new Thread(socketHandler).start();
        this.view = view;
        this.game = null;
    }

    @Override
    public void handleMessage(String msg) {
        String[] arguments = msg.split(ProtocolMessages.delimiter);
        String keyWord = arguments[0];

        try {
            switch (ClientMessages.valueOf(keyWord)) {
                case HELLO:
                    this.handleHello(arguments);
                    break;
                case LOGIN:
                    this.handleLogin(arguments);
                    break;
                case LIST:
                    this.handleList();
                    break;
                case QUEUE:
                    if (this.state == ClientHandlerStates.LOGGEDIN ||
                            this.state == ClientHandlerStates.IDLE) {
                        this.handleQueue();
                    } else if (this.state == ClientHandlerStates.INQUEUE) {
                        throw new AlreadyInQueueException("Yer already in a queue");
                    } else {
                        throw new LoginException();
                    }
                    break;
                case MOVE:
                    if (this.state == ClientHandlerStates.INGAME) {
                       this.handleMove(arguments);
                    } else {
                        throw new UnexpectedResponseException("You're not in a game");
                    }
                    break;
                case CHAT:
                    this.handleChat(msg);
                    break;
                case WHISPER:
                    this.handleWhisper(msg);
                    break;
            }
        } catch (IllegalArgumentException e) {
            view.showMessage("["+ this.name + "] unknown response. Response: " + msg);
            if (!e.getMessage().equals("")) view.showMessage("Reason: " + e.getMessage());
            socketHandler.write(ServerMessages.ERROR.constructMessage("Unknown command. Received: " + msg));
        } catch (ProtocolException e) {
            view.showMessage("["+ this.name + "] response invalid. Response: " + msg);
            if (!e.getMessage().equals("")) view.showMessage("Reason: " + e.getMessage());
            socketHandler.write(ServerMessages.ERROR.constructMessage("Invalid command. Received: " + msg));
        } catch (NotYourTurnException e) {
            view.showMessage("["+ this.name + "] tried to move before his turn");
            socketHandler.write(ServerMessages.ERROR.constructMessage("It's not your turn"));
        } catch (LoginException e) {
            view.showMessage("[" + this.name + "] tried to access the Queue without loggin in first");
            socketHandler.write(ServerMessages.ERROR.constructMessage("You need to log in first"));
        } catch (InvalidMoveException e) {
            view.showMessage("[" + this.name + "] tried to make an invalid move");
            socketHandler.write(ServerMessages.ERROR.constructMessage("Your move was invalid"));
        } catch (UnexpectedResponseException e) {
            view.showMessage("[" + this.name + "] tried to make a move but he isn't in a game");
            socketHandler.write(ServerMessages.ERROR.constructMessage("You're not in a game"));
        } catch (AlreadyInQueueException e) {
            view.showMessage("[" + this.name + "] tried to enter the queue, but is already in queue");
            socketHandler.write(ServerMessages.ERROR.constructMessage("You're already in queue"));
        } catch (ClientHandlerNotFoundException e) {
            view.showMessage(e.getMessage());
            socketHandler.write(ServerMessages.ERROR.constructMessage("Could not find you in the list of players. Are you solid snake?"));
        }
    }


    @Override
    public void handleHello(String[] arguments) throws ProtocolException {
        try {
            this.name = arguments[1];

            if(arguments.length > 2){ //Enable any extensions for this Client
                for(int i = 2; i < arguments.length; i++){
                    switch(arguments[i]){
                        case "CHAT":
                            this.chatEnabled = true;
                            break;
                        case "AUTH":
                            this.authEnabled = true;
                            break;
                        case "CRYPT":
                            this.cryptEnabled = true;
                            break;
                        case "RANK":
                            this.rankEnabled = true;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("Invalid number of arguments");
        }
        socketHandler.write(ServerMessages.HELLO.constructMessage(server));
        synchronized (server) {
            server.notify();
        }
    }

    @Override
    public void handleLogin(String[] arguments) throws ProtocolException {
        String userName;
        try {
            userName = arguments[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("Invalid number of arguments");
        }
        if(this.server.isUserLoggedIn(userName)) {
            socketHandler.write(ServerMessages.ALREADYLOGGEDIN.constructMessage());
        } else {
            socketHandler.write(ServerMessages.LOGIN.constructMessage());
            this.userName = userName;
            this.name = userName;
            this.socketHandler.setName(userName);
            this.server.addUserToLoggedInList(userName);
        }
        this.state = ClientHandlerStates.LOGGEDIN;
    }

    @Override
    public void handleChat(String msg) throws ProtocolException {
        try {
            String[] arguments = msg.split(ProtocolMessages.delimiter, 2);
            String sender = this.name;
            String message = ServerMessages.CHAT.constructMessage(sender, arguments[1]);
            for (ClientHandler handler : server.getAllClientHandler()){
                handler.getSocketHandler().write(message);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            throw new ProtocolException("Invalid number of arguments");
        }
    }

    @Override
    public void handleWhisper(String msg) throws ProtocolException {
        try {
            String[] arguments = msg.split(ProtocolMessages.delimiter, 3);
            String sender = this.name;
            String recipient = arguments[1];
            ClientHandler receivingHandler;
            String message = ServerMessages.WHISPER.constructMessage(sender, arguments[2]);

            receivingHandler = server.getClientHandler(recipient);
            if(receivingHandler != null){
                if(receivingHandler.chatEnabled) { //Checks whether the client has chatting enabled.
                    receivingHandler.getSocketHandler().write(message);
                    if(!(receivingHandler == this)){ //But don't write the message to yourself twice.
                        socketHandler.write(message);
                    }
                }
            } else { //If the recipient could not be found, send an error message to the client.
                socketHandler.write(ServerMessages.CANNOTWHISPER.constructMessage(recipient));
            }
        } catch (ArrayIndexOutOfBoundsException e){
            throw new ProtocolException("Invalid number of arguments");
        }
    }

    @Override
    public void handleList() {
        socketHandler.write(ServerMessages.LIST.constructMessage(server.getLoggedInUsers()));
    }

    @Override
    public void handleQueue() {
        this.state = ClientHandlerStates.INQUEUE;
        gameManager.addToQueue(this);
    }

    @Override
    public void startGame(boolean startsFirst, ClientHandler opponent, Game game) {
        this.game = game;
        this.opponent = opponent;
        if(startsFirst) {
            socketHandler.write(ServerMessages.NEWGAME
                    .constructMessage(
                            game.getBoard().getBoardState(),
                            this.userName,
                            opponent.userName));
            this.myTurn = true;
        } else {
            socketHandler.write(ServerMessages.NEWGAME
                    .constructMessage(
                            game.getBoard().getBoardState(),
                            opponent.userName,
                            this.userName));
            this.myTurn = false;
        }
        this.state = ClientHandlerStates.INGAME;
        this.opponent.setState(ClientHandlerStates.INGAME);
    }

    public void closeGame(){
        this.game = null;
        this.opponent = null;
        this.setState(ClientHandlerStates.LOGGEDIN);
    }

    public void handleMove(String[] arguments) throws ProtocolException, NotYourTurnException, InvalidMoveException, ClientHandlerNotFoundException {
        Move move;
        switch (arguments.length) {
            case 1:
                throw new ProtocolException("Not enough arguments");
            case 2:
                move = new Move(Integer.parseInt(arguments[1]));
                break;
            case 3:
                move = new Move(Integer.parseInt(arguments[1]),
                        Integer.parseInt(arguments[2]));
                break;
            default:
                throw new ProtocolException("Too many arguments");
        }
        if(this.myTurn) {
            this.makeMove(move);
            this.myTurn = false;
            if(opponent != null) {
                opponent.setMyTurn(true);
            }
            String moveMsg = ServerMessages.MOVE.constructMessage(move);
            socketHandler.write(moveMsg);
            opponent.getSocketHandler().write(moveMsg);
        } else {
            throw new NotYourTurnException("Not your turn");
        }
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    private void makeMove(Move move) throws NumberFormatException, InvalidMoveException, ClientHandlerNotFoundException {
        this.game.makeMove(move, this);
    }


    @Override
    public void handlePeerShutdown() {
        this.view.showMessage("["+ this.name + "] Disconnected");
        if(this.game != null){
            this.game.playerDisconnected(this);
        }
        this.socketHandler.shutDown();
        this.shutDown();
    }

    @Override
    public void shutDown() {
        this.server.removeClientHandler(this);
        this.server.removeUser(this.name);
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    public String getName() {
        return this.name;
    }

    public Game getGame(){
        return this.game;
    }

    public void setState(ClientHandlerStates state) {
        this.state = state;
    }


    public void gameOver(ServerMessages.GameOverReasons reason, ClientHandler winner) {
        socketHandler.write(ServerMessages.GAMEOVER.constructMessage(reason.toString(), winner.getName()));
        this.closeGame();
    }
}
