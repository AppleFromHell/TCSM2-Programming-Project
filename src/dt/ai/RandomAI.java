package dt.ai;

import dt.model.Board;
import dt.util.Move;

import java.util.Arrays;
import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class RandomAI implements AI {

    public RandomAI(){

    }

    @Override
    public Move findBestMove(Board board) {
        List<Move> validMoves = board.findValidMoves();
        int randomMove = Board.randomNumber(0, validMoves.size() - 1);
        return validMoves.get(randomMove);
    }
}
