package jfxlabproj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Processor");

        Scene welcomeScene = WelcomeScreen.createScene(primaryStage);
        primaryStage.setScene(welcomeScene);

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
        styleButton(loadButton);

        leftSide.getChildren().addAll(imageView, loadButton, statusLabel);

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

        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        saveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        specialLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        musicLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

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
        Button invertButton = new Button("Invert Colors");

        int buttonWidth = 150;
        for (Button btn : new Button[] {
            blurButton,
            grayscaleButton,
            sepiaButton,
            resetButton,
            saveAsJpegButton,
            saveAsPngButton,
            saveAsHeifButton,
            gameOfLifeButton,
            playMusicButton,
            pauseMusicButton,
            invertButton,
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
        filterGrid.add(resetButton, 1, 1);
        filterGrid.add(invertButton, 0, 2);

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

        invertButton.setOnAction(e -> {
            if (originalImage != null) {
                setActiveButton(invertButton);
                ImageProcessor.applyInvert(imageView);
                updateStatus("Invert Colors");
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
            saveImage(primaryStage, "jpeg");
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
                updateStatus("Playing Music");
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
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(
                        imageView.getImage(),
                        null
                    );
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
