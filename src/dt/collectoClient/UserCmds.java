package dt.collectoClient;

import dt.protocol.ClientMessages;

import java.util.Objects;

public enum UserCmds {
    LIST(new String[]{"l", "list"}),
    MOVE(new String[]{"m", "move"}),
    QUEUE(new String[]{"q","queue", "newgame", "kwewe"}),
    EXIT(new String[]{"quit", "exit", "x"}),
    HELP(new String[]{"?", "h", "help"});


    private String[]  cmds;

    UserCmds(String[] strings) {
        this.cmds = strings;
    }
    public boolean isValid(String str) {
        for(String cmd : cmds) {
            if(cmd.equals(str)) return true;
        }
        return false;
    }

    public static UserCmds getUserCmd(String userCmd) {
        for(UserCmds cmd : UserCmds.values()) {
            if(cmd.isValid(userCmd)) return cmd;
        }
        return null;
    }
}
