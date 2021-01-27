package dt.ai;

import dt.model.Board;
import dt.util.Move;

import java.util.List;

/** @author Emiel Rous and Wouter Koning
 * A minimax algorithm with a default depth of 4 to keep moves under 20 seconds.
 */
public class MiniMaxAI implements AI{

    private static int DEFAULTDEPTH = 4;

    private final int depth;

    public MiniMaxAI(){
        this.depth = DEFAULTDEPTH;
    }

    /**
     * Finds the best move using a MiniMAx algorithm.
     * @param board The {@link Board} on which the AI has to find the best available move.
     * @return The best available move that the AI could find.
     */
    @Override
    public Move findBestMove(Board board) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move nextMove : board.findValidMoves()){
            int nextScore = this.minimizer(board, nextMove, this.depth, 0);
            if(nextScore > bestScore){
                bestScore = nextScore;
                bestMove = nextMove;
            }
        }

        return bestMove;
    }

    /**
     * The minimizer, which mimics the enemy player.
     * @param board The board you're playing on
     * @param move The last move performed, and thus the new board that you're playing on.
     * @param depth The depth of the minimax algorithm.
     * @param score The score you're keeping track of and want to maximize in the end.
     * @return the score that it has assigned this board.
     */
    private int minimizer(Board board, Move move, int depth, int score){
        Board nextBoard = board.deepCopy();
        this.executeMove(nextBoard, move);
        int boardYieldScore = nextBoard.getYield().values().stream().reduce(0, Integer::sum);
        score += boardYieldScore;

        if(depth == 0){ //If you're out of your depth, just return the score that the opponent would get
            return score;
        }

        List<Move> validMoves = nextBoard.findValidMoves();
        if(validMoves.isEmpty()){ //If there are no more valid moves, return a negative win score. AKA a loss score.
            return score;
        }

        int minScore = Integer.MAX_VALUE;// Initialize to a value that is incredibly high.
        for(Move nextMove : validMoves){ //Loop through all the moves
            int nextScore = this.maximizer(nextBoard, nextMove, depth -1, score); //Find the next score of the board
            if(nextScore < minScore){
                minScore = nextScore; //You have found a better score for this player! Damn nice!
            }
        }
        return minScore;
    }

    /**
     * The maximizer, which mimics the enemy player.
     * @param board The board you're playing on
     * @param move The last move performed, and thus the new board that you're playing on.
     * @param depth The depth of the minimax algorithm.
     * @param score The score you're keeping track of and want to maximize in the end.
     * @return the score that it has assigned this board.
     */
    private int maximizer(Board board, Move move, int depth, int score){
        Board nextBoard = board.deepCopy();
        this.executeMove(nextBoard, move);

        //Update the score with the previous move made.
        int boardYieldScore = nextBoard.getYield().values().stream().reduce(0, Integer::sum);
        score -= boardYieldScore;

        if(depth == 0){ //If you're out of your depth, just return the score that the opponent would get
            return score;
        }

        List<Move> validMoves = nextBoard.findValidMoves();
        if(validMoves.isEmpty()){ //If there are no more valid moves, return a negative win score. AKA a loss score.
            return score;
        }

        int maxScore = Integer.MIN_VALUE;// Initialize to a value that is incredibly high.
        for(Move nextMove : validMoves){ //Loop through all the moves
            int nextScore = this.minimizer(nextBoard, nextMove, depth -1, score); //Find the next score of the board
            if(nextScore > maxScore){
                maxScore = nextScore; //You have found a better score for this player! Damn nice!
            }
        }

        return maxScore;
    }

    /**
     * A method introduced to keep the {@link MiniMaxAI#maximizer(Board, Move, int, int)} and
     * {@link MiniMaxAI#minimizer(Board, Move, int, int)} as clean as possible.
     * @param board The board you're playing on
     * @param move The last move performed, and thus the new board that you're playing on.
     */
    private void executeMove(Board board, Move move){
        board.executeMove(move.getMove1());
        if(move.isDoubleMove()){
            board.executeMove(move.getMove2());
        }
    }
}
