

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import static javafx.scene.input.MouseEvent.*;


/**
 * Created by russ on 7/08/2016.
 */
public class PopupImage
{

    private Stage pstage;
    private Stage dialog;
    private List<File> files;
    private double zfactor = 0.1;
    private double zoomby = 1.0;
    private Group imgGrp = new Group();
    private Point2D dragStartedAt;
    private Point2D offsetby = new Point2D(0.0,0.0);
    private ContextMenu cc = null;
    private int pos=0;
    private Image i=null;
    private ImageView img=null;
    private BorderPane pane;
    private final static SLog l = SLog.GetL();
    private Dimension is;
    private double monW;
    private double monH;

    public PopupImage(Stage pstage, List<File> files, int pos)
    {
        this.files = files;
        this.pstage = pstage;
        this.pos = pos;
    }

    public void Close()
    {
        dialog.close();
    }
    public void Full()
    {
        dialog.setFullScreen(!dialog.isFullScreen());
    }

    public void ToFront()
    {
        dialog.toFront();
    }

    public void ToBack()
    {
        dialog.toBack();
    }

    public void reset()
    {
        dialog.setFullScreen(false);
        double scale = calcFirstScale(i);
        Dimension2D wanted = new Dimension2D(i.getWidth()*scale, i.getHeight()*scale);

        img.setFitHeight(wanted.getHeight());
        img.setFitWidth(wanted.getWidth());
        zoomby=0.0;
        zoom(scale);
        offsetby =  new Point2D(0.0,0.0);
        translate(0.0,0.0);
        pane.requestLayout();
    }

    private double calcFirstScale(Image i)
    {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        monW = gd.getDisplayMode().getWidth();
        monH = gd.getDisplayMode().getHeight();
        monW -= 60;
        monH -=60;

        double zoomby = 1.0;
        double zh = monH / i.getHeight();
        double zw = monW / i.getWidth();
        zoomby = Math.min(zw,zh);
        zoomby = Math.min(zoomby,1.0);

        return zoomby;
    }

    public Stage display(ContextMenu cc)
    {
        dialog = new Stage(StageStyle.UNIFIED);

        this.cc = cc;
        dialog.initModality(Modality.NONE);

        try
        {
            i = new Image(files.get(pos).toURI().toURL().toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        double scale = calcFirstScale(i);

        // TODO double click expands to max size to fit on screen

        Dimension2D wanted = new Dimension2D(i.getWidth()*scale, i.getHeight()*scale);

        img = new ImageView();
        img.setImage(i);
        img.setFitHeight(wanted.getHeight());
        img.setFitWidth(wanted.getWidth());

        pane = new BorderPane();

        pane.setCenter(img);
        img.setOnContextMenuRequested(
                new EventHandler<ContextMenuEvent>()
                {
                    @Override
                    public void handle(ContextMenuEvent event)
                    {
                        if ( cc != null )
                            cc.show(img, event.getScreenX(), event.getScreenY());
                    }
                });
        imgGrp.getChildren().add(pane);

        imgGrp.setOnScroll(new ScrollEventHandler() );
        pane.addEventHandler(MOUSE_RELEASED, new MouseHandler());
        pane.addEventHandler(MOUSE_CLICKED, new MouseHandler());
        pane.addEventHandler(MOUSE_PRESSED, new MouseHandler());
        pane.addEventHandler(MOUSE_DRAGGED, new MouseHandler());

        Scene scene = new Scene(imgGrp);
		scene.addEventFilter(KeyEvent.KEY_PRESSED, new OnSceneKeyFilter());

        dialog.setTitle(String.format("%s  %.0fx%.0f  %s", files.get(pos).getName(), i.getWidth(), i.getHeight(), files.get(pos).getParent()));
        dialog.setScene(scene);

        dialog.widthProperty().addListener(new ResizeListener());
        dialog.heightProperty().addListener(new ResizeListener());
        dialog.setFullScreenExitHint("");
        dialog.show();

        return dialog;

    }

    private void changeTo(int _pos)
    {
        try
        {
            i = new Image(files.get(pos).toURI().toURL().toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        double scale = calcFirstScale(i);

        Dimension2D wanted = new Dimension2D(i.getWidth()*scale, i.getHeight()*scale);

        img.setImage(i);
        img.setFitHeight(wanted.getHeight());
        img.setFitWidth(wanted.getWidth());

        dialog.setTitle(String.format("%s  %.0fx%.0f  %s", files.get(pos).getName(), i.getWidth(), i.getHeight(), files.get(pos).getParent()));

        double zh = monH / i.getHeight();
        double zw = monW / i.getWidth();
        double fsfit = Math.min(zw,zh);

        double xfree = monW - i.getWidth()*fsfit;
        double yfree = monH - i.getHeight()*fsfit;


        zoomby=0F;
        zoom(fsfit);
        offsetby = new Point2D(0F,0F);
        translate(xfree/2.0, yfree/2.0);
        oldPos = null;

        l.warn("CHANGE to %s, scale %.1f free %.0f %.0f" , files.get(pos).getName() , fsfit, xfree, yfree );
        //dialog.setHeight(wanted.getHeight());
        //dialog.setWidth(wanted.getWidth());
    }

    private Dimension2D oldPos = null;
    private void onSized()
    {
        if ( Double.isNaN(dialog.getWidth()) || Double.isNaN(dialog.getHeight()) )
            return;

        if ( oldPos == null )
        {
            oldPos = new Dimension2D(dialog.getWidth(), dialog.getHeight());
            return;
        }
        double dx = (dialog.getWidth() - oldPos.getWidth())/2.0;
        double dy = (dialog.getHeight() - oldPos.getHeight())/2.0;

        l.warn(String.format("Resize old %.0f,%.0f new %.0f,%.0f change %.0f %.0f ",
                oldPos.getWidth(), oldPos.getHeight(),
                dialog.getWidth(), dialog.getHeight(),
                dx,dy));

        translate(dx,dy);
        oldPos = new Dimension2D(dialog.getWidth(), dialog.getHeight());
        //iv.setFitHeight(dialogVbox.getHeight());
        //iv.setFitWidth(dialogVbox.getWidth());
    }
    private class ResizeListener implements ChangeListener<Number>
    {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
        {
            onSized();
        }
    }
    private void zoom(double to)
    {
        zoomby += to;
        imgGrp.setScaleX(zoomby);
        imgGrp.setScaleY(zoomby);
    }

    private void translate(double byx, double byy)
    {
        offsetby = offsetby.add(byx, byy);
        imgGrp.setTranslateX(offsetby.getX());
        imgGrp.setTranslateY(offsetby.getY());
    }
    private class OnSceneKeyFilter implements EventHandler<KeyEvent>
    {
        public void handle(KeyEvent kev)
        {
            switch( kev.getCode() )
            {
                case ESCAPE:
                    System.exit(0);
                    break;

                case F:
                    Full();
                    break;

                case Q:
                    Close();
                    break;

                case R:
                    reset();
                    break;

                case RIGHT:
                    if ( !dialog.isFullScreen())
                        return;
                    if ( pos < files.size()-1 )
                        changeTo(++pos);
                    break;
                case LEFT:
                    if ( !dialog.isFullScreen())
                        return;
                    if ( pos > 0 )
                        changeTo(--pos);
                    break;
            }
        }
    }
    private class ScrollEventHandler implements EventHandler
    {
        @Override
        public void handle(javafx.event.Event event)
        {
            ScrollEvent sev = (ScrollEvent)event;
            zfactor = zoomby / 10.0;
           if (  sev.getDeltaY() > 0 )
                zoom(zfactor);
            else if (  sev.getDeltaY() < 0 )
                zoom(-zfactor);
        }
    }

    private class MouseHandler implements EventHandler<InputEvent>
    {
        @Override
        public void handle(InputEvent event)
        {

            MouseEvent me = (MouseEvent)event;
            if ( me.getEventType().equals(MOUSE_CLICKED)  )
            {
                System.out.printf("Click..btn=%s cc=%d",  me.getButton().toString(), me.getClickCount() );
                if ( me.getButton() == MouseButton.PRIMARY && me.getClickCount() == 2 )
                    Full();
            }
            else if ( me.getEventType().equals(MOUSE_PRESSED) )
            {
                if ( me.getButton() == MouseButton.MIDDLE )
                {
                    Close();
                    return;
                }
                dragStartedAt = new Point2D(me.getScreenX(), me.getScreenY());
            }
            else if ( me.getEventType().equals(MOUSE_DRAGGED) && dragStartedAt!= null )
            {
                double offx = me.getScreenX() - dragStartedAt.getX();
                double offy = me.getScreenY() - dragStartedAt.getY();
                dragStartedAt = new Point2D(me.getScreenX(), me.getScreenY());
                translate(offx, offy);
            }
        }
    }
}
