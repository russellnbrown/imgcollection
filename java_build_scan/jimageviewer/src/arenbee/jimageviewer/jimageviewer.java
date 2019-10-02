/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arenbee.jimageviewer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import arenbee.jimageutils.ImgCollection;
import arenbee.jimageutils.ImgCollectionDirItem;
import arenbee.jimageutils.ImgCollectionFileItem;
import arenbee.jimageutils.ImgCollectionImageItem;
import arenbee.jimageviewer.jfxImg.DirObservableList;
import arenbee.jimageviewer.jfxImg.FileObservableList;
import arenbee.jutils.Logger;
import arenbee.jutils.Utils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author russe
 */
public class jimageviewer extends Application {

    jfxImg ji = new jfxImg();

    Text statusTxt;
    ListView dirList;
    ListView fileList;
    TilePane iconPane;
    ScrollPane iconScroll;
    //AnchorPane previewPane;
    ImageViewPane viewPane;
    boolean iconSizeChanged = true; // force a check on size when first run
    public static final int tndispsize = 32;
    public static final int tpad = 3;
 
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        Logger.Create("jimageviewer", Logger.Level.Debug, Logger.Level.Info);

        Parameters p = getParameters();
        String setName = "";
        Map<String, String> pars = p.getNamed();

        if (!pars.containsKey("set")) {
            throw (new Exception("no --set defined"));
        }
        setName = pars.get("set");

        StackPane root = new StackPane();

        VBox rootContainer = addRootContainer();

        ToolBar tbtop = addToolbarTop();
        ToolBar tbbtm = addToolbarBottom();
        SplitPane dirFilePrvSplit = addDirFilePreviewPane();

        rootContainer.getChildren().add(tbtop);
        rootContainer.getChildren().add(dirFilePrvSplit);
        rootContainer.getChildren().add(tbbtm);

        root.getChildren().add(rootContainer);

        Scene scene = new Scene(root, 900, 600);

        loadSet(setName);

        
        Timeline tmr = new Timeline(new KeyFrame(Duration.millis(500), ae -> onTimer()));
        tmr.setCycleCount(Animation.INDEFINITE);
        tmr.play();        
        
        primaryStage.setTitle("Collection Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void loadSet(String setName) {
        ji.Open(Paths.get(setName));
        Integer nimg = ji.getNumberOfImages();
        statusTxt.setText("Images in set:" + nimg);

        ObservableList<DirObservableList> dirs = ji.getListOfDirectories();

        dirList.setItems(dirs);
    }

    
    
    
    public Pane addDirPane()
    {
        VBox lp = new VBox();
        lp.setPrefHeight(Double.MAX_VALUE);
        lp.setMinWidth(50.0);
        lp.setMaxWidth(200.0);
        lp.setPrefWidth(200.0);
        lp.setStyle("-fx-background-color: #996633;");  
        
        lp.setPadding(new Insets(1, 1, 1, 1)); 
        dirList = new ListView();
        dirList.setPrefHeight(10000.0);
        dirList.setPrefWidth(10000.0);
        dirList.setStyle("-fx-background-color: #aabbcc;");  
        
        dirList.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent arg0) 
            {
                DirObservableList selDir = (DirObservableList) dirList.getSelectionModel().getSelectedItem(); 
                if ( selDir == null )
                    return;
                Logger.Info("clicked on " + selDir.getPath() );

                List<ImgCollectionFileItem> files = ji.getListOfFilesInDirectory(selDir.icdi);
                loadADirIntoIconView(files);
  //              fileList.setItems(files);
            }});
        
        lp.getChildren().add(dirList);
        
        return lp;
    }
   
    private void loadADirIntoIconView(List<ImgCollectionFileItem> files)
    {
        
            iconPane.getChildren().clear();
            
            List<ImageView> ilist = new LinkedList<ImageView>();
            for (ImgCollectionFileItem f : files)
            {
                Path p = ji.getPathOfImgFile(f);
                
                ImageView iv = ji.getThumbImageFrom(f,tndispsize,tndispsize);
   
 
                iv.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Path px = ji.getPathOfImgFile(f);
                        Logger.Info("Tile pressed, img is  " + px.getFileName() );
                        ImageView v = ji.getImageFrom(p);
                        viewPane.setImageView(v);
                       
                        event.consume();
                    }
                });
 
                ilist.add(iv);
            }            
            
            for (ImageView v : ilist)
            {

                //v.setOnMouseClicked(new OnIconImageViewMouseClicked(v));
                //v.setOnContextMenuRequested(new OnIconImageViewContextMenuRequested(v));
            }
            iconPane.getChildren().addAll(ilist);
            iconPane.requestFocus();//setOnKeyPressed(new OnIconPaneKeyPress());   
    }
    
    private class OnIconPaneKeyPress implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent kev)
        {
/*
            ImageView v = null;
            int cwid = iconPane.getPrefColumns();
            Logger.Warn("IP - Key:" + kev.getCode().toString() + ", Wid:" + cwid );

            switch( kev.getCode() )
            {
                case ESCAPE:
                    System.exit(0);
                    return;

                case ENTER:
                    v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex);
                    if ( v != null )
                        onIconViewImageDoubleClicked(v);
                    return;

                case RIGHT:
                    if ( selectedIconViewIndex < iconPane.getChildren().size()  )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex + 1);
                        Logger.Warn("MoveRIGHT, select " + (selectedIconViewIndex + 1));
                    }
                    break;

                case DOWN:
                    if ( selectedIconViewIndex+cwid < iconPane.getChildren().size()  )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex + cwid);
                        Logger.Warn("MoveDOWN, select " + (selectedIconViewIndex + 1));
                    }
                    break;

                case LEFT:
                    if ( selectedIconViewIndex > 0 && iconPane.getChildren().size() > 0 )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex - 1);
                        Logger.Warn("MoveLEFT, select " + (selectedIconViewIndex - 1));
                    }
                    break;

                case UP:
                    if ( selectedIconViewIndex-cwid >= 0  )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex - cwid);
                        Logger.Warn("MoveUP, select " + (selectedIconViewIndex - cwid));
                    }
                    break;

            }
            if ( v != null )
                onIconViewImageSingleClicked(v);
            */

        }
    } 
   
    public ScrollPane addFilePane()
    {

        iconScroll = new ScrollPane();        
        iconPane = new TilePane();
        
        iconPane.setVgap((double) 3.0);
        iconPane.setHgap((double) 3.0);
        iconPane.setPrefHeight(10000.0);
        iconPane.setPrefWidth(10000.0);
        iconScroll.setMinWidth(50.0);
        iconScroll.setMaxWidth(200.0);

        iconPane.setStyle("-fx-background-color: DAE6F3;");
        
        iconScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);    // Horizontal scroll bar
        iconScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Vertical scroll bar
        iconScroll.setFitToHeight(true);
        iconScroll.setFitToWidth(true);
        iconScroll.setContent(iconPane);   
        
        iconScroll.widthProperty().addListener(new ChangeListener<Number>() { 
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                iconSizeChanged = true;
            }});
        iconScroll.heightProperty().addListener(new ChangeListener<Number>() { 
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                iconSizeChanged = true;
            }});

        iconPane.setOnKeyPressed(new OnIconPaneKeyPress());        

 
        return iconScroll;
    }
 
    

   public void onTimer()
    {

        try
        {
            if (iconSizeChanged)
            {
                Logger.Info("Detect change in scroll area");
                int cols = (int) (iconScroll.getWidth() / (float) (tndispsize + tpad));
                iconPane.setMaxWidth(iconScroll.getWidth() - 10F);
                iconPane.setPrefColumns(cols);
                iconSizeChanged = false;
            }
        }
        catch (Exception e)
        {
            Logger.Warn("OnTimer:");
            e.printStackTrace();
        }

    }
    
    
   
    public ImageViewPane addPreviewPane()
    {
        //Image image = new Image(getClass().getResourceAsStream("blank.png"));
        //ImageView imageView = new ImageView();
        //imageView.setImage(image);
        ImageView bi = ji.getImageFrom("blank");
        viewPane = new ImageViewPane(bi);

        return viewPane;
    }
    
    public SplitPane addDirFilePreviewPane() {
        SplitPane sp = new SplitPane();

        sp.setPrefHeight(Double.MAX_VALUE);
        sp.setPrefHeight(10000.0);
        sp.setPrefWidth(10000.0);
        sp.setMinHeight(100.0);
        sp.setMinWidth(100.0);

        Pane lp = addDirPane();
        ScrollPane mp = addFilePane();
        

       ImageViewPane pp = addPreviewPane();
 

        sp.getItems().add(lp);
        sp.getItems().add(mp);
        sp.getItems().add(pp);

        return sp;
    }

    

    
    public VBox addRootContainer() {
        VBox hbox = new VBox();
        hbox.setStyle("-fx-background-color: #aaaaaa;");
        return hbox;
    }

    public ToolBar addToolbarTop() {
        ToolBar toolBar = new ToolBar();

        //Button button1 = new Button("Button 1");
        //toolBar.getItems().add(button1);

       // Button button2 = new Button("Button 2");
        //toolBar.getItems().add(button2);

        return toolBar;
    }

    public ToolBar addToolbarBottom() {
        statusTxt = new Text();
        statusTxt.setFont(new Font(20));
        ToolBar toolBar = new ToolBar();

        toolBar.getItems().add(statusTxt);
        return toolBar;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }

}
