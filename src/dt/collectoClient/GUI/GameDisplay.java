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
        this.paintComponent(this.getGraphics());
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

        for(int y = 0; y < this.BOARDSIZE * squareSize; y+= squareSize) {
            for (int x = 0; x < this.BOARDSIZE * squareSize; x+= squareSize) {
                int ballNumber = gameState[x/squareSize+(y/squareSize)];
                g.setColor(ballColors[ballNumber]);
                g.fillOval(x + offset/2, y+offset/2, ballSize, ballSize);
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
        GameDisplay gameDisplay = new GameDisplay(new int[]{1,2,4,5,2,4,3,2,2,6,6,4,3,3,2,3,4,0,5,3,5,4,4,3,2,4,5,3,2,3,4,4,3,2,1,1,2,3,4,5,4,3,2,2,3,4,5,2,2});
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
