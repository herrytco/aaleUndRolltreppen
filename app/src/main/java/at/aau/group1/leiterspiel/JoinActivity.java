package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import at.aau.group1.leiterspiel.Network.AckChecker;
import at.aau.group1.leiterspiel.Network.Client;
import at.aau.group1.leiterspiel.Network.ILobby;
import at.aau.group1.leiterspiel.Network.INsdObserver;
import at.aau.group1.leiterspiel.Network.MessageComposer;
import at.aau.group1.leiterspiel.Network.NsdDiscovery;
import at.aau.group1.leiterspiel.Network.NsdService;

public class JoinActivity extends AppCompatActivity implements INsdObserver, ILobby {

    private final String TAG = "Join";

    // UI
    private LinearLayout list;
    private EditText playerNameInput;
    private Button startButton;
    private static boolean uiChanged = false;
    private SoundManager soundManager;

    // network
    public static Client client;
    public static MessageComposer composer;
    public static int msgID = 0;

    private AckChecker ackChecker = new AckChecker();
    private static boolean discoveryStarted = false;
    private static NsdDiscovery discovery;
    private static Timer uiTimer;
    private static TimerTask timerTask;
    private static ArrayList<NsdServiceInfo> unhandledInfos = new ArrayList<>();
    private static TreeMap<Integer, NsdServiceInfo> availableServices = new TreeMap<>();

    // lobby settings, set by server
    private static boolean[] playerSelection = new boolean[LobbyActivity.MAX_PLAYERS];
    private static String[] playerNames = new String[LobbyActivity.MAX_PLAYERS];
    private static String[] playerTypes = new String[LobbyActivity.MAX_PLAYERS];
    private static boolean cheatsEnabled = false;
    private static int turnSkips = 1;
    private static int boardType = 0;

    private static String clientName = "Missing name";
    private static int playerIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        list = (LinearLayout) findViewById(R.id.servicesList);
        playerNameInput = (EditText) findViewById(R.id.clientNameView);
        startButton = (Button) findViewById(R.id.searchButton);

        soundManager = new SoundManager(getApplicationContext());

        // Prevents the keyboard from immediately appearing, but then the button has to be pressed
        // twice before it reacts...because Android.
//        startButton.setFocusableInTouchMode(true);
//        startButton.requestFocus();
    }

    private void updateUI() {
        if (uiChanged) {
            for (NsdServiceInfo info: unhandledInfos) {
                String serverName = info.getServiceName().replace(NsdService.SERVICE_NAME, "");
                // space gets transmitted as \032
                serverName = serverName.replace("\\032", " ");

                // create a new button in the server list for connecting
                View view = getLayoutInflater().inflate(getResources().getLayout(R.layout.list_button), list);
                Button button = (Button) view.findViewById(R.id.listButton);
                button.setText(serverName);
                int id = availableServices.size()+1;
                availableServices.put(id, info);
                button.setId(id); // set ID so it can be identified later on when clicked
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinService(v);
                    }
                });
            }
            unhandledInfos.clear();
            uiChanged = false;
        }
    }

    @Override
    protected void onPause() {
        if (discoveryStarted) discovery.stopDiscovery();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (discoveryStarted) discovery.startDiscovery();
        super.onResume();
    }

    @Override
    public void notifyOfNewService(NsdServiceInfo serviceInfo) {
        unhandledInfos.add(serviceInfo);
        uiChanged = true;
    }

    private void joinService(View view) {
        int id = view.getId();
        NsdServiceInfo info = availableServices.get(id);

        if (client == null) {
            client = new Client();
            client.registerLobby(this);
            client.connectToServer(info);
            composer = new MessageComposer(clientName, false);
            composer.registerClient(client);
        } else {
            client.disconnect();
            client.connectToServer(info);
        }

        for (int i=0; i<list.getChildCount(); i++) {
            list.getChildAt(i).setEnabled(false);
        }
        // in case connecting takes a bit longer
        startButton.setText(getString(R.string.connecting));

        composer.joinLobby(msgID++);
        Log.d(TAG, "Attempt to join selected lobby...(ID "+(msgID-1)+")");

        if (ackChecker.waitForAcknowledgement(msgID-1)) {
            startButton.setText(getString(R.string.connected_wait));
            startButton.setBackgroundColor(getResources().getColor(R.color.darkgreen));
            soundManager.playConnectionSound();
        } else {
            startButton.setText(getString(R.string.connection_failed));
            startButton.setBackgroundColor(getResources().getColor(R.color.darkred));
        }
    }

    public void initDiscovery(View view) {
        Log.d(TAG, "initDiscovery");
        if (!discoveryStarted) {
            clientName = playerNameInput.getText().toString();

            discovery = new NsdDiscovery(getApplicationContext(), clientName);
            discovery.registerObserver(this);

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
            startButton.setText(R.string.searching_games);
            startButton.setBackgroundColor(getResources().getColor(R.color.orange));

            discovery.startDiscovery();
            discoveryStarted = true;
            Log.d(TAG, "Started discovery as "+clientName);
        }
    }

    @Override
    public void ack(int id) {
        ackChecker.setLastAckID(id);
    }

    @Override
    public void joinLobby(int id, String name) {
        // do nothing as client
    }

    @Override
    public void assignIndex(int id, int index, String clientName) {
        if (this.clientName.equals(clientName)) playerIndex = index;

        composer.ack(id);
    }

    @Override
    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
        this.playerSelection[playerIndex] = true;
        this.playerTypes[playerIndex] = playerType;
        this.playerNames[playerIndex] = playerName;

        composer.ack(id);
    }

    @Override
    public void allowCheats(int id, boolean permitCheats, int turnSkips) {
        this.cheatsEnabled = permitCheats;
        this.turnSkips = turnSkips;

        composer.ack(id);
    }

    @Override
    public void setBoardType(int id, int type) {
        boardType = type;

        composer.ack(id);
    }

    @Override
    public void startGame(int id) {
        // This is the player controlled on this client instance, which means it's local instead of
        // online here. See startGame() method in LobbyActivity.
        for (int i=0; i<LobbyActivity.MAX_PLAYERS; i++) {
            if (i == this.playerIndex && playerTypes[i].equals(LobbyActivity.ONLINE))
                playerTypes[i] = LobbyActivity.LOCAL;
        }

        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        // add all lobby settings to intent so GameActivity can use them in initGame()
        intent.putExtra("PlayerSelection", playerSelection);
        intent.putExtra("PlayerNames", playerNames);
        intent.putExtra("PlayerTypes", playerTypes);
        intent.putExtra("CheatPermission", cheatsEnabled);
        intent.putExtra("TurnSkips", turnSkips);
        intent.putExtra("BoardType", boardType);
        intent.putExtra("ClientInstance", true);
        intent.putExtra("PlayerIndex", playerIndex);
        intent.putExtra("Online", true);

        composer.ack(id);

        startActivity(intent); // start the game activity
        finish(); // end this activity as soon as the game activity finished
    }
}
