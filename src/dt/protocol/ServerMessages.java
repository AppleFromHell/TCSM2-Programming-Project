package dt.protocol;

import java.util.List;

public enum ServerMessages implements ProtocolMessages {
    HELLO (Messages.HELLO),
    LOGIN (Messages.LOGIN),
    ALREADYLOGGEDIN (Messages.ALREADYLOGGEDIN),
    LIST (Messages.LIST),
    NEWGAME (Messages.NEWGAME),
    MOVE (Messages.MOVE),
    GAMEOVER (Messages.GAMEOVER),
    ERROR (Messages.ERROR)
    ;

    public enum GameOverReasons {
        DRAW,
        DISCONNECT,
        VICTORY
    }
    private String msg = "";

    ServerMessages(ProtocolMessages.Messages msg) {
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
        String msg = this.msg;
        for(String arg : args) {
            msg += delimiter + arg;
        }
        return msg;
    }
}
