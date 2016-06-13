package at.aau.group1.leiterspiel.Game;

import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.GameActivity;

/**
 * Created by Igor on 21.04.2016.
 */
public class BotPlayer extends Player {

    Timer timer;
    TimerTask waitTask;

    public BotPlayer(IPlayerObserver observer) {
        super("Bot", observer);
        this.setName("Player " + super.getPlayerID() + " (Bot)");
    }

    @Override
    public void poke() {
        super.poke();

        if (!GameActivity.clientInstance) { // if this runs on a client, wait for the server's move
            final int diceResult = observer.rollDice(this.getPlayerID());

            // wait for a short period of time before making a move
            timer = new Timer();
            waitTask = new TimerTask() {
                @Override
                public void run() {
                    move(diceResult);
                }
            };
            timer.schedule(waitTask, 750);
        }
//        else {
//            observer.move(getPlayerID(), observer.getDiceResult(), false);
//        }
    }

    @Override
    public void setDiceResult(int dice) {
        super.setDiceResult(dice);

        observer.move(getPlayerID(), dice, false);
    }

    private void move(int diceResult) {
        this.observer.move(this.getPlayerID(), diceResult, true);

        waitTask.cancel();
        timer.cancel();
    }

}
