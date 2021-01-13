package dt.collectoClient;

import dt.protocol.ClientMessages;

import java.util.Objects;

public enum UserCmds {
    LIST("l", "list"),
    MOVE("m", "move"),
    QUEUE("q","queue", "newgame");

    private String cmd1;
    private String cmd2;
    private String cmd3;

    UserCmds(String cmd1, String cmd2) {
        this.cmd1 = cmd1;
        this.cmd2 = cmd2;
    }
    UserCmds(String cmd1, String cmd2, String cmd3) {
        this.cmd1 = cmd1;
        this.cmd2 = cmd2;
        this.cmd3 = cmd3;
    }
    public boolean isValid(String str) {
        return this.cmd1.equals(str) || this.cmd2.equals(str) || Objects.equals(this.cmd3, str);
    }

    public static UserCmds getUserCmd(String userCmd) {
        for(UserCmds cmd : UserCmds.values()) {
            if(cmd.isValid(userCmd)) return cmd;
        }
        return null;
    }
}
