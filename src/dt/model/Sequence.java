package dt.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emiel Rous and Wouter Koning
 * The class only used in {@link Board}. This class stores the actual board data in sequences. This means that
 * this is the data storage class of both all the columns and all the rows.
 */
public class Sequence {
    private List<BallType> balls;

    public Sequence(List<BallType> balls) {
        this.balls = new ArrayList<>();
        this.balls = balls;
    }

    /**
     * Returns all the balls this sequence has stored.
     *
     * @return The balls of this sequence.
     */
    public List<BallType> getBalls() {
        return this.balls;
    }

    /**
     * A method which shift all the balls towards the end of the list. This is a method both used for when
     * shifting right and when shifting down.
     *
     * @ensures The same number of balls are still in the list, but only the order has changed
     * @ensures The order of the list of balls has changed, such that there is only empty spots at the front of the array
     */
    public void shiftRightOrdown() {
        int emptyAmount = findEmptyBallAmount();
        while (emptyAmount != 0) {
            for (int i = balls.size() - 2; i >= 0; i--) {
                if (balls.get(i + 1) == BallType.EMPTY) {
                    for (int j = i; j >= 0;
                         j--) { //Shift all the balls after the one you're looking at as well
                        balls.set(j + 1, balls.get(j));
                        balls.set(j, BallType.EMPTY);
                    }
                }
            }
            emptyAmount--;
        }
        balls.set(0, BallType.EMPTY);
    }

    /**
     * A method which shift all the balls towards the beginning of the list. This is a method both used for when
     * shifting up and when shifting left.
     *
     * @ensures The same number of balls are still in the list, but only the order has changed
     * @ensures The order of the list of balls has changed, such that there is only empty spots at the back of the array
     */
    public void shiftLeftOrUp() {
        int emptyAmount = findEmptyBallAmount();
        while (emptyAmount != 0) {
            for (int i = 1; i < balls.size(); i++) {
                if (balls.get(i - 1) == BallType.EMPTY) {
                    for (int j = i; j < balls.size();
                         j++) { //Shift all the balls after the one you're looking at as well
                        balls.set(j - 1, balls.get(j));
                        balls.set(j, BallType.EMPTY);
                    }
                }
            }
            emptyAmount--;
        }
        balls.set(balls.size() - 1, BallType.EMPTY);
    }

    /**
     * A method which finds how many empty balls there are in the Sequence.
     *
     * @return The amount of empty balls in the sequence.
     */
    private int findEmptyBallAmount() {
        int returnValue = 0;
        for (BallType ball : balls) {
            if (ball == BallType.EMPTY) {
                returnValue++;
            }
        }
        return returnValue;
    }
}
