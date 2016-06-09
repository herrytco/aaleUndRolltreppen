package at.aau.group1.leiterspiel;

import android.graphics.Bitmap;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.Game.BotPlayer;
import at.aau.group1.leiterspiel.Game.GameManager;
import at.aau.group1.leiterspiel.Game.IGameUI;
import at.aau.group1.leiterspiel.Game.Ladder;
import at.aau.group1.leiterspiel.Game.LocalPlayer;
import at.aau.group1.leiterspiel.Game.Player;

public class GameActivity extends AppCompatActivity implements IGameUI {

    // controlling the framerate
    private final int FPS = 50; // generally 30-50fps is probably the best range for fluid animations
    private Timer graphicsTimer;
    private TimerTask drawTask;
    private boolean graphicsActive = false;
    // framerate measurement
    private final boolean LOG_FPS = false;
    private long frametime = 0; // time needed to draw a frame and update the UI in ms
    private int measuredFPS = 0;
    private int frameCount = 0;
    private long startTime = 0;

    // drawing the graphics
    private int canvasWidth = 512;
    private int canvasHeight = 512;
    private int diceResult = 0;
    private String status;
    private static boolean uiChanged = true;
    private static boolean uiEnabled = false;

    // UI elements
    private LinearLayout layout;
    private TextView statusView;
    private ImageButton diceButton;
    private Button cheatButton;
    private LinearLayout loadingScreen;
    private LinearLayout uiContainer;
    private boolean uiAlignedToBottom = false;

    //Fullscreen
    private Fullscreen fs = new Fullscreen();

    // static values to make them persistent over GameActivity lifecycles
    private static GameManager gameManager;
    private static SoundManager soundManager;
    private static GamePainter gamePainter;
    private static boolean gameInitialized = false;
    private static boolean uiInitialized = false;

    private long lastBackPress = 0;

    private Random random = new Random();

    private void initUI() {
        layout = (LinearLayout) findViewById(R.id.gameCanvas);
        diceButton = (ImageButton) findViewById(R.id.diceButton);
        statusView = (TextView) findViewById(R.id.statusView);
        cheatButton = (Button) findViewById(R.id.cheatCheckButton);
        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        uiContainer = (LinearLayout) findViewById(R.id.uiContainer);

        random.setSeed(System.currentTimeMillis());

        // touch listener for player input
        layout.setOnTouchListener(new View.OnTouchListener() {
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
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.patrick));
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.sponge));
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.krabs));
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.squid));
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.plankton));
        gamePainter.addPieceImg(BitmapFactory.decodeResource(getResources(), R.drawable.sandy));

        // create a game board
        gameManager.getGameBoard().setNumberOfFields(60);
        gameManager.addLadder(new Ladder(Ladder.LadderType.UP, 13, 26));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 5, 29));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 23, 37));
        gameManager.addLadder(new Ladder(Ladder.LadderType.UP, 39, 46));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 34, 57));
        // for a lesson of how not to do random-generated content, use this:
//        gameManager.setGameBoard(new BoardGenerator(60, 8).generateBoard());

        gameManager.setCheatsEnabled(cheatsEnabled);

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
                gameManager.addPlayer(player);
            }
        }

        gameInitialized = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (!gameInitialized) initGame();
            initUI();
        }

        if (gameInitialized && uiInitialized) {
            canvasWidth = layout.getWidth();
            canvasHeight = layout.getHeight();
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
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPress > 3000) { // create warning
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.press_back_again), Toast.LENGTH_LONG).show();
            lastBackPress = System.currentTimeMillis();
        } else { // kill game session and go back to lobby
            gameInitialized = false;
            uiInitialized = false;
            uiChanged = true;
            uiEnabled = false;
            status = null;
            if (loadingScreen!=null) loadingScreen.setVisibility(View.VISIBLE);
            gameManager = null;
            gamePainter = null;
            soundManager = null;
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

        gamePainter.clearCanvas(); // clear previous frame
        gamePainter.drawBoard(gameManager.getGameBoard()); // draw the board on the canvas
        gamePainter.drawPieces(gameManager.getGameBoard()); // draw the players on the canvas
        layout.setBackground(new BitmapDrawable(gamePainter.getFrame())); // display updated canvas
        updateUI(); // update parts of the user interface based on the game's state
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
            if (diceResult == 1) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_1));
            if (diceResult == 2) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_2));
            if (diceResult == 3) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_3));
            if (diceResult == 4) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_4));
            if (diceResult == 5) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_5));
            if (diceResult == 6) diceButton.setBackground(getResources().getDrawable(R.drawable.dice_6));
            diceButton.setEnabled(uiEnabled);
            if (uiEnabled) diceButton.setAlpha(1f);
            else diceButton.setAlpha(0.5f);

            statusView.setText(status);

            cheatButton.setEnabled(gameManager.areCheatsEnabled());
            cheatButton.setVisibility(gameManager.areCheatsEnabled()? View.VISIBLE : View.INVISIBLE);

            if (gameInitialized && uiInitialized && loadingScreen!=null) loadingScreen.setVisibility(View.INVISIBLE);
            else if(loadingScreen!=null) {
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
        soundManager.playDiceSound();

        int result = random.nextInt(6)+1;
        diceResult = result;
        uiChanged = true;

        gameManager.highlightField(result);
        gameManager.setPlayerRolled(true);
        disableUI(); // make sure the player can't roll more than once

        return result;
    }

    public void checkForCheat(View view) {
        String name = gameManager.checkForCheat();
        if (name == null)
            showStatus(getString(R.string.nobody)+" "+getString(R.string.somebody_cheated));
        else
            showStatus(name+" "+getString(R.string.somebody_cheated));
        uiChanged = true;
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
    public void endGame() {
        this.disableUI();
        this.showStatus(getResources().getString(R.string.game_finished));
    }

    @Override
    public void skipTurn() {
        showStatus(getString(R.string.skip_turn));
    }
}
