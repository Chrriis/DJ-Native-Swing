/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * A web browser window.
 * @author Christopher Deckers
 */
public class JWebBrowserWindow extends JFrame {

  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JWebBrowserWindow.class.getPackage().getName().replace('.', '/') + "/resource/WebBrowser");
  
  private JWebBrowser webBrowser;
  
  /**
   * Create a web browser window.
   */
  public JWebBrowserWindow() {
    this(new JWebBrowser());
  }
  
  /**
   * Create a web browser window with a given web browser.
   * @param webBrowser the web browser.
   */
  public JWebBrowserWindow(JWebBrowser webBrowser) {
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.webBrowser = webBrowser;
    JMenu fileMenu = webBrowser.getFileMenu();
    fileMenu.addSeparator();
    JMenuItem fileCloseMenuItem = new JMenuItem(RESOURCES.getString("FileCloseMenu"));
    fileCloseMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    fileMenu.add(fileCloseMenuItem);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void titleChanged(WebBrowserEvent e) {
        MessageFormat mf = new MessageFormat(RESOURCES.getString("BrowserTitle"));
        setTitle(mf.format(new Object[] {e.getWebBrowser().getPageTitle()}));
      }
    });
    String value = RESOURCES.getString("BrowserIcon");
    if(value.length() > 0) {
      setIconImage(new ImageIcon(JWebBrowserWindow.class.getResource(value)).getImage());
    }
    getContentPane().add(webBrowser, BorderLayout.CENTER);
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    size.width = size.width * 80 / 100;
    size.height = size.height * 80 / 100;
    setSize(size);
    setLocationByPlatform(true);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        JWebBrowserWindow.this.webBrowser.requestFocus();
      }
    });
  }
  
  /**
   * Get the web browser that this window presents.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  /**
   * Show or hide all the bars at once.
   * @param areBarsVisible true to show all bars, false to hide them all.
   */
  public void setBarsVisible(boolean areBarsVisible) {
    webBrowser.setBarsVisible(areBarsVisible);
  }
  
  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    webBrowser.setStatusBarVisible(isStatusBarVisible);
  }
  
  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  public boolean isStatusBarVisisble() {
    return webBrowser.isStatusBarVisible();
  }
  
  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    webBrowser.setMenuBarVisible(isMenuBarVisible);
  }
  
  /**
   * Indicate whether the menu bar is visible.
   * @return true if the menu bar is visible.
   */
  public boolean isMenuBarVisisble() {
    return webBrowser.isMenuBarVisible();
  }
  
  /**
   * Set whether the button bar is visible.
   * @param isButtonBarVisible true if the button bar should be visible, false otherwise.
   */
  public void setButtonBarVisible(boolean isButtonBarVisible) {
    webBrowser.setButtonBarVisible(isButtonBarVisible);
  }
  
  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  public boolean isButtonBarVisisble() {
    return webBrowser.isButtonBarVisible();
  }
  
  /**
   * Set whether the location bar is visible.
   * @param isLocationBarVisible true if the location bar should be visible, false otherwise.
   */
  public void setLocationBarVisible(boolean isLocationBarVisible) {
    webBrowser.setLocationBarVisible(isLocationBarVisible);
  }
  
  /**
   * Indicate whether the location bar is visible.
   * @return true if the location bar is visible.
   */
  public boolean isLocationBarVisisble() {
    return webBrowser.isLocationBarVisible();
  }
  
}
