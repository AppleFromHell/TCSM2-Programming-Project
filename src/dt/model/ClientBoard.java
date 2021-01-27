package dt.model;


import dt.util.Move;

import java.util.List;

/** @author Emiel Rous and Wouter Koning
 * The extension of Board which only the {@link dt.collectoClient.Client} uses.
 */
public class ClientBoard extends Board {

    public ClientBoard(int[] boardState) {
       super();
       super.fillBoard(boardState);
    }
    public ClientBoard() {
        super();
    }

    /**
     * Finds all valid moves on the current board and returns a random one of those valid moves as a hint.
     * @return A random valid move.
     */
    public Move getAHint() {
        List<Move> validMoves = findValidMoves();
        return validMoves.get(randomNumber(0, validMoves.size()-1));
    }

}
