package dt.server;

import java.io.*;
import java.net.Socket;

/**
 * Peer for a simple client-server application
 * @author  Theo Ruys
 * @version 2005.02.21
 */
public class Peer implements Runnable {
    public static final String EXIT = "exit";

    protected String name;
    protected Socket sock;
    protected BufferedReader in;
    protected BufferedWriter out;

    /**
     * @requires (nameArg != null) && (sockArg != null)
     * @param   nameArg name of the Peer process
     * @param   sockArg Socket of the Peer process
     */
    public Peer(String nameArg, Socket sockArg) {
        this.sock = sockArg;
        this.name = nameArg;
    }

    /**
     * Reads strings of the stream of the socket connection and
     * writes the characters to the default output.
     */
    public void run() {
        BufferedReader reader =null;
        try {

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < 10000; i++) {

            try {
                assert reader != null;
                if(!sock.isClosed()) {
                    System.out.println("\n" + reader.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("done looping");
    }

    /**
     * Reads a string from the console and sends this string over
     * the socket-connection to the Peer process.
     * On Peer.EXIT the method ends
     */
    public void handleTerminalInput() throws IOException {
        BufferedWriter writer = new BufferedWriter(new PrintWriter(this.sock.getOutputStream()));

//      THIS WORKS WHAT
//        PrintWriter printer = new PrintWriter(sock.getOutputStream());
//        BufferedWriter writer; // = new BufferedWriter(printer);
//        for(int i = 0; i < 100; i++){
//            writer = new BufferedWriter(printer);
//            printer = new PrintWriter(writer);
//        }

        while(true) {
            String input = String.format("[%s]: %s",name, readString(""));
            if(input.toLowerCase().contains("exit")) {
                break;
            }
            System.out.println(input);
            writer.write(input);
            writer.newLine();
            writer.flush();
        }
    }

    /**
     * Closes the connection, the sockets will be terminated
     */
    public void shutDown() throws IOException {
        this.sock.close();
    }

    /**  returns name of the peer object*/
    public String getName() {
        return this.name;
    }

    /** read a line from the default input */
    static public String readString(String text) {
        System.out.print(text);
        String antw = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            antw = in.readLine();
        } catch (IOException e) {
        }

        return (antw == null) ? "" : antw;
    }
}
