package modelTests;

import dt.model.board.BallType;
import dt.model.board.Board;
import dt.model.board.ServerBoard;

import dt.model.board.Sequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static dt.model.board.Board.BOARDSIZE;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    Board board;

    @BeforeEach
    void setup(){
        board = new ServerBoard();
    }

    @Test
    void fillBoard() {
    }

    @Test
    void makeMove() {
    }

    @Test
    void testMakeMove() {
    }

    @Test
    void isValidMove() {
    }

    @Test
    void testIsValidMove() {
    }

    @Test
    void findValidMoves() {
    }

    @Test
    void findPossibleMoves() {
    }

    @Test
    void sameBallsInSequenceWithNoScore() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.BLUE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.PURPLE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.PURPLE);
        balls.add(BallType.BLUE);
        balls.add(BallType.EMPTY);
        Sequence sequence = new Sequence(balls);
        int initialScore = 1;
        assertEquals(initialScore, board.sameBallsInSequence(sequence, 0, initialScore));
    }

    @Test
    void sameBallsInSequenceWithPositiveScore() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.ORANGE);
        balls.add(BallType.BLUE);
        balls.add(BallType.BLUE);
        balls.add(BallType.BLUE);
        balls.add(BallType.PURPLE);
        balls.add(BallType.GREEN);
        balls.add(BallType.EMPTY);
        Sequence sequence = new Sequence(balls);
        int score = 0;
        for(int i = 0; i < BOARDSIZE; i++){
            int sameBalls = board.sameBallsInSequence(sequence, i, 1);
            if(sameBalls > 1){
                i += sameBalls - 1;
                score += sameBalls;
            }
        }
        assertEquals(3, score);
    }

    @Test
    void sameBallsInSequenceWithMultiplePositiveScore() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.ORANGE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.PURPLE);
        balls.add(BallType.BLUE);
        balls.add(BallType.BLUE);
        balls.add(BallType.BLUE);
        balls.add(BallType.EMPTY);

        Sequence sequence = new Sequence(balls);

        int score = 0;
        for(int i = 0; i < BOARDSIZE; i++){
            int sameBalls = board.sameBallsInSequence(sequence, i, 1);
            if(sameBalls > 1){
                i += sameBalls - 1;
                score += sameBalls;
            }
        }
        assertEquals(5, score);
    }

    @Test
    void sameBallsInSequenceWithMultipleEmpty() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.ORANGE);
        balls.add(BallType.BLUE);
        balls.add(BallType.PURPLE);
        balls.add(BallType.BLUE);
        balls.add(BallType.EMPTY);
        balls.add(BallType.EMPTY);
        balls.add(BallType.EMPTY);

        Sequence sequence = new Sequence(balls);

        int score = 0;
        for(int i = 0; i < balls.size(); i++){
            int sameBalls = board.sameBallsInSequence(sequence, i, 1);
            if(sameBalls > 1){
                i += sameBalls - 1;
                score += sameBalls;
            }
        }
        assertEquals(0, score);
    }

    @Test
    void sameBallsInSequenceWithWrongDifferentSequenceSize(){
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.ORANGE);
        balls.add(BallType.BLUE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.BLUE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.BLUE);
        balls.add(BallType.ORANGE); //Go looking from this point onwards, at index 6
        balls.add(BallType.ORANGE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.ORANGE);
        balls.add(BallType.ORANGE);

        Sequence sequence = new Sequence(balls);
        assertEquals(6, board.sameBallsInSequence(sequence, 6, 1));
    }

    @Test
    void isGameOver() {
    }
}