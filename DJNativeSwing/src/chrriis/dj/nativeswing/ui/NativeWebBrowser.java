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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.regex.Pattern;

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

import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowOpeningEvent;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowser extends NativeComponent {

  protected static final String COMMAND_PREFIX = "command://";

  protected Browser browser;
  protected Reference<JWebBrowser> webBrowser;
  
  public NativeWebBrowser(JWebBrowser webBrowser) {
    this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
  }

  @Override
  protected Control createControl(Shell shell) {
    int style = SWT.NONE;
    if("mozilla".equals(System.getProperty("dj.nativeswing.webbrowser"))) {
      style |= SWT.MOZILLA;
    }
    browser = new Browser(shell, style);
    browser.addStatusTextListener(new StatusTextListener() {
      public void changed(StatusTextEvent e) {
        final String status = e.text;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateStatus(status);
          }
        });
      }
    });
    browser.addTitleListener(new TitleListener() {
      public void changed(TitleEvent e) {
        final String title = e.title;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateTitle(title);
          }
        });
      }
    });
    browser.addCloseWindowListener(new CloseWindowListener() {
      public void close(WindowEvent e) {
        NativeInterfaceHandler.invokeSwing(new Runnable() {
          public void run() {
            Object[] listeners = listenerList.getListenerList();
            WebBrowserEvent e = null;
            for(int i=listeners.length-2; i>=0; i-=2) {
              if(listeners[i] == WebBrowserListener.class) {
                if(e == null) {
                  e = new WebBrowserEvent(webBrowser.get());
                }
                ((WebBrowserListener)listeners[i + 1]).windowClosing(e);
              }
            }
            Window window = SwingUtilities.getWindowAncestor(webBrowser.get());
            if(window instanceof JWebBrowserWindow) {
              window.dispose();
            }
          }
        });
      }
    });
    browser.addOpenWindowListener(new OpenWindowListener() {
      public void open(WindowEvent e) {
        // This forces the user to open it himself
        e.required = true;
        final Display display = NativeInterfaceHandler.getDisplay();
        final Shell shell = new Shell(display);
        Browser browser_ = new Browser(shell, browser.getStyle());
        e.browser = browser_;
        class BrowserAttributes {
          protected Point location;
          protected Dimension size;
          protected boolean hasMenuBar = true;
          protected boolean hasToolBar = true;
          protected boolean hasAddressBar = true;
          protected boolean hasStatusBar = true;
          protected String url;
        }
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
            NativeInterfaceHandler.invokeSwing(new Runnable() {
              public void run() {
                JWebBrowser jWebBrowser = new JWebBrowser();
                jWebBrowser.setAddressBarVisible(browserAttributes.hasAddressBar);
                jWebBrowser.setMenuBarVisible(browserAttributes.hasMenuBar);
                jWebBrowser.setStatusBarVisible(browserAttributes.hasStatusBar);
                jWebBrowser.setButtonBarVisible(browserAttributes.hasToolBar);
                Object[] listeners = listenerList.getListenerList();
                WebBrowserWindowOpeningEvent e = null;
                for(int i=listeners.length-2; i>=0 && jWebBrowser != null; i-=2) {
                  if(listeners[i] == WebBrowserListener.class) {
                    if(e == null) {
                      e = new WebBrowserWindowOpeningEvent(NativeWebBrowser.this.webBrowser.get(), jWebBrowser, browserAttributes.url, browserAttributes.location, browserAttributes.size);
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
              }
            });
            display.asyncExec(new Runnable() {
              public void run() {
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
        final String location = e.location;
        final boolean isTopFrame = e.top;
        NativeInterfaceHandler.invokeSwing(new Runnable() {
          public void run() {
            Object[] listeners = listenerList.getListenerList();
            WebBrowserNavigationEvent e = null;
            for(int i=listeners.length-2; i>=0; i-=2) {
              if(listeners[i] == WebBrowserListener.class) {
                if(e == null) {
                  e = new WebBrowserNavigationEvent(webBrowser.get(), location, isTopFrame);
                }
                ((WebBrowserListener)listeners[i + 1]).urlChanged(e);
              }
            }
          }
        });
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
          final String command_ = command;
          NativeInterfaceHandler.invokeSwing(new Runnable() {
            public void run() {
              Object[] listeners = listenerList.getListenerList();
              WebBrowserEvent e = null;
              for(int i=listeners.length-2; i>=0; i-=2) {
                if(listeners[i] == WebBrowserListener.class) {
                  if(e == null) {
                    e = new WebBrowserEvent(webBrowser.get());
                  }
                  ((WebBrowserListener)listeners[i + 1]).commandReceived(e, command_);
                }
              }
            }
          });
          return;
        }
        if(location.startsWith("javascript:")) {
          return;
        }
        final boolean isTopFrame = e.top;
        final boolean[] hasRun = new boolean[1];
        final boolean[] isNavigating = new boolean[] {true};
        NativeInterfaceHandler.invokeSwing(new Runnable() {
          public void run() {
            Object[] listeners = listenerList.getListenerList();
            WebBrowserNavigationEvent e = null;
            for(int i=listeners.length-2; i>=0; i-=2) {
              if(listeners[i] == WebBrowserListener.class) {
                if(e == null) {
                  e = new WebBrowserNavigationEvent(webBrowser.get(), location, isTopFrame);
                }
                ((WebBrowserListener)listeners[i + 1]).urlChanging(e);
                isNavigating[0] &= !e.isConsumed();
              }
            }
            hasRun[0] = true;
          }
        });
//        while(!hasRun[0]) {
//          NativeInterfaceHandler.dispatch();
//        }
        e.doit = isNavigating[0];
        if(!e.doit) {
          NativeInterfaceHandler.invokeSwing(new Runnable() {
            public void run() {
              Object[] listeners = listenerList.getListenerList();
              WebBrowserNavigationEvent e = null;
              for(int i=listeners.length-2; i>=0; i-=2) {
                if(listeners[i] == WebBrowserListener.class) {
                  if(e == null) {
                    e = new WebBrowserNavigationEvent(webBrowser.get(), location, isTopFrame);
                  }
                  ((WebBrowserListener)listeners[i + 1]).urlChangeCanceled(e);
                }
              }
            }
          });
        }
      }
    });
    browser.addProgressListener(new ProgressListener() {
      public void changed(ProgressEvent e) {
        final int loadingProgressValue = e.total == 0? 100: e.current * 100 / e.total;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateProgress(loadingProgressValue);
          }
        });
      }
      public void completed(ProgressEvent progressevent) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateProgress(100);
          }
        });
      }
    });
    return browser;
  }
  
  public boolean setText(final String html) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.setText(html);
      }
    });
    return result[0];
  }
  
  public boolean setURL(final String url) {
    if(url == null) {
      throw new IllegalArgumentException("The url cannot be null!");
    }
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.setUrl(url);
      }
    });
    return result[0];
  }

  public boolean isBackEnabled() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.isBackEnabled();
      }
    });
    return result[0];
  }
  
  public boolean back() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.back();
      }
    });
    return result[0];
  }
  
  public boolean isForwardEnabled() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.isForwardEnabled();
      }
    });
    return result[0];
  }
  
  public boolean forward() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.forward();
      }
    });
    return result[0];
  }
  
  public void refresh() {
    run(new Runnable() {
      public void run() {
        browser.refresh();
      }
    });
  }
  
  public void stop() {
    run(new Runnable() {
      public void run() {
        browser.stop();
      }
    });
  }
  
  public boolean execute(final String script) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        // Remove line comments, because it does not work properly on Mozilla.
        String script_ = Pattern.compile("^//.*$", Pattern.MULTILINE).matcher(script).replaceAll("");
        result[0] = browser.execute(script_);
      }
    });
    return result[0];
  }
  
  public String getURL() {
    final String[] result = new String[1];
    run(new Runnable() {
      public void run() {
        result[0] = browser.getUrl();
      }
    });
    return result[0];
  }
  
  protected String status;
  
  protected void updateStatus(String status) {
    this.status = status;
    Object[] listeners = listenerList.getListenerList();
    WebBrowserEvent e = null;
    for(int i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i] == WebBrowserListener.class) {
        if(e == null) {
          e = new WebBrowserEvent(webBrowser.get());
        }
        ((WebBrowserListener)listeners[i + 1]).statusChanged(e);
      }
    }
  }
  
  public String getStatus() {
    return status == null? "": status;
  }
  
  protected String title;
  
  protected void updateTitle(String title) {
    this.title = title;
    Object[] listeners = listenerList.getListenerList();
    WebBrowserEvent e = null;
    for(int i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i] == WebBrowserListener.class) {
        if(e == null) {
          e = new WebBrowserEvent(webBrowser.get());
        }
        ((WebBrowserListener)listeners[i + 1]).titleChanged(e);
      }
    }
  }
  
  public String getTitle() {
    return title == null? "": title;
  }
  
  protected int pageLoadingProgressValue = 100;
  
  protected void updateProgress(int loadingProgressValue) {
    if(this.pageLoadingProgressValue == loadingProgressValue) {
      return;
    }
    this.pageLoadingProgressValue = loadingProgressValue;
    Object[] listeners = listenerList.getListenerList();
    WebBrowserEvent e = null;
    for(int i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i] == WebBrowserListener.class) {
        if(e == null) {
          e = new WebBrowserEvent(webBrowser.get());
        }
        ((WebBrowserListener)listeners[i + 1]).loadingProgressChanged(e);
      }
    }
  }
  
  /**
   * @return A value between 0 and 100 indicating the current loading progress.
   */
  public int getPageLoadingProgressValue() {
    return pageLoadingProgressValue;
  }
  
  public static void clearSessions() {
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        Browser.clearSessions();
      }
    });
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
