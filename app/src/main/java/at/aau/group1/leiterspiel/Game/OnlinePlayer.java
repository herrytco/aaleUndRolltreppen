package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 22.04.2016.
 */
public class OnlinePlayer extends Player {

    public OnlinePlayer(String name, IPlayerObserver observer) {
        super(name, observer);
    }

    @Override
    public boolean isOnline() {
        return true;
    }
}
