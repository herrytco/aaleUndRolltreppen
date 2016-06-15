package at.aau.group1.leiterspiel;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Igor on 25.04.2016.
 */
public class SoundManager {

    private SoundPool soundPool;
    private int diceID;
    private int connectedID;
    private float volume;
    private Context appContext;

    public SoundManager(Context context) {
        this.appContext = context;
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        diceID = soundPool.load(appContext, R.raw.roll_dice, 1);
        connectedID = soundPool.load(appContext, R.raw.connected, 1);
        volume = 1.0f;
    }

    public void playDiceSound() {
        soundPool.play(diceID, volume, volume, 1, 0, 1.0f);
    }

    public void playConnectionSound() {
        soundPool.play(connectedID, volume, volume, 1, 0, 1.0f);
    }
}
