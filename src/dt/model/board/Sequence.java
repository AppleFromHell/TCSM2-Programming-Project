package dt.model.board;

import java.util.ArrayList;
import java.util.List;

public class Sequence {
    private List<BallType> balls;

    public Sequence(List<BallType> balls){
        this.balls = new ArrayList<>();
        this.balls = balls;
    }

    public List<BallType> getBalls(){
        return this.balls;
    }

    public void shiftRightOrdown(){
        int emptyAmount = findEmptyBallAmount();
        while(emptyAmount != 0){
            for(int i = balls.size() - 2; i > 0; i--){
                if (balls.get(i + 1) == BallType.EMPTY) {
                    for(int j = i; j > 0; j--) { //Shift all the balls after the one you're looking at as well
                        balls.set(j + 1, balls.get(j));
                    }
                }
            }
            emptyAmount--;
        }
        balls.set(0, BallType.EMPTY);
    }

    public void shiftLeftOrUp(){
        int emptyAmount = findEmptyBallAmount();
        while(emptyAmount != 0) {
            for (int i = 1; i < balls.size(); i++) {
                if (balls.get(i - 1) == BallType.EMPTY) {
                    for(int j = i; j < balls.size(); j++) { //Shift all the balls after the one you're looking at as well
                        balls.set(j - 1, balls.get(j));
                    }
                }
            }
            emptyAmount--;
        }
        balls.set(balls.size() - 1, BallType.EMPTY);
    }

    private int findEmptyBallAmount(){
        int returnValue = 0;
        for(BallType ball : balls){
            if (ball == BallType.EMPTY) {
                returnValue++;
            }
        }
        return returnValue;
    }
}
