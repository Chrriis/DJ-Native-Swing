/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class ConstrainVisibility {

  protected static final String LS = System.getProperty("line.separator");
  protected static final int OFFSET_X = 150;
  protected static final int OFFSET_Y = 120;
  protected static final int WIDTH = 200;
  protected static final int HEIGHT = 200;

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setOpaque(true);
    layeredPane.setBackground(new Color(200, 200, 255));
    int layerIndex = 0;
    // A flash player
    JFlashPlayer flashPlayer = new JFlashPlayer(JFlashPlayer.constrainVisibility());
    flashPlayer.setControlBarVisible(false);
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    flashPlayer.setBounds(0, 0, 200, 200);
    layeredPane.setLayer(flashPlayer, layerIndex++);
    layeredPane.add(flashPlayer);
    // A swing panel
    JPanel swingPanel = new JPanel();
    swingPanel.setBorder(BorderFactory.createTitledBorder("Swing JPanel"));
    swingPanel.setBackground(Color.GREEN);
    swingPanel.setBounds(200, 100, WIDTH, HEIGHT);
    layeredPane.setLayer(swingPanel, layerIndex++);
    layeredPane.add(swingPanel);
    // A web browser
    JWebBrowser webBrowser = new JWebBrowser(JWebBrowser.constrainVisibility());
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>A web page</h1>" + LS +
        "    <p>A paragraph with a <a href=\"http://www.google.com\">link</a>.</p>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowser.setBounds(300, 250, WIDTH, HEIGHT);
    layeredPane.setLayer(webBrowser, layerIndex++);
    // A swing button
    JButton swingButton = new JButton("Swing JButton");
    swingButton.setBounds(400, 400, WIDTH, HEIGHT);
    layeredPane.setLayer(swingButton, layerIndex++);
    layeredPane.add(swingButton);
    layeredPane.add(webBrowser);
    layeredPane.setPreferredSize(new Dimension(600, 600));
    contentPane.add(new JScrollPane(layeredPane), BorderLayout.CENTER);
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
