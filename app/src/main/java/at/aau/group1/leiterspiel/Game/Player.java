package at.aau.group1.leiterspiel.Game;

/**
 * Created by Igor on 18.04.2016.
 */
public abstract class Player {

    private static int nextID = 0;

    private int playerID;
    private String name;
    public IPlayerObserver observer;

    public Player(String name, IPlayerObserver observer) {
        this.playerID = nextID;
        nextID++;
        this.name = name;
        this.observer = observer;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Tells player it's his turn.
     */
    public void poke() {

    }

    public void setDiceResult(int dice) {

    }

    public boolean isOnline() { return false; }

    /**
     * @return true if this Player reacts to touch input, otherwise false
     */
    public boolean expectsTouchInput() { return false; }

    public static void resetIDs() { nextID = 0; }
}
