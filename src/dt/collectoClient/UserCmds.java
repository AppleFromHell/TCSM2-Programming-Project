package dt.collectoClient;


/**
 * A list of all possible commands
 *
 * @author Emiel Rous and Wouter Koning
 */
public enum UserCmds {
    LIST(new String[] {"l", "list"}, "Get a list of players on the server"),
    MOVE(new String[] {"m", "move"}, "Make a move. Seperate by any of these: '~', '-', '=', '|'"),
    HINT(new String[] {"hint", "tip", "imdumb"}, "Gives a random valid move as a hint"),
    QUEUE(new String[] {"q", "queue", "newgame", "kwewe"},
        "Queue up. Game will start o nce an opponent has been found"),
    CHAT(new String[] {"c", "chat", "say"}, "Sends a chat message to all Clients"),
    WHISPER(new String[] {"w", "whisper", "silentSay", "uWuInThEar"},
        "Sends a private chat message to a Clients"),
    EXIT(new String[] {"quit", "exit", "x"}, "Exit. This shuts down the client"),
    PLAYER(new String[] {"player", "playa", "ai"}, "Set the player type"),
    HELP(new String[] {"?", "h", "help"}, "Print a help menu"),
    RANK(new String[] {"rank", "pikorde"}, "Request the server for a ranking");


    public static String separators = "[ ~+=|-]"; //Possible seperators. All and any can be used
    private final String[] cmds; //Acceptable user inputs
    private final String description; //Descrpition of the command

    UserCmds(String[] strings, String str) {
        this.cmds = strings;
        this.description = str;
    }

    /**
     * Return a {@link UserCmds} based on a user input
     *
     * @param userCmd
     * @return
     * @ensures either null or a {@link UserCmds} is retured
     */
    public static UserCmds getUserCmd(String userCmd) {
        for (UserCmds cmd : UserCmds.values()) {
            if (cmd.isValid(userCmd)) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Create a list of commands
     *
     * @return the commands with their description in a 'pretty' format
     */
    public static String getPrettyCommands() {
        StringBuilder strB = new StringBuilder();
        for (UserCmds cmd : UserCmds.values()) {
            StringBuilder subSTr = new StringBuilder();
            subSTr.setLength(100);
            subSTr.append('[').append(cmd.cmds[0]);
            for (int i = 1; i < cmd.cmds.length; i++) {
                subSTr.append(",").append(cmd.cmds[i]);
            }
            subSTr.append("]");
            subSTr.append(String.format("[cmd]*%-100s", separators));
            subSTr.append(String.format("%10s", cmd.description));
            strB.append(subSTr).append('\n');
        }
        return strB.toString();
    }

    /**
     * Check for each {@link UserCmds} if the str is part of their acceptable cmds
     *
     * @param str
     * @return
     */
    public boolean isValid(String str) {
        for (String cmd : cmds) {
            if (cmd.equals(str.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
