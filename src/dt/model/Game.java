package dt.model;

import dt.model.board.Board;
import dt.model.board.ServerBoard;
import dt.util.Move;

public class Game {
    private ServerBoard board;

    public Game() {
        this.board = new ServerBoard();
        this.board.setupBoard();
    }
    public Board getBoard() {
        return this.board;
    }

    public void makeMove(Move move) {
    }
}
