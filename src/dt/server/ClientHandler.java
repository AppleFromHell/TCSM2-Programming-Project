package dt.server;

import dt.model.Game;
import dt.peer.NetworkEntity;
import dt.peer.SocketHandler;
import dt.protocol.ClientMessages;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.protocol.ServerProtocol;

import javax.security.auth.login.LoginException;
import java.net.ProtocolException;
import java.net.Socket;

public class ClientHandler implements NetworkEntity, Runnable, ServerProtocol { //TODO moet dit concurrent zijn???
    private final Server server;
    private final GameManager gameManager;
    private Game game;
    private final SocketHandler socketHandler;
    private final ServerTUI view;
    private String name;
    private String userName;
    private ClientHandlerStates state;

    public void run() {

    }

    ClientHandler(Server server, GameManager gameManager, ServerTUI view, Socket socket) {
        this.server = server;
        this.gameManager = gameManager;
        this.socketHandler = new SocketHandler(this, socket);
        new Thread(socketHandler).start();
        this.view = view;
        this.state = ClientHandlerStates.IDLE;
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
            }
        } catch (IllegalArgumentException e) {
            view.showMessage("["+ this.name + "] unknown response. Response: " + msg);
            if (!e.getMessage().equals("")) view.showMessage("Reason: " + e.getMessage());
            socketHandler.write(ServerMessages.ERROR.constructMessage("Unknown command. Received: " + msg));
        } catch (ProtocolException e) {
            view.showMessage("["+ this.name + "] response invalid. Response: " + msg);
            if (!e.getMessage().equals("")) view.showMessage("Reason: " + e.getMessage());
            socketHandler.write(ServerMessages.ERROR.constructMessage("Invalid command. Received: " + msg));

        } catch (LoginException e) {
            view.showMessage("["+ this.name + "] tried to access the Queue without loggin in first");
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
    }

    @Override
    public void startGame(boolean startsFirst, ClientHandler opponent, Game game) {
        this.game = game;
        socketHandler.write(ServerMessages.NEWGAME.constructMessage(game.getBoard().getBoardState(), this.userName, opponent.userName));
    }

    @Override
    public void handlePeerShutdown() {

    }

    @Override
    public void shutDown() {
        this.server.removeUser(this.name);
    }

    public String getName() {
        return this.name;
    }


}
