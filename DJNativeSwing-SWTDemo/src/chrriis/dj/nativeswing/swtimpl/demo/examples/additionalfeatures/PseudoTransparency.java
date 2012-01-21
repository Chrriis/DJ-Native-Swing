/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class PseudoTransparency {

  public static JComponent createContent() {
    final JWebBrowser webBrowser = new JWebBrowser(JWebBrowser.constrainVisibility());
    final AtomicBoolean isDisposedRef = new AtomicBoolean(false);
    JPanel contentPane = new JPanel(null) {
      @Override
      public boolean isOptimizedDrawingEnabled() {
        // This indicates that the component allows layering of children.
        return false;
      }
      @Override
      public void removeNotify() {
        super.removeNotify();
        isDisposedRef.set(true);
      }
    };
    webBrowser.setBarsVisible(false);
    webBrowser.navigate("http://www.google.com");
    webBrowser.setBounds(50, 50, 500, 400);
    contentPane.add(webBrowser);
    JLabel descriptionLabel = new JLabel("Grab and move that image over the native component: ");
    descriptionLabel.setSize(descriptionLabel.getPreferredSize());
    descriptionLabel.setLocation(5, 15);
    contentPane.add(descriptionLabel);
    ImageIcon icon = new ImageIcon(PseudoTransparency.class.getResource("resource/DJIcon48x48.png"));
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
    contentPane.add(imageLabel);
    // Force label to be logically on top.
    contentPane.setComponentZOrder(imageLabel, 0);
    updateBackgroundBuffer(webBrowser, isDisposedRef);
    return contentPane;
  }

  private static void updateBackgroundBuffer(final JWebBrowser webBrowser, final AtomicBoolean isDisposedRef) {
    // we refresh the background buffer outside the UI thread, to minimize the overhead.
    new Thread("NativeSwing Pseudo Transparency Refresh") {
      @Override
      public void run() {
        int i = 0;
        while(!isDisposedRef.get()) {
          if(i == 0) {
            // Every now and then we refresh the full buffer.
            webBrowser.getNativeComponent().createBackBuffer();
          } else {
            // The rest of the time we only update the areas that is covered with our overlay.
            webBrowser.getNativeComponent().updateBackBufferOnVisibleTranslucentAreas();
          }
          i = (i + 1) % 4;
          try {
            sleep(500);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterface.open();
    UIUtils.setPreferredLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createContent(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
