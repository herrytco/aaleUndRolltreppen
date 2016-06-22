package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.network.AckChecker;
import at.aau.group1.leiterspiel.network.ICommListener;
import at.aau.group1.leiterspiel.network.ILobby;
import at.aau.group1.leiterspiel.network.MessageComposer;
import at.aau.group1.leiterspiel.network.NsdService;
import at.aau.group1.leiterspiel.network.Server;

/**
 * Created by Igor on 22.05.2016.
 */
public class LobbyActivity extends AppCompatActivity implements ICommListener, ILobby {

    private static final String TAG = "Lobby";

    public static final int MAX_PLAYERS = 6;
    public static final String BOT = "Bot";
    public static final String LOCAL = "Local";
    public static final String ONLINE = "Online";
    private static final int BOARD_TYPES = 3;

    private static final int MAX_TURN_SKIPS = 3;
    private static int turnSkips = 1;

    private static NsdService service;
    private static boolean serviceActive = false;
    private static Timer uiTimer;
    private static String newPlayer;
    private static int openOnlineSlots = 0;
    private static boolean uiChanged = false;
    // true if an onlineMode client is connected, otherwise start game in offline mode
    private static boolean onlineMode = false;

    public static Server server;
    public static MessageComposer composer;

    private static String[] playerNames = new String[MAX_PLAYERS];
    private static boolean[] playerSelection = new boolean[MAX_PLAYERS];
    private static String[] playerTypes = new String[MAX_PLAYERS];

    private ImageButton[] playerImages = new ImageButton[MAX_PLAYERS];
    private Button[] typeButtons = new Button[MAX_PLAYERS];
    private EditText[] playerNameFields = new EditText[MAX_PLAYERS];
    private Button cheatToggleButton;
    private Button boardToggleButton;
    private int[] playerIcons = {
            R.drawable.patrick,
            R.drawable.sponge,
            R.drawable.krabs,
            R.drawable.squid,
            R.drawable.plankton,
            R.drawable.sandy };

    private boolean cheatsEnabled = false;
    private int boardType = 0;

    private int msgID = 0;
    private AckChecker ackChecker = new AckChecker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_lobby);

        // create the list of players
        createPlayerList();

        cheatToggleButton = (Button) findViewById(R.id.cheatToggleButton);
        boardToggleButton = (Button) findViewById(R.id.boardToggleButton);
    }

    private void createPlayerList() {
        LinearLayout list = (LinearLayout) findViewById(R.id.playerList);
        for (int i=0; i<MAX_PLAYERS; i++) {
            // inflate the layout and add it as child
            getLayoutInflater().inflate(getResources().getLayout(R.layout.player_item), list);
            // get the children of the loaded layout
            playerImages[i] = (ImageButton) list.getChildAt(i).findViewById(R.id.playerImage);
            typeButtons[i] = (Button) list.getChildAt(i).findViewById(R.id.playerType);
            playerNameFields[i] = (EditText) list.getChildAt(i).findViewById(R.id.playerName);
            // the ID doesn't have to be unique for every single View,
            // so for simple identification use the index
            playerImages[i].setId(i);
            typeButtons[i].setId(i);
            playerNameFields[i].setId(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onBackPressed() {
        stopService();
        super.onBackPressed();
    }

    private void init() {
        Arrays.fill(playerNames, "");
        Arrays.fill(playerSelection, false);
        Arrays.fill(playerTypes, BOT);
        // by default, create a local player and a bot
        togglePlayer(0);
        togglePlayer(1);
        // player 0 is always the host/local player
        playerTypes[0] = LOCAL;
        typeButtons[0].setText(R.string.local);
        playerNameFields[0].setEnabled(true);
        playerImages[0].setBackground(getResources().getDrawable(playerIcons[0]));
    }

    /**
     * Registers the game service in the network and regularly update the UI.
     */
    private void startService() {
        if (!serviceActive) {
            String serverName = String.valueOf(playerNameFields[0].getText());
            if ("".equals(serverName))
                serverName = getString(R.string.default_name);

            service = new NsdService(getApplicationContext(), serverName);
            service.startService();
            if (server == null) {
                server = new Server();
                server.registerListener(this);
                server.registerLobby(this);
                server.startCommunication(service.getServerSocket());
                // attach a composer to the server
                composer = new MessageComposer(serverName, true);
                composer.registerServer(server);
                // start the timer for updating the UI
                uiTimer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }
                };
                uiTimer.scheduleAtFixedRate(timerTask, 0, 1000);
            }

            serviceActive = true;
        } else {
            server.runCommunicator();
        }
    }

    /**
     * Stops the service, making sure the lobby isn't visible for searching clients anymore
     */
    private void stopService() {
        if (serviceActive) {
            service.stopService();
            service = null;
            serviceActive = false;
        }
    }

    /**
     * Updates the UI accordingly when a new client joins the lobby
     */
    private void updateUI() {
        if (uiChanged) {
            if (newPlayer != null && openOnlineSlots>0) {
                // begin with index 1 because player 0 is always the server
                for (int i=1; i<MAX_PLAYERS; i++) {
                    if (playerSelection[i] && playerTypes[i].equals(ONLINE) && "".equals(playerNames[i])) {
                        playerNameFields[i].setText(newPlayer);
                        playerNameFields[i].setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                        playerNameFields[i].setTextColor(getResources().getColor(R.color.white));
                        playerNameFields[i].setAlpha(1.0f);
                        playerNames[i] = newPlayer;
                        break;
                    }
                }

                openOnlineSlots--;
                newPlayer = null;
                onlineMode = true;
            }

            uiChanged = false;
        }
    }

    /**
     * Sends all lobby information and settings to the clients, also pushes them into the intent for
     * the next Activity, and then starts the game.
     *
     * @param view The button calling this method
     */
    public void startGame(View view) {
        // if there are still open invitations for clients, display a message and cancel the start
        if (openOnlineSlots > 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.open_slot), Toast.LENGTH_SHORT).show();
            return;
        }

        // make sure every player has a name
        correctNames();

        // transmit lobby information to the client
        if (onlineMode && !transmitLobbyInfo())
            return;

        // create the intent and push the lobby infos
        Intent intent = initIntent();

        // make system ui "invisible"
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);

        // transmit final settings and start the game on all connected instances
        if (onlineMode && !initiateGameStart())
            return;

        if (uiTimer != null)
            uiTimer.cancel();
        stopService(); // the service isn't needed anymore as soon as the connection runs
        startActivity(intent); // start the game activity

        finish(); // end this activity as soon as the game activity finished
    }

    private void correctNames() {
        for (int i=0; i<MAX_PLAYERS; i++) {
            playerNames[i] = String.valueOf(playerNameFields[i].getText());
            if ("".equals(playerNames[i]))
                playerNames[i] = getString(R.string.default_name)+" "+i;
        }
    }

    private boolean waitAck(int id) {
        if (!ackChecker.waitForAcknowledgement(id)) {
            Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private boolean transmitLobbyInfo() {
        // in case the server-side player changes the name before starting the game, update the name just to be safe
        composer.changeName(playerNames[0]);

        for (int i = 0; i < MAX_PLAYERS; i++) {
            // if the player on this index is not enabled
            if (!playerSelection[i])
                continue;

            String type = playerTypes[i];
            if (type.equals(ONLINE)) {
                composer.assignIndex(msgID++, i, playerNames[i]);
                if (!waitAck(msgID-1))
                    return false;
            }
            // Send player info:
            // A player which is an onlineMode player on this server instance, is a local player
            // on a client instance, and vice versa. This has to be considered, or else the
            // different GameManagers get confused.
            //
            // If multiple clients get connected, only one onlineMode player per client becomes
            // local(the one with the same index as i). This is currently considered
            // client-side with an extra check.
            if (type.equals(LOCAL))
                type = ONLINE;

            composer.setPlayer(msgID++, i, type, playerNames[i]);
            if (!waitAck(msgID-1))
                return false;
        }
        return true;
    }

    private Intent initIntent() {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        // add all lobby settings to intent so GameActivity can use them for init
        intent.putExtra("PlayerSelection", playerSelection);
        intent.putExtra("PlayerNames", playerNames);
        intent.putExtra("PlayerTypes", playerTypes);
        intent.putExtra("CheatPermission", cheatsEnabled);
        intent.putExtra("TurnSkips", turnSkips);
        intent.putExtra("BoardType", boardType);
        intent.putExtra("ClientInstance", false);
        intent.putExtra("PlayerIndex", 0);
        intent.putExtra("Online", onlineMode);

        return intent;
    }

    private boolean initiateGameStart() {
        composer.allowCheats(msgID++, cheatsEnabled, turnSkips);
        if (!waitAck(msgID-1))
            return false;
        composer.setBoardType(msgID++, boardType);
        if (!waitAck(msgID-1))
            return false;
        composer.startGame(msgID++);
        if (!waitAck(msgID-1))
            return false;

        return true;
    }

    public void toggleCheats(View view) {
        if (!cheatsEnabled) {
            cheatsEnabled = true;
            turnSkips = 0;
        }

        if (++turnSkips > MAX_TURN_SKIPS) {
            cheatsEnabled = false;
            cheatToggleButton.setText(getString(R.string.allow_cheats));
        } else cheatToggleButton.setText(getString(R.string.punishment)+": "+turnSkips+"x "+getString(R.string.skip_cheater));
    }

    public void toggleGameBoard(View view) {
        if (++boardType >= BOARD_TYPES)
            boardType = 0;
        if (boardType == 0)
            boardToggleButton.setText(getString(R.string.default_eels));
        if (boardType == 1)
            boardToggleButton.setText(getString(R.string.many_eels));
        if (boardType == 2)
            boardToggleButton.setText(getString(R.string.no_eels));
    }

    public void togglePlayer(View view) {
        for (int i=1; i<MAX_PLAYERS; i++) {
            if ((int)view.getId() == i) {
                togglePlayer(i);
                break;
            }
        }
    }

    public void togglePlayerType(View view) {
        for (int i=1; i<MAX_PLAYERS; i++) {
            if ((int)view.getId() == i) {
                togglePlayerType(i);
                break;
            }
        }
    }

    private void togglePlayer(int index) {
        playerSelection[index] = !playerSelection[index];

        for (int i=1; i<MAX_PLAYERS; i++) {
            if (index != i)
                continue;

            playerNameFields[i].setEnabled(playerSelection[i]);
            if (playerSelection[i]) {
                playerImages[i].setBackground(getResources().getDrawable(playerIcons[i]));
                if (playerTypes[i].equals(ONLINE))
                    openOnlineSlots++;
            } else {
                playerImages[i].setBackground(getResources().getDrawable(R.drawable.no_player));
                if (playerTypes[i].equals(ONLINE))
                    openOnlineSlots--;
            }
            break;
        }
        toggleUI();
    }

    private void togglePlayerType(int index) {
        for (int i = 0; i<MAX_PLAYERS; i++) {
            if (i != index)
                continue;
            if (!playerSelection[i])
                return;

            if (playerTypes[i] == BOT) { // if bot
                playerTypes[i] = ONLINE;
                typeButtons[i].setText(R.string.online);
                playerNameFields[i].setText("");
                playerNameFields[i].setHint(R.string.waiting);
                openOnlineSlots++;
                // start service if an online player is requested
                startService();
                Toast.makeText(getApplicationContext(), getString(R.string.service_visible), Toast.LENGTH_SHORT).show();

            } else if (playerTypes[i] == LOCAL) { // if local
                playerTypes[i] = BOT;
                typeButtons[i].setText(R.string.bot);
            } else { // if online
                if ("".equals(playerNames[i])) { // if nobody joined yet
                    playerTypes[i] = LOCAL;
                    typeButtons[i].setText(R.string.local);
                    playerNameFields[i].setHint(R.string.default_name);
                    openOnlineSlots--;
                }
            }
            break;
        }
        toggleUI();
    }

    private void toggleUI() {
        for (int i=1; i<MAX_PLAYERS; i++) {
            playerNameFields[i].setEnabled( playerTypes[i].equals(LOCAL) && playerSelection[i] );
        }
    }

    @Override
    public void inputReceived() {
        String input = server.getInput();
        Log.d(TAG, "Server received input: "+input);
    }

    @Override
    public void ack(int id) {
        ackChecker.setLastAckID(id);
    }

    @Override
    public void joinLobby(int id, String name) {
        Log.d("Debug", "joinLobby(): "+openOnlineSlots+" open slots");
        if (openOnlineSlots>0) {
            newPlayer = name;
            uiChanged = true;
            // answer with an ack
            composer.ack(id);
            Log.d(TAG, "Online player "+name+" joined the lobby");
        } else {
            Log.d(TAG, "Online player "+name+" can't join: no available slots");
        }
    }

    @Override
    public void assignIndex(int id, int index, String clientName) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void allowCheats(int id, boolean permitCheats, int turnSkips) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void setBoardType(int id, int type) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void startGame(int id) {
        // this is the server, so ignore server-exclusive messages
    }
}
