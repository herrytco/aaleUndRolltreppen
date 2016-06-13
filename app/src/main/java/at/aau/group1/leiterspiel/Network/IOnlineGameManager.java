package at.aau.group1.leiterspiel.Network;

/**
 * Created by Igor on 13.06.2016.
 */
public interface IOnlineGameManager {

    /**
     * Used for sending back an acknowledgement, ensuring the message with the given ID was
     * received and successfully processed.
     *
     * @param id ID of the previously received and processed message
     */
    public void ack(int id);

    public void poke(int id, int index); // not sure if needed

    public void setDice(int id, int dice);

    public void checkForCheat(int id);

    public void movePiece(int id, int fields, String player);

}
