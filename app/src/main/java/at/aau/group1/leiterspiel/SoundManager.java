package at.aau.group1.leiterspiel;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Igor on 25.04.2016.
 *
 * Loads and plays all needed sounds in the game.
 */
public class SoundManager {

    private SoundPool soundPool; // SoundPool loads the files and keeps them in memory for replaying, avoiding delays unlike MediaPlayer
    private int diceID;
    private int connectedID;
    private int failID;
    private float volume;

    public SoundManager(Context context) {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        diceID = soundPool.load(context, R.raw.roll_dice, 1);
        connectedID = soundPool.load(context, R.raw.connected, 1);
        failID = soundPool.load(context, R.raw.fail, 1);
        volume = 1.0f;
    }

    public void playSound(String sound) {
        if (sound.equals("dice"))
            soundPool.play(diceID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("connected"))
            soundPool.play(connectedID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("fail"))
            soundPool.play(failID, volume, volume, 1, 0, 1.0f);
    }
}
