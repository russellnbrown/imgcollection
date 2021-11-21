package arenbee.jfxview;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static arenbee.jfxview.Utils.humanReadableByteCount;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;


import static arenbee.jfxview.ViewSettings.tndispsize;



/**
 * @author russ
 */
public class jfxviewdocumentcontroller implements Initializable
{
    @FXML private TreeView dirTree;
    @FXML private ImageView imageView;
    @FXML private TilePane iconPane;
    @FXML private ScrollPane iconScroll;
    @FXML private AnchorPane imageAnchor;
    @FXML private CheckBox dirCB;
    @FXML private CheckBox filesCB;
    @FXML private ImageView simageIV;
    @FXML private TableView tsearchLV;
    @FXML private Tab scanTAB;
    @FXML private TextField searchTF;
    @FXML private VBox topvbox;
    @FXML private AnchorPane imageAP;
    @FXML private Label listLabel;
    @FXML private Label imageLabel;
    @FXML private Label imageDim;
    @FXML private Label statusMem;
    @FXML private Label statusSearch;
    @FXML private ListView<ScanSet.SSFile> instancesList;
    @FXML private ChoiceBox scanTypeCB;
    @FXML private TextField scanSetTF;
    @FXML private TextField topDirTF;

    @FXML private Button fwdBtn;
    @FXML private Button backBtn;
    @FXML private ChoiceBox dirSortCB;


    @FXML public void exitApplication(ActionEvent event)
    {
        Platform.exit();
    }

    @FXML void onFwdBtn(ActionEvent event)
    {
        dhistory.Fwd();
    }

    @FXML void onBackBtn(ActionEvent event)
    {
        dhistory.Back();
    }

    @FXML void onSearchTextBtnPushed(ActionEvent event)
    {
        boolean sdir = dirCB.isSelected();
        boolean stext = filesCB.isSelected();
        ScanSet.SSSearch.SSSearchTextTask tt = srch.createTextSearch();
        tt.setSearchDirs(sdir);
        tt.setSearchFiles(stext);
        tt.setTextSearchString(searchTF.getText());
        if (srch.startSearch(tt))
            currentSearch = tt;
        else
            Utils.whoops("Search Engine Error", "Refused our search", "the search engine was not ready to do a search");
    }

    @FXML void onChangeScanSet(ActionEvent event)
    {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File current = null;
        if ( ViewSettings.searchset != null && ViewSettings.searchset.length() > 0 )
        {
            current = new File(ViewSettings.searchset);
            if ( current.exists() )
                fileChooser.setInitialDirectory(current);
        }

        fileChooser.setTitle("Choose scan set");
        File f = fileChooser.showDialog(stage);
        if ( current == null || (f != null  && !f.getPath().equals(current.getPath())))
        {
            Path tpath = f.toPath();

            sc.close();
            sc = new SC();
            if ( !sc.load(f.getPath()) ) {
                l.severe("Unable to load scan set");
                statusSearch.setText("Search: Failed to load set");
            }
            scanTAB.setDisable(true);
            checkSC = true;

            ViewSettings.searchset = f.getPath();
            ViewSettings.save();

            scanSetTF.setText(ViewSettings.searchset);
        }
    }

    @FXML void onByRating(ActionEvent event)
    {
        ScanSet.SSSearch.SSSearchRatingTask tt = srch.createRatingSearch();
        if (srch.startSearch(tt))
            currentSearch = tt;
        else
            Utils.whoops("Search Engine Error", "Refused our search", "the search engine was not ready to do a search");
    }

    @FXML void onChangeTopDir(ActionEvent event)
    {
        File current = new File(ViewSettings.top);
        DirectoryChooser fileChooser = new DirectoryChooser();
        if ( current.exists() )
            fileChooser.setInitialDirectory(current);

        fileChooser.setTitle("Choose top level dir");

        File f = fileChooser.showDialog(stage);
        if ( f != null && !f.getPath().equals(current.getPath()))
        {
            ViewSettings.top = f.getPath();
            ViewSettings.save();
            Path tpath = f.toPath();
            topDirTF.setText("Loading...");
            TreeItem<File> root = createNode(tpath.toFile());
            dirTree.setRoot(root);
            topDirTF.setText(ViewSettings.top);
        }
    }

    private final static SLog l = SLog.GetL();
    public Object slock = new Object();
    private DirLoadEngine loadEngine = null;
    private ScanSet.SSSearch srch = null;
    private boolean iconSizeChanged = false;
    private Stage stage = null;
    private SC sc = new SC();
    private int selectedIconViewIndex = -1;
    private ObservableList<ScanSet.SSFile> instancesItems = FXCollections.observableArrayList ();
//    private List<ScanSet.SSImage> instanceItems = new LinkedList<>();

    private ObservableList<String> smodes = FXCollections.observableArrayList ();
    private DHistory dhistory = new DHistory(5);
    public enum SortBy { Name, ModificationDate, ModificationDate_Reverse };

    public class IFileCell extends ListCell<ScanSet.SSFile>
    {
        @Override
        protected void updateItem(ScanSet.SSFile item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else
            {
                setText(item.GetName());
                Tooltip.install(this, new Tooltip(item.GetDiskPath()));
                final ContextMenu cCm = new ContextMenu();
                MenuItem cItem = new MenuItem("Show " + item.GetName());
                cCm.getItems().add(cItem);
                //if ( Utils.isWindows() )
                //{
                    MenuItem cItem2 = new MenuItem("Select in tree");
                    cCm.getItems().add(cItem2);
                    cItem2.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            File f = new File(item.GetDiskPath());
                            loadADirIntoIconView(f.toPath().getParent().toFile(),true);
                            //navigateTo(new File( SC.gets().GetDirPath(item)));
                        }
                    });
               // }
                cItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        List<File> files = new LinkedList<File>();
                        files.add(new File( item.GetDiskPath()));
                        popup(files,0);
                    }
                });
                setContextMenu(cCm);

            }
        }
    }

    private void navigateTo(File f)
    {
        Path p = f.toPath();
        int parts = p.getNameCount();
        TreeItem<File> leaf = dirTree.getRoot();

        l.warn("NAV: GOTO: %s  ROOT: %s", f.toString(), leaf.getValue().toString() );

        if ( !f.toPath().startsWith(leaf.getValue().toPath()))
        {
            Utils.whoops("Not in tree","","");
            return;
        }

        leaf.setExpanded(true);
        boolean found = true;
        while(found)
        {
            found = false;
            l.warn("NAV: INTREE: AT:%s", leaf.toString() );
            for (TreeItem<File> c : leaf.getChildren())
            {
                if (p.startsWith(c.getValue().toPath()))
                {
                    l.warn("NAV: Found leaf at %s", c.getValue().toString());
                    leaf = c;
                    found = true;
                    c.setExpanded(true);
                    break;
                }
            }
            if ( found )
                dirTree.getSelectionModel().select(leaf);
            else
                l.warn("NAV: No more nodes");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {

        ViewSettings.init();
        scanTAB.setDisable(true);
        rt = Runtime.getRuntime();
        Path tpath = Paths.get(ViewSettings.top);
        topDirTF.setText(ViewSettings.top);

        if (!Files.exists(tpath))
        {
            l.severe("Top dir " + tpath.toString() + " dosnt exist");
            tpath = Paths.get(System.getProperty("user.home"));
            topDirTF.setText(tpath.toString());
            ViewSettings.save();
        }
        TreeItem<File> root = createNode(tpath.toFile());
        dirTree.setRoot(root);
        dirTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dirTree.getSelectionModel().selectedItemProperty().addListener(new OnDirTreeSelection());
        dirTree.setCellFactory(tv ->  new TreeCell<File>() {
            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                {
                    setText(null);
                }
                else
                {
                    setText(item.getName()); // appropriate text for item
                }
            }
        });

        instancesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);




        instancesList.setCellFactory(new Callback<ListView<ScanSet.SSFile>, ListCell<ScanSet.SSFile>>() {
            @Override
            public ListCell<ScanSet.SSFile> call(ListView<ScanSet.SSFile> p) {
                return new IFileCell();
            }
        });


        dirSortCB.setItems(FXCollections.observableArrayList("Name", "Modification Date","Modification Date Reverse", "Size", "Size Reverse"));
//        dirSortCB.getSelectionModel().select("Name");
        dirSortCB.setValue(ViewSettings.dirsort);
        System.out.println("XX Init Setpull to=" + ViewSettings.dirsort);

        dirSortCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue ov, Number value, Number new_value)
            {
                onNewDirSort(new_value.intValue());
            }
        });
        //instancesList.getSelectionModel().selectedItemProperty().addListener(new OnInstanceItemSelection());
/*
        final ContextMenu cCm = new ContextMenu();
        MenuItem cItem = new MenuItem("Show in File View");
        cItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Utils.whoops("Selected Item Action", "In File View","");
            }
        });
        cCm.getItems().add(cItem);

        if ( Utils.isWindows() )
        {
            MenuItem cItem2 = new MenuItem("Show in Explorer");
            cItem2.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    instancesList.getSelectionModel().selectedItemProperty().toString();
                    //Utils.OpenInExplorer(new_val);
                }
            });
            cCm.getItems().add(cItem2);
        }
        instancesList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    //its OK here:
                    System.out.println(instancesList.getSelectionModel().getSelectedItem().toString());
                    cCm.show(instancesList, e.getScreenX(), e.getScreenY());
                }
            }
        });
*/


        smodes.addAll("Simple", "Luma", "Mono");
        scanTypeCB.setItems(smodes);
        scanTypeCB.setValue("Luma");
        scanTypeCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                switch((int)newValue)
                {
                    case 0: ViewSettings.scanType = ScanSet.SSScanType.Simple; break;
                    case 1: ViewSettings.scanType = ScanSet.SSScanType.Luma; break;
                    case 2: ViewSettings.scanType = ScanSet.SSScanType.Mono; break;
                }
            }
        });

        iconPane.setVgap((double) ViewSettings.tpad);
        iconPane.setHgap((double) ViewSettings.tpad);
        iconScroll.widthProperty().addListener(new ResizeListener());
        iconScroll.heightProperty().addListener(new ResizeListener());

        iconPane.setOnKeyPressed(new OnIconPaneKeyPress());

        TableColumn nameColumn = new TableColumn("Name");
        nameColumn.setMinWidth(200);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tsearchLV.setItems(tsearchResults);
        tsearchLV.getColumns().addAll(nameColumn);
        tsearchLV.getSelectionModel().selectedIndexProperty().addListener(new OnSelectionOfAnItemInSearchList());

        statusSearch.setText("Database: Loading.");
        statusSearch.setTextFill(Paint.valueOf("black"));
        scanSetTF.setText(ViewSettings.searchset);
        if ( !sc.load(ViewSettings.searchset) ) {
            l.severe("Unable to load image database.");
            statusSearch.setText("Database: Failed to load.");
            statusSearch.setTextFill(Paint.valueOf("red"));
        }

        dirCB.setSelected(true);
        filesCB.setSelected(true);

        Timeline tmr = new Timeline(new KeyFrame(Duration.millis(100), ae -> onTimer()));
        tmr.setCycleCount(Animation.INDEFINITE);
        tmr.play();

        instancesList.setItems(instancesItems);

        topvbox.requestLayout();

    }


    private void onNewDirSort(int i)
    {
        switch(i)
        {
            case 0:  ViewSettings.dirsort = "Name"; break;
            case 1:  ViewSettings.dirsort = "Modification Date"; break;
            case 2:  ViewSettings.dirsort = "Modification Date Reverse"; break;
            case 3:  ViewSettings.dirsort = "Size"; break;
            case 4:  ViewSettings.dirsort = "Size Reverse"; break;
        }
        ViewSettings.save();
        System.out.println("XX New selection " + ViewSettings.dirsort);
    }

    public void initializeLater(Scene myScene, Stage stage)
    {
//        Image appImg = new Image(getClass().getResourceAsStream("file:pics_1.ico"));
        Image appImg = new Image(getClass().getResourceAsStream("appt.png"));

        this.stage = stage;
        stage.getIcons().add(appImg);
        //myScene.setOnKeyPressed(new OnSceneKeyPress());
        myScene.addEventFilter(KeyEvent.KEY_PRESSED, new OnSceneKeyFilter());
    }

 

    static int firstTimer = 0;
    private Runtime rt;

    public void onTimer()
    {
        if ( firstTimer++ < 2 )
        {
            return;
        }
        long now = System.currentTimeMillis();

        if ( firstTimer % 10 == 0 )
        {
            long endfree = rt.freeMemory()/1000/1000;
            statusMem.setText(String.format("Mem Free: %d MB.", endfree));
            if ( endfree < 100 )
                statusMem.setTextFill(Paint.valueOf("red"));
            else
                statusMem.setTextFill(Paint.valueOf("green"));
        }
        try
        {
            if (iconSizeChanged)
            {
                int cols = (int) (iconScroll.getWidth() / (float) (tndispsize + ViewSettings.tpad));
                iconPane.setMaxWidth(iconScroll.getWidth() - 10F);
                iconPane.setPrefColumns(cols);
                //l.warn("Icon spacing at " + cols + " image size " + imageAnchor.getWidth() + "," + imageAnchor.getHeight());
                imageView.setFitWidth(imageAnchor.getWidth());
                imageView.setFitHeight(imageAnchor.getHeight());
                iconSizeChanged = false;
            }
        }
        catch (Exception e)
        {
            l.warn("OnTimer:");
            e.printStackTrace();
        }

        if (loadEngine != null)
        {
            loadEngine.loadPendingResults();
        }

        checkIfLoadIsFinishedAndEnableTab();

        if ( srch != null && currentSearch != null )
        {
            checkStateOfActiveSearch();
        }

    }


    private class ResizeListener implements ChangeListener<Number>
    {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
        {
            iconSizeChanged = true;
        }
    }

    private class OnDirTreeSelection implements ChangeListener
    {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue)
        {
            if ( newValue == null )
                return;
            TreeItem<File> selectedItem = (TreeItem<File>) newValue;
            l.info("Main - Select dir tree leaf: " + selectedItem.getValue().getPath());
            loadADirIntoIconView(selectedItem.getValue(),true);
        }
    }


/*
    private class OnInstanceItemSelection implements ChangeListener<String>
    {
        @Override
        public void changed(ObservableValue<? extends String> ov,  String old_val, String new_val)
        {
            Utils.OpenInExplorer(new_val);
        }
    }
*/


    private class DHistory
    {
        public DHistory(int size)
        {
            entries = size;
        }

        List<DirViewHistoryItem> dhistory = new LinkedList<>();
        int dhitem = 0;
        int entries;

        void Add(File f)
        {
            DirViewHistoryItem fi = new DirViewHistoryItem(f);
            dhistory.add(fi);
            trim();
            dhitem = dhistory.size();
            l.warn("DHISTORY - add dir, size now %d pos %d", dhistory.size(), dhitem );
        }

        void trim()
        {
            if (dhistory.size() > entries)
            {
                dhistory.remove(0);
                l.warn("DHISTORY - trim to %d", dhistory.size());
            }
        }

        void Add(ScanSet.SSSearch.SSSearchImageTask f)
        {
            DirViewHistoryItem fi = new DirViewHistoryItem(f);
            dhistory.add(fi);
            trim();
            dhitem = dhistory.size();
            l.warn("DHISTORY - add imsearch, size now %d pos %d", dhistory.size(), dhitem );
        }

        void Add(ScanSet.SSSearch.SSSearchRatingTask f)
        {
            DirViewHistoryItem fi = new DirViewHistoryItem(f);
            dhistory.add(fi);
            trim();
            dhitem = dhistory.size();
            l.warn("DHISTORY - add imsearch, size now %d pos %d", dhistory.size(), dhitem );
        }

        void Add(TextSearchItems f)
        {
            DirViewHistoryItem fi = new DirViewHistoryItem(f);
            dhistory.add(fi);
            trim();
            dhitem = dhistory.size();
            l.warn("DHISTORY - add txt srch, size now %d pos %d", dhistory.size(), dhitem );
        }

        public void Fwd()
        {
            if ( dhitem >= dhistory.size() )
            {
                l.warn("DHISTORY Can't FWD, at end %d , %d", dhitem, entries);
                return;
            }
            dhitem++;
            l.warn("DHISTORY FWD to %d", dhitem);
            seklect();
        }

        public void Back()
        {
            if ( dhitem <= 1 )
            {
                l.warn("DHISTORY Can't BAK, at start %d ", dhitem);
                return;
            }
            dhitem--;
            l.warn("DHISTORY BAK to %d ", dhitem);
            seklect();
        }

        private void seklect()
        {
            DirViewHistoryItem dh = dhistory.get(dhitem-1);
            if ( dh.dir != null )
                loadADirIntoIconView(dh.dir, false);
            else if ( dh.txt != null )
                loadTheResultsOfATextSearchIntoIconView(dh.txt, false);
            else if ( dh.rat != null )
            {
                currentSearch = dh.img;
                loadTheResultsOfARatingSearchIntoIconView(false);
            }
            else if ( dh.img != null )
            {
                currentSearch = dh.img;
                loadTheResultsOfAnImageSearchIntoIconView(false);
            }
        }
    }

    private class DirViewHistoryItem
    {
        public DirViewHistoryItem(File d)
        {
            l.warn("DHIST: Add Dir");
            dir = d;
        }

        public DirViewHistoryItem(ScanSet.SSSearch.SSSearchRatingTask d)
        {
            l.warn("DHIST: Add ScanSet.SSSearch");
            rat = d;
        }
        public DirViewHistoryItem(ScanSet.SSSearch.SSSearchImageTask d)
        {
            l.warn("DHIST: Add ScanSet.SSSearch");
            img = d;
        }
        public DirViewHistoryItem(TextSearchItems d)
        {
            l.warn("DHIST: Add ScanSet.SSSearch");
            txt = d;
        }

        public File dir = null;
        public ScanSet.SSSearch.SSSearchImageTask img = null;
        public ScanSet.SSSearch.SSSearchRatingTask rat = null;
        public TextSearchItems txt = null;
    }


    private void loadADirIntoIconView(File dir, boolean store)
    {
        if ( store )
            dhistory.Add(dir);

        l.info("Main - loadADirIntoIconView: " + dir.getPath().toString() );
        closeInstances();
        try
        {
            if (dir == null)
            {
                l.warn("loadADirIntoIconView - dir passed to me was null");
                return;
            }
            if (!dir.isDirectory())
            {
                l.warn("loadADirIntoIconView - dir passed to me was not a dir");
                return;
            }

            if (loadEngine != null)
                loadEngine.Stop();

            listLabel.setText("Dir:" + dir.getPath());
            Tooltip.install(listLabel, new Tooltip(listLabel.getText()));

            l.info("Main - loadADirIntoIconView, create direngine for: " + dir.getPath().toString() );
            loadEngine = new DirLoadEngine(dir);
            l.info("Main - loadADirIntoIconView, direngine returned : " + loadEngine.ilist + " initial items for " + dir.getPath().toString() );
            iconPane.getChildren().clear();
            for (ImageView v : loadEngine.ilist)
            {

                v.setOnMouseClicked(new OnIconImageViewMouseClicked(v));
                v.setOnContextMenuRequested(new OnIconImageViewContextMenuRequested(v));
            }
            iconPane.getChildren().addAll(loadEngine.ilist);
            iconPane.requestFocus();//setOnKeyPressed(new OnIconPaneKeyPress());
        }
        catch (Exception e)
        {
            l.warn("Ex3:" + e.toString());
            e.printStackTrace();
        }
    }

    private void loadImageIntoImagePane(String path) throws ImException
    {
        ScanSet.SSCRC crc = ScanSet.GetCRCAndBytes(path);
        InputStream inputStream = null;

        inputStream  = new ByteArrayInputStream(crc.bytes); //FileInputStream(path);

        Image pi = new Image(inputStream);

        imageView.setImage(pi);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(imageAnchor.getWidth());
        imageView.setFitHeight(imageAnchor.getHeight()-50);

        ScanSet.SSImage i = SC.gets().GetImage(crc.crc);

        String ff =String.format("%s  %d x %d  %s ( %s )", path,  (int)pi.getWidth(), (int)pi.getHeight(), humanReadableByteCount(crc.bytes.length,true),
                ( i == null ?  "n/a" : Integer.toString(i.GetRating()) ) );

        imageLabel.setText(ff );
        imageDim.setText(path);

        Tooltip.install(imageLabel, new Tooltip(imageLabel.getText()));
    }

    private void onIconViewImageSingleClicked(ImageView v)
    {
        selectedIconViewIndex = iconPane.getChildren().indexOf(v);
        l.warn("IV selected itemix=" + selectedIconViewIndex);
        if ( v.getUserData().getClass() == DirLoadEngine.DirLoadTag.class ) {
            closeInstances();
            try {
                DirLoadEngine.DirLoadTag t = (DirLoadEngine.DirLoadTag) v.getUserData();
                //String path = Utils.PathToUri(t.file);
                //l.warn("MAIN-Load dir image: " + path);
                loadImageIntoImagePane(t.file.toString());
            }
            catch (ImException e) {
                l.warn("Error displayinng selected image: " + e.toString());
            }
            catch (Exception e) {
                l.warn("Error displayinng selected image: " + e.toString());
            }
        }
        else if ( v.getUserData().getClass() == ScanSet.SSFile.class )
        {
            closeInstances();
            try {
                ScanSet.SSFile imageFile = (ScanSet.SSFile) v.getUserData();
                //String path = imageFile.GetUrlPath();
               // l.warn("MAIN-Load ifile image: " + path);
                loadImageIntoImagePane(imageFile.GetDiskPath());
            }
            catch (Exception e)
            {
                l.warn("Error displayinng selected image: " + e.toString());
            }
            catch (ImException e)
            {
                l.warn("Error displayinng selected image: " + e.toString());
            }
        }
        else if ( v.getUserData().getClass() == ScanSet.SSImage.class ) {
            openInstances();
                try {
                    ScanSet.SSImage imgFile = (ScanSet.SSImage) v.getUserData();

                    loadIImageInstancesIntoInstanceList(imgFile);


                    List<ScanSet.SSFile> files = SC.gets().GetSSFilesForCrc(imgFile.crc);
                    if ( files.size() > 0 )
                    {
                        //String path = files.get(0).GetUrlPath();
                        //l.warn("MAIN-Load iimage image: " + path);
                        loadImageIntoImagePane(files.get(0).GetDiskPath());
                    }
                } catch (Exception e) {
                    l.warn("Error displayinng selected image: " + e.toString());
                }
                catch (ImException e)
                {
                    l.warn("Error displayinng selected image: " + e.toString());
                }
            }
            iconPane.requestFocus();
    }

    private void onIconViewImageDoubleClicked(ImageView v)
    {
        List<File> files = new LinkedList<File>();

        int ix=0;
        int fix = 0;
        for(Object n : iconPane.getChildren())
        {
            if ( n.getClass() != ImageView.class )
                continue;

            ImageView iv = (ImageView)n;
            Object uc = iv.getUserData();

            if ( uc.getClass() == DirLoadEngine.DirLoadTag.class )
            {
                files.add(((DirLoadEngine.DirLoadTag)uc).file);
            }
            else if ( uc.getClass() == ScanSet.SSFile.class )
            {
                files.add(new File( ((ScanSet.SSFile)uc).GetDiskPath() ));
            }
            else  if ( uc.getClass() == ScanSet.SSImage.class )
            {
                ScanSet.SSImage imgFile = (ScanSet.SSImage)uc;
                List<ScanSet.SSFile> instanceList =  SC.gets().GetSSFilesForCrc(imgFile.crc);
                if ( instanceList.size() > 0 )
                    files.add(new File( instanceList.get(0).GetDiskPath()));
            }

            if ( iv.equals(v) )
                fix = ix;
            ix++;

        }
        popup(files, fix);

        /*
        try
        {
            selectedIconViewIndex = iconPane.getChildren().indexOf(v);
            l.warn("IV selected itemix=" + selectedIconViewIndex);
            if ( v.getUserData().getClass() == DirLoadEngine.DirLoadTag.class )
            {
                DirLoadEngine.DirLoadTag t = (DirLoadEngine.DirLoadTag) v.getUserData();
                popup(t.file);
            }
            else if ( v.getUserData().getClass() == ScanSet.SSFile.class ) {
                ScanSet.SSFile imageFile = (ScanSet.SSFile) v.getUserData();
                String sfile = SC.gets().GetDirPath(imageFile);
                File f = new File(sfile);
                popup(f);
            }
            else if ( v.getUserData().getClass() == ScanSet.SSImage.class ) {
                    try {
                        ScanSet.SSImage imgFile = (ScanSet.SSImage) v.getUserData();

                        List<ScanSet.SSFile> xfiles = SC.gets().GetIFilesForCrc(imgFile.crc, true);
                        if ( xfiles.size() > 0 )
                        {
                            String sfile = SC.gets().GetDirPath(xfiles.get(0));
                            File f = new File(sfile);
                            popup(f);
                        }
                    } catch (Exception e) {
                        l.warn("Error displayinng selected image: " + e.toString());
                    }
                }
            iconPane.requestFocus();
        }
        catch (Exception e)
        {
            l.warn("Error displayinng selected image: " + e.toString());
        }*/
    }

    private void onRate(ImageView v, int lvl)
    {
            Object uc = v.getUserData();

            if ( uc.getClass() == DirLoadEngine.DirLoadTag.class )
            {
                File f = ((DirLoadEngine.DirLoadTag)uc).file;
                l.warn("Rate %s %d", f.getName() , lvl);
                SC.gets().Rate(f, lvl);
            }
            else if ( uc.getClass() == ScanSet.SSFile.class )
            {
                ScanSet.SSFile ifi = (ScanSet.SSFile)uc;
                l.warn("Rate %s %d", ifi.GetName() , lvl);
                SC.gets().Rate(ifi, lvl);
            }
            else  if ( uc.getClass() == ScanSet.SSImage.class )
            {
                ScanSet.SSImage ifi = (ScanSet.SSImage)uc;
                l.warn("Rate %s %d", ifi.toString() , lvl);
                SC.gets().Rate(ifi, lvl);
            }

    }


    private TreeItem<File> createNode(final File f)
    {
        return new TreeItem<File>(f)
        {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public String toString()
            {
                return f.getName();
            }

            @Override
            public ObservableList<TreeItem<File>> getChildren()
            {
                if (isFirstTimeChildren)
                {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf()
            {
                if (isFirstTimeLeaf)
                {
                    isFirstTimeLeaf = false;
                    File f = (File) getValue();
                    isLeaf = f.isFile();
                }

                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem)
            {
                File f = TreeItem.getValue();
                //TreeItem.super.setText("fred");
                if (f != null && f.isDirectory())
                {
                    File[] files = f.listFiles();

                    boolean hasSubdir = false;
                    for (File childFile : files)
                    {
                        if (childFile.isDirectory())
                        {
                            hasSubdir = true;
                            break;
                        }
                    }

                    if (files != null && hasSubdir)
                    {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();

                        for (File childFile : files)
                        {
                            if (childFile.isDirectory())
                                children.add(createNode(childFile));
                        }

                        return children;
                    }
                    else
                        isLeaf = true;
                }

                return FXCollections.emptyObservableList();
            }
        };
    }


   public ScanSet.SSSearch.SSSearchTask currentSearch;

    private void makeCurrent(ScanSet.SSSearch.SSSearchTask load)
    {
        currentSearch = load;
    }


    public void srchDoneWithCurrent()
    {
        currentSearch.retire();
        statusSearch.setText("Search: Not Active: " + currentSearch.statusStr );
        currentSearch = null;
        l.warn("MAIN SRCH Ended.");
    }

    private boolean checkSC = true;

    private void checkIfLoadIsFinishedAndEnableTab()
    {
        if ( !checkSC )
            return;

        if ( sc.error )
        {
            checkSC = false;
            Utils.whoops("Scan Set", null, "Error in the scanset load");
        }

        if (sc.ready && scanTAB.isDisabled())
        {
            statusSearch.setText("Database: " + SC.gets().GetName() );
            statusSearch.setTextFill(Paint.valueOf("green"));
            scanTAB.setDisable(false);
            checkSC = false;

            srch = SC.gets().CreateSearch();
        }
    }

    private void checkStateOfActiveSearch()
    {


        if (currentSearch != null)
        {
            if (currentSearch.getClass() == ScanSet.SSSearch.SSSearchTextTask.class)
            {
                l.debug("MAIN TIMR monitor SETXT(" + currentSearch.taskNum + ")");
                statusSearch.setText("Search: Underway: " + currentSearch.statusStr );

                ScanSet.SSSearch.SSSearchTextTask st = (ScanSet.SSSearch.SSSearchTextTask) currentSearch;
                //l.warn("JFX - its the load task");
                if (st.state == ScanSet.SSState.Finished)
                {
                    l.warn("MAIN TIMR SETXT(" + currentSearch.taskNum + ") is finished, load results");
                    statusSearch.setText("Search: Loading Result: " + currentSearch.statusStr );
                    OnHandleResultsOfATextSearch();
                    l.info("MAIN TIMR SETXT(" + currentSearch.taskNum + ") done with");
                    srchDoneWithCurrent();
                }
                return;
            }
            if (currentSearch.getClass() == ScanSet.SSSearch.SSSearchImageTask.class)
            {
                statusSearch.setText("Search: Underway:" + currentSearch.statusStr );
                l.debug("MAIN TIMR monitor SEIMG(" + currentSearch.taskNum + ")");
                if (currentSearch.state == ScanSet.SSState.Finished)
                {
                    l.warn("MAIN TIMR SEIMG(" + currentSearch.taskNum + ") is finished, load results");
                    statusSearch.setText("Search: Loading Result: " + currentSearch.statusStr );
                    loadTheResultsOfAnImageSearchIntoIconView(true);
                }
            }
            if (currentSearch.getClass() == ScanSet.SSSearch.SSSearchRatingTask.class)
            {
                statusSearch.setText("Search: Underway:" + currentSearch.statusStr );
                if (currentSearch.state == ScanSet.SSState.Finished)
                {
                    l.warn("MAIN TIMR SERATE(" + currentSearch.taskNum + ") is finished, load results");
                    statusSearch.setText("Search: Loading Result: " + currentSearch.statusStr );
                    loadTheResultsOfARatingSearchIntoIconView(true);
                }
            }

        }

    }
    private ObservableList<TextSearchItems> tsearchResults = FXCollections.observableArrayList();

    private void OnHandleResultsOfATextSearch()
    {
        tsearchResults.clear();
        ScanSet.SSSearch.SSSearchTextTask load = (ScanSet.SSSearch.SSSearchTextTask) currentSearch;

        if (!load.results.isEmpty())
        {
            synchronized (load.results)
            {
                List<ScanSet.SSFile> files = new LinkedList<ScanSet.SSFile>();

                for(ScanSet.SSSearch.SSSearchResult r : load.results)
                {
                    if ( r.getClass() == ScanSet.SSSearch.SSTextDirSearchResult.class)
                    {
                        tsearchResults.add(new TextSearchItems( ((ScanSet.SSSearch.SSTextDirSearchResult)r).dir));
                    }
                    if ( r.getClass() == ScanSet.SSSearch.SSTextFileSearchResult.class)
                    {
                        files.add( ((ScanSet.SSSearch.SSTextFileSearchResult)r).file);
                    }
                }
                if ( files.size() > 0 )
                tsearchResults.add(new TextSearchItems(files));
            }
        }

    }

    private class OnContextShowItemAssociatedWithImageView implements EventHandler<ActionEvent>
    {
        OnContextShowItemAssociatedWithImageView(ImageView v)
        {
            imageView = v;
        }

        ImageView imageView=null;

        @Override
        public void handle(ActionEvent event)
        {
            //if ( contextItem.getClass() == ImageView.class )
                onIconViewImageDoubleClicked(imageView);
            event.consume();
        }
    }

    private class OnCtxRate implements EventHandler<ActionEvent>
    {
        OnCtxRate(ImageView v, int l)
        {
            imageView = v;
            level = l;
        }

        ImageView imageView=null;
        int level = 0;

        @Override
        public void handle(ActionEvent event)
        {
            onRate(imageView,level);
            event.consume();
        }
    }




    List<PopupImage> displayed = new LinkedList<PopupImage>();

    private void addPopup(PopupImage pi)
    {
        displayed.add(pi);
    }
    private void delPopup(PopupImage pi)
    {
        displayed.remove(pi);
    }

    private class OnPopupImageContextAction implements EventHandler<ActionEvent>
    {
        int act = -1;
        PopupImage pi = null;
        public OnPopupImageContextAction(PopupImage pi, int act)
        {
            this.act = act;
            this.pi = pi;
        }

        @Override
        public void handle(ActionEvent event)
        {
            switch(act)
            {
                case 0:
                    pi.Close();
                    break;

                case 1:
                    for(PopupImage i : displayed)
                    {
                        i.ToFront();
                    }
                    break;

                case 2:
                    for(PopupImage i : displayed)
                    {
                        i.Close();
                    }
                    break;

                case 3:
                    jfxview.get().stage.toFront();
                case 4:
                    pi.Full();
            }
            l.warn("Action " + act);
            event.consume();
        }
    }

    private void popup(List<File> files, int pos)
    {

        PopupImage pi = new PopupImage(jfxview.get().stage, files,  pos);

        ContextMenu cc = new ContextMenu();
        javafx.scene.control.MenuItem sh0 = new javafx.scene.control.MenuItem("Close");
        javafx.scene.control.MenuItem sh4 = new javafx.scene.control.MenuItem("Full");
        javafx.scene.control.MenuItem sh1 = new javafx.scene.control.MenuItem("Show All");
        javafx.scene.control.MenuItem sh2 = new javafx.scene.control.MenuItem("Close All");
        javafx.scene.control.MenuItem sh3 = new javafx.scene.control.MenuItem("Hide All");
        sh0.setOnAction(new OnPopupImageContextAction(pi, 0));
        sh4.setOnAction(new OnPopupImageContextAction(pi, 4));
        sh1.setOnAction(new OnPopupImageContextAction(pi, 1));
        sh2.setOnAction(new OnPopupImageContextAction(pi, 2));
        sh3.setOnAction(new OnPopupImageContextAction(pi, 3));
        cc.getItems().addAll(sh0, sh4, sh1, sh2, sh3);

        Stage dialog = pi.display(cc);
        dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we)
            {
                delPopup(pi);
            }
        });

        dialog.show();
        addPopup(pi);
    }

    private class OnCtxOpenFolder implements EventHandler<ActionEvent> {
        ImageView imageView = null;

        public OnCtxOpenFolder(ImageView v) {
            imageView = v;
        }

        @Override
        public void handle(ActionEvent event)
        {
            String path = "";
            try {
                if (imageView.getUserData().getClass() == DirLoadEngine.DirLoadTag.class) {
                    DirLoadEngine.DirLoadTag t = (DirLoadEngine.DirLoadTag) imageView.getUserData();
                    path = t.file.getPath();
                } else if (imageView.getUserData().getClass() == ScanSet.SSFile.class) {
                    ScanSet.SSFile imageFile = (ScanSet.SSFile) imageView.getUserData();
                    path = imageFile.GetDiskPath();
                }
            }
            catch (Exception e)
            {
                l.warn("Error displayinng selected image: " + e.toString());
            }
            event.consume();
            if ( path.length() > 0 )
                Utils.OpenInExplorer(path);
        }
    }

    private class OnCtxFindSimilar implements EventHandler<ActionEvent>
    {
        ImageView imageView=null;

        public OnCtxFindSimilar(ImageView v)
        {
            imageView = v;
        }
        @Override
        public void handle(ActionEvent event)
        {
            try
            {
                if ( imageView.getUserData().getClass() == DirLoadEngine.DirLoadTag.class )
                {
                    DirLoadEngine.DirLoadTag t = (DirLoadEngine.DirLoadTag) imageView.getUserData();
                    l.info("MAIN CTX FSIM: imageView is a DirLoadTag, file is " + t.file.getName() );
                    startAnImageSearchForAFile(t.file);
                }
                else if ( imageView.getUserData().getClass() == ScanSet.SSFile.class ) {
                    ScanSet.SSFile imageFile = (ScanSet.SSFile) imageView.getUserData();
                    String sfile = imageFile.GetDiskPath();
                    File f = new File(sfile);
                    l.info("MAIN CTX FSIM: imageView is a IFile, file is " + f.getName() );
                    startAnImageSearchForAFile(f);
                }
            }
            catch (Exception e)
            {
                l.warn("Error displayinng selected image: " + e.toString());
            }
            event.consume();
        }
    }

    private void startAnImageSearchForAFile(File f)
    {
        l.warn("MAIN SFSIM: create SE for " + f.getName() + ", type=" + ViewSettings.scanType );

        ScanSet.SSSearch.SSSearchImageTask tt = srch.createImageSearch();
        tt.setImageSearch(ViewSettings.scanType, f.getPath());
        if (srch.startSearch(tt))
            currentSearch = tt;
        else
            Utils.whoops("Warning", "SearchEngine refused our search", "bummer");
    }

    private class OnSelectionOfAnItemInSearchList implements ChangeListener
    {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue)
        {
            int ix = ((Number) newValue).intValue();
            if ((ix >= tsearchResults.size()))
            {
                return; // invalid data
            }
            TextSearchItems c = tsearchResults.get(ix);
            loadTheResultsOfATextSearchIntoIconView(c,true);
        }
    }

    private ImageView createAnImageViewForAnIFile(final ScanSet.SSFile f)
    {
        Image thumb =  f.GetThumbnail();

        final ImageView imageView =  new ImageView(thumb);
        imageView.setFitWidth(tndispsize);
        imageView.setFitHeight(tndispsize);
        imageView.setUserData(f);

        imageView.setOnContextMenuRequested(new OnIconImageViewContextMenuRequested(imageView));
        imageView.setOnMouseClicked(new OnIconImageViewMouseClicked(imageView));

        return imageView;
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
            }
        }
    }

    private class OnIconPaneKeyPress implements EventHandler<KeyEvent>
    {
        public void handle(KeyEvent kev)
        {

            ImageView v = null;
            int cwid = iconPane.getPrefColumns();
            l.warn("IP - Key:" + kev.getCode().toString() + ", Wid:" + cwid );

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
                        l.warn("MoveRIGHT, select " + (selectedIconViewIndex + 1));
                    }
                    break;

                case DOWN:
                    if ( selectedIconViewIndex+cwid < iconPane.getChildren().size()  )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex + cwid);
                        l.warn("MoveDOWN, select " + (selectedIconViewIndex + 1));
                    }
                    break;

                case LEFT:
                    if ( selectedIconViewIndex > 0 && iconPane.getChildren().size() > 0 )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex - 1);
                        l.warn("MoveLEFT, select " + (selectedIconViewIndex - 1));
                    }
                    break;

                case UP:
                    if ( selectedIconViewIndex-cwid >= 0  )
                    {
                        v = (ImageView) iconPane.getChildren().get(selectedIconViewIndex - cwid);
                        l.warn("MoveUP, select " + (selectedIconViewIndex - cwid));
                    }
                    break;

            }
            if ( v != null )
                onIconViewImageSingleClicked(v);

        }
    }

    private class OnIconImageViewContextMenuRequested implements EventHandler<ContextMenuEvent>
    {
        ImageView imageView = null;
        OnIconImageViewContextMenuRequested(ImageView v)
        {
            imageView = v;
        }
        @Override
        public void handle(ContextMenuEvent event)
        {

            if ( imageView.getUserData().getClass() == ScanSet.SSImage.class )
            {
                ContextMenu cc = new ContextMenu();
                ScanSet.SSImage img = (ScanSet.SSImage)imageView.getUserData();
                List<ScanSet.SSFile> files = SC.gets().GetSSFilesForCrc(img.crc);
                MenuItem sh = new MenuItem("Show");
                sh.setOnAction(new OnContextShowItemAssociatedWithImageView(imageView));
                cc.getItems().addAll(sh);
                cc.show(imageView, event.getScreenX(), event.getScreenY());
            }
            else
            {
                ContextMenu cc = new ContextMenu();
                Menu mt = new Menu("Mark");
                MenuItem mt5 = new MenuItem("*****");
                MenuItem mt4 = new MenuItem("****");
                MenuItem mt3 = new MenuItem("***");
                MenuItem mt2 = new MenuItem("**");
                MenuItem mt1 = new MenuItem("*");
                MenuItem mt0 = new MenuItem("Clear");
                mt5.setOnAction(new OnCtxRate(imageView,5));
                mt4.setOnAction(new OnCtxRate(imageView,4));
                mt3.setOnAction(new OnCtxRate(imageView,3));
                mt2.setOnAction(new OnCtxRate(imageView,2));
                mt1.setOnAction(new OnCtxRate(imageView,1));
                mt0.setOnAction(new OnCtxRate(imageView,0));


                mt.getItems().addAll(mt5,mt4,mt3,mt2,mt1,mt0);
                MenuItem fc = new MenuItem("Find Similar");
                MenuItem sh = new MenuItem("Show");
                MenuItem shf = new MenuItem("Open Dir");
                sh.setOnAction(new OnContextShowItemAssociatedWithImageView(imageView));
                fc.setOnAction(new OnCtxFindSimilar(imageView));
                shf.setOnAction(new OnCtxOpenFolder(imageView));
                cc.getItems().addAll(fc, mt, sh, shf);
                cc.show(imageView, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        }
    };



    private class OnIconImageViewMouseClicked implements EventHandler<MouseEvent>
    {
        ImageView imageView = null;

        OnIconImageViewMouseClicked(ImageView v)
        {
            imageView = v;
        }
        @Override
        public void handle(MouseEvent mev)
        {
            if (mev.getButton().equals(MouseButton.PRIMARY))
            {
                ObservableList<ScanSet.SSFile> selectedItems =  instancesList.getSelectionModel().getSelectedItems();

                for(ScanSet.SSFile s : selectedItems)
                    System.out.println("selected item " + s.toString());


                if (mev.getClickCount() == 1)
                    onIconViewImageSingleClicked(imageView);
                else
                    if (mev.getClickCount() == 2)
                        onIconViewImageDoubleClicked(imageView);
            }
            mev.consume();
            iconPane.requestFocus();
        }
    }

    private void loadTheResultsOfATextSearchIntoIconView(TextSearchItems c, boolean store)
    {
        if ( store )
            dhistory.Add(c);
        ImageView thumbs[];
        listLabel.setText("Text Search");
        Tooltip.install(listLabel, new Tooltip(listLabel.getText()));
        closeInstances();
        int i = 0;
        iconPane.getChildren().clear();
        if (c.getFilelist() != null)
        {
            thumbs = new ImageView[c.getFilelist().size()];
            for (ScanSet.SSFile jf : c.getFilelist())
            {
                thumbs[i] = createAnImageViewForAnIFile(jf);
                iconPane.getChildren().add(thumbs[i++]);
            }
        }

        else
            if (c.getDir() != null)
            {
                String dir = c.getDir().GetDiskPath();// SC.gets().GetDirPath(c.getDir().dhash);
                loadADirIntoIconView(new File(dir),true);
                /*
                List<ScanSet.SSFile> dfiles = new LinkedList<ScanSet.SSFile>();
                for (ScanSet.SSFile jf : SC.gets().getFiles())
                {
                    if (c.getDir().GetHash == jf.GetDirHash())
                    {
                        dfiles.add(jf);
                    }
                }
                thumbs = new ImageView[dfiles.size()];

                for (ScanSet.SSFile jf : dfiles)
                {
                    thumbs[i] = createAnImageViewForAnIFile(jf);
                    iconPane.getChildren().add(thumbs[i++]);
                }*/
            }
    }


    private void loadAnIImageIntoTheIconView(ScanSet.SSImage img, double close)
    {

        try
        {
            Image iimg = img.GetThumbnail();//SC.gets().GetThumbnail(img.crc);
//            ISet.IInstances ii = SC.gets().getInstances().get(img.crc);
            String num = "?";
            List<ScanSet.SSFile> ii = SC.gets().GetSSFilesForCrc(img.crc);

            if ( ii != null )
                num = Integer.toString(ii.size());

            String tt = "Exact: " + num;
            if ( close >= 0 )
                tt = String.format("Closeness %.2f: %s", close, num);
            ImageView v = new ImageView();
            v.setUserData(img);
            v.setFitHeight(ViewSettings.tndispsize);
            v.setFitWidth(ViewSettings.tndispsize);
            v.setImage(iimg);
            l.info("MAIN loadAnIImageIntoTheIconView crc=" + img.crc + ", c=" + close + ", tt=" + tt );

            Tooltip.install(v, new Tooltip(tt));

            v.setOnContextMenuRequested(new OnIconImageViewContextMenuRequested(v));
            v.setOnMouseClicked(new OnIconImageViewMouseClicked(v));

            iconPane.getChildren().add(v);
        }
        catch (Exception e)
        {
            l.warn("Ex4:" + e.toString());
            e.printStackTrace();
        }
    }

    private void openInstances()
    {
        instancesList.setPrefHeight(200);
    }

    private void closeInstances()
    {
        instancesList.setPrefHeight(5);
    }

    private void loadIImageInstancesIntoInstanceList(ScanSet.SSImage imgFile)
    {
        instancesItems.clear();
        List<ScanSet.SSFile> ii = SC.gets().GetSSFilesForCrc(imgFile.crc);
        for (ScanSet.SSFile f : ii )
        {
            instancesItems.add(f);
        }
    }



    private void loadTheResultsOfARatingSearchIntoIconView(boolean store)
    {
        try
        {
            if ( store )
                dhistory.Add((ScanSet.SSSearch.SSSearchRatingTask)currentSearch);
            ScanSet.SSSearch.SSSearchRatingTask results = (ScanSet.SSSearch.SSSearchRatingTask) currentSearch;
            listLabel.setText("Rating Search, found " + results.results.size() + ".");
            Tooltip.install(listLabel, new Tooltip(listLabel.getText()));

            ImageView thumbs[];

            openInstances();

            if ( results.results.size() == 0 )
            {
                srchDoneWithCurrent();
                statusSearch.setText("Search: Nothing Found");
                Utils.whoops("Image Search",null,"Nothing Found");
                return;
            }
            int i = 0;
            iconPane.getChildren().clear();


            for(ScanSet.SSSearch.SSSearchResult r : results.results)
            {
                ScanSet.SSSearch.SSRatingSearchResult ir = (ScanSet.SSSearch.SSRatingSearchResult)r;
                loadAnIImageIntoTheIconView(ir.i, ir.i.GetRating());
            }


            l.warn("MAIN - loadTheResultsOfAnImageSearchIntoIconView, load resuts");
        }
        catch(Exception e)
        {
            l.severe("MAIN - loadTheResultsOfAnImageSearchIntoIconView, problem:" + e.toString());
            e.printStackTrace();
        }

        l.info("MAIN TIMR SEIMG(" + currentSearch.taskNum + ") done with");
        srchDoneWithCurrent();

        return;

    }

    private void loadTheResultsOfAnImageSearchIntoIconView(boolean store)
    {
        try
        {
            if ( store )
                dhistory.Add((ScanSet.SSSearch.SSSearchImageTask)currentSearch);
            ScanSet.SSSearch.SSSearchImageTask results = (ScanSet.SSSearch.SSSearchImageTask) currentSearch;
            listLabel.setText("Image Search for " + results.searchImagePath + ", " + results.results.size() + ".");
            Tooltip.install(listLabel, new Tooltip(listLabel.getText()));

            ImageView thumbs[];

            openInstances();

            if ( results.results.size() == 0 )
            {
                srchDoneWithCurrent();
                statusSearch.setText("Search: Nothing Found");
                Utils.whoops("Image Search",null,"Nothing Found");
                return;
            }
            int i = 0;
            iconPane.getChildren().clear();


            for(ScanSet.SSSearch.SSSearchResult r : results.results)
            {
                ScanSet.SSSearch.SSImageSearchResult ir = (ScanSet.SSSearch.SSImageSearchResult)r;
               // l.info("MAIN - loadResults img=" + ir.image.crc + ", close=" + ir.closeness );
                loadAnIImageIntoTheIconView(ir.image, ir.closeness);
            }


            l.warn("MAIN - loadTheResultsOfAnImageSearchIntoIconView, load resuts");
        }
        catch(Exception e)
        {
            l.severe("MAIN - loadTheResultsOfAnImageSearchIntoIconView, problem:" + e.toString());
            e.printStackTrace();
        }

        l.info("MAIN TIMR SEIMG(" + currentSearch.taskNum + ") done with");
        srchDoneWithCurrent();

        return;

    }



    // todo open dir browser in linux has issues!
    // TODO ratings



    @FXML
    void onImageDragOver(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
        {
            File f = db.getFiles().get(0);
            if (Utils.isImageFile(f.getName()))
            {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                return;
            }
        }
        event.consume();
    }

    @FXML
    void onImageDragEntered(DragEvent event)
    {
        event.consume();
    }

    @FXML
    void onImageDragExited(DragEvent event)
    {
        event.consume();
    }

    @FXML
    void onImageDropped(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles())
        {
            try
            {
                File f = db.getFiles().get(0);
                if (Utils.isImageFile(f.getName()))
                {
                    System.out.println("Got file :" + f.getAbsolutePath());
                    loadFileIntoSearchImageAndSearch(f);
                    success = true;
                }
            }
            catch (Exception e)
            {
                Utils.Fatal(e.getMessage());
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void loadFileIntoSearchImageAndSearch(File f)
    {
        try
        {
            int px = (int) imageAP.getWidth();
            Image image = new Image(f.toURI().toURL().toExternalForm(), px, px, true, true);
            simageIV.setPreserveRatio(false);
            simageIV.setImage(image);

            startAnImageSearchForAFile(f);

        }
        catch (MalformedURLException ex)
        {
            Utils.whoops("Propblem opening " , null,  f.getPath());
        }
    }
}
