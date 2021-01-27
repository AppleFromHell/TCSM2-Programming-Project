package dt.ai;

import dt.model.Board;
import dt.util.Move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Emiel Rous and Wouter Koning
 * A minimax algorithm with alpha beta pruning to optimize performance.
 */
public class MiniMaxAI2 implements AI{
    private static int DEFAULTDEPTH = 6; // Depth of 7 really seems like a maximum.

    private final int depth;
    Map<int[], Integer> dictionary;

    public MiniMaxAI2(){
        this.depth = DEFAULTDEPTH;
        dictionary = new HashMap<>();
    }

    /**
     * Finds the best move using a MiniMAx algorithm.
     * @param board The {@link Board} on which the AI has to find the best available move.
     * @return The best available move that the AI could find.
     */
    @Override
    public Move findBestMove(Board board) {
        long millis = System.currentTimeMillis();
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move nextMove : board.findValidMoves()){
            int nextScore = this.minimizer(board, nextMove, this.depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if(nextScore > bestScore){
                bestScore = nextScore;
                bestMove = nextMove;
            }
        }
        System.out.println("Time: "+ (System.currentTimeMillis()-millis)/1000);
        return bestMove;
    }

    /**
     * The method trying to minimize your score.
     * @param board The board you're playing on
     * @param move The last move performed, and thus the new board that you're playing on.
     * @param depth The depth of the minimax algorithm.
     * @param score The score you're keeping track of and want to maximize in the end
     * @param alpha The score of the alpha for Alpha-Beta pruning
     * @param beta The score of the beta for Alpha-Beta pruning
     * @return The score that the maximizer assigned to the current board.
     */
    private int minimizer(Board board, Move move, int depth, int score, int alpha, int beta){
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
            int nextScore = this.maximizer(nextBoard, nextMove, depth -1, score, alpha, beta); //Find the next score of the board

            if(nextScore < minScore){
                minScore = nextScore; //You have found a better score for this player! Damn nice!
            }
            beta = Math.min(beta, minScore);
            if(beta <= alpha) {
                break;
            }
        }
        return minScore;
    }

    /**
     * The method trying to maximize your score.
     * @param board The board you're playing on
     * @param move The last move performed, and thus the new board that you're playing on.
     * @param depth The depth of the minimax algorithm.
     * @param score The score you're keeping track of and want to maximize in the end
     * @param alpha The score of the alpha for Alpha-Beta pruning
     * @param beta The score of the beta for Alpha-Beta pruning
     * @return The score that the maximizer assigned to the current board.
     */
    private int maximizer(Board board, Move move, int depth, int score, int alpha, int beta){
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
            int nextScore = this.minimizer(nextBoard, nextMove, depth -1, score, alpha, beta); //Find the next score of the board
            if(nextScore > maxScore){
                maxScore = nextScore; //You have found a better score for this player! Damn nice!
            }
            alpha = Math.max(alpha, maxScore);
            if(alpha >= beta){
                break;
            }
        }

        return maxScore;
    }

    /**
     * A method introduced to keep the {@link MiniMaxAI2#maximizer(Board, Move, int, int, int, int)} and
     * {@link MiniMaxAI2#minimizer(Board, Move, int, int, int, int)} as clean as possible.
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
