package dt.collectoClient.GUI;

import dt.model.BallType;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;

public class GameDisplay extends JPanel {
    private int[] gameState;
    private String[] balls;
    private Color[] ballColors;
    private final Color BACKGROUND;
    private final int BOARDSIZE = 7;
    private int ballSize;
    private int squareSize;

    public GameDisplay() {
        this.setEmptyBoard();
        this.ballColors = new Color[BallType.values().length];
        this.BACKGROUND = Color.WHITE;
        this.setBackground(BACKGROUND);
        this.setSize(new Dimension(400,400));
        this.balls = Arrays.stream(BallType.values()).map(BallType::toString).toArray(String[]::new);
        for(int i = 0; i < balls.length; i++) {
            try {
                Field field = Class.forName("java.awt.Color").getField(balls[i]);
                ballColors[i] = (Color)field.get(null);
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
    }

    public int getSquareSize() {
        return squareSize;
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int offset = 30;
        int width = this.getWidth() -offset;

        squareSize = width/this.BOARDSIZE;
        ballSize = squareSize - width / 100;

        for(int y = 0; y < this.BOARDSIZE; y++) {
            for (int x = 0; x < this.BOARDSIZE; x++) {
                int ballNumber = gameState[x+y * this.BOARDSIZE];
                g.setColor(ballColors[ballNumber]);
                g.fillOval(x * this.squareSize + offset/2, y * this.squareSize+offset/2, ballSize, ballSize);
            }
        }
        for(int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(3*this.BOARDSIZE + i), squareSize*i + squareSize - offset/2 -3, offset/2-3);
        }
        for(int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(this.BOARDSIZE * 2 - i -1), squareSize*i + squareSize/4*3, width + offset/4*3);
        }

        for(int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(i), width + offset/2, squareSize * i + squareSize - offset/2 + 3);
        }
        for(int i = 0; i < this.BOARDSIZE; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(this.BOARDSIZE* 3 - i -1), offset/20, squareSize * i + squareSize - offset/2 +3);
        }

        g.drawRect(offset/2, offset/2, width-offset/4, width-offset/4);

    }
    public static void main(String[] args) {
        JFrame win = new JFrame("Mandelbrot Set");
        win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int[] bord = new int[49];
        for(int i = 0; i < 7*7; i++) {
            bord[i] = i % 7;
        }
        GameDisplay gameDisplay = new GameDisplay(bord);
        win.setSize(400, 440);
        win.setContentPane(gameDisplay);
        win.setVisible(true);
    }

    public void setEmptyBoard() {
        this.gameState = new int[BOARDSIZE*BOARDSIZE];
        for(int i =0; i < BOARDSIZE * BOARDSIZE; i++) {
            gameState[i] = 0;
        }
    }
}
