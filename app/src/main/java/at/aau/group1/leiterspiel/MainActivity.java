package at.aau.group1.leiterspiel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start game(GameActivity) immediately
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(intent);
    }
}
