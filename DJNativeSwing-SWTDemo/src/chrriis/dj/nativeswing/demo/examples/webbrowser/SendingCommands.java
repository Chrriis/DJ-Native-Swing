/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.webbrowser;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import chrriis.dj.nativeswing.ui.JWebBrowser;
import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;

/**
 * @author Christopher Deckers
 */
public class SendingCommands extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public SendingCommands() {
    super(new BorderLayout(0, 0));
    JPanel contentPane = new JPanel(new BorderLayout(5, 5));
    JPanel commandPanel = new JPanel(new BorderLayout(0, 0));
    commandPanel.add(new JLabel("Received command: "), BorderLayout.WEST);
    commandPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    final JTextField receivedCommandTextField = new JTextField();
    commandPanel.add(receivedCommandTextField, BorderLayout.CENTER);
    contentPane.add(commandPanel, BorderLayout.SOUTH);
    JPanel webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command) {
        receivedCommandTextField.setText(command);
        if(command.startsWith("store:")) {
          String data = command.substring("store:".length());
          if(JOptionPane.showConfirmDialog(webBrowser, "Do you want to store \"" + data + "\" in a database?\n(Not for real of course!)", "Data received from the web browser", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Here the data should be used
          }
        }
      }
    });
    webBrowser.setText(
        "<html>" + LS +
        "  <head>" + LS +
        "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
        "      <!--" + LS +
        "      function sendCommand(command) {" + LS +
        "        window.location = 'command://' + encodeURIComponent(command);" + LS +
        "      }" + LS +
        "      //-->" + LS +
        "    </script>" + LS +
        "  </head>" + LS +
        "  <body>" + LS +
        "    <a href=\"command://A%20static%20command\">A static link, with a predefined command</a><br/>" + LS +
        "    <form name=\"form\" onsubmit=\"sendCommand(form.commandField.value); return false\">" + LS +
        "      A dynamic command, sent through Javascript:<br/>" + LS +
        "      <input name=\"commandField\" type=\"text\" value=\"some command\"/>" + LS +
        "      <input type=\"button\" value=\"Send\" onclick=\"sendCommand(form.commandField.value)\"/>" + LS +
        "    </form>" + LS +
        "    <form name=\"form2\" onsubmit=\"sendCommand('store:' + form2.commandField.value); return false\">" + LS +
        "      A more concrete example: ask the application to store some data in a database:<br/>" + LS +
        "      Client: <input name=\"commandField\" type=\"text\" value=\"John Smith\"/>" + LS +
        "      <input type=\"button\" value=\"Send\" onclick=\"sendCommand('store:' + form2.commandField.value)\"/>" + LS +
        "    </form>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    add(contentPane, BorderLayout.CENTER);
  }
  
}
