package dt.util;

import java.util.Objects;

public class Move {

    private Integer move1 = null;
    private Integer move2 = null;

    public Move(int move1){
        this.move1 = move1;
    }

    public Move(int move1, int move2){
        this.move1 = move1;
        this.move2 = move2;
    }

    public boolean isDoubleMove(){
        return this.move2 != null;
    }

    public Integer getMove1(){
        return this.move1;
    }

    public Integer getMove2(){
        return this.move2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(move1, move.move1) &&
                Objects.equals(move2, move.move2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(move1, move2);
    }

    @Override
    public String toString() {
        return isDoubleMove()? "[" + this.move1 + "][" + this.move2 + "]" : "[" + this.move1 + "]";
    }

    public String toServerMove() {
        return isDoubleMove()? "[" + this.move1 + "][" + this.move2 + "]" : "[" + this.move1 + "]";
    }

    public boolean isLegal(){
        boolean validSingle = move1 >= 0 && move1 <= 27;
        if(isDoubleMove()){
            boolean validDouble = move2 >= 0 && move2 <= 27;
            return validSingle && validDouble;
        }
        return validSingle;
    }


}
