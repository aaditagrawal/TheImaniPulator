package jfxlabproj.musicplayer;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javax.sound.sampled.*;

public class MusicPlayer {

    private static final int BASE_NOTE = 262; // C4 note
    private ScheduledExecutorService executor;
    private volatile boolean isPlaying = false;

    public void playImageAsMusic(Image image) {
        if (image == null) return;
        if (isPlaying) return; // Prevent starting multiple playbacks simultaneously

        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        executor = Executors.newScheduledThreadPool(1);
        isPlaying = true;
        executor.scheduleAtFixedRate(
            () -> {
                Random random = new Random();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (!isPlaying) return; // Stop processing if paused

                        Color color = reader.getColor(x, y);
                        double brightness = color.getBrightness();
                        int note = BASE_NOTE + (int) (brightness * 300); // Vary note based on brightness, reduced range for smoother sound
                        int duration = 150 + random.nextInt(150); // Randomize duration between 150ms and 300ms
                        playTone(note, duration, 0.2f); // Reduce volume
                    }
                }
            },
            0,
            300,
            TimeUnit.MILLISECONDS
        );
    }

    public void pauseMusic() {
        if (executor != null) {
            isPlaying = false;
            executor.shutdown();
        }
    }

    private void playTone(int hz, int duration, float volume) {
        try {
            byte[] buf = new byte[1];
            AudioFormat audioFormat = new AudioFormat(44100, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(audioFormat);
            sdl.open(audioFormat);
            sdl.start();

            FloatControl volumeControl = (FloatControl) sdl.getControl(
                FloatControl.Type.MASTER_GAIN
            );
            volumeControl.setValue(20f * (float) Math.log10(volume)); // Set volume level

            for (int i = 0; i < (duration * 44100) / 1000; i++) {
                double angle = (i / (44100.0 / hz)) * 2.0 * Math.PI;
                buf[0] = (byte) (Math.sin(angle) * 100.0); // Reduce amplitude for a softer sound
                sdl.write(buf, 0, 1);
            }

            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
