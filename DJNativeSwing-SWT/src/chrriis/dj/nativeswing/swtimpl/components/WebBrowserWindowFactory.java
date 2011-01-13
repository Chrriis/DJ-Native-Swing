/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;

import chrriis.common.Utils;

public class WebBrowserWindowFactory {

  private static class WebBrowserFrame extends JFrame implements JWebBrowserWindow {

    private WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy;

    public WebBrowserFrame(WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy) {
      this.webBrowserWindowStrategy = webBrowserWindowStrategy;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void show() {
      boolean isLocationByPlatform = isLocationByPlatform();
      super.show();
      if(isLocationByPlatform) {
        WebBrowserWindowStrategy.adjustInScreen(this);
      }
    }

    public JWebBrowser getWebBrowser() {
      return webBrowserWindowStrategy.getWebBrowser();
    }

    public void setBarsVisible(boolean areBarsVisible) {
      webBrowserWindowStrategy.setBarsVisible(areBarsVisible);
    }

    public void setStatusBarVisible(boolean isStatusBarVisible) {
      webBrowserWindowStrategy.setStatusBarVisible(isStatusBarVisible);
    }

    public boolean isStatusBarVisisble() {
      return webBrowserWindowStrategy.isStatusBarVisisble();
    }

    public void setMenuBarVisible(boolean isMenuBarVisible) {
      webBrowserWindowStrategy.setMenuBarVisible(isMenuBarVisible);
    }

    public boolean isMenuBarVisisble() {
      return webBrowserWindowStrategy.isMenuBarVisisble();
    }

    public void setButtonBarVisible(boolean isButtonBarVisible) {
      webBrowserWindowStrategy.setButtonBarVisible(isButtonBarVisible);
    }

    public boolean isButtonBarVisisble() {
      return webBrowserWindowStrategy.isButtonBarVisisble();
    }

    public void setLocationBarVisible(boolean isLocationBarVisible) {
      webBrowserWindowStrategy.setLocationBarVisible(isLocationBarVisible);
    }

    public boolean isLocationBarVisisble() {
      return webBrowserWindowStrategy.isLocationBarVisisble();
    }

  }

  private static class WebBrowserDialog extends JDialog implements JWebBrowserWindow {

    private WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy;

    public WebBrowserDialog(WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy, Frame parentWindow) {
      super(parentWindow);
      this.webBrowserWindowStrategy = webBrowserWindowStrategy;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public WebBrowserDialog(WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy, Dialog parentWindow) {
      super(parentWindow);
      this.webBrowserWindowStrategy = webBrowserWindowStrategy;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void show() {
      boolean isLocationByPlatform = isLocationByPlatform();
      super.show();
      if(isLocationByPlatform) {
        WebBrowserWindowStrategy.adjustInScreen(this);
      }
    }

    @Override
    public void setIconImage(Image image) {
      if(Utils.IS_JAVA_6_OR_GREATER) {
        super.setIconImage(image);
      }
    }

    public JWebBrowser getWebBrowser() {
      return webBrowserWindowStrategy.getWebBrowser();
    }

    public void setBarsVisible(boolean areBarsVisible) {
      webBrowserWindowStrategy.setBarsVisible(areBarsVisible);
    }

    public void setStatusBarVisible(boolean isStatusBarVisible) {
      webBrowserWindowStrategy.setStatusBarVisible(isStatusBarVisible);
    }

    public boolean isStatusBarVisisble() {
      return webBrowserWindowStrategy.isStatusBarVisisble();
    }

    public void setMenuBarVisible(boolean isMenuBarVisible) {
      webBrowserWindowStrategy.setMenuBarVisible(isMenuBarVisible);
    }

    public boolean isMenuBarVisisble() {
      return webBrowserWindowStrategy.isMenuBarVisisble();
    }

    public void setButtonBarVisible(boolean isButtonBarVisible) {
      webBrowserWindowStrategy.setButtonBarVisible(isButtonBarVisible);
    }

    public boolean isButtonBarVisisble() {
      return webBrowserWindowStrategy.isButtonBarVisisble();
    }

    public void setLocationBarVisible(boolean isLocationBarVisible) {
      webBrowserWindowStrategy.setLocationBarVisible(isLocationBarVisible);
    }

    public boolean isLocationBarVisisble() {
      return webBrowserWindowStrategy.isLocationBarVisisble();
    }

  }

  /**
   * Create a web browser window, as a frame, with a given web browser.
   * @param webBrowser the web browser.
   * @return the web browser window that was created.
   */
  public static JWebBrowserWindow create(JWebBrowser webBrowser) {
    return create(null, webBrowser);
  }

  /**
   * Create a web browser window, as a dialog or a frame depending on whether a parent window is specified, with a given web browser.
   * @param parentWindow the parent window, which can be null.
   * @param webBrowser the web browser.
   * @return the web browser window that was created.
   */
  public static JWebBrowserWindow create(Window parentWindow, JWebBrowser webBrowser) {
    final WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy = new WebBrowserWindowStrategy(webBrowser);
    JWebBrowserWindow webBrowserWindow = createWindow(webBrowserWindowStrategy, parentWindow, parentWindow != null);
    webBrowser.getWebBrowserDecorator().configureForWebBrowserWindow(webBrowserWindow);
    ((RootPaneContainer)webBrowserWindow).getContentPane().add(webBrowser, BorderLayout.CENTER);
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    size.width = size.width * 80 / 100;
    size.height = size.height * 80 / 100;
    Window window = (Window)webBrowserWindow;
    window.setSize(size);
    window.setLocationByPlatform(true);
    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        webBrowserWindowStrategy.getWebBrowser().requestFocus();
      }
    });
    return webBrowserWindow;
  }

  private static JWebBrowserWindow createWindow(WebBrowserWindowFactory.WebBrowserWindowStrategy webBrowserWindowStrategy, Window parentWindow, boolean isDialog) {
    JWebBrowserWindow window;
    if(isDialog) {
      if(parentWindow instanceof Frame) {
        window = new WebBrowserDialog(webBrowserWindowStrategy, (Frame)parentWindow);
      } else {
        window = new WebBrowserDialog(webBrowserWindowStrategy, (Dialog)parentWindow);
      }
    } else {
      window = new WebBrowserFrame(webBrowserWindowStrategy);
    }
    return window;
  }

  private static class WebBrowserWindowStrategy {

    private JWebBrowser webBrowser;

    public WebBrowserWindowStrategy(JWebBrowser webBrowser) {
      this.webBrowser = webBrowser;
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

    public void setButtonBarVisible(boolean isButtonBarVisible) {
      webBrowser.setButtonBarVisible(isButtonBarVisible);
    }

    public boolean isButtonBarVisisble() {
      return webBrowser.isButtonBarVisible();
    }

    public void setLocationBarVisible(boolean isLocationBarVisible) {
      webBrowser.setLocationBarVisible(isLocationBarVisible);
    }

    public boolean isLocationBarVisisble() {
      return webBrowser.isLocationBarVisible();
    }

    private static void adjustInScreen(Window window) {
      GraphicsConfiguration gc = window.getGraphicsConfiguration();
      Rectangle gcBounds = gc.getBounds();
      Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
      gcBounds.x += screenInsets.left;
      gcBounds.width -= screenInsets.left + screenInsets.right;
      gcBounds.y += screenInsets.top;
      gcBounds.height -= screenInsets.top + screenInsets.bottom;
      Rectangle bounds = window.getBounds();
      if(gcBounds.x + gcBounds.width < bounds.x + bounds.width) {
        bounds.x = gcBounds.x + gcBounds.width - bounds.width;
      }
      if(bounds.x < gcBounds.x) {
        bounds.x = gcBounds.x;
      }
      if(gcBounds.y + gcBounds.height < bounds.y + bounds.height) {
        bounds.y = gcBounds.y + gcBounds.height - bounds.height;
      }
      if(bounds.y < gcBounds.y) {
        bounds.y = gcBounds.y;
      }
      if(!window.getBounds().equals(bounds)) {
        window.setBounds(bounds);
      }
    }

  }

}