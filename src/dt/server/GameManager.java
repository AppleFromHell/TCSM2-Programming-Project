package dt.server;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emiel Rous and Wouter Koning
 * The manager that makes sure that people can join a queue and that games are created. Once a {@link Game} has been created,
 * that gama is entirely self sufficient.
 */
public class GameManager {
    private final List<ClientHandler> queue;
    private final List<Game> activeGames;

    GameManager() {
        this.queue = new ArrayList<>();
        this.activeGames = new ArrayList<>();
    }

    /**
     * Add a {@link ClientHandler} to the queue. If then the queue size is larger than 1, start a game
     * using {@link GameManager#startGame()}.
     *
     * @param clientHandler The {@link ClientHandler} to be added to the queue
     */
    public synchronized void addToQueue(ClientHandler clientHandler) {
        queue.add(clientHandler);
        if (queue.size() > 1) startGame();
    }

    /**
     * Starts a game with the first two people in the queue. It creates a new game with the two clients assigned to
     * it, and adds the game to the list of active games. It also calls the {@link ClientHandler#startGame(boolean, ClientHandler, Game)} method.
     * After the game has been created, the clients are removed from the queue
     */
    public synchronized void startGame() {
        ClientHandler player1 = queue.get(0);
        ClientHandler player2 = queue.get(1);

        Game game = new Game(this, player1, player2);
        activeGames.add(game);

        player1.startGame(true, player2, game);
        player2.startGame(false, player1, game);
        queue.remove(player1);
        queue.remove(player2);
    }

    /**
     * Removes a game from the list of active games.
     *
     * @param game The game to be removed from the list of active games.
     */
    public synchronized void removeGame(Game game) {
        this.activeGames.remove(game);
    }

    /**
     * Removes a player from the queue.
     *
     * @param clientHandler The player to be removed from the queue.
     */
    public void removePlayer(ClientHandler clientHandler) {
        queue.remove(clientHandler);
    }
}
