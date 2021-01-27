package dt.peer;

/**
 * An entity that can exist in a network. Either client or server
 *
 * @author Emiel Rous and Wouter Koning
 */
public interface NetworkEntity {

    void handleMessage(String msg);

    void handlePeerShutdown(boolean ownShutdown);

    void shutDown();
}
