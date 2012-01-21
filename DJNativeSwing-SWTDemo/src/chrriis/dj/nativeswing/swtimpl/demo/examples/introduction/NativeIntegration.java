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
public class NativeIntegration {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JEditorPane editorPane = new JEditorPane("text/html",
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>Why Swing? Why native integration?</h1>" + LS +
        "    <p>Swing is a powerful user-interface toolkit. You can almost render any effects without caring much about portability, since most is handled in a lightweight manner.</p>" + LS +
        "    <p>This portability has a drawback: if something cannot be rendered exactly the same across the supported platforms, the Java Runtime Environment (JRE) will not include the feature. This is the reason why there is no proper web browser component in the JRE.<br/>" + LS +
        "    Of course, sometimes having native components make sense, but I have yet to find a solution that I like...</p>" + LS +
        "    <h1>Mixing native components in Swing</h1>" + LS +
        "    <p>Swing is lightweight, and mixing native components (heavyweight) generates all sorts of problems. Here are some of the most commong problems:" + LS +
        "      <ul>" + LS +
        "        <li>Lightweight and heavyweight components produce visual glitches, like Swing popup menus, tooltips and combo drop menu to appear behind the native components.</li>" + LS +
        "        <li>Hidden heavyweight components added to the user interface steal the focus, or mess it up.</li>" + LS +
        "        <li>Swing modality works for Swing components, but the embedded native component are not blocked.</li>" + LS +
        "        <li>Native integration is hardly portable (by definition).</li>" + LS +
        "        <li>The threading of the user interface is different in Swing and the native components, as each have their own event pump. Deadlocks occur easily.</li>" + LS +
        "      </ul>" + LS +
        "    </p>" + LS +
        "  </body>" + LS +
        "</html>");
    editorPane.setEditable(false);
    contentPane.add(new JScrollPane(editorPane));
    return contentPane;
  }

}
