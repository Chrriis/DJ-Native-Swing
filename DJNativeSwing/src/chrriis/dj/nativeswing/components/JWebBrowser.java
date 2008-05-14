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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.Map;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.Message;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.NSPanelComponent;

/**
 * A native web browser, using Internet Explorer or Mozilla on Windows, and Mozilla on other platforms.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JWebBrowser extends NSPanelComponent {

  private static final String USE_XULRUNNER_RUNTIME_OPTION_KEY = "XULRunner Runtime";
  private static final NSOption XUL_RUNNER_RUNTIME_OPTION = new NSOption(USE_XULRUNNER_RUNTIME_OPTION_KEY);
  
  /**
   * Create an option to make the web browser use the Mozilla XULRunner runtime.
   * @return the option to use the XULRunner runtime.
   */
  public static NSOption useXULRunnerRuntime() {
    return XUL_RUNNER_RUNTIME_OPTION;
  }
  
  /**
   * Clear all session cookies from all current web browser instances.
   */
  public static void clearSessionCookies() {
    NativeWebBrowser.clearSessionCookies();
  }
  
  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JWebBrowser.class.getPackage().getName().replace('.', '/') + "/resource/WebBrowser");

  private NativeWebBrowser nativeComponent;

  private JPanel menuToolAndLocationBarPanel;
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenu viewMenu;
  private ButtonBarPane buttonBarPane;
  private JCheckBoxMenuItem buttonBarCheckBoxMenuItem;
  private LocationBarPane locationBarPane;
  private JCheckBoxMenuItem locationBarCheckBoxMenuItem;
  private StatusBarPane statusBarPane;
  private JCheckBoxMenuItem statusBarCheckBoxMenuItem;
  private JPanel webBrowserPanel;
  
  private JMenuItem backMenuItem;
  private JMenuItem forwardMenuItem;
  private JMenuItem reloadMenuItem;
  private JMenuItem stopMenuItem;

  private static class NWebBrowserListener extends WebBrowserAdapter {
    @Override
    public void locationChanged(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      updateStopButton(webBrowser, false);
      if(e.isTopFrame()) {
        if(webBrowser.locationBarPane != null) {
          webBrowser.locationBarPane.updateLocation();
        }
      }
      webBrowser.updateNavigationButtons();
    }
    @Override
    public void locationChanging(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      if(e.isTopFrame()) {
        if(webBrowser.locationBarPane != null) {
          webBrowser.locationBarPane.updateLocation(e.getNewResourceLocation());
        }
      }
      updateStopButton(webBrowser, true);
    }
    @Override
    public void locationChangeCanceled(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      updateStopButton(webBrowser, false);
      if(e.isTopFrame()) {
        if(webBrowser.locationBarPane != null) {
          webBrowser.locationBarPane.updateLocation();
        }
      }
      webBrowser.updateNavigationButtons();
    }
    @Override
    public void statusChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      if(webBrowser.statusBarPane != null) {
        webBrowser.statusBarPane.updateStatus();
      }
    }
    @Override
    public void loadingProgressChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      if(webBrowser.statusBarPane != null) {
        webBrowser.statusBarPane.updateProgressValue();
      }
      updateStopButton(webBrowser, false);
    }
    private void updateStopButton(JWebBrowser webBrowser, boolean isForcedOn) {
      boolean isStopEnabled = isForcedOn || webBrowser.getLoadingProgress() != 100;
      if(webBrowser.buttonBarPane != null) {
        webBrowser.buttonBarPane.stopButton.setEnabled(isStopEnabled);
      }
      webBrowser.stopMenuItem.setEnabled(isStopEnabled);
    }
  }

  private boolean isViewMenuVisible;
  
  private void updateNavigationButtons() {
    if(isViewMenuVisible || isButtonBarVisible()) {
      boolean isBackEnabled = nativeComponent.isNativePeerInitialized()? nativeComponent.isBackNavigationEnabled(): false;
      if(buttonBarPane != null) {
        buttonBarPane.backButton.setEnabled(isBackEnabled);
      }
      backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = nativeComponent.isNativePeerInitialized()? nativeComponent.isForwardNavigationEnabled(): false;
      if(buttonBarPane != null) {
        buttonBarPane.forwardButton.setEnabled(isForwardEnabled);
      }
      forwardMenuItem.setEnabled(isForwardEnabled);
    }
  }
  
  /**
   * Copy the appearance, the visibility of the various bars, from one web browser to another.
   * @param fromWebBrowser the web browser to copy the appearance from.
   * @param toWebBrowser the web browser to copy the appearance to.
   */
  public static void copyAppearance(JWebBrowser fromWebBrowser, JWebBrowser toWebBrowser) {
    toWebBrowser.setLocationBarVisible(fromWebBrowser.isLocationBarVisible());
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
    String location = fromWebBrowser.getResourceLocation();
    if("about:blank".equals(location)) {
      toWebBrowser.setHTMLContent(fromWebBrowser.getHTMLContent());
    } else {
      toWebBrowser.navigate(location);
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
  
  private class ButtonBarPane extends JPanel {
    
    private JButton backButton;
    private JButton forwardButton;
    private JButton reloadButton;
    private JButton stopButton;

    public ButtonBarPane() {
      super(new BorderLayout(0, 0));
      JToolBar buttonToolBar = new JToolBar();
      buttonToolBar.add(Box.createHorizontalStrut(2));
      buttonToolBar.setFloatable(false);
      backButton = new JButton(createIcon("BackIcon"));
      backButton.setEnabled(backMenuItem.isEnabled());
      backButton.setToolTipText(RESOURCES.getString("BackText"));
      backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          navigateBack();
          nativeComponent.requestFocus();
        }
      });
      buttonToolBar.add(backButton);
      forwardButton = new JButton(createIcon("ForwardIcon"));
      forwardButton.setToolTipText(RESOURCES.getString("ForwardText"));
      forwardButton.setEnabled(forwardMenuItem.isEnabled());
      forwardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          navigateForward();
          nativeComponent.requestFocus();
        }
      });
      buttonToolBar.add(forwardButton);
      reloadButton = new JButton(createIcon("ReloadIcon"));
      reloadButton.setToolTipText(RESOURCES.getString("ReloadText"));
      reloadButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          reloadPage();
          nativeComponent.requestFocus();
        }
      });
      buttonToolBar.add(reloadButton);
      stopButton = new JButton(createIcon("StopIcon"));
      stopButton.setToolTipText(RESOURCES.getString("StopText"));
      stopButton.setEnabled(stopMenuItem.isEnabled());
      stopButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          stopLoading();
        }
      });
      buttonToolBar.add(stopButton);
      add(buttonToolBar, BorderLayout.CENTER);
    }
    
  }
  
  private class LocationBarPane extends JPanel {
    
    private JTextField locationField;

    public LocationBarPane() {
      super(new BorderLayout(0, 0));
      JToolBar locationToolBar = new JToolBar();
      // We have to force the layout manager because in Synth L&F the text field does not take the full available width.
      locationToolBar.setLayout(new BoxLayout(locationToolBar, BoxLayout.LINE_AXIS));
      JPanel locationToolBarInnerPanel = new JPanel(new BorderLayout(0, 0));
      locationToolBarInnerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      locationToolBarInnerPanel.setOpaque(false);
      locationToolBar.setFloatable(false);
      locationField = new JTextField();
      locationField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            updateLocation();
          }
        }
      });
      ActionListener goActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          navigate(locationField.getText());
          nativeComponent.requestFocus();
        }
      };
      locationField.addActionListener(goActionListener);
      updateLocation();
      locationToolBarInnerPanel.add(locationField, BorderLayout.CENTER);
      JButton goButton = new JButton(createIcon("GoIcon"));
      goButton.setToolTipText(RESOURCES.getString("GoText"));
      goButton.addActionListener(goActionListener);
      locationToolBar.add(locationToolBarInnerPanel);
      locationToolBar.add(goButton);
      add(locationToolBar, BorderLayout.CENTER);
    }
    
    public void updateLocation(String location) {
      locationField.setText(location);
    }
    
    public void updateLocation() {
      locationField.setText(nativeComponent.isNativePeerInitialized()? nativeComponent.getResourceLocation(): "");
    }
    
  }
  
  private class StatusBarPane extends JPanel {
    
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public StatusBarPane() {
      super(new BorderLayout(0, 0));
      setBorder(BorderFactory.createCompoundBorder(STATUS_BAR_BORDER, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      statusLabel = new JLabel();
      updateStatus();
      add(statusLabel, BorderLayout.CENTER);
      progressBar = new JProgressBar() {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(getParent().getWidth() / 10, 0);
        }
      };
      updateProgressValue();
      add(progressBar, BorderLayout.EAST);
    }
    
    public void updateProgressValue() {
      int loadingProgress = nativeComponent.isNativePeerInitialized()? nativeComponent.getLoadingProgress(): 100;
      progressBar.setValue(loadingProgress);
      progressBar.setVisible(loadingProgress < 100);
    }
    
    public void updateStatus() {
      String status = nativeComponent.isNativePeerInitialized()? nativeComponent.getStatusText(): "";
      statusLabel.setText(status.length() == 0? " ": status);
    }
    
  }
  
  /**
   * Construct a new web browser.
   * @param options the options to configure the behavior of this component.
   */
  public JWebBrowser(NSOption... options) {
    Map<Object, Object> optionMap = NSOption.createOptionMap(options);
    nativeComponent = new NativeWebBrowser(this, optionMap.get(USE_XULRUNNER_RUNTIME_OPTION_KEY) != null);
    initialize(nativeComponent);
    menuToolAndLocationBarPanel = new JPanel(new BorderLayout(0, 0));
    menuBar = new JMenuBar();
    menuToolAndLocationBarPanel.add(menuBar, BorderLayout.NORTH);
    add(menuToolAndLocationBarPanel, BorderLayout.NORTH);
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(nativeComponent.createEmbeddableComponent(optionMap), BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    nativeComponent.addWebBrowserListener(new NWebBrowserListener());
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
          navigate(path);
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
            navigate(fileChooser.getSelectedFile().getAbsolutePath());
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
    locationBarCheckBoxMenuItem = new JCheckBoxMenuItem(RESOURCES.getString("ViewToolbarsLocationBarMenu"));
    locationBarCheckBoxMenuItem.setSelected(isLocationBarVisible());
    locationBarCheckBoxMenuItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setLocationBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    viewToolbarsMenu.add(locationBarCheckBoxMenuItem);
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
    backMenuItem = new JMenuItem(RESOURCES.getString("ViewMenuBack"), createIcon("ViewMenuBackIcon"));
    backMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        navigateBack();
        nativeComponent.requestFocus();
      }
    });
    backMenuItem.setEnabled(false);
    viewMenu.add(backMenuItem);
    forwardMenuItem = new JMenuItem(RESOURCES.getString("ViewMenuForward"), createIcon("ViewMenuForwardIcon"));
    forwardMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        navigateForward();
        nativeComponent.requestFocus();
      }
    });
    forwardMenuItem.setEnabled(false);
    viewMenu.add(forwardMenuItem);
    reloadMenuItem = new JMenuItem(RESOURCES.getString("ViewMenuReload"), createIcon("ViewMenuReloadIcon"));
    reloadMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reloadPage();
        nativeComponent.requestFocus();
      }
    });
    reloadMenuItem.setEnabled(false);
    viewMenu.add(reloadMenuItem);
    stopMenuItem = new JMenuItem(RESOURCES.getString("ViewMenuStop"), createIcon("ViewMenuStopIcon"));
    stopMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopLoading();
      }
    });
    stopMenuItem.setEnabled(false);
    viewMenu.add(stopMenuItem);
    menuBar.add(viewMenu);
    viewMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        isViewMenuVisible = false;
      }
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        isViewMenuVisible = true;
        if(!isButtonBarVisible()) {
          updateNavigationButtons();
        }
      }
    });
    setButtonBarVisible(true);
    setLocationBarVisible(true);
    setStatusBarVisible(true);
  }
  
  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    if(isStatusBarVisible == isStatusBarVisible()) {
      return;
    }
    if(isStatusBarVisible) {
      statusBarPane = new StatusBarPane();
      add(statusBarPane, BorderLayout.SOUTH);
    } else {
      remove(statusBarPane);
      statusBarPane = null;
    }
    revalidate();
    repaint();
    statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible);
    adjustBorder();
  }
  
  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  public boolean isStatusBarVisible() {
    return statusBarPane != null;
  }
  
  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    if(isMenuBarVisible == isMenuBarVisible()) {
      return;
    }
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
    if(isButtonBarVisible == isButtonBarVisible()) {
      return;
    }
    if(isButtonBarVisible) {
      buttonBarPane = new ButtonBarPane();
      menuToolAndLocationBarPanel.add(buttonBarPane, BorderLayout.WEST);
    } else {
      menuToolAndLocationBarPanel.remove(buttonBarPane);
      buttonBarPane = null;
    }
    menuToolAndLocationBarPanel.revalidate();
    menuToolAndLocationBarPanel.repaint();
    buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible);
    adjustBorder();
    if(isButtonBarVisible && !isViewMenuVisible) {
      updateNavigationButtons();
    }
  }
  
  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  public boolean isButtonBarVisible() {
    return buttonBarPane != null;
  }
  
  /**
   * Set whether the location bar is visible.
   * @param isLocationBarVisible true if the location bar should be visible, false otherwise.
   */
  public void setLocationBarVisible(boolean isLocationBarVisible) {
    if(isLocationBarVisible == isLocationBarVisible()) {
      return;
    }
    if(isLocationBarVisible) {
      locationBarPane = new LocationBarPane();
      menuToolAndLocationBarPanel.add(locationBarPane, BorderLayout.CENTER);
    } else {
      menuToolAndLocationBarPanel.remove(locationBarPane);
      locationBarPane = null;
    }
    menuToolAndLocationBarPanel.revalidate();
    menuToolAndLocationBarPanel.repaint();
    locationBarCheckBoxMenuItem.setSelected(isLocationBarVisible);
    adjustBorder();
    if(locationBarPane != null) {
    }
  }
  
  /**
   * Indicate whether the location bar is visible.
   * @return true if the location bar is visible.
   */
  public boolean isLocationBarVisible() {
    return locationBarPane != null;
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
   * Get the location of the resource currently displayed.
   * @return the location.
   */
  public String getResourceLocation() {
    return nativeComponent.getResourceLocation();
  }
  
  /**
   * Navigate to a resource, with its location specified as a URL or path.
   * @param resourceLocation the URL or path.
   * @return true if the navigation was successful. 
   */
  public boolean navigate(String resourceLocation) {
    return nativeComponent.navigate(resourceLocation);
  }
  
  /**
   * Indicate if the web browser Back functionality is enabled.
   * @return true if the web browser Back functionality is enabled.
   */
  public boolean isBackNavigationEnabled() {
    return nativeComponent.isBackNavigationEnabled();
  }
  
  /**
   * Invoke the web browser Back functionality.
   */
  public void navigateBack() {
    nativeComponent.navigateBack();
  }
  
  /**
   * Indicate if the web browser Forward functionality is enabled.
   * @return true if the web browser Forward functionality is enabled.
   */
  public boolean isForwardNavigationEnabled() {
    return nativeComponent.isForwardNavigationEnabled();
  }
  
  /**
   * Invoke the web browser Forward functionality.
   */
  public void navigateForward() {
    nativeComponent.navigateForward();
  }
  
  /**
   * Invoke the web browser Reload functionality.
   */
  public void reloadPage() {
    nativeComponent.reloadPage();
  }
  
  /**
   * Invoke the web browser Stop functionality, to stop all current loading operations.
   */
  public void stopLoading() {
    nativeComponent.stopLoading();
  }
  
//  /**
//   * Execute some javascript, and wait for the indication of success.
//   * @param javascript the javascript to execute.
//   * @return true if the execution succeeded. 
//   */
//  public boolean executeJavascriptAndWait(String javascript) {
//    return nativeComponent.executeJavascriptAndWait(javascript);
//  }
  
  /**
   * Execute some javascript.
   * @param javascript the javascript to execute. 
   */
  public void executeJavascript(String javascript) {
    nativeComponent.executeJavascript(javascript);
  }
  
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
        "try {" +
        "  var result = function() {" + javascript + "}();" +
        "  var type = result? typeof(result): '';" +
        "  if('string' == type) {" +
        "    window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(result);" +
        "  } else {" +
        "    window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(type) + '&' + encodeURIComponent(result);" +
        "  }" +
        "} catch(exxxxx) {" +
        "  window.location = 'command://' + encodeURIComponent('[[getScriptResult]]') + '&&'" +
        "}");
    if(result == null) {
      return null;
    }
    if(result.length == 1) {
      return convertJavascriptObjectToJava("string", result[0]);
    }
    return convertJavascriptObjectToJava(result[0], result[1]);
  }

  /**
   * Create the Javascript function call using the function name and Java objects as arguments. Note that it does not contain a semi-colon at the end of the statement, to allow call chaining.
   * @param functionName the name of the Javascript funtion.
   * @param args the Java objects (String, number, boolean, or array) which will get converted to Javascript arguments.
   * @return the function call, in the form "functionName(convArg1, convArg2, ...)".
   */
  public static String createJavascriptFunctionCall(String functionName, Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append(functionName).append('(');
    for(int i=0; i<args.length; i++) {
      if(i > 0) {
        sb.append(", ");
      }
      sb.append(convertJavaObjectToJavascript(args[i]));
    }
    sb.append(")");
    return sb.toString();
  }
  
  /**
   * Convert a Java object to Javascript, to simplify the task of executing scripts. Conversion adds quotes around Strings (with Java escaping and Javascript unescaping around), add brackets to arrays, treats arrays of arrays, and can handle null values.
   * @param o the object to convert, which can be a String, number, boolean, or array.
   */
  public static String convertJavaObjectToJavascript(Object o) {
    if(o == null) {
      return "null";
    }
    if(o instanceof Boolean || o instanceof Number) {
      return o.toString();
    }
    if(o.getClass().isArray()) {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      int length = Array.getLength(o);
      for(int i=0; i<length; i++) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(convertJavaObjectToJavascript(Array.get(o, i)));
      }
      sb.append(']');
      return sb.toString();
    }
    o = o.toString();
    String encodedArg = Utils.encodeURL((String)o);
    if(o.equals(encodedArg)) {
      return '\'' + (String)o + '\'';
    }
    return "decodeURIComponent('" + encodedArg + "')";
  }
  
  private static Object convertJavascriptObjectToJava(String type, String value) {
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
    private String command;
    private Object[] resultArray;
    private NCommandListener(String command, Object[] resultArray) {
      this.command = command;
      this.resultArray = resultArray;
    }
    @Override
    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      if(this.command.equals(command)) {
        resultArray[0] = args;
        ((NativeWebBrowser)e.getWebBrowser().getNativeComponent()).removeWebBrowserListener(this);
      }
    }
  }
  
  private String[] executeJavascriptWithCommandResult(final String command, String script) {
    if(!nativeComponent.isNativePeerInitialized()) {
      return null;
    }
    final Object[] resultArray = new Object[] {null};
    WebBrowserAdapter webBrowserListener = new NCommandListener(command, resultArray);
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
   * Get the loading progress, a value between 0 and 100, where 100 means it is fully loaded.
   * @return a value between 0 and 100 indicating the current loading progress.
   */
  public int getLoadingProgress() {
    return nativeComponent.getLoadingProgress();
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
    setLocationBarVisible(areBarsVisible);
    setStatusBarVisible(areBarsVisible);
  }
  
  private void adjustBorder() {
    if(isMenuBarVisible() || isButtonBarVisible() || isLocationBarVisible() || isStatusBarVisible()) {
      webBrowserPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    } else {
      webBrowserPanel.setBorder(null);
    }
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
  
  /**
   * Set whether this component is able to detect a popup menu gesture to show its default popup menu.
   * @param isDefaultPopupMenuRegistered true if the default popup menu is registered.
   */
  public void setDefaultPopupMenuRegistered(boolean isDefaultPopupMenuRegistered) {
    nativeComponent.setDefaultPopupMenuRegistered(isDefaultPopupMenuRegistered);
  }
  
}
