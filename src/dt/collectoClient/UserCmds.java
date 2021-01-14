package dt.collectoClient;

import dt.protocol.ClientMessages;

import java.util.Objects;

public enum UserCmds {
    LIST(new String[]{"l", "list"}, "Get a list of players on the server"),
    MOVE(new String[]{"m", "move"}, "Make a move. Seperate by any of these: '~', '-', '=', '|'"),
    QUEUE(new String[]{"q","queue", "newgame", "kwewe"}, "Queue up. Game will start o nce an opponent has been found"),
    EXIT(new String[]{"quit", "exit", "x"}, "Exit. This shuts down the client"),
    HELP(new String[]{"?", "h", "help"}, "Print a help menu");


    private String[]  cmds;
    private String description;
    public static String separators = "[ ~+=|-]";

    UserCmds(String[] strings, String str) {
        this.cmds = strings;
        this.description = str;
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

    public static String getPrettyCommands() {
        StringBuilder strB = new StringBuilder();
        for (UserCmds cmd : UserCmds.values()) {
            StringBuilder subSTr = new StringBuilder();
            subSTr.setLength(100);
            subSTr.append('[').append(cmd.cmds[0]);
            for (int i = 1; i < cmd.cmds.length; i++ ) {
                subSTr.append(",").append(cmd.cmds[i]);
            }
            subSTr.append("]");
            subSTr.append(String.format("[cmd]*%-100s", cmd.separators));
            subSTr.append(String.format("%10s", cmd.description));
            strB.append(subSTr).append('\n');
        }
        return strB.toString();
    }
}
