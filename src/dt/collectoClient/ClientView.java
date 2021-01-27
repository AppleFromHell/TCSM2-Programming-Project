package dt.collectoClient;

import dt.ai.AITypes;
import dt.exceptions.CommandException;
import dt.exceptions.UserExit;
import dt.model.ClientBoard;
import dt.util.Move;

/**
 * Interface used by both GUI and TUI
 *
 * @author Emiel Rous and Wouter Koning
 */
public interface ClientView extends Runnable {

    String UNKOWNCOMMAND = "Unkown command: '%s' For a list of valid commands type h";
    String NOTINTEGERMOVE = "Move was not an integer";

    void start();

    void run();

    /**
     * Show a message in the console
     *
     * @param msg
     */
    void showMessage(String msg);

    void displayList(String[] list);

    /**
     * Prompt the user to reconnect
     *
     * @return
     * @throws UserExit
     */
    boolean reconnect() throws UserExit;

    void displayChatMessage(String msg);


    /**
     * Parse the arguments into a move
     *
     * @param arguments
     * @return
     * @throws CommandException
     * @requires the arguments to be integers and valid moves given the boardsize
     */
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

    void showBoard(ClientBoard board);

    void showHint(String toString);

    void showRank(String toString);
}
