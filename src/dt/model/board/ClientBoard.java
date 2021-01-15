package dt.model.board;

import java.util.List;

public class ClientBoard extends Board {
    public ClientBoard(int boardSize) {
        super(boardSize);
    }

    public ClientBoard(int[] boardState) {
       super();
       super.fillBoard(boardState);
    }
    public ClientBoard() {
        super();
    }

    public List<Integer> getValidMoves(){
        return null;
    }

}
