import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;

import static javafx.embed.swing.SwingFXUtils.toFXImage;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by russ on 28/10/2016.
 */
public class ChoiceItem
{

    private final static SLog l = SLog.GetL();
    public Action       state = Action.Store;
    public File         file;
    public Image        image;
    public ImageView    imageView;
    public Button       imageBtn;
    public Dimension    dim = null;
    public long         crc=0;
    public boolean      manualSet=false;
    public enum Action { Store, Keep, Special, Delete };

    @Override
    public String toString()
    {
        return "[fl:" + file.getName() + ", st:" + state + ", ma:" + manualSet + "]";
    }

    public boolean getHashAndBytes()
    {
        try
        {

            long filelength = file.length();
            byte bytes[] = new byte[(int)filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);
            dis.close();

            crc = Utils.BHash(bytes);
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage bimage = null;
            try
            {
                bimage = ImageIO.read(in);
                dim = new Dimension(bimage.getWidth(),bimage.getHeight());
            }
            catch (IOException ex)
            {
                l.severe("IOError:" + ex.toString());
                return false;
            }
            catch (Exception ex)
            {
                l.severe("Error:" + ex.toString());
                return false;
            }

            image = toFXImage(bimage,null);
            return true;
        }

        catch (Exception ex)
        {
            l.severe(ex.getMessage());
            crc=0;
            return false;
        }

    }

    public void Set(Action s)
    {
        manualSet = true;
        state = s;
        switch(state)
        {
            case Store:
                imageBtn.setStyle("-fx-base: LightGray;");
                break;
            case Keep:
                imageBtn.setStyle("-fx-base: Lime;");
                break;
            case Special:
                imageBtn.setStyle("-fx-base: Aqua;");
                break;
            case Delete:
                imageBtn.setStyle("-fx-base: Red;");
                break;
        }        
        
        l.info("Set: " + toString());
    }


 
    public Dimension SetFile(File f)
    {
        this.file = f;

        if ( !getHashAndBytes() )
            return null;

        imageView = new ImageView();
        imageView.setFitHeight(70);
        imageView.setFitWidth(70);
        imageView.setPreserveRatio(false);
        imageView.setImage(image);

        //StackPane sp = new StackPane();
        imageBtn = new Button();
        imageBtn.setPrefSize(90,90);
        imageBtn.setGraphic(imageView);


        imageBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if(button== MouseButton.PRIMARY){
                    Nudge(true);
                }else if(button==MouseButton.SECONDARY){
                    show();
                }else if(button==MouseButton.MIDDLE){
                    System.out.println("MIDDLE button clicked on button");
                }
            }
        });

        imageBtn.setStyle("-fx-base: LightGray;");


        return dim;

    }


    private void show()
    {
        List<File> files = new LinkedList<File>();
        files.add(file);
        PopupImage pi = new PopupImage(jarrivefx.myStage, files, 0);
        pi.display(null);
    }

    public void Nudge(boolean man)
    {
        // if has been manually set and this is not a manual set, just ignore
        if ( manualSet && !man )
        {
            l.info("Nudging, " + toString() + ", man=" + man + ", CAN'T as it manually set & not overridden");
             return;
        }
        
         // nudge it
        switch(state)
        {
            case Store:
                state = Action.Keep;
                imageBtn.setStyle("-fx-base: Lime;");
                break;
            case Keep:
                state = Action.Special;
                imageBtn.setStyle("-fx-base: Aqua;");
                break;
            case Special:
                state = Action.Delete;
                imageBtn.setStyle("-fx-base: Red;");
                break;
            case Delete:
                state = Action.Store;
                imageBtn.setStyle("-fx-base: LightGray;");
                break;
        }
        
        // and record state
        if ( man ) 
            manualSet = true;
        l.info("Nudged, " + toString() + ", man=" + man);

    }

    public void Reset()
    {
        state = Action.Store;
        manualSet = false;
        l.info("Reset, " + toString() );
        imageBtn.setStyle("-fx-base: LightGray;");
    }

}
