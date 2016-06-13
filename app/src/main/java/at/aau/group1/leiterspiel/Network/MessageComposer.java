package at.aau.group1.leiterspiel.Network;

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

    public void registerServer(Server server) {
        this.server = server;
    }

    public void registerClient(Client client) {
        this.client = client;
    }

    private void sendMsg(String msg) {
        if (serverSide && server != null) server.writeOutput(msg);
        if (!serverSide && client != null) client.writeOutput(msg);
    }

    public void ack(int id) {
        String msg = name + sep + MessageParser.ACK + sep + id;
        sendMsg(msg);
    }

    public void joinLobby(int id) {
        String msg = name + sep + MessageParser.JOIN_LOBBY + sep + id;
        sendMsg(msg);
    }

    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
        String msg = name + sep + MessageParser.SET_PLAYER + sep + id + sep + playerIndex + sep + playerType + sep +playerName;
        sendMsg(msg);
    }

    public void allowCheats(int id, boolean permitCheats) {
        String permission = permitCheats ? MessageParser.YES : MessageParser.NO;
        String msg = name + sep + MessageParser.ALLOW_CHEATS + sep + id + sep + permission;
        sendMsg(msg);
    }

    public void startGame(int id) {
        String msg = name + sep + MessageParser.START_GAME + sep + id;
        sendMsg(msg);
    }

}
