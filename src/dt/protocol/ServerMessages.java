package dt.protocol;

import dt.model.board.Board;
import dt.server.Server;

import java.util.List;

public enum ServerMessages implements ProtocolMessages {
    HELLO (Messages.HELLO) {
        @Override
        public String constructMessage(Server server) {
            StringBuilder strB = new StringBuilder(HELLO.msg);
            strB.append(delimiter).append(server.getName());
            if(server.chatIsEnabled()) strB.append(delimiter).append(Messages.CHAT);
            if(server.rankIsEnabled()) strB.append(delimiter).append(Messages.RANK);
            if(server.authIsEnabled()) strB.append(delimiter).append(Messages.AUTH);
            if(server.cryptIsEnabled()) strB.append(delimiter).append(Messages.CRYPT);
            return strB.toString();
        }
    },
    LOGIN (Messages.LOGIN),
    ALREADYLOGGEDIN (Messages.ALREADYLOGGEDIN),
    LIST (Messages.LIST),
    NEWGAME (Messages.NEWGAME) {
        @Override
        public String constructMessage(int[] boardState, String plyr1, String plyr2) {
            StringBuilder strB = new StringBuilder(NEWGAME.msg);
            for(int c : boardState) {
                strB.append(delimiter).append(c);
            }
            strB.append(delimiter).append(plyr1).append(delimiter).append(plyr2);
            return strB.toString();
        }
    },
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
    public String constructMessage(Server server) {return "SERVER ERROR: Invalid access to Enum";}
    public String constructMessage(int[] boardState, String plyr1, String plyr2) {return "SERVER ERROR: Invalid access to Enum"; };

    public String constructMessage(List<String> args) {
        String msg = this.msg;
        for(String arg : args) {
            msg += delimiter + arg;
        }
        return msg;
    }
}
