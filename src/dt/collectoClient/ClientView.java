package dt.collectoClient;

import dt.ai.AITypes;
import dt.exceptions.CommandException;
import dt.exceptions.UserExit;
import dt.model.ClientBoard;
import dt.util.Move;

/** @author Emiel Rous and Wouter Koning */
public interface ClientView extends Runnable {

    String UNKOWNCOMMAND = "Unkown command: '%s' For a list of valid commands type h";
    String NOTINTEGERMOVE = "Move was not an integer";

    void start();

    void run();

    void showMessage(String msg);

    void displayList(String[] list);

    boolean reconnect() throws UserExit;

    void displayChatMessage(String msg);

    default Move parseMove(String[] arguments) throws CommandException {
        if (arguments.length == 2) {
            return new Move(Integer.parseInt(arguments[1]));
        } else if (arguments.length == 3) {
            return new Move(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
        } else {
            throw new CommandException("Too many moves");
        }
    }

    void setClientAI(AITypes type) throws UserExit;

    void clearBoard();

    void showBoard(ClientBoard board);
}
