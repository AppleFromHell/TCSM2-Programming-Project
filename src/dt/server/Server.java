package dt.server;


import dt.exceptions.UserExit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class Server implements Runnable{
    //TODO when a client disconnects from a game, the other player should be informed that they
    // have won due to a disconnect.

    private Integer port;
    private List<ClientHandler> connectedClients;
    private List<String> loggedinUsers;
    private ServerTUI view;
    private GameManager gameManager;
    private ServerSocket serverSocket;
    private String serverName;
    private boolean chatEnabled;
    private boolean rankEnabled;
    private boolean cryptEnabled;
    private boolean authEnabled;

    private Server() {
        this.view = new ServerTUI(this);
        this.gameManager = new GameManager();
        this.connectedClients = new ArrayList<>();
        this.loggedinUsers = new ArrayList<>();
        this.serverName = "Wouter en Emiels meest awesome server evvur";
        this.chatEnabled = true;
        this.rankEnabled = false;
        this.cryptEnabled = false;
        this.authEnabled = false;
    }
    /** Starts a Server-application. */
    public static void main(String[] args) {
        Server server = new Server();
        if(args.length != 0) server.setPort(Integer.parseInt(args[0]));
        new Thread(server).start();
    }

    /**
     * Starts the server application. This is a method exclusively used for testing.
     * @param args Arguments given to the server, which should just be the port of the server.
     * @return The {@link Server} instance it has started, such that it can be used for testing purposes
     */
    public static Server testMain(String[] args) {
        Server server = new Server();
        if(args.length != 0) server.setPort(Integer.parseInt(args[0]));
        new Thread(server).start();
        return server;
    }

    /**
     * The continuous loop that the server is running in, where it accepts clients and adds them to the list
     * of connected clients.
     */
    public synchronized void run() {
        setup();
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(this ,this.gameManager, this.view, clientSocket);
                this.connectedClients.add(handler);

                this.wait();
                view.showMessage("New client: [" + handler.getName() + "] connected!");
            } catch (IOException  | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Performs the setup of the server. The user receives feedback at what state of starting up the server is.
     * It is possible that the user gets an error message that the server could not start due to an
     * {@link IOException} being thrown. In this case, the user is prompted whether they would like to try again.
     */
    private void setup() {
        new Thread(view).start();
        serverSocket = null;
        while(serverSocket == null) {
            try {
                view.showMessage("Starting a server on port: " + this.port + "...");
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
                view.showMessage("Server is started!");
            } catch (IOException e) {
                view.showMessage("Could not start server");
                try {
                    if (!view.getBoolean("Would you like to try again?")) {
                        throw new UserExit();
                    }
                } catch (UserExit ex) {
                    this.shutDown();
                }
            }
        }
    }

    /**
     * @return Returns a list of logged in users.
     */
    public List<String> getLoggedInUsers() {
        return this.loggedinUsers;
    }

    /**
     * Adds a user to the users that is logged in.
     * @param name The name of the user to be added to the list of logged in users.
     */
    public void addUserToLoggedInList(String name) {
        this.loggedinUsers.add(name);
    }

    /**
     * Removes a user from the list of logged in users.
     * @param name The name of the user to be removed from the logged in users.
     */
    public void removeUser(String name) {
        this.loggedinUsers.remove(name);
    }

    /**
     * Checks whether a client is logged in or not.
     * @param name The name of the client.
     * @return Whether a user is logged in.
     */
    public boolean isUserLoggedIn(String name) {
        return loggedinUsers.contains(name);
    }

    /**
     * Sets the port of the server. Note, changing this does not change the port of the server once it is running.
     * @param port The port on which the server will be running.
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return The port that the server is running on.
     */
    public Integer getPort() {
        return this.port;
    }

    /**
     * @return The name of the server.
     */
    public String getName() {
        return this.serverName;
    }

    /**
     * @return A list of {@link ClientHandler} that are currently connected to the server.
     */
    public List<ClientHandler> getAllClientHandler(){
        return this.connectedClients;
    }

    /**
     * Looks for a {@link ClientHandler} with the name given in the parameters.
     * @param name The name of the ClientHandler that is trying to be found.
     * @return The {@link ClientHandler} if it is exists, otherwise it returns null.
     */
    public ClientHandler getClientHandler(String name){
        for(ClientHandler client : connectedClients){
            if(client.getName().equals(name)){
                return client;
            }
        }
        return null;
    }

    /**
     * @return returns whether the chat is enabled for this server or not.
     */
    public boolean chatIsEnabled() {
        return this.chatEnabled;
    }

    /**
     * @return returns whether ranking is enabled for this server or not.
     */
    public boolean rankIsEnabled() {
        return this.rankEnabled;
    }

    /**
     * @return returns whether authentication is enabled for this server or not.
     */
    public boolean authIsEnabled() {
        return this.authEnabled;
    }

    /**
     * @return returns whether encryption is enabled for this server or not.
     */
    public boolean cryptIsEnabled() {
        return this.cryptEnabled;
    }

    /**
     * Disconnects all clients using {@link ClientHandler#shutDown()}. It then shuts down the server and exits the program with exit code 69.
     */
    public void shutDown() {
        this.connectedClients.forEach(ClientHandler::shutDown);
        this.view.showMessage("Server is shutting down. Cya lator aligator");
        System.exit(69);
    }

    /**
     * Removes a {@link ClientHandler} from the list of connected clients. The ClientHandler is shut down before removing it from the list.
     * @param clientHandler The {@link ClientHandler} to be removed.
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
    }
}
