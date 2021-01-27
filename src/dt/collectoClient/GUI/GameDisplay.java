package dt.collectoClient.GUI;

import dt.model.BallType;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * The actual board with all the balls
 *
 * @author Emiel Rous and Wouter Koning
 */
public class GameDisplay extends JPanel {
    private final String[] balls;
    private final Color[] ballColors;
    private final Color BACKGROUND;
    private final int BOARDSIZE = 7;
    private int[] gameState;
    private int ballSize;
    private int squareSize;

    public GameDisplay() {
        this.setEmptyBoard();
        this.ballColors = new Color[BallType.values().length];
        this.BACKGROUND = Color.WHITE;
        this.setBackground(BACKGROUND);
        this.setSize(new Dimension(400, 400));
        this.balls = Arrays.stream(BallType.values()).map(BallType::toString).toArray(String[]::new);
        for (int i = 0; i < balls.length; i++) {
            try {
                Field field = Class.forName("java.awt.Color").getField(balls[i]);
                ballColors[i] = (Color) field.get(null);
            } catch (Exception e) {
                ballColors[i] = BACKGROUND; // Not defined
            }
        }
    }

    public GameDisplay(int[] gameState) {
        this();
        this.gameState = gameState;
    }

    public void setGameState(int[] state) {
        this.gameState = state;
        paintComponent(this.getGraphics());
    }

    public int getSquareSize() {
        return squareSize;
    }


    /**
     * Draw the board.
     *
     * @param g
     * @requires g to not be null
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int offset = 30;
        int width = this.getWidth() - offset;

        squareSize = width / this.BOARDSIZE;
        ballSize = squareSize - width / 100;

        for (int y = 0; y < this.BOARDSIZE; y++) {
            for (int x = 0; x < this.BOARDSIZE; x++) {
                int ballNumber = gameState[x + y * this.BOARDSIZE];
                g.setColor(ballColors[ballNumber]);
                g.fillOval(x * this.squareSize + offset / 2, y * this.squareSize + offset / 2, ballSize, ballSize);
            }
        }
        for (int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(3 * this.BOARDSIZE + i), squareSize * i + squareSize - offset / 2 - 3, offset / 2 - 3);
        }
        for (int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(this.BOARDSIZE * 2 + i), squareSize * i + squareSize / 4 * 3, width + offset / 4 * 3);
        }

        for (int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(i), width + offset / 2, squareSize * i + squareSize - offset / 2 + 3);
        }
        for (int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(this.BOARDSIZE + i), offset / 20, squareSize * i + squareSize - offset / 2 + 3);
        }

        g.drawRect(offset / 2, offset / 2, width - offset / 4, width - offset / 4);

    }

    /**
     * Set the board to an empty state
     */
    public void setEmptyBoard() {
        this.gameState = new int[BOARDSIZE * BOARDSIZE];
        for (int i = 0; i < BOARDSIZE * BOARDSIZE; i++) {
            gameState[i] = 0;
        }
    }
}
