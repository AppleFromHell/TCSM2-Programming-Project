package dt.server;

import dt.exceptions.ClientHandlerNotFoundException;
import dt.exceptions.InvalidMoveException;
import dt.model.Board;
import dt.model.ServerBoard;
import dt.protocol.ServerMessages;
import dt.util.Move;

import java.util.ArrayList;
import java.util.List;

/** @author Emiel Rous and Wouter Koning
 * This is the class that that maanages an individual game.*/
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

    /**
     * Return the instance of this class.
     * @return The instance of this class
     */
    public synchronized Board getBoard() {
        return this.board;
    }

    /**
     * Checks whether the client that made the move is a client that is connected to this game, and if so performs
     * a move on the board by calling the method {@link Board#makeMove(Move)}
     * @param move The move to be making
     * @param mover The client that is performing the move
     * @throws InvalidMoveException If the move that is attempted to be made is not valid.
     * @throws ClientHandlerNotFoundException If the client handler that is handed to this method is not found.
     */
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

    /**
     * Finds a player by their {@link ClientHandler}
     * @param playa The {@link ClientHandler} to which you are trying to find a matching player.
     * @return The player fi the player is found, and otherwise the method returns null.
     */
    private synchronized Player findPlayer(ClientHandler playa) {
        for(Player player : players){
            if(player.getClientHandler() == playa){
                return player;
            }
        }
        return  null;
    }

    /**
     * A method that is called whenever a game is game over. The methods looks for a winner by using the
     * {@link Game#findWinner()} method, after which it sends over who has won, if anyone. It then removes this game
     * from the {@link GameManager}.
     */
    private synchronized void gameOver(){
        Player winner = findWinner();

        if(winner != null) {
           sendGameOverWin(winner);
        } else {
           sendGameOverDraw(this.players);
        }
        this.manager.removeGame(this);
    }

    /**
     * A method which finds out who has won the game by checking the scores of the players and their amount of balls.
     * @return The winner of the game.
     */
    private synchronized Player findWinner(){
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        int scorePlayer1 = player1.getScore();
        int scorePlayer2 = player2.getScore();
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
        return winner;
    }

    /**
     * A method called if a player disconnects. The other player is then sent a message that they have won, and the
     * game is removed from the {@link GameManager}.
     * @param rageQuitter The client who has disconnected
     */
    public synchronized void playerDisconnected(ClientHandler rageQuitter) {
        Player quitter = findPlayer(rageQuitter);
        for(Player player : this.players) {
            if(player != quitter) sendGameOverWin(player);
        }
        this.players.remove(quitter);
    }

    /**
     * A method called if a player has won. All players are then sent a message who has won, and the
     * game is removed from the {@link GameManager}.
     * @param winner The client who has won.
     */
    private synchronized void sendGameOverWin(Player winner) {
        for(Player player : this.players) {
            if(player.getClientHandler() != null) {
                player.getClientHandler().gameOver(ServerMessages.GameOverReasons.VICTORY, winner.getClientHandler());
            }
        }
    }

    /**
     * A method called if there is a draw. All players are then sent a message that a draw has happened, and the
     * game is removed from the {@link GameManager}.
     * @param players The players who are not winners nor losers.
     */
    private synchronized void sendGameOverDraw(List<Player> players){
        for(Player player : players){
            if(player.getClientHandler() != null){
                player.getClientHandler().gameOver(ServerMessages.GameOverReasons.DRAW);
            }
        }
    }
}
