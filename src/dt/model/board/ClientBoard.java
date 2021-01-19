package dt.model.board;

/** @author Emiel Rous and Wouter Koning */
import dt.util.Move;

import java.util.List;

public class ClientBoard extends Board {
    public ClientBoard(int boardSize) {
        super(boardSize);
    }

    public ClientBoard(int[] boardState) {
       super();
       super.fillBoard(boardState);
    }
    public ClientBoard() {
        super();
    }

    public Move getAHint() {
        List<Move> validMoves = findValidMoves();
        return validMoves.get(randomNumber(0, validMoves.size()-1));
    }

}
