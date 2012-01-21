/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.introduction;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Christopher Deckers
 */
public class Codewise {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JEditorPane editorPane = new JEditorPane("text/html",
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>Using the library is straightforward</h1>" + LS +
        "    <p>Simply make a few additions to the main method, and the rest is just using the API exposed by the components:<br/>" + LS +
        "    <pre>" + LS +
        "      public static void main(String[] args) {" + LS +
        "        NativeInterface.open();" + LS +
        "        // Here goes the rest of the program initialization" + LS +
        "        NativeInterface.runEventPump();" + LS +
        "      }</pre>" + LS +
        "    </p>" + LS +
        "    <p>Note that NativeInterface.runEventPump() is optional on certain platforms (Linux, Windows).</p>" + LS +
        "    <h1>Native components are Swing-like</h1>" + LS +
        "    <p>Try the next examples and look at the source code: they could not be easier to manipulate.</p>" + LS +
        "    <p>Note that Swing expects the components to be manipulated in the user interface thread. It is not enforced but DJ Native Swing enforces it.<br/>" + LS +
        "    For that reason, don't forget to use such code if you are not in the right thread:<br/>" + LS +
        "    <pre>" + LS +
        "      SwingUtilities.invokeLater(new Runnable() {" + LS +
        "        public void run() {" + LS +
        "          // Here goes user interface calls" + LS +
        "        }" + LS +
        "      });</pre>" + LS +
        "    </p>" + LS +
        "  </body>" + LS +
        "</html>");
    editorPane.setEditable(false);
    contentPane.add(new JScrollPane(editorPane));
    return contentPane;
  }

}
