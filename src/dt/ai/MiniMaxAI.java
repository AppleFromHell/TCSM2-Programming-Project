package dt.ai;

import dt.model.Board;
import dt.server.Player;
import dt.util.Move;

import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public class MiniMaxAI implements AI{

    private final int depth;
    private Player you;

    public MiniMaxAI(int depth){
        this.depth = depth;
    }

    public MiniMaxAI(int depth, Player you){
        this.depth = depth;
        this.you = you;
    }

    @Override
    public Move findBestMove(Board board) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move nextMove : board.findValidMoves()){
            int nextScore = this.maximizer(board, nextMove, this.depth, 0);
            if(nextScore > bestScore){
                bestScore = nextScore;
                bestMove = nextMove;
            }
        }

        return bestMove;
    }

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

    private void executeMove(Board board, Move move){
        board.executeMove(move.getMove1());
        if(move.isDoubleMove()){
            board.executeMove(move.getMove2());
        }
    }
}
