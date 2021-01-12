package dt.model.board;

import dt.exceptions.InvalidMoveException;

import java.util.*;

public abstract class Board {
    public static final int BOARDSIZE = 7;

    private List<Sequence> rows;
    private List<Sequence> columns;
    private int boardSize;

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

    protected void fillBoard(int[] newBoard){ //Parse van int[] to BallType[]
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
        executeMove(move);
        return getYield();
    }

    public HashMap<BallType, Integer> makeMove(int move1, int move2) throws InvalidMoveException {
        executeMove(move1);
        executeMove(move2);
        return getYield();
    }

    private void executeMove(int move) throws InvalidMoveException {
        if(isValidMove(move)) {
            if (move > (3 * BOARDSIZE) - 1) { // 20
                columns.get(move - (3 * BOARDSIZE)).shiftLeftOrDown();
            } else if (move > (2 * BOARDSIZE) - 1) { // 13
                columns.get(move - (2 * BOARDSIZE)).shiftRightOrUp();
            } else if (move > BOARDSIZE - 1) { // 6
                rows.get(move - BOARDSIZE).shiftRightOrUp();
            } else {
                rows.get(move).shiftLeftOrDown();
            }
        }
    }

    public boolean isValidMove(int move) throws InvalidMoveException {
        if(move < 0 || move > 27){
            throw new InvalidMoveException("Move integer given is lower than 0 or higher than 27");
        }
        return findValidMoves().contains(move);
    }

    public boolean isValidMove(int move1, int move2) throws InvalidMoveException {
        return isValidMove(move1) && isValidMove(move2);
    }

    public List<Integer> findValidMoves() {
        //findPossibleMoves();

        //Find the moves that result in a yield.
        return null;
    }

    public List<Integer> findPossibleMoves(){
        List<Integer> possibleMoves = new ArrayList<>();
        for(int r = 0; r < BOARDSIZE; r++){
            Sequence row = rows.get(r);
            List<BallType> balls;

            for(int b = 0; b < (balls = row.getBalls()).size(); b++){
                BallType ball = balls.get(b);

                if(ball.equals(BallType.EMPTY)){
                    // Check whether it can move left
                    if(b > 0) possibleMoves.add(r);

                    // Check whether it can move right
                    if(b < BOARDSIZE - 1) possibleMoves.add(r + BOARDSIZE);

                    // check whether it can move up
                    if(r > 0) possibleMoves.add(b + (2 * BOARDSIZE));

                    // Check whether it can move down
                    if(r < BOARDSIZE - 1) possibleMoves.add(b + (3 * BOARDSIZE));
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Go over all rows and columns and find the type and amount of balls that lie next to each other.
     * @return a HashMap<BallType, Integer> with as key the BallType, and as value the amount of balls the move yielded.
     */
    public HashMap<BallType, Integer> getYield(){
        HashMap<BallType, Integer> ballScore = new HashMap<>();
        List<List<Sequence>> rowsAndColumns = new ArrayList<>();
        rowsAndColumns.add(this.rows);
        rowsAndColumns.add(this.columns);
        HashSet<Integer> toBeRemovedBalls = new HashSet<>();

        for(List<Sequence> sequenceList : rowsAndColumns) { //rows and columns
            for(int seq = 0; seq < sequenceList.size(); seq++) { //sequences in the row/column
                for (int i = 0; i < this.boardSize; i++) {  //elements in the sequence
                    int sameBalls = sameBallsInSequence(sequenceList.get(seq), i, 1);
                    if (sameBalls > 1) {
                        i += sameBalls - 1;
                        //Store the coordinates of that fecker so you can remove it later
                        toBeRemovedBalls.add(calculateBallCoordinates(sequenceList, seq, i));
                        //Save the ball and the amount of its neighbours to a HashMap for adding player score.
                        BallType thisBall = sequenceList.get(seq).getBalls().get(i);
                        if (!ballScore.containsKey(thisBall)) {
                            ballScore.put(thisBall, sameBalls);
                        }
                        ballScore.replace(thisBall, sameBalls);
                    }
                }
            }
        }

        removeYield(toBeRemovedBalls);

        return ballScore;
    }

    private int calculateBallCoordinates(List<Sequence> sequenceList, int sequenceNum, int elementNum){
        if(sequenceList == this.rows){
            return sequenceNum * this.boardSize + elementNum;
        } else {
            return elementNum * this.boardSize + sequenceNum;
        }
    }

    private void removeYield(HashSet<Integer> toBeRemovedBalls){
        for (int coord : toBeRemovedBalls) {
            //for the rows
            int yCoord = coord / 7;
            int xCoord = coord % 7;
            this.rows.get(yCoord).getBalls().set(xCoord, BallType.EMPTY);
            this.columns.get(xCoord).getBalls().set(yCoord, BallType.EMPTY);

        }
    }

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

    public List<Sequence> getBoardState(){
        return null;
    }
    public String getPrettyBoardState (){
        return null;
    }

    public boolean isGameOver(){
        return false;
    }
}
