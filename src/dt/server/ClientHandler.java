package dt.server;

import dt.exceptions.NotYourTurnException;
import dt.model.Game;
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

public class ClientHandler implements NetworkEntity, ServerProtocol { //TODO moet dit concurrent zijn???
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

    ClientHandler(Server server, GameManager gameManager, ServerTUI view, Socket socket) {
        this.server = server;
        this.gameManager = gameManager;
        this.socketHandler = new SocketHandler(this, socket);
        new Thread(socketHandler).start();
        this.view = view;
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
                    if(this.state == ClientHandlerStates.LOGGEDIN ||
                        this.state == ClientHandlerStates.IDLE) {
                        this.handleQueue();
                    } else {
                        throw new LoginException();
                    }
                    break;
                case MOVE:
                    if(this.state == ClientHandlerStates.INGAME) {
                        this.handleMove(arguments);
                        break;
                    }
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
        }
    }


    @Override
    public void handleHello(String[] arguments) throws ProtocolException {
        try {
            this.name = arguments[1];
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
        String name;
        try {
            name = arguments[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("Invalid number of arguments");
        }
        if(this.server.isUserLoggedIn(name)) {
            socketHandler.write(ServerMessages.ALREADYLOGGEDIN.constructMessage());
        } else {
            socketHandler.write(ServerMessages.LOGIN.constructMessage());
            this.userName = name;
            this.server.addUserToList(name);
        }
        this.state = ClientHandlerStates.LOGGEDIN;
    }

    @Override
    public void handleList() {
        socketHandler.write(ServerMessages.LIST.constructMessage(server.getLoggedInUsers()));
    }

    @Override
    public void handleQueue() {
        gameManager.addToQueue(this);
        this.state = ClientHandlerStates.INQUEUE;
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
    }
    public void handleMove(String[] arguments) throws ProtocolException, NotYourTurnException {
        if(this.myTurn) {
            this.makeOurMoveHere(arguments);
        } else {
            throw new NotYourTurnException("Not your turn");
        }
    }

    //This one is called from here
    private void makeOurMoveHere(String[] arguments) throws ProtocolException, NotYourTurnException {
        makeMove(arguments);
        opponent.makeOurMoveThere(arguments);
        this.myTurn = false;
    }
    //Tis one is called from the other ClientHandler
    public void makeOurMoveThere(String[] arguments) throws ProtocolException, NotYourTurnException {
        if(!this.myTurn) {
            makeMove(arguments);
            this.myTurn = true;
        } else {
            throw new NotYourTurnException("Turn mismatch");
        }
    }

    private synchronized void makeMove(String[] arguments) throws ProtocolException, NumberFormatException{

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
        this.game.makeMove(move);
        socketHandler.write(ServerMessages.MOVE.constructMessage(move));
    }


    @Override
    public void handlePeerShutdown() {
        this.socketHandler.shutDown();
        this.shutDown();
    }

    @Override
    public void shutDown() {
        this.server.removeClientHandler(this);
        this.server.removeUser(this.name);
    }

    public String getName() {
        return this.name;
    }


}
