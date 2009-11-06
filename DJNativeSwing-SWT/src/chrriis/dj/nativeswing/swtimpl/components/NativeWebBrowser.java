/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
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
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowser extends NativeComponent {

  public static final String COMMAND_FUNCTION = "sendNSCommand";
  public static final String COMMAND_LOCATION_PREFIX = "command://";
  public static final String COMMAND_STATUS_PREFIX = "scommand://";

  private static class CMJ_closeWindow extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      JWebBrowser jWebBrowser;
      if(nativeWebBrowser.isXULRunnerRuntime()) {
        jWebBrowser = new JWebBrowser(JWebBrowser.useXULRunnerRuntime());
      } else {
        jWebBrowser = new JWebBrowser();
      }
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
          windowAncestor = new JWebBrowserWindow(jWebBrowser);
        } else {
        }
        jWebBrowser.getNativeComponent().initializeNativePeer();
      }
      return ((NativeWebBrowser)jWebBrowser.getNativeComponent()).getComponentID();
    }
  }

  private static class CMJ_showWindow extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      final JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      int componentID = (Integer)args[0];
      final JWebBrowser newWebBrowser = ((NativeWebBrowser)getNativeComponentRegistry().get(componentID)).webBrowser.get();
      newWebBrowser.setMenuBarVisible((Boolean)args[1]);
      newWebBrowser.setButtonBarVisible((Boolean)args[2]);
      newWebBrowser.setLocationBarVisible((Boolean)args[3]);
      newWebBrowser.setStatusBarVisible((Boolean)args[4]);
      Point location = (Point)args[5];
      Dimension size = (Dimension)args[6];
      JWebBrowserWindow browserWindow = newWebBrowser.getWebBrowserWindow();
      if(browserWindow != null) {
        if(size != null) {
          browserWindow.validate();
          Dimension windowSize = browserWindow.getSize();
          Dimension webBrowserSize = browserWindow.getWebBrowser().getEmbeddableComponent().getSize();
          if(size.width > 0) {
            windowSize.width -= webBrowserSize.width;
            windowSize.width += size.width;
          }
          if(size.height > 0) {
            windowSize.height -= webBrowserSize.height;
            windowSize.height += size.height;
          }
          browserWindow.setSize(windowSize);
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
      new Thread() {
        @Override
        public void run() {
          try {
            sleep(600);
          } catch(Exception e) {
          }
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JWebBrowserWindow browserWindow = newWebBrowser.getWebBrowserWindow();
              if(browserWindow != null && !newWebBrowser.getNativeComponent().isNativePeerDisposed()) {
                browserWindow.setVisible(true);
              }
            }
          });
        }
      }.start();
      return null;
    }
  }

  private static class CMJ_locationChanged extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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
          ((WebBrowserListener)listeners[i + 1]).locationChanged(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_commandReceived extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      String command = (String)args[0];
      Object[] arguments = (Object[])args[1];
      boolean isInternal = command.startsWith("[Chrriis]");
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          WebBrowserListener webBrowserListener = (WebBrowserListener)listeners[i + 1];
          if(!isInternal || webBrowserListener.getClass().getName().startsWith("chrriis.")) {
            webBrowserListener.commandReceived(e, command, arguments);
          }
        }
      }
      return null;
    }
  }

  private static class CMJ_locationChanging extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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
          ((WebBrowserListener)listeners[i + 1]).locationChanging(e);
          isNavigating &= !e.isConsumed();
        }
      }
      return isNavigating;
    }
  }

  private static class CMJ_locationChangeCanceled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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
          ((WebBrowserListener)listeners[i + 1]).locationChangeCanceled(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_updateTitle extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
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

  private static class CMJ_updateLoadingProgress extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.loadingProgress = (Integer)args[0];
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

  private boolean isXULRunnerRuntime;

  public boolean isXULRunnerRuntime() {
    return isXULRunnerRuntime;
  }

  private String xulRunnerHome;

  @Override
  protected Object[] getNativePeerCreationParameters() {
    return new Object[] {xulRunnerHome, isXULRunnerRuntime};
  }

  protected static Control createControl(Shell shell, Object[] parameters) {
    String xulRunnerPath = (String)parameters[0];
    if(xulRunnerPath != null) {
      System.setProperty("org.eclipse.swt.browser.XULRunnerPath", xulRunnerPath);
    } else {
      xulRunnerPath = System.getProperty("org.eclipse.swt.browser.XULRunnerPath");
      if(xulRunnerPath == null) {
        xulRunnerPath = System.getenv("XULRUNNER_HOME");
        if(xulRunnerPath != null) {
          System.setProperty("org.eclipse.swt.browser.XULRunnerPath", xulRunnerPath);
        }
      }
    }
    int style = SWT.NONE;
    if(((Boolean)parameters[1])) {
      style |= SWT.MOZILLA;
    }
    final Browser browser = new Browser(shell, style);
    configureBrowserFunction(browser);
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
          configureBrowserFunction(newWebBrowser);
        } else {
          isDisposed = false;
          newWebBrowser = (Browser)NativeComponent.getControlRegistry().get(componentID);
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
              browser.removeVisibilityWindowListener(this);
              new CMJ_showWindow().asyncExec(newWebBrowser, componentID, e.menuBar, e.toolBar, e.addressBar, e.statusBar, e.location == null? null: new Point(e.location.x, e.location.y), e.size == null? null: new Dimension(e.size.x, e.size.y));
            }
          }
        });
      }
    });
    browser.addLocationListener(new LocationListener() {
      public void changed(LocationEvent e) {
        browser.setData("Browser.loading", false);
        new CMJ_locationChanged().asyncExec(browser, e.location, e.top);
      }
      public void changing(LocationEvent e) {
        final String location = e.location;
        if(location.startsWith(COMMAND_LOCATION_PREFIX)) {
          e.doit = false;
          String query = location.substring(COMMAND_LOCATION_PREFIX.length());
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
        browser.setData("CMJ_updateStatus.status", null);
        browser.setData("CMJ_updateProgress.progress", null);
        browser.setData("Browser.loading", true);
        e.doit = Boolean.TRUE.equals(new CMJ_locationChanging().syncExec(browser, location, e.top));
        if(!e.doit) {
          browser.setData("Browser.loading", false);
          new CMJ_locationChangeCanceled().asyncExec(browser, location, e.top);
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
        final String oldStatus = (String)browser.getData("CMJ_updateStatus.status");
        final String newStatus = e.text;
        if(newStatus.startsWith(COMMAND_STATUS_PREFIX)) {
          // XULRunner on Linux: "window" is not defined when synchronous... so we defer.
          e.display.asyncExec(new Runnable() {
            public void run() {
              browser.execute(fixJavascript(browser, "if(decodeURIComponent('" + Utils.encodeURL(newStatus) + "') == window.status) {window.status = decodeURIComponent('" + Utils.encodeURL(oldStatus == null? "": oldStatus) + "');}"));
            }
          });
          String query = newStatus.substring(COMMAND_STATUS_PREFIX.length());
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
        if(!Utils.equals(oldStatus, newStatus)) {
          browser.setData("CMJ_updateStatus.status", newStatus);
          new CMJ_updateStatus().asyncExec(browser, newStatus);
        }
      }
    });
    browser.addProgressListener(new ProgressListener() {
      private void updateProgress(int loadingProgress) {
        Integer oldLoadingProgress = (Integer)browser.getData("CMJ_updateProgress.progress");
        if(!Utils.equals(oldLoadingProgress, loadingProgress)) {
          browser.setData("CMJ_updateProgress.progress", loadingProgress);
          new CMJ_updateLoadingProgress().asyncExec(browser, loadingProgress);
        }
      }
      public void changed(ProgressEvent e) {
        if(e.total <= 0 || e.total < e.current) {
          return;
        }
        browser.setData("Browser.loading", true);
        updateProgress(e.current == e.total? 100: Math.min(e.current * 100 / e.total, 99));
      }
      public void completed(ProgressEvent progressevent) {
        browser.setData("Browser.loading", false);
        updateProgress(100);
      }
    });
    registerDefaultPopupMenu(browser);
    return browser;
  }

  private static class NSCommandBrowserFunction extends BrowserFunction {
    public NSCommandBrowserFunction(Browser browser) {
      super(browser, COMMAND_FUNCTION);
    }
    @Override
    public Object function(Object[] args) {
      String command = args.length >= 1? args[0] instanceof String? (String)args[0]: "": "";
      Object[] commandArgs;
      if(args.length > 1) {
        commandArgs = new Object[args.length - 1];
        System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
        args = commandArgs;
      } else {
        commandArgs = new Object[0];
      }
      new CMJ_commandReceived().asyncExec(getBrowser(), command, commandArgs);
      return null;
    }
  }

  private static void configureBrowserFunction(final Browser browser) {
    new NSCommandBrowserFunction(browser);
  }

  private Reference<JWebBrowser> webBrowser;

  public NativeWebBrowser(JWebBrowser webBrowser, boolean isXULRunnerRuntime) {
    this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
    this.isXULRunnerRuntime = isXULRunnerRuntime || "xulrunner".equals(System.getProperty("nativeswing.webbrowser.runtime"));
    xulRunnerHome = System.getProperty("nativeswing.webbrowser.xulrunner.home");
  }

  private static class CMN_clearSessionCookies extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Browser.clearSessions();
      return null;
    }
  }

  public static void clearSessionCookies() {
    new CMN_clearSessionCookies().asyncExec(true);
  }

  private static class CMN_getCookie extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      return Browser.getCookie((String)args[1], (String)args[0]);
    }
  }

  public static String getCookie(String url, String name) {
    return (String)new CMN_getCookie().syncExec(true, url, name);
  }

  private static class CMN_setCookie extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Browser.setCookie((String)args[1], (String)args[0]);
      return null;
    }
  }

  public static void setCookie(String url, String value) {
    new CMN_setCookie().asyncExec(true, url, value);
  }

  private static class CMN_getResourceLocation extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).getUrl();
    }
  }

  public String getResourceLocation() {
    return (String)runSync(new CMN_getResourceLocation());
  }

  private static class CMN_navigate extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).setUrl((String)args[0], (String)args[1], (String[])args[2]);
    }
  }

  public boolean navigate(String resourceLocation, WebBrowserNavigationParameters parameters) {
    return Boolean.TRUE.equals(runSync(new CMN_navigate(), resourceLocation, parameters == null? null: parameters.getPostData(), parameters == null? null: parameters.getHeaders()));
  }

  private static class CMN_getHTMLContent extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).getText();
    }
  }

  public String getHTMLContent() {
    return (String)runSync(new CMN_getHTMLContent());
  }

  private static class CMN_setHTMLContent extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).setText((String)args[0]);
    }
  }

  public boolean setHTMLContent(String html) {
    return Boolean.TRUE.equals(runSync(new CMN_setHTMLContent(), html));
  }

  private static class CMN_isJavascriptEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).getJavascriptEnabled();
    }
  }

  public boolean isJavascriptEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isJavascriptEnabled()));
  }

  private static class CMN_setJavascriptEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      ((Browser)getControl()).setJavascriptEnabled((Boolean)args[0]);
      return null;
    }
  }

  public void setJavascriptEnabled(boolean isJavascriptEnabled) {
    runAsync(new CMN_setJavascriptEnabled(), isJavascriptEnabled);
  }

  private static Pattern JAVASCRIPT_LINE_COMMENT_PATTERN = Pattern.compile("^\\s*//.*$", Pattern.MULTILINE);

  private static volatile Boolean isFixedJS;

  private static String fixJavascript(Browser browser, String script) {
    if("mozilla".equals(browser.getBrowserType())) {
      if(isFixedJS == null) {
        isFixedJS = "%25".equals(browser.evaluate("return '%25'"));
      }
      if(!isFixedJS) {
        // 2 workarounds for issues that seem to be happening with XULRunner < 1.9.
        // Remove line comments, because it does not work properly on Mozilla.
        // cf. bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=215335
        script = JAVASCRIPT_LINE_COMMENT_PATTERN.matcher(script).replaceAll("");
        // encode the script, because it is passed as a URL in Mozilla and gets URI-decoded.
        // cf. bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=255462
        script = Utils.encodeURL(script);
      }
    }
    return script;
  }

  private static class CMN_executeJavascript extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      String script = (String)args[0];
      Browser browser = (Browser)getControl();
      return browser.execute(fixJavascript(browser, script));
    }
  }

  public boolean executeJavascriptAndWait(String script) {
    return Boolean.TRUE.equals(runSync(new CMN_executeJavascript(), script));
  }

  public void executeJavascript(String script) {
    runAsync(new CMN_executeJavascript(), script);
  }

  private static class CMN_executeJavascriptWithResult extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      String script = (String)args[0];
      Browser browser = (Browser)getControl();
      return browser.evaluate(fixJavascript(browser, script));
    }
  }

  public Object executeJavascriptWithResult(String script) {
    return runSync(new CMN_executeJavascriptWithResult(), script);
  }

  private static class CMN_stopLoading extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      ((Browser)getControl()).stop();
      return null;
    }
  }

  public void stopLoading() {
    runAsync(new CMN_stopLoading());
  }

  private static class CMN_reloadPage extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      ((Browser)getControl()).refresh();
      return null;
    }
  }

  public void reloadPage() {
    runAsync(new CMN_reloadPage());
  }

  private static class CMN_isBackNavigationEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).isBackEnabled();
    }
  }

  public boolean isBackNavigationEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isBackNavigationEnabled()));
  }

  private static class CMN_navigateBack extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).back();
    }
  }

  public void navigateBack() {
    runAsync(new CMN_navigateBack());
  }

  private static class CMN_isForwardNavigationEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).isForwardEnabled();
    }
  }

  public boolean isForwardNavigationEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isForwardNavigationEnabled()));
  }

  private static class CMN_navigateForward extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).forward();
    }
  }

  public void navigateForward() {
    runAsync(new CMN_navigateForward());
  }

  private static void registerDefaultPopupMenu(final Browser browser) {
    Menu oldMenu = browser.getMenu();
    if(oldMenu != null) {
      oldMenu.dispose();
    }
    if(!"mozilla".equals(browser.getBrowserType())) {
      browser.setMenu(null);
      return;
    }
    Menu menu = new Menu(browser.getShell(), SWT.POP_UP);
    String className = NativeWebBrowser.class.getName();
    ResourceBundle bundle = ResourceBundle.getBundle(className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resource/WebBrowser");
    final MenuItem backMenuItem = new MenuItem(menu, SWT.PUSH);
    backMenuItem.setText(bundle.getString("SystemMenuBack"));
    backMenuItem.setImage(new Image(browser.getDisplay(), NativeWebBrowser.class.getResourceAsStream(bundle.getString("SystemMenuBackIcon"))));
    backMenuItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        browser.back();
      }
    });
    final MenuItem forwardMenuItem = new MenuItem(menu, SWT.PUSH);
    forwardMenuItem.setText(bundle.getString("SystemMenuForward"));
    forwardMenuItem.setImage(new Image(browser.getDisplay(), NativeWebBrowser.class.getResourceAsStream(bundle.getString("SystemMenuForwardIcon"))));
    forwardMenuItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        browser.forward();
      }
    });
    final MenuItem reloadMenuItem = new MenuItem(menu, SWT.PUSH);
    reloadMenuItem.setText(bundle.getString("SystemMenuReload"));
    reloadMenuItem.setImage(new Image(browser.getDisplay(), NativeWebBrowser.class.getResourceAsStream(bundle.getString("SystemMenuReloadIcon"))));
    reloadMenuItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        browser.refresh();
      }
    });
    final MenuItem stopMenuItem = new MenuItem(menu, SWT.PUSH);
    stopMenuItem.setText(bundle.getString("SystemMenuStop"));
    stopMenuItem.setImage(new Image(browser.getDisplay(), NativeWebBrowser.class.getResourceAsStream(bundle.getString("SystemMenuStopIcon"))));
    stopMenuItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        browser.stop();
      }
    });
    menu.addMenuListener(new MenuAdapter() {
      @Override
      public void menuShown(MenuEvent e) {
        backMenuItem.setEnabled(browser.isBackEnabled());
        forwardMenuItem.setEnabled(browser.isForwardEnabled());
        stopMenuItem.setEnabled(Boolean.TRUE.equals(browser.getData("Browser.loading")));
      }
    });
    browser.setMenu(menu);
  }

  private static class CMN_setDefaultPopupMenuRegistered extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Browser browser = (Browser)getControl();
      boolean isDefaultPopupMenuRegistered = (Boolean)args[0];
      if(isDefaultPopupMenuRegistered) {
        registerDefaultPopupMenu(browser);
      } else {
        Menu oldMenu = browser.getMenu();
        if(oldMenu != null) {
          oldMenu.dispose();
        }
        final Menu menu = new Menu(browser.getShell(), SWT.POP_UP);
        menu.addMenuListener(new MenuAdapter() {
          @Override
          public void menuShown(MenuEvent e) {
            menu.setVisible(false);
          }
        });
        browser.setMenu(menu);
      }
      return null;
    }
  }

  public void setDefaultPopupMenuRegistered(boolean isDefaultPopupMenuRegistered) {
    runAsync(new CMN_setDefaultPopupMenuRegistered(), isDefaultPopupMenuRegistered);
  }

  private String status;

  public String getStatusText() {
    return status == null? "": status;
  }

  private String title;

  public String getPageTitle() {
    return title == null? "": title;
  }

  private int loadingProgress = 100;

  /**
   * @return a value between 0 and 100 indicating the current loading progress.
   */
  public int getLoadingProgress() {
    return loadingProgress;
  }

  private static class CMJ_invokeFunction extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      if(nativeWebBrowser.nameToFunctionMap != null) {
        WebBrowserFunction function = nativeWebBrowser.nameToFunctionMap.get(args[0]);
        if(function != null) {
          return function.invoke(webBrowser, (Object[])args[1]);
        }
      }
      return null;
    }
  }

  private static class CMN_registerFunction extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Browser browser = (Browser)getControl();
      String functionName = (String)args[0];
      BrowserFunction browserFunction = new BrowserFunction(browser, functionName) {
        @Override
        public Object function(Object[] arguments) {
          return new CMJ_invokeFunction().syncExec(getBrowser(), getName(), arguments);
        }
      };
      browser.setData("nsFunction_" + functionName, browserFunction);
      return null;
    }
  }

  private Map<String, WebBrowserFunction> nameToFunctionMap;

  public void registerFunction(WebBrowserFunction function) {
    String functionName = function.getName();
    if(nameToFunctionMap == null) {
      nameToFunctionMap = new HashMap<String, WebBrowserFunction>();
    } else {
      WebBrowserFunction oldFunction = nameToFunctionMap.get(functionName);
      if(oldFunction == function) {
        return;
      }
      if(oldFunction != null) {
        unregisterFunction(oldFunction);
      }
    }
    nameToFunctionMap.put(functionName, function);
    runAsync(new CMN_registerFunction(), functionName);
  }

  private static class CMN_unregisterFunction extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Browser browser = (Browser)getControl();
      String key = "nsFunction_" + (String)args[0];
      BrowserFunction browserFunction = (BrowserFunction)browser.getData(key);
      browser.setData(key, null);
      browserFunction.dispose();
      return null;
    }
  }

  public void unregisterFunction(WebBrowserFunction function) {
    if(nameToFunctionMap == null) {
      return;
    }
    String functionName = function.getName();
    WebBrowserFunction currentFunction = nameToFunctionMap.get(functionName);
    if(currentFunction != function) {
      return;
    }
    nameToFunctionMap.remove(function);
    if(nameToFunctionMap.isEmpty()) {
      nameToFunctionMap = null;
    }
    runAsync(new CMN_unregisterFunction(), functionName);
  }

  private static class CMJ_getCredentials extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getNativeComponent();
      JWebBrowser webBrowser = nativeWebBrowser == null? null: nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      WebBrowserAuthenticationHandler authenticationHandler = nativeWebBrowser.getAuthenticationHandler();
      if(authenticationHandler == null) {
        return new Object[] {true, null, null};
      }
      String resourceLocation = (String)args[0];
      Credentials credentials = authenticationHandler.getCredentials(webBrowser, resourceLocation);
      if(credentials == null) {
        return new Object[] {false, null, null};
      }
      return new Object[] {true, credentials.getUserName(), credentials.getPassword()};
    }
  }

  private static class CMN_setAuthenticationHandler extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      final Browser browser = (Browser)getControl();
      boolean isActive = (Boolean)args[0];
      if(isActive) {
        AuthenticationListener authenticationListener = new AuthenticationListener() {
          public void authenticate(AuthenticationEvent e) {
            Object[] result = (Object[])new CMJ_getCredentials().syncExec(browser, e.location);
            boolean doIt = (Boolean)result[0];
            if(doIt) {
              e.user = (String)result[1];
              e.password = (String)result[2];
            } else {
              e.doit = false;
            }
          }
        };
        browser.setData("Browser.authenticationListener", authenticationListener);
        browser.addAuthenticationListener(authenticationListener);
      } else {
        browser.removeAuthenticationListener((AuthenticationListener)browser.getData("Browser.authenticationListener"));
        browser.setData("Browser.authenticationListener", null);
      }
      return null;
    }
  }

  private WebBrowserAuthenticationHandler authenticationHandler;

  public void setAuthenticationHandler(WebBrowserAuthenticationHandler authenticationHandler) {
    if(this.authenticationHandler == authenticationHandler) {
      return;
    }
    boolean isActivated = this.authenticationHandler == null;
    boolean isDeactivated = authenticationHandler == null;
    this.authenticationHandler = authenticationHandler;
    if(isActivated || isDeactivated) {
      runAsync(new CMN_setAuthenticationHandler(), isActivated);
    }
  }

  public WebBrowserAuthenticationHandler getAuthenticationHandler() {
    return authenticationHandler;
  }

  private static class CMN_getBrowserType extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return ((Browser)getControl()).getBrowserType();
    }
  }

  public String getBrowserType() {
    return (String)runSync(new CMN_getBrowserType());
  }

  private static class CMN_getBrowserVersion extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return new JSBrowserDetection((Browser)getControl()).browserVersion;
    }
  }

  public String getBrowserVersion() {
    return (String)runSync(new CMN_getBrowserVersion());
  }

  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
  }

  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
  }

  @Override
  protected Component createEmbeddableComponent(Map<Object, Object> optionMap) {
    return super.createEmbeddableComponent(optionMap);
  }

  @Override
  protected void disposeNativePeer() {
    super.disposeNativePeer();
  }

  private static class CMN_disposeWebBrowser extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      boolean isAlive = true;
      Browser browser = (Browser)getControl();
      if(browser != null) {
        if(!browser.isDisposed()) {
          Shell shell = browser.getShell();
          if(browser.close()) {
            isAlive = false;
            if(shell != null) {
              shell.dispose();
            }
          }
        }
      }
      return isAlive;
    }
  }

  protected boolean unloadAndDispose() {
    if(isNativePeerInitialized()) {
      // We return "isAlive" (and not "isDisposed") because if the call fails it would return false by default.
      if((Boolean)runSync(new CMN_disposeWebBrowser())) {
        return false;
      }
    }
    disposeNativePeer();
    return true;

  }

}
