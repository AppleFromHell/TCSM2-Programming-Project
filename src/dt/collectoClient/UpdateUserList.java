package dt.collectoClient;

import java.util.TimerTask;

public class UpdateUserList extends TimerTask {
    private Client client;

    public UpdateUserList(Client client) {
        this.client = client;
    }
    public void run() {
        client.doGetList();
    }
}
