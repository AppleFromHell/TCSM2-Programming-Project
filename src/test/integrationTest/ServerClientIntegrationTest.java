package integrationTest;

import dt.collectoClient.Client;
import dt.server.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerClientIntegrationTest {
    static String username = "TestClient";
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    Client client = new Client();
    PrintStream standardOut = System.out;
    static Server  server;
    static int port = 6969;
    private final int TIMEOUT = 100;

    private dt.collectoClient.ClientStates ClientStates;

    @BeforeAll
    static void setup() {
        ServerRunner runner = new ServerRunner();
        new Thread(runner).start();
        server = runner.getServer();
        System.setOut(new PrintStream(outContent));
    }

    @BeforeEach
    void startup() throws IOException, InterruptedException {

        client.setIp(InetAddress.getByName("localhost"));
        client.setPort(port);
        client.setUsername(username);
        client.start();
        timeOut(TIMEOUT); //Let it wait for a response from the server

    }

    @Test
    void testDoHello() throws InterruptedException {
        assertTrue(outContent.toString().contains("HELLO~"));
    }

    @Test
    void testLogin() throws InterruptedException {
        client.doLogin(username);
        timeOut(TIMEOUT); //Let it wait for a response from the server
        assertTrue(outContent.toString().contains("LOGIN"));
    }

    @Test
    void testUsernameAlreadyTaken() throws IOException, InterruptedException {
        client.doLogin(username);
        Client clientDouble = new Client();
        clientDouble.setIp(InetAddress.getByName("localhost"));
        clientDouble.setPort(port);
        clientDouble.setUsername(username);
        clientDouble.createConnection();
        clientDouble.doLogin(username);
        timeOut(TIMEOUT); //Let it wait for a response from the server
        assertTrue(outContent.toString().contains("ALREADYLOGGEDIN"));
    }

    @Test
    void testWhisper() throws InterruptedException {
        client.doLogin(username);
        client.setDebug(false);
        String whisperMessage = "This is a message that is being whispered";
        timeOut(TIMEOUT); //Let it wait for a response from the server

        client.doSendWhisper(username, whisperMessage);
        assertTrue(outContent.toString().contains(username));
        assertTrue(outContent.toString().contains(whisperMessage));
        String receiver = "ClientThatIsNotConnected";

        outContent.reset();
        client.doSendWhisper("ClientThatIsNotConnected", whisperMessage);
        timeOut(TIMEOUT); //Let it wait for a response from the server

        assertTrue(outContent.toString().contains(receiver));
        assertTrue(outContent.toString().contains("Cannot receive whispers"));
    }

    @Test
    void testClientServerBoardEquality() throws IOException, InterruptedException {
        System.setOut(standardOut);
        InetAddress ip = InetAddress.getByName("localhost");
        String client1Name = "client1";
        String client2Name = "client2";

        timeOut(TIMEOUT); //Let it wait for a response from the server
        Client client1 = new Client();
        client1.setIp(ip);
        client1.setPort(port);
        client1.setUsername(client1Name);
        client1.createConnection();
        timeOut(TIMEOUT); //Let it wait for a response from the server
        client1.doLogin(client1Name);
        timeOut(TIMEOUT); //Let it wait for a response from the server
        client1.doEnterQueue();
        timeOut(TIMEOUT); //Let it wait for a response from the server


        Client client2 = new Client();
        client2.setIp(ip);
        client2.setPort(port);
        client2.setUsername(client2Name);
        client2.createConnection();
        timeOut(TIMEOUT); //Let it wait for a response from the server
        client2.doLogin(client2Name);
        timeOut(TIMEOUT); //Let it wait for a response from the server
        client2.doEnterQueue();
        timeOut(TIMEOUT); //Let it wait for a response from the server

        TimeUnit.MILLISECONDS
            .sleep(TIMEOUT * 10); //Let it wait for the server to throw both clients into a game.

        System.out.println(client1.getState());
        System.out.println(client2.getState());

        assertTrue(client1.getState() == dt.collectoClient.ClientStates.WAITOURMOVE ||
            client1.getState() == dt.collectoClient.ClientStates.WAITTHEIRMOVE);

        assertTrue(client2.getState() == dt.collectoClient.ClientStates.WAITOURMOVE ||
            client2.getState() == dt.collectoClient.ClientStates.WAITTHEIRMOVE);

        int[] serverBoardState =
            server.getClientHandler(client1Name).getGame().getBoard().getBoardState();
        int[] clientBoardState = client1.getBoard().getBoardState();

        assertArrayEquals(serverBoardState, clientBoardState);
    }
    private void timeOut(int wait) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(wait);
    }
    static class ServerRunner implements Runnable {
        public Server server;
        public ServerRunner() {
            this.server = new Server();
            this.server.setPort(port);
        }
        public void run() {
            server.start();
        }
        public Server getServer(){
            return this.server.getServer();
        }
    }


}
