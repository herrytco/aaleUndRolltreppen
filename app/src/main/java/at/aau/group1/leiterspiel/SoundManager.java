package at.aau.group1.leiterspiel;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Igor on 25.04.2016.
 */
public class SoundManager {

    private MediaPlayer mediaPlayer;
    private Context appContext;

    public SoundManager(Context context) {
        this.appContext = context;
        mediaPlayer = MediaPlayer.create(appContext, R.raw.roll_dice);// = new MediaPlayer();
    }

    public void playDiceSound() {
        if (appContext!=null) {
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
            mediaPlayer.seekTo(0);
        }
    }
}
