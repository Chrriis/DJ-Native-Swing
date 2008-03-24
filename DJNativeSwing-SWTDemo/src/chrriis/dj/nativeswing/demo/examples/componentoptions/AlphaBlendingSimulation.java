/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.componentoptions;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import chrriis.dj.nativeswing.ui.JWebBrowser;
import chrriis.dj.nativeswing.ui.NativeComponent;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.VisibilityConstraint;

/**
 * @author Christopher Deckers
 */
public class AlphaBlendingSimulation extends JPanel {

  private JWebBrowser webBrowser;
  
  @Override
  public boolean isOptimizedDrawingEnabled() {
    // This indicates that the component allows layering of children.
    return false;
  }
  
  public AlphaBlendingSimulation() {
    super(null);
    NativeComponent.getNextInstanceOptions().setVisibilityConstraint(VisibilityConstraint.FULL_COMPONENT_TREE);
    webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setURL("http://www.google.com");
    webBrowser.setBounds(50, 50, 500, 400);
    add(webBrowser);
    JLabel descriptionLabel = new JLabel("Grab and move that image over the native component: ");
    descriptionLabel.setSize(descriptionLabel.getPreferredSize());
    descriptionLabel.setLocation(5, 15);
    add(descriptionLabel);
    ImageIcon icon = new ImageIcon(getClass().getResource("../../resource/DJIcon48x48.png"));
    final JLabel imageLabel = new JLabel(icon);
    MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
      private Point originalMouseLocation;
      private Point originalLocation;
      @Override
      public void mousePressed(MouseEvent e) {
        originalMouseLocation = SwingUtilities.convertPoint(imageLabel, e.getPoint(), imageLabel.getParent());
        originalLocation = imageLabel.getLocation();
      }
      @Override
      public void mouseDragged(MouseEvent e) {
        Point newMouseLocation = SwingUtilities.convertPoint(imageLabel, e.getPoint(), imageLabel.getParent());
        imageLabel.setLocation(originalLocation.x - originalMouseLocation.x + newMouseLocation.x, originalLocation.y - originalMouseLocation.y + newMouseLocation.y);
        imageLabel.repaint();
      }
    };
    imageLabel.addMouseMotionListener(mouseInputAdapter);
    imageLabel.addMouseListener(mouseInputAdapter);
    imageLabel.setSize(imageLabel.getPreferredSize());
    imageLabel.setLocation(descriptionLabel.getWidth(), 0);
    imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    add(imageLabel);
    // Force label to be logically on top.
    setComponentZOrder(imageLabel, 0);
    updateBackgroundBuffer();
  }
  
  private void updateBackgroundBuffer() {
    new Thread() {
      @Override
      public void run() {
        while(true) {
          // we refresh the background buffer outside the UI thread, to minimize the overhead.
          webBrowser.getNativeComponent().createBackBuffer();
          try {
            sleep(500);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
  }
  
}
