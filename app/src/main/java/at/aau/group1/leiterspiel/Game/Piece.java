package at.aau.group1.leiterspiel.Game;

/**
 * Created by Igor on 18.04.2016.
 */
public class Piece {

    private int field;
    private int playerID;

    public Piece(int playerID) {
        this.playerID = playerID;
        this.field = 0;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int getPlayerID() {
        return playerID;
    }
}
