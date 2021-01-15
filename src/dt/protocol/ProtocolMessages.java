package dt.protocol;

import dt.model.board.Board;
import dt.util.Move;

import java.util.List;

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
        CRYPT,
        AUTH,
    }
}
