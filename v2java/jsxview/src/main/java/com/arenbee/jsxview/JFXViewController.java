package com.arenbee.jsxview;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class JFXViewController implements Initializable {
    @FXML
    private Label welcomeText;

    @FXML private TreeView dirTree;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Path tpath = Paths.get("C:/");

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
    }

    public void initializeLater(Scene scene, Stage stage) {

    }

    private class OnDirTreeSelection implements ChangeListener
    {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue)
        {
            if ( newValue == null )
                return;
            TreeItem<File> selectedItem = (TreeItem<File>) newValue;
         //   l.info("Main - Select dir tree leaf: " + selectedItem.getValue().getPath());
       //     loadADirIntoIconView(selectedItem.getValue(),true);
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



}