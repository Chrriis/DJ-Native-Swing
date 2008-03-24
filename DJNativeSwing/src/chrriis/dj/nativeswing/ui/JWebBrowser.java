/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.border.BevelBorder;

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.Message.EmptyMessage;
import chrriis.dj.nativeswing.ui.event.InitializationListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserNavigationEvent;

/**
 * @author Christopher Deckers
 */
public class JWebBrowser extends JPanel implements Disposable {

  public static void clearSessionCookies() {
    NativeWebBrowser.clearSessionCookies();
  }
  
  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JWebBrowser.class.getPackage().getName().replace('.', '/') + "/resource/WebBrowser");

  private Component embeddableComponent;
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
      boolean isBackEnabled = webBrowser.nativeComponent.isBackEnabled();
      webBrowser.backButton.setEnabled(isBackEnabled);
      webBrowser.backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = webBrowser.nativeComponent.isForwardEnabled();
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
      boolean isBackEnabled = webBrowser.nativeComponent.isBackEnabled();
      webBrowser.backButton.setEnabled(isBackEnabled);
      webBrowser.backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = webBrowser.nativeComponent.isForwardEnabled();
      webBrowser.forwardButton.setEnabled(isForwardEnabled);
      webBrowser.forwardMenuItem.setEnabled(isForwardEnabled);
    }
    @Override
    public void statusChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = this.webBrowser.get();
      if(webBrowser == null) {
        return;
      }
      String status = webBrowser.nativeComponent.getStatus();
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

  public void copyAppearance(JWebBrowser webBrowser) {
    setAddressBarVisible(webBrowser.isAddressBarVisible());
    setButtonBarVisible(webBrowser.isButtonBarVisible());
    setMenuBarVisible(webBrowser.isMenuBarVisible());
    setStatusBarVisible(webBrowser.isStatusBarVisible());
  }
  
  public void copyContent(JWebBrowser webBrowser) {
    String url = webBrowser.getURL();
    if("about:blank".equals(url)) {
      setText(webBrowser.getText());
    } else {
      setURL(url);
    }
  }
  
  public JWebBrowser() {
    setLayout(new BorderLayout(0, 0));
    nativeComponent = new NativeWebBrowser(this);
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
        back();
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
        forward();
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
    embeddableComponent = nativeComponent.createEmbeddableComponent();
    webBrowserPanel.add(embeddableComponent, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    statusBarPanel = new JPanel(new BorderLayout(0, 0));
    statusBarPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, statusBarPanel.getBackground().darker()), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
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
        webBrowser.copyAppearance(JWebBrowser.this);
        webBrowser.copyContent(JWebBrowser.this);
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
  
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    statusBarPanel.setVisible(isStatusBarVisible);
    statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible);
    adjustBorder();
  }
  
  public boolean isStatusBarVisible() {
    return statusBarPanel.isVisible();
  }
  
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    menuBar.setVisible(isMenuBarVisible);
    adjustBorder();
  }
  
  public boolean isMenuBarVisible() {
    return menuBar.isVisible();
  }
  
  public void setButtonBarVisible(boolean isButtonBarVisible) {
    buttonBarPanel.setVisible(isButtonBarVisible);
    buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible);
    adjustBorder();
  }
  
  public boolean isButtonBarVisible() {
    return buttonBarPanel.isVisible();
  }
  
  public void setAddressBarVisible(boolean isAddressBarVisible) {
    addressBarPanel.setVisible(isAddressBarVisible);
    addressBarCheckBoxMenuItem.setSelected(isAddressBarVisible);
    adjustBorder();
  }
  
  public boolean isAddressBarVisible() {
    return addressBarPanel.isVisible();
  }
  
  public String getTitle() {
    return nativeComponent.getTitle();
  }
  
  public String getStatus() {
    return nativeComponent.getStatus();
  }

  public String getText() {
    return nativeComponent.getText();
  }
  
  public boolean setText(String html) {
    return nativeComponent.setText(html);
  }
  
  public String getURL() {
    return nativeComponent.getURL();
  }
  
  public boolean setURL(String url) {
    return nativeComponent.setURL(url);
  }
  
  public boolean isBackEnabled() {
    return nativeComponent.isBackEnabled();
  }
  
  public void back() {
    nativeComponent.back();
  }
  
  public boolean isForwardEnabled() {
    return nativeComponent.isForwardEnabled();
  }
  
  public void forward() {
    nativeComponent.forward();
  }
  
  public void refresh() {
    nativeComponent.refresh();
  }
  
  public void stop() {
    nativeComponent.stop();
  }
  
  /**
   * Execute a script and wait for the indication of success.
   */
  public boolean executeAndWait(String script) {
    return nativeComponent.executeAndWait(script);
  }
  
  public void execute(String script) {
    nativeComponent.execute(script);
  }
  
  public String executeAndWaitForCommandResult(final String commandName, String script) {
    if(!nativeComponent.isInitialized()) {
      return null;
    }
    final String TEMP_RESULT = new String();
    final String[] resultArray = new String[] {TEMP_RESULT};
    WebBrowserAdapter webBrowserListener = new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command, String[] args) {
        if(command.equals(commandName)) {
          resultArray[0] = args[0];
          nativeComponent.removeWebBrowserListener(this);
        }
      }
    };
    nativeComponent.addWebBrowserListener(webBrowserListener);
    if(nativeComponent.executeAndWait(script)) {
      for(int i=0; i<20; i++) {
        if(resultArray[0] != TEMP_RESULT) {
          break;
        }
        NativeInterfaceHandler.syncExec(new EmptyMessage());
        if(resultArray[0] != TEMP_RESULT) {
          break;
        }
        try {
          Thread.sleep(50);
        } catch(Exception e) {}
      }
    }
    nativeComponent.removeWebBrowserListener(webBrowserListener);
    String result = resultArray[0];
    return result == TEMP_RESULT? null: result;
  }
  
  public int getPageLoadingProgressValue() {
    return nativeComponent.getPageLoadingProgressValue();
  }
  
  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
    nativeComponent.addWebBrowserListener(listener);
  }
  
  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
    nativeComponent.removeWebBrowserListener(listener);
  }
  
  public WebBrowserListener[] getWebBrowserListeners() {
    return listenerList.getListeners(WebBrowserListener.class);
  }
  
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
  
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeWebBrowserListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }

  /**
   * @return true if the control was initialized. If the initialization failed, this would return true but isValidControl would return false.
   */
  public boolean isInitialized() {
    return nativeComponent.isInitialized();
  }
  
  /**
   * @return true if the component is initialized and is properly created.
   */
  public boolean isValidControl() {
    return nativeComponent.isValidControl();
  }
  
  public JMenuBar getMenuBar() {
    return menuBar;
  }
  
  public JMenu getFileMenu() {
    return fileMenu;
  }
  
  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JWebBrowser.class.getResource(value));
  }
  
  public void dispose() {
    if(embeddableComponent instanceof Disposable) {
      ((Disposable)embeddableComponent).dispose();
    }
    nativeComponent.releaseResources();
  }
  
  public boolean isDisposed() {
    return nativeComponent.isDisposed();
  }
  
  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is initialized, and this method adds to the queue of calls in case it is not.
   */
  public void run(Runnable runnable) {
    nativeComponent.run(runnable);
  }
  
  /**
   * Forces the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initializeNativePeer() {
    nativeComponent.initializeNativePeer();
  }
  
  /**
   * @return the Browser Window if it is contained in one, or null.
   */
  public JWebBrowserWindow getBrowserWindow() {
    Window window = SwingUtilities.getWindowAncestor(this);
    if(window instanceof JWebBrowserWindow) {
      return (JWebBrowserWindow)window;
    }
    return null;
  }
  
}
