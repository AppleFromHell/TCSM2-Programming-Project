package modelTests;

import dt.model.ServerBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


public class ServerBoardTest {

    private ServerBoard board;

    @BeforeEach
    void setup() {
        this.board = new ServerBoard();
        this.board.setupBoard();
    }

    @Test
    void testSetupBoardConsistency() {
        for(int i = 0; i < 20000; i++) {
            this.board.setupBoard();
            assertNotEquals(Collections.emptyList(), this.board.findValidMoves());
        }
    }
}
