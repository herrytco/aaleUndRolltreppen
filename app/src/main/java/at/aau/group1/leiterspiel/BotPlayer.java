package at.aau.group1.leiterspiel;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

    public BotPlayer(String name, IPlayerObserver observer) {
        super(name+" (Bot)", observer);
    }

    @Override
    public void poke() {
        super.poke();

        final int diceResult = observer.rollDice(this.getPlayerID());

        // wait for a short period of time before making a move
        timer = new Timer();
        waitTask = new TimerTask() {
            @Override
            public void run() {
                move(diceResult);
            }
        };
        timer.schedule(waitTask, 1500);
    }

    private void move(int diceResult) {
        this.observer.move(this.getPlayerID(), diceResult);

        waitTask.cancel();
        timer.cancel();
    }

}
