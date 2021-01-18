package modelTests;

import dt.exceptions.InvalidMoveException;
import dt.model.board.BallType;
import dt.model.board.ServerBoard;

import dt.model.board.Sequence;
import dt.util.Move;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    ServerBoard board;
    int boardSize;
    int[] emptyBoardState;
    ServerBoard testBoard;


    @BeforeEach
    void setup(){
        board = new ServerBoard();
        board.fillBoard(board.createBoard());
        boardSize = board.getBoardSize();

        emptyBoardState = new int[boardSize*boardSize];
        testBoard = new ServerBoard();
        for(int i =0; i < boardSize * boardSize; i++) {
            emptyBoardState[i] = 0;
        }
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
    void testInvalidMove() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        testBoard.fillBoard(boardState);
        assertThrows(InvalidMoveException.class, () ->  testBoard.makeMove(new Move(0)));
        assertThrows(InvalidMoveException.class, () ->  testBoard.makeMove(new Move(20)));
    }

    @Test
    void testMakeMove() throws InvalidMoveException {
        int[] boardState = emptyBoardState.clone();
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(20));
        int[] targetBoardState = boardState;
        assertEquals(targetBoardState, testBoard.getBoardState());
    }

    @Test
    void testPossibleMoves() {
        int[] boardState = emptyBoardState.clone();
        List<Move> possibleMoves;

        boardState[0] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves= testBoard.findPossibleMoves();
        assertTrue(possibleMoves.contains(new Move(20)) && possibleMoves.contains(new Move(21)));

        boardState = emptyBoardState.clone();
        boardState[3] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves= testBoard.findPossibleMoves();
        assertTrue(
                possibleMoves.contains(new Move(0)) &&
                        possibleMoves.contains(new Move(20)) &&
                        possibleMoves.contains(new Move(24))
        );

        boardState = emptyBoardState.clone();
        boardState[24] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves= testBoard.findPossibleMoves();
        System.out.println(possibleMoves.toString());
        assertTrue(
                possibleMoves.contains(new Move(3)) &&
                        possibleMoves.contains(new Move(17)) &&
                        possibleMoves.contains(new Move(24)) &&
                        possibleMoves.contains(new Move(10))
        );
    }

    @Test
    void testFindValidSingleMoves() {
        int[] boardState = emptyBoardState.clone();
        List<Move> validSingleMoves;
        boardState[0] = 1;
        boardState[6] = 1;
        testBoard.fillBoard(boardState);
        System.out.println(testBoard.getPrettyBoardState());
        validSingleMoves = testBoard.findValidSingleMoves();
        System.out.println(validSingleMoves.toString());

        assertTrue(
                validSingleMoves.contains(new Move(0)) &&
                        validSingleMoves.contains(new Move(20))
        );
    }

    @Test
    void testSameBallInSequence() {
        Sequence sequence = new Sequence(Arrays.asList(
                BallType.BLUE,
                BallType.BLUE,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY));
        assertEquals(3, testBoard.sameBallsInSequence(sequence, 0, 0));

        sequence = new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.BLUE,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE));
        assertEquals(2, testBoard.sameBallsInSequence(sequence, 1, 0));
    }

    @Test
    void testGetYield() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[1] = 1;
        boardState[8] = 1;
        testBoard.fillBoard(boardState);
        System.out.println(testBoard.getPrettyBoardState());
        HashMap<BallType, Integer> targetYield = new HashMap<>();
        targetYield.put(BallType.BLUE, 3);
        assertEquals(targetYield, testBoard.getYield());
        assertEquals(emptyBoardState, testBoard.getBoardState());
    }

    @Test
    void testFillBoard() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[1] = 1;
        testBoard.fillBoard(boardState);
        Sequence firstRow = new Sequence(Arrays.asList(
                BallType.BLUE,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
        ));
        assertEquals(firstRow.getBalls(), testBoard.getRows().get(0).getBalls());

        Sequence firstColumn = new Sequence(Arrays.asList(
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
        ));
        assertEquals(firstColumn.getBalls(), testBoard.getColumns().get(0).getBalls());

        Sequence secondColumn = new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
        ));
        assertEquals(secondColumn.getBalls(), testBoard.getColumns().get(1).getBalls());

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
    void testCalculateBallCoordinates() {

        assertEquals(0, testBoard.calculateBallCoordinates(testBoard.getColumns(), 0, 0));
        assertEquals(0,testBoard.calculateBallCoordinates(testBoard.getRows(), 0, 0));

        assertEquals(6, testBoard.calculateBallCoordinates(testBoard.getColumns(), 6, 0));
        assertEquals(6,testBoard.calculateBallCoordinates(testBoard.getRows(), 0, 6));

        assertEquals(48, testBoard.calculateBallCoordinates(testBoard.getColumns(), 6, 6));
        assertEquals(48,testBoard.calculateBallCoordinates(testBoard.getRows(), 6, 6));
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