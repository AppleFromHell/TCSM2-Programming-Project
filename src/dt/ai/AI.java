package dt.ai;

import dt.model.Board;
import dt.util.Move;

/** @author Emiel Rous and Wouter Koning */
public abstract class AI {

    abstract Move findBestMove(Board board);

}
