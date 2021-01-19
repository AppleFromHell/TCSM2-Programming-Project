package dt.server;


import dt.exceptions.UserExit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> getLoggedInUsers() {
        return this.loggedinUsers;
    }
    public void addUserToList(String name) {
        this.loggedinUsers.add(name);
    }
    public void removeUser(String name) {
        this.loggedinUsers.remove(name);
    }
    public boolean isUserLoggedIn(String name) {
        return loggedinUsers.contains(name);
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public Integer getPort() {
        return this.port;
    }
    public String getName() {
        return this.serverName;
    }
    public List<ClientHandler> getAllClientHandler(){
        return this.connectedClients;
    }
    public boolean chatIsEnabled() {
        return this.chatEnabled;
    }

    public boolean rankIsEnabled() {
        return this.rankEnabled;
    }

    public boolean authIsEnabled() {
        return this.authEnabled;
    }

    public boolean cryptIsEnabled() {
        return this.cryptEnabled;
    }

    public void shutDown() {
        this.connectedClients.forEach(ClientHandler::shutDown);
        this.view.showMessage("Server is shutting down. Cya lator aligator");
        System.exit(69);
    }

    public void removeClientHandler(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
    }
}
