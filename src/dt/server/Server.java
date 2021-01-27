package dt.server;


import dt.exceptions.UserExit;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Emiel Rous and Wouter Koning
 * The class that runs the server.
 */
public class Server {
    private final static File rankFile = new File("src/dt/server/Ranking.txt");
    private final List<ClientHandler> connectedClients;
    private final List<String> loggedinUsers;
    private final ServerTUI view;
    private final GameManager gameManager;
    private final String serverName;
    private final boolean chatEnabled;
    private final boolean rankEnabled;
    private final boolean cryptEnabled;
    private final boolean authEnabled;
    private Integer port;
    private ServerSocket serverSocket;
    private boolean debug;

    private Server() {
        this.view = new ServerTUI(this);
        this.gameManager = new GameManager();
        this.connectedClients = new ArrayList<>();
        this.loggedinUsers = new ArrayList<>();
        this.serverName = "Wouter en Emiels meest awesome server evvur";
        this.chatEnabled = true;
        this.rankEnabled = true;
        this.cryptEnabled = false;
        this.authEnabled = false;
        this.debug = false;
    }

    /**
     * Starts a Server-application.
     */
    public static void main(String[] args) {
        Server server = new Server();
        if (args.length != 0) {
            server.setPort(Integer.parseInt(args[0]));
            if (Arrays.asList(args).contains("debug")) {
                server.setDebug(true);
            }
        }
        server.start();
    }

    /**
     * Starts the server application. This is a method exclusively used for testing.
     *
     * @param args Arguments given to the server, which should just be the port of the server.
     * @return The {@link Server} instance it has started, such that it can be used for testing purposes
     */
    public static Server testMain(String[] args) {
        Server server = new Server();
        if (args.length != 0) {
            server.setPort(Integer.parseInt(args[0]));
        }
        //new Thread(server).start();
        return server;
    }

    /**
     * This method scans the file in which the rankings are stored, and then puts all of those names and rankings
     * in a {@link HashMap} and returns that.
     *
     * @return A {@link HashMap} with all the players in it and their rankings.
     */
    public static HashMap<String, Integer> getRankAsHashMap() {

        HashMap<String, Integer> scores = new HashMap<>();
        try {
            Scanner scanner = new Scanner(new FileInputStream(rankFile));
            while (scanner.hasNextLine() && scanner.hasNext()) {
                String name = scanner.next();
                int wins = scanner.nextInt();
                scores.putIfAbsent(name, wins);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return scores;
    }

    /**
     * Adds a new player to the ranking system.
     *
     * @param username The username of the player to be added.
     */
    public static void addNewPlayer(String username) {
        HashMap<String, Integer> scores = getRankAsHashMap();
        scores.putIfAbsent(username, 0);
        storeRanksInFile(scores);
    }

    /**
     * Increases the score of the player that has won
     *
     * @param username The winner of the game, whose ranking is now increased.
     */
    public static void increaseScore(String username) {
        HashMap<String, Integer> scores = getRankAsHashMap();
        int wins = scores.get(username);
        scores.put(username, wins + 1);
        storeRanksInFile(scores);
    }

    /**
     * Writes all the scores of the ranking system to a file.
     *
     * @param scores A {@link HashMap} with all the usernames and their scores.
     */
    private static void storeRanksInFile(HashMap<String, Integer> scores) {
        try {
            BufferedWriter writer = new BufferedWriter(new PrintWriter(rankFile));
            for (Entry<String, Integer> entry : scores.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setDebug(boolean b) {
        this.debug = true;
    }

    /**
     * The continuous loop that the server is running in, where it accepts clients and adds them to the list
     * of connected clients.
     */
    public void start() {
        setup();
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler =
                    new ClientHandler(this, this.gameManager, this.view, clientSocket, this.debug);
                this.connectedClients.add(handler);
                view.showMessage("New client: [" + handler.getName() + "] connected!");
            } catch (IOException e) {

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
        while (serverSocket == null) {
            try {
                try {
                    synchronized (this) {
                        if (this.port == null) {
                            this.wait();
                        }
                    }
                    view.showMessage("Starting a server on port: " + this.port + "...");
                    serverSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
                    view.showMessage("Server is started!");

                    //Signaling that the server is started
                    synchronized (view) {
                        view.notify();
                    }
                } catch (IOException | InterruptedException e) {
                    view.showMessage("Could not start server");

                    if (!view.getBoolean("Would you like to try again?")) {
                        throw new UserExit();
                    }

                    //Set port to null so view will ask again
                    this.port = null;
                    //Signaling that the server is not started
                    synchronized (view) {
                        view.notify();
                    }

                }
            } catch (UserExit ex) {
                this.shutDown();
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
     *
     * @param name The name of the user to be added to the list of logged in users.
     */
    public void addUserToLoggedInList(String name) {
        this.loggedinUsers.add(name);
    }

    /**
     * Removes a user from the list of logged in users.
     *
     * @param name The name of the user to be removed from the logged in users.
     */
    public void removeUser(String name) {
        this.loggedinUsers.remove(name);
    }

    /**
     * Checks whether a client is logged in or not.
     *
     * @param name The name of the client.
     * @return Whether a user is logged in.
     */
    public boolean isUserLoggedIn(String name) {
        return loggedinUsers.contains(name);
    }

    /**
     * @return The port that the server is running on.
     */
    public Integer getPort() {
        return this.port;
    }

    /**
     * Sets the port of the server. Note, changing this does not change the port of the server once it is running.
     *
     * @param port The port on which the server will be running.
     */
    public void setPort(Integer port) {
        this.port = port;
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
    public List<ClientHandler> getAllClientHandler() {
        return this.connectedClients;
    }

    /**
     * Looks for a {@link ClientHandler} with the name given in the parameters.
     *
     * @param name The name of the ClientHandler that is trying to be found.
     * @return The {@link ClientHandler} if it is exists, otherwise it returns null.
     */
    public ClientHandler getClientHandler(String name) {
        for (ClientHandler client : connectedClients) {
            if (client.getName().equals(name)) {
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
        this.connectedClients.forEach(ClientHandler::serverShutdown);
        this.view.showMessage("Server is shutting down. Cya lator aligator");
        System.exit(69);
    }

    /**
     * Removes a {@link ClientHandler} from the list of connected clients. The ClientHandler is shut down before removing it from the list.
     *
     * @param clientHandler The {@link ClientHandler} to be removed.
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
    }
}
