package dt.model.board;

import dt.model.BallType;

import java.util.List;

class Sequence {
    private List<BallType> balls;
    int sequence;

    public Sequence(List<BallType> balls, int sequenceCount){
        this.balls = balls;
        this.sequence = sequenceCount;
    }

    public void shiftRightOrUp(){
        int emptyAmount = findEmptyBallAmount();
        while(emptyAmount != 0){
            for(int i = balls.size() - 2; i > 0; i--){
                if (balls.get(i + 1) == BallType.EMPTY) {
                    balls.set(i + 1, balls.get(i));
                }
            }
            emptyAmount--;
        }
    }

    public void shiftLeftOrDown(){
        int emptyAmount = findEmptyBallAmount();
        while(emptyAmount != 0) {
            for (int i = 1; i < balls.size(); i++) {
                if (balls.get(i - 1) == BallType.EMPTY) {
                    balls.set(i - 1, balls.get(i));
                }
            }
            emptyAmount--;
        }
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
