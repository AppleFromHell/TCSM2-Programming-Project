package dt.peer;

/** @author Emiel Rous and Wouter Koning */
public interface NetworkEntity {

    void handleMessage(String msg);
    void handlePeerShutdown();
    void shutDown();
}
