// ImageProcessor.java
package jfxlabproj;

import javafx.animation.FadeTransition;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ImageProcessor {

    public static void applySepia(ImageView imageView) {
        if (imageView.getImage() != null) {
            SepiaTone sepia = new SepiaTone(0.7);
            Bloom bloom = new Bloom(0.2);
            bloom.setInput(sepia);

            DropShadow shadow = new DropShadow();
            shadow.setRadius(10.0);
            shadow.setSpread(0.3);
            shadow.setColor(Color.rgb(0, 0, 0, 0.4));
            shadow.setInput(bloom);

            FadeTransition ft = new FadeTransition(
                Duration.millis(300),
                imageView
            );
            ft.setFromValue(0.7);
            ft.setToValue(1.0);

            imageView.setEffect(shadow);
            ft.play();
        }
    }

    public static void applyVignette(ImageView imageView) {
        if (imageView.getImage() != null) {
            // Create radial gradient for vignette effect
            double width = imageView.getImage().getWidth();
            double height = imageView.getImage().getHeight();
            double radius = Math.max(width, height) * 0.7; // Adjust this value to control vignette size

            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setRadius(radius * 0.3);
            innerShadow.setChoke(0.2);
            innerShadow.setColor(Color.rgb(0, 0, 0, 0.7));

            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(radius * 0.2);
            dropShadow.setSpread(0.4);
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.6));
            dropShadow.setInput(innerShadow);

            FadeTransition ft = new FadeTransition(
                Duration.millis(300),
                imageView
            );
            ft.setFromValue(0.7);
            ft.setToValue(1.0);

            imageView.setEffect(dropShadow);
            ft.play();
        }
    }

    public static void applyBlur(ImageView imageView) {
        if (imageView.getImage() != null) {
            GaussianBlur blur = new GaussianBlur(15);
            Bloom bloom = new Bloom(0.3);
            bloom.setInput(blur);

            DropShadow shadow = new DropShadow();
            shadow.setRadius(15.0);
            shadow.setSpread(0.4);
            shadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadow.setInput(bloom);

            FadeTransition ft = new FadeTransition(
                Duration.millis(300),
                imageView
            );
            ft.setFromValue(0.7);
            ft.setToValue(1.0);

            imageView.setEffect(shadow);
            ft.play();
        }
    }

    public static void applyGrayscale(ImageView imageView) {
        if (imageView.getImage() != null) {
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1);
            grayscale.setContrast(0.2);
            grayscale.setBrightness(0.1);

            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setRadius(5.0);
            innerShadow.setColor(Color.rgb(0, 0, 0, 0.3));
            innerShadow.setInput(grayscale);

            FadeTransition ft = new FadeTransition(
                Duration.millis(300),
                imageView
            );
            ft.setFromValue(0.7);
            ft.setToValue(1.0);

            imageView.setEffect(innerShadow);
            ft.play();
        }
    }
}
