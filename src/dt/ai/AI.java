package dt.ai;

import dt.model.Board;
import dt.util.Move;

/** @author Emiel Rous and Wouter Koning */
public interface AI {

    Move findBestMove(Board board);

}
