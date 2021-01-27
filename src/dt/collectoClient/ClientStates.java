package dt.collectoClient;


/**
 * These are the states the {@link Client} can be in
 *
 * @author Emiel Rous and Wouter Koning
 */
public enum ClientStates {
    STARTINGUP,
    PENDINGHELLO,
    HELLOED,
    PENDINGLOGIN,
    LOGGEDIN,
    INQUEUE,
    WAITTHEIRMOVE,
    WAITOURMOVE,
    WAITVERIFYMOVE,
    GAMEOVER,


}