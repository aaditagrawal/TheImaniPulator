package jfxlabproj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jfxlabproj.gameoflife.GameOfLifeProcessor;
import jfxlabproj.musicplayer.MusicPlayer;

public class TheImaniPulator extends Application {

    private ImageView imageView;
    private Image originalImage;
    private MusicPlayer musicPlayer;
    private Button activeButton = null;
    private Label statusLabel;

    // Store color frequency analysis results
    private LinkedList<String> topColors;

    // Helper class to store color counts for sorting
    private class ColorCount implements Comparable<ColorCount> {

        String hexColor;
        int count;

        ColorCount(String hexColor, int count) {
            this.hexColor = hexColor;
            this.count = count;
        }

        @Override
        public int compareTo(ColorCount other) {
            return other.count - this.count; // For descending order
        }
    }

    // Method to analyze color frequencies in image
    private void analyzeColors() {
        if (originalImage == null) return;

        // Use ConcurrentHashMap for thread safety
        Map<String, Integer> colorFrequency = new ConcurrentHashMap<>();

        // Get PixelReader to read image colors
        PixelReader pixelReader = originalImage.getPixelReader();

        int height = (int) originalImage.getHeight();
        int width = (int) originalImage.getWidth();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Split image into rows for parallel processing
        int rowsPerThread = height / numThreads;

        for (int i = 0; i < numThreads; i++) {
            final int startY = i * rowsPerThread;
            final int endY = (i == numThreads - 1)
                ? height
                : (i + 1) * rowsPerThread;

            executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        String hex = String.format(
                            "#%02X%02X%02X",
                            (int) (color.getRed() * 255),
                            (int) (color.getGreen() * 255),
                            (int) (color.getBlue() * 255)
                        );
                        colorFrequency.merge(hex, 1, Integer::sum);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Priority queue to find top 10 colors
        PriorityQueue<ColorCount> pq = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : colorFrequency.entrySet()) {
            pq.offer(new ColorCount(entry.getKey(), entry.getValue()));
        }

        // Get top 10 colors
        topColors = new LinkedList<>();
        for (int i = 0; i < 10 && !pq.isEmpty(); i++) {
            ColorCount cc = pq.poll();
            topColors.add(
                String.format("%s: %d pixels", cc.hexColor, cc.count)
            );
        }

        // Show results in alert on JavaFX thread
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Color Analysis");
            alert.setHeaderText("Top 10 Most Common Colors in this image:");
            alert.setContentText(String.join("\n", topColors));
            alert.showAndWait();
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Processor");

        Scene welcomeScene = WelcomeScreen.createScene(primaryStage);
        primaryStage.setScene(welcomeScene);

        primaryStage.setOnCloseRequest(e -> {
            if (musicPlayer != null) {
                musicPlayer.pauseMusic();
            }
        });

        primaryStage.show();
    }

    public Scene createMainScene(Stage primaryStage) {
        HBox root = new HBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F5F5F7;");
        root.setPadding(new Insets(30));

        VBox leftSide = new VBox(20);
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setStyle(
            "-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(500);
        imageView.setPreserveRatio(true);
        imageView.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        statusLabel = new Label("Image Processor");
        statusLabel.setStyle(
            "-fx-font-family: 'SF Pro Display'; -fx-font-size: 18px; " +
            "-fx-font-weight: bold; -fx-text-fill: #1D1D1F;"
        );

        Button loadButton = new Button("Choose Image");
        Button analyzeButton = new Button("Analyze Colors"); // New button for color analysis
        styleButton(loadButton);
        styleButton(analyzeButton);

        leftSide
            .getChildren()
            .addAll(imageView, loadButton, analyzeButton, statusLabel);

        VBox rightSide = new VBox(15);
        rightSide.setAlignment(Pos.TOP_CENTER);
        rightSide.setPrefWidth(400);
        rightSide.setStyle(
            "-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 30;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        Label titleLabel = new Label("Image Processor");
        titleLabel.setStyle(
            "-fx-font-family: 'SF Pro Display'; -fx-font-size: 32px; -fx-font-weight: bold;"
        );
        titleLabel.setTextFill(Paint.valueOf("#1D1D1F"));

        Label filterLabel = new Label("Image Filters");
        Label saveLabel = new Label("Save Options");
        Label specialLabel = new Label("Special Effects");
        Label musicLabel = new Label("Music Controls");
        Label resetLabel = new Label("Reset Options");

        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        saveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        specialLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        musicLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        resetLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button blurButton = new Button("Blur");
        Button grayscaleButton = new Button("Grayscale");
        Button sepiaButton = new Button("Sepia");
        Button resetButton = new Button("Reset");
        Button saveAsJpegButton = new Button("Save as JPEG");
        Button saveAsPngButton = new Button("Save as PNG");
        Button saveAsHeifButton = new Button("Save as HEIF");
        Button gameOfLifeButton = new Button("Game of Life");
        Button playMusicButton = new Button("Play as Music");
        Button pauseMusicButton = new Button("Pause Music");
        Button vignetteButton = new Button("Vignette");

        // Style reset button differently
        resetButton.setStyle(
            "-fx-background-color: black;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'SF Pro Text';" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;"
        );

        int buttonWidth = 150;
        for (Button btn : new Button[] {
            blurButton,
            grayscaleButton,
            sepiaButton,
            saveAsJpegButton,
            saveAsPngButton,
            saveAsHeifButton,
            gameOfLifeButton,
            playMusicButton,
            pauseMusicButton,
            vignetteButton,
        }) {
            styleButton(btn);
            btn.setPrefWidth(buttonWidth);
            btn.setWrapText(true);
        }

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);
        filterGrid.add(blurButton, 0, 0);
        filterGrid.add(grayscaleButton, 1, 0);
        filterGrid.add(sepiaButton, 0, 1);
        filterGrid.add(vignetteButton, 0, 2);

        GridPane resetGrid = new GridPane();
        resetGrid.setHgap(10);
        resetGrid.setVgap(10);
        resetGrid.add(resetButton, 0, 0);

        GridPane saveGrid = new GridPane();
        saveGrid.setHgap(10);
        saveGrid.setVgap(10);
        saveGrid.add(saveAsJpegButton, 0, 0);
        saveGrid.add(saveAsPngButton, 1, 0);
        saveGrid.add(saveAsHeifButton, 0, 1);

        GridPane specialGrid = new GridPane();
        specialGrid.setHgap(10);
        specialGrid.add(gameOfLifeButton, 0, 0);

        GridPane musicGrid = new GridPane();
        musicGrid.setHgap(10);
        musicGrid.add(playMusicButton, 0, 0);
        musicGrid.add(pauseMusicButton, 1, 0);

        analyzeButton.setOnAction(e -> {
            if (originalImage != null) {
                analyzeColors();
                updateStatus("Analyzing Colors");
            }
        });

        blurButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(blurButton);
                ImageProcessor.applyBlur(imageView);
                updateStatus("Blur");
            }
        });

        grayscaleButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(grayscaleButton);
                ImageProcessor.applyGrayscale(imageView);
                updateStatus("Grayscale");
            }
        });

        sepiaButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(sepiaButton);
                ImageProcessor.applySepia(imageView);
                updateStatus("Sepia");
            }
        });

        vignetteButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(vignetteButton);
                ImageProcessor.applyVignette(imageView);
                updateStatus("Vignette");
            }
        });

        resetButton.setOnAction(e -> {
            if (originalImage != null) {
                GameOfLifeProcessor.stopGameOfLife();
                setActiveButton(null);
                imageView.setImage(originalImage);
                imageView.setEffect(null);
                updateStatus("Image Processor");
            }
        });

        saveAsJpegButton.setOnAction(e -> {
            saveImage(primaryStage, "jpg"); // Changed from jpeg to jpg
            updateStatus("Saving as JPEG");
        });

        saveAsPngButton.setOnAction(e -> {
            saveImage(primaryStage, "png");
            updateStatus("Saving as PNG");
        });

        saveAsHeifButton.setOnAction(e -> {
            saveImage(primaryStage, "heif");
            updateStatus("Saving as HEIF");
        });

        gameOfLifeButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(gameOfLifeButton);
                GameOfLifeProcessor.startGameOfLife(imageView);
                updateStatus("Game of Life");
            }
        });

        playMusicButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(playMusicButton);
                musicPlayer = new MusicPlayer();
                musicPlayer.playImageAsMusic(originalImage);
                updateStatus(
                    "Playing Music - " +
                    String.format("%.1f%%", musicPlayer.getProgress() * 100)
                );
            }
        });

        pauseMusicButton.setOnAction(e -> {
            if (musicPlayer != null) {
                setActiveButton(null);
                musicPlayer.pauseMusic();
                updateStatus("Music Paused");
            }
        });

        loadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser
                .getExtensionFilters()
                .add(
                    new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif"
                    )
                );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                originalImage = image;
                setActiveButton(null);
                updateStatus("Image Processor");
            }
        });

        rightSide
            .getChildren()
            .addAll(
                titleLabel,
                filterLabel,
                filterGrid,
                resetLabel,
                resetGrid,
                saveLabel,
                saveGrid,
                specialLabel,
                specialGrid,
                musicLabel,
                musicGrid
            );

        root.getChildren().addAll(leftSide, rightSide);

        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.web("#F5F5F7"));

        return scene;
    }

    private void updateStatus(String status) {
        statusLabel.setText(status);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            styleButton(activeButton);
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.setStyle(
                "-fx-background-color: #FFC799;" +
                "-fx-text-fill: black;" +
                "-fx-font-family: 'SF Pro Text';" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 12px 20px;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);"
            );
        }
    }

    private void saveImage(Stage stage, String format) {
        if (imageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.setInitialFileName("processed_image." + format);
            fileChooser
                .getExtensionFilters()
                .add(
                    new FileChooser.ExtensionFilter(
                        format.toUpperCase() + " files",
                        "*." + format
                    )
                );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    // Get the original image directly instead of taking a snapshot
                    Image image = imageView.getImage();
                    int width = (int) image.getWidth();
                    int height = (int) image.getHeight();

                    BufferedImage bufferedImage = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                    );

                    PixelReader reader = image.getPixelReader();
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            Color color = reader.getColor(x, y);
                            int rgb =
                                ((int) (color.getRed() * 255) << 16) |
                                ((int) (color.getGreen() * 255) << 8) |
                                ((int) (color.getBlue() * 255));
                            bufferedImage.setRGB(x, y, rgb);
                        }
                    }

                    ImageIO.write(bufferedImage, format, file);
                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not save image");
                    alert.setContentText("Error: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    private void styleButton(Button button) {
        String defaultStyle =
            "-fx-background-color: #F5F5F7;" +
            "-fx-text-fill: #1D1D1F;" +
            "-fx-font-family: 'SF Pro Text';" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #D2D2D7;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-width: 1px;";

        String hoverStyle =
            "-fx-background-color: #E8E8ED;" +
            "-fx-text-fill: #1D1D1F;" +
            "-fx-font-family: 'SF Pro Text';" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12px 20px;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #007AFF;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-width: 2px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);";

        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
