package clientTests;

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
    Client client = new Client();
    static String username = "TestClient";
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private dt.collectoClient.ClientStates ClientStates;
    PrintStream standardOut = System.out;


    static class ServerRunner implements Runnable {

        public void run() {
            Server.main(new String[] {"888"});
        }
    }
    @BeforeAll
    static void setup() {
        ServerRunner runner = new ServerRunner();
        new Thread(runner).start();
        System.setOut(new PrintStream(outContent));
    }

    @BeforeEach
    void startup() throws IOException {

        client.setIp(InetAddress.getByName("localhost"));
        client.setPort(6969);
        client.setUsername(username);
        client.start();

    }

    @Test
    void testDoHello(){
        assertTrue(outContent.toString().contains("HELLO~"));
    }

    @Test
    void testLogin() throws InterruptedException {
        client.doLogin(username);
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        assertTrue(outContent.toString().contains("LOGIN"));
    }

    @Test
    void testUsernameAlreadyTaken() throws IOException, InterruptedException {
        client.doLogin(username);
        Client clientDouble = new Client();
        clientDouble.setIp(InetAddress.getByName("localhost"));
        clientDouble.setPort(888);
        clientDouble.setUsername(username);
        clientDouble.createConnection();
        clientDouble.doLogin(username);
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        assertTrue(outContent.toString().contains("ALREADYLOGGEDIN"));
    }

    @Test
    void testWhisper() throws InterruptedException {
        client.doLogin(username);
        client.setDebug(false);
        String whisperMessage = "This is a message that is being whispered";
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server

        client.doSendWhisper(username, whisperMessage);
        assertTrue(outContent.toString().contains(username));
        assertTrue(outContent.toString().contains(whisperMessage));
        String receiver = "ClientThatIsNotConnected";

        outContent.reset();
        client.doSendWhisper("ClientThatIsNotConnected", whisperMessage);
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server

        assertTrue(outContent.toString().contains(receiver));
        assertTrue(outContent.toString().contains("Cannot receive whispers"));
    }

    @Test
    void testClientServerBoardEquality() throws IOException, InterruptedException {
        System.setOut(standardOut);
        int port = 888;
        InetAddress ip = InetAddress.getByName("localhost");
        String client1Name = "client1";
        String client2Name = "client2";

        Server server = Server.testMain((new String[] {String.valueOf(port)}));
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        Client client1 = new Client();
        client1.setIp(ip);
        client1.setPort(port);
        client1.setUsername(client1Name);
        client1.createConnection();
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        client1.doLogin(client1Name);
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        client1.doEnterQueue();
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server


        Client client2 = new Client();
        client2.setIp(ip);
        client2.setPort(port);
        client2.setUsername(client2Name);
        client2.createConnection();
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        client1.doLogin(client2Name);
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server
        client2.doEnterQueue();
        TimeUnit.MILLISECONDS.sleep(500); //Let it wait for a response from the server

        TimeUnit.MILLISECONDS.sleep(1000); //Let it wait for the server to throw both clients into a game.

        System.out.println(client1.getState());
        System.out.println(client2.getState());

        assertTrue(client1.getState() == dt.collectoClient.ClientStates.WAITOURMOVE ||
                client1.getState() == dt.collectoClient.ClientStates.WAITTHEIRMOVE);

        assertTrue(client2.getState() == dt.collectoClient.ClientStates.WAITOURMOVE ||
                client2.getState() == dt.collectoClient.ClientStates.WAITTHEIRMOVE);

        int[] serverBoardState = server.getClientHandler(client1Name).getGame().getBoard().getBoardState();
        int[] clientBoardState = client1.getBoard().getBoardState();

        assertArrayEquals(serverBoardState, clientBoardState);
    }



}
