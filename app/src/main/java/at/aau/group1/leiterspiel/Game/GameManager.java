package at.aau.group1.leiterspiel.game;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.GameActivity;
import at.aau.group1.leiterspiel.network.AckChecker;
import at.aau.group1.leiterspiel.network.IOnlineGameManager;

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
    private int maxTurnSkips = 1; // by default, a cheater has to skip 1 round
    private int turnSkips = 0; // the number of turns the cheater still has to skip
    private boolean isMoving = false;

    private IGameUI ui;

    private AckChecker ackChecker = new AckChecker();
    private static final int WAIT_TIMEOUT = 5000;
    private static final int DC_TIMEOUT = 3000; // 3 seconds until a client is considered disconnected and thrown out
    private Timer waitTimer;
    private TimerTask waitTask;
    private Timer dcTimer;
    private int pingID = -1;
    private boolean pingAcknowledged = false;
    private ArrayList<Integer> disconnectedPlayers = new ArrayList<>();

    public GameManager(IGameUI ui) {
        this.ui = ui;
        gameBoard = new GameBoard();
        players = new ArrayList<>();
        init();
    }

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

    public void setCheatTurns(int turns) { maxTurnSkips = turns; }

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
        if (players.get(activePlayer).expectsTouchInput())
            ui.enableUI();
        else
            ui.disableUI();

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
    private boolean move(int playerID, int steps) {
        // check if the player has already rolled the dice, otherwise the player can't move yet
        if (!playerRolled) {
            Log.d("Tag", "Player "+activePlayer+" didn't roll yet.");
            return false;
        }
        // check move validity so the player can't go back or more than 6 fields forward
        if (steps <=0 || steps > 6)
            return false;
        // check if the move is valid considering cheat setting
        if (!cheatsEnabled && steps != latestDiceResult)
            return false;
        // check if the player who wants to make the move is active
        if (playerID != activePlayer)
            return false;
        // check if this is a cheating move
        if (cheatsEnabled && steps != latestDiceResult) {
            // make sure cheats can't be used for the last winning move
            if (gameBoard.checkWinningMove(playerID, steps))
                return false;
            cheat = new CheatAction(playerID, steps);
        }

        // finally make the move
        if (executeMove(playerID, steps)) { // if game has ended
            ui.endGame(players.get(activePlayer));
            this.active = false;
        }
        isMoving = true;

        if (waitTimer != null)
            waitTimer.cancel();
        if (dcTimer != null)
            dcTimer.cancel();
        return true;
    }

    /**
     * Calls the move() method, and additionally notifies all connected app instances if the move
     * happened on this device.
     *
     * @param playerID ID of the calling player
     * @param steps value rolled by the dice
     * @param localMove if the move happened by the player controlling this device
     */
    @Override
    public void move(int playerID, int steps, boolean localMove) {
        if (!move(playerID, steps))
            return;

        if (localMove)
            GameActivity.gameComposer.movePiece(GameActivity.msgID++, steps);
    }

    /**
     * Checks whether the current player has finished the move animation, and if the animation is
     * complete, switch to the next player.
     */
    public void checkProgress() {
        if (isMoving && !gameBoard.isMoving()) {
            isMoving = false;
            switchToNextPlayer();
        }
    }

    /**
     * After a player completes their turn, switch to the next one and poke them.
     * If this is a client instance, the method does nothing, as the server controls which player's
     * turn it is(works more reliable to keep the client in sync).
     */
    private void switchToNextPlayer() {
        if (GameActivity.online && GameActivity.clientInstance)
            return; // let the server switch the players
        if (!active)
            return;

        // ignoring all disconnected players
        do {
            // switching to the next player
            if (++activePlayer >= players.size())
                activePlayer = 0;
        } while (disconnectedPlayers.contains(activePlayer));

        updateCheat();

        updateUI();
        ui.showPlayer(activePlayer);

        if (GameActivity.online) {
            GameActivity.gameComposer.poke(GameActivity.msgID++, activePlayer);
            ackChecker.waitForAcknowledgement(GameActivity.msgID - 1);
            // wait for the client to make a move
            if (activePlayer != GameActivity.playerIndex)
                schedulePing();
        }

        if (active)
            players.get(activePlayer).poke();
    }

    private void updateCheat() {
        if (activePlayer == cheaterID) {
            if (++activePlayer >= players.size())
                activePlayer = 0;
            turnSkips--;
            if (turnSkips == 0)
                cheaterID = -1;
        }
        playerRolled = false;
        // reset cheat when it's the cheating player's turn again
        if (cheat != null && cheat.getPlayerID() == activePlayer && cheaterID == -1)
            cheat = null;
    }

    private void schedulePing() {
        waitTimer = new Timer();
        waitTask = new TimerTask() {
            @Override
            public void run() {
                checkConnection();
            }
        };
        waitTimer.schedule(waitTask, WAIT_TIMEOUT);
    }

    /**
     * Checks whether a player cheated during the last turn, and punishes the player accordingly.
     *
     * @return name of the cheater, or null if nobody cheated
     */
    public String checkForCheat() {
        if (GameActivity.clientInstance && GameActivity.online) {
            GameActivity.gameComposer.checkForCheat(GameActivity.msgID++);
            if (!ackChecker.waitForAcknowledgement(GameActivity.msgID-1))
                return null;
        }

        if (cheat != null) { // if someone cheated
            // mark cheater for the next rounds
            cheaterID = cheat.getPlayerID();
            turnSkips = maxTurnSkips;
            // revert move
            gameBoard.revertMove(cheaterID, cheat.getSteps());
            // return cheater's name
            for (Player player: players) {
                if (player.getPlayerID() == cheaterID)
                    return player.getName();
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
            if (field < gameBoard.getNumberOfFields())
                getGameBoard().getFields()[field].setHighlighted(true);
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
                // if this is the server, switch to the next player, otherwise tell the server to
                // skip to the next player
                if (!GameActivity.online || !GameActivity.clientInstance) switchToNextPlayer();
                else GameActivity.gameComposer.skip(GameActivity.msgID++);
                return;
            }

            int field = gameBoard.getFieldAtPosition(point);
            move(players.get(activePlayer).getPlayerID(),
                    field - gameBoard.getPieces().get(activePlayer).getField(), true);
        }
    }

    public void setPlayerRolled(boolean b) { playerRolled = b; }

    /**
     * Check if the connection is still alive by sending a ping and waiting for a response for
     * DC_TIMEOUT milliseconds.
     */
    private void checkConnection() {
        // ping the client/server
        GameActivity.gameComposer.ping(GameActivity.msgID++, activePlayer);
        pingID = GameActivity.msgID - 1;
        dcTimer = new Timer();
        dcTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkPingStatus();
            }
        }, DC_TIMEOUT);
    }

    /**
     * React to the result of a ping, depending on whether a response arrived.
     */
    private void checkPingStatus() {
        if (pingAcknowledged) {
            pingAcknowledged = false;
            // start waiting again in case the connection is lost later
            schedulePing();
        } else {
            // if client has not responded yet, ignore it and switch to the next player
            disconnectedPlayers.add(activePlayer);
            // check if there are any connected online players left
            boolean stillOnline = false;
            for (int i=0; i<players.size(); i++) {
                if (players.get(i).isOnline() && !disconnectedPlayers.contains(i)) {
                    stillOnline = true;
                    break;
                }
            }
            GameActivity.online = stillOnline;
            ui.notifyClientDisconnect();
            switchToNextPlayer();
        }
        pingID = -1;
    }

    @Override
    public void ack(int id) {
        ackChecker.setLastAckID(id);
        if (pingID == id)
            pingAcknowledged = true;
    }

    @Override
    public void ping(int id, int index) {
        if (index == GameActivity.playerIndex)
            GameActivity.gameComposer.ack(id);
    }

    @Override
    public void poke(int id, int index) {
        if (GameActivity.clientInstance) {
            if (activePlayer != index)
                playerRolled = false;

            activePlayer = index;
            updateUI();
            ui.showPlayer(activePlayer);
            players.get(activePlayer).poke();
            GameActivity.gameComposer.ack(id);
        }
    }

    @Override
    public void skip(int id, String player) {
        if (!GameActivity.clientInstance) {
            if (players.get(activePlayer).getName().equals(player)) {
                switchToNextPlayer();
            }
            GameActivity.gameComposer.ack(id);
        }
    }

    @Override
    public void setDice(int id, int dice) {
        this.ui.setDice(dice);
        if (GameActivity.clientInstance)
            players.get(activePlayer).setDiceResult(dice);
        GameActivity.gameComposer.ack(id);
    }

    @Override
    public void checkForCheat(int id) {
        if (!GameActivity.clientInstance && this.ui.checkForCheat())
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
}
