package dt.ai;

import dt.model.Board;
import dt.util.Move;

import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class RandomAI implements AI {

    public RandomAI(){

    }

    @Override
    public Move findBestMove(Board board) {
        List<Move> validMoves = board.findValidMoves();
        double random = Math.random();
        return validMoves.get((int) random * validMoves.size() + 1);
    }
}
