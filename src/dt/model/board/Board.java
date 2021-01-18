package dt.model.board;

import dt.exceptions.InvalidMoveException;
import dt.util.Move;

import java.util.*;

public class Board {
    private static final int BOARDSIZE = 7;

    private List<Sequence> rows;
    private List<Sequence> columns;
    protected int boardSize;
    private boolean singleMoveAvailable;

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

    public int getBoardSize(){
        return this.boardSize;
    }

    public void fillBoard(int[] newBoard){ //Parse van int[] to BallType[]
        this.rows.clear();
        this.columns.clear();
        List<List<BallType>> columns = Arrays.asList(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
                );

        for (int r = 0; r < this.boardSize; r++){
            List<BallType> balls = new ArrayList<>();
            for(int i = 0; i < this.boardSize; i++){
                balls.add(findBallType(newBoard[r * this.boardSize + i]));
                columns.get(i).add(findBallType(newBoard[r * this.boardSize + i]));
            }
            this.rows.add(new Sequence(balls));
        }
        for(List<BallType> balls : columns) {
           this.columns.add(new Sequence(balls));
        }
    }

    private BallType findBallType(int ball){
        BallType[] allTypes = BallType.values();
        return allTypes[ball];
    }

    public HashMap<BallType, Integer> makeMove(Move move) throws InvalidMoveException {
        if (isValidMove(move)) {
            if(move.isDoubleMove()){
                executeMove(move.getMove1());
                executeMove(move.getMove2());
            } else {
                executeMove(move.getMove1());
            }
            return getYield();
        } else {
            throw new InvalidMoveException("The move that you tried to make is not a valid move");
        }
    }

    /**
     * Executes a move on the board that is provided in the parameter of the function
     * @param move The move to be executed, conform the protocol.
     */
    public void executeMove(int move) {
        boolean changedColumn = false;

        if(move < this.boardSize) { // < 7
            rows.get(move).shiftLeftOrUp();
        } else if( move < (2 * this.boardSize)) { //< 14
            columns.get((2 * this.boardSize) - move - 1).shiftLeftOrUp();
            changedColumn = true;
        } else if( move < (3 * this.boardSize)) { //<21
            rows.get((3 * this.boardSize) - move - 1).shiftRightOrdown();
        } else if( move < (4 * this.boardSize)) { //<28
            columns.get( move - (3 * this.boardSize)).shiftRightOrdown();
            changedColumn = true;
        }

        if(changedColumn){
            synchronize(this.columns, this.rows);
        } else {
            synchronize(this.rows, this.columns);
        }
    }

    public void synchronize(List<Sequence> updatedList, List<Sequence> outdatedList) {
        for (int r = 0; r < this.boardSize; r++){
            Sequence row = updatedList.get(r);
            for(int b = 0; b < this.boardSize; b++){
                BallType ball = row.getBalls().get(b);
                outdatedList.get(b).getBalls().set(r, ball);
            }
        }
    }

    public boolean isValidMove(Move move) throws InvalidMoveException {
        boolean validity = false;

        if(move.isLegal()) {
            //calling isDoubleMove lowers complexity if it is a single move
            if (!move.isDoubleMove()) { //If it is a single move
                validity = findValidSingleMoves().contains(move);
            } else if (move.isDoubleMove()) { //If it is a double move
                if(this.singleMoveAvailable)
                    throw new InvalidMoveException("You tried to make a double move, while a single move is still available.");
                validity = findValidDoubleMoves().contains(move);
            }
        } else {
            throw new InvalidMoveException("Move integer given is lower than 0 or higher than 27");
        }

        return validity;
    }

    public List<Move> findValidDoubleMoves(){
        List<Move> validDoubleMoves = new ArrayList<>();
        for(Move move1 : this.findPossibleMoves()){
            Board copyBoard = this.deepCopy();
            copyBoard.executeMove(move1.getMove1());
            for(Move move2 : copyBoard.findValidSingleMoves()){
                validDoubleMoves.add(new Move(move1.getMove1(), move2.getMove1())); //All of these bois are goin' to work, hell yea
            }
        }
        return validDoubleMoves;
    }

    public List<Move> findValidSingleMoves() {
        List<Move> validMoves = new ArrayList<>();
        List<Move> possibleMoves = this.findPossibleMoves();
        for(Move move : possibleMoves){
            Board copyBoard = this.deepCopy();
            copyBoard.executeMove(move.getMove1());               // Place move on the copyBoard
            HashMap<BallType, Integer> yield = copyBoard.getYield();
            if(!yield.values().isEmpty()){
                //throw a party and lets go to the casino because we've got a valid move on our hands bois
                validMoves.add(move);
                this.singleMoveAvailable = true;
            }
        }

        return validMoves;
    }

    /**
     * A method that finds the moves that are possible to do. Though, the return value of this is not per say the moves that are valid.
     * @return A list of integers, indicating the moves that are possible given the current board state.
     */
    public List<Move> findPossibleMoves() {
        List<Move> possibleMoves = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < this.boardSize; rowIndex++) {
            Sequence row = this.rows.get(rowIndex);
            List<BallType> balls;

            for (int ballIndex = 0; ballIndex < (balls = row.getBalls()).size(); ballIndex++) {
                BallType ball = balls.get(ballIndex);

                List<BallType> rowBalls = row.getBalls();

                //If the sequence before or after the ball contains an empty ball, add it to the moves
                if(ball != BallType.EMPTY) {
                    if(rowBalls.subList(ballIndex, this.boardSize-1).contains(BallType.EMPTY)) {
                        possibleMoves.add(new Move(this.boardSize*3-1 - rowIndex));
                    }
                    if(rowBalls.subList(0, ballIndex).contains(BallType.EMPTY)) {
                        possibleMoves.add(new Move(rowIndex));
                    }
                    if(this.columns.get(ballIndex).getBalls().subList(rowIndex, this.boardSize-1).contains(BallType.EMPTY)) {
                        possibleMoves.add(new Move(this.boardSize * 3 + ballIndex));
                    }
                    if(this.columns.get(ballIndex).getBalls().subList(0, rowIndex).contains(BallType.EMPTY)) {
                        possibleMoves.add(new Move(this.boardSize + ballIndex));
                    }
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
        HashMap<Integer, BallType> toBeRemovedBalls = new HashMap<>();

        for(List<Sequence> sequenceList : rowsAndColumns) { //rows and columns
            for(int seq = 0; seq < sequenceList.size(); seq++) { //sequences in the row/column
                for (int element = 0; element < this.boardSize; element++) {  //elements in the sequence
                    int sameBallsInARow = sameBallsInSequence(sequenceList.get(seq), element, 1);
                    if (sameBallsInARow > 1) { //Houston, we got a score.


                        BallType thisBall = sequenceList.get(seq).getBalls().get(element);
                        //Store the coordinates of those feckers so they can be removed later
                        for(int offset = 0; offset < sameBallsInARow; offset++) {
                            toBeRemovedBalls.putIfAbsent(calculateBallCoordinates(sequenceList, seq, element + offset), thisBall);
                        }
                        element += sameBallsInARow; //Update the value of the iterator

                        //Save the ball and the amount of its neighbours to a HashMap for adding player score.
                    }
                }
            }
        }
        for(BallType b : toBeRemovedBalls.values()) {
            if(ballScore.containsKey(b)) {
                ballScore.put(b, ballScore.get(b) + 1);
            } else {
                ballScore.put(b, 1);
            }
        }
        removeYield(new HashSet<>(toBeRemovedBalls.keySet()));

        return ballScore;
    }

    /**
     * Calculates the coordinates of the ball, given a list of Sequences, what number in the list, and what element.
     * @param sequenceList A list of Sequences, which is either this.rows or this.columns.
     * @param sequenceNum The sequence index of the list.
     * @param elementNum The element index of the sequence.
     * @return the coordinates of the elementNum in sequenceNum, in the sequenceList.
     */
    public int calculateBallCoordinates(List<Sequence> sequenceList, int sequenceNum, int elementNum){
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
    public void removeYield(HashSet<Integer> toBeRemovedBalls){
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
        if(ballSequence == 0) ballSequence++; //count the first one
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

    public boolean isGameOver() {
        boolean gameOver = false;
        if (!findValidSingleMoves().isEmpty() || !findValidDoubleMoves().isEmpty()) {
            gameOver = true;
        }
        return gameOver;
    }

    /**
     * Creates a deep copy of the current board and returns this.
     * @return a deep copy of the board.
     */
    public Board deepCopy(){
        Board copyBoard = new Board(this.boardSize);
        copyBoard.fillBoard(getBoardState());
        return copyBoard;
    }

    @Override
    public String toString() {
        return getPrettyBoardState();
    }

    public List<Sequence> getColumns() {
        return this.columns;
    }

    public List<Sequence> getRows(){
        return this.rows;
    }
}
