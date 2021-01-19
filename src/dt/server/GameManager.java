package dt.server;

import dt.model.Game;

import java.util.ArrayList;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class GameManager {
    private List<ClientHandler> queue;
    private List<Game> activeGames;

    GameManager() {
        this.queue = new ArrayList<>();
        this.activeGames = new ArrayList<>();
    }

    public synchronized void addToQueue(ClientHandler clientHandler) {
        queue.add(clientHandler);
        if(queue.size() > 1) startGame();
    }

    public synchronized void startGame() {
        Game game = new Game();
        activeGames.add(game);
        ClientHandler player1 = queue.get(0);
        ClientHandler player2 = queue.get(1);
        player1.startGame(true, player2, game);
        player2.startGame(false, player1, game);
        queue.remove(player1);
        queue.remove(player2);
    }
}
