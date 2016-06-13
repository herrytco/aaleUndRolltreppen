package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Fullscreen
    private Fullscreen fs = new Fullscreen();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        fs.setDecorView(getWindow().getDecorView());
        fs.hideSystemUI();

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
    }

    public void startLogin(View view) {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void startLobby(View view) {
         Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void joinLobby(View view) {
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        startActivity(intent);
    }

}
