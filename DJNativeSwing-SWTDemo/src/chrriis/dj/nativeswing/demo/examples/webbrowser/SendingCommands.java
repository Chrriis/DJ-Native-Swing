/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
    final JLabel commandLabel = new JLabel("-");
    commandPanel.add(commandLabel, BorderLayout.CENTER);
    contentPane.add(commandPanel, BorderLayout.SOUTH);
    JPanel webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command) {
        commandLabel.setText("\"" + command + "\"");
        if(command.startsWith("store:")) {
          Window w = SwingUtilities.getWindowAncestor(webBrowser);
          final JDialog dialog;
          String title = "Store message from Web Browser";
          if(w instanceof Frame) {
            dialog = new JDialog((Frame)w, title, true);
          } else {
            dialog = new JDialog((Dialog)w, title, true);
          }
          Container contentPane = dialog.getContentPane();
          JPanel labelPanel = new JPanel(new BorderLayout(0, 0));
          labelPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
          JLabel label = new JLabel("Do you want to store \"" + command.substring("store:".length()) + "\" in a database?");
          labelPanel.add(label, BorderLayout.CENTER);
          contentPane.add(labelPanel, BorderLayout.CENTER);
          JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
          ActionListener disposeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              dialog.dispose();
            }
          };
          JButton button = new JButton("Nope");
          button.addActionListener(disposeListener);
          buttonPanel.add(button);
          button = new JButton("Don't want");
          button.addActionListener(disposeListener);
          buttonPanel.add(button);
          button = new JButton("Not sure...");
          button.addActionListener(disposeListener);
          buttonPanel.add(button);
          button = new JButton("Never!");
          button.addActionListener(disposeListener);
          buttonPanel.add(button);
          contentPane.add(buttonPanel, BorderLayout.SOUTH);
          dialog.pack();
          dialog.setLocationRelativeTo(webBrowser);
          dialog.setVisible(true);
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
        "      A more concrete example would be to ask the application to store some data in a database:<br/>" + LS +
        "      <input name=\"commandField\" type=\"text\" value=\"some data\"/>" + LS +
        "      <input type=\"button\" value=\"Send\" onclick=\"sendCommand('store:' + form2.commandField.value)\"/>" + LS +
        "    </form>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    add(contentPane, BorderLayout.CENTER);
  }
  
}
