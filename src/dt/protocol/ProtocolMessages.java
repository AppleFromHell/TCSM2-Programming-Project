package dt.protocol;

import dt.util.Move;

import java.util.List;

/** @author Emiel Rous and Wouter Koning */
public interface ProtocolMessages {
    String delimiter = "~";

    String constructMessage();
    String constructMessage(String arg1);
    String constructMessage(String arg1, String arg2);
    String constructMessage(List<String> args);
    String constructMessage(Move move);

    enum Messages{
        LOGIN,
        HELLO,
        QUEUE,
        LIST,
        ALREADYLOGGEDIN,
        MOVE,
        NEWGAME,
        ERROR,
        GAMEOVER,
        RANK,
        CHAT,
        WHISPER,
        CANNOTWHISPER,
        CRYPT,
        AUTH,
    }
}
