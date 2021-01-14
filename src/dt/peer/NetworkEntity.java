package dt.peer;

public interface NetworkEntity {

    void handleMessage(String msg);
    void handlePeerShutdown();
    void shutDown();
}
