package at.aau.group1.leiterspiel;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameManager implements IPlayerObserver, ITouchObserver {

    private boolean active = false;

    private GameBoard gameBoard;
    private ArrayList<Player> players;
    private int activePlayer;

    private IGameUI ui;

    public GameManager(IGameUI ui) {
        this.ui = ui;
        gameBoard = new GameBoard();
        players = new ArrayList<Player>();
        init();
    }

    public GameManager(IGameUI ui, GameBoard gameBoard, ArrayList<Player> players) {
        this.ui = ui;
        this.gameBoard = gameBoard;
        this.players = players;
        init();
    }

    private void init() {
        activePlayer = 0;
    }

    public void startGame() {
        if(!active) {
            this.active = true;
            updateUI();
            players.get(activePlayer).poke();
        }
    }

    private void updateUI() {
        if (players.get(activePlayer).expectsTouchInput()) ui.enableUI();
        else ui.disableUI();

        ui.showStatus(players.get(activePlayer).getName());
    }

    public void pauseGame() { this.active = false; }

    public void addPlayer(Player player) {
        players.add(player);
        gameBoard.addPiece(new Piece(player.getPlayerID()));
    }

    public GameBoard getGameBoard() { return gameBoard; }

    public void addLadder(Ladder ladder) { gameBoard.addLadder(ladder); }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public boolean executeMove(int playerID, int fields) {
        return gameBoard.movePiece(playerID, fields);
    }

    @Override
    public void move(int playerID, int diceRoll) {
//        if (playerID != activePlayer) throw new IllegalArgumentException("it's a different player's turn");
        if (playerID == activePlayer) {
            if (executeMove(playerID, diceRoll)) { // if game has ended
                ui.endGame();
            } else {
                // switching to the next player
                if (++activePlayer >= players.size()) activePlayer = 0;
                updateUI();
                if (active) players.get(activePlayer).poke();
            }
        }
    }

    @Override
    public int rollDice(int playerID) {
        int number = this.ui.rollDice(null); // get the rolled number

        return number;
    }

    public void highlightField(int dice) {
        // highlight the target field
        Piece piece = getGameBoard().getPieceOfPlayer( players.get(activePlayer).getPlayerID() );
        int field = dice;
        if(piece != null) {
            field += piece.getField();
            if (field < gameBoard.getNumberOfFields()) getGameBoard().getFields()[field].setHighlighted(true);
        }
    }

    @Override
    public void notify(Point point) {
        if (players.get(activePlayer).expectsTouchInput()) {
            int field = gameBoard.getFieldAtPosition(point);
            move(players.get(activePlayer).getPlayerID(),
                    field - gameBoard.getPieces().get(activePlayer).getField());
        }
    }
}
