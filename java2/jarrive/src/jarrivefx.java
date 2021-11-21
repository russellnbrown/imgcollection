/**
 * Created by russ on 2/10/2016.
 */

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import roz.settools.TxQuery;
import roz.settools.TxQueryReply;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class jarrivefx extends Application
{

    public static boolean connected = false;

    private final static SLog l = SLog.CreateL("jarrive", SLog.SLevel.Low, SLog.SLevel.Info);
    private Pane items;

    private Image errorImg;

    private String scanDir;
    private String scanDir2;
    private String archDir;
    private String keepDir;
    private String specialDir;
    private DirMon dm;
    private DirMon dm2=null;
    private KillMon km;
    private Label btmlabel;
    private int iter=0;
    public boolean oldConnect = false;
    public static Stage myStage;



    public static void main(String[] args)
    {
        launch(args);
    }

    public void jfatal(String msg)
    {
        l.severe(msg);
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
        System.exit(0);

    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.initStyle(StageStyle.UTILITY);

        final Parameters params = getParameters();
        final Map<String,String> parameters = params.getNamed();

        ImageIO.scanForPlugins();
        
        String hd = System.getProperty("user.home");
        try
        {
            Settings.Open(hd + "/settings/jarrive.properties");
        }
        catch (IOException e)
        {
            jfatal("Can't find jarrive.properties in " + hd + "\\settings");
        }

        scanDir = Utils.StandardizePath(Settings.GetStr("scan",""));
        scanDir2 = Utils.StandardizePath(Settings.GetStr("scan2",""));
        archDir = Utils.StandardizePath(Settings.GetStr("archive",""));
        keepDir = Utils.StandardizePath(Settings.GetStr("keep",""));
        specialDir = Utils.StandardizePath(Settings.GetStr("special",""));

        l.info("Scan dir: " + scanDir);
        l.info("Scan dir2: " + scanDir2);
        l.info("Archive dir: " + archDir);
        l.info("Keep dir: " + keepDir);
        l.info("Special dir: " + specialDir);

        makeArch();

        if ( !Files.exists(Paths.get(scanDir)) )
            jfatal("Missing scan dir:" + scanDir );
        if ( scanDir2.length() > 0 && !Files.exists(Paths.get(scanDir2)) )
            jfatal("Missing scan dir2:" + scanDir2 );
        if ( !Files.exists(Paths.get(archDir))  )
            jfatal("Missing archive dir:" + archDir );
        if ( !Files.exists(Paths.get(keepDir))  )
            jfatal("Missing keep dir:" + keepDir );
        if ( !Files.exists(Paths.get(specialDir))  )
            jfatal("Missing special dir:" + specialDir );

        primaryStage.setTitle("Arrive");
        String ss = parameters.get("root");

        // VBox
        VBox vb = new VBox();
        vb.setPadding(new Insets(1, 1, 1, 1));
        vb.setSpacing(1);

        Pane top = makeTop();
        btmlabel = new Label("Bottom");
        btmlabel.setAlignment(Pos.CENTER);

        items = createGrid();

        ScrollPane sp = new ScrollPane();
        sp.setContent(items);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        vb.getChildren().add(top);
        vb.getChildren().add(sp);
        vb.getChildren().add(btmlabel);

        int mScr = 0;//Utils.GetScreenUnderCursor();
        if ( mScr < 0 )
        {
            l.warn("Screen invalid, uding default of 0");
            mScr=0;
        }

        Screen screen = Screen.getScreens().get(mScr);
        int h = (int)screen.getBounds().getHeight();
        int w = (int)screen.getBounds().getWidth();
        int mw = (int)screen.getBounds().getMaxX();
        int my = (int)screen.getBounds().getMinY();

        l.warn("Screen=%d w=%d h=%d mw=%d my=%d", mScr, w, h, mw, my );

        Scene scene = new Scene(vb,132,h - (Utils.isWindows() ? 70 : 40 ) );
        sp.setPrefSize(132, h - (Utils.isWindows() ? 100 : 80 ));

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setX(mw-133);
        primaryStage.setY(my);

        dm = new DirMon(scanDir,null);
        if ( scanDir2.length()>0)
            dm2 = new DirMon(scanDir2,null);
        //km = new KillMon();
        //Thread kt = new Thread(km, "km");
        //kt.start();

        loadUp();

        Timeline tmr = new Timeline(new KeyFrame(Duration.millis(100), ae -> onTimer()));
        tmr.setCycleCount(Animation.INDEFINITE);
        tmr.play();

        myStage = primaryStage;

    }

    public void onTimer()
    {
        iter++;
        List<File> newFiles = dm.Check();
        if ( dm2 != null )
            newFiles.addAll(dm2.Check());
        
        if ( newFiles != null && newFiles.size() > 0 )
        {
            l.info("Timer - Check new arrivals:");
            for (File ifi : newFiles)
            {
                l.info("  file:" + ifi.toString());
                if (!Utils.isImageFile(ifi.getName()))
                {
                    l.info("      not image");
                    continue;
                }
                Pane item = CreateItem(ifi);
                boolean dup = checkForDup((ChoiceItem)item.getUserData());
                if ( !dup )
                {
                    l.info("  Check for better res");
                    checkForBetterRes((ChoiceItem)item.getUserData());
                }
                else
                    l.info("   is a dup");
                
                if (item != null)
                {
                    l.info("   add to list");
                    items.getChildren().add(0,item);
                }
            }
        }
        if ( iter==1 || connected != oldConnect)
        {
            oldConnect = connected;
            if ( connected )
            {
                btmlabel.setStyle("-fx-text-fill: blue; -fx-text-alignment: center; -fx-font-weight: regular;");
                btmlabel.setText("VPN");
            }
            else
            {
                btmlabel.setStyle("-fx-text-fill: red; -fx-text-alignment: center; -fx-font-weight: bold;");
                btmlabel.setText("UNSAFE");
            }
        }
    }

    private boolean checkForDup(ChoiceItem userData)
    {
        for (Node o : items.getChildren())
        {
            ChoiceItem ci = (ChoiceItem) o.getUserData();
            if (ci.crc == userData.crc)
            {
                userData.Set(ChoiceItem.Action.Delete);
                l.info("Dup :" + ci.toString() + " is a dup of " + userData.toString());
                return true;
            }
        }
        return false;
    }

    private boolean checkForBetterRes(ChoiceItem userData)
    {
        String rexmatchstr = "(.*tumblr.*)_(\\d{3,})(\\..*)";
        Pattern p = Pattern.compile(rexmatchstr);

        String nf = userData.file.getName();

        Matcher m = p.matcher(nf);
        int rfsize = userData.dim.height;

        if ( m.find() )
        {
            String sname = m.group(1);
            String ssize = m.group(2);
            String sext = m.group(3);

            l.info("Res Check :" + sname + ", size: " + ssize );

            for (Node o : items.getChildren())
            {
                ChoiceItem ci = (ChoiceItem) o.getUserData();
                String fx = ci.file.getName();
                //int rxsize = ci.dim.height;


                Matcher mx = p.matcher(fx);

                if ( mx.find() )
                {
                    String xname = mx.group(1);
                    String xsize = mx.group(2);
                    String xext = mx.group(3);
 

                    if ( !xname.equals(sname) )
                        continue;
                    
                    l.info("  consider:" + fx + ", size: " + xsize );

                    int ss=0;
                    int xs=0;
                    try
                    {
                        ss = Integer.parseInt(ssize);
                        xs = Integer.parseInt(xsize);
                    }
                    catch( Exception e)
                    {
                        l.warn("can't parse to int:" + ssize + " " + xsize);
                        return false;
                    }
                    if ( xs < ss )
                    {
                        l.info("  its smaller, remove one in list");
                        ci.Set(ChoiceItem.Action.Delete);
                        l.info("OldOne now:" + ci.toString() );
                    }
                    else
                    {
                        l.info("  its bigger or same, remove new one");
                        userData.Set(ChoiceItem.Action.Delete);
                        l.info("NewOne now:" + userData.toString() );
                    }
                   
                    return true;
                }
            }


        }


        return false;
    }

    private void checkArchive(File f)
    {
        TxQuery obj = new TxQuery(f.getPath(),0);
        TxQueryReply reply = obj.waitreply();
    }

    private Pane makeTop()
    {
        VBox b = new VBox();

        Image imageDecline = new Image(getClass().getResourceAsStream("perform.png"));

        Image imageReset = new Image(getClass().getResourceAsStream("reset.png"));
        Image imageNudge = new Image(getClass().getResourceAsStream("nudge.png"));

        Button ka = new Button();
        ka.setGraphic(new ImageView(imageDecline));
        ka.setText("Perform");
        ka.setStyle("-fx-font: 10 arial; -fx-base: #b6f7c9;");
        ka.setPrefSize(125,25);
        ka.setAlignment(Pos.BASELINE_LEFT);

        ka.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                perform(1);
            }
        });

        Button pe = new Button();
        pe.setGraphic(new ImageView(imageDecline));
        pe.setText("Partial");
        pe.setStyle("-fx-font: 10 arial; -fx-base: #d6e7e2;");
        pe.setPrefSize(125,25);
        pe.setAlignment(Pos.BASELINE_LEFT);
        pe.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                perform(0);
            }
        });

        Button ne = new Button();
        ne.setGraphic(new ImageView(imageNudge));
        ne.setText("Nudge All");
        ne.setStyle("-fx-font: 10 arial; -fx-base: #c6c7f2;");
        ne.setPrefSize(125,25);
        ne.setAlignment(Pos.BASELINE_LEFT);
        ne.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                perform(2);
            }
        });

        Button re = new Button();
        re.setGraphic(new ImageView(imageReset));
        re.setText("Reset");
        re.setStyle("-fx-font: 10 arial; -fx-base: #e7b6c9;");
        re.setPrefSize(125,25);
        re.setAlignment(Pos.BASELINE_LEFT);
        re.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                reset();
            }
        });
        b.getChildren().addAll(ka,pe,ne,re);

        return b;
    }

    private boolean moveto(ChoiceItem ci, String d)
    {
        String dstName = d + "/" + ci.file.getName();
        Path dst = Paths.get(dstName);
        Path src = ci.file.toPath();
        
        
        try
        {
            
            if ( !Files.exists(dst) )
            {  // dst dosnt exist, simple move
                l.info("   Moving " + ci.file.getName() + " to " + dst.toString());
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
            else
            { // dst already exists
                if ( Files.size(dst) == Files.size(src) ) // src contains same file, just delete local copy
                { // dst is same size, dont copy and delete local
                    l.info("   DUP, Not Moving " + ci.file.getName() + " to " + dst.toString());
                    Files.delete(src);
                }
                else
                { // dst exists but is different file    
                    int c = 1;
                    while(Files.exists(dst))
                    {
                        dstName = d + "/" + Integer.toString(c) + "_" + ci.file.getName();
                        dst = Paths.get(dstName);
                        l.info("   DUP name only, test  " + dst.toString());
                        c++;
                    }
                    Files.move(src,dst,StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return true;
        }
        catch (IOException e)
        {
            l.warn("Problem moving " + ci.file.getPath() );
        }
        return false;
    }

    private void perform(int level)
    {
        List<Node> toRemove = new LinkedList<Node>();
        l.info("Perform, ac:" + level);

        for( Node o : items.getChildren() )
        {
            ChoiceItem ci = (ChoiceItem)o.getUserData();

            //l.info("Perform, it:" + ci.toString() );
            if ( level == 2  )
            {
                
                ci.Nudge(false);
                l.info("Perform, NUDGED it:" + ci.toString() );
            }
            else
            {
                switch (ci.state)
                {
                    case Store:
                        if (level == 1)
                        {
                            l.info("Perform, STORE " + ci.toString() );
                            if (moveto(ci, archDir))
                                toRemove.add(o);
                        }
                        break;

                    case Keep:
                        l.info("Perform, KEEP " + ci.toString() );
                        if (moveto(ci, keepDir))
                            toRemove.add(o);
                        break;

                    case Special:
                        l.info("Perform, SPECIAL " + ci.toString() );
                        if (moveto(ci, specialDir))
                            toRemove.add(o);
                        break;

                    case Delete:
                        l.info("Perform, DELETE " + ci.toString() );
                        ci.file.delete();
                        toRemove.add(o);
                        break;
                }
            }


        }


        if ( level == 2 )
            return;

        for( Node o : toRemove )
        {
            items.getChildren().remove(o);
        }

    }

    private void reset()
    {
        items.getChildren().clear();
        dm.Rescan();
        if ( dm2 != null )
            dm2.Rescan();
    }

    private void loadUp()
    {

        List<File> files = dm.Existing();
        File f = new File(Settings.GetStr("scan",""));

        for(File ifi:files)
        {
            if ( !Utils.isImageFile(ifi.getName()) )
                continue;
            Pane item = CreateItem(ifi);
            if ( item != null )
                items.getChildren().add(item);

        }
    }

    @Override
    public void stop()
    {
        l.warn("Stopping...km...");
        if ( km != null )
            km.Stop();
        l.warn("Stopped.");
    }


    public void makeArch()
    {
        l.debug("Cleanup running");

        // Name of archiving dir
        Date date = new Date();

        String dname = String.format("%td_%tm_%ty", date,date,date);

        archDir += "/";
        archDir += dname;

        Path archmDir = Paths.get(archDir);
        l.warn("Archive dir is " + archDir.toString());
        if ( !archmDir.toFile().exists() )
        {
            l.warn("(Making it)");
            archmDir.toFile().mkdirs();
        }

    }


    public Pane createGrid()
    {
        errorImg = new Image(getClass().getResourceAsStream("error.png"));

        VBox items = new VBox();
        items.setPadding(new Insets(1, 1, 1, 1));
        items.setSpacing(4);

        return (Pane)items;
    }

    private Pane CreateItem(File f)
    {
        ChoiceItem ci = new ChoiceItem();
        Dimension d = ci.SetFile(f);
        if ( d == null )
            return null;

        Label b1 = new Label();
        b1.setPrefSize(90,15);
        b1.setText(String.format("%.0f x %.0f", d.getWidth(), d.getHeight())  );
        b1.setTextFill(Color.web("#996633"));
        b1.setAlignment(Pos.CENTER);

        VBox item = new VBox();

        item.getChildren().addAll(ci.imageBtn,b1);

        item.setUserData(ci);

        return item;
    }

    // TODO 3 buttons - old - keep - keep special
    // TODO after 10 seconds of incativity, chosen buttons move there
    // TODO overall button to clean remaining to old and coosen to wherever

}
