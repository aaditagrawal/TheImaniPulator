package jfxlabproj;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WelcomeScreen {

    public static Scene createScene(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1E1E1E;" + "-fx-padding: 40px;");

        Label welcomeLabel = new Label("Image Processor");
        welcomeLabel.setStyle(
            "-fx-font-family: 'SF Pro Display';" +
            "-fx-font-size: 48px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFFFFF;"
        );

        Label subtitleLabel = new Label("Made by Aadit, Akhil and Saurabh.");
        subtitleLabel.setStyle(
            "-fx-font-family: 'SF Pro Text';" +
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #BBBBBB;"
        );

        Button nextButton = new Button("Get Started");
        nextButton.setStyle(
            "-fx-background-color: #0F0F0F;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'SF Pro Text';" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;"
        );

        nextButton.setOnMouseEntered(e ->
            nextButton.setStyle(
                nextButton.getStyle() + "-fx-background-color: #0071E3;"
            )
        );
        nextButton.setOnMouseExited(e ->
            nextButton.setStyle(
                nextButton.getStyle() + "-fx-background-color: #0F0F0F;"
            )
        );

        nextButton.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(event -> {
                TheImaniPulator mainApp = new TheImaniPulator();
                primaryStage.setScene(mainApp.createMainScene(primaryStage));
            });
            ft.play();
        });

        root.getChildren().addAll(welcomeLabel, subtitleLabel, nextButton);

        Scene scene = new Scene(root, 800, 600);
        scene.setFill(Color.web("#1E1E1E"));

        return scene;
    }
}
