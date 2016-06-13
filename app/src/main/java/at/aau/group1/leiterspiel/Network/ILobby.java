package at.aau.group1.leiterspiel.Network;

/**
 * Created by Igor on 13.06.2016.
 */
public interface ILobby {

    /**
     * Used for sending back an acknowledgement, ensuring the message with the given ID was
     * received and successfully processed.
     *
     * @param id ID of the previously received and processed message
     */
    public void ack(int id);

    /**
     * Used by the client to join the lobby.
     * @param id ID of the message(any integer, if possible unique for the message
     * @param name Name of the joining player
     */
    public void joinLobby(int id, String name);

    // messages transmitting lobby/game information to the client, so the client can start an
    // identical game session.

    public void setPlayer(int id, int playerIndex, String playerType, String playerName);

    public void allowCheats(int id, boolean permitCheats);

    public void startGame(int id);

}
