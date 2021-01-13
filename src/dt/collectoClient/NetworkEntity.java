package dt.collectoClient;

public interface NetworkEntity {

    void handleMessage(String msg);
    void handlePeerShutdown();
    void shutDown();
}
