package at.aau.group1.leiterspiel;

/**
 * Created by Igor on 18.04.2016.
 */
public abstract class Player {

    private static int nextID = 0;

    private int playerID;
    private String name;
    IPlayerObserver observer;

    public Player() {
        this.playerID = nextID;
        nextID++;
        this.name = "Player "+this.playerID;
    }

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

    public void poke() { // tells player his turn has come

    }

    public boolean expectsTouchInput() { return false; }

}
