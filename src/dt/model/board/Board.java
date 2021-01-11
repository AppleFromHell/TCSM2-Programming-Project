package dt.model.board;

import dt.model.BallType;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int BOARDSIZE = 7;

    private List<Sequence> rows;
    private List<Sequence> columns;
    private final int boardSize;
    public Board() {this.boardSize = BOARDSIZE;}
    public Board(int boardSize){
        this.boardSize = boardSize;
        rows = new ArrayList<>();
        columns = new ArrayList<>();

//        for(int i = 0; i < this.boardSize; i++){
//            rows.add(new Row)
//        }

    }

    public List<BallType> makeMove(int move){
        if(move < 0 || move > 28){
            //youDumbFuckException
        } else if (move > 20) {
            columns.get(move - 21).shiftLeftOrDown();
        } else if (move > 13){

        }

        return null;
    }

    public List<BallType> makeMove(int move1, int move2){
        return null;
    }

    public List<BallType> getYield(){
        return null;
    }

    public boolean isValidMove(int move){
        return false;
    }

    public boolean isValidMove(int move1, int move2){
        return false;
    }

    public String getBoardState(){
        return null;
    }

    public boolean isGameOver(){
        return false;
    }

}
