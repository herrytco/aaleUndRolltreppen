package at.aau.group1.leiterspiel.game;

import at.aau.group1.leiterspiel.LobbyActivity;

/**
 * Created by Igor on 22.04.2016.
 */
public class OnlinePlayer extends Player {

    public OnlinePlayer(String name, IPlayerObserver observer) {
        super(name, observer);
    }

    @Override
    public String getIdentifier() {
        return LobbyActivity.ONLINE;
    }

    @Override
    public boolean isOnline() {
        return true;
    }
}
