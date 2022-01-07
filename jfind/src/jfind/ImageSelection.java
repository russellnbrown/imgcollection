/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jfind;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

class ImageSelection extends TransferHandler implements Transferable {

  private static final DataFlavor flavors[] = { DataFlavor.imageFlavor };

  private Image image;

  public int getSourceActions(JComponent c) {
    return TransferHandler.COPY;
  }

  public boolean canImport(JComponent comp, DataFlavor flavor[]) {
    if (!(comp instanceof JLabel) && !(comp instanceof AbstractButton)) {
      return false;
    }
    for (int i = 0, n = flavor.length; i < n; i++) {
      for (int j = 0, m = flavors.length; j < m; j++) {
        if (flavor[i].equals(flavors[j])) {
          return true;
        }
      }
    }
    return false;
  }

  public Transferable createTransferable(JComponent comp) {
    // Clear
    image = null;

    if (comp instanceof JLabel) {
      JLabel label = (JLabel) comp;
      Icon icon = label.getIcon();
      if (icon instanceof ImageIcon) {
        image = ((ImageIcon) icon).getImage();
        return this;
      }
    } else if (comp instanceof AbstractButton) {
      AbstractButton button = (AbstractButton) comp;
      Icon icon = button.getIcon();
      if (icon instanceof ImageIcon) {
        image = ((ImageIcon) icon).getImage();
        return this;
      }
    }
    return null;
  }

  public boolean importData(JComponent comp, Transferable t) {
    if (comp instanceof JLabel) {
      JLabel label = (JLabel) comp;
      if (t.isDataFlavorSupported(flavors[0])) {
        try {
          image = (Image) t.getTransferData(flavors[0]);
          ImageIcon icon = new ImageIcon(image);
          label.setIcon(icon);
          return true;
        } catch (UnsupportedFlavorException ignored) {
        } catch (IOException ignored) {
        }
      }
    } else if (comp instanceof AbstractButton) {
      AbstractButton button = (AbstractButton) comp;
      if (t.isDataFlavorSupported(flavors[0])) {
        try {
          image = (Image) t.getTransferData(flavors[0]);
          ImageIcon icon = new ImageIcon(image);
          button.setIcon(icon);
          return true;
        } catch (UnsupportedFlavorException ignored) {
        } catch (IOException ignored) {
        }
      }
    }
    return false;
  }

  // Transferable
  public Object getTransferData(DataFlavor flavor) {
    if (isDataFlavorSupported(flavor)) {
      return image;
    }
    return null;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavors[0].equals(flavor);
  }
}
