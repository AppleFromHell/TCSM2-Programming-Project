package dt.model;

import dt.exceptions.ClientHandlerNotFoundException;
import dt.exceptions.InvalidMoveException;
import dt.model.board.Board;
import dt.model.board.ServerBoard;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;
import dt.server.ClientHandler;
import dt.server.GameManager;
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

        if(board.isGameOver()){
            //woo its game over wow it's great so great oh my god lets call the ClientHandlers and tell them!
            this.gameOver();
        }
    }

    private synchronized void gameOver(){
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        int scorePlayer1 = player1.getScore();
        int scorePlayer2 = player2.getScore();
        String message;

        //Construct a message depending on what the outcome of the game is.
        if(scorePlayer1 > scorePlayer2) { //Player 1 has won
            message = ServerMessages.GAMEOVER.constructMessage(
                    ServerMessages.GameOverReasons.VICTORY.toString(), player1.getName());
        //Player 2 has won
        } else if (scorePlayer2 > scorePlayer1){
            message = ServerMessages.GAMEOVER.constructMessage(
                    ServerMessages.GameOverReasons.VICTORY.toString(), player2.getName());
        //Players have the same score, so you gotta count the balls they got
        } else {
            int player1Balls = player1.getBallAmount();
            int player2Balls = player2.getBallAmount();
            if (player1Balls > player2Balls) { //Player 1 got more balls than player 2
                message = ServerMessages.GAMEOVER.constructMessage(
                        ServerMessages.GameOverReasons.VICTORY.toString(), player1.getName());
            } else if(player2Balls > player1Balls){ //Player 2 got more balls than player 1
                message = ServerMessages.GAMEOVER.constructMessage(
                        ServerMessages.GameOverReasons.VICTORY.toString(), player2.getName());
            } else { //They got the same amount of balls, so nobody won
                message = ServerMessages.GAMEOVER.constructMessage(
                        ServerMessages.GameOverReasons.DRAW.toString());
            }
        }

        for (Player p : players) {
            ClientHandler handler = p.getClientHandler();
            handler.getSocketHandler().write(message); //Send that message over to the clients
            handler.gameOver(); //Tell the ClientHandler class it is also game over
        }
        this.manager.removeGame(this);
    }

    public synchronized void playerDisconnected() {

    }

}
