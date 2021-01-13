package dt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SimpleTUI {

    private final String EXITSTATEMENT = "exit";
    protected boolean exit = false;
    protected SimpleTUI(){}
    private final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private final PrintWriter stdOut = new PrintWriter(System.out, true);


    protected String getString(String question) {
        showMessage(question);
        String answer = null;
        try {
            answer = stdIn.readLine();
            if(answer.equals(EXITSTATEMENT)) exit = true;
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

    protected Integer getPort() {
        return getInt("Which Port is the server running on:");
    }

    protected int getInt(String question) {
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

    protected boolean getBoolean(String question) {
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
