package at.aau.group1.leiterspiel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // creating a tiny test game
        GameManager gameManager = new GameManager();
        gameManager.setNumberOfFields(100);
        gameManager.addLadder(new Ladder(Ladder.LadderType.BIDIRECTIONAL, 5, 20));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 50, 65));
        gameManager.addLadder(new Ladder(Ladder.LadderType.UP, 42, 52));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 70, 81));

        Player player0 = new Player("Player 0");
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        gameManager.addPlayer(player0);
        gameManager.addPlayer(player1);
        gameManager.addPlayer(player2);

        gameManager.startSim();
    }
}
