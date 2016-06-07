package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onStart() {
        super.onStart();

        //Help button: Not Implemented Yet: Triggers Easter egg!

        Button helpbutton = (Button) findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //URL: Easter Egg Video. Since Help has not function yet: Computer says No!
                Uri uri = Uri.parse("https://drive.google.com/file/d/0B0IOMdsaLshDVTdnV1NreUpjODQ/view?usp=sharing"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Start game(GameActivity) immediately
//        startLobby(null);
    }

    public void startLogin(View view) {
       // Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void startLobby(View view) {
        // Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
/*
    //This should hide system bars

    public void hideSystemUI() {
        //Set IMMERSIVE Flag
        //Set content to appear UNDER system bars
        //No resizeing when sys bar hides/shows

        mDecorView.setSystemUIVisibility(
                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );

    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.

    public void showSystemUI(){
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

*/
}
