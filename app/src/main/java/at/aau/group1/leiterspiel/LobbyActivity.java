package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.group1.leiterspiel.Network.ICommListener;
import at.aau.group1.leiterspiel.Network.ILobby;
import at.aau.group1.leiterspiel.Network.MessageComposer;
import at.aau.group1.leiterspiel.Network.MessageParser;
import at.aau.group1.leiterspiel.Network.NsdService;
import at.aau.group1.leiterspiel.Network.Server;

/**
 * Created by Igor on 22.05.2016.
 */
public class LobbyActivity extends AppCompatActivity implements ICommListener, ILobby {

    private final String TAG = "Lobby";

    public static final int MAX_PLAYERS = 6;
    public static final String BOT = "Bot";
    public static final String LOCAL = "Local";
    public static final String ONLINE = "Online";

    private NsdService service;
    private String serverName = "Missing name";
    private boolean serviceActive = false;
    private Timer uiTimer;
    private TimerTask timerTask;
    private String newPlayer;
    private boolean uiChanged = false;

    public static Server server;
    public static MessageComposer composer;

    private String[] playerNames = new String[MAX_PLAYERS];
    private boolean[] playerSelection = new boolean[MAX_PLAYERS];
    private String[] playerTypes = new String[MAX_PLAYERS];

    private ImageButton playerImage0;
    private ImageButton playerImage1;
    private ImageButton playerImage2;
    private ImageButton playerImage3;
    private ImageButton playerImage4;
    private ImageButton playerImage5;
    private Button typeButton0;
    private Button typeButton1;
    private Button typeButton2;
    private Button typeButton3;
    private Button typeButton4;
    private Button typeButton5;
    private EditText playerName0;
    private EditText playerName1;
    private EditText playerName2;
    private EditText playerName3;
    private EditText playerName4;
    private EditText playerName5;
    private Button cheatToggleButton;

    private boolean cheatsEnabled = false;

    private int msgID = 0;

    //Fullscreen
    private Fullscreen fs = new Fullscreen();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        playerImage0 = (ImageButton) findViewById(R.id.playerImage0);
        playerImage1 = (ImageButton) findViewById(R.id.playerImage1);
        playerImage2 = (ImageButton) findViewById(R.id.playerImage2);
        playerImage3 = (ImageButton) findViewById(R.id.playerImage3);
        playerImage4 = (ImageButton) findViewById(R.id.playerImage4);
        playerImage5 = (ImageButton) findViewById(R.id.playerImage5);
        typeButton0 = (Button) findViewById(R.id.playerType0);
        typeButton1 = (Button) findViewById(R.id.playerType1);
        typeButton2 = (Button) findViewById(R.id.playerType2);
        typeButton3 = (Button) findViewById(R.id.playerType3);
        typeButton4 = (Button) findViewById(R.id.playerType4);
        typeButton5 = (Button) findViewById(R.id.playerType5);
        playerName0 = (EditText) findViewById(R.id.playerName0);
        playerName1 = (EditText) findViewById(R.id.playerName1);
        playerName2 = (EditText) findViewById(R.id.playerName2);
        playerName3 = (EditText) findViewById(R.id.playerName3);
        playerName4 = (EditText) findViewById(R.id.playerName4);
        playerName5 = (EditText) findViewById(R.id.playerName5);
        cheatToggleButton = (Button) findViewById(R.id.cheatToggleButton);

        fs.setDecorView(getWindow().getDecorView());
        fs.hideSystemUI();

        init();
    }

    @Override
    protected void onStop() {
        stopService();
        super.onStop();
    }

    private void init() {
        Arrays.fill(playerTypes, BOT);
        // by default, create a local player and a bot
        togglePlayer(0);
        togglePlayer(1);
        // player 0 is always the host/local player
        playerTypes[0] = LOCAL;
        typeButton0.setText(R.string.local);
        playerName0.setEnabled(true);
    }

    private void startService() {
        if (!serviceActive) {
            serverName = playerName0.getText().toString();

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
                timerTask = new TimerTask() {
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
        }
    }

    private void stopService() {
        if (serviceActive) {
            service.stopService();
            service = null;
            serviceActive = false;
        }
    }

    private void updateUI() {
        if (uiChanged) {
            if (newPlayer != null) {
                if (playerSelection[1] && playerTypes[1].equals(ONLINE)) {
                    playerName1.setText(newPlayer);
                    playerName1.setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                    playerName1.setTextColor(getResources().getColor(R.color.white));
                    playerName1.setAlpha(1.0f);
                } else if (playerSelection[2] && playerTypes[2].equals(ONLINE)) {
                    playerName2.setText(newPlayer);
                    playerName2.setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                    playerName2.setTextColor(getResources().getColor(R.color.white));
                    playerName2.setAlpha(1.0f);
                } else if (playerSelection[3] && playerTypes[3].equals(ONLINE)) {
                    playerName3.setText(newPlayer);
                    playerName3.setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                    playerName3.setTextColor(getResources().getColor(R.color.white));
                    playerName3.setAlpha(1.0f);
                } else if (playerSelection[4] && playerTypes[4].equals(ONLINE)) {
                    playerName4.setText(newPlayer);
                    playerName4.setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                    playerName4.setTextColor(getResources().getColor(R.color.white));
                    playerName4.setAlpha(1.0f);
                } else if (playerSelection[5] && playerTypes[5].equals(ONLINE)) {
                    playerName5.setText(newPlayer);
                    playerName5.setBackground(getResources().getDrawable(R.drawable.rounded_online_player));
                    playerName5.setTextColor(getResources().getColor(R.color.white));
                    playerName5.setAlpha(1.0f);
                }

                newPlayer = null;
            }

            uiChanged = false;
        }
    }

    public void startGame(View view) {
        playerNames[0] = String.valueOf(playerName0.getText());
        playerNames[1] = String.valueOf(playerName1.getText());
        playerNames[2] = String.valueOf(playerName2.getText());
        playerNames[3] = String.valueOf(playerName3.getText());
        playerNames[4] = String.valueOf(playerName4.getText());
        playerNames[5] = String.valueOf(playerName5.getText());

        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        // add all lobby settings to intent so GameActivity can use them for init
        intent.putExtra("PlayerSelection", playerSelection);
        intent.putExtra("PlayerNames", playerNames);
        intent.putExtra("PlayerTypes", playerTypes);
        intent.putExtra("CheatPermission", cheatsEnabled);

        //makes system ui "inivisible"           --NOT WORKING PROPERLY YET!! HAS TO BE UPDATED!
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);

        startActivity(intent); // start the game activity
        finish(); // end this activity as soon as the game activity finished (?)
    }

    public void toggleCheats(View view) {
        cheatsEnabled = !cheatsEnabled;
        if (cheatsEnabled)
            cheatToggleButton.setText(getResources().getString(R.string.forbid_cheats));
        else
            cheatToggleButton.setText(getResources().getString(R.string.allow_cheats));
    }

    public void togglePlayer(View view) {
        if (view.getId() == playerImage0.getId()) togglePlayer(0);
        else if (view.getId() == playerImage1.getId()) togglePlayer(1);
        else if (view.getId() == playerImage2.getId()) togglePlayer(2);
        else if (view.getId() == playerImage3.getId()) togglePlayer(3);
        else if (view.getId() == playerImage4.getId()) togglePlayer(4);
        else if (view.getId() == playerImage5.getId()) togglePlayer(5);
    }

    public void togglePlayerType(View view) {
        if (view.getId() == typeButton0.getId()) togglePlayerType(0);
        if (view.getId() == typeButton1.getId()) togglePlayerType(1);
        if (view.getId() == typeButton2.getId()) togglePlayerType(2);
        if (view.getId() == typeButton3.getId()) togglePlayerType(3);
        if (view.getId() == typeButton4.getId()) togglePlayerType(4);
        if (view.getId() == typeButton5.getId()) togglePlayerType(5);
    }

    private void togglePlayer(int index) {
        playerSelection[index] = !playerSelection[index];

        if (index == 0) playerName0.setEnabled(playerSelection[index]);
        if (index == 1) playerName1.setEnabled(playerSelection[index]);
        if (index == 2) playerName2.setEnabled(playerSelection[index]);
        if (index == 3) playerName3.setEnabled(playerSelection[index]);
        if (index == 4) playerName4.setEnabled(playerSelection[index]);
        if (index == 5) playerName5.setEnabled(playerSelection[index]);

        if (playerSelection[index]) {
            if (index == 0) playerImage0.setBackground(getResources().getDrawable(R.drawable.patrick));
            if (index == 1) playerImage1.setBackground(getResources().getDrawable(R.drawable.sponge));
            if (index == 2) playerImage2.setBackground(getResources().getDrawable(R.drawable.krabs));
            if (index == 3) playerImage3.setBackground(getResources().getDrawable(R.drawable.squid));
            if (index == 4) playerImage4.setBackground(getResources().getDrawable(R.drawable.plankton));
            if (index == 5) playerImage5.setBackground(getResources().getDrawable(R.drawable.sandy));
        } else {
            if (index == 0) playerImage0.setBackground(getResources().getDrawable(R.drawable.no_player));
            if (index == 1) playerImage1.setBackground(getResources().getDrawable(R.drawable.no_player));
            if (index == 2) playerImage2.setBackground(getResources().getDrawable(R.drawable.no_player));
            if (index == 3) playerImage3.setBackground(getResources().getDrawable(R.drawable.no_player));
            if (index == 4) playerImage4.setBackground(getResources().getDrawable(R.drawable.no_player));
            if (index == 5) playerImage5.setBackground(getResources().getDrawable(R.drawable.no_player));
        }
        toggleUI();
    }

    private void togglePlayerType(int index) {
        for (int i = 0; i<MAX_PLAYERS; i++) {
            if (i == index) {
                if (playerTypes[i] == BOT) { // if bot
                    playerTypes[i] = ONLINE;
                    if (index==1) typeButton1.setText(R.string.online);
                    if (index==2) typeButton2.setText(R.string.online);
                    if (index==3) typeButton3.setText(R.string.online);
                    if (index==4) typeButton4.setText(R.string.online);
                    if (index==5) typeButton5.setText(R.string.online);

                    // start service if an online player is requested
                    startService();

                } else if (playerTypes[i] == LOCAL) { // if local
                    playerTypes[i] = BOT;
                    if (index==1) typeButton1.setText(R.string.bot);
                    if (index==2) typeButton2.setText(R.string.bot);
                    if (index==3) typeButton3.setText(R.string.bot);
                    if (index==4) typeButton4.setText(R.string.bot);
                    if (index==5) typeButton5.setText(R.string.bot);
                } else { // if online
                    playerTypes[i] = LOCAL;
                    if (index==1) typeButton1.setText(R.string.local);
                    if (index==2) typeButton2.setText(R.string.local);
                    if (index==3) typeButton3.setText(R.string.local);
                    if (index==4) typeButton4.setText(R.string.local);
                    if (index==5) typeButton5.setText(R.string.local);
                }
                break;
            }
        }
        toggleUI();
    }

    private void toggleUI() {
        if (playerTypes[1]==BOT || playerTypes[1]==ONLINE || !playerSelection[1]) playerName1.setEnabled(false);
        else playerName1.setEnabled(true);
        if (playerTypes[2]==BOT || playerTypes[2]==ONLINE || !playerSelection[2]) playerName2.setEnabled(false);
        else playerName2.setEnabled(true);
        if (playerTypes[3]==BOT || playerTypes[2]==ONLINE || !playerSelection[3]) playerName3.setEnabled(false);
        else playerName3.setEnabled(true);
        if (playerTypes[4]==BOT || playerTypes[2]==ONLINE || !playerSelection[4]) playerName4.setEnabled(false);
        else playerName4.setEnabled(true);
        if (playerTypes[5]==BOT || playerTypes[2]==ONLINE || !playerSelection[5]) playerName5.setEnabled(false);
        else playerName5.setEnabled(true);
    }

    @Override
    public void inputReceived() {
        String input = server.getInput();
        Log.d(TAG, "Server received input: "+input);
    }

    @Override
    public void ack(int id) {
        // TODO
    }

    @Override
    public void joinLobby(int id, String name) {
        newPlayer = name;
        uiChanged = true;
        // answer with an ack
        composer.ack(id);
        Log.d(TAG, "Online player "+name+" joined the lobby");
    }

    @Override
    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void allowCheats(int id, boolean permitCheats) {
        // this is the server, so ignore server-exclusive messages
    }

    @Override
    public void startGame(int id) {
        // this is the server, so ignore server-exclusive messages
    }
}
