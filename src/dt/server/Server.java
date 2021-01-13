
package dt.server;

import dt.exceptions.ServerUnavailableException;
import dt.protocol.ClientMessages;
import dt.protocol.ProtocolMessages;
import dt.protocol.ServerMessages;

import java.io.*;
import java.net.*;

/**
 * Server.
 * @author  Theo Ruys
 * @version 2005.02.21
 */
public class Server {
    private BufferedReader in;
    private BufferedWriter out;
    private Integer port;

    /** Starts a Server-application. */
    public static void main(String[] args) throws IOException, ServerUnavailableException {
        Server serverr = new Server();
        if(args.length > 0 ) serverr.port = Integer.parseInt(args[0]);

        ServerTUI view = new ServerTUI(serverr);
        view.start();



        String name = args[0];
        InetAddress addr = null;
        Socket sock = null;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverr.port);
        } catch (IOException e) {
//            e.printStackTrace();

        }

        //wait until client connects
        try {
            assert serverSocket != null;
            sock = serverSocket.accept();
            System.out.println("I have found the one.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverr.in = new BufferedReader(new InputStreamReader(
                sock.getInputStream()));
        serverr.out = new BufferedWriter(new OutputStreamWriter(
                sock.getOutputStream()));

        serverr.doHello();
        Peer peer = new Peer("Emiel", sock);
        new Thread(peer).start();
        System.out.println("Thread shutdown");
        peer.handleTerminalInput();
        System.out.println("Server shutdown");
    }

    private synchronized void write(String input) throws ServerUnavailableException {
        if(out != null) {
            try {
                out.write(input);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new ServerUnavailableException("Could not write to server");
            }
        } else {
            throw new ServerUnavailableException("Socket is not available");
        }
    }
    public void doHello() throws ServerUnavailableException, ProtocolException {
        String rawResponse = readLineFromServer();
        String[] response = rawResponse.split(ProtocolMessages.delimiter);
        if(!response[0].equals(ClientMessages.HELLO.toString())) {
            throw new ProtocolException("SHIT BROKE");
        }
        System.out.println("[CLIENT]" + response[1]);
        write(ServerMessages.HELLO.constructMessage("Server by yo fat mama"));
    }

    private String readLineFromServer() throws ServerUnavailableException {
        if (in != null) {
            try {
                // Read and return answer from Server
                String answer = in.readLine();
                if (answer == null) {
                    throw new ServerUnavailableException("Could not read "
                            + "from server.");
                }
                return answer;
            } catch (IOException e) {
                throw new ServerUnavailableException("Could not read "
                        + "from server.");
            }
        } else {
            throw new ServerUnavailableException("Could not read "
                    + "from server.");
        }
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return this.port;
    }
} // end of class Server
