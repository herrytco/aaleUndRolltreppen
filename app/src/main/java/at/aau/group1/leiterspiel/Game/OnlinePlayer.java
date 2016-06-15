package at.aau.group1.leiterspiel.Game;

/**
 * Created by Igor on 22.04.2016.
 */
public class OnlinePlayer extends Player {

    private boolean active = false;

    public OnlinePlayer(String name, IPlayerObserver observer) {
        super(name, observer);
    }

    @Override
    public boolean isOnline() {
        return true;
    }
}
