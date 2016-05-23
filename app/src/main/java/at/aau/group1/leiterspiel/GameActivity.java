package at.aau.group1.leiterspiel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private final int FPS = 30;
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
    private Bitmap bmp;
    private int diceResult = 0;
    private String status;
    private static boolean uiChanged = true;
    private static boolean uiEnabled = false;

    // UI elements
    private LinearLayout layout;
    private TextView statusView;
    private ImageButton diceButton;
    private Button cheatButton;

    // static values to make them persistent over GameActivity lifecycles
    private static GameManager gameManager;
    private static SoundManager soundManager;
    private static GamePainter gamePainter;
    private static boolean gameInitialized = false;

    private long lastBackPress = 0;

    private Random random = new Random();

    private void init() {
        layout = (LinearLayout) findViewById(R.id.gameCanvas);
        diceButton = (ImageButton) findViewById(R.id.diceButton);
        statusView = (TextView) findViewById(R.id.statusView);
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/spongefont.ttf");
        statusView.setTypeface(typeFace);
        cheatButton = (Button) findViewById(R.id.cheatCheckButton);

        canvasWidth = layout.getWidth();
        canvasHeight = layout.getHeight();
        Log.d("Init", "canvas size set to " + canvasWidth + "x" + canvasHeight);
        gamePainter.setDimensions(canvasWidth, canvasHeight);

        random.setSeed(System.currentTimeMillis());

        // touch listener for player input
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == event.ACTION_DOWN) {
                    int height = v.getHeight();
                    Point point = new Point((int) event.getX(), (int) (height - event.getY()));
                    gameManager.notify(point);
                }

                return false;
            }
        });

        lastBackPress = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (!gameInitialized) initGame();
    }

    private void initGame() {
        // get the intent call parameters
        boolean[] playerSelection = getIntent().getBooleanArrayExtra("PlayerSelection");
        String[] playerNames = getIntent().getStringArrayExtra("PlayerNames");
        String[] playerTypes = getIntent().getStringArrayExtra("PlayerTypes");
        boolean cheatsEnabled = getIntent().getBooleanExtra("CheatPermission", false);

        gameManager = new GameManager(this);
        soundManager = new SoundManager(getApplicationContext());
        gamePainter = new GamePainter(canvasWidth, canvasHeight);

        // set graphics
        gamePainter.setBackgroundImg(BitmapFactory.decodeResource(getResources(), R.drawable.bg_tile));
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

        init();

        gamePainter.buildBoard(gameManager.getGameBoard()); // generate game board

        runGraphics();

        gameManager.startGame();
    }

    @Override
    protected void onStop() {
        super.onStop();

        gameManager.pauseGame();

        drawTask.cancel(); // stop graphics output
        graphicsTimer.cancel();
        graphicsActive = false;
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPress > 3000) { // create warning
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.press_back_again), Toast.LENGTH_SHORT).show();
            lastBackPress = System.currentTimeMillis();
        } else { // kill game session and go back to lobby
            gameInitialized = false;
            super.onBackPressed();
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
                            drawGame();
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
        updateUI();

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
}
