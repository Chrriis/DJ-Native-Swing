/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowAdapter;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.CommandMessage;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowOpeningEvent;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowser extends NativeComponent {

  private static final String COMMAND_PREFIX = "command://";
  
  private static class CMJ_closeWindow extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).windowClosing(e);
        }
      }
      Window window = SwingUtilities.getWindowAncestor(webBrowser);
      if(window instanceof JWebBrowserWindow) {
        window.dispose();
      }
      return null;
    }
  }
  
  private static class CMJ_showWindow extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      BrowserAttributes browserAttributes = (BrowserAttributes)args[0];
      JWebBrowser jWebBrowser = new JWebBrowser();
      jWebBrowser.setAddressBarVisible(browserAttributes.hasAddressBar);
      jWebBrowser.setMenuBarVisible(browserAttributes.hasMenuBar);
      jWebBrowser.setStatusBarVisible(browserAttributes.hasStatusBar);
      jWebBrowser.setButtonBarVisible(browserAttributes.hasToolBar);
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserWindowOpeningEvent e = null;
      for(int i=listeners.length-2; i>=0 && jWebBrowser != null; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserWindowOpeningEvent(webBrowser, jWebBrowser, browserAttributes.url, browserAttributes.location, browserAttributes.size);
          }
          ((WebBrowserListener)listeners[i + 1]).windowOpening(e);
          jWebBrowser = e.isConsumed()? null: e.getNewWebBrowser();
        }
      }
      if(jWebBrowser != null) {
        if(SwingUtilities.getWindowAncestor(jWebBrowser) == null) {
          JWebBrowserWindow webBrowserWindow = new JWebBrowserWindow(jWebBrowser);
          if(browserAttributes.size != null) {
            webBrowserWindow.setSize(browserAttributes.size);
          }
          if(browserAttributes.location != null) {
            webBrowserWindow.setLocation(browserAttributes.location);
          }
          webBrowserWindow.setVisible(true);
        }
        if(browserAttributes.url != null) {
          jWebBrowser.setURL(browserAttributes.url);
        }
      }
      return null;
    }
  }

  private static class CMJ_urlChanged extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChanged(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_commandReceived extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      String command = (String)args[0];
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).commandReceived(e, command);
        }
      }
      return null;
    }
  }

  private static class CMJ_urlChanging extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return false;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      boolean isNavigating = true;
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChanging(e);
          isNavigating &= !e.isConsumed();
        }
      }
      return isNavigating;
    }
  }
      
  private static class CMJ_urlChangeCanceled extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChangeCanceled(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_updateTitle extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.title = (String)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).titleChanged(e);
        }
      }
      return null;
    }
  }
  
  private static class CMJ_updateStatus extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.status = (String)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).statusChanged(e);
        }
      }
      return null;
    }
  }
  
  private static class CMJ_updateProgress extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.pageLoadingProgressValue = (Integer)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).loadingProgressChanged(e);
        }
      }
      return null;
    }
  }
  
  private static class BrowserAttributes implements Serializable {
    protected Point location;
    protected Dimension size;
    protected boolean hasMenuBar = true;
    protected boolean hasToolBar = true;
    protected boolean hasAddressBar = true;
    protected boolean hasStatusBar = true;
    protected String url;
  }

  
  protected static Control createControl(Shell shell) {
    int style = SWT.NONE;
    if("mozilla".equals(System.getProperty("dj.nativeswing.webbrowser"))) {
      style |= SWT.MOZILLA;
    }
    final Browser browser = new Browser(shell, style);
    browser.addCloseWindowListener(new CloseWindowListener() {
      public void close(WindowEvent event) {
        asyncExec(browser, new CMJ_closeWindow());
      }
    });
    browser.addOpenWindowListener(new OpenWindowListener() {
      public void open(WindowEvent e) {
        // This forces the user to open it himself
        e.required = true;
        final Display display = NativeInterfaceHandler.getDisplay();
        final Shell shell = new Shell(display);
        final Browser browser_ = new Browser(shell, browser.getStyle());
        e.browser = browser_;
        final BrowserAttributes browserAttributes = new BrowserAttributes();
        browser_.addVisibilityWindowListener(new VisibilityWindowAdapter() {
          @Override
          public void show(WindowEvent e) {
            ((Browser)e.widget).removeVisibilityWindowListener(this);
            browserAttributes.location = e.location == null? null: new Point(e.location.x, e.location.y);
            browserAttributes.size = e.size == null? null: new Dimension(e.size.x, e.size.y);
            browserAttributes.hasMenuBar = e.menuBar;
            browserAttributes.hasToolBar = e.toolBar;
            browserAttributes.hasAddressBar = e.addressBar;
            browserAttributes.hasStatusBar = e.statusBar;
            display.asyncExec(new Runnable() {
              public void run() {
                asyncExec(browser, new CMJ_showWindow(), browserAttributes);
                shell.dispose();
              }
            });
          }
        });
        browser_.addLocationListener(new LocationAdapter() {
          public void changing(LocationEvent e) {
            ((Browser)e.widget).removeLocationListener(this);
            browserAttributes.url = e.location;
            e.doit = false;
          }
        });
      }
    });
    browser.addLocationListener(new LocationListener() {
      public void changed(LocationEvent e) {
        asyncExec(browser, new CMJ_urlChanged(), e.location, e.top);
      }
      @SuppressWarnings("deprecation")
      public void changing(LocationEvent e) {
        final String location = e.location;
        if(location.startsWith(COMMAND_PREFIX)) {
          e.doit = false;
          String command = location.substring(COMMAND_PREFIX.length());
          if(command.endsWith("/")) {
            command = command.substring(0, command.length() - 1);
          }
          try {
            command = URLDecoder.decode(command, "UTF-8");
          } catch(Exception ex) {
            command = URLDecoder.decode(command);
            ex.printStackTrace();
          }
          asyncExec(browser, new CMJ_commandReceived(), command);
          return;
        }
        if(location.startsWith("javascript:")) {
          return;
        }
        e.doit = (Boolean)syncExec(browser, new CMJ_urlChanging(), location, e.top);
        if(!e.doit) {
          asyncExec(browser, new CMJ_urlChangeCanceled(), location, e.top);
        }
      }
    });
    browser.addTitleListener(new TitleListener() {
      public void changed(TitleEvent e) {
        asyncExec(browser, new CMJ_updateTitle(), e.title);
      }
    });
    browser.addStatusTextListener(new StatusTextListener() {
      public void changed(StatusTextEvent e) {
        asyncExec(browser, new CMJ_updateStatus(), e.text);
      }
    });
    browser.addProgressListener(new ProgressListener() {
      public void changed(ProgressEvent e) {
        int loadingProgressValue = e.total == 0? 100: e.current * 100 / e.total;
        asyncExec(browser, new CMJ_updateProgress(), loadingProgressValue);
      }
      public void completed(ProgressEvent progressevent) {
        asyncExec(browser, new CMJ_updateProgress(), 100);
      }
    });
    return browser;
  }

  private Reference<JWebBrowser> webBrowser;
  
  public NativeWebBrowser(JWebBrowser webBrowser) {
    this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
  }

  private static class CMN_clearSessions extends CommandMessage {
    @Override
    public Object run() {
      Browser.clearSessions();
      return null;
    }
  }

  public static void clearSessions() {
    new CMN_clearSessions().asyncExec();
  }

  private static class CMN_getURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).getUrl();
    }
  }
  
  public String getURL() {
    return (String)run(new CMN_getURL());
  }
  
  private static class CMN_setURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setUrl((String)args[0]);
    }
  }
  
  public boolean setURL(String url) {
    return Boolean.TRUE.equals(run(new CMN_setURL(), url));
  }
  
  private static class CMN_setText extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setText((String)args[0]);
    }
  }
  
  public boolean setText(String html) {
    return Boolean.TRUE.equals(run(new CMN_setText(), html));
  }
  
  private static class CMN_execute extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).execute((String)args[0]);
    }
  }
  
  public boolean execute(String js) {
    return Boolean.TRUE.equals(run(new CMN_execute(), js));
  }
  
  private static class CMN_stop extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).stop();
      return null;
    }
  }
  
  public void stop() {
    run(new CMN_stop());
  }
  
  private static class CMN_refresh extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).refresh();
      return null;
    }
  }
  
  public void refresh() {
    run(new CMN_refresh());
  }
  
  private static class CMN_isBackEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isBackEnabled();
    }
  }
  
  public boolean isBackEnabled() {
    return Boolean.TRUE.equals(run(new CMN_isBackEnabled()));
  }
  
  private static class CMN_back extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).back();
    }
  }
  
  public boolean back() {
    return Boolean.TRUE.equals(run(new CMN_back()));
  }
  
  private static class CMN_forward extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).forward();
    }
  }
  
  private static class CMN_isForwardEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isForwardEnabled();
    }
  }
  
  public boolean isForwardEnabled() {
    return Boolean.TRUE.equals(run(new CMN_isForwardEnabled()));
  }
  
  public boolean forward() {
    return Boolean.TRUE.equals(run(new CMN_forward()));
  }
  
  private String status;

  public String getStatus() {
    return status == null? "": status;
  }
  
  private String title;

  public String getTitle() {
    return title == null? "": title;
  }
  
  private int pageLoadingProgressValue = 100;
  
  /**
   * @return A value between 0 and 100 indicating the current loading progress.
   */
  public int getPageLoadingProgressValue() {
    return pageLoadingProgressValue;
  }
  
  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
  }
  
  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
  }
  
  public WebBrowserListener[] getWebBrowserListeners() {
    return listenerList.getListeners(WebBrowserListener.class);
  }
  
}
