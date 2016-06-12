package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Arrays;

/**
 * Created by Igor on 22.05.2016.
 */
public class LobbyActivity extends AppCompatActivity {

    public static final int MAX_PLAYERS = 6;
    public static final String BOT = "Bot";
    public static final String LOCAL = "Local";
    public static final String ONLINE = "Online";

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

    private void init() {
        Arrays.fill(playerTypes, BOT);
        // by default, create a local player and a bot
        togglePlayer(0);
        togglePlayer(1);
        togglePlayerType(0);
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
        updateUI();
    }

    private void togglePlayerType(int index) {
        for (int i = 0; i<MAX_PLAYERS; i++) {
            if (i == index) {
                if (playerTypes[i] == BOT) { // if bot
                    playerTypes[i] = LOCAL;
                    if (index==0) typeButton0.setText(R.string.local);
                    if (index==1) typeButton1.setText(R.string.local);
                    if (index==2) typeButton2.setText(R.string.local);
                    if (index==3) typeButton3.setText(R.string.local);
                    if (index==4) typeButton4.setText(R.string.local);
                    if (index==5) typeButton5.setText(R.string.local);
                } else if (playerTypes[i] == LOCAL) { // if local
                    playerTypes[i] = BOT;
                    if (index==0) typeButton0.setText(R.string.bot);
                    if (index==1) typeButton1.setText(R.string.bot);
                    if (index==2) typeButton2.setText(R.string.bot);
                    if (index==3) typeButton3.setText(R.string.bot);
                    if (index==4) typeButton4.setText(R.string.bot);
                    if (index==5) typeButton5.setText(R.string.bot);
                } else { // if online
                    playerTypes[i] = LOCAL;
                    if (index==0) typeButton0.setText(R.string.local);
                    if (index==1) typeButton1.setText(R.string.local);
                    if (index==2) typeButton2.setText(R.string.local);
                    if (index==3) typeButton3.setText(R.string.local);
                    if (index==4) typeButton4.setText(R.string.local);
                    if (index==5) typeButton5.setText(R.string.local);
                }
                break;
            }
        }
        updateUI();
    }

    private void updateUI() {
        if (playerTypes[0]==BOT || !playerSelection[0]) playerName0.setEnabled(false);
        else playerName0.setEnabled(true);
        if (playerTypes[1]==BOT || !playerSelection[1]) playerName1.setEnabled(false);
        else playerName1.setEnabled(true);
        if (playerTypes[2]==BOT || !playerSelection[2]) playerName2.setEnabled(false);
        else playerName2.setEnabled(true);
        if (playerTypes[3]==BOT || !playerSelection[3]) playerName3.setEnabled(false);
        else playerName3.setEnabled(true);
        if (playerTypes[4]==BOT || !playerSelection[4]) playerName4.setEnabled(false);
        else playerName4.setEnabled(true);
        if (playerTypes[5]==BOT || !playerSelection[5]) playerName5.setEnabled(false);
        else playerName5.setEnabled(true);
    }

}
