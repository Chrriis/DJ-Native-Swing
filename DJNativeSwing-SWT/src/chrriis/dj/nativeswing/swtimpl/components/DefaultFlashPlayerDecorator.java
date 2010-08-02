/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import chrriis.common.WebServer;

/**
 * @author Christopher Deckers
 */
public class DefaultFlashPlayerDecorator extends FlashPlayerDecorator {

  public static enum FlashDecoratorComponentType {
    PLAY_BUTTON,
    PAUSE_BUTTON,
    STOP_BUTTON,
  }

  private final ResourceBundle RESOURCES;

  {
    String className = JFlashPlayer.class.getName();
    RESOURCES = ResourceBundle.getBundle(className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resource/FlashPlayer");
  }

  public class FlashPlayerControlBar extends JPanel {

    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;

    private WebBrowserAdapter webBrowserListener;

    FlashPlayerControlBar() {
      super(new FlowLayout(FlowLayout.CENTER, 4, 2));
      playButton = new JButton();
      configureComponent(playButton, FlashDecoratorComponentType.PLAY_BUTTON);
      playButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flashPlayer.play();
        }
      });
      pauseButton = new JButton();
      configureComponent(pauseButton, FlashDecoratorComponentType.PAUSE_BUTTON);
      pauseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flashPlayer.pause();
        }
      });
      stopButton = new JButton();
      configureComponent(stopButton, FlashDecoratorComponentType.STOP_BUTTON);
      stopButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flashPlayer.stop();
        }
      });
      adjustButtonState();
      webBrowserListener = new WebBrowserAdapter() {
        @Override
        public void locationChanged(WebBrowserNavigationEvent e) {
          adjustButtonState();
        }
      };
      flashPlayer.getWebBrowser().addWebBrowserListener(webBrowserListener);
      addControlBarComponents(this, this);
    }

    void disconnect() {
      flashPlayer.getWebBrowser().removeWebBrowserListener(webBrowserListener);
    }

    void adjustButtonState() {
      String resourceLocation = flashPlayer.getWebBrowser().getResourceLocation();
      boolean isEnabled = resourceLocation != null && resourceLocation.startsWith(WebServer.getDefaultWebServer().getURLPrefix());
      playButton.setEnabled(isEnabled);
      pauseButton.setEnabled(isEnabled);
      stopButton.setEnabled(isEnabled);
    }

    public JButton getPlayButton() {
      return playButton;
    }

    public JButton getPauseButton() {
      return pauseButton;
    }

    public JButton getStopButton() {
      return stopButton;
    }

  }

  private JFlashPlayer flashPlayer;
  private FlashPlayerControlBar controlBar;

  public DefaultFlashPlayerDecorator(JFlashPlayer flashPlayer, Component renderingComponent) {
    this.flashPlayer = flashPlayer;
    nativeComponentBorderContainerPane = new JPanel(new BorderLayout());
    nativeComponentBorderContainerPane.add(renderingComponent, BorderLayout.CENTER);
    add(nativeComponentBorderContainerPane, BorderLayout.CENTER);
    setControlBarVisible(false);
  }

  protected JFlashPlayer getFlashPlayer() {
    return flashPlayer;
  }

  private JPanel nativeComponentBorderContainerPane;

  private void adjustBorder() {
    nativeComponentBorderContainerPane.setBorder(getInnerAreaBorder());
  }

  /**
   * Return the border to use for the inner area, which by default return a border if at least one of the bars is visible.
   * Note that this method is called every time the visibility of a bar changes.
   */
  protected Border getInnerAreaBorder() {
    Border border;
    if(isControlBarVisible()) {
      border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    } else {
      border = null;
    }
    return border;
  }

  @Override
  public void setControlBarVisible(boolean isControlBarVisible) {
    if(isControlBarVisible == isControlBarVisible()) {
      return;
    }
    if(isControlBarVisible) {
      controlBar = new FlashPlayerControlBar();
      add(controlBar, BorderLayout.SOUTH);
    } else {
      remove(controlBar);
      controlBar.disconnect();
      controlBar = null;
    }
    revalidate();
    repaint();
    adjustBorder();
  }

  @Override
  public boolean isControlBarVisible() {
    return controlBar != null;
  }

  /**
   * Add the components that compose the control bar.
   * Overriden versions do not need to call their super implementation, instead they can selectively add certain default components, for example: <code>buttonContainer.add(controlBar.getPlayButton())</code>.
   */
  protected void addControlBarComponents(FlashPlayerControlBar controlBar, JComponent buttonContainer) {
    buttonContainer.add(controlBar.getPlayButton());
    buttonContainer.add(controlBar.getPauseButton());
    buttonContainer.add(controlBar.getStopButton());
  }

  /**
   * Configure a component (its text, icon, tooltip, etc.).
   */
  protected void configureComponent(JComponent c, FlashDecoratorComponentType componentType) {
    switch(componentType) {
      case PLAY_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("PlayIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("PlayText"));
        return;
      }
      case PAUSE_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("PauseIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("PauseText"));
        return;
      }
      case STOP_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("StopIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("StopText"));
        return;
      }
    }
    throw new IllegalStateException("Type not handled: " + componentType);
  }

  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JFlashPlayer.class.getResource(value));
  }

}
