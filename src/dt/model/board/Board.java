package dt.model.board;

import dt.exceptions.InvalidMoveException;

import java.util.*;
import java.util.stream.Collectors;

public class Board {
    public static final int BOARDSIZE = 7;

    private List<Sequence> rows;
    private List<Sequence> columns;
    protected int boardSize;

    public Board() {
        this.boardSize = BOARDSIZE;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
    }

    public Board(int boardSize){
        this.boardSize = boardSize;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
    }

    public int getBoardsize(){
        return this.boardSize;
    }

    public void fillBoard(int[] newBoard){ //Parse van int[] to BallType[]
        for (int r = 0; r < BOARDSIZE; r++){
            List<BallType> balls = new ArrayList<>();
            for(int i = 0; i < BOARDSIZE; i++){
                balls.add(findBallType(newBoard[r * BOARDSIZE + i]));
            }
            this.rows.add(new Sequence(balls));
        }

        for(int r = 0; r < BOARDSIZE; r++){
            List<BallType> balls = new ArrayList<>();
            for(int i = 0; i < BOARDSIZE; i ++){
                List<BallType> rowBalls = this.rows.get(r).getBalls();
                balls.add(rowBalls.get(i)); //Essentially doing column[r][i] = row[i][r]
            }
            this.columns.add(new Sequence(balls));
        }
    }

    private BallType findBallType(int ball){
        BallType[] allTypes = BallType.values();
        return allTypes[ball];
    }

    public HashMap<BallType, Integer> makeMove(int move) throws InvalidMoveException {
        isValidMove(move);
        executeMove(move);
        return getYield();
    }

    public HashMap<BallType, Integer> makeMove(int move1, int move2) {
        isValidMove(move1, move2);
        executeMove(move1);
        executeMove(move2);
        return getYield();
    }

    /**
     * Executes a move on the board that is provided in the parameter of the function
     * @param move The move to be executed, conform the protocol.
     */
    private void executeMove(int move) {
        if (move > (3 * BOARDSIZE) - 1) { // move > 20
            columns.get(move - (3 * BOARDSIZE)).shiftLeftOrDown();
        } else if (move > (2 * BOARDSIZE) - 1) { // move > 13
            columns.get(move - (2 * BOARDSIZE)).shiftRightOrUp();
        } else if (move > BOARDSIZE - 1) { // move > 6
            rows.get(move - BOARDSIZE).shiftRightOrUp();
        } else { // move <= 6
            rows.get(move).shiftLeftOrDown();
        }

        //TODO Rows and Columns need to be synchronized again after they have shifted
    }

    public boolean isValidMove(int move) throws InvalidMoveException {
        if(move < 0 || move > 27){
            throw new InvalidMoveException("Move integer given is lower than 0 or higher than 27");
        }
        return findValidMoves().contains(move);
    }

    /**
     * First looks at whether move1 is valid, and if so places this move on a deep copy of the board, and then sees
     * whether move2 is a valid move.
     * @param move1 The first move to be done.
     * @param move2 The second move to be done.
     * @return Whether the move sequence is valid or not.
     */
    public boolean isValidMove(int move1, int move2) {
        return false;
    }

    public List<Integer> findValidMoves() {
        // Find all the possible moves
        // Iterate through them
            // Create a deep copy of the current board
            // Place the move on the copyBoard
            // If the getYield is not empty, it is a valid move.
        // Add iteratable to list of valid moves
        List<Integer> validMoves = new ArrayList<>();
        List<Integer> possibleMoves = this.findPossibleMoves();
        for(Integer move : possibleMoves){
            Board copyBoard = new Board(this.boardSize);
            copyBoard.fillBoard(this.getBoardState()); // Create deep copy
            copyBoard.executeMove(move);               // Place move on the copyBoard
            HashMap<BallType, Integer> yield = copyBoard.getYield(); //Get the yield
            if(yield.values().stream().reduce(0, Integer::sum) > 0){
                //throw a party and lets go to the casino because we've got a valid move on our hands bois
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * A method that finds the moves that are possible to do. Though, the return value of this is not per say the moves that are valid.
     * @return A list of integers, indicating the moves that are possible given the current board state.
     */
    public List<Integer> findPossibleMoves(){
        List<Integer> possibleMoves = new ArrayList<>();
        for(int rowIndex = 0; rowIndex < BOARDSIZE; rowIndex++){
            Sequence row = this.rows.get(rowIndex);
            List<BallType> balls;

            for(int ballIndex = 0; ballIndex < (balls = row.getBalls()).size(); ballIndex++){
                BallType ball = balls.get(ballIndex);

                if(ball.equals(BallType.EMPTY)){
                    // Check whether it can move left
                    if(ballIndex > 0) possibleMoves.add(rowIndex);

                    // Check whether it can move right
                    if(ballIndex < BOARDSIZE - 1) possibleMoves.add(rowIndex + BOARDSIZE);

                    // check whether it can move up
                    if(rowIndex > 0) possibleMoves.add(ballIndex + (2 * BOARDSIZE));

                    // Check whether it can move down
                    if(rowIndex < BOARDSIZE - 1) possibleMoves.add(ballIndex + (3 * BOARDSIZE));
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Go over all rows and columns and find the type and amount of balls that lie next to each other.
     * @return a HashMap<BallType, Integer> with as key the {@link BallType}, and as value the amount of balls the move yielded.
     */
    public HashMap<BallType, Integer> getYield(){ //TODO this sometimes checks on diagonals, rather than just on the sequences.
        HashMap<BallType, Integer> ballScore = new HashMap<>();
        List<List<Sequence>> rowsAndColumns = new ArrayList<>();
        rowsAndColumns.add(this.rows);
        rowsAndColumns.add(this.columns);
        HashSet<Integer> toBeRemovedBalls = new HashSet<>();

        for(List<Sequence> sequenceList : rowsAndColumns) { //rows and columns
            for(int seq = 0; seq < sequenceList.size(); seq++) { //sequences in the row/column
                for (int i = 0; i < this.boardSize; i++) {  //elements in the sequence
                    int sameBallsInARow = sameBallsInSequence(sequenceList.get(seq), i, 1);
                    if (sameBallsInARow > 1) {
                        i += sameBallsInARow - 1;
                        //Store the coordinates of those feckers so they can be removed later
                        for(int offset = 0; offset < sameBallsInARow; offset++) {
                            toBeRemovedBalls.add(calculateBallCoordinates(sequenceList, seq, i + offset));
                        }
                        //Save the ball and the amount of its neighbours to a HashMap for adding player score.
                        BallType thisBall = sequenceList.get(seq).getBalls().get(i);
                        if (!ballScore.containsKey(thisBall)) {
                            ballScore.put(thisBall, sameBallsInARow);
                        }
                        ballScore.replace(thisBall, sameBallsInARow);
                    }
                }
            }
        }

        removeYield(toBeRemovedBalls);

        return ballScore;
    }

    /**
     * Calculates the coordinates of the ball, given a list of Sequences, what number in the list, and what element.
     * @param sequenceList A list of Sequences, which is either this.rows or this.columns.
     * @param sequenceNum The sequence index of the list.
     * @param elementNum The element index of the sequence.
     * @return the coordinates of the elementNum in sequenceNum, in the sequenceList.
     */
    private int calculateBallCoordinates(List<Sequence> sequenceList, int sequenceNum, int elementNum){
        if(sequenceList == this.rows){
            return sequenceNum * this.boardSize + elementNum;
        } else {
            return elementNum * this.boardSize + sequenceNum;
        }
    }

    /**
     * Removes the yield that is calculated by {@link Board#getYield()}.
     * @param toBeRemovedBalls A HashSet containing the coordinates of the balls to be removed from the board,
     *                         calculated by the {@link Board#calculateBallCoordinates(List, int, int)} method.
     */
    private void removeYield(HashSet<Integer> toBeRemovedBalls){
        for (int coord : toBeRemovedBalls) {
            //for the rows
            int yCoord = coord / 7;
            int xCoord = coord % 7;
            this.rows.get(yCoord).getBalls().set(xCoord, BallType.EMPTY);
            this.columns.get(xCoord).getBalls().set(yCoord, BallType.EMPTY);

        }
    }

    /**
     * Recursively calculated how many of the same balls are found after each other in a {@link Sequence}, given an index.
     * @param sequence The sequence in which to look for similar balls.
     * @param index The index of the element to be looking at in the sequence.
     * @param ballSequence The amount of balls found that are the same in the sequence
     * @return The amount of balls found that are the same in the sequence
     */
    public int sameBallsInSequence(Sequence sequence, int index, int ballSequence){ //first call index should be 0 or BOARDSIZE - 1
        if(index > sequence.getBalls().size() - 2 ){ //At the edge of the board.
            return ballSequence;
        }

        BallType nextBall = sequence.getBalls().get(index + 1);
        BallType thisBall = sequence.getBalls().get(index);

        if(!nextBall.equals(BallType.EMPTY) && nextBall.equals(thisBall)){ // The next ball is the same ball
            ballSequence = sameBallsInSequence(sequence, index + 1, ballSequence + 1);
            return ballSequence;
        } else { //The next ball is not the same ball.
            return ballSequence;
        }
    }

    public int[] getBoardState(){
        int[] boardState = new int[this.boardSize * this.boardSize];
        for(int r = 0; r < this.rows.size(); r++){
            Sequence row = this.rows.get(r);
            List<BallType> balls;
            for(int b = 0; b < (balls = row.getBalls()).size(); b++){
                boardState[r * this.boardSize + b] = balls.get(b).ordinal();
            }
        }
        return boardState;
    }

    public String getPrettyBoardState(){
        int[] boardState = getBoardState();
        String rowSeperator = "---+---+---+---+---+---+---";
        StringBuilder board = new StringBuilder();
        for(int i = 0; i < boardState.length; i+=7){
            StringBuilder row = new StringBuilder();
            for(int el = 0; el < this.boardSize; el++){
                row.append(" ").append(boardState[i + el]).append(" ");
                if(el < this.boardSize - 1){
                    row.append("|");
                }
            }
            board.append(row);
            if(i/7 < this.boardSize - 1) {
                board.append(System.lineSeparator()).append(rowSeperator).append(System.lineSeparator());
            }
        }
        return board.toString();
    }

    public boolean isGameOver(){
        return false;
    }

    /**
     * Creates a deep copy of the current board and returns this.
     * @return a deep copy of the board.
     */
    public Board deepCopy(){
        Board copyBoard = new Board(this.boardSize);
        copyBoard.fillBoard(getBoardState());
        return null;
    }

    @Override
    public String toString() {
        return getPrettyBoardState();
    }
}
