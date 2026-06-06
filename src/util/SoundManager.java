package util;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {

    private static final String SOUND_PATH = "assets/sounds/";

    public static void playSound(String fileName) {
        try {
            File file = new File(SOUND_PATH + fileName);

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(file);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Clip playLoop(String fileName) {
        try {
            File file = new File(SOUND_PATH + fileName);

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(file);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);

            return clip;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void stopSound(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
