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
public class TheSolution {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JEditorPane editorPane = new JEditorPane("text/html",
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>What about SWT then?</h1>" + LS +
        "    <p>SWT is a different animal. It is not as flexible as Swing and portability is far less guaranteed. Nevertheless, it is really great at platform integration, well maintained, and must-have native components are offered on the major platforms.</p>" + LS +
        "    <h1>Bridging Swing and SWT?</h1>" + LS +
        "    <p>The ideal would be a layer that offers the major native components of SWT, hidden behind a Swing-like API, taking care of all the gory details. That is exactly the goal of the DJ Project - NativeSwing.</p>" + LS +
        "    <h1>What about common integration issues?</h1>" + LS +
        "    <p>The library takes care of all the points that were raised:" + LS +
        "      <ul>" + LS +
        "        <li>All Swing popups are turned to heavyweight components in order to avoid overlapping glitches.</li>" + LS +
        "        <li>Hidden heavyweight components are prevented from messing up with the focus.</li>" + LS +
        "        <li>Swing modality is tracked to automatically adjust the modality of the embedded native components.</li>" + LS +
        "        <li>SWT is mostly portable: the important components are available on most platforms with a consistent API.</li>" + LS +
        "        <li>The threading of the two toolkits is taken care internally, so users of the library only have to deal with Swing's threading.</li>" + LS +
        "      </ul>" + LS +
        "    </p>" + LS +
        "  </body>" + LS +
        "</html>");
    editorPane.setEditable(false);
    contentPane.add(new JScrollPane(editorPane));
    return contentPane;
  }

}
