package dt.model;

import dt.util.DistributedRandomNumberGenerator;

import java.util.*;
import java.util.stream.Collectors;

/** @author Emiel Rous and Wouter Koning */
public class ServerBoard extends Board{

    private int[] boardToClient;

    public ServerBoard(){
        super();
    }

    public ServerBoard(int boardSize) {
        super(boardSize);
    }

    public void setupBoard(){
        do {
            int[] newBoard = createBoard();
            this.boardToClient = newBoard;
            super.fillBoard(newBoard);
        } while(findValidSingleMoves().isEmpty());
    }

    private int randomBall(){
        return randomNumber(1, BallType.values().length - 1);
    }

    private int randomBall(int except1){
        ArrayList<Integer> availableNumbers = new ArrayList<>();
        for(int i = 1; i < this.boardSize; i++){
            if(i != except1){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));
    }

    private int randomBall(int except1, int except2){
        ArrayList<Integer> availableNumbers = new ArrayList<>();
//        Collections.addAll(availableNumbers, 1, 2, 3, 4, 5, 6);
        for(int i = 1; i < this.boardSize; i++){
            if(i != except1 && i != except2){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));
    }

    private int randomBall(List<Integer> exceptions){
        List<Integer> availableNumbers = new ArrayList<>();
        for(int i = 1; i < this.boardSize; i++){
            if(!exceptions.contains(i)){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));
    }

    private int[] createBoard() {
        //fill the board up daddy
        int[] newBoard = new int[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;
        int[] ballValues = new int[6];
        int[] ballsLeft = new int[7];
        DistributedRandomNumberGenerator randomNumberGenerator = new DistributedRandomNumberGenerator();

        Arrays.fill(ballsLeft, 8);
        for (int i = 0; i < ballValues.length; i++) {
            ballValues[i] = i + 1;
            randomNumberGenerator.addNumber(i + 1, ballsLeft[i + 1]); //Add all the possible numbers to the generator.
        }

        for (int i = 0; i < this.boardSize * this.boardSize; i++) {
            int left = i - 1;
            int up = i - this.boardSize;
            if (i == middle) {
                newBoard[i] = 0;
            } else if (i % this.boardSize != 0 && up >= 0) {   //If it's not on the left edge, and not at the top
                newBoard[i] = randomBall(new ArrayList<>(List.of(newBoard[left], newBoard[up])));
//                newBoard[i] = randomNumberGenerator.getRandomNumber();
//                ballsLeft[newBoard[i]]--;
//                randomNumberGenerator.addNumber(newBoard[i], ballsLeft[newBoard[i]]);

            } else if (i % this.boardSize != 0 && left >= 0) { //If it's not on the left edge, and at the top
                newBoard[i] = randomBall(new ArrayList<>(List.of(newBoard[left])));
            } else if (up > 0) {                                    //If it's on the left edge, and not at the top
                newBoard[i] = randomBall(new ArrayList<>(List.of(newBoard[up])));
            } else {                                               //If it's on the left edge, and at the top
                newBoard[i] = randomBall();
            }
        }

        return newBoard;
    }

    private int[] createBoard2(){

        int[] newBoard = createRandomBoard();
        boolean boardFucked = true;

        while(!boardFucked) {
            for (int i = 0; i < newBoard.length; i++) {
                if (newBoard[i] == newBoard[i + 1]) {
                    swap(newBoard, i, i + 1);
                }
                if (newBoard[i] == newBoard[i + 7]) {
                    swap(newBoard, i, i + 7);
                }
            }
        }

        return newBoard;
    }

    private void swap(int[] array, int i1, int i2){
        int temp = array[i2];
        array[i2] = array[i1];
        array[i1] = temp;
    }

    private int[] createRandomBoard(){
        int[] newBoard = new int[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;
        List<BallType> ballList = List.of(BallType.values());
        Map<BallType, Integer> ballCount = new HashMap<>();
        for(BallType ball : ballList){
            ballCount.put(ball, 8);
        }
        ballCount.remove(BallType.EMPTY);

//        List<BallType> newBoard = new ArrayList<>();
        for(int i = 0; i < this.boardSize * this.boardSize; i++){
            if(i != middle) {
                int randomNr = randomNumber(0, ballCount.keySet().size() - 1);
                BallType randomKey = getFromSet(ballCount.keySet(), randomNr);
                ballCount.put(randomKey, ballCount.get(randomKey) - 1);
                if (ballCount.get(randomKey) == 0) {
                    ballCount.remove(randomKey);
                }
                assert randomKey != null;
                newBoard[i] = randomKey.ordinal();
            } else {
                newBoard[i] = 0;
            }
        }
        return newBoard;
    }

    private BallType getFromSet(Set<BallType> set, int getIndex){
        int i = 0;
        for(Iterator<BallType> it = set.iterator(); it.hasNext(); ){
            BallType ball = it.next();
            if(i == getIndex){
                return ball;
            }
            i++;
        }
        return null;
    }
}
