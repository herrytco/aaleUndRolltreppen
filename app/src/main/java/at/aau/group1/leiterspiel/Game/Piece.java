package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 18.04.2016.
 */
public class Piece {

    private int field;
    private int playerID;
    private double turnProgress; // for animations

    public Piece(int playerID) {
        this.playerID = playerID;
        this.field = 0;
        turnProgress = 0;
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

    public boolean increaseProgress(double difference) {
        this.turnProgress += difference;
        if (turnProgress >= 1.0) {
            turnProgress = 0.0;
            return true;
        }
        return false;
    }

    public double getTurnProgress() {
        return turnProgress;
    }
}
