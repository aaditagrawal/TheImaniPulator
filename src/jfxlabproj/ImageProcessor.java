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

    public static void applyInvert(ImageView imageView) {
        if (imageView.getImage() != null) {
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-1.0);
            colorAdjust.setContrast(0);
            colorAdjust.setHue(1.0);
            colorAdjust.setSaturation(-1.0);

            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setRadius(5.0);
            innerShadow.setColor(Color.rgb(255, 255, 255, 0.3));
            innerShadow.setInput(colorAdjust);

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
