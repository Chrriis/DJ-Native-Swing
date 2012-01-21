/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.DefaultWebBrowserDecorator;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser.WebBrowserDecoratorFactory;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserDecorator;

/**
 * @author Christopher Deckers
 */
public class CustomDecorators {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    // We create a web browser that replaces its decorator.
    final JWebBrowser webBrowser = new JWebBrowser() {
      @Override
      protected WebBrowserDecorator createWebBrowserDecorator(Component renderingComponent) {
        return createCustomWebBrowserDecorator(this, renderingComponent);
      }
    };
    webBrowser.navigate("http://www.google.com");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    JPanel southPanel = new JPanel();
    southPanel.setBorder(BorderFactory.createTitledBorder("Global change of decorator to customize a whole application"));
    JButton setCustomButton = new JButton("Set custom decorator for all instances");
    setCustomButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JWebBrowser.setWebBrowserDecoratorFactory(new WebBrowserDecoratorFactory() {
          public WebBrowserDecorator createWebBrowserDecorator(JWebBrowser webBrowser, Component renderingComponent) {
            return createCustomWebBrowserDecorator(webBrowser, renderingComponent);
          }
        });
      }
    });
    southPanel.add(setCustomButton);
    JButton setDefaultsButton = new JButton("Reset to defaults");
    setDefaultsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JWebBrowser.setWebBrowserDecoratorFactory(null);
      }
    });
    southPanel.add(setDefaultsButton);
    contentPane.add(southPanel, BorderLayout.SOUTH);
    return contentPane;
  }

  private static WebBrowserDecorator createCustomWebBrowserDecorator(JWebBrowser webBrowser, Component renderingComponent) {
    // Let's extend the default decorator.
    // We could rewrite our own decorator, but this is far more complex and we generally do not need this.
    return new DefaultWebBrowserDecorator(webBrowser, renderingComponent) {
      @Override
      protected void addMenuBarComponents(WebBrowserMenuBar menuBar) {
        // We let the default menus to be added and then we add ours.
        super.addMenuBarComponents(menuBar);
        JMenu myMenu = new JMenu("[[My Custom Menu]]");
        myMenu.add(new JMenuItem("My Custom Item 1"));
        myMenu.add(new JMenuItem("My Custom Item 2"));
        menuBar.add(myMenu);
      }
      @Override
      protected void addButtonBarComponents(WebBrowserButtonBar buttonBar) {
        // We completely override this method so we decide which buttons to add
        buttonBar.add(buttonBar.getBackButton());
        final JButton button = new JButton("[[My Custom Button!]]");
        button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(button, "My Custom Button was pressed!");
          }
        });
        buttonBar.add(button);
        buttonBar.add(buttonBar.getForwardButton());
        buttonBar.add(buttonBar.getReloadButton());
        buttonBar.add(buttonBar.getStopButton());
      }
    };
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterface.open();
    UIUtils.setPreferredLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createContent(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
