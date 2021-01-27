package dt.model;

import java.util.*;

/**
 * @author Emiel Rous and Wouter Koning
 * The extensions of the {@link Board} class, which only the server uses. This class is capable of board creation.
 */
public class ServerBoard extends Board {

    public ServerBoard() {
        super();
    }

    public void setupBoard() {
        do {
            int[] newBoard = createBoard();
            super.fillBoard(newBoard);
        } while (findValidSingleMoves().isEmpty());
    }

    /**
     * The method to be called when you want to create a new board.
     *
     * @return A valid board in the shape of {@link int[]}.
     * @ensures The board created will hold to the rules that there are 6 balls of which each 8 colours, none of
     * which lie next to to each other.
     */
    public int[] createBoard() {
        //fill the board up daddy
        BallType[] newBoard = createBallTypeBoard();

        fixNeighbouringBalls(newBoard);

        return convertToIntArray(newBoard);
    }

    /**
     * A method which converts a {@link BallType[]} to an {@link int[]} by applying the {@link Enum#ordinal()}
     * method to every element in the {@link BallType[]}.
     *
     * @param array The {@link BallType[]} to be converted to an {@link int[]}.
     * @return The converted {@link BallType[]} in the shape of an {@link int[]}.
     */
    private int[] convertToIntArray(BallType[] array) {
        int[] returnBoard = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            returnBoard[i] = array[i].ordinal();
        }
        return returnBoard;
    }

    /**
     * A method which creates a board using the Enum {@link BallType}. This is first done by iteratively placing balls
     * on the board, which places a ball that is not in conflict with one of its current neighbours. At some point,
     * it can happen that there is no more valid balls to place, after which it tries to switch all the balls that did
     * fit on the board with balls at another location as best it can.
     * <p>
     * It is recommended to run the method {@link ServerBoard#fixNeighbouringBalls(BallType[])} after this, to make
     * sure that the board creation is done proper and well.
     *
     * @return A representation of a board which is nearly correct.
     */
    public BallType[] createBallTypeBoard() {
        BallType[] newBoard = new BallType[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;

        List<BallType> ballList = new ArrayList<>(List.of(BallType.values()));
        ballList.remove(BallType.EMPTY);
        Map<BallType, Integer> availableBalls = new HashMap<>();

        for (BallType ball : ballList) {
            availableBalls.put(ball, 8);
        }

        int lastBallsIterator = 0;

        for (int i = 0; i < this.boardSize * this.boardSize;
             i++) { //Iterate through the various postions on the board.
            int left = i - 1;
            int up = i - this.boardSize;
            BallType insertBall = null;

            if (i == middle) {
                newBoard[i] = BallType.EMPTY;
            } else if (i % this.boardSize != 0 &&
                up >= 0) {   //If it's not on the left edge, and not at the top
                insertBall = getRandomBallKeyFromMap(availableBalls, newBoard[left], newBoard[up]);

                if (insertBall == null) { //shit hit the fan, we're gettin' out
                    lastBallsIterator = i;
                    break;
                }
                decrementAvailableBalls(availableBalls, insertBall);

                newBoard[i] = insertBall;

            } else if (i % this.boardSize != 0 &&
                left >= 0) { //If it's not on the left edge, and at the top
                insertBall = getRandomBallKeyFromMap(availableBalls, newBoard[left]);

                if (insertBall == null) { //shit hit the fan, we're gettin' out
                    lastBallsIterator = i;
                    break;
                }
                decrementAvailableBalls(availableBalls, insertBall);

                newBoard[i] = insertBall;

            } else if (up > 0) { //If it's on the left edge, and not at the top
                insertBall = getRandomBallKeyFromMap(availableBalls, newBoard[up]);

                if (insertBall == null) { //shit hit the fan, we're gettin' out
                    lastBallsIterator = i;
                    break;
                }
                decrementAvailableBalls(availableBalls, insertBall);

                newBoard[i] = insertBall;

            } else { //If it's on the left edge, and at the top
                insertBall = getRandomBallKeyFromMap(availableBalls);
                decrementAvailableBalls(availableBalls, insertBall);
                newBoard[i] = insertBall;
            }
        }

        while (availableBalls.size() !=
            0) { //Shit hit the fan at iteration i, preparing the squad we're moving in
            BallType insertBall = getRandomBallKeyFromMap(availableBalls);

            for (int i = 0; i < newBoard.length; i++) { // Loop through the board

                if (isSwapable(newBoard, i, insertBall) &&
                    isSwapable(newBoard, lastBallsIterator,
                        newBoard[i])) { //If the element is swapable both ways, swap them.

                    if (i !=
                        middle) { //Tho don't try to swap out the middle ball, because that needs to be empty.
                        newBoard[lastBallsIterator] = insertBall;
                        swap(newBoard, i, lastBallsIterator);
                        lastBallsIterator++;

                        decrementAvailableBalls(availableBalls, insertBall);
                        break;
                    }
                }
            }
        }
        return newBoard;
    }

    /**
     * This method decrement the amount of balls that are available, given a ball inserted. It then removes the ball
     * from the list of available balls if it is not available anymore.
     *
     * @param availableBalls The {@link Map} of balls that are available and how many of them.
     * @param insertedBall   The {@link BallType} which has just been inserted.
     */
    private void decrementAvailableBalls(Map<BallType, Integer> availableBalls,
                                         BallType insertedBall) {
        availableBalls.replace(insertedBall, availableBalls.get(insertedBall) -
            1); //removes 1 from the amount of balls removed from the available balls.
        if (availableBalls.get(insertedBall) == 0) {
            availableBalls.remove(insertedBall);
        }
    }

    /**
     * Iterates through all of the positions of the board, and sees whether any of them need any fixing up. If they do,
     * it tries to swap them with a position that they can go to, and the other ball can come back to.
     *
     * @param board The board that needs an additional check and possibly some fixing.
     * @ensures That that the {@link BallType[]} is now a valid board.
     */
    private void fixNeighbouringBalls(BallType[] board) {
        boolean validBoard = true;

        for (int i = 0; i < board.length; i++) {
            int up = i - 7;
            int down = i + 7;
            int left = i - 1;
            int right = i + 1;
            if (up > 0 && board[up] == board[i]) {
                validBoard = false;
            }
            if (down < board.length && board[down] == board[i]) {
                validBoard = false;
            }
            if ((right) % 7 != 0 && board[right] == board[i]) { //check for right
                validBoard = false;
            }
            if ((i + 1) % 7 > 0 && (i - 1 >= 0) && board[right] == board[i]) { //check for left
                validBoard = false;
            }
            if (!validBoard) {
                findAndExecuteSwap(board, i);
                i--;
                validBoard = true;
            }
        }
    }

    /**
     * Finds a place to swap a ball with, and then swaps that ball.
     *
     * @param board The board on which swapping will happen
     * @param index The index that needs to be swapped.
     */
    private void findAndExecuteSwap(BallType[] board, int index) {
        for (int i = 0; i < board.length; i++) {
            if (isSwapable(board, index, board[i]) && isSwapable(board, i, board[index])) {
                swap(board, i, index);
            }
        }
    }

    /**
     * Finds out whether a ball can put put on that position by checking whether the parameter ball matches with
     * any of its neighbours.
     *
     * @param board The array in which the swapping will be happening
     * @param index The index of the element which is being checked for whether it can be swapped
     * @param ball  The {@link BallType} which would like to be swapped.
     * @return whether the parameter ball is suitable at index.
     */
    public boolean isSwapable(BallType[] board, int index, BallType ball) {
        //TODO check whether it's swapable both ways.
        int up = index - 7;
        int down = index + 7;
        int left = index - 1;
        int right = index + 1;
        List<BallType> neighbours = new ArrayList<>();

        if (up > 0) {
            neighbours.add(board[up]);
        }
        if (down < board.length) {
            neighbours.add(board[down]);
        }
        if (index % 7 > 0) {
            neighbours.add(board[left]);
        }
        if (right % 7 > 0) {
            neighbours.add(board[right]);
        }

        for (BallType neighbour : neighbours) {
            if (neighbour == ball) {
                return false;
            }
        }

        return true;
    }

    /**
     * Swap two elements in an array of {@link BallType}.
     *
     * @param array The array which has the swapping going on.
     * @param b1    Index 1 of the swapping
     * @param b2    Index 2 of the swapping.
     * @ensures That the elements at the two indexes are now swapped, and nothing else has changed.
     */
    public void swap(BallType[] array, int b1, int b2) {
        BallType temp = array[b2];
        array[b2] = array[b1];
        array[b1] = temp;
    }

    /**
     * Retrieves a random key from a {@link Map}.
     *
     * @param map The map which of which to return a random key from.
     * @return A random key from the map.
     * @requires map != null and !map.isEmpty()
     */
    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map) {
        int randomInt = Board.randomNumber(0, map.keySet().size() - 1);
        return getFromSet(map.keySet(), randomInt);
    }

    /**
     * Retrieves a random key from a {@link Map}, under the constraint of a single value.
     *
     * @param map     The map which of which to return a random key from.
     * @param except1 A value the method cannot return.
     * @return A random key that is not except1.
     */
    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map, BallType except1) {
        List<BallType> availableBalls =
            new ArrayList<>(); //Create a list of what balls are available.
        for (BallType ball : map.keySet()) {
            if (ball != except1) {
                availableBalls.add(ball);
            }
        }

        if (availableBalls.size() == 0) {
            return null;
        }

        int randomInt = Board.randomNumber(0, availableBalls.size() - 1);
        return availableBalls.get(randomInt);
    }

    /**
     * Retrieves a random key from a {@link Map}, under the constraint of two values.
     *
     * @param map     The map which of which to return a random key from.
     * @param except1 A value the method cannot return.
     * @param except2 A value the method cannot return.
     * @return A random key that is not except1 or except2.
     */
    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map, BallType except1,
                                             BallType except2) {
        List<BallType> availableBalls = new ArrayList<>();
        for (BallType ball : map.keySet()) {
            if (ball != except1 && ball != except2) {
                availableBalls.add(ball);
            }
        }
        if (availableBalls.size() == 0) {
            return null;
        }
        int randomInt = Board.randomNumber(0, availableBalls.size() - 1);
        return availableBalls.get(randomInt);
    }

    /**
     * Retrieves an item at index getIndex from a {@link Set}, since {@link Set} does not have its own get method.
     *
     * @param set      The set to be retrieving something from.
     * @param getIndex The index at you want to have something retrieved from.
     * @return The element of the set at index getIndex
     */
    private BallType getFromSet(Set<BallType> set, int getIndex) {
        int i = 0;
        for (Iterator<BallType> it = set.iterator(); it.hasNext(); ) {
            BallType ball = it.next();
            if (i == getIndex) {
                return ball;
            }
            i++;
        }
        return null;
    }
}
