package dt.ai;

import dt.model.Board;
import dt.util.Move;

import java.util.List;

/**
 * @author Emiel Rous and Wouter Koning
 * An AI of the lowest level, which just returns a random move that is available.
 */
public class RandomAI implements AI {

    public RandomAI() {

    }

    /**
     * Returns a random move of the moves that are found to be valid.
     *
     * @param board The board that you're playing on.
     * @return A random valid move.
     */
    @Override
    public Move findBestMove(Board board) {
        List<Move> validMoves = board.findValidMoves();
        int randomMove = Board.randomNumber(0, validMoves.size() - 1);
        return validMoves.get(randomMove);
    }
}
