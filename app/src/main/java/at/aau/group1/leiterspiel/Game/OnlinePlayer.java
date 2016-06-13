package at.aau.group1.leiterspiel.Game;

/**
 * Created by Igor on 22.04.2016.
 */
public class OnlinePlayer extends Player {

    private boolean active = false;

//    public OnlinePlayer(IPlayerObserver observer) {
//        super("Online", observer);
//        this.setName("Player " + super.getPlayerID() + " (Online)");
//    }

    public OnlinePlayer(String name, IPlayerObserver observer) {
        super(name, observer);
    }

    @Override
    public void poke() {
        super.poke();
        active = true; // lets this player instance react to touch input
    }

    @Override
    public boolean expectsTouchInput() {
        return false;
    }
}
