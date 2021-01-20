package dt.server;

import dt.collectoClient.UserCmds;
import dt.exceptions.UserExit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** @author Emiel Rous and Wouter Koning */
public class SimpleTUI implements Runnable {

    protected SimpleTUI(){}
    private final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private final PrintWriter stdOut = new PrintWriter(System.out, true);


    protected String getString(String question) throws UserExit {
        showMessage(question);
        return getString();
//        String answer = null;
//        try {
//            answer = stdIn.readLine();
//            if(UserCmds.getUserCmd(answer) == UserCmds.EXIT) throw new UserExit();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (answer == null || answer.equals("\n")) {
//            return "Invalid Input";
//        } else {
//            return answer.replace("\n", "");
//        }
    }

    protected String getString() throws UserExit {
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

    public void start(){}

    public void showMessage(String message) {
        stdOut.println(message);
    }

    protected Integer getPort() throws UserExit {
        return getInt("Which Port is the server running on:");
    }

    protected int getInt(String question) throws UserExit {
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

    protected boolean getBoolean(String question) throws UserExit {
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

    public void run() {
        this.start();
    }


}
