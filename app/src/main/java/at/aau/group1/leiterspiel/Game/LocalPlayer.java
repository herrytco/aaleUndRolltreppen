package at.aau.group1.leiterspiel.game;

import at.aau.group1.leiterspiel.LobbyActivity;

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
    public String getIdentifier() {
        return LobbyActivity.LOCAL;
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
