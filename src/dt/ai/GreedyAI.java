package dt.ai;

import dt.model.BallType;
import dt.model.Board;
import dt.util.Move;

import java.util.HashMap;

/**
 * @author Emiel Rous and Wouter Koning
 * An AI written that uses a greedy algorithm
 */
public class GreedyAI implements AI {

    public GreedyAI() {
    }

    /**
     * Finds the best move using a Greedy algorithm.
     *
     * @param board The {@link Board} on which the AI has to find the best available move.
     * @return The best available move that the AI could find.
     */
    @Override
    public Move findBestMove(Board board) {
        Move bestMove = null;
        int bestYield = 0;
        for (Move move : board.findValidMoves()) {
            Board boardCopy = board.deepCopy();

            boardCopy.executeMove(move.getMove1());
            if (move.isDoubleMove()) {
                boardCopy.executeMove(move.getMove2());
            }

            HashMap<BallType, Integer> yield = boardCopy.getYield();
            int yieldTotal = yield.values().stream().reduce(0, Integer::sum);
            if (yieldTotal > bestYield) {
                bestYield = yieldTotal;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
