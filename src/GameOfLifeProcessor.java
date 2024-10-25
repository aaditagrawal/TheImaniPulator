// GameOfLifeProcessor.java
package jfxlabproj.gameoflife;

import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameOfLifeProcessor {

    private static Timeline timeline;

    public static void startGameOfLife(ImageView imageView) {
        if (imageView.getImage() == null) return;

        // Convert to black and white with chunks
        Image source = imageView.getImage();
        int width = 640;
        int height = (int) (source.getHeight() * (640.0 / source.getWidth()));
        int chunkSize = 8; // Reduced chunk size for better detail
        WritableImage bwImage = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();

        // Calculate average brightness and variance of the entire image
        double totalBrightness = 0;
        double[] brightnesses = new double[width * height];
        int pixelCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(
                    (int) ((x * source.getWidth()) / 640.0),
                    (int) ((y * source.getWidth()) / 640.0)
                );
                double brightness = color.getBrightness();
                brightnesses[pixelCount] = brightness;
                totalBrightness += brightness;
                pixelCount++;
            }
        }

        double averageBrightness = totalBrightness / pixelCount;

        // Calculate variance
        double variance = 0;
        for (int i = 0; i < pixelCount; i++) {
            variance += Math.pow(brightnesses[i] - averageBrightness, 2);
        }
        variance /= pixelCount;

        // Adjust threshold based on variance
        double varianceWeight = Math.min(1.0, Math.max(0.1, variance * 10));
        double threshold = averageBrightness * varianceWeight;

        for (int y = 0; y < height; y += chunkSize) {
            for (int x = 0; x < width; x += chunkSize) {
                double chunkBrightness = 0;
                int validPixels = 0;

                // Calculate average brightness for the chunk
                for (int cy = 0; cy < chunkSize && (y + cy) < height; cy++) {
                    for (int cx = 0; cx < chunkSize && (x + cx) < width; cx++) {
                        Color color = reader.getColor(
                            (int) (((x + cx) * source.getWidth()) / 640.0),
                            (int) (((y + cy) * source.getWidth()) / 640.0)
                        );
                        chunkBrightness += color.getBrightness();
                        validPixels++;
                    }
                }

                double avgChunkBrightness = chunkBrightness / validPixels;
                Color chunkColor = (avgChunkBrightness < threshold)
                    ? Color.BLACK
                    : Color.WHITE;

                // Set the color for the entire chunk
                for (int cy = 0; cy < chunkSize && (y + cy) < height; cy++) {
                    for (int cx = 0; cx < chunkSize && (x + cx) < width; cx++) {
                        bwImage
                            .getPixelWriter()
                            .setColor(x + cx, y + cy, chunkColor);
                    }
                }
            }
        }

        AtomicReference<WritableImage> bwImageRef = new AtomicReference<>(
            bwImage
        );
        imageView.setImage(bwImageRef.get());

        // Start Game of Life
        timeline = new Timeline(
            new KeyFrame(Duration.millis(200), event -> {
                WritableImage nextGeneration = new WritableImage(width, height);
                PixelReader pixelReader = bwImageRef.get().getPixelReader();

                for (int y = 0; y < height; y += chunkSize) {
                    for (int x = 0; x < width; x += chunkSize) {
                        int aliveNeighbors = countAliveNeighbors(
                            pixelReader,
                            x,
                            y,
                            width,
                            height,
                            chunkSize
                        );
                        Color currentColor = pixelReader.getColor(x, y);
                        Color nextColor;

                        if (currentColor.equals(Color.BLACK)) {
                            nextColor = (aliveNeighbors == 3 ||
                                    aliveNeighbors == 2)
                                ? Color.BLACK
                                : Color.WHITE;
                        } else {
                            nextColor = (aliveNeighbors == 3)
                                ? Color.BLACK
                                : Color.WHITE;
                        }

                        // Set the color for the entire chunk in the next generation
                        for (
                            int cy = 0;
                            cy < chunkSize && (y + cy) < height;
                            cy++
                        ) {
                            for (
                                int cx = 0;
                                cx < chunkSize && (x + cx) < width;
                                cx++
                            ) {
                                nextGeneration
                                    .getPixelWriter()
                                    .setColor(x + cx, y + cy, nextColor);
                            }
                        }
                    }
                }

                bwImageRef.set(nextGeneration);
                imageView.setImage(bwImageRef.get());
            })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void stopGameOfLife() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private static int countAliveNeighbors(
        PixelReader reader,
        int x,
        int y,
        int width,
        int height,
        int chunkSize
    ) {
        int count = 0;
        for (int dy = -chunkSize; dy <= chunkSize; dy += chunkSize) {
            for (int dx = -chunkSize; dx <= chunkSize; dx += chunkSize) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (reader.getColor(nx, ny).equals(Color.BLACK)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
