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
public class WebStartAndApplets {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JEditorPane editorPane = new JEditorPane("text/html",
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>WebStart support</h1>" + LS +
        "    <p>An application using DJ NativeSwing can be deployed through Java WebStart.<br/>" + LS +
        "      With recent versions of Java (6u18+), there is one system property to set in the JNLP descriptor:" + LS +
        "      <pre>-Dsun.awt.disableMixing=true</pre>" + LS +
        "    </p>" + LS +
        "    <h1>Applets support </h1>" + LS +
        "    <p>It is possible to write applets that use DJ NativeSwing, using the NativeInterfaceAppletHandler class.<br/>" + LS +
        "      With recent versions of Java (6u18+), there is one system property to set:" + LS +
        "      <pre>-Dsun.awt.disableMixing=true</pre>" + LS +
        "      To set this system property, one needs to declare the applet using the new JNLP-desctriptor based approach." + LS +
        "    </p>" + LS +
        "  </body>" + LS +
        "</html>");
    editorPane.setEditable(false);
    contentPane.add(new JScrollPane(editorPane));
    return contentPane;
  }

}
