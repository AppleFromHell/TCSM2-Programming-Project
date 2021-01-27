package modelTests;

import dt.model.BallType;
import dt.model.Sequence;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceTest {

    Sequence sequence;
    Sequence original;


    @Test
    void shiftRightOrdown() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.EMPTY);
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        sequence = new Sequence(balls);
        original = new Sequence(balls);
        sequence.shiftRightOrdown();
        assertEquals(original.getBalls(), sequence.getBalls());

        Sequence sequence = new Sequence(Arrays.asList(
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.EMPTY
        ));
        sequence.shiftRightOrdown();
        assertEquals(Arrays.asList(
            BallType.EMPTY,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW
        ), sequence.getBalls());

        sequence = new Sequence(Arrays.asList(
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY));
        sequence.shiftRightOrdown();

        assertEquals(Arrays.asList(
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE
        ), sequence.getBalls());

        sequence = new Sequence(Arrays.asList(
            BallType.BLUE,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY));
        sequence.shiftRightOrdown();

        assertEquals(Arrays.asList(
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.BLUE,
            BallType.BLUE
        ), sequence.getBalls());


        sequence = new Sequence(Arrays.asList(
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE));
        sequence.shiftRightOrdown();

        assertEquals(Arrays.asList(
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.BLUE
        ), sequence.getBalls());
    }

    @Test
    void shiftLeftOrUp() {
        List<BallType> balls = new ArrayList<>();
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        balls.add(BallType.BLUE);
        balls.add(BallType.YELLOW);
        balls.add(BallType.EMPTY);
        sequence = new Sequence(balls);
        original = new Sequence(balls);
        sequence.shiftLeftOrUp();
        assertEquals(original.getBalls(), sequence.getBalls());

        Sequence sequence = new Sequence(Arrays.asList(
            BallType.EMPTY,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW
        ));
        sequence.shiftLeftOrUp();
        assertEquals(Arrays.asList(
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.BLUE,
            BallType.YELLOW,
            BallType.EMPTY
        ), sequence.getBalls());

        sequence = new Sequence(Arrays.asList(
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE
        ));
        sequence.shiftLeftOrUp();

        assertEquals(Arrays.asList(
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY
        ), sequence.getBalls());

        sequence = new Sequence(Arrays.asList(
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.BLUE));
        sequence.shiftLeftOrUp();

        assertEquals(Arrays.asList(
            BallType.BLUE,
            BallType.BLUE,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY
        ), sequence.getBalls());


        sequence = new Sequence(Arrays.asList(
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.BLUE));
        sequence.shiftLeftOrUp();

        assertEquals(Arrays.asList(
            BallType.BLUE,
            BallType.BLUE,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY,
            BallType.EMPTY
        ), sequence.getBalls());
    }
}