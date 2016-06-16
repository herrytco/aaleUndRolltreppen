package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 23.05.2016.
 */
public class CheatAction {
    private int playerID;
    private int steps;

    public CheatAction(int playerID, int steps) {
        this.playerID = playerID;
        this.steps = steps;
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getSteps() {
        return steps;
    }
}
