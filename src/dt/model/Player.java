package dt.model;

import dt.model.board.BallType;
import dt.server.ClientHandler;
import java.util.Map;

/** @author Emiel Rous and Wouter Koning */
public class Player {
    private String name;
    private ClientHandler client;
    private Map<BallType, Integer> collectedBalls;

    public Player(String name, ClientHandler client) {
        this.name = name;
        this.client = client;
    }
}
