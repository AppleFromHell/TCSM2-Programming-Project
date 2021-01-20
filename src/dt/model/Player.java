package dt.model;

import dt.model.board.BallType;
import dt.server.ClientHandler;

import java.util.HashMap;
import java.util.Map;

/** @author Emiel Rous and Wouter Koning */
public class Player {
    public static final int minBallForScore = 3;
    private String name;
    private ClientHandler client;
    private Map<BallType, Integer> collectedBalls;

    //TODO This needs to be actually implemented into the game!!!

    public Player(ClientHandler client) {
        this.client = client;
        this.name = client.getName();
    }

    public ClientHandler getClientHandler() {
        return client;
    }

    public void addBalls(HashMap<BallType, Integer> ballsCount){
        for(BallType ball : ballsCount.keySet()){
            if(collectedBalls.containsKey(ball)){
                collectedBalls.put(ball, collectedBalls.get(ball) + ballsCount.get(ball));
            }
            collectedBalls.put(ball, ballsCount.get(ball));
        }
    }
    public int getScore(){
        int score = 0;
        for(Integer count : collectedBalls.values()){
            score += count / minBallForScore;
        }
        return score;
    }

    public int getBallAmount(){
        return collectedBalls.values().stream().reduce(0, Integer::sum);
    }

    public String getName() {
        return name;
    }
}
