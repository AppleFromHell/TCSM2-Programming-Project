package clientTests;

import dt.collectoClient.CollectoClient;
import dt.exceptions.ServerUnavailableException;
import dt.server.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerClientIntegrationTest {
    CollectoClient client = new CollectoClient();

    static class ServerRunner implements Runnable {
        public void run() {
            try {
                Server.main(new String[] {"Server by Emiel", "888"});
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServerUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
    @BeforeAll
    static void setup() {
        ServerRunner runner = new ServerRunner();
        new Thread(runner).start();
    }

    @Test
    void startup() throws UnknownHostException {
        client.setIp(InetAddress.getByName("localhost"));
        client.setPort(888);
        client.setUsername("TestClient");
        client.createConnection();
    }
    @Test
    void testDoHello() throws ServerUnavailableException, ProtocolException {
        client.doHello();
    }
}
