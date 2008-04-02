/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.Message;
import chrriis.dj.nativeswing.NSPanelComponent;
import chrriis.dj.nativeswing.NativeComponent;

/**
 * A native web browser, using Internet Explorer or Mozilla on Windows, and Mozilla on other platforms.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JWebBrowser extends NSPanelComponent {

  /**
   * Clear all session cookies from all current web browser instances.
   */
  public static void clearSessionCookies() {
    NativeWebBrowser.clearSessionCookies();
  }
  
  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JWebBrowser.class.getPackage().getName().replace('.', '/') + "/resource/WebBrowser");

  private NativeWebBrowser nativeComponent;

  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenu viewMenu;
  private JPanel buttonBarPanel;
  private JCheckBoxMenuItem buttonBarCheckBoxMenuItem;
  private JPanel addressBarPanel;
  private JCheckBoxMenuItem addressBarCheckBoxMenuItem;
  private JPanel statusBarPanel;
  private JCheckBoxMenuItem statusBarCheckBoxMenuItem;
  private JPanel webBrowserPanel;
  
  private JTextField addressField;
  private JLabel statusLabel;
  private JProgressBar progressBar;
  private JButton backButton;
  private JMenuItem backMenuItem;
  private JButton forwardButton;
  private JMenuItem forwardMenuItem;
  private JButton refreshButton;
  private JMenuItem refreshMenuItem;
  private JButton stopButton;
  private JMenuItem stopMenuItem;

  private static class NWebBrowserListener extends WebBrowserAdapter {
    protected Reference<JWebBrowser> webBrowser;
    protected NWebBrowserListener(JWebBrowser webBrowser) {
      this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
    }
    @Override
    public void urlChanged(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      webBrowser.stopButton.setEnabled(false);
      webBrowser.stopMenuItem.setEnabled(false);
      webBrowser.addressField.setText(webBrowser.nativeComponent.getURL());
      boolean isBackEnabled = webBrowser.nativeComponent.isGoBackEnabled();
      webBrowser.backButton.setEnabled(isBackEnabled);
      webBrowser.backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = webBrowser.nativeComponent.isGoForwardEnabled();
      webBrowser.forwardButton.setEnabled(isForwardEnabled);
      webBrowser.forwardMenuItem.setEnabled(isForwardEnabled);
    }
    @Override
    public void urlChanging(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      webBrowser.addressField.setText(e.getNewURL());
      webBrowser.stopButton.setEnabled(true);
      webBrowser.stopMenuItem.setEnabled(true);
    }
    @Override
    public void urlChangeCanceled(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      webBrowser.stopButton.setEnabled(false);
      webBrowser.stopMenuItem.setEnabled(false);
      webBrowser.addressField.setText(webBrowser.nativeComponent.getURL());
      boolean isBackEnabled = webBrowser.nativeComponent.isGoBackEnabled();
      webBrowser.backButton.setEnabled(isBackEnabled);
      webBrowser.backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = webBrowser.nativeComponent.isGoForwardEnabled();
      webBrowser.forwardButton.setEnabled(isForwardEnabled);
      webBrowser.forwardMenuItem.setEnabled(isForwardEnabled);
    }
    @Override
    public void statusChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      String status = webBrowser.nativeComponent.getStatusText();
      webBrowser.statusLabel.setText(status.length() == 0? " ": status);
    }
    @Override
    public void loadingProgressChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      int loadingProgress = webBrowser.getPageLoadingProgressValue();
      webBrowser.progressBar.setValue(loadingProgress);
      webBrowser.progressBar.setVisible(loadingProgress < 100);
    }
  }

  /**
   * Copy the appearance, the visibility of the various bars, from one web browser to another.
   * @param fromWebBrowser the web browser to copy the appearance from.
   * @param toWebBrowser the web browser to copy the appearance to.
   */
  public static void copyAppearance(JWebBrowser fromWebBrowser, JWebBrowser toWebBrowser) {
    toWebBrowser.setAddressBarVisible(fromWebBrowser.isAddressBarVisible());
    toWebBrowser.setButtonBarVisible(fromWebBrowser.isButtonBarVisible());
    toWebBrowser.setMenuBarVisible(fromWebBrowser.isMenuBarVisible());
    toWebBrowser.setStatusBarVisible(fromWebBrowser.isStatusBarVisible());
  }
  
  /**
   * Copy the content, whether a URL or its HTML content, from one web browser to another.
   * @param fromWebBrowser the web browser to copy the content from.
   * @param toWebBrowser the web browser to copy the content to.
   */
  public static void copyContent(JWebBrowser fromWebBrowser, JWebBrowser toWebBrowser) {
    String url = fromWebBrowser.getURL();
    if("about:blank".equals(url)) {
      toWebBrowser.setHTMLContent(fromWebBrowser.getHTMLContent());
    } else {
      toWebBrowser.setURL(url);
    }
  }
  
  private static final Border STATUS_BAR_BORDER = new AbstractBorder() {
    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(1, 1, 1, 1);
    }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Color background = c.getBackground();
      g.setColor(background == null? Color.LIGHT_GRAY: background.darker());
      g.drawLine(0, 0, width-1, 0);
      g.drawLine(width-1, 0, width-1, height-1);
      g.drawLine(0, height-1, width-1, height-1);
      g.drawLine(0, 0, 0, height-1);
    }
  };
  
  /**
   * Construct a new web browser.
   */
  public JWebBrowser() {
    nativeComponent = new NativeWebBrowser(this);
    initialize(nativeComponent);
    JPanel menuToolAndAddressBarPanel = new JPanel(new BorderLayout(0, 0));
    menuBar = new JMenuBar();
    menuToolAndAddressBarPanel.add(menuBar, BorderLayout.NORTH);
    buttonBarPanel = new JPanel(new BorderLayout(0, 0));
    JToolBar buttonToolBar = new JToolBar();
    buttonToolBar.add(Box.createHorizontalStrut(2));
    buttonToolBar.setFloatable(false);
    backButton = new JButton(createIcon("BackIcon"));
    backButton.setEnabled(false);
    backButton.setToolTipText(RESOURCES.getString("BackText"));
    ActionListener backActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        goBack();
        nativeComponent.requestFocus();
      }
    };
    backButton.addActionListener(backActionListener);
    buttonToolBar.add(backButton);
    forwardButton = new JButton(createIcon("ForwardIcon"));
    forwardButton.setToolTipText(RESOURCES.getString("ForwardText"));
    forwardButton.setEnabled(false);
    ActionListener forwardActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        goForward();
        nativeComponent.requestFocus();
      }
    };
    forwardButton.addActionListener(forwardActionListener);
    buttonToolBar.add(forwardButton);
    refreshButton = new JButton(createIcon("RefreshIcon"));
    refreshButton.setToolTipText(RESOURCES.getString("RefreshText"));
    ActionListener refreshActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refresh();
        nativeComponent.requestFocus();
      }
    };
    refreshButton.addActionListener(refreshActionListener);
    buttonToolBar.add(refreshButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.setEnabled(false);
    ActionListener stopActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stop();
      }
    };
    stopButton.addActionListener(stopActionListener);
    buttonToolBar.add(stopButton);
    buttonBarPanel.add(buttonToolBar, BorderLayout.CENTER);
    menuToolAndAddressBarPanel.add(buttonBarPanel, BorderLayout.WEST);
    addressBarPanel = new JPanel(new BorderLayout(0, 0));
    JToolBar addressToolBar = new JToolBar();
    // We have to force the layout manager because in Synth L&F the text field does not take the full available width.
    addressToolBar.setLayout(new BoxLayout(addressToolBar, BoxLayout.LINE_AXIS));
    JPanel addressToolBarInnerPanel = new JPanel(new BorderLayout(0, 0));
    addressToolBarInnerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    addressToolBarInnerPanel.setOpaque(false);
    addressToolBar.setFloatable(false);
    addressField = new JTextField();
    ActionListener goActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setURL(addressField.getText());
        nativeComponent.requestFocus();
      }
    };
    addressField.addActionListener(goActionListener);
    addressToolBarInnerPanel.add(addressField, BorderLayout.CENTER);
    JButton goButton = new JButton(createIcon("GoIcon"));
    goButton.setToolTipText(RESOURCES.getString("GoText"));
    goButton.addActionListener(goActionListener);
    addressToolBar.add(addressToolBarInnerPanel);
    addressToolBar.add(goButton);
    addressBarPanel.add(addressToolBar, BorderLayout.CENTER);
    menuToolAndAddressBarPanel.add(addressBarPanel, BorderLayout.CENTER);
    add(menuToolAndAddressBarPanel, BorderLayout.NORTH);
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(nativeComponent.createEmbeddableComponent(), BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    statusBarPanel = new JPanel(new BorderLayout(0, 0));
    statusBarPanel.setBorder(BorderFactory.createCompoundBorder(STATUS_BAR_BORDER, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    statusLabel = new JLabel(" ");
    statusBarPanel.add(statusLabel, BorderLayout.CENTER);
    progressBar = new JProgressBar() {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(getParent().getWidth() / 10, 0);
      }
    };
    progressBar.setVisible(false);
    statusBarPanel.add(progressBar, BorderLayout.EAST);
    add(statusBarPanel, BorderLayout.SOUTH);
    nativeComponent.addWebBrowserListener(new NWebBrowserListener(this));
    adjustBorder();
    fileMenu = new JMenu(RESOURCES.getString("FileMenu"));
    JMenuItem fileNewWindowMenuItem = new JMenuItem(RESOURCES.getString("FileNewWindowMenu"));
    fileNewWindowMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JWebBrowser webBrowser = new JWebBrowser();
        JWebBrowser.copyAppearance(JWebBrowser.this, webBrowser);
        JWebBrowser.copyContent(JWebBrowser.this, webBrowser);
        JWebBrowserWindow webBrowserWindow = new JWebBrowserWindow(webBrowser);
        webBrowserWindow.setVisible(true);
      }
    });
    fileMenu.add(fileNewWindowMenuItem);
    final JMenuItem fileOpenLocationMenuItem = new JMenuItem(RESOURCES.getString("FileOpenLocationMenu"));
    fileOpenLocationMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String path = JOptionPane.showInputDialog(JWebBrowser.this, RESOURCES.getString("FileOpenLocationDialogMessage"), RESOURCES.getString("FileOpenLocationDialogTitle"), JOptionPane.QUESTION_MESSAGE);
        if(path != null) {
          setURL(path);
        }
      }
    });
    fileMenu.add(fileOpenLocationMenuItem);
    final JMenuItem fileOpenFileMenuItem = new JMenuItem(RESOURCES.getString("FileOpenFileMenu"));
    fileOpenFileMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if(fileChooser.showOpenDialog(JWebBrowser.this) == JFileChooser.APPROVE_OPTION) {
          try {
            setURL(fileChooser.getSelectedFile().getAbsolutePath());
          } catch(Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });
    fileMenu.add(fileOpenFileMenuItem);
    menuBar.add(fileMenu);
    viewMenu = new JMenu(RESOURCES.getString("ViewMenu"));
    JMenu viewToolbarsMenu = new JMenu(RESOURCES.getString("ViewToolbarsMenu"));
    buttonBarCheckBoxMenuItem = new JCheckBoxMenuItem(RESOURCES.getString("ViewToolbarsButtonBarMenu"));
    buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible());
    buttonBarCheckBoxMenuItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setButtonBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    viewToolbarsMenu.add(buttonBarCheckBoxMenuItem);
    addressBarCheckBoxMenuItem = new JCheckBoxMenuItem(RESOURCES.getString("ViewToolbarsAddressBarMenu"));
    addressBarCheckBoxMenuItem.setSelected(isAddressBarVisible());
    addressBarCheckBoxMenuItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setAddressBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    viewToolbarsMenu.add(addressBarCheckBoxMenuItem);
    viewMenu.add(viewToolbarsMenu);
    statusBarCheckBoxMenuItem = new JCheckBoxMenuItem(RESOURCES.getString("ViewStatusBarMenu"));
    statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible());
    statusBarCheckBoxMenuItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setStatusBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    viewMenu.add(statusBarCheckBoxMenuItem);
    viewMenu.addSeparator();
    backMenuItem = new JMenuItem(RESOURCES.getString("ViewBackMenu"), createIcon("BackMenuIcon"));
    backMenuItem.addActionListener(backActionListener);
    backMenuItem.setEnabled(backButton.isEnabled());
    viewMenu.add(backMenuItem);
    forwardMenuItem = new JMenuItem(RESOURCES.getString("ViewForwardMenu"), createIcon("ForwardMenuIcon"));
    forwardMenuItem.addActionListener(forwardActionListener);
    forwardMenuItem.setEnabled(forwardButton.isEnabled());
    viewMenu.add(forwardMenuItem);
    refreshMenuItem = new JMenuItem(RESOURCES.getString("ViewRefreshMenu"), createIcon("RefreshMenuIcon"));
    refreshMenuItem.addActionListener(refreshActionListener);
    refreshMenuItem.setEnabled(refreshButton.isEnabled());
    viewMenu.add(refreshMenuItem);
    stopMenuItem = new JMenuItem(RESOURCES.getString("ViewStopMenu"), createIcon("StopMenuIcon"));
    stopMenuItem.addActionListener(stopActionListener);
    stopMenuItem.setEnabled(stopButton.isEnabled());
    viewMenu.add(stopMenuItem);
    menuBar.add(viewMenu);
  }
  
  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    statusBarPanel.setVisible(isStatusBarVisible);
    statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible);
    adjustBorder();
  }
  
  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  public boolean isStatusBarVisible() {
    return statusBarPanel.isVisible();
  }
  
  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    menuBar.setVisible(isMenuBarVisible);
    adjustBorder();
  }
  
  /**
   * Indicate whether the menu bar is visible.
   * @return true if the menu bar is visible.
   */
  public boolean isMenuBarVisible() {
    return menuBar.isVisible();
  }
  
  /**
   * Set whether the button bar is visible.
   * @param isButtonBarVisible true if the button bar should be visible, false otherwise.
   */
  public void setButtonBarVisible(boolean isButtonBarVisible) {
    buttonBarPanel.setVisible(isButtonBarVisible);
    buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible);
    adjustBorder();
  }
  
  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  public boolean isButtonBarVisible() {
    return buttonBarPanel.isVisible();
  }
  
  /**
   * Set whether the address bar is visible.
   * @param isAddressBarVisible true if the address bar should be visible, false otherwise.
   */
  public void setAddressBarVisible(boolean isAddressBarVisible) {
    addressBarPanel.setVisible(isAddressBarVisible);
    addressBarCheckBoxMenuItem.setSelected(isAddressBarVisible);
    adjustBorder();
  }
  
  /**
   * Indicate whether the address bar is visible.
   * @return true if the address bar is visible.
   */
  public boolean isAddressBarVisible() {
    return addressBarPanel.isVisible();
  }
  
  /**
   * Get the title of the web page.
   * @return the title of the page.
   */
  public String getPageTitle() {
    return nativeComponent.getPageTitle();
  }
  
  /**
   * Get the status text.
   * @return the status text.
   */
  public String getStatusText() {
    return nativeComponent.getStatusText();
  }

  /**
   * Get the HTML content.
   * @return the HTML content.
   */
  public String getHTMLContent() {
    return nativeComponent.getHTMLContent();
  }
  
  /**
   * Set the HTML content.
   * @param html the HTML content.
   */
  public boolean setHTMLContent(String html) {
    return nativeComponent.setHTMLContent(html);
  }
  
  /**
   * Get the URL.
   * @return the URL.
   */
  public String getURL() {
    return nativeComponent.getURL();
  }
  
  /**
   * Set the URL.
   * @param url the URL.
   */
  public boolean setURL(String url) {
    return nativeComponent.setURL(url);
  }
  
  /**
   * Indicate if the web browser Go Back functionality is enabled.
   * @return true if the web browser Go Back functionality is enabled.
   */
  public boolean isGoBackEnabled() {
    return nativeComponent.isGoBackEnabled();
  }
  
  /**
   * Invoke the web browser Go Back functionality.
   */
  public void goBack() {
    nativeComponent.goBack();
  }
  
  /**
   * Indicate if the web browser Go Forward functionality is enabled.
   * @return true if the web browser Go Forward functionality is enabled.
   */
  public boolean isGoForwardEnabled() {
    return nativeComponent.isGoForwardEnabled();
  }
  
  /**
   * Invoke the web browser Go Forward functionality.
   */
  public void goForward() {
    nativeComponent.goForward();
  }
  
  /**
   * Invoke the web browser Refresh functionality.
   */
  public void refresh() {
    nativeComponent.refresh();
  }
  
  /**
   * Invoke the web browser Stop functionality, to stop all current loading operations.
   */
  public void stop() {
    nativeComponent.stop();
  }
  
  /**
   * Execute some javascript, and wait for the indication of success.
   * @param javascript the javascript to execute.
   * @return true if the execution succeeded. 
   */
  public boolean executeJavascriptAndWait(String javascript) {
    return nativeComponent.executeJavascriptAndWait(javascript);
  }
  
  /**
   * Execute some javascript.
   * @param javascript the javascript to execute. 
   */
  public void executeJavascript(String javascript) {
    nativeComponent.executeJavascript(javascript);
  }
  
  private static final String LS = Utils.LINE_SEPARATOR;

  /**
   * Execute some javascript, and wait for the result coming from the return statements.
   * @param javascript the javascript to execute which must contain explicit return statements. 
   * @return the value, potentially a String, Number, Boolean.
   */
  public Object executeJavascriptWithResult(String javascript) {
    if(!javascript.endsWith(";")) {
      javascript = javascript + ";";
    }
    String[] result = executeJavascriptWithCommandResult("[[getScriptResult]]",
        "try {" + LS +
        "  var result = function() {" + javascript + "}();" + LS +
        "  var type = result? typeof(result): '';" + LS +
        "  if('string' == type) {" + LS +
        "    window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(result);" + LS +
        "  } else {" + LS +
        "    window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(type) + '&' + encodeURIComponent(result);" + LS +
        "  }" + LS +
        "} catch(exxxxx) {" + LS +
        "  window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&&'" + LS +
        "}");
    if(result == null) {
      return null;
    }
    if(result.length == 1) {
      return convertJSObject("string", result[0]);
    }
    return convertJSObject(result[0], result[1]);
  }
  
  private Object convertJSObject(String type, String value) {
    if(type.length() == 0) {
      return null;
    }
    if("boolean".equals(type)) {
      return Boolean.parseBoolean(value);
    }
    if("number".equals(type)) {
      try {
        return Integer.parseInt(value);
      } catch(Exception e) {}
      try {
        return Float.parseFloat(value);
      } catch(Exception e) {}
      try {
        return Long.parseLong(value);
      } catch(Exception e) {}
      throw new IllegalStateException("Could not convert number: " + value);
    }
    return value;
  }
  
  private static class NCommandListener extends WebBrowserAdapter {
    private NativeWebBrowser nativeComponent;
    private String command;
    private Object[] resultArray;
    private NCommandListener(NativeWebBrowser nativeComponent, String command, Object[] resultArray) {
      this.nativeComponent = nativeComponent;
      this.command = command;
      this.resultArray = resultArray;
    }
    @Override
    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      if(this.command.equals(command)) {
        resultArray[0] = args;
        nativeComponent.removeWebBrowserListener(this);
      }
    }
  }
  
  private String[] executeJavascriptWithCommandResult(final String command, String script) {
    if(!nativeComponent.isNativePeerInitialized()) {
      return null;
    }
    final Object[] resultArray = new Object[] {null};
    WebBrowserAdapter webBrowserListener = new NCommandListener(nativeComponent, command, resultArray);
    nativeComponent.addWebBrowserListener(webBrowserListener);
    if(nativeComponent.executeJavascriptAndWait(script)) {
      for(int i=0; i<20; i++) {
        if(resultArray[0] != null) {
          break;
        }
        new Message().syncSend();
        if(resultArray[0] != null) {
          break;
        }
        try {
          Thread.sleep(50);
        } catch(Exception e) {}
      }
    }
    nativeComponent.removeWebBrowserListener(webBrowserListener);
    return (String[])resultArray[0];
  }
  
  /**
   * Get the page loading progress, a value between 0 and 100, where 100 means it is fully loaded.
   * @return a value between 0 and 100 indicating the current loading progress.
   */
  public int getPageLoadingProgressValue() {
    return nativeComponent.getPageLoadingProgressValue();
  }
  
  /**
   * Add a web browser listener.
   * @param listener The web browser listener to add.
   */
  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
    nativeComponent.addWebBrowserListener(listener);
  }
  
  /**
   * Remove a web browser listener.
   * @param listener the web browser listener to remove.
   */
  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
    nativeComponent.removeWebBrowserListener(listener);
  }

  /**
   * Get the web browser listeners.
   * @return the web browser listeners.
   */
  public WebBrowserListener[] getWebBrowserListeners() {
    return listenerList.getListeners(WebBrowserListener.class);
  }
  
  /**
   * Show or hide all the bars at once.
   * @param areBarsVisible true to show all bars, false to hide them all.
   */
  public void setBarsVisible(boolean areBarsVisible) {
    setMenuBarVisible(areBarsVisible);
    setButtonBarVisible(areBarsVisible);
    setAddressBarVisible(areBarsVisible);
    setStatusBarVisible(areBarsVisible);
  }
  
  private void adjustBorder() {
    if(isMenuBarVisible() || isButtonBarVisible() || isAddressBarVisible() || isStatusBarVisible()) {
      webBrowserPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    } else {
      webBrowserPanel.setBorder(null);
    }
  }
  
  /**
   * Get the native component.
   * @return the native component.
   */
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }
  
  /**
   * Get the menu bar, which allows to modify the items.
   * @return the menu bar.
   */
  public JMenuBar getMenuBar() {
    return menuBar;
  }
  
  /**
   * Get the file menu, which allows to modify the items.
   * @return the file menu.
   */
  public JMenu getFileMenu() {
    return fileMenu;
  }
  
  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JWebBrowser.class.getResource(value));
  }
  
  /**
   * Get the web browser window if the web browser is contained in one.
   * @return the web browser Window, or null.
   */
  public JWebBrowserWindow getWebBrowserWindow() {
    Window window = SwingUtilities.getWindowAncestor(this);
    if(window instanceof JWebBrowserWindow) {
      return (JWebBrowserWindow)window;
    }
    return null;
  }
  
}
