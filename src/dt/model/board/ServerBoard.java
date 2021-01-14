package dt.model.board;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerBoard extends Board{

    private int[] boardToClient;

    public ServerBoard(){
        super();
    }

    public ServerBoard(int boardSize) {
        super(boardSize);
    }

    public void setupBoard(){
        int[] newBoard = createBoard();
        this.boardToClient = newBoard;
        super.fillBoard(newBoard);
    }

    private int randomNumber(int min, int max){
        return (int) (Math.random() * (max - min + 1) + min);
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

    public int[] createBoard(){
        //fill the board up daddy
        int[] newBoard = new int[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;

        for(int i = 0; i < this.boardSize * this.boardSize; i++) { //i = 1
            int left = i - 1; //left =0
            int up = i - this.boardSize; //up = -6
            if(i == middle) {
                newBoard[i] = 0;
            } else if (i % this.boardSize != 0 && up >= 0) {   //If it's not on the left edge, and not at the top
                newBoard[i] = randomBall(newBoard[left], newBoard[up]);
            } else if (i % this.boardSize != 0 && left >= 0) { //If it's not on the left edge, and at the top
                newBoard[i] = randomBall(newBoard[left]);
            } else if (up > 0){                                    //If it's on the left edge, and not at the top
                newBoard[i] = randomBall(newBoard[up]);
            } else {                                               //If it's on the left edge, and at the top
                newBoard[i] = randomBall();
            }
        }

        return newBoard;
    }
}
