package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class CommandException extends Exception {
    public CommandException(String invalid_number_of_arguments_give) {
        super(invalid_number_of_arguments_give);
    }
}
