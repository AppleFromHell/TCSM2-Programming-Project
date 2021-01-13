package modelTests;

import dt.exceptions.InvalidMoveException;
import dt.model.board.BallType;
import dt.model.board.Board;
import dt.model.board.ServerBoard;

import dt.model.board.Sequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    ServerBoard board;
    int boardSize;

    @BeforeEach
    void setup(){
        board = new ServerBoard();
        board.fillBoard(board.createBoard());
        boardSize = board.getBoardsize();
    }

    @Test
    void testSetup() {
        int middle = (boardSize * boardSize - 1) / 2;
        ServerBoard copyBoard = new ServerBoard();

        copyBoard.fillBoard(board.getBoardState());
        int[] boardState = board.getBoardState();
        assertTrue(Arrays.equals(board.getBoardState(), copyBoard.getBoardState()));

        assertEquals(0, boardState[middle]);
        HashMap<BallType, Integer> yield = board.getYield();
        assertEquals(0, yield.values().stream().reduce(0, Integer::sum));
    }

    @Test
    void testSetupForConsistency() {
        for(int i = 0; i < 2000; i++){
            testSetup();
        }
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
    void testDeepCopy() {
        ServerBoard original = new ServerBoard();
        original.setupBoard();
        ServerBoard copy = new ServerBoard();
        copy.fillBoard(original.getBoardState());
        assertFalse(original.equals(copy));
        int[] oBoardState = original.getBoardState();
        int[] cBoardState = copy.getBoardState();
        assertTrue(Arrays.equals(original.getBoardState(), copy.getBoardState()));
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
        balls.add(BallType.ORANGE); //0
        balls.add(BallType.BLUE);   //1
        balls.add(BallType.BLUE);   //2
        balls.add(BallType.BLUE);   //3
        balls.add(BallType.PURPLE); //4
        balls.add(BallType.GREEN);  //5
        balls.add(BallType.EMPTY);  //6
        Sequence sequence = new Sequence(balls);
        int score = 0;
        for(int i = 0; i < boardSize; i++){
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
        for(int i = 0; i < boardSize; i++){
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