/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

/**
 * @author Christopher Deckers
 */
public class SendingCommands extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public SendingCommands() {
    super(new BorderLayout());
    JPanel contentPane = new JPanel(new BorderLayout(5, 5));
    JPanel commandPanel = new JPanel(new BorderLayout());
    commandPanel.add(new JLabel("Received command: "), BorderLayout.WEST);
    commandPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    final JTextField receivedCommandTextField = new JTextField();
    commandPanel.add(receivedCommandTextField, BorderLayout.CENTER);
    contentPane.add(commandPanel, BorderLayout.SOUTH);
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command, Object[] args) {
        String commandText = command;
        if(args.length > 0) {
          commandText += " " + Arrays.toString(args);
        }
        receivedCommandTextField.setText(commandText);
        if("store".equals(command)) {
          String data = (String)args[0];
          if(JOptionPane.showConfirmDialog(webBrowser, "Do you want to store \"" + data + "\" in a database?\n(Not for real of course!)", "Data received from the web browser", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Data should be used here
          }
        }
      }
    });
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <head>" + LS +
        "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
        "      <!--" + LS +
        "      function sendCommand(command) {" + LS +
        "        var s = 'command://' + encodeURIComponent(command);" + LS +
        "        for(var i=1; i<arguments.length; s+='&'+encodeURIComponent(arguments[i++]));" + LS +
        "        window.location = s;" + LS +
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
        "    <form name=\"form2\" onsubmit=\"sendCommand('store', form2.commandField.value); return false\">" + LS +
        "      A more concrete example: ask the application to store some data in a database, by sending a command with some arguments:<br/>" + LS +
        "      Client: <input name=\"commandField\" type=\"text\" value=\"John Smith\"/>" + LS +
        "      <input type=\"button\" value=\"Send\" onclick=\"sendCommand('store', form2.commandField.value)\"/>" + LS +
        "    </form>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    add(contentPane, BorderLayout.CENTER);
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SendingCommands(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
