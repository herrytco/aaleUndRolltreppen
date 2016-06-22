package at.aau.group1.leiterspiel;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.Random;

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
    private int eelID;
    private int escID1;
    private int escID2;
    private float volume;

    public SoundManager(Context context) {
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        diceID = soundPool.load(context, R.raw.roll_dice, 1);
        connectedID = soundPool.load(context, R.raw.connected, 1);
        failID = soundPool.load(context, R.raw.dolphin, 1);
        eelID = soundPool.load(context, R.raw.aale, 1);
        escID1 = soundPool.load(context, R.raw.rolltreppen_1, 1);
        escID2 = soundPool.load(context, R.raw.rolltreppen_2, 1);
        volume = 1.0f;
    }

    public void playSound(String sound) {
        if (sound == null)
            return;

        if (sound.equals("dice"))
            soundPool.play(diceID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("connected"))
            soundPool.play(connectedID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("fail"))
            soundPool.play(failID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("eel"))
            soundPool.play(eelID, volume, volume, 1, 0, 1.0f);
        if (sound.equals("esc")) {
            Random r = new Random();
            r.setSeed(System.currentTimeMillis());
            if (r.nextBoolean())
                soundPool.play(escID1, volume, volume, 1, 0, 1.0f);
            else
                soundPool.play(escID2, volume, volume, 1, 0, 1.0f);
        }
    }
}
