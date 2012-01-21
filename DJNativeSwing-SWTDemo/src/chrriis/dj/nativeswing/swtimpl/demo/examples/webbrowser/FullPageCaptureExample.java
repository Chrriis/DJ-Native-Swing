/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class FullPageCaptureExample {

  private static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    // Constant is placed in code and not in class, because Dimension contains a static initializer that loads AWT, and NativeSwing initialization must happen first.
    final Dimension THUMBNAIL_SIZE = new Dimension(400, 300);
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.navigate("http://www.google.com");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    // Create an panel with a screen capture button.
    JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    JButton captureButton = new JButton("Full-page capture");
    captureButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String result = (String)webBrowser.executeJavascriptWithResult(
        		"var width = 0;" + LS +
        		"var height = 0;" + LS +
        		"if(document.documentElement) {" + LS +
        		"  width = Math.max(width, document.documentElement.scrollWidth);" + LS +
        		"  height = Math.max(height, document.documentElement.scrollHeight);" + LS +
        		"}" + LS +
        		"if(self.innerWidth) {" + LS +
        		"  width = Math.max(width, self.innerWidth);" + LS +
        		"  height = Math.max(height, self.innerHeight);" + LS +
        		"}" + LS +
        		"if(document.body.scrollWidth) {" + LS +
        		"  width = Math.max(width, document.body.scrollWidth);" + LS +
        		"  height = Math.max(height, document.body.scrollHeight);" + LS +
        		"}" + LS +
        		"return width + '/' + height;");
        // This may happen from time to time so we have to fail gracefully.
        int index = result == null? -1: result.indexOf("/");
        if(index < 0) {
          JOptionPane.showMessageDialog(webBrowser, "An error occurred while capturing the full-page", "Full-page capture failure", JOptionPane.ERROR_MESSAGE);
        } else {
          NativeComponent nativeComponent = webBrowser.getNativeComponent();
          Dimension originalSize = nativeComponent.getSize();
          Dimension imageSize = new Dimension(Integer.parseInt(result.substring(0, index)), Integer.parseInt(result.substring(index + 1)));
          // We add some artificial spacing because with scrollbars logic it is likely to be wrong...
          imageSize.width = Math.max(originalSize.width, imageSize.width + 50);
          imageSize.height = Math.max(originalSize.height, imageSize.height + 50);
          nativeComponent.setSize(imageSize);
          BufferedImage image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_RGB);
          nativeComponent.paintComponent(image);
          nativeComponent.setSize(originalSize);
          Window window = SwingUtilities.getWindowAncestor(webBrowser);
          JDialog dialog;
          if(window instanceof Frame) {
            dialog = new JDialog((Frame)window, "Full-page capture", true);
          } else {
            dialog = new JDialog((Dialog)window, "Full-page capture", true);
          }
          int tWidth = THUMBNAIL_SIZE.width;
          int tHeight = THUMBNAIL_SIZE.height;
          final ImageIcon imageIcon;
          if(imageSize.width <= tWidth && imageSize.height <= tHeight) {
            imageIcon = new ImageIcon(image);
          } else {
            float ratio1 = imageSize.width / (float)imageSize.height;
            float ratio2 = tWidth / (float)tHeight;
            int width = ratio1 > ratio2? tWidth: Math.round(tWidth * ratio1 / ratio2);
            int height = ratio1 < ratio2? tHeight: Math.round(tHeight * ratio2 / ratio1);
            imageIcon = new ImageIcon(image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
          }
          dialog.getContentPane().add(new JLabel(imageIcon));
          dialog.pack();
          dialog.setLocationRelativeTo(window);
          dialog.setVisible(true);
        }
      }
    });
    southPanel.add(captureButton);
    contentPane.add(southPanel, BorderLayout.SOUTH);
    return contentPane;
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
