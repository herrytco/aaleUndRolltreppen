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
    private int minPlayerID;
    private boolean playerRolled = false;

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

    /**
     * Initializes stuff
     */
    private void init() {
        activePlayer = 0;
    }

    /**
     * Pokes the currently selected player to signal him to make his turn.
     */
    public void startGame() {
        if(!active) {
            this.active = true;
            updateUI();
            players.get(activePlayer).poke();
        }
    }

    /**
     * Tells the UI to change according to the type of the current player.
     */
    private void updateUI() {
        if (players.get(activePlayer).expectsTouchInput()) ui.enableUI();
        else ui.disableUI();

        ui.showStatus(players.get(activePlayer).getName());
    }

    /**
     * Sets the GameManager to not notify the next player anymore if the current one finished his
     * turn.
     */
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

    /**
     * Calls the GameBoard to move the active player's piece.
     *
     * @param playerID ID of the active player
     * @param fields the number of fields by which the piece gets moved(aka value rolled by dice)
     * @return true if this move ended the game, otherwise false
     */
    public boolean executeMove(int playerID, int fields) {
        return gameBoard.movePiece(playerID, fields);
    }

    /**
     * If called by the right player, calls executeMove() and checks if the game ended.
     * If not, poke the next player.
     *
     * @param playerID ID of the calling player
     * @param steps value rolled by the dice
     */
    @Override
    public void move(int playerID, int steps) {
        // check if the player has already rolled the dice, otherwise the player can't move yet
        if (!playerRolled) {
            Log.d("Tag", "Player "+activePlayer+" didn't roll yet.");
            return;
        }
        // check move validity so the player can't go back or more than 6 fields forward
        if (steps <=0 || steps > 6) return;

        if (playerID == activePlayer) {
            if (executeMove(playerID, steps)) { // if game has ended
                ui.endGame();
                this.active = false;
            } else {
                // switching to the next player
                if (++activePlayer >= players.size()) activePlayer = 0;
                playerRolled = false;
                updateUI();
                if (active) players.get(activePlayer).poke();
            }
        } else Log.d("Tag", "GameManager: playerID and activePlayer don't match("+playerID+"!="+activePlayer+").");
    }

    /**
     * Calls the UI to roll the dice and returns the result to the calling player.
     *
     * @param playerID ID of the calling player
     * @return value rolled by the dice
     */
    @Override
    public int rollDice(int playerID) {
        int number = this.ui.rollDice(null); // get the rolled number
        return number;
    }

    /**
     * Highlights the rolled field.
     *
     * @param dice value rolled by the dice
     */
    public void highlightField(int dice) {
        // highlight the target field
        Piece piece = getGameBoard().getPieceOfPlayer( players.get(activePlayer).getPlayerID() );
        int field = dice;
        if(piece != null) {
            field += piece.getField();
            if (field < gameBoard.getNumberOfFields()) getGameBoard().getFields()[field].setHighlighted(true);
        }
    }

    /**
     * Processes touch input detected by the canvas' layout.
     *
     * @param point Position of the touch input event
     */
    @Override
    public void notify(Point point) {
        if (players.get(activePlayer).expectsTouchInput()) {
            int field = gameBoard.getFieldAtPosition(point);
            move(players.get(activePlayer).getPlayerID(),
                    field - gameBoard.getPieces().get(activePlayer).getField());
        }
    }

    public void setPlayerRolled(boolean b) { playerRolled = b; }

}
