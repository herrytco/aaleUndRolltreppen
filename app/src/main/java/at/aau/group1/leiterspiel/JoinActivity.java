package at.aau.group1.leiterspiel;

import android.content.Context;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import at.aau.group1.leiterspiel.Network.Client;
import at.aau.group1.leiterspiel.Network.ILobby;
import at.aau.group1.leiterspiel.Network.INsdObserver;
import at.aau.group1.leiterspiel.Network.MessageComposer;
import at.aau.group1.leiterspiel.Network.MessageParser;
import at.aau.group1.leiterspiel.Network.NsdDiscovery;
import at.aau.group1.leiterspiel.Network.NsdService;

public class JoinActivity extends AppCompatActivity implements INsdObserver, ILobby {

    private final String TAG = "Join";

    private LinearLayout list;
    private EditText playerNameInput;
    private Button startButton;

    public static Client client;
    public static MessageComposer composer;

    private boolean discoveryStarted = false;
    private NsdDiscovery discovery;
    private Timer uiTimer;
    private TimerTask timerTask;
    private ArrayList<NsdServiceInfo> unhandledInfos = new ArrayList<>();
    private TreeMap<Integer, NsdServiceInfo> availableServices = new TreeMap<>();

    private boolean uiChanged = false;

    private String clientName = "Missing name";
    private int msgID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        list = (LinearLayout) findViewById(R.id.servicesList);
        playerNameInput = (EditText) findViewById(R.id.clientNameView);
        startButton = (Button) findViewById(R.id.searchButton);
    }

    private void updateUI() {
        if (uiChanged) {
            for (NsdServiceInfo info: unhandledInfos) {
                String serverName = info.getServiceName().replace(NsdService.SERVICE_NAME, "");
                serverName = serverName.replace("\\032", " ");

                Button button = new Button(getApplicationContext());
                button.setText(serverName);
                button.setBackgroundColor(getResources().getColor(R.color.blue));
                button.setTextColor(getResources().getColor(R.color.white));
                int id = availableServices.size()+1;
                availableServices.put(id, info);
                button.setId(id); // set ID so it can be identified later on when clicked
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinService(v);
                    }
                });
                list.addView(button);
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

        composer.joinLobby(msgID++);
        Log.d(TAG, "Attempt to join selected lobby...(ID "+(msgID-1)+")");
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

            discovery.startDiscovery();
            discoveryStarted = true;
            Log.d(TAG, "Started discovery as "+clientName);
        }
    }

    @Override
    public void ack(int id) {
        Log.d(TAG, "Ack for message ID "+id+" received");
    }

    @Override
    public void joinLobby(int id, String name) {

    }

    @Override
    public void setPlayer(int id, int playerIndex, String playerType, String playerName) {

    }

    @Override
    public void allowCheats(int id, boolean permitCheats) {

    }

    @Override
    public void startGame(int id) {

    }
}
