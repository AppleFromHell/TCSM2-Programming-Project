package dt.model;

import dt.server.ClientHandler;
import java.util.Map;

public class Player {
    private String name;
    private ClientHandler client;
    private Map<BallType, Integer> collectedBalls;

    public Player(String name, ClientHandler client) {
        this.name = name;
        this.client = client;
    }
}
