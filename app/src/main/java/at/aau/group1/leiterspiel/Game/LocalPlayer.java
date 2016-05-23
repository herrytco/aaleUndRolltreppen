package at.aau.group1.leiterspiel.Game;

/**
 * Created by Igor on 22.04.2016.
 */
public class LocalPlayer extends Player {

    private boolean active = false;

    public LocalPlayer(IPlayerObserver observer) {
        super("Local", observer);
        this.setName("Player " + super.getPlayerID());
    }

    public LocalPlayer(String name, IPlayerObserver observer) {
        super(name, observer);
    }

    @Override
    public void poke() {
        super.poke();
        active = true; // lets this player instance react to touch input
    }

    @Override
    public boolean expectsTouchInput() {
        return true;
    }
}
