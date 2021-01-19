package dt.model;

import dt.exceptions.InvalidMoveException;
import dt.model.board.Board;
import dt.model.board.ServerBoard;
import dt.util.Move;

public class Game {
    private ServerBoard board;

    public Game() {
        this.board = new ServerBoard();
        this.board.setupBoard();
    }
    public synchronized Board getBoard() {
        return this.board;
    }

    public synchronized void makeMove(Move move) throws InvalidMoveException {
        this.board.makeMove(move);
    }
}
