module jfxlabproj {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop; // To access BufferedImage and ImageIO
    requires javafx.swing; // To access SwingFXUtils

    opens jfxlabproj to javafx.fxml;
    exports jfxlabproj;
}
