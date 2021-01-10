package java.model.board;

import java.model.BallType;
import java.util.List;

public interface Sequence {

    List<BallType> balls = null;

    void doMove(int move);

}
