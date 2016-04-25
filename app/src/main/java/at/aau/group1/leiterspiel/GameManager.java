package at.aau.group1.leiterspiel;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameManager {

    private GameBoard gameBoard;
    private ArrayList<Player> players;
    private int activePlayer;

    // simple simulation stuff
    private Timer simTimer = new Timer();
    private TimerTask simTask = new TimerTask() {
        @Override
        public void run() {
            simulateTurn();
        }
    };

    public GameManager() {
        gameBoard = new GameBoard();
        players = new ArrayList<Player>();
        init();
    }

    public GameManager(GameBoard gameBoard, ArrayList<Player> players) {
        this.gameBoard = gameBoard;
        this.players = players;
        init();
    }

    private void init() {
        activePlayer = 0;
    }

    public void startSim() {
        simTimer.scheduleAtFixedRate(simTask, 0, 1000); // run the simulation with 1 turn per second
    }

    public void addPlayer(Player player) {
        players.add(player);
        gameBoard.addPiece(new Piece(player.getPlayerID()));
    }

//    public void setNumberOfFields(int fields) { gameBoard.setNumberOfFields(fields); }

    public GameBoard getGameBoard() { return gameBoard; }

    public void addLadder(Ladder ladder) { gameBoard.addLadder(ladder); }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public boolean executeMove(int playerID, int fields) {
        if (playerID != activePlayer) throw new IllegalArgumentException("it's a different player's turn");

        boolean result = gameBoard.movePiece(playerID, fields);
        // switching to the next player
        if (++activePlayer >= players.size()) activePlayer = 0;

        return result;
    }

    private void simulateTurn() {
        Random random = new Random();
        int diceRoll = random.nextInt(6)+1;
        Log.d("Tag", "Player "+activePlayer+" rolled "+diceRoll);

        if ( executeMove(activePlayer, diceRoll) ) {
            simTimer.cancel();
        }
    }

}
