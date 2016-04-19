package at.aau.group1.leiterspiel;

/**
 * Created by Igor on 18.04.2016.
 */
public class Player {

    private static int nextID = 0;

    private int playerID;
    private String name;

    public Player(String name) {
        this.playerID = nextID;
        nextID++;
        this.name = name;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }
}
