/**
 * Created by russellb on 9/08/2016.
 */

package arenbee.jfxview;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;


public class jfxview extends Application
{

    public static void main(String[] args)
    {

        launch(args);
    }
    private final static SLog l = SLog.CreateL("jfxview", SLog.SLevel.Low, SLog.SLevel.Warning);
    private static jfxview instance = null;
    public static jfxview get() { return instance; };
    public Scene scene;
    public Stage stage;

    @Override
    public void start(Stage primaryStage)
    {
        instance = this;
        stage = primaryStage;

        final Parameters params = getParameters();
        final Map<String,String> parameters = params.getNamed();

        ViewSettings.init();

        String ss = parameters.get("scanset");
        String top = parameters.get("top");
        if ( top != null && top.length() > 0 )
            ViewSettings.top = top;
        if ( ss != null && ss.length() > 0 )
            ViewSettings.searchset = ss;

        ViewSettings.save();

        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("jfxview.fxml"));
            Parent root = (Parent)loader.load();
            scene = new Scene(root);
            jfxviewdocumentcontroller controller = (jfxviewdocumentcontroller)loader.getController();
            controller.initializeLater(scene,primaryStage);

            primaryStage.setMinHeight(750);
            primaryStage.setMinWidth(1000);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
