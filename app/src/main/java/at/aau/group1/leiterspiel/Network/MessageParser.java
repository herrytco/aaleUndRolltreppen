package at.aau.group1.leiterspiel.Network;

import android.util.Log;

/**
 * Created by Igor on 13.06.2016.
 */
public class MessageParser {

    private final String TAG = "MessageParser";

    // command syntax: <name of sender>:<command>:<ID>[:<additional parameters>]
    // valid commands in messages
    public static final String SEPARATOR = ":";
    // ILobby-related commands
    public static final String ACK = "ack";
    public static final String JOIN_LOBBY = "join";
    public static final String SET_PLAYER = "setplayer";
    public static final String ALLOW_CHEATS = "cheats";
    public static final String START_GAME = "start";
    public static final String YES = "y";
    public static final String NO = "n";

    private ILobby lobby; // Registered listener that will receive commands based on the parsed messages

    public MessageParser() {
    }

    public void registerLobby(ILobby lobby) {
        this.lobby = lobby;
    }

    public void parseMessage(String msg) {
        // remove all line breaks to prevent parsing errors
        msg = msg.replace("\n", "");
        // split the message into its parameters
        String[] args = msg.split(SEPARATOR);
        String name = args[0];
        String command = args[1];
        int id = Integer.parseInt(args[2]);

        if (lobby != null) { // lobby-related commands can be skipped if no related listener exists
            try {
                if (command.equals(ACK)) {
                    lobby.ack(id);
                }
                if (command.equals(JOIN_LOBBY)) {
                    lobby.joinLobby(id, name);
                }
                if (command.equals(SET_PLAYER)) {
                    int playerIndex = Integer.parseInt(args[3]);
                    String playerType = args[4];
                    String playerName = args[5];
                    lobby.setPlayer(id, playerIndex, playerType, playerName);
                }
                if (command.equals(ALLOW_CHEATS)) {
                    boolean permitCheats = false;
                    if (args[3].equals(YES)) permitCheats = true;
                    lobby.allowCheats(id, permitCheats);
                }
                if (command.equals(START_GAME)) {
                    lobby.startGame(id);
                }
            } catch(NumberFormatException nfe) {
                Log.e(TAG, "SYNTAX ERROR - number format exception: "+msg);
            } catch (Exception e) {
                Log.e(TAG, "SYNTAX ERROR - couldn't parse message: "+msg);
                e.printStackTrace();
            }
        }

    }
}
