package jfxlabproj.musicplayer;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.sound.sampled.*;

public class MusicPlayer {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 2;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;
    private static final int VISUALIZER_WIDTH = 200;
    private static final int VISUALIZER_HEIGHT = 80;
    private static final int BUFFER_SIZE = 4096;
    private static final int PIXEL_SKIP = 100; // Skip factor for pixel processing

    private SourceDataLine line;
    private boolean isPlaying = false;
    private Thread playThread;
    private byte[] audioBuffer;

    private TextArea infoBox;
    private Stage musicInfoStage;
    private double progress = 0;
    private Canvas visualizer;
    private GraphicsContext gc;
    private double[] audioData = new double[100];
    private int audioDataIndex = 0;
    private AnimationTimer visualizerTimer;
    private double lastFrequency = 0;

    public MusicPlayer() {
        initializeUIComponents();
        setupAudioLine();
        audioBuffer = new byte[BUFFER_SIZE];
    }

    private void initializeUIComponents() {
        visualizer = new Canvas(VISUALIZER_WIDTH, VISUALIZER_HEIGHT);
        gc = visualizer.getGraphicsContext2D();
        gc.setStroke(Color.web("#007AFF"));
        gc.setLineWidth(2);

        infoBox = new TextArea();
        infoBox.setEditable(false);
        infoBox.setWrapText(true);
        infoBox.setPrefRowCount(6);
        infoBox.setPrefColumnCount(30);
        infoBox.setStyle(
            "-fx-font-family: 'SF Pro Text'; -fx-font-size: 14px;"
        );
        infoBox.setText(
            "Image to Music Conversion:\n\n" +
            "1. RGB to frequency mapping\n" +
            "2. Brightness controls volume\n" +
            "3. Frequency ranges:\n" +
            "   R: 20-400Hz | G: 400-4000Hz | B: 4000-20000Hz\n\n" +
            "Currently processing: Initializing..."
        );

        VBox infoContainer = new VBox(10);
        infoContainer.setStyle(
            "-fx-padding: 10px; -fx-background-color: white;" +
            "-fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        infoContainer.getChildren().addAll(visualizer, infoBox);

        musicInfoStage = new Stage(StageStyle.DECORATED);
        musicInfoStage.setTitle("Music Generation");
        musicInfoStage.setScene(new Scene(infoContainer));
        musicInfoStage.setAlwaysOnTop(true);
        musicInfoStage.setResizable(false);

        visualizerTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawVisualizer();
            }
        };
    }

    private void drawVisualizer() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, VISUALIZER_WIDTH, VISUALIZER_HEIGHT);

        gc.beginPath();
        gc.moveTo(0, VISUALIZER_HEIGHT / 2);

        for (int i = 0; i < audioData.length; i++) {
            double x = i * (VISUALIZER_WIDTH / (audioData.length - 1));
            double y =
                VISUALIZER_HEIGHT / 2 + (audioData[i] * VISUALIZER_HEIGHT) / 3;
            gc.lineTo(x, y);
        }

        gc.stroke();
    }

    private void setupAudioLine() {
        try {
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
            );
            DataLine.Info info = new DataLine.Info(
                SourceDataLine.class,
                format
            );

            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("Audio line not supported");
            }

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, BUFFER_SIZE);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playImageAsMusic(Image image) {
        if (isPlaying) return;

        musicInfoStage.show();
        isPlaying = true;
        line.start();
        visualizerTimer.start();

        playThread = new Thread(() -> {
            try {
                PixelReader pixelReader = image.getPixelReader();
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();
                int bufferIndex = 0;

                for (int y = 0; y < height && isPlaying; y += PIXEL_SKIP) {
                    for (int x = 0; x < width && isPlaying; x += PIXEL_SKIP) {
                        Color color = pixelReader.getColor(x, y);

                        // Map colors to pentatonic scale frequencies
                        double baseFreq = 220.0; // A3
                        int[] pentatonic = { 0, 2, 4, 7, 9, 12 }; // Pentatonic scale intervals
                        int noteIndex = (int) (color.getRed() * 5);
                        double redFreq =
                            baseFreq *
                            Math.pow(2, pentatonic[noteIndex] / 12.0);

                        noteIndex = (int) (color.getGreen() * 5);
                        double greenFreq =
                            (baseFreq * 2) *
                            Math.pow(2, pentatonic[noteIndex] / 12.0);

                        noteIndex = (int) (color.getBlue() * 5);
                        double blueFreq =
                            (baseFreq * 4) *
                            Math.pow(2, pentatonic[noteIndex] / 12.0);

                        double targetFreq =
                            (redFreq + greenFreq + blueFreq) / 3.0;

                        // Slower, smoother transition
                        lastFrequency =
                            lastFrequency + (targetFreq - lastFrequency) * 0.1;
                        double amplitude = color.getBrightness() * 0.3; // Reduced volume further

                        audioData[audioDataIndex] = amplitude;
                        audioDataIndex =
                            (audioDataIndex + 1) % audioData.length;

                        byte[] soundLeft = generateTone(
                            lastFrequency,
                            50,
                            amplitude
                        );
                        byte[] soundRight = generateTone(
                            lastFrequency * 1.003,
                            50,
                            amplitude * 0.95
                        );
                        byte[] stereoSound = mergeStereoChannels(
                            soundLeft,
                            soundRight
                        );

                        for (byte b : stereoSound) {
                            audioBuffer[bufferIndex++] = b;
                            if (bufferIndex == BUFFER_SIZE) {
                                line.write(audioBuffer, 0, BUFFER_SIZE);
                                bufferIndex = 0;
                            }
                        }

                        final double currentProgress =
                            (y * width + x) / (double) (width * height);
                        final int currentY = y;
                        Platform.runLater(() -> {
                            progress = currentProgress;
                            infoBox.setText(
                                infoBox
                                    .getText()
                                    .replaceAll(
                                        "Currently processing:.*",
                                        String.format(
                                            "Processing: Row %d of %d (%.1f%%)",
                                            currentY,
                                            height,
                                            currentProgress * 100
                                        )
                                    )
                            );
                        });

                        Thread.sleep(20); // Add slight delay between notes
                    }
                }

                if (bufferIndex > 0) {
                    line.write(audioBuffer, 0, bufferIndex);
                }
                line.drain();

                Platform.runLater(() -> {
                    infoBox.appendText("\n\nMusic generation complete!");
                    visualizerTimer.stop();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        playThread.start();
    }

    private byte[] mergeStereoChannels(byte[] left, byte[] right) {
        byte[] stereo = new byte[left.length * 2];
        for (int i = 0, j = 0; i < left.length; i += 2, j += 4) {
            stereo[j] = left[i];
            stereo[j + 1] = left[i + 1];
            stereo[j + 2] = right[i];
            stereo[j + 3] = right[i + 1];
        }
        return stereo;
    }

    private byte[] generateTone(
        double frequency,
        int duration,
        double amplitude
    ) {
        int numSamples = (duration * SAMPLE_RATE) / 1000;
        byte[] buffer = new byte[2 * numSamples];

        for (int i = 0; i < numSamples; i++) {
            double angle = (2.0 * Math.PI * i * frequency) / SAMPLE_RATE;
            double sample =
                Math.sin(angle) * 0.5 + // fundamental
                Math.sin(2 * angle) * 0.1 + // second harmonic
                Math.sin(4 * angle) * 0.05 + // fourth harmonic
                Math.sin(angle / 2) * 0.1; // subharmonic

            // Apply envelope for smoother sound
            double envelope = Math.sin((Math.PI * i) / numSamples);
            short value = (short) (amplitude *
                Short.MAX_VALUE *
                sample *
                envelope);

            buffer[2 * i] = (byte) (value >> 8);
            buffer[2 * i + 1] = (byte) (value & 0xFF);
        }

        return buffer;
    }

    private void updateProgress(double value) {
        progress = value;
    }

    public void pauseMusic() {
        isPlaying = false;
        visualizerTimer.stop();
        if (line != null) {
            line.stop();
            line.flush();
        }
        if (playThread != null) {
            playThread.interrupt();
        }
        musicInfoStage.hide();
    }

    public double getProgress() {
        return progress;
    }

    public void cleanup() {
        if (line != null) {
            line.close();
        }
        visualizerTimer.stop();
    }
}
