/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;

import javax.swing.JPanel;


/**
 * A web browser decorator is a component that wraps the rendering component and is added to the web browser
 * to provide the various button bars, fields and menus.<br>
 * Generally, it is not needed to create a custom subclass: it is usually enough to subclass a default web
 * browser decorator and override certain methods.
 * @author Christopher Deckers
 */
public abstract class WebBrowserDecorator extends JPanel {

  public WebBrowserDecorator() {
    super(new BorderLayout());
  }

  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  public abstract void setStatusBarVisible(boolean isStatusBarVisible);

  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  public abstract boolean isStatusBarVisible();

  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  public abstract void setMenuBarVisible(boolean isMenuBarVisible);

  /**
   * Indicate whether the menu bar is visible.
   * @return true if the menu bar is visible.
   */
  public abstract boolean isMenuBarVisible();

  /**
   * Set whether the button bar is visible.
   * @param isButtonBarVisible true if the button bar should be visible, false otherwise.
   */
  public abstract void setButtonBarVisible(boolean isButtonBarVisible);

  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  public abstract boolean isButtonBarVisible();

  /**
   * Set whether the location bar is visible.
   * @param isLocationBarVisible true if the location bar should be visible, false otherwise.
   */
  public abstract void setLocationBarVisible(boolean isLocationBarVisible);

  /**
   * Indicate whether the location bar is visible.
   * @return true if the location bar is visible.
   */
  public abstract boolean isLocationBarVisible();

  /**
   * Configure the decorator for integration of the web browser in a web browser window.
   */
  public abstract void configureForWebBrowserWindow(JWebBrowserWindow webBrowserWindow);

}
