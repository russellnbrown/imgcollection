module com.arenbee.jfxview {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
                    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires com.twelvemonkeys.common.image;
    requires org.apache.commons.lang3;

    opens com.arenbee.jfxview to javafx.fxml;
    exports com.arenbee.jfxview;
}