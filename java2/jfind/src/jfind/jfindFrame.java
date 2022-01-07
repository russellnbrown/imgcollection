/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jfind;

import arenbee.api.*;
import arenbee.other.*;
import com.google.gson.Gson;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import arenbee.other.Logger;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import okhttp3.MediaType;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author russell.brown
 */
public class jfindFrame extends javax.swing.JFrame
{

    public static jfindFrame instance = null;

    TextSearchTable tm;

    /**
     * Creates new form jfindFrame
     */
    public jfindFrame()
    {

        Logger.Create("jfind", Logger.Level.Info, Logger.Level.Info);
        instance = this;

        initComponents();

        imgPNL.setDropTarget(new DropTarget()
        {
            public synchronized void drop(DropTargetDropEvent evt)
            {
                try
                {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Logger.Info("File dropped");
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles)
                    {
                        Logger.Info("Dropped file is " + file.getAbsolutePath());
                        loadFile(file.getAbsolutePath());
                        findImage(file.getAbsolutePath());
                        Logger.Info("Results are back");
                        break;
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        tm = new TextSearchTable();

        ts.setModel(tm);

        ts.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                if (event.getValueIsAdjusting() == false)
                {
                    String selected = top + "/" + ts.getValueAt(ts.getSelectedRow(), 0).toString();
                    String file = ts.getValueAt(ts.getSelectedRow(), 1).toString();
                    if (file != null && file.length() > 0)
                    {
                        loadFile(selected + "/" + file);
                    } else
                    {
                        Logger.Info("No file specified");
                    }
                }
            }

        });

        ts.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                try
                {
                    String selected = top + "/" + ts.getValueAt(ts.getSelectedRow(), 0).toString();
                    String file = ts.getValueAt(ts.getSelectedRow(), 1).toString();
                    if (e.getClickCount() == 2)
                    {
                        //System.out.println("TWO " + selected + ":" + file);

                        if (file.length() == 0)
                        {
                            if (Desktop.isDesktopSupported())
                            {
                                Desktop.getDesktop().open(new File(selected));
                            }
                        } else
                        {

                            showFile(selected + "/" + file);

                        }
                    }
                    if (e.getClickCount() == 1)
                    {
                        //System.out.println("ONE " + ts.getValueAt(ts.getSelectedRow(), 0).toString());
                    }
                } catch (Exception exx)
                {
                    Logger.Severe("Error opening " + exx.getMessage());
                }

            }
        });
    }

    private void showFile(String p)
    {
        try
        {
            JFrame f = new JFrame(); //creates jframe f
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //this is your screen size
            File ff = new File(p);
            BufferedImage img = ImageIO.read(ff);
            int nw = img.getWidth(), nh = img.getHeight();
            boolean resize = false;
            while (nw > (screenSize.width - 50) && nh > (screenSize.height - 50))
            {
                nw -= 10;
                nh -= 10;
                resize = true;
            }
            Image newimg = img.getScaledInstance(nw, nh, java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(newimg);
            JLabel lbl = new JLabel(icon); //puts the image into a jlabel
            f.getContentPane().add(lbl); //puts label inside the jframe
            f.setSize(icon.getIconWidth(), icon.getIconHeight()); //gets h and w of image and sets jframe to the size
            int x = (screenSize.width - f.getSize().width) / 2; //These two lines are the dimensions
            int y = (screenSize.height - f.getSize().height) / 2;//of the center of the screen
            f.setLocation(x, y); //sets the location of the jframe
            f.setVisible(true); //makes the jframe visible    
        } catch (IOException ex)
        {
            java.util.logging.Logger.getLogger(jfindFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadFile(String p)
    {
        try
        {
            File f = new File(p);
            BufferedImage img = ImageIO.read(f);
            Image newimg = img.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(newimg);
            Logger.Info("Loading image pane with " + p);
            imgLBL.setIcon(icon);
            imgLBL.setText(p);
        } catch (Exception exx)
        {
            Logger.Severe("Error opening " + exx.getMessage());
        }

    }

    private void findImage(String ifind)
    {
        Logger.Info("Send request to find image " + ifind);
        GenericSearchResult ds = apiSearch("imgsrch", Helpers.strToHex(ifind));
        Logger.Info("Got result back, loading " + ds.items.size() + " results");
        tm.Clear();
        for (int i = 0; i < ds.items.size(); i++)
        {
            tm.Add(ds.items.get(i).path, ds.items.get(i).file);
        }
        tm.Finished();
        Logger.Info("Image search finished.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        textSearchPNL = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textSearchTB = new javax.swing.JTextField();
        textSearchTGL = new javax.swing.JToggleButton();
        srchTextBTN = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        ts = new javax.swing.JTable();
        imgPNL = new javax.swing.JPanel();
        imgLBL = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        textSearchPNL.setBackground(new java.awt.Color(153, 255, 51));

        jLabel1.setText("Search For:");
        jLabel1.setToolTipText("");

        textSearchTB.setToolTipText("Enter search term here");

        textSearchTGL.setText("Dir ( or File )");
        textSearchTGL.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                textSearchTGLItemStateChanged(evt);
            }
        });
        textSearchTGL.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                textSearchTGLActionPerformed(evt);
            }
        });

        srchTextBTN.setText("Srch");
        srchTextBTN.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                srchTextBTNActionPerformed(evt);
            }
        });

        ts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(ts);

        javax.swing.GroupLayout textSearchPNLLayout = new javax.swing.GroupLayout(textSearchPNL);
        textSearchPNL.setLayout(textSearchPNLLayout);
        textSearchPNLLayout.setHorizontalGroup(
            textSearchPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textSearchPNLLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textSearchPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(textSearchPNLLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textSearchTB, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textSearchTGL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(srchTextBTN)
                        .addGap(0, 41, Short.MAX_VALUE)))
                .addContainerGap())
        );
        textSearchPNLLayout.setVerticalGroup(
            textSearchPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textSearchPNLLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textSearchPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textSearchTB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textSearchTGL)
                    .addComponent(srchTextBTN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
        );

        textSearchTB.getAccessibleContext().setAccessibleName("");

        imgPNL.setBackground(new java.awt.Color(204, 204, 255));

        imgLBL.setText("jLabel2");

        javax.swing.GroupLayout imgPNLLayout = new javax.swing.GroupLayout(imgPNL);
        imgPNL.setLayout(imgPNLLayout);
        imgPNLLayout.setHorizontalGroup(
            imgPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgPNLLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imgLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        imgPNLLayout.setVerticalGroup(
            imgPNLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgPNLLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imgLBL, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textSearchPNL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(imgPNL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(imgPNL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textSearchPNL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textSearchTGLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_textSearchTGLItemStateChanged
        Logger.Info("text search toggle changed to %s", evt.toString());
    }//GEN-LAST:event_textSearchTGLItemStateChanged

    private void textSearchTGLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textSearchTGLActionPerformed
        Logger.Info("text search action perormed %s", evt.toString());
        // TODO add your handling code here:
    }//GEN-LAST:event_textSearchTGLActionPerformed

    GenericSearchResult apiSearch(String type, String search)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:6020/" + type + "/" + search).build();

        try
        {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            String reply = response.body().string();
            System.out.println(reply);
            GenericSearchResult dr = new Gson().fromJson(reply, GenericSearchResult.class);
            top = dr.top;
            System.out.println(dr.toString());
            return dr;
        } catch (Exception e)
        {
            System.out.println("Err:" + e.getLocalizedMessage());
        }
        return null;
    }

    private String top = "";

    private void srchTextBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srchTextBTNActionPerformed

        tm.Clear();

        if (textSearchTGL.isSelected())
        {
            GenericSearchResult ds = apiSearch("txtsrch", textSearchTB.getText());
            for (int i = 0; i < ds.items.size(); i++)
            {
                tm.Add(ds.items.get(i).path, ds.items.get(i).file);
            }
        } else
        {
            GenericSearchResult ds = apiSearch("dirsrch", textSearchTB.getText());
            for (int i = 0; i < ds.items.size(); i++)
            {
                tm.Add(ds.items.get(i).path);
            }
        }

        tm.Finished();

    }//GEN-LAST:event_srchTextBTNActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new jfindFrame().setVisible(true);
            }
        });
    }

    private void testConnect()
    {

        String ifind = "C:\\\\TestEnvironments\\\\sync\\\\src\\\\i2\\\\test.jpg";

        GenericSearchResult ds = apiSearch("imgsrch", Helpers.strToHex(ifind));
        for (int i = 0; i < ds.items.size(); i++)
        {
            tm.Add(ds.items.get(i).path, ds.items.get(i).file);
        }
        /*
            ImageSearchRequest isr = new ImageSearchRequest();
            isr.path = "C:\\TestEnvironments\\sync\\src\\i2\\test.jpg";
            String jsonBody = new Gson().toJson(isr);
            
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jsonBody);
            
            OkHttpClient client = new OkHttpClient();
            
            Request request = new Request.Builder()
                .url("http://localhost:6020/imgsrch")
            //    .body(body) //PUT
                //.addHeader("Authorization", header)
                .build();
            

          
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String reply = response.body().string();
                System.out.println(reply);
                GenericSearchResult dr = new Gson().fromJson(reply, GenericSearchResult.class );
                top = dr.top;
                System.out.println(dr.toString());
                return;
            }
            catch(Exception e)
            {
                System.out.println("Err:"+e.getLocalizedMessage());            
            }
         */
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel imgLBL;
    private javax.swing.JPanel imgPNL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton srchTextBTN;
    private javax.swing.JPanel textSearchPNL;
    private javax.swing.JTextField textSearchTB;
    private javax.swing.JToggleButton textSearchTGL;
    private javax.swing.JTable ts;
    // End of variables declaration//GEN-END:variables
}
