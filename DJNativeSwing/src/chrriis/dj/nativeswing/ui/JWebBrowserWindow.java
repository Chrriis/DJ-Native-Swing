/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;

/**
 * @author Christopher Deckers
 */
public class JWebBrowserWindow extends JFrame {

  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JWebBrowserWindow.class.getPackage().getName().replace('.', '/') + "/resource/WebBrowser");
  
  private JWebBrowser webBrowser;
  
  public JWebBrowserWindow() {
    this(new JWebBrowser());
  }
  
  public JWebBrowserWindow(JWebBrowser webBrowser) {
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
        setTitle(mf.format(new Object[] {e.getWebBrowser().getTitle()}));
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
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void show() {
    super.show();
    webBrowser.requestFocus();
  }
  
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  public void setBarsVisible(boolean areBarsVisible) {
    webBrowser.setBarsVisible(areBarsVisible);
  }
  
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    webBrowser.setStatusBarVisible(isStatusBarVisible);
  }
  
  public boolean isStatusBarVisisble() {
    return webBrowser.isStatusBarVisible();
  }
  
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    webBrowser.setMenuBarVisible(isMenuBarVisible);
  }
  
  public boolean isMenuBarVisisble() {
    return webBrowser.isMenuBarVisible();
  }
  
  public void setButtonVisible(boolean isButtonBarVisible) {
    webBrowser.setButtonBarVisible(isButtonBarVisible);
  }
  
  public boolean isButtonBarVisisble() {
    return webBrowser.isButtonBarVisible();
  }
  
  public void setAddressBarVisible(boolean isAddressBarVisible) {
    webBrowser.setAddressBarVisible(isAddressBarVisible);
  }
  
  public boolean isAddressBarVisisble() {
    return webBrowser.isAddressBarVisible();
  }
  
}
