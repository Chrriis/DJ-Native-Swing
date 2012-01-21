/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class ThumbnailCreation {

  private static abstract class ThumbnailPane extends JPanel {

    public static final Dimension THUMBNAIL_SIZE = new Dimension(200, 150);
    
    private JLabel thumbnailLabel;

    public ThumbnailPane(String title) {
      super(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(title));
      JPanel thumbnailPanel = new JPanel(new GridBagLayout());
      JPanel thumbnailImagePanel = new JPanel(new BorderLayout());
      thumbnailImagePanel.setBorder(BorderFactory.createEtchedBorder());
      thumbnailLabel = new JLabel();
      thumbnailLabel.setHorizontalAlignment(JLabel.CENTER);
      thumbnailLabel.setVerticalAlignment(JLabel.CENTER);
      thumbnailImagePanel.add(thumbnailLabel, BorderLayout.CENTER);
      thumbnailPanel.add(thumbnailImagePanel);
      add(thumbnailPanel, BorderLayout.CENTER);
      JPanel thumbnailButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
      JButton createThumbnailButton = new JButton("Create");
      createThumbnailButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          createThumbnail();
        }
      });
      thumbnailButtonPanel.add(createThumbnailButton);
      add(thumbnailButtonPanel, BorderLayout.SOUTH);
      setThumbnail(null);
      thumbnailLabel.setPreferredSize(THUMBNAIL_SIZE);
      setPreferredSize(getPreferredSize());
      thumbnailLabel.setPreferredSize(null);
    }

    public abstract void createThumbnail();

    public void setThumbnail(ImageIcon thumbnailIcon) {
      if(thumbnailIcon == null) {
        thumbnailLabel.setText(" No thumbnail ");
        thumbnailLabel.setIcon(null);
      } else {
        thumbnailLabel.setText(null);
        thumbnailLabel.setIcon(thumbnailIcon);
      }
    }

  }

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.navigate("http://www.google.com");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    JPanel eastPanel = new JPanel(new GridBagLayout());
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    eastPanel.add(new ThumbnailPane("Full Web Browser") {
      @Override
      public void createThumbnail() {
        ThumbnailCreation.createThumbnail(this, webBrowser);
      }
    }, cons);
    cons.gridy++;
    eastPanel.add(new ThumbnailPane("Native Area Only") {
      @Override
      public void createThumbnail() {
        ThumbnailCreation.createThumbnail(this, webBrowser.getNativeComponent());
      }
    }, cons);
    contentPane.add(eastPanel, BorderLayout.EAST);
    return contentPane;
  }

  private static void createThumbnail(final ThumbnailPane thumbnailPane, final Component component) {
    final int cWidth = component.getWidth();
    final int cHeight = component.getHeight();
    if(cWidth <= 0 || cHeight <= 0) {
      thumbnailPane.setThumbnail(null);
      return;
    }
    new Thread("NativeSwing Thumbnail Loader") {
      @Override
      public void run() {
        BufferedImage image = new BufferedImage(cWidth, cHeight, BufferedImage.TYPE_INT_ARGB);
        if(component instanceof NativeComponent) {
          ((NativeComponent)component).paintComponent(image);
        } else {
          // In fact, print can also be used with NativeComponent.
          Graphics g = image.getGraphics();
          component.print(g);
          g.dispose();
        }
        int tWidth = ThumbnailPane.THUMBNAIL_SIZE.width;
        int tHeight = ThumbnailPane.THUMBNAIL_SIZE.height;
        final ImageIcon imageIcon;
        if(cWidth <= tWidth && cHeight <= tHeight) {
          imageIcon = new ImageIcon(image);
        } else {
          float ratio1 = cWidth / (float)cHeight;
          float ratio2 = tWidth / (float)tHeight;
          int width = ratio1 > ratio2? tWidth: Math.round(tWidth * ratio1 / ratio2);
          int height = ratio1 < ratio2? tHeight: Math.round(tHeight * ratio2 / ratio1);
          imageIcon = new ImageIcon(image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            thumbnailPane.setThumbnail(imageIcon);
          }
        });
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
