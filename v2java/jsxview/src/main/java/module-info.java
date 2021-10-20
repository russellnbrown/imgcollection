module com.arenbee.jsxview {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
                    requires org.kordamp.bootstrapfx.core;
        
    opens com.arenbee.jsxview to javafx.fxml;
    exports com.arenbee.jsxview;
}