package modelTests;

import dt.exceptions.InvalidMoveException;
import dt.model.BallType;
import dt.model.Sequence;
import dt.model.ServerBoard;
import dt.util.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    ServerBoard board;
    int boardSize;
    int[] emptyBoardState;
    ServerBoard testBoard;


    @BeforeEach
    void setup(){
        board = new ServerBoard();
        board.setupBoard();
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
        assertNotEquals(Collections.emptyList(), board.findValidMoves());
    }

    @Test
    void testSetupForConsistency() {
        for(int i = 0; i < 10000; i++){
            testSetup();
        }
    }

    @Test
    void testInvalidMove() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[5] = 2;
        testBoard.fillBoard(boardState);
        assertThrows(InvalidMoveException.class, () ->  testBoard.makeMove(new Move(0)));
        assertThrows(InvalidMoveException.class, () ->  testBoard.makeMove(new Move(20)));
    }

    @Test
    void testValidMove() throws InvalidMoveException {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[2] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(0));

        boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[44] = 1;
        testBoard.fillBoard(boardState);
        print();
        assertTrue(testBoard.isValidMove(new Move(21, 13)));
        assertTrue(testBoard.isValidMove(new Move(21, 6)));
        assertFalse(testBoard.isValidMove(new Move(21, 16)));
    }

    @Test
    void testMakeMove() throws InvalidMoveException {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[6] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(0));
        assertArrayEquals(emptyBoardState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[2] = 2;
        boardState[3] = 1;
        boardState[13] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(7));
        int[] targetBordState = emptyBoardState.clone();
        targetBordState[5] = 2;
        targetBordState[4] = 1;
        assertArrayEquals(targetBordState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        boardState[14] = 1;
        boardState[19] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(9));

        boardState = emptyBoardState.clone();
        boardState[44] = 1;
        boardState[16] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(16));

        boardState = emptyBoardState.clone();
        boardState[1] = 1;
        boardState[43] = 1;
        testBoard.fillBoard(boardState);
        testBoard.makeMove(new Move(22));
    }

    @Test
    void testPossibleMoves() {
        int[] boardState = emptyBoardState.clone();
        List<Move> possibleMoves;

        boardState[0] = 1;
        testBoard.fillBoard(boardState);

        possibleMoves= testBoard.findPossibleMoves();

        assertTrue(
                possibleMoves.contains(new Move(21)) &&
                        possibleMoves.contains(new Move(7)) &&
                        possibleMoves.size() ==2

                        );

        boardState = emptyBoardState.clone();
        boardState[3] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves= testBoard.findPossibleMoves();
        assertTrue(
                possibleMoves.contains(new Move(0)) &&
                        possibleMoves.contains(new Move(7)) &&
                        possibleMoves.contains(new Move(24)) &&
                        possibleMoves.size() == 3
        );

        boardState = emptyBoardState.clone();
        boardState[24] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves= testBoard.findPossibleMoves();
        assertTrue(
                possibleMoves.contains(new Move(3)) &&
                        possibleMoves.contains(new Move(17)) &&
                        possibleMoves.contains(new Move(24)) &&
                        possibleMoves.contains(new Move(10)) &&
                        possibleMoves.size() == 4
        );

        boardState = emptyBoardState.clone();
        boardState[48] = 1;
        testBoard.fillBoard(boardState);
        possibleMoves = testBoard.findPossibleMoves();
        assertTrue(
                possibleMoves.contains(new Move(6)) &&
                        possibleMoves.contains(new Move(20)) &&
                        possibleMoves.size() == 2
        );
    }

    @Test
    void testFindValidSingleMoves() {
        int[] boardState = emptyBoardState.clone();
        List<Move> validSingleMoves;
        boardState[0] = 1;
        boardState[6] = 1;
        testBoard.fillBoard(boardState);
        validSingleMoves = testBoard.findValidSingleMoves();

        assertTrue(
                validSingleMoves.contains(new Move(0)) &&
                        validSingleMoves.contains(new Move(7)) &&
                        validSingleMoves.size() == 2
        );

        boardState = emptyBoardState.clone();
        testBoard.fillBoard(boardState);
        assertEquals(Collections.emptyList(), testBoard.findValidSingleMoves());
        boardState[6] = 1;
        boardState[48] = 1;
        testBoard.fillBoard(boardState);
        validSingleMoves = testBoard.findValidSingleMoves();

        assertTrue(
                validSingleMoves.contains(new Move(27)) &&
                        validSingleMoves.contains(new Move(20)) &&
                        validSingleMoves.size() == 2
        );

        boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[44] = 1;
        testBoard.fillBoard(boardState);
        print();
        assertEquals(Collections.emptyList(), testBoard.findValidSingleMoves());
    }

    @Test
    void testFindValidDoubleMoves() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[48] = 1;
        testBoard.fillBoard(boardState);
        assertEquals(Collections.emptyList() ,testBoard.findValidSingleMoves());
        List<Move> validDoubleMoves = testBoard.findValidDoubleMoves();
        assertTrue(
                validDoubleMoves.contains(new Move(7, 27)) &&
                        validDoubleMoves.contains(new Move(7, 20)) &&
                        validDoubleMoves.contains(new Move(21, 13)) &&
                        validDoubleMoves.contains(new Move(21, 6)) &&
                        validDoubleMoves.contains(new Move(6, 14)) &&
                        validDoubleMoves.contains(new Move(6, 21)) &&
                        validDoubleMoves.contains(new Move(20, 0)) &&
                        validDoubleMoves.contains(new Move(20, 7)) &&
                        validDoubleMoves.size() == 8);
        boardState[6] = 2;
        testBoard.fillBoard(boardState);
        assertEquals(Collections.emptyList() ,testBoard.findValidSingleMoves());
        validDoubleMoves = testBoard.findValidDoubleMoves();
        assertTrue(
                validDoubleMoves.contains(new Move(21,13)) &&
                        validDoubleMoves.contains(new Move(21, 6)) &&
                        validDoubleMoves.contains(new Move(6,14)) &&
                        validDoubleMoves.contains(new Move(6, 21)) &&
                        validDoubleMoves.contains(new Move(7,26)) &&
                        validDoubleMoves.contains(new Move(20, 1)) &&
                        validDoubleMoves.size() == 6
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
        HashMap<BallType, Integer> targetYield = new HashMap<>();
        targetYield.put(BallType.BLUE, 3);
        assertEquals(targetYield, testBoard.getYield());
        assertArrayEquals(emptyBoardState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[7] = 1;
        boardState[1] = 2;
        boardState[8] = 2;
        testBoard.fillBoard(boardState);
        targetYield.clear();
        targetYield.put(BallType.BLUE, 2);
        targetYield.put(BallType.ORANGE, 2);
        assertEquals(targetYield, testBoard.getYield());
        assertArrayEquals(emptyBoardState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        boardState[8] = 1;
        boardState[9] = 2;
        boardState[10] = 1;
        boardState[15] = 2;
        boardState[16] = 2;
        boardState[17] = 3;
        testBoard.fillBoard(boardState);
        targetYield.clear();
        targetYield.put(BallType.ORANGE, 3);
        assertEquals(targetYield, testBoard.getYield());

        int[] targetBoardState = boardState.clone();
        targetBoardState[9] = 0;
        targetBoardState[15] = 0;
        targetBoardState[16] = 0;
        assertArrayEquals(targetBoardState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        Arrays.fill(boardState, 1);
        testBoard.fillBoard(boardState);
        targetYield.clear();
        targetYield.put(BallType.BLUE, 49);
        assertEquals(targetYield, testBoard.getYield());
        assertArrayEquals(emptyBoardState, testBoard.getBoardState());

        boardState = emptyBoardState.clone();
        Arrays.fill(boardState, 0, 14, 1);
        testBoard.fillBoard(boardState);

        targetYield.clear();
        targetYield.put(BallType.BLUE, 14);
        assertEquals(targetYield, testBoard.getYield());
        assertArrayEquals(emptyBoardState, testBoard.getBoardState());

    }

    @Test
    void testFillBoard() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        boardState[1] = 1;
        boardState[16] = 1;
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

        Sequence thirdRow = new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
        ));
        assertEquals(thirdRow.getBalls(), testBoard.getRows().get(2).getBalls());

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
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
        ));
        assertEquals(secondColumn.getBalls(), testBoard.getColumns().get(1).getBalls());

        Sequence thirdColumn = new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
                ));
        assertEquals(thirdColumn.getBalls(), testBoard.getColumns().get(2).getBalls());
    }

    @Test
    void testExecuteMove() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        testBoard.fillBoard(boardState);

        testBoard.executeMove(7);
        boardState[0] = 0;
        boardState[6] = 1;
        testBoard.fillBoard(boardState);



        assertArrayEquals(boardState, testBoard.getBoardState());

        boardState[20] = 1;
        testBoard.fillBoard(boardState);

        testBoard.executeMove(27);
        boardState = emptyBoardState.clone();
        boardState[41] = 1;
        boardState[48] = 1;
        assertArrayEquals(boardState, testBoard.getBoardState());

        testBoard.executeMove(6);
        boardState[48] = 0;
        boardState[42] = 1;
        assertArrayEquals(boardState, testBoard.getBoardState());
        testBoard.executeMove(14);
        boardState[42] = 0;
        boardState[0] = 1;
        assertArrayEquals(boardState, testBoard.getBoardState());

    }

    @Test
    void testSynchronize() {
        int[] boardState = emptyBoardState.clone();
        boardState[0] = 1;
        testBoard.fillBoard(boardState);
        testBoard.getRows().set(0, new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE
                )));
        testBoard.synchronize(testBoard.getRows(), testBoard.getColumns());
        assertEquals(new Sequence(Arrays.asList(
                BallType.BLUE,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY
                )).getBalls(), testBoard.getColumns().get(boardSize-1).getBalls());
        testBoard.getColumns().set(boardSize-1, new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE
                )));
        testBoard.synchronize(testBoard.getColumns(), testBoard.getRows());
        assertEquals(new Sequence(Arrays.asList(
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.EMPTY,
                BallType.BLUE
        )).getBalls(), testBoard.getRows().get(boardSize-1).getBalls());

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
        balls.add(BallType.PINK);
        balls.add(BallType.ORANGE);
        balls.add(BallType.PINK);
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
        balls.add(BallType.PINK); //4
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
        balls.add(BallType.PINK);
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
        balls.add(BallType.PINK);
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

    private void print() {
        System.out.println(testBoard.getPrettyBoardState() + "\n################################");
    }
}