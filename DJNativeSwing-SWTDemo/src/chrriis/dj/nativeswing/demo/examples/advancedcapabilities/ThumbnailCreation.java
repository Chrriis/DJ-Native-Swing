/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.advancedcapabilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeComponent;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class ThumbnailCreation extends JPanel {

  private static final Dimension THUMBNAIL_SIZE = new Dimension(200, 150);
  
  private JWebBrowser webBrowser;
  private JLabel thumbnailLabel;
  
  public ThumbnailCreation() {
    super(new BorderLayout(0, 0));
    webBrowser = new JWebBrowser();
    webBrowser.setBorder(BorderFactory.createTitledBorder("Web Browser component"));
    webBrowser.setURL("http://www.google.com");
    add(webBrowser, BorderLayout.CENTER);
    JPanel eastPanel = new JPanel(new GridBagLayout());
    JPanel thumbnailPanel = new JPanel(new BorderLayout(0, 0));
    JPanel thumbnailBorderPanel = new JPanel(new GridBagLayout());
    thumbnailBorderPanel.setBorder(BorderFactory.createTitledBorder("Thumbnail"));
    JPanel thumbnailImagePanel = new JPanel(new BorderLayout(0, 0));
    thumbnailImagePanel.setBorder(BorderFactory.createEtchedBorder());
    thumbnailLabel = new JLabel(" No thumbnail ");
    thumbnailLabel.setHorizontalAlignment(JLabel.CENTER);
    thumbnailLabel.setVerticalAlignment(JLabel.CENTER);
    thumbnailImagePanel.add(thumbnailLabel, BorderLayout.CENTER);
    thumbnailBorderPanel.add(thumbnailImagePanel);
    thumbnailPanel.add(thumbnailBorderPanel, BorderLayout.CENTER);
    JPanel thumbnailButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    JButton createThumbnailButton = new JButton("Create");
    createThumbnailButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createThumbnail();
      }
    });
    thumbnailButtonPanel.add(createThumbnailButton);
    thumbnailPanel.add(thumbnailButtonPanel, BorderLayout.SOUTH);
    eastPanel.add(thumbnailPanel);
    thumbnailLabel.setPreferredSize(THUMBNAIL_SIZE);
    thumbnailPanel.setPreferredSize(thumbnailPanel.getPreferredSize());
    thumbnailLabel.setPreferredSize(null);
    add(eastPanel, BorderLayout.EAST);
  }
  
  private void createThumbnail() {
    final NativeComponent nativeComponent = webBrowser.getNativeComponent();
    final int cWidth = nativeComponent.getWidth();
    final int cHeight = nativeComponent.getHeight();
    if(cWidth <= 0 || cHeight <= 0) {
      thumbnailLabel.setText(" No thumbnail ");
      thumbnailLabel.setIcon(null);
      return;
    }
    new Thread() {
      @Override
      public void run() {
        BufferedImage image = new BufferedImage(cWidth, cHeight, BufferedImage.TYPE_INT_ARGB);
        nativeComponent.paintComponent(image);
        int tWidth = THUMBNAIL_SIZE.width;
        int tHeight = THUMBNAIL_SIZE.height;
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
            thumbnailLabel.setText(null);
            thumbnailLabel.setIcon(imageIcon);
          }
        });
      }
    }.start();
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ThumbnailCreation(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
