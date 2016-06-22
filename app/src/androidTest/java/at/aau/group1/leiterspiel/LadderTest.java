package at.aau.group1.leiterspiel;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import at.aau.group1.leiterspiel.game.Ladder;

public class LadderTest {

    @Test
    public void testActivation() {
        Ladder ladder0 = new Ladder(Ladder.LadderType.BIDIRECTIONAL, 0, 10);
        Ladder ladder1 = new Ladder(Ladder.LadderType.UP, 0, 10);
        Ladder ladder2 = new Ladder(Ladder.LadderType.DOWN, 0, 10);

        assertTrue (ladder0.checkActivation(0) == 10);
        assertTrue (ladder0.checkActivation(-1) == -1);
        assertTrue (ladder0.checkActivation(10) == 0);

        assertTrue (ladder1.checkActivation(0) == 10);
        assertTrue (ladder1.checkActivation(-1) == -1);
        assertTrue (ladder1.checkActivation(10) != 0);

        assertTrue (ladder2.checkActivation(0) != 10);
        assertTrue (ladder2.checkActivation(-1) == -1);
        assertTrue (ladder2.checkActivation(10) == 0);
    }

}
