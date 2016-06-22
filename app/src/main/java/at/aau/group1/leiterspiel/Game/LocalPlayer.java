package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 22.04.2016.
 */
public class LocalPlayer extends Player {

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
    }

    @Override
    public boolean expectsTouchInput() {
        return true;
    }
}
