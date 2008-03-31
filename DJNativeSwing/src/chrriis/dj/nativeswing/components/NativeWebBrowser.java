/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
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
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.CommandMessage;
import chrriis.dj.nativeswing.ControlCommandMessage;
import chrriis.dj.nativeswing.NativeComponent;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowser extends NativeComponent {

  private static final String COMMAND_PREFIX = "command://";
  
  private static class CMJ_closeWindow extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      JWebBrowserWindow browserWindow = webBrowser.getWebBrowserWindow();
      if(browserWindow != null) {
        browserWindow.dispose();
      }
      return null;
    }
  }
  
  private static class CMJ_createWindow extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      JWebBrowser jWebBrowser = new JWebBrowser();
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserWindowWillOpenEvent e = null;
      for(int i=listeners.length-2; i>=0 && jWebBrowser != null; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserWindowWillOpenEvent(webBrowser, jWebBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).windowWillOpen(e);
          jWebBrowser = e.isConsumed()? null: e.getNewWebBrowser();
        }
      }
      if(jWebBrowser == null) {
        return null;
      }
      if(!jWebBrowser.isNativePeerInitialized()) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(jWebBrowser);
        if(windowAncestor == null) {
          final JWebBrowserWindow webBrowserWindow = new JWebBrowserWindow(jWebBrowser);
          windowAncestor = webBrowserWindow;
        } else {
        }
        jWebBrowser.getNativeComponent().initializeNativePeer();
      }
      return ((NativeWebBrowser)jWebBrowser.getNativeComponent()).getComponentID();
    }
  }

  private static class CMJ_showWindow extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      final JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      int componentID = (Integer)args[0];
      final JWebBrowser newWebBrowser = ((NativeWebBrowser)getRegistry().get(componentID)).webBrowser.get();
      newWebBrowser.setMenuBarVisible((Boolean)args[1]);
      newWebBrowser.setButtonBarVisible((Boolean)args[2]);
      newWebBrowser.setAddressBarVisible((Boolean)args[3]);
      newWebBrowser.setStatusBarVisible((Boolean)args[4]);
      Point location = (Point)args[5];
      Dimension size = (Dimension)args[6];
      JWebBrowserWindow browserWindow = newWebBrowser.getWebBrowserWindow();;
      if(browserWindow != null) {
        if(size != null) {
          browserWindow.setSize(size);
        }
        if(location != null) {
          browserWindow.setLocation(location);
        }
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserWindowOpeningEvent e = null;
      for(int i=listeners.length-2; i>=0 && newWebBrowser != null; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserWindowOpeningEvent(webBrowser, newWebBrowser, location, size);
          }
          ((WebBrowserListener)listeners[i + 1]).windowOpening(e);
        }
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JWebBrowserWindow browserWindow = newWebBrowser.getWebBrowserWindow();
          if(browserWindow != null && !newWebBrowser.getNativeComponent().isNativePeerDisposed()) {
            browserWindow.setVisible(true);
          }
        }
      });
      return null;
    }
  }
  
  private static class CMJ_urlChanged extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      String command = (String)args[0];
      String[] arguments = (String[])args[1];
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).commandReceived(e, command, arguments);
        }
      }
      return null;
    }
  }

  private static class CMJ_urlChanging extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
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
  
  protected static Control createControl(Shell shell) {
    int style = SWT.NONE;
    if("mozilla".equals(System.getProperty("dj.nativeswing.ui.webbrowser"))) {
      style |= SWT.MOZILLA;
    }
    final Browser browser = new Browser(shell, style);
    browser.addCloseWindowListener(new CloseWindowListener() {
      public void close(WindowEvent event) {
        new CMJ_closeWindow().asyncExec(browser);
      }
    });
    browser.addOpenWindowListener(new OpenWindowListener() {
      public void open(WindowEvent e) {
        // This forces the user to open it himself
        e.required = true;
        final Integer componentID = (Integer)new CMJ_createWindow().syncExec(browser);
        final Browser newWebBrowser;
        final boolean isDisposed;
        if(componentID == null) {
          isDisposed = true;
          Shell shell = new Shell();
          newWebBrowser = new Browser(shell, browser.getStyle());
        } else {
          isDisposed = false;
          newWebBrowser = (Browser)NativeComponent.getRegistry().get(componentID);
        }
        e.browser = newWebBrowser;
        newWebBrowser.addVisibilityWindowListener(new VisibilityWindowAdapter() {
          @Override
          public void show(WindowEvent e) {
            Browser browser = (Browser)e.widget;
            if(isDisposed) {
              final Shell shell = browser.getShell();
              e.display.asyncExec(new Runnable() {
                public void run() {
                  shell.close();
                }
              });
            } else {
              (browser).removeVisibilityWindowListener(this);
              new CMJ_showWindow().asyncExec(newWebBrowser, componentID, e.menuBar, e.toolBar, e.addressBar, e.statusBar, e.location == null? null: new Point(e.location.x, e.location.y), e.size == null? null: new Dimension(e.size.x, e.size.y));
            }
          }
        });
      }
    });
    browser.addLocationListener(new LocationListener() {
      public void changed(LocationEvent e) {
        new CMJ_urlChanged().asyncExec(browser, e.location, e.top);
      }
      public void changing(LocationEvent e) {
        final String location = e.location;
        if(location.startsWith(COMMAND_PREFIX)) {
          e.doit = false;
          String query = location.substring(COMMAND_PREFIX.length());
          if(query.endsWith("/")) {
            query = query.substring(0, query.length() - 1);
          }
          List<String> queryElementList = new ArrayList<String>();
          StringTokenizer st = new StringTokenizer(query, "&", true);
          String lastToken = null;
          while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if("&".equals(token)) {
              if(lastToken == null) {
                queryElementList.add("");
              }
              lastToken = null;
            } else {
              lastToken = token;
              queryElementList.add(Utils.decodeURL(token));
            }
          }
          if(lastToken == null) {
            queryElementList.add("");
          }
          String command = queryElementList.isEmpty()? "": queryElementList.remove(0);
          String[] args = queryElementList.toArray(new String[0]);
          new CMJ_commandReceived().asyncExec(browser, command, args);
          return;
        }
        if(location.startsWith("javascript:")) {
          return;
        }
        e.doit = (Boolean)new CMJ_urlChanging().syncExec(browser, location, e.top);
        if(!e.doit) {
          new CMJ_urlChangeCanceled().asyncExec(browser, location, e.top);
        }
      }
    });
    browser.addTitleListener(new TitleListener() {
      public void changed(TitleEvent e) {
        new CMJ_updateTitle().asyncExec(browser, e.title);
      }
    });
    browser.addStatusTextListener(new StatusTextListener() {
      public void changed(StatusTextEvent e) {
        new CMJ_updateStatus().asyncExec(browser, e.text);
      }
    });
    browser.addProgressListener(new ProgressListener() {
      public void changed(ProgressEvent e) {
        int loadingProgressValue = e.total == 0? 100: e.current * 100 / e.total;
        new CMJ_updateProgress().asyncExec(browser, loadingProgressValue);
      }
      public void completed(ProgressEvent progressevent) {
        new CMJ_updateProgress().asyncExec(browser, 100);
      }
    });
    return browser;
  }

  private Reference<JWebBrowser> webBrowser;
  
  public NativeWebBrowser(JWebBrowser webBrowser) {
    this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
  }

  private static class CMN_clearSessionCookies extends CommandMessage {
    @Override
    public Object run() {
      Browser.clearSessions();
      return null;
    }
  }

  public static void clearSessionCookies() {
    new CMN_clearSessionCookies().asyncExec();
  }

  private static class CMN_getURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).getUrl();
    }
  }
  
  public String getURL() {
    return (String)runSync(new CMN_getURL());
  }
  
  private static class CMN_setURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setUrl((String)args[0]);
    }
  }
  
  public boolean setURL(String url) {
    return Boolean.TRUE.equals(runSync(new CMN_setURL(), url));
  }
  
  private static class CMN_getHTMLContent extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).getText();
    }
  }
  
  public String getHTMLContent() {
    return (String)runSync(new CMN_getHTMLContent());
  }
  
  private static class CMN_setHTMLContent extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setText((String)args[0]);
    }
  }
  
  public boolean setHTMLContent(String html) {
    return Boolean.TRUE.equals(runSync(new CMN_setHTMLContent(), html));
  }
  
  private static class CMN_executeJavascript extends ControlCommandMessage {
    private static Pattern JAVASCRIPT_LINE_COMMENT_PATTERN = Pattern.compile("^\\s*//.*$", Pattern.MULTILINE);
    @Override
    public Object run() {
      String script = (String)args[0];
      // Remove line comments, because it does not work properly on Mozilla.
      script = JAVASCRIPT_LINE_COMMENT_PATTERN.matcher(script).replaceAll("");
      return ((Browser)getControl()).execute(script);
    }
  }
  
  public boolean executeJavascriptAndWait(String script) {
    return Boolean.TRUE.equals(runSync(new CMN_executeJavascript(), script));
  }
  
  public void executeJavascript(String script) {
    runAsync(new CMN_executeJavascript(), script);
  }
  
  private static class CMN_stop extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).stop();
      return null;
    }
  }
  
  public void stop() {
    runAsync(new CMN_stop());
  }
  
  private static class CMN_refresh extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).refresh();
      return null;
    }
  }
  
  public void refresh() {
    runAsync(new CMN_refresh());
  }
  
  private static class CMN_isGoBackEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isBackEnabled();
    }
  }
  
  public boolean isGoBackEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isGoBackEnabled()));
  }
  
  private static class CMN_goBack extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).back();
    }
  }
  
  public void goBack() {
    runAsync(new CMN_goBack());
  }
  
  private static class CMN_isGoForwardEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isForwardEnabled();
    }
  }
  
  public boolean isGoForwardEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isGoForwardEnabled()));
  }
  
  private static class CMN_goForward extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).forward();
    }
  }
  
  public void goForward() {
    runAsync(new CMN_goForward());
  }
  
  private String status;

  public String getStatusText() {
    return status == null? "": status;
  }
  
  private String title;

  public String getPageTitle() {
    return title == null? "": title;
  }
  
  private int pageLoadingProgressValue = 100;
  
  /**
   * @return a value between 0 and 100 indicating the current loading progress.
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
  
  @Override
  protected Component createEmbeddableComponent() {
    return super.createEmbeddableComponent();
  }
  
  @Override
  protected void disposeNativePeer() {
    super.disposeNativePeer();
  }
  
}
