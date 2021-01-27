package clientTests;


import dt.collectoClient.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {
    Client client;
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private dt.collectoClient.ClientStates ClientStates;
    private final String username = "testUser";

    PrintStream standardOut = System.out;
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
