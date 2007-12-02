/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Window;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.NativeInterfaceHandler;

/**
 * @author Christopher Deckers
 */
public class JDirectoryChooser {

  public static final int CANCEL_OPTION = 0;
  public static final int APPROVE_OPTION = 1;
  
  protected volatile File directory;
  protected volatile String title;
  
  public int showDialog(final Component invoker) {
    final JDialog modalWindow = new JDialog(invoker instanceof Window? (Window)invoker: SwingUtilities.getWindowAncestor(invoker));
    modalWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    modalWindow.setUndecorated(true);
    modalWindow.setLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
    modalWindow.setModal(true);
    modalWindow.setFocusableWindowState(false);
    modalWindow.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    Canvas canvas = new Canvas();
    modalWindow.getContentPane().add(canvas, BorderLayout.CENTER);
    // Realize the component and the canvas
    modalWindow.addNotify();
    new Thread() {
      @Override
      public void run() {
        NativeInterfaceHandler.invokeSWT(new Runnable() {
          public void run() {
            Shell parentShell = SWT_AWT.new_Shell(NativeInterfaceHandler.getDisplay(), (Canvas)modalWindow.getContentPane().getComponent(0));
            DirectoryDialog dialog = new DirectoryDialog(parentShell);
            if(directory != null) {
              dialog.setFilterPath(directory.getAbsolutePath());
            }
            if(title != null) {
              dialog.setText(title);
            }
            dialog.open();
            String filterPath = dialog.getFilterPath();
            directory = filterPath == null || "".equals(filterPath)? null: new File(filterPath);
            title = dialog.getText();
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                modalWindow.setVisible(false);
              }
            }); 
            parentShell.dispose();
          }
        });
      }
    }.start();
    modalWindow.setVisible(true);
    return directory == null? CANCEL_OPTION: APPROVE_OPTION;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public void setDirectory(File directory) {
    this.directory = directory;
  }
  
  public String getTitle() {
    return title;
  }
  
  public File getDirectory() {
    return directory;
  }
  
}
