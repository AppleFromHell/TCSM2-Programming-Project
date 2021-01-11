package dt.model.board;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerBoard extends Board{

    private int[] boardToClient;

    public ServerBoard(){
        super();
    }

    public ServerBoard(int boardSize) {
        super(boardSize);
    }

    private void setupBoard(){
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
        List<Integer> availableNumbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        availableNumbers.remove(except1);
        return availableNumbers.get(randomNumber(1, availableNumbers.size()));
    }

    private int randomBall(int except1, int except2){
        List<Integer> availableNumbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        availableNumbers.remove(except1);
        availableNumbers.remove(except2);
        return availableNumbers.get(randomNumber(1, availableNumbers.size()));
    }

    public int[] createBoard(){
        //fill the board up daddy
        int[] newBoard = new int[BOARDSIZE * BOARDSIZE];
        int middle = (BOARDSIZE * BOARDSIZE + 1) / 2;

        for(int i = 0; i < BOARDSIZE * BOARDSIZE; i++) {
            int left = i - 1;
            int up = i - BOARDSIZE;
            if(i == middle){
                newBoard[i] = 0;
            } else if (left % BOARDSIZE != 0 && up > 0) {
                newBoard[i] = randomBall(newBoard[left], newBoard[up]);
            } else if (left % BOARDSIZE != 0) {
                newBoard[i] = randomBall(newBoard[left]);
            } else if (up > 0){
                newBoard[i] = randomBall(newBoard[up]);
            } else {
                newBoard[i] = randomBall();
            }
        }

        return newBoard;


    }





}
