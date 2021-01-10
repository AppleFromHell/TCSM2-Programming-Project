package dt.collectoClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientTUI implements ClientView, Runnable {
    private final String EXITSTATEMENT = "exit";

    private BufferedReader stdIn = new BufferedReader(
            new InputStreamReader(System.in));
    private PrintWriter stdOut = new PrintWriter(System.out, true);

    private CollectoClient collectoClient;
    private boolean exit = false;


    ClientTUI(CollectoClient collectoClient) {
        this.collectoClient = collectoClient;
    }

    @Override
    public void start() {
        while(collectoClient.getIp() == null) {
            setIp(getIp());
        }
        while(collectoClient.getPort() == null) {
            setPort(getPort());
        }
        while(collectoClient.getUserName() == null) {
            setUsername(getUsername());
        }
        collectoClient.createConnection();

        while(!exit) {
            try {
                String input = getString("What would you like to do?");
                handleUserInput(input);
            } catch (IOException e) {
                stdOut.println("Shit broke");
            }
        }
    }




    private void handleUserInput(String input) throws IOException {
        collectoClient.write(input);
    }


    public String getString(String question) {
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
            return answer;
        }
    }

    public void showMessage(String message) {
        stdOut.println(message);
    }

    @Override
    public void setIp(InetAddress ip) {
        this.collectoClient.setIp(ip);
    }

    @Override
    public void setUsername(String username) {
        this.collectoClient.setUsername(username);
    }

    @Override
    public void setPort(Integer port) {
        this.collectoClient.setPort(port);
    }

    private String getUsername() {
        return getString("What username would you like to have?");
    }

    private Integer getPort() {
        return getInt("Which Port is the server running on:");
    }

    public int getInt(String question) {
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

    public boolean getBoolean(String question) {
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

    public InetAddress getIp() {
        while(!exit) {
            try {
                return InetAddress.getByName(getString("What IP address is the server running on (format: x.x.x.x)"));
            } catch (UnknownHostException e) {
                showMessage("Invalid IP, try again. Format: x.x.x.x where x stands for 1-3 integers");
            }
        }
        return null;
    }

    @Override
    public void run() {
        this.start();
    }
}
