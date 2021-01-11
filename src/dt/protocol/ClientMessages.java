package dt.protocol;

import java.util.List;

public enum ClientMessages implements ProtocolMessages {
    HELLO (Messages.HELLO),
    LOGIN (Messages.LOGIN),
    LIST (Messages.LIST),
    MOVE (Messages.MOVE),
    QUEUE (Messages.QUEUE);

    private String msg = "";

    ClientMessages(Messages msg) {
        this.msg = msg.toString();
    }

    public String constructMessage() {
        return this.msg;
    }
    public String constructMessage(String arg) {
        return this.msg + delimiter + arg;
    }
    public String constructMessage(String arg1, String arg2) {
        return this.msg + delimiter + arg1 + delimiter + arg2;
    }
    public String constructMessage(List<String> args) {
        StringBuilder msg = new StringBuilder(this.msg);
        for(String arg : args) {
            msg.append(delimiter).append(arg);
        }
        return msg.toString();
    }
}
