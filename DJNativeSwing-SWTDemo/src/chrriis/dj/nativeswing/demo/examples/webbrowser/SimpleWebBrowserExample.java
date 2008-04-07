/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class SimpleWebBrowserExample extends JPanel {

  public SimpleWebBrowserExample() {
    super(new BorderLayout(0, 0));
    final JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar");
    final JCheckBox buttonBarCheckBox = new JCheckBox("Button Bar");
    final JCheckBox locationBarCheckBox = new JCheckBox("Location Bar");
    final JCheckBox statusBarCheckBox = new JCheckBox("Status Bar");
    JPanel webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser() {
      @Override
      public void setMenuBarVisible(boolean isMenuBarVisible) {
        super.setMenuBarVisible(isMenuBarVisible);
        menuBarCheckBox.setSelected(isMenuBarVisible);
      }
      @Override
      public void setButtonBarVisible(boolean isButtonBarVisible) {
        super.setButtonBarVisible(isButtonBarVisible);
        buttonBarCheckBox.setSelected(isButtonBarVisible);
      }
      @Override
      public void setLocationBarVisible(boolean isLocationBarVisible) {
        super.setLocationBarVisible(isLocationBarVisible);
        locationBarCheckBox.setSelected(isLocationBarVisible);
      }
      @Override
      public void setStatusBarVisible(boolean isStatusBarVisible) {
        super.setStatusBarVisible(isStatusBarVisible);
        statusBarCheckBox.setSelected(isStatusBarVisible);
      }
    };
    webBrowser.navigate("http://www.google.com");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    // Create the check boxes, to show/hide the various bars
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    menuBarCheckBox.setSelected(webBrowser.isMenuBarVisible());
    menuBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(menuBarCheckBox);
    buttonBarCheckBox.setSelected(webBrowser.isButtonBarVisible());
    buttonBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setButtonBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(buttonBarCheckBox);
    locationBarCheckBox.setSelected(webBrowser.isLocationBarVisible());
    locationBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setLocationBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(locationBarCheckBox);
    statusBarCheckBox.setSelected(webBrowser.isStatusBarVisible());
    statusBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setStatusBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(statusBarCheckBox);
    add(buttonPanel, BorderLayout.SOUTH);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SimpleWebBrowserExample(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
