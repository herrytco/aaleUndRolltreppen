package at.aau.group1.leiterspiel;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.Game.BoardGenerator;
import at.aau.group1.leiterspiel.Game.BotPlayer;
import at.aau.group1.leiterspiel.Game.GameManager;
import at.aau.group1.leiterspiel.Game.IGameUI;
import at.aau.group1.leiterspiel.Game.Ladder;
import at.aau.group1.leiterspiel.Game.LocalPlayer;
import at.aau.group1.leiterspiel.Game.OnlinePlayer;
import at.aau.group1.leiterspiel.Game.Player;
import at.aau.group1.leiterspiel.Network.MessageComposer;

public class GameActivity extends AppCompatActivity implements IGameUI {

    // controlling the framerate
    // generally 30-50fps is probably the best range for fluid animations.
    // if the setting is too high, everything starts to lag, but below that threshold higher fps
    // mean smoother animations.
    private final int FPS = 50;
    private Timer graphicsTimer;
    private TimerTask drawTask;
    private boolean graphicsActive = false;
    // framerate measurement
    private final boolean LOG_FPS = false;
    private long frametime = 0; // time needed to draw a frame and update the UI in ms
    private int measuredFPS = 0;
    private int frameCount = 0;
    private long startTime = 0;

    // online game settings
    public static boolean online;
    public static boolean clientInstance;
    public static int playerIndex; // index of the player playing on this app instance(on server always 0)
    public static MessageComposer gameComposer;
    public static int msgID;

    // UI
    private int canvasWidth = 512;
    private int canvasHeight = 512;
    private static int diceResult = 0;
    private static String status;
    private static boolean clientDisconnected = false;
    private static boolean uiChanged = true;
    private static boolean uiEnabled = false;

    // UI elements
    private LinearLayout gameCanvas;
    private TextView statusView;
    private ImageButton diceButton;
    private Button cheatButton;
    private LinearLayout loadingScreen;
    private LinearLayout uiContainer;
    private boolean uiAlignedToBottom = false;
    private LinearLayout endScreen;
    private ImageView winnerPictureView;
    private TextView winnerNameView;
    private int[] diceFaces = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6 };
    private int[] playerIcons = {
            R.drawable.patrick,
            R.drawable.sponge,
            R.drawable.krabs,
            R.drawable.squid,
            R.drawable.plankton,
            R.drawable.sandy };

    //Fullscreen
    private Fullscreen fs = new Fullscreen();

    // static values to make them persistent over GameActivity lifecycles
    private static GameManager gameManager;
    private static SoundManager soundManager;
    private static GamePainter gamePainter;
    private static boolean gameInitialized = false;
    private static boolean uiInitialized = false;

    private static Player winner;
    private long lastBackPress = 0;

    private Random random = new Random();

    private void initUI() {
        gameCanvas = (LinearLayout) findViewById(R.id.gameCanvas);
        diceButton = (ImageButton) findViewById(R.id.diceButton);
        statusView = (TextView) findViewById(R.id.statusView);
        cheatButton = (Button) findViewById(R.id.cheatCheckButton);
        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        uiContainer = (LinearLayout) findViewById(R.id.uiContainer);
        endScreen = (LinearLayout) findViewById(R.id.endPopup);
        winnerPictureView = (ImageView) findViewById(R.id.winnerView);
        winnerNameView = (TextView) findViewById(R.id.winnerName);

        random.setSeed(System.currentTimeMillis());

        // touch listener for player input
        gameCanvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == event.ACTION_DOWN) {
                    int height = v.getHeight();
                    Point point = new Point((int) event.getX() - gamePainter.getXOffset(), (int) (height - event.getY() + gamePainter.getYOffset()));
                    gameManager.notify(point);
                }

                return false;
            }
        });

        // touch listener for uiContainer dragging
        uiContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) uiContainer.getLayoutParams();
                    if (uiContainer.getHeight() < uiContainer.getWidth()) { // portrait mode
                        if (!uiAlignedToBottom) params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        else params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    } else { // landscape mode
                        if (!uiAlignedToBottom) params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        else params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    }
                    uiContainer.setLayoutParams(params);
                    uiAlignedToBottom = !uiAlignedToBottom;
                }

                return false;
            }

        });

        uiInitialized = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        fs.setDecorView(getWindow().getDecorView());
        fs.hideSystemUI();
    }

    private void initGame() {
        // get the intent call parameters
        boolean[] playerSelection = getIntent().getBooleanArrayExtra("PlayerSelection");
        String[] playerNames = getIntent().getStringArrayExtra("PlayerNames");
        String[] playerTypes = getIntent().getStringArrayExtra("PlayerTypes");
        boolean cheatsEnabled = getIntent().getBooleanExtra("CheatPermission", false);
        int turnSkips = getIntent().getIntExtra("TurnSkips", 1);
        int boardType = getIntent().getIntExtra("BoardType", 0);
        clientInstance = getIntent().getBooleanExtra("ClientInstance", false);
        playerIndex = getIntent().getIntExtra("PlayerIndex", -1);
        online = getIntent().getBooleanExtra("Online", false);

        gameManager = new GameManager(this);
        gameManager.setFps(FPS);
        soundManager = new SoundManager(getApplicationContext());
        gamePainter = new GamePainter(canvasWidth, canvasHeight);

        // set graphics
        gamePainter.setEscalatorImg(BitmapFactory.decodeResource(getResources(), R.drawable.esc_step));
        gamePainter.setFieldImg(BitmapFactory.decodeResource(getResources(), R.drawable.field),
                BitmapFactory.decodeResource(getResources(), R.drawable.field_high));
        gamePainter.setLadderFieldImg(BitmapFactory.decodeResource(getResources(), R.drawable.field_up),
                BitmapFactory.decodeResource(getResources(), R.drawable.field_down));
        for (int i=0; i<playerIcons.length; i++) {
            gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), playerIcons[i]));
        }

        // create a game board
        gameManager.setGameBoard(new BoardGenerator().generateBoard(boardType));

        gameManager.setCheatsEnabled(cheatsEnabled);
        gameManager.setCheatTurns(turnSkips);

        // create players based on given parameters
        Player.resetIDs(); // start counting IDs with 0 again or else GameManager gets confused
        for (int n=0; n<LobbyActivity.MAX_PLAYERS; n++) {
            if (playerSelection[n]) {
                Player player = null;
                Log.d("Tag", "PlayerType: "+playerTypes[n]);
                if (playerTypes[n].equals(LobbyActivity.BOT))
                    player = new BotPlayer(gameManager);
                if (playerTypes[n].equals(LobbyActivity.LOCAL))
                    player = new LocalPlayer(playerNames[n], gameManager);
                if (playerTypes[n].equals(LobbyActivity.ONLINE))
                    player = new OnlinePlayer(playerNames[n], gameManager);
                gameManager.addPlayer(player);
            }
        }

        // get current composer for further use, and register the GameManager as message receiver
        if (clientInstance && JoinActivity.composer != null) {
            gameComposer = JoinActivity.composer;
            this.msgID = JoinActivity.msgID;
            JoinActivity.client.registerOnlineGameManager(gameManager);
        }
        else if (!clientInstance && LobbyActivity.composer != null) {
            gameComposer = LobbyActivity.composer;
            this.msgID = LobbyActivity.msgID;
            LobbyActivity.server.registerOnlineGameManager(gameManager);
        }
        else {
            gameComposer = new MessageComposer("Dummy", false); // assume offline game
            msgID = 0;
        }

        gameInitialized = true;
    }

    /**
     * Completely resets all static global values and instances, to make sure the next session is
     * started in a clean state and to free memory.
     */
    private void resetGame() {
        gameInitialized = false;
        uiInitialized = false;
        uiChanged = true;
        uiEnabled = false;
        status = null;
        if (loadingScreen!=null) loadingScreen.setVisibility(View.VISIBLE);
        gameManager = null;
        gamePainter = null;
        soundManager = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (!gameInitialized) initGame();
            initUI();
        }

        if (gameInitialized && uiInitialized) {
            canvasWidth = gameCanvas.getWidth();
            canvasHeight = gameCanvas.getHeight();
            Log.d("Init", "canvas size set to " + canvasWidth + "x" + canvasHeight);
            gamePainter.setDimensions(canvasWidth, canvasHeight);

            gamePainter.buildBoard(gameManager.getGameBoard()); // generate game board
            runGraphics();
            gameManager.startGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (gameManager!=null) gameManager.pauseGame();

        if (drawTask!=null) drawTask.cancel(); // stop graphics output
        if (graphicsTimer!=null) graphicsTimer.cancel();
        graphicsActive = false;

        // unregister service in the network
        if (!clientInstance) {
            if (LobbyActivity.server != null) LobbyActivity.server.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPress > 3000) { // create warning
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.press_back_again), Toast.LENGTH_LONG).show();
            lastBackPress = System.currentTimeMillis();
        } else { // kill game session and go back to lobby
            resetGame();
            super.onBackPressed(); // exit activity
        }
    }

    /**
     * If currently not active, the thread drawing the game graphics onto the canvas is being
     * started. The framerate depends on the variable FPS.
     */
    private void runGraphics() {
        if (!graphicsActive) {
            graphicsTimer = new Timer();
            drawTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (gameInitialized && uiInitialized) drawGame();
                        }
                    });
                }
            };
            graphicsTimer.scheduleAtFixedRate(drawTask, 0, 1000 / FPS); // start graphics output
            graphicsActive = true;
        }
    }

    /**
     * When called, clears the canvas and draws the current state of the game once again.
     * Afterwards the updated bitmap is being displayed.
     */
    private void drawGame() {
        long start = System.currentTimeMillis();
        if (startTime == 0) startTime = start;

        if (gamePainter.getFinished()) { // as soon as the frame is drawn, slap it onto the gameCanvas, or wait a bit longer
            gameCanvas.setBackground(new BitmapDrawable(gamePainter.getFrame()));
            gamePainter.drawFrame(gameManager.getGameBoard()); // call drawing of the next frame
        }
        // update parts of the user interface based on the game's state
        // this is only done a few times per second to help minimize the impact on the framerate,
        // as it can sometimes cause huge random delay spikes(25-90ms):
        if (frameCount % 10 == 0) updateUI();
        gameManager.checkProgress(); // make the GameManager check if a player's turn was finished

        long end = System.currentTimeMillis();
        frametime = end - start;
        frameCount++;
        if (end - startTime >= 1000) {
            int oldFPS = measuredFPS;
            measuredFPS = frameCount;
            frameCount = 0;
            startTime = 0;
            if (oldFPS != measuredFPS && LOG_FPS) Log.d("FPS", measuredFPS+"fps, last frametime: "+frametime+"ms");
        }
    }

    /**
     * Only the UI thread is able to access the different UI elements, so the changes to the UI
     * are being requested, and this method then applies the changes.
     * The flag uiChanged makes sure the UI gets only updated when necessary.
     */
    private void updateUI() {
        if (uiChanged) {
            for (int i=0; i<diceFaces.length; i++) {
                // getDrawable(int) is already deprecated, but the newer alternative requires a higher API level...
                if (diceResult == i+1) diceButton.setBackground(getResources().getDrawable(diceFaces[i]));
            }
            diceButton.setEnabled(uiEnabled);
            if (uiEnabled) diceButton.setAlpha(1f);
            else diceButton.setAlpha(0.5f);

            statusView.setText(status);

            if (clientDisconnected) {
                clientDisconnected = false;
                Toast.makeText(getApplicationContext(), getString(R.string.player_dc), Toast.LENGTH_SHORT).show();
            }

            cheatButton.setEnabled(gameManager.areCheatsEnabled());
            cheatButton.setVisibility(gameManager.areCheatsEnabled()? View.VISIBLE : View.INVISIBLE);

            if (winner != null && endScreen != null) {
                winnerNameView.setText(winner.getName());
                if (winner.getPlayerID() < playerIcons.length)
                    winnerPictureView.setBackground(getResources().getDrawable( playerIcons[winner.getPlayerID()] ));
                endScreen.setVisibility(View.VISIBLE);
            } else if (endScreen != null)
                endScreen.setVisibility(View.INVISIBLE);

            if (gameInitialized && uiInitialized && loadingScreen!=null) {
                loadingScreen.setVisibility(View.INVISIBLE);
            } else if(loadingScreen!=null) {
                loadingScreen.setVisibility(View.VISIBLE);
            }

            uiChanged = false;
        }
    }

    /**
     * Rolls a virtual dice, requests a UI update and tells the GameManager to highlight the rolled
     * field(which depends on the currently active player).
     *
     * @param view The view who called this method via onClick(), or null(isn't needed anyway)
     * @return The number rolled with the dice
     */
    public int rollDice(View view) {
        int result = random.nextInt(6)+1;
        setDice(result);

        gameComposer.setDice(msgID++, result);

        return result;
    }

    public void setDice(int result) {
        soundManager.playDiceSound();
        diceResult = result;
        uiChanged = true;

        gameManager.highlightField(result);
        gameManager.setPlayerRolled(true);
        disableUI(); // make sure the player can't roll more than once
    }

    @Override
    public void checkForCheat() {
        String name = gameManager.checkForCheat();
        if (name == null)
            showStatus(getString(R.string.nobody)+" "+getString(R.string.somebody_cheated));
        else
            showStatus(name+" "+getString(R.string.somebody_cheated));
        uiChanged = true;
    }

    public void checkForCheat(View view) {
        checkForCheat();

        gameComposer.checkForCheat(msgID++);
    }

    /**
     * Disables certain UI elements which are only used by a LocalPlayer for touch input.
     */
    @Override
    public void disableUI() {
        if (uiEnabled) uiChanged = true;
        uiEnabled = false;
    }

    /**
     * Enables certain UI elements which are only used by a LocalPlayer for touch input.
     */
    @Override
    public void enableUI() {
        if (!uiEnabled) uiChanged = true;
        uiEnabled = true;
    }

    /**
     * Updates the status TextView.
     *
     * @param status The String to be displayed
     */
    @Override
    public void showStatus(String status) {
        uiChanged = true;
        this.status = status;
    }

    /**
     * Calls showStatus() with the game_finished string.
     */
    @Override
    public void endGame(Player winner) {
        this.disableUI();
        this.showStatus(getResources().getString(R.string.game_finished));
        this.winner = winner;
    }

    @Override
    public void skipTurn() {
        showStatus(getString(R.string.skip_turn));
    }

    @Override
    public void notifyClientDisconnect() {
        uiChanged = true;
        clientDisconnected = true;
    }

    public void backToStartScreen(View view) {
        resetGame();
        finish(); // end the activity
    }

}
