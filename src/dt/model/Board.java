package dt.model;

import dt.exceptions.InvalidMoveException;
import dt.util.Move;

import java.util.*;

/** @author Emiel Rous and Wouter Koning
 * This class is the class that stores all the board data and houses all the logic for the game.
 */
public class Board {
    private static final int BOARDSIZE = 7;

    private final List<Sequence> rows;
    private final List<Sequence> columns;
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

    /**
     * Retrieves the size of the board.
     * @return the size of the board.
     */
    public int getBoardSize(){
        return this.boardSize;
    }

    /**
     * Fills the board given an integer array. It fills the board with rows and columns, which are both of
     * the {@link Sequence} class.
     * @param newBoard
     */
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
            //Continuously creates Sequences and adds these until there are an amount equal to the board size.
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

    /**
     * Returns a {@link BallType} value, given a certain integer.
     * @param ball The ordinal of the enumerator.
     * @return a {@link BallType} value corresponding to the parameter given.
     */
    private BallType findBallType(int ball){
        BallType[] allTypes = BallType.values();
        return allTypes[ball];
    }

    /**
     * The method that is responsible for proper handling and executing of moves. This method runs past a whole lot
     * of other functions that together make a legal and valid move on the board.
     * @requires the parameter given to not be null.
     * @param move The {@link Move} that is wanted to be made on the board.
     * @return The yield that the board gives after making a move.
     * @throws InvalidMoveException When the move made is not a valid move.
     * @ensures oldBoard != newBoard.
     * @ensures that the balls removed from the board are returned.
     * @ensures No more than boardsize * (boardsize - 1) coloured balls can ever be present in a game.
     */
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
     * @requires The parameter given to the method to be a move that is conform protocol (0 <= move <= 27)
     * @requires The parameter given to the method to be a move that is possible given the current board
     * @param move The move to be executed, conform the protocol.
     * @ensures oldboard != newboard.
     * @ensures rows and columns are synchronized again after a move has been performed.
     */
    public void executeMove(int move) {
        boolean changedColumn = false;

        if(move < this.boardSize) { // < 7
            rows.get(move).shiftLeftOrUp();
        } else if( move < (2 * this.boardSize)) { //< 14
            rows.get(move - this.boardSize).shiftRightOrdown();
        } else if( move < (3 * this.boardSize)) { //<21
            columns.get(move - (2 * this.boardSize)).shiftLeftOrUp();
            changedColumn = true;
        } else if( move < (4 * this.boardSize)) { //<28
            columns.get( move -( 3 * this.boardSize)).shiftRightOrdown();
            changedColumn = true;
        }

        if(changedColumn){
            synchronize(this.columns, this.rows);
        } else {
            synchronize(this.rows, this.columns);
        }
    }

    /**
     * A method which synchronizes the rows and columns that this board holds.
     * @param upToDateList The {@link List<Sequence>} that has been changed
     * @param outdatedList The {@link List<Sequence>} that has not been changed and thus needs changing
     * @ensures rows and columns are synchronized again after a move has been performed.
     */
    public void synchronize(List<Sequence> upToDateList, List<Sequence> outdatedList) {
        for (int r = 0; r < this.boardSize; r++){
            Sequence row = upToDateList.get(r);
            for(int b = 0; b < this.boardSize; b++){
                BallType ball = row.getBalls().get(b);
                outdatedList.get(b).getBalls().set(r, ball);
            }
        }
    }

    /**
     * Finds out whether the move passed to the method is a move that is valid. It does this by checking whether
     * the move is legal via {@link Move#isLegal()}, after which it checks whether it is either a valid single move
     * via {@link Board#findValidSingleMoves()} if the parameter given is a single move. If the parameter given is
     * a double move, it first checks whether a single move was possible or not. If not, it goes to find whether
     * {@link Board#findValidDoubleMoves()} returns a move that is equal to the parameter..
     * @param move The move to be checked for whether it is valid or not.
     * @return The validity of the move.
     * @throws InvalidMoveException If the move you tried to make is a double move, but a single move is possible.
     * @throws InvalidMoveException If the move is a move that is not conform protocol.
     * @ensures The board is not changed.
     */
    public boolean isValidMove(Move move) throws InvalidMoveException {
        boolean validity = false;

        if(move.isLegal()) {
            //calling isDoubleMove lowers complexity if it is a single move
            if (!move.isDoubleMove()) { //If it is a single move
                List<Move> validSingleMoves = findValidSingleMoves();
                validity = validSingleMoves.contains(move);
            } else if (move.isDoubleMove()) { //If it is a double move
                if(!findValidSingleMoves().isEmpty())
                    throw new InvalidMoveException("You tried to make a double move, while a single move is still available.");
                validity = findValidDoubleMoves().contains(move);
            }
        } else {
            throw new InvalidMoveException("Move integer given is lower than 0 or higher than 27");
        }

        return validity;
    }

    /**
     * A method which returns all valid moves possible. It only returns a {@link List<Move>} of single moves or
     * double moves, but not one containing both. If there are no single moves available, it returns a list of
     * double moves.
     * @return A list of the moves which are valid on the current board.
     * @ensures The board is not changed.
     */
    public List<Move> findValidMoves() {
        List<Move> validMoves = findValidSingleMoves();
        if(validMoves.isEmpty()){
            validMoves = findValidDoubleMoves();
        }
        return validMoves;
    }

    /**
     * A method which returns a list of all valid double moves.
     * @return A {@link List<Move>} of valid double moves.
     * @ensures The board is not changed.
     */
    public List<Move> findValidDoubleMoves(){
        List<Move> validDoubleMoves = new ArrayList<>();
        List<Move> possibleMoves = this.findPossibleMoves();
        for(Move move1 : possibleMoves){
            Board copyBoard = this.deepCopy();
            copyBoard.executeMove(move1.getMove1());
            List<Move> possibleSecondMoves = copyBoard.findValidSingleMoves();
            for(Move move2 : possibleSecondMoves){
                validDoubleMoves.add(new Move(move1.getMove1(), move2.getMove1())); //All of these bois are goin' to work, hell yea
            }
        }
        return validDoubleMoves;
    }

    /**
     * A method which returns a list of all valid single moves.
     * @return A {@link List<Move>} of valid single moves.
     * @ensures The board is not changed.
     */
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
            }
        }

        return validMoves;
    }

    /**
     * A method that finds the moves that are possible to do. Though, the return value of this is not per
     * say the moves that are valid. This method only finds the moves that are possible on a single move.
     * @return A {@link List<Move>}, indicating the moves that are possible given the current board state.
     * @ensures The board is not changed.
     */
    public List<Move> findPossibleMoves(){
        List<Move> possibleMoves = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < this.boardSize; rowIndex++) {
            Sequence row = this.rows.get(rowIndex);
            List<BallType> balls;

            for (int ballIndex = 0; ballIndex < (balls = row.getBalls()).size(); ballIndex++) {
                BallType ball = balls.get(ballIndex);

                List<BallType> rowBalls = row.getBalls();

                //If the sequence before or after the ball contains an empty ball, add it to the moves
                if(ball != BallType.EMPTY) {

                    Move move = new Move(this.boardSize + rowIndex);
                    //Check the rows and columns for an empty square
                    //Check from ballindex to right of board -> left to right possible
                    if(rowBalls.subList(ballIndex, this.boardSize).contains(BallType.EMPTY) &&
                        !possibleMoves.contains(move)) {
                        possibleMoves.add(move);
                    }
                    //Check from left to ballindex -> right to left possible
                    move = new Move(rowIndex);
                    if(rowBalls.subList(0, ballIndex).contains(BallType.EMPTY) &&
                        !possibleMoves.contains(move)) {
                        possibleMoves.add(move);
                    }

                    //Check row index to bottom -> top to bottom possible
                    move = new Move(this.boardSize * 3 + ballIndex);
                    if(this.columns.get(ballIndex).getBalls().subList(rowIndex, this.boardSize).contains(BallType.EMPTY) &&
                        !possibleMoves.contains(move)) {
                        possibleMoves.add(move);
                    }
                    //Check top to rowindex -> bottom to top possible
                    move = new Move(2 * this.boardSize + ballIndex);
                    if(this.columns.get(ballIndex).getBalls().subList(0, rowIndex).contains(BallType.EMPTY) &&
                        !possibleMoves.contains(move)){
                        possibleMoves.add(move);
                    }
                }

            }
        }
        return possibleMoves;
    }

    /**
     * Go over all rows and columns and find the type and amount of balls that lie next to each other and then
     * remove those from the board.
     * @return a HashMap<BallType, Integer> with as key the {@link BallType}, and as value the amount of balls the move yielded.
     * @ensures oldBoard != newboard.
     */
    public HashMap<BallType, Integer> getYield(){
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
                        element += sameBallsInARow - 1; //Update the value of the iterator
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
     * Calculates the coordinates of the ball, given a {@link List<Sequence>}, what number in the list, and what element.
     * @param sequenceList A {@link List<Sequence>}, which is either this.rows or this.columns.
     * @param sequenceNum The sequence index of the {@link List}.
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
     * @requires The index given to the method to not be negative.
     * @param sequence The sequence in which to look for similar balls.
     * @param index The index of the element to be looking at in the sequence and from which to be finding equal elements.
     * @param ballSequence The amount of balls found that are the same in the sequence
     * @return The amount of balls found that are the same in the sequence
     * @ensures The board is not changed.
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

    /**
     * A method which returns the board state in the form of an {@link int[]}.
     * @return An {@link int[]} which contains the board state.
     */
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

    /**
     * A method which makes the board state readable for human beings.
     * @return A {@link String} which contains a boardstate that is legible for human beings.
     */
    public String getPrettyBoardState(){
        int[] boardState = getBoardState();
        String rowSeparator = "---+---+---+---+---+---+---";
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
                board.append(System.lineSeparator())
                        .append(rowSeparator)
                        .append(System.lineSeparator());
            }
        }
        return board.toString().replace("0", " "); //Make the empty places in the board an empty cell.
//        return board.toString();
    }

    /**
     * A method which founds out whether the game is over or not by checking whether there are still moves available to do.
     * @return Whether the game is over or not.
     */
    public boolean isGameOver() {
        boolean gameOver = false;
        if (this.findValidMoves().isEmpty()) {
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

    /**
     * Returns the columns of the board.
     * @return Returns the columns of the board.
     */
    public List<Sequence> getColumns() {
        return this.columns;
    }

    /**
     * Returns the rows of the board.
     * @return Returns the rows of the board.
     */
    public List<Sequence> getRows(){
        return this.rows;
    }

    /**
     * Creates a random int between a minimum and a maximum, given to the method. This uses {@link Math#random()}.
     * @param min The lowest value it is allowed to be.
     * @param max The highest value it is allowed to be.
     * @return A random int value such that min <= return <= max
     */
    static public int randomNumber(int min, int max){
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
