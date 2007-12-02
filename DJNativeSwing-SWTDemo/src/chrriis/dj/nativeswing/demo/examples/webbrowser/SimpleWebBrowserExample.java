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
import javax.swing.JPanel;

import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class SimpleWebBrowserExample extends JPanel {

  public SimpleWebBrowserExample() {
    super(new BorderLayout(0, 0));
    final JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar");
    final JCheckBox buttonBarCheckBox = new JCheckBox("Button Bar");
    final JCheckBox addressBarCheckBox = new JCheckBox("Address Bar");
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
      public void setAddressBarVisible(boolean isAddressBarVisible) {
        super.setAddressBarVisible(isAddressBarVisible);
        addressBarCheckBox.setSelected(isAddressBarVisible);
      }
      @Override
      public void setStatusBarVisible(boolean isStatusBarVisible) {
        super.setStatusBarVisible(isStatusBarVisible);
        statusBarCheckBox.setSelected(isStatusBarVisible);
      }
    };
    webBrowser.setURL("http://www.google.com");
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
    addressBarCheckBox.setSelected(webBrowser.isAddressBarVisible());
    addressBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setAddressBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(addressBarCheckBox);
    statusBarCheckBox.setSelected(webBrowser.isStatusBarVisible());
    statusBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        webBrowser.setStatusBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(statusBarCheckBox);
    add(buttonPanel, BorderLayout.SOUTH);
  }
  
}
