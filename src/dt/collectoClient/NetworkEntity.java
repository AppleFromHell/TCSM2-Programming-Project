package dt.collectoClient;

public interface NetworkEntity {

    void handleMessage(String msg);
    void handleShutdown();
}
