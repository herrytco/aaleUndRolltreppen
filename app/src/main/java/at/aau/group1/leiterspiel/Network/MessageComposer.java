package at.aau.group1.leiterspiel.network;

/**
 * Created by Igor on 13.06.2016.
 */
public class MessageComposer {

    private String sep = MessageParser.SEPARATOR;

    private Server server;
    private Client client;
    // name of the sending party(client or server)
    String name;
    // specifies if this MessageComposer instance should write to the server or client
    private boolean serverSide = false;

    public MessageComposer(String name, boolean isServerSide) {
        this.name = name;
        this.serverSide = isServerSide;
    }

    public void changeName(String name) { this.name = name; }

    public void registerServer(Server server) {
        this.server = server;
    }

    public void registerClient(Client client) {
        this.client = client;
    }

    private void sendMsg(String msg) {
//        Log.d("Composer", "Sending message: "+msg);
        if (serverSide && server != null) server.writeOutput(msg);
        if (!serverSide && client != null) client.writeOutput(msg);
    }

    public void ack(int id) {
        String msg = name + sep + MessageParser.ACK + sep + id;
        sendMsg(msg);
    }

    // ILobby

    public void joinLobby(int id) {
        String msg = name + sep + MessageParser.JOIN_LOBBY + sep + id;
        sendMsg(msg);
    }

    public void assignIndex(int id, int index, String clientName) {
        String msg = name + sep + MessageParser.ASSIGN_INDEX + sep + id + sep + index + sep + clientName;
        sendMsg(msg);
    }

    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
        String msg = name + sep + MessageParser.SET_PLAYER + sep + id + sep + playerIndex + sep + playerType + sep + playerName;
        sendMsg(msg);
    }

    public void allowCheats(int id, boolean permitCheats, int turnSkips) {
        String permission = permitCheats ? MessageParser.YES : MessageParser.NO;
        String msg = name + sep + MessageParser.ALLOW_CHEATS + sep + id + sep + permission + sep + turnSkips;
        sendMsg(msg);
    }

    public void setBoardType(int id, int type) {
        String msg = name + sep + MessageParser.SET_BOARD + sep + id + sep + type;
        sendMsg(msg);
    }

    public void startGame(int id) {
        String msg = name + sep + MessageParser.START_GAME + sep + id;
        sendMsg(msg);
    }

    // IOnlineGameManager

    public void ping(int id, int index) {
        String msg = name + sep + MessageParser.PING + sep + id + sep + index;
        sendMsg(msg);
    }

    public void poke(int id, int index) {
        String msg = name + sep + MessageParser.POKE + sep + id + sep + index;
        sendMsg(msg);
    }

    public void skip(int id) {
        String msg = name + sep + MessageParser.SKIP + sep + id;
        sendMsg(msg);
    }

    public void setDice(int id, int dice) {
        String msg = name + sep + MessageParser.SET_DICE + sep + id + sep + dice;
        sendMsg(msg);
    }

    public void checkForCheat(int id) {
        String msg = name + sep + MessageParser.CHECK_CHEAT + sep + id;
        sendMsg(msg);
    }

    public void movePiece(int id, int fields) {
        String msg = name + sep + MessageParser.MOVE_PIECE + sep + id + sep + fields;
        sendMsg(msg);
    }

}
