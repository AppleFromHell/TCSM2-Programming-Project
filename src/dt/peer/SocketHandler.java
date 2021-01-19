package dt.peer;

import java.io.*;
import java.net.Socket;

public class SocketHandler implements Runnable {
    private Socket socket;
    private NetworkEntity networkEntity;
    private BufferedReader socketIn;
    private BufferedWriter socketOut;
    private String name;
    private boolean debug = false;

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
            networkEntity.handlePeerShutdown();
        }
    }

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
            networkEntity.handlePeerShutdown();
        }
    }

    public void write(String msg) {
        if(!socket.isClosed()) {
            try {
                if(debug) System.out.println("[OUT]:" + msg);
                socketOut.write(msg);
                socketOut.newLine();
                socketOut.flush();
            } catch (IOException e) {
                networkEntity.handlePeerShutdown();
            }
        }
    }
    public void setName(String name) {
        this.name = name;
    }
    public void shutDown() {
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
