package dt.ai;

import dt.model.BallType;
import dt.model.Board;
import dt.util.Move;

import java.util.HashMap;

/** @author Emiel Rous and Wouter Koning */
public class GreedyAI implements AI {

    public GreedyAI(){

    }

    @Override
    public Move findBestMove(Board board) {
        Move bestMove = null;
        int bestYield = 0;
        for(Move move : board.findValidMoves()){
            Board boardCopy = board.deepCopy();

            boardCopy.executeMove(move.getMove1());
            if(move.isDoubleMove()){
                boardCopy.executeMove(move.getMove2());
            }

            HashMap<BallType, Integer> yield = boardCopy.getYield();
            int yieldTotal = yield.values().stream().reduce(0, Integer::sum);
            if(yieldTotal > bestYield){
                bestYield = yieldTotal;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
