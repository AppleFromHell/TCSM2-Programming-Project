package dt.exceptions;

/** @author Emiel Rous and Wouter Koning */
public class UserExit extends Exception {
    public UserExit(String msg) {
        super(msg);
    }
    public UserExit(){}
}
