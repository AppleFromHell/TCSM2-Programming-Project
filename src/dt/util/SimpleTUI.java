package dt.util;

import dt.collectoClient.UserCmds;
import dt.exceptions.UserExit;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/** @author Emiel Rous and Wouter Koning */
public class SimpleTUI implements Runnable {

    protected SimpleTUI(){}
    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private final PrintWriter stdOut = new PrintWriter(System.out, true);

    /**
     * Asks the user a question, and then returns the input from the user.
     * @param question The question to ask the user.
     * @return The answer that the user has given.
     * @throws UserExit If the user types the exit command.
     */
    public String getString(String question) throws UserExit {
        showMessage(question);
        return getString();
    }

    /**
     * Retrieves a String from the standard in that the user has put in.
     * @return A String representation of what the user has put in to the system.
     * @throws UserExit If the user types the exit command.
     */
    public String getString() throws UserExit {
        String answer = null;
        try {
            answer = stdIn.readLine();
            if(UserCmds.getUserCmd(answer) == UserCmds.EXIT) throw new UserExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (answer == null || answer.equals("\n")) {
            return "Invalid Input";
        } else {
            return answer.replace("\n", "");
        }
    }

    /**
     * A method that is to be overwritten by other methods.
     */
    public void start(){}

    /**
     * Show the user a message given as the parameter.
     * @param message The message to be shown to the user.
     */
    public void showMessage(String message) {
        stdOut.println(message);
    }

    /**
     * Returns the port the TUI is using.
     * @return The port that the TUI is using
     * @throws UserExit If the user decides to exit the program.
     */
    public Integer getPort() throws UserExit {
        return getInt("Which Port is the server running on:");
    }

    /**
     * Gets an int representation of what the user has put into the system.
     * @param question A question to ask the user.
     * @return An int representation of what the user has put into the system.
     * @throws UserExit If the user decides to exit the program.
     */
    public int getInt(String question) throws UserExit {
        do {
            String answer = getString(question);
            try {
                if (answer.matches("-?\\d+")) {
                    return Integer.parseInt(answer);
                }
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer");
            }
        } while (true);
    }

    /**
     * Gets a boolean representation of what the user has put into the system.
     * @param question A question to ask the user.
     * @return A boolean representation of what the user has put into the system.
     * @throws UserExit If the user decides to exit the program.
     */
    public boolean getBoolean(String question) throws UserExit {
        do {
            String answer = getString(question);

            if (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")) {
                return true;
            } else if (answer.toLowerCase().equals("n") || answer.toLowerCase().equals("no")) {
                return false;
            }
            System.out.println("Enter a valid yes/no answer.");
        } while(true);
    }

    /**
     * To be overwritten by other classes.
     */
    public void run() {
        this.start();
    }


}
