package dt.peer;

import java.io.*;
import java.net.Socket;

/**
 * A hanlder of the socket. Sends and receives messages
 * @author Emiel Rous and Wouter Koning */
public class SocketHandler implements Runnable {
    private final Socket socket;
    private final NetworkEntity networkEntity;
    private BufferedReader socketIn;
    private BufferedWriter socketOut;
    private String name;
    private boolean debug = true;
    private boolean shutDown =false;

    public void run() {
        readSocketInput();
    }

    public SocketHandler(NetworkEntity networkEntity, Socket socket, String name) {
        this.networkEntity = networkEntity;
        this.socket = socket;
        this.name = name;

        try {
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socketOut = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
        } catch (IOException e) {
            networkEntity.handlePeerShutdown(this.shutDown);
        }
    }

    /**
     * Read the line from the socket.
     * @ensures A shutdown from either side results in a neat shutdown of the {@link NetworkEntity}
     */
    private void readSocketInput() {
        try {
            while(!socket.isClosed() && socketIn != null ) {
                String msg = socketIn.readLine();
                if(debug) System.out.println("[IN]:" +msg);
                if(msg == null){
                    throw new IOException();
                }
                networkEntity.handleMessage(msg);
            }
        } catch (IOException e) {
            networkEntity.handlePeerShutdown(shutDown);
        }
    }

    /**
     * Write a line to the socket.
     * @ensures A shutdown from either side results in a neat shutdown of the {@link NetworkEntity}
     */
    public void write(String msg) {
        if(!socket.isClosed()) {
            try {
                if(debug) System.out.println("[OUT]:" + msg);
                socketOut.write(msg);
                socketOut.newLine();
                socketOut.flush();
            } catch (IOException e) {
                networkEntity.handlePeerShutdown(shutDown);
            }
        }
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Neatly handles a shutdown
     */
    public void shutDown() {
        this.shutDown = true;
        try {
            this.socketIn = null;
            this.socketOut = null;
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDebug(Boolean state) {
        this.debug = state;
    }
}
