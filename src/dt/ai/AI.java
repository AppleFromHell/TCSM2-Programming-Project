package dt.ai;

import dt.model.Board;
import dt.util.Move;

/**
 * @author Emiel Rous and Wouter Koning
 * An interface which all of the AI use to ensure that you can switch AIs smoothly.
 */
public interface AI {

    /**
     * A method which will find the best move available for the chosen AI
     */
    Move findBestMove(Board board);

}
