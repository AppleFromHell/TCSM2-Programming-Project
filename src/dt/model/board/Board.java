package dt.model.board;

import dt.model.BallType;

import java.util.List;

public class Board {

    private List<Row> rows;
    private List<Column> columns;
    private final int boardSize;

    public Board(int boardSize){
        this.boardSize = boardSize;
    }

    public List<BallType> makeMove(int move){
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
