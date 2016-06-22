package at.aau.group1.leiterspiel;

import org.junit.Test;

import at.aau.group1.leiterspiel.game.BotPlayer;
import at.aau.group1.leiterspiel.game.IPlayerObserver;
import at.aau.group1.leiterspiel.game.LocalPlayer;
import at.aau.group1.leiterspiel.game.Player;

import static org.junit.Assert.assertTrue;

public class PlayerTest {

    IPlayerObserver observer = new IPlayerObserver() {
        @Override
        public void move(int playerID, int diceRoll, boolean localMove) {

        }

        @Override
        public int rollDice(int playerID) {
            return 0;
        }
    };

    @Test
    public void testIDs() {
        Player p0 = new BotPlayer(observer);
        Player p1 = new LocalPlayer(observer);
        assertTrue (p1.getPlayerID() == 1);

        Player.resetIDs();
        p1 = new BotPlayer(observer);
        assertTrue (p1.getPlayerID() == 0);
    }

    @Test
    public void testTypes() {
        Player p0 = new BotPlayer(observer);
        Player p1 = new LocalPlayer(observer);
        assertTrue (!p0.expectsTouchInput());
        assertTrue (p1.expectsTouchInput());
    }

    @Test
    public void testNames() {
        Player p0 = new BotPlayer(observer);
        p0.setName("Test");
        p0.setName(null);
        assertTrue (p0.getName()!=null);
    }

}
