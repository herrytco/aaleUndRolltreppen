package at.aau.group1.leiterspiel.Game;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import at.aau.group1.leiterspiel.GameActivity;
import at.aau.group1.leiterspiel.JoinActivity;
import at.aau.group1.leiterspiel.LobbyActivity;
import at.aau.group1.leiterspiel.Network.IOnlineGameManager;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameManager implements IPlayerObserver, ITouchObserver, IOnlineGameManager {

    private boolean active = false;

    private GameBoard gameBoard;
    private ArrayList<Player> players;
    private int activePlayer;
    private boolean playerRolled = false;
    private int latestDiceResult = 0;
    private boolean cheatsEnabled = false;
    private CheatAction cheat;
    private int cheaterID = -1;
    private boolean isMoving = false;

    private IGameUI ui;

    private final int TIMEOUT = 2000;
    private int lastAckID = -1;

    public GameManager(IGameUI ui) {
        this.ui = ui;
        gameBoard = new GameBoard();
        players = new ArrayList<>();
        init();
    }

//    public GameManager(IGameUI ui, boolean clientInstance, GameBoard gameBoard, ArrayList<Player> players) {
//        this.ui = ui;
//        this.clientInstance = clientInstance;
//        this.gameBoard = gameBoard;
//        this.players = players;
//        init();
//    }

    public void setFps(int fps) {
        gameBoard.setFps(fps);
    }

    /**
     * Initializes stuff
     */
    private void init() {
        activePlayer = 0;
        isMoving = false;
    }

    public void setCheatsEnabled(boolean b) { cheatsEnabled = b; }

    public boolean areCheatsEnabled() { return cheatsEnabled; }

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
        Log.d("Debug", "updateUI for player "+activePlayer+" - touchInput: "+players.get(activePlayer).expectsTouchInput());
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
    private void move(int playerID, int steps) {
        // check if the player has already rolled the dice, otherwise the player can't move yet
        if (!playerRolled) {
            Log.d("Tag", "Player "+activePlayer+" didn't roll yet.");
            return;
        }
        // check move validity so the player can't go back or more than 6 fields forward
        if (steps <=0 || steps > 6) return;
        // check if the move is valid considering cheat setting
        if (!cheatsEnabled && steps != latestDiceResult) return;

        if (playerID == activePlayer) {
            if (cheatsEnabled && steps != latestDiceResult) {
                cheat = new CheatAction(playerID, steps);
                // make sure cheats can't be used for the last winning move
                if (gameBoard.checkWinningMove(playerID, steps)) return;
            }

            if (executeMove(playerID, steps)) { // if game has ended
                ui.endGame(players.get(activePlayer));
                this.active = false;
            }
            isMoving = true;
        } else Log.d("Tag", "GameManager: playerID and activePlayer don't match("+playerID+"!="+activePlayer+").");
    }

    @Override
    public void move(int playerID, int steps, boolean localMove) {
        move(playerID, steps);

        if (localMove) GameActivity.gameComposer.movePiece(GameActivity.msgID++, steps);
    }

    public void checkProgress() {
        if (isMoving && !gameBoard.isMoving()) {
            isMoving = false;
            switchToNextPlayer();
        }
    }

    private void switchToNextPlayer() {
        if (GameActivity.clientInstance) return; // let the server switch the players

        if (!active) return;
        // switching to the next player
        if (++activePlayer >= players.size()) activePlayer = 0;
        if (activePlayer == cheaterID) { // make cheater skip one turn
            if (++activePlayer >= players.size()) activePlayer = 0;
            cheaterID = -1;
        }
        playerRolled = false;
        // reset cheat when it's the cheating player's turn again
        if (cheat != null && cheat.getPlayerID() == activePlayer) cheat = null;

        updateUI();

        GameActivity.gameComposer.poke(GameActivity.msgID++, activePlayer);
        waitForAcknowledgement(GameActivity.msgID-1);

        if (active) players.get(activePlayer).poke();
    }

    /**
     * Checks whether a player cheated during the last turn, and punishes the player accordingly.
     *
     * @return name of the cheater, or null if nobody cheated
     */
    public String checkForCheat() {
        if (cheat != null) { // if someone cheated
            // mark cheater for the next round
            cheaterID = cheat.getPlayerID();
            // revert move
            gameBoard.revertMove(cheaterID, cheat.getSteps());
            // return cheater's name
            for (Player player: players) {
                if (player.getPlayerID() == cheaterID) return player.getName();
            }
        }
        return null;
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
        latestDiceResult = number;
        return number;
    }

//    @Override
//    public int getDiceResult() {
//        return latestDiceResult;
//    }

    /**
     * Highlights the rolled field.
     *
     * @param dice value rolled by the dice
     */
    public void highlightField(int dice) {
        latestDiceResult = dice;
        // highlight the target field
        Piece piece = getGameBoard().getPieceOfPlayer( players.get(activePlayer).getPlayerID() );
        int field = dice;
        if(piece != null) {
            field += piece.getField();
            if (field < gameBoard.getNumberOfFields()) getGameBoard().getFields()[field].setHighlighted(true);
            else ui.skipTurn();
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
            // check if the turn should be skipped
            if(gameBoard.checkOvershootingMove(players.get(activePlayer).getPlayerID(), latestDiceResult)) {
                switchToNextPlayer();
                return;
            }

            int field = gameBoard.getFieldAtPosition(point);
            move(players.get(activePlayer).getPlayerID(),
                    field - gameBoard.getPieces().get(activePlayer).getField(), true);
        }
    }

    public void setPlayerRolled(boolean b) { playerRolled = b; }

    @Override
    public void ack(int id) {
        lastAckID = id;
    }

    @Override
    public void poke(int id, int index) {
        if (activePlayer != index) playerRolled = false;
        activePlayer = index;
        updateUI();
        players.get(activePlayer).poke();
        GameActivity.gameComposer.ack(id);
    }

    @Override
    public void setDice(int id, int dice) {
        this.ui.setDice(dice);
        if (GameActivity.clientInstance) players.get(activePlayer).setDiceResult(dice);
        GameActivity.gameComposer.ack(id);
    }

    @Override
    public void checkForCheat(int id) {
        this.ui.checkForCheat();
        GameActivity.gameComposer.ack(id);
    }

    @Override
    public void movePiece(int id, int fields, String name) {
        for (Player p: players) {
            if (p.getName().equals(name)) {
                this.move(p.getPlayerID(), fields);
                break;
            }
        }
        GameActivity.gameComposer.ack(id);
    }

    /**
     * Waits for TIMEOUT ms for an acknowledgement with the specified ID.
     * If no correct ack message is received in time, return false.
     *
     * @param id ID of the expected ack message
     * @return true if the acknowledgement was successfully received
     */
    private boolean waitForAcknowledgement(int id) {
        // wait for ack
        long start = System.currentTimeMillis();
        while (lastAckID != id) {
            // if no acknowledgement comes in time
            if (System.currentTimeMillis() - start >= TIMEOUT) {
                return false;
            }
        }
        return true;
    }

}
