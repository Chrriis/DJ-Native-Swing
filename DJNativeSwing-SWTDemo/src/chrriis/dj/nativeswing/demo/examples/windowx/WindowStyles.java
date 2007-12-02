/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.windowx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.ui.JWindowX;

/**
 * @author Christopher Deckers
 */
public class WindowStyles extends JPanel {

  public WindowStyles() {
    super(new GridBagLayout());
    JPanel contentPane = new JPanel(new BorderLayout(0, 0));
    JPanel stylesPanel = new JPanel();
    stylesPanel.setBorder(BorderFactory.createTitledBorder("Styles"));
    stylesPanel.setLayout(new BoxLayout(stylesPanel, BoxLayout.Y_AXIS));
    final JCheckBox closableCheckBox = new JCheckBox("Closable");
    stylesPanel.add(closableCheckBox);
    final JCheckBox maximizableCheckBox = new JCheckBox("Maximizable");
    stylesPanel.add(maximizableCheckBox);
    final JCheckBox minimizableCheckBox = new JCheckBox("Minimizable");
    stylesPanel.add(minimizableCheckBox);
    final JCheckBox borderCheckBox = new JCheckBox("Border");
    stylesPanel.add(borderCheckBox);
    final JCheckBox titleBarCheckBox = new JCheckBox("Title Bar");
    stylesPanel.add(titleBarCheckBox);
    final JCheckBox toolWindowCheckBox = new JCheckBox("Tool Window");
    stylesPanel.add(toolWindowCheckBox);
    contentPane.add(stylesPanel, BorderLayout.WEST);
    JPanel attributesPanel = new JPanel();
    attributesPanel.setBorder(BorderFactory.createTitledBorder("Attributes"));
    attributesPanel.setLayout(new BoxLayout(attributesPanel, BoxLayout.Y_AXIS));
    final JCheckBox parentCheckBox = new JCheckBox("Set a parent (Dialog vs Frame)");
    attributesPanel.add(parentCheckBox);
//    final JCheckBox modalCheckBox = new JCheckBox("Modal");
//    attributesPanel.add(modalCheckBox);
    final JCheckBox resizableCheckBox = new JCheckBox("Resizable");
    attributesPanel.add(resizableCheckBox);
    final JCheckBox undecoratedCheckBox = new JCheckBox("Undecorated");
    attributesPanel.add(undecoratedCheckBox);
    final JCheckBox alwaysOnTopCheckBox = new JCheckBox("Always on Top");
    attributesPanel.add(alwaysOnTopCheckBox);
    final JCheckBox defineShapeCheckBox = new JCheckBox("Shaped (forcing undecorated)");
    attributesPanel.add(defineShapeCheckBox);
    contentPane.add(attributesPanel, BorderLayout.EAST);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    JButton openButton = new JButton("Open");
    openButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int windowStyleMask = 0;
        if(closableCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.CLOSABLE_STYLE_MASK;
        }
        if(minimizableCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.MINIMIZABLE_STYLE_MASK;
        }
        if(maximizableCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.MAXIMIZABLE_STYLE_MASK;
        }
        if(borderCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.BORDER_STYLE_MASK;
        }
        if(titleBarCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.TITLE_BAR_STYLE_MASK;
        }
        if(toolWindowCheckBox.isSelected()) {
          windowStyleMask |= JWindowX.TOOL_WINDOW_STYLE_MASK;
        }
        final JWindowX windowX = new JWindowX(parentCheckBox.isSelected()? SwingUtilities.getWindowAncestor(WindowStyles.this): null);
        windowX.setWindowStyle(windowStyleMask);
//        windowX.setModal(modalCheckBox.isSelected());
        windowX.setResizable(resizableCheckBox.isSelected());
        windowX.setUndecorated(undecoratedCheckBox.isSelected());
        windowX.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        // Create the content of the window
        final Container contentPane = windowX.getContentPane();
        JPanel progressPane = new JPanel(new BorderLayout(0, 0));
        progressPane.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        progressPane.setBackground(Color.BLACK);
        JLabel label = new JLabel("Fake Waiting...");
        label.setForeground(Color.WHITE);
        progressPane.add(label, BorderLayout.NORTH);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressPane.add(progressBar, BorderLayout.CENTER);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            windowX.setVisible(false);
          }
        });
        progressPane.add(closeButton, BorderLayout.EAST);
        contentPane.add(progressPane, BorderLayout.CENTER);
        windowX.pack();
        if(defineShapeCheckBox.isSelected()) {
          Dimension s = windowX.getPreferredSize();
          Area area = new Area();
          area.add(new Area(new RoundRectangle2D.Double(0, 0, s.width, s.height, 20, 20)));
          area.add(new Area(new Rectangle(0, s.height - 20, 20, 20)));
          area.add(new Area(new Rectangle(s.width - 20, 0, 20, 20)));
          windowX.setShape(area);
        }
        windowX.setLocationRelativeTo(WindowStyles.this);
        windowX.setVisible(true);
      }
    });
    // Defaults
    toolWindowCheckBox.setSelected(true);
    alwaysOnTopCheckBox.setSelected(true);
    buttonPanel.add(openButton);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    add(contentPane);
  }
  
}
