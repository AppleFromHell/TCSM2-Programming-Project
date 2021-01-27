package dt.server;

import java.util.ArrayList;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class GameManager {
    private final List<ClientHandler> queue;
    private final List<Game> activeGames;

    GameManager() {
        this.queue = new ArrayList<>();
        this.activeGames = new ArrayList<>();
    }

    public synchronized void addToQueue(ClientHandler clientHandler) {
        queue.add(clientHandler);
        if(queue.size() > 1) startGame();
    }

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

    public synchronized void removeGame(Game game){
        this.activeGames.remove(game);
    }

    public void removePlayer(ClientHandler clientHandler) {
        queue.remove(clientHandler);
    }
}
