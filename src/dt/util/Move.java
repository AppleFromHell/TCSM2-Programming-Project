package dt.util;

import java.util.Objects;

/**
 * @author Emiel Rous and Wouter Koning
 * This a class that is used for transferring the moves across various classes in the system. The introduction of
 * this class meant that there was no need for methods specifically for double and single moves across the system.
 */
public class Move {

    private Integer move1 = null;
    private Integer move2 = null;

    public Move(Move move) {
        this.move1 = move.getMove1();
        this.move2 = move.getMove2();
    }

    public Move(int move1) {
        this.move1 = move1;
    }

    public Move(int move1, int move2) {
        this.move1 = move1;
        this.move2 = move2;
    }

    /**
     * Returns whether this move instance is a double move or not.
     *
     * @return Whether the move is a double move or not.
     */
    public boolean isDoubleMove() {
        return this.move2 != null;
    }

    /**
     * A get method for the first move
     *
     * @return the first move of the Move
     */
    public Integer getMove1() {
        return this.move1;
    }

    /**
     * A get method for the second move
     *
     * @return the second move of the Move
     */
    public Integer getMove2() {
        return this.move2;
    }

    /**
     * A method which returns whether the move instance is a move that is legal, and thus conform the procotol.
     *
     * @return Whether the move is a move conform protocol and not something that is out of bounds of the valid range
     * of the protocol.
     */
    public boolean isLegal() {
        boolean validSingle = move1 >= 0 && move1 <= 27;
        if (isDoubleMove()) {
            boolean validDouble = move2 >= 0 && move2 <= 27;
            return validSingle && validDouble;
        }
        return validSingle;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return Objects.equals(move1, move.move1) &&
                Objects.equals(move2, move.move2);
    }

    @Override
    public String toString() {
        return isDoubleMove() ? "[" + this.move1 + "][" + this.move2 + "]" : "[" + this.move1 + "]";
    }
}
