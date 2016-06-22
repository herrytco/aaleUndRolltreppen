package at.aau.group1.leiterspiel.game;

import android.view.View;

/**
 * Created by Igor on 22.04.2016.
 *
 * This interface provides methods for the GameManager so it can influence the UI without directly
 * accessing it(which isn't possible anyway as the GameManager doesn't run in the UI thread).
 */
public interface IGameUI {

    public int rollDice(View view);

    public void disableUI();
    public void enableUI();

    public void showStatus(String status);
    public void showPlayer(int index);
    public void endGame(Player winner);
    public void skipTurn();
    public void notifyClientDisconnect();

    public void playLadder(Ladder.LadderType type);

    // online

    public void setDice(int result);
    public boolean checkForCheat();

}
