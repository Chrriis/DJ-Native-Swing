/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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
import javax.swing.border.AbstractBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import chrriis.dj.nativeswing.swtimpl.components.internal.INativeWebBrowser;

/**
 * A default web browser decorator, which can actually be subclassed for simple customization.
 * @author Christopher Deckers
 */
public class DefaultWebBrowserDecorator extends WebBrowserDecorator {

  public static enum WebBrowserDecoratorComponentType {
    FILE_MENU,
    FILE_NEW_WINDOW_MENU_ITEM,
    FILE_OPEN_LOCATION_MENU_ITEM,
    FILE_OPEN_FILE_MENU_ITEM,
    FILE_CLOSE_MENU_ITEM,
    VIEW_MENU,
    VIEW_TOOLBARS_MENU,
    VIEW_TOOLBARS_BUTTON_BAR_CHECKBOX_MENU_ITEM,
    VIEW_TOOLBARS_LOCATION_BAR_CHECKBOX_MENU_ITEM,
    VIEW_STATUS_BAR_CHECKBOX_MENU_ITEM,
    VIEW_BACK_MENU_ITEM,
    VIEW_FORWARD_MENU_ITEM,
    VIEW_RELOAD_MENU_ITEM,
    VIEW_STOP_MENU_ITEM,
    BACK_BUTTON,
    FORWARD_BUTTON,
    RELOAD_BUTTON,
    STOP_BUTTON,
    GO_BUTTON,
    STATUS_LABEL,
  }

  private final ResourceBundle RESOURCES;

  {
    String className = JWebBrowser.class.getName();
    RESOURCES = ResourceBundle.getBundle(className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resource/WebBrowser");
  }

  private static class NWebBrowserListener extends WebBrowserAdapter {
    @Override
    public void locationChanged(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      updateStopButton(webBrowser, false);
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(e.isTopFrame()) {
        if(decorator.locationBar != null) {
          decorator.locationBar.updateLocation();
        }
      }
      decorator.updateNavigationButtons();
    }
    @Override
    public void locationChanging(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(e.isTopFrame()) {
        if(decorator.locationBar != null) {
          decorator.locationBar.updateLocation(e.getNewResourceLocation());
        }
      }
      updateStopButton(webBrowser, true);
    }
    @Override
    public void locationChangeCanceled(WebBrowserNavigationEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      updateStopButton(webBrowser, false);
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(e.isTopFrame()) {
        if(decorator.locationBar != null) {
          decorator.locationBar.updateLocation();
        }
      }
      decorator.updateNavigationButtons();
    }
    @Override
    public void statusChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(decorator.statusBar != null) {
        decorator.statusBar.updateStatus();
      }
    }
    @Override
    public void loadingProgressChanged(WebBrowserEvent e) {
      JWebBrowser webBrowser = e.getWebBrowser();
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(decorator.statusBar != null) {
        decorator.statusBar.updateProgressValue();
      }
      updateStopButton(webBrowser, false);
    }
    private void updateStopButton(JWebBrowser webBrowser, boolean isForcedOn) {
      boolean isStopEnabled = isForcedOn || webBrowser.getLoadingProgress() != 100;
      DefaultWebBrowserDecorator decorator = (DefaultWebBrowserDecorator)webBrowser.getWebBrowserDecorator();
      if(decorator.buttonBar != null) {
        decorator.buttonBar.getStopButton().setEnabled(isStopEnabled);
      }
      decorator.menuBar.stopMenuItem.setEnabled(isStopEnabled);
    }
  }

  private boolean isViewMenuVisible;

  private void updateNavigationButtons() {
    if(!nativeWebBrowser.isNativePeerDisposed() && (isViewMenuVisible || isButtonBarVisible())) {
      boolean isBackEnabled = nativeWebBrowser.isNativePeerInitialized()? nativeWebBrowser.isBackNavigationEnabled(): false;
      if(buttonBar != null) {
        buttonBar.getBackButton().setEnabled(isBackEnabled);
      }
      menuBar.backMenuItem.setEnabled(isBackEnabled);
      boolean isForwardEnabled = nativeWebBrowser.isNativePeerInitialized()? nativeWebBrowser.isForwardNavigationEnabled(): false;
      if(buttonBar != null) {
        buttonBar.getForwardButton().setEnabled(isForwardEnabled);
      }
      menuBar.forwardMenuItem.setEnabled(isForwardEnabled);
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
   * The menu bar.
   * @author Christopher Deckers
   */
  public class WebBrowserMenuBar extends JMenuBar {

    private JMenu fileMenu;
    private JMenu viewMenu;
    private JCheckBoxMenuItem buttonBarCheckBoxMenuItem;
    private JCheckBoxMenuItem locationBarCheckBoxMenuItem;
    private JCheckBoxMenuItem statusBarCheckBoxMenuItem;
    private JMenuItem backMenuItem;
    private JMenuItem forwardMenuItem;
    private JMenuItem reloadMenuItem;
    private JMenuItem stopMenuItem;

    WebBrowserMenuBar() {
      fileMenu = new JMenu();
      configureComponent(fileMenu, WebBrowserDecoratorComponentType.FILE_MENU);
      JMenuItem fileNewWindowMenuItem = new JMenuItem();
      configureComponent(fileNewWindowMenuItem, WebBrowserDecoratorComponentType.FILE_NEW_WINDOW_MENU_ITEM);
      fileNewWindowMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JWebBrowser newWebBrowser;
          switch(((INativeWebBrowser)webBrowser.getNativeComponent()).getRuntime()) {
            case WEBKIT:
              newWebBrowser = new JWebBrowser(JWebBrowser.useWebkitRuntime());
              break;
            case XULRUNNER:
              newWebBrowser = new JWebBrowser(JWebBrowser.useXULRunnerRuntime());
              break;
            default:
              newWebBrowser = new JWebBrowser();
              break;
          }
          JWebBrowser.copyAppearance(webBrowser, newWebBrowser);
          JWebBrowser.copyContent(webBrowser, newWebBrowser);
          JWebBrowserWindow webBrowserWindow = WebBrowserWindowFactory.create(newWebBrowser);
          webBrowserWindow.setVisible(true);
        }
      });
      fileMenu.add(fileNewWindowMenuItem);
      JMenuItem fileOpenLocationMenuItem = new JMenuItem();
      configureComponent(fileOpenLocationMenuItem, WebBrowserDecoratorComponentType.FILE_OPEN_LOCATION_MENU_ITEM);
      fileOpenLocationMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String path = askLocation();
          if(path != null) {
            webBrowser.navigate(path);
          }
        }

      });
      fileMenu.add(fileOpenLocationMenuItem);
      JMenuItem fileOpenFileMenuItem = new JMenuItem();
      configureComponent(fileOpenFileMenuItem, WebBrowserDecoratorComponentType.FILE_OPEN_FILE_MENU_ITEM);
      fileOpenFileMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser fileChooser = new JFileChooser();
          if(fileChooser.showOpenDialog(webBrowser) == JFileChooser.APPROVE_OPTION) {
            try {
              webBrowser.navigate(fileChooser.getSelectedFile().getAbsolutePath());
            } catch(Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      });
      fileMenu.add(fileOpenFileMenuItem);
      viewMenu = new JMenu();
      configureComponent(viewMenu, WebBrowserDecoratorComponentType.VIEW_MENU);
      JMenu viewToolbarsMenu = new JMenu();
      configureComponent(viewToolbarsMenu, WebBrowserDecoratorComponentType.VIEW_TOOLBARS_MENU);
      buttonBarCheckBoxMenuItem = new JCheckBoxMenuItem();
      configureComponent(buttonBarCheckBoxMenuItem, WebBrowserDecoratorComponentType.VIEW_TOOLBARS_BUTTON_BAR_CHECKBOX_MENU_ITEM);
      buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible());
      buttonBarCheckBoxMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          setButtonBarVisible(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      viewToolbarsMenu.add(buttonBarCheckBoxMenuItem);
      locationBarCheckBoxMenuItem = new JCheckBoxMenuItem();
      configureComponent(locationBarCheckBoxMenuItem, WebBrowserDecoratorComponentType.VIEW_TOOLBARS_LOCATION_BAR_CHECKBOX_MENU_ITEM);
      locationBarCheckBoxMenuItem.setSelected(isLocationBarVisible());
      locationBarCheckBoxMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          setLocationBarVisible(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      viewToolbarsMenu.add(locationBarCheckBoxMenuItem);
      viewMenu.add(viewToolbarsMenu);
      statusBarCheckBoxMenuItem = new JCheckBoxMenuItem();
      configureComponent(statusBarCheckBoxMenuItem, WebBrowserDecoratorComponentType.VIEW_STATUS_BAR_CHECKBOX_MENU_ITEM);
      statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible());
      statusBarCheckBoxMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          setStatusBarVisible(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      viewMenu.add(statusBarCheckBoxMenuItem);
      viewMenu.addSeparator();
      backMenuItem = new JMenuItem();
      configureComponent(backMenuItem, WebBrowserDecoratorComponentType.VIEW_BACK_MENU_ITEM);
      backMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.navigateBack();
          nativeWebBrowser.requestFocus();
        }
      });
      backMenuItem.setEnabled(false);
      viewMenu.add(backMenuItem);
      forwardMenuItem = new JMenuItem();
      configureComponent(forwardMenuItem, WebBrowserDecoratorComponentType.VIEW_FORWARD_MENU_ITEM);
      forwardMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.navigateForward();
          nativeWebBrowser.requestFocus();
        }
      });
      forwardMenuItem.setEnabled(false);
      viewMenu.add(forwardMenuItem);
      reloadMenuItem = new JMenuItem();
      configureComponent(reloadMenuItem, WebBrowserDecoratorComponentType.VIEW_RELOAD_MENU_ITEM);
      reloadMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.reloadPage();
          nativeWebBrowser.requestFocus();
        }
      });
      viewMenu.add(reloadMenuItem);
      stopMenuItem = new JMenuItem();
      configureComponent(stopMenuItem, WebBrowserDecoratorComponentType.VIEW_STOP_MENU_ITEM);
      stopMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.stopLoading();
        }
      });
      stopMenuItem.setEnabled(false);
      viewMenu.add(stopMenuItem);
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
      addMenuBarComponents(this);
    }

    /**
     * Get the file menu.
     * @return the file menu.
     */
    public JMenu getFileMenu() {
      return fileMenu;
    }

    /**
     * Get the view menu.
     * @return the view menu.
     */
    public JMenu getViewMenu() {
      return viewMenu;
    }

  }

  /**
   * The bar containing the buttons.
   * @author Christopher Deckers
   */
  public class WebBrowserButtonBar extends JToolBar {

    private JButton backButton;
    private JButton forwardButton;
    private JButton reloadButton;
    private JButton stopButton;

    WebBrowserButtonBar() {
      setFloatable(false);
      backButton = new JButton();
      configureComponent(backButton, WebBrowserDecoratorComponentType.BACK_BUTTON);
      backButton.setEnabled(menuBar.backMenuItem.isEnabled());
      backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.navigateBack();
          nativeWebBrowser.requestFocus();
        }
      });
      forwardButton = new JButton();
      configureComponent(forwardButton, WebBrowserDecoratorComponentType.FORWARD_BUTTON);
      forwardButton.setEnabled(menuBar.forwardMenuItem.isEnabled());
      forwardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.navigateForward();
          nativeWebBrowser.requestFocus();
        }
      });
      reloadButton = new JButton();
      configureComponent(reloadButton, WebBrowserDecoratorComponentType.RELOAD_BUTTON);
      reloadButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.reloadPage();
          nativeWebBrowser.requestFocus();
        }
      });
      stopButton = new JButton();
      configureComponent(stopButton, WebBrowserDecoratorComponentType.STOP_BUTTON);
      stopButton.setEnabled(menuBar.stopMenuItem.isEnabled());
      stopButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.stopLoading();
        }
      });
      addButtonBarComponents(this);
      add(Box.createHorizontalStrut(2));
    }

    public JButton getBackButton() {
      return backButton;
    }

    public JButton getForwardButton() {
      return forwardButton;
    }

    public JButton getReloadButton() {
      return reloadButton;
    }

    public JButton getStopButton() {
      return stopButton;
    }

  }

  /**
   * The bar containing the location field.
   * @author Christopher Deckers
   */
  public class WebBrowserLocationBar extends JToolBar {

    private JTextField locationField;
    private JButton goButton;

    WebBrowserLocationBar() {
      // We have to force the layout manager because in Synth L&F the text field does not take the full available width.
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setFloatable(false);
      locationField = new JTextField();
      locationField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            updateLocation();
            locationField.selectAll();
          }
        }
      });
      ActionListener goActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          webBrowser.navigate(locationField.getText());
          nativeWebBrowser.requestFocus();
        }
      };
      locationField.addActionListener(goActionListener);
      updateLocation();
      goButton = new JButton();
      configureComponent(goButton, WebBrowserDecoratorComponentType.GO_BUTTON);
      goButton.addActionListener(goActionListener);
      addLocationBarComponents(this);
    }

    public JTextField getLocationField() {
      return locationField;
    }

    public JButton getGoButton() {
      return goButton;
    }

    void updateLocation(String location) {
      locationField.setText(location);
    }

    void updateLocation() {
      locationField.setText(nativeWebBrowser.isNativePeerInitialized() && !nativeWebBrowser.isNativePeerDisposed()? nativeWebBrowser.getResourceLocation(): "");
    }

  }

  private class WebBrowserStatusBar extends JPanel {

    private JLabel statusLabel;
    private JProgressBar progressBar;

    public WebBrowserStatusBar() {
      super(new BorderLayout());
      setBorder(BorderFactory.createCompoundBorder(STATUS_BAR_BORDER, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      statusLabel = new JLabel();
      configureComponent(statusLabel, WebBrowserDecoratorComponentType.STATUS_LABEL);
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
      int loadingProgress = nativeWebBrowser.isNativePeerInitialized()? nativeWebBrowser.getLoadingProgress(): 100;
      progressBar.setValue(loadingProgress);
      progressBar.setVisible(loadingProgress < 100);
    }

    public void updateStatus() {
      String status = nativeWebBrowser.isNativePeerInitialized()? nativeWebBrowser.getStatusText(): "";
      statusLabel.setText(status.length() == 0? " ": status);
    }

  }

  /**
   * Add the components that compose the button bar.
   * Overriden versions do not need to call their super implementation, instead they can selectively add certain default components, for example: <code>buttonBar.add(buttonBar.getBackButton())</code>.
   */
  protected void addButtonBarComponents(WebBrowserButtonBar buttonBar) {
    buttonBar.add(buttonBar.getBackButton());
    buttonBar.add(buttonBar.getForwardButton());
    buttonBar.add(buttonBar.getReloadButton());
    buttonBar.add(buttonBar.getStopButton());
  }

  /**
   * Add the components that compose the location bar.<br>
   * Overriden versions do not need to call their super implementation, instead they can selectively add certain default components, for example: <code>locationBar.add(locationBar.getLocationField())</code>.
   */
  protected void addLocationBarComponents(WebBrowserLocationBar locationBar) {
    JPanel locationToolBarInnerPanel = new JPanel(new GridBagLayout());
    locationToolBarInnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
    locationToolBarInnerPanel.setOpaque(false);
    locationToolBarInnerPanel.add(locationBar.getLocationField(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    locationBar.add(locationToolBarInnerPanel);
    locationBar.add(locationBar.getGoButton());
  }

  /**
   * Add the components (most likely menus) that compose the menu bar.<br>
   * Overriden versions do not need to call their super implementation, instead they can selectively add certain default components, for example: <code>menuBar.add(menuBar.getFileMenu())</code>.
   * @param menuBar the bar to add the components to.
   */
  protected void addMenuBarComponents(WebBrowserMenuBar menuBar) {
    menuBar.add(menuBar.getFileMenu());
    menuBar.add(menuBar.getViewMenu());
  }

  private WebBrowserMenuBar menuBar;
  private WebBrowserButtonBar buttonBar;
  private WebBrowserLocationBar locationBar;
  private WebBrowserStatusBar statusBar;

  private JWebBrowser webBrowser;
  private INativeWebBrowser nativeWebBrowser;

  public DefaultWebBrowserDecorator(JWebBrowser webBrowser, Component renderingComponent) {
    this.webBrowser = webBrowser;
    nativeWebBrowser = (INativeWebBrowser)webBrowser.getNativeComponent();
    menuToolAndLocationBarPanel = new JPanel(new BorderLayout());
    menuBar = new WebBrowserMenuBar();
    menuToolAndLocationBarPanel.add(menuBar, BorderLayout.NORTH);
    add(menuToolAndLocationBarPanel, BorderLayout.NORTH);
    nativeWebBrowserBorderContainerPane = new JPanel(new BorderLayout());
    nativeWebBrowserBorderContainerPane.add(renderingComponent, BorderLayout.CENTER);
    add(nativeWebBrowserBorderContainerPane, BorderLayout.CENTER);
    nativeWebBrowser.addWebBrowserListener(new NWebBrowserListener());
    adjustBorder();
    setButtonBarVisible(true);
    setLocationBarVisible(true);
    setStatusBarVisible(true);
  }

  protected JWebBrowser getWebBrowser() {
    return webBrowser;
  }

  private JPanel menuToolAndLocationBarPanel;

  private JPanel nativeWebBrowserBorderContainerPane;

  private void adjustBorder() {
    nativeWebBrowserBorderContainerPane.setBorder(getInnerAreaBorder());
  }

  /**
   * Return the border to use for the inner area, which by default return a border if at least one of the bars is visible.
   * Note that this method is called every time the visibility of a bar changes.
   */
  protected Border getInnerAreaBorder() {
    Border border;
    if(isMenuBarVisible() || isButtonBarVisible() || isLocationBarVisible() || isStatusBarVisible()) {
      border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    } else {
      border = null;
    }
    return border;
  }

  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  @Override
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    if(isStatusBarVisible == isStatusBarVisible()) {
      return;
    }
    if(isStatusBarVisible) {
      statusBar = new WebBrowserStatusBar();
      webBrowser.add(statusBar, BorderLayout.SOUTH);
    } else {
      webBrowser.remove(statusBar);
      statusBar = null;
    }
    webBrowser.revalidate();
    webBrowser.repaint();
    menuBar.statusBarCheckBoxMenuItem.setSelected(isStatusBarVisible);
    adjustBorder();
  }

  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  @Override
  public boolean isStatusBarVisible() {
    return statusBar != null;
  }

  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  @Override
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
  @Override
  public boolean isMenuBarVisible() {
    return menuBar.isVisible();
  }

  /**
   * Set whether the button bar is visible.
   * @param isButtonBarVisible true if the button bar should be visible, false otherwise.
   */
  @Override
  public void setButtonBarVisible(boolean isButtonBarVisible) {
    if(isButtonBarVisible == isButtonBarVisible()) {
      return;
    }
    if(isButtonBarVisible) {
      buttonBar = new WebBrowserButtonBar();
      menuToolAndLocationBarPanel.add(buttonBar, BorderLayout.WEST);
    } else {
      menuToolAndLocationBarPanel.remove(buttonBar);
      buttonBar = null;
    }
    menuToolAndLocationBarPanel.revalidate();
    menuToolAndLocationBarPanel.repaint();
    menuBar.buttonBarCheckBoxMenuItem.setSelected(isButtonBarVisible);
    adjustBorder();
    if(isButtonBarVisible && !isViewMenuVisible) {
      updateNavigationButtons();
    }
  }

  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  @Override
  public boolean isButtonBarVisible() {
    return buttonBar != null;
  }

  /**
   * Set whether the location bar is visible.
   * @param isLocationBarVisible true if the location bar should be visible, false otherwise.
   */
  @Override
  public void setLocationBarVisible(boolean isLocationBarVisible) {
    if(isLocationBarVisible == isLocationBarVisible()) {
      return;
    }
    if(isLocationBarVisible) {
      locationBar = new WebBrowserLocationBar();
      menuToolAndLocationBarPanel.add(locationBar, BorderLayout.CENTER);
    } else {
      menuToolAndLocationBarPanel.remove(locationBar);
      locationBar = null;
    }
    menuToolAndLocationBarPanel.revalidate();
    menuToolAndLocationBarPanel.repaint();
    menuBar.locationBarCheckBoxMenuItem.setSelected(isLocationBarVisible);
    adjustBorder();
  }

  /**
   * Indicate whether the location bar is visible.
   * @return true if the location bar is visible.
   */
  @Override
  public boolean isLocationBarVisible() {
    return locationBar != null;
  }

  @Override
  public void configureForWebBrowserWindow(final JWebBrowserWindow webBrowserWindow) {
    JMenu fileMenu = menuBar.fileMenu;
    fileMenu.addSeparator();
    JMenuItem fileCloseMenuItem = new JMenuItem();
    configureComponent(fileCloseMenuItem, WebBrowserDecoratorComponentType.FILE_CLOSE_MENU_ITEM);
    fileCloseMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowserWindow.dispose();
      }
    });
    fileMenu.add(fileCloseMenuItem);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void titleChanged(WebBrowserEvent e) {
        setWebBrowserWindowTitle(webBrowserWindow, e.getWebBrowser().getPageTitle());
      }
    });
    setWebBrowserWindowIcon(webBrowserWindow);
  }

  protected void setWebBrowserWindowTitle(JWebBrowserWindow webBrowserWindow, String pageTitle) {
    webBrowserWindow.setTitle(new MessageFormat(RESOURCES.getString("BrowserTitle")).format(new Object[] {pageTitle}));
  }

  protected void setWebBrowserWindowIcon(JWebBrowserWindow webBrowserWindow) {
    String value = RESOURCES.getString("BrowserIcon");
    if(value.length() > 0) {
      webBrowserWindow.setIconImage(new ImageIcon(JWebBrowserWindow.class.getResource(value)).getImage());
    }
  }

  /**
   * Ask the user, by default using an option pane (input dialog), the location to open.
   */
  protected String askLocation() {
    return JOptionPane.showInputDialog(webBrowser, RESOURCES.getString("FileOpenLocationDialogMessage"), RESOURCES.getString("FileOpenLocationDialogTitle"), JOptionPane.QUESTION_MESSAGE);
  }

  /**
   * Configure a component (its text, icon, tooltip, etc.).
   */
  protected void configureComponent(JComponent c, WebBrowserDecoratorComponentType componentType) {
    switch(componentType) {
      case FILE_MENU: ((AbstractButton)c).setText(RESOURCES.getString("FileMenu")); return;
      case FILE_NEW_WINDOW_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("FileNewWindowMenu")); return;
      case FILE_OPEN_LOCATION_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("FileOpenLocationMenu")); return;
      case FILE_OPEN_FILE_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("FileOpenFileMenu")); return;
      case FILE_CLOSE_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("FileCloseMenu")); return;
      case VIEW_MENU: ((AbstractButton)c).setText(RESOURCES.getString("ViewMenu")); return;
      case VIEW_TOOLBARS_MENU: ((AbstractButton)c).setText(RESOURCES.getString("ViewToolbarsMenu")); return;
      case VIEW_TOOLBARS_BUTTON_BAR_CHECKBOX_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("ViewToolbarsButtonBarMenu")); return;
      case VIEW_TOOLBARS_LOCATION_BAR_CHECKBOX_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("ViewToolbarsLocationBarMenu")); return;
      case VIEW_STATUS_BAR_CHECKBOX_MENU_ITEM: ((AbstractButton)c).setText(RESOURCES.getString("ViewStatusBarMenu")); return;
      case VIEW_BACK_MENU_ITEM: {
        ((AbstractButton)c).setText(RESOURCES.getString("ViewMenuBack"));
        ((AbstractButton)c).setIcon(createIcon("ViewMenuBackIcon"));
        return;
      }
      case VIEW_FORWARD_MENU_ITEM: {
        ((AbstractButton)c).setText(RESOURCES.getString("ViewMenuForward"));
        ((AbstractButton)c).setIcon(createIcon("ViewMenuForwardIcon"));
        return;
      }
      case VIEW_RELOAD_MENU_ITEM: {
        ((AbstractButton)c).setText(RESOURCES.getString("ViewMenuReload"));
        ((AbstractButton)c).setIcon(createIcon("ViewMenuReloadIcon"));
        return;
      }
      case VIEW_STOP_MENU_ITEM: {
        ((AbstractButton)c).setText(RESOURCES.getString("ViewMenuStop"));
        ((AbstractButton)c).setIcon(createIcon("ViewMenuStopIcon"));
        return;
      }
      case BACK_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("BackIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("BackText"));
        return;
      }
      case FORWARD_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("ForwardIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("ForwardText"));
        return;
      }
      case RELOAD_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("ReloadIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("ReloadText"));
        return;
      }
      case STOP_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("StopIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("StopText"));
        return;
      }
      case GO_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("GoIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("GoText"));
        return;
      }
      case STATUS_LABEL:
        return;
    }
    throw new IllegalStateException("Type not handled: " + componentType);
  }

  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JWebBrowser.class.getResource(value));
  }

}
