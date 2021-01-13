package dt.collectoClient;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private Socket socket;
    private NetworkEntity networkEntity;
    private BufferedReader socketIn;
    private BufferedWriter socketOut;
    private boolean exit = false;
    private boolean debug = true;

    public void run() {
        readSocketInput();
    }

    public ConnectionHandler(NetworkEntity networkEntity, Socket socket) {
        this.networkEntity = networkEntity;
        this.socket = socket;

        try {
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socketOut = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSocketInput() {
        while (!exit) {
            try {
                if (!socket.isClosed() && socketIn != null) {
                    String msg = socketIn.readLine();
                    if(debug) System.out.println(">[Sever]:" +msg);
                    networkEntity.handleMessage(msg);
                }
            } catch (IOException e) {
                shutDown();//TODO dit ff uitzoeken. Wat gebuert er als de server shutdownt met ch
                networkEntity.handleShutdown();
            }
        }
    }

    public void write(String msg) {
        if(!socket.isClosed()) {
            try {
                if(debug) System.out.println("<[Client]:" + msg);
                socketOut.write(msg);
                socketOut.newLine();
                socketOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
}
