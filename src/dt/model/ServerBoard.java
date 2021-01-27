package dt.model;

import java.util.*;

/** @author Emiel Rous and Wouter Koning */
public class ServerBoard extends Board{

    private int[] boardToClient;

    public ServerBoard(){
        super();
    }

    public ServerBoard(int boardSize) {
        super(boardSize);
    }

    public void setupBoard(){
        do {
            int[] newBoard = createBoard();
            this.boardToClient = newBoard;
            super.fillBoard(newBoard);
        } while(findValidSingleMoves().isEmpty());
    }

    private int randomBall(){
        return randomNumber(1, BallType.values().length - 1);
    }

    private int randomBall(int except1){
        ArrayList<Integer> availableNumbers = new ArrayList<>();
        for(int i = 1; i < this.boardSize; i++){
            if(i != except1){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));
    }

    private int randomBall(int except1, int except2){
        ArrayList<Integer> availableNumbers = new ArrayList<>();
//        Collections.addAll(availableNumbers, 1, 2, 3, 4, 5, 6);
        for(int i = 1; i < this.boardSize; i++){
            if(i != except1 && i != except2){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));
    }

    private int randomBall(List<Integer> exceptions){
        List<Integer> availableNumbers = new ArrayList<>();
        for(int i = 1; i < this.boardSize; i++){
            if(!exceptions.contains(i)){
                availableNumbers.add(i);
            }
        }
        return availableNumbers.get(randomNumber(0, availableNumbers.size() - 1));

    }

    public int[] createBoard(){
        //fill the board up daddy
        BallType[] newBoard = createBallTypeBoard();

        checkForMatchingNeighbours(newBoard);

        int[] returnBoard = convertToIntArray(newBoard);

        return returnBoard;
    }

    private boolean checkForMatchingNeighbours(BallType[] board){
        boolean validBoard = true;

        for(int i = 0; i < board.length; i++){
            int up = i - 7;
            int down = i + 7;
            int left = i - 1;
            int right = i + 1;
            if(up > 0 && board[up] == board[i]){
                validBoard = false;
            }
            if(down < board.length && board[down] == board[i]){
                validBoard = false;
            }
            if((right) % 7 != 0 && board[right] == board[i]){ //check for right
                validBoard = false;
            }
            if((i + 1) % 7 > 0 && (i - 1 >= 0) && board[right] == board[i]){ //check for left
                validBoard = false;
            }
            if(!validBoard){
                findAndExecuteSwap(board, i);
                i--;
                validBoard = true;
            }
        }

        return validBoard;
    }

    private void findAndExecuteSwap(BallType[] board, int index){
        for(int i = 0; i < board.length; i++){
            if(isSwapable(board, index, board[i]) && isSwapable(board, i, board[index])){
                swap(board, i, index);
            }
        }
    }

    private int[] convertToIntArray(BallType[] array){
        int[] returnBoard = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            returnBoard[i] = array[i].ordinal();
        }
        return returnBoard;
    }

    public BallType[] createBallTypeBoard(){
        BallType[] newBoard = new BallType[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;

        List<BallType> ballList = new ArrayList<>(List.of(BallType.values()));
        ballList.remove(BallType.EMPTY);
        Map<BallType, Integer> ballCount = new HashMap<>();

        for(BallType ball : ballList){
            ballCount.put(ball, 8);
        }

        int forLoopLeftOff = 0;

        for(int i = 0; i < this.boardSize * this.boardSize; i++) {
            int left = i - 1;
            int up = i - this.boardSize;

            if(i == middle) {
                newBoard[i] = BallType.EMPTY;
            } else if (i % this.boardSize != 0 && up >= 0) {   //If it's not on the left edge, and not at the top
                BallType randomBall = getRandomBallKeyFromMap(ballCount, newBoard[left], newBoard[up]);
                if(randomBall == null){ //shit hit the fan, we're gettin' out
                    forLoopLeftOff = i;
                    break;
                }
                ballCount.replace(randomBall, ballCount.get(randomBall) - 1);
                newBoard[i] = randomBall;

            } else if (i % this.boardSize != 0 && left >= 0) { //If it's not on the left edge, and at the top
                BallType randomBall = getRandomBallKeyFromMap(ballCount, newBoard[left]);
                if(randomBall == null){ //shit hit the fan, we're gettin' out
                    forLoopLeftOff = i;
                    break;
                }
                ballCount.replace(randomBall, ballCount.get(randomBall) - 1);
                newBoard[i] = randomBall;

            } else if (up > 0){ //If it's on the left edge, and not at the top
                BallType randomBall = getRandomBallKeyFromMap(ballCount, newBoard[up]);
                if(randomBall == null){ //shit hit the fan, we're gettin' out
                    forLoopLeftOff = i;
                    break;
                }
                ballCount.replace(randomBall, ballCount.get(randomBall) - 1);
                newBoard[i] = randomBall;

            } else { //If it's on the left edge, and at the top
                BallType randomBall = getRandomBallKeyFromMap(ballCount);
                ballCount.replace(randomBall, ballCount.get(randomBall) - 1);
                newBoard[i] = randomBall;
            }

            Set<BallType> toBeRemoved = new HashSet<>();
            for (BallType key : ballCount.keySet()){ //Checking whether a certain ball has already been placed enough.
                if(ballCount.get(key) == 0){
                    toBeRemoved.add(key);
                }
            }

            for(BallType removeKey : toBeRemoved){ //Removing the balls from the list that need not be on there anymore.
                ballCount.remove(removeKey);
            }

        }

        while(ballCount.size() != 0){ //Shit hit the fan at iteration i, preparing the squad we're moving in
            BallType insertBall = getRandomBallKeyFromMap(ballCount);

            for(int i = 0; i < newBoard.length; i++) { // Loop through the board

                if (isSwapable(newBoard, i, insertBall) &&
                        isSwapable(newBoard, forLoopLeftOff, newBoard[i])) { //If the element is swapable both ways, swap them

                    if (i != middle) {
                        newBoard[forLoopLeftOff] = insertBall;
                        swap(newBoard, i, forLoopLeftOff);
                        forLoopLeftOff++;
                        ballCount.replace(insertBall, ballCount.get(insertBall) - 1); // Decrease the ballcount counter.

                        if (ballCount.get(insertBall) == 0) { //If the counter is at 0, remove it from the list.
                            ballCount.remove(insertBall);
                        }
                        break;
                    }
                }

            }
        }
        return newBoard;
    }

    public boolean isSwapable(BallType[] board, int index, BallType ball){
        //TODO check whether it's swapable both ways.
        int up = index - 7;
        int down = index + 7;
        int left = index - 1;
        int right = index + 1;
        List<BallType> neighbours = new ArrayList<>();

        if(up > 0){
            neighbours.add(board[up]);
        }
        if(down < board.length){
            neighbours.add(board[down]);
        }
        if(index % 7 > 0){
            neighbours.add(board[left]);
        }
        if(right % 7 > 0){
            neighbours.add(board[right]);
        }

        for(BallType neighbour : neighbours){
            if(neighbour == ball){
                return false;
            }
        }

        return true;
    }

    private void swap(int[] array, int i1, int i2){
        int temp = array[i2];
        array[i2] = array[i1];
        array[i1] = temp;
    }

    public void swap(BallType[] array, int b1, int b2){
        BallType temp = array[b2];
        array[b2] = array[b1];
        array[b1] = temp;
    }

    public int[] createRandomBoard(){
        int[] newBoard = new int[this.boardSize * this.boardSize];
        int middle = (this.boardSize * this.boardSize - 1) / 2;
        List<BallType> ballList = List.of(BallType.values());
        Map<BallType, Integer> ballCount = new HashMap<>();

        for(BallType ball : ballList){
            ballCount.put(ball, 8);
        }
        ballCount.remove(BallType.EMPTY);

//        List<BallType> newBoard = new ArrayList<>();
        for(int i = 0; i < this.boardSize * this.boardSize; i++){
            if(i != middle) {
                int randomNr = randomNumber(0, ballCount.keySet().size() - 1);
                BallType randomKey = getFromSet(ballCount.keySet(), randomNr);
                ballCount.put(randomKey, ballCount.get(randomKey) - 1);
                if (ballCount.get(randomKey) == 0) {
                    ballCount.remove(randomKey);
                }
                assert randomKey != null;
                newBoard[i] = randomKey.ordinal();
            } else {
                newBoard[i] = 0;
            }
        }
        return newBoard;
    }

    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map){
        int randomInt = Board.randomNumber(0, map.keySet().size() - 1);
        return getFromSet(map.keySet(), randomInt);
    }

    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map, BallType except1){
        List<BallType> availableBalls = new ArrayList<>(); //Create a list of what balls are available.
        for (BallType ball : map.keySet()){
            if(ball != except1){
                availableBalls.add(ball);
            }
        }

        if(availableBalls.size() == 0){
            return null;
        }

        int randomInt = Board.randomNumber(0, availableBalls.size() - 1);
        return availableBalls.get(randomInt);
    }

    private BallType getRandomBallKeyFromMap(Map<BallType, Integer> map, BallType except1, BallType except2){
        List<BallType> availableBalls = new ArrayList<>();
        for (BallType ball : map.keySet()){
            if(ball != except1 && ball != except2){
                availableBalls.add(ball);
            }
        }
        if(availableBalls.size() == 0){
            return null;
        }
        int randomInt = Board.randomNumber(0, availableBalls.size() - 1);
        return availableBalls.get(randomInt);
    }

    private BallType getFromSet(Set<BallType> set, int getIndex){
        int i = 0;
        for(Iterator<BallType> it = set.iterator(); it.hasNext(); ){
            BallType ball = it.next();
            if(i == getIndex){
                return ball;
            }
            i++;
        }
        return null;
    }
}
