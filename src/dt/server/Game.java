package dt.server;

import dt.exceptions.ClientHandlerNotFoundException;
import dt.exceptions.InvalidMoveException;
import dt.model.Board;
import dt.model.ServerBoard;
import dt.protocol.ServerMessages;
import dt.util.Move;

import java.util.ArrayList;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class Game {
    private final ServerBoard board;
    private List<Player> players;
    private GameManager manager;

    public Game(GameManager manager, ClientHandler client1, ClientHandler client2) {
        this.board = new ServerBoard();
        this.board.setupBoard();
        this.players = new ArrayList<>();
        players.add(new Player(client1));
        players.add(new Player(client2));
        this.manager = manager;
    }
    public synchronized Board getBoard() {
        return this.board;
    }

    public synchronized void makeMove(Move move, ClientHandler mover) throws InvalidMoveException, ClientHandlerNotFoundException {
        Player player = findPlayer(mover);

        if(player == null){
            throw new ClientHandlerNotFoundException("Client could not be found while trying to make a move.");
        }

        player.addBalls(this.board.makeMove(move));

        if(board.isGameOver()){
            //woo its game over wow it's great so great oh my god lets call the ClientHandlers and tell them!
            this.gameOver();
        }
    }

    private synchronized Player findPlayer(ClientHandler playa) {
        for(Player player : players){
            if(player.getClientHandler() == playa){
                return player;
            }
        }
        return  null;
    }
    private synchronized void gameOver(){
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        int scorePlayer1 = player1.getScore();
        int scorePlayer2 = player2.getScore();
        String message;
        Player winner = null;
        //Construct a message depending on what the outcome of the game is.
        if(scorePlayer1 > scorePlayer2) { //Player 1 has won
            winner = player1;
        //Player 2 has won
        } else if (scorePlayer2 > scorePlayer1){
            winner = player2;
        //Players have the same score, so you gotta count the balls they got
        } else {
            int player1Balls = player1.getBallAmount();
            int player2Balls = player2.getBallAmount();
            if (player1Balls > player2Balls) { //Player 1 got more balls than player 2
                winner = player1;
            } else if(player2Balls > player1Balls){ //Player 2 got more balls than player 1
                winner = player2;
            }
        }
        if(winner != null) {
           sendGameOverWin(winner);
        } else {
           sendGameOverDraw(this.players);
        }
        this.manager.removeGame(this);
    }

    public synchronized void playerDisconnected(ClientHandler rageQuitter) {
        Player quitter = findPlayer(rageQuitter);
        for(Player player : this.players) {
            if(player != quitter) sendGameOverWin(player);
        }
        this.players.remove(quitter);
    }

    private synchronized void sendGameOverWin(Player winner) {
        for(Player player : this.players) {
            if(player.getClientHandler() != null) {
                player.getClientHandler().gameOver(ServerMessages.GameOverReasons.VICTORY, winner.getClientHandler());
            }
        }
    }

    private synchronized void sendGameOverDraw(List<Player> players){
        for(Player player : players){
            if(player.getClientHandler() != null){
                player.getClientHandler().gameOver(ServerMessages.GameOverReasons.DRAW);
            }
        }
    }
}
