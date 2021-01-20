package dt.model;

import dt.util.DistributedRandomNumberGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        } while(findValidMoves().isEmpty());
        System.out.println("Valid moves: "+ findValidMoves().toString());
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

    public int[] createBoard() {
        //TODO Make it so that only 8 balls of every colour exist in the board (use the DistributedRandomNumberGenerator)
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

        for (int i = 0; i < this.boardSize * this.boardSize; i++) { //i = 1
            int left = i - 1; //left =0
            int up = i - this.boardSize; //up = -6
            if (i == middle) {
                newBoard[i] = 0;
            } else if (i % this.boardSize != 0 && up >= 0) {   //If it's not on the left edge, and not at the top
                newBoard[i] = randomBall(new ArrayList<>(List.of(newBoard[left], newBoard[up])));
                newBoard[i] = randomNumberGenerator.getRandomNumber();
                ballsLeft[newBoard[i]]--;
                randomNumberGenerator.addNumber(newBoard[i], ballsLeft[newBoard[i]]);

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
}
