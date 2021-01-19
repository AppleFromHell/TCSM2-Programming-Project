package dt.model;

import dt.exceptions.ClientHandlerNotFoundException;
import dt.exceptions.InvalidMoveException;
import dt.model.board.BallType;
import dt.model.board.Board;
import dt.model.board.ServerBoard;
import dt.server.ClientHandler;
import dt.util.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class Game {
    private final ServerBoard board;
    private List<Player> players;


    public Game(ClientHandler client1, ClientHandler client2) {
        this.board = new ServerBoard();
        this.board.setupBoard();
        this.players = new ArrayList<>();
        players.add(new Player(client1));
        players.add(new Player(client2));
    }
    public synchronized Board getBoard() {
        return this.board;
    }

    public synchronized void makeMove(Move move, ClientHandler mover) throws InvalidMoveException, ClientHandlerNotFoundException {
        boolean playerFound = false;
        for(Player player : players){
            if(player.getClientHandler() == mover){
                player.addBalls(this.board.makeMove(move));
                playerFound = true;
            }
        }
        if(!playerFound){
            throw new ClientHandlerNotFoundException("Client could not be found while trying to make a move.");
        }
    }

    public synchronized void playerDisconnected() {

    }
}
