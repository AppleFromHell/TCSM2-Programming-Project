package dt.server;

import dt.model.BallType;

import java.util.HashMap;
import java.util.Map;

/** @author Emiel Rous and Wouter Koning
 * This class stores the balls that a player scores during a game.
 */
public class Player {
    public static final int minBallForScore = 3;
    private String name;
    private ClientHandler client;
    private Map<BallType, Integer> collectedBalls;

    public Player(ClientHandler client) {
        this.client = client;
        this.name = client.getName();
        this.collectedBalls = new HashMap<>();
    }

    /**
     * Returns the {@link ClientHandler} associated with this player.
     * @return The {@link ClientHandler} associated with this player.
     */
    public ClientHandler getClientHandler() {
        return client;
    }

    /**
     * Add balls the ball collection of the player.
     * @param ballsCount A {@link Map} which houses all the balls and their amounts yielded from a move.
     */
    public void addBalls(Map<BallType, Integer> ballsCount){
        for(BallType ball : ballsCount.keySet()){
            if(collectedBalls.containsKey(ball)){
                collectedBalls.put(ball, collectedBalls.get(ball) + ballsCount.get(ball));
            }
            collectedBalls.put(ball, ballsCount.get(ball));
        }
    }

    /**
     * Calaculates the score of the player and returns this.
     * @return The score of the player
     */
    public int getScore(){
        int score = 0;
        for(Integer count : collectedBalls.values()){
            score += count / minBallForScore;
        }
        return score;
    }

    /**
     * Returns the amount of balls the player has.
     * @return The amount of balls collected by the player.
     */
    public int getBallAmount(){
        return collectedBalls.values().stream().reduce(0, Integer::sum);
    }

    /**
     * Returns the name of the player.
     * @return The name of the player.
     */
    public String getName() {
        return name;
    }
}
