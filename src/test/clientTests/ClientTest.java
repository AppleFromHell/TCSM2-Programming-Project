package clientTests;


import dt.collectoClient.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientTest {
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final String username = "testUser";
    Client client;
    PrintStream standardOut = System.out;
    private dt.collectoClient.ClientStates ClientStates;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        this.setOutContent();
        this.client = new Client();
        this.client.setIp(InetAddress.getByName("130.89.253.65"));
        this.client.setPort(4114);
        this.client.setUsername(username);
        this.testLogin();
    }

    void setOutContent() {
        System.setOut(new PrintStream(outContent));
    }

    void testLogin() throws InterruptedException {
        this.client.start();
        TimeUnit.MILLISECONDS.sleep(50); //Let it wait for a response from the server
        assertTrue(outContent.toString().contains("Handshake successful"));
        outContent.reset();

        this.client.doLogin(username);
        TimeUnit.MILLISECONDS.sleep(50); //Let it wait for a response from the server
        outContent.reset();
    }

    @Test
    void testDoList() {

    }

    void print() {
        String out = outContent.toString();
        System.setOut(standardOut);
        System.out.println(out);
        setOutContent();
    }
}
