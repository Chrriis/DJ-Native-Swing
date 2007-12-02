/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.evolutions;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Christopher Deckers
 */
public class WorkInProgress extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public WorkInProgress() {
    super(new BorderLayout(0, 0));
    JEditorPane editorPane = new JEditorPane("text/html", 
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>A Web Browser and a Flash player: more?</h1>" + LS +
        "    <p>The web browser component is definitely the most wanted native component one would want to integrate in a Swing application.<br/>" + LS +
        "    The next one that usually comes to mind is a flash player, as Flash is quite ubiquitus now.</p>" + LS +
        "    <p>Other candidates are:" + LS +
        "      <ul>" + LS +
        "        <li>A native multimedia player.</li>" + LS +
        "        <li>Office suite components.</li>" + LS +
        "        <li>Custom third party components.</li>" + LS +
        "      </ul>" + LS +
        "    </p>" + LS +
        "    <p>Note that the web browser component can be used to open various files (Flash applications, PDF files, etc.) but programmatically controlling the content is usually limited if not possible.</p>" + LS +
        "    <h1>Non-portable controls</h1>" + LS +
        "    <p>On the Windows operating system, Ole controls can be integrated easily in a Swing application with this library. The general rule is to create a Swing component with a Swing-like API that uses the library to manipulate the Ole control.</p>" + LS +
        "    <p>In fact, this library contains an Ole-based version of a Media Player, that can serve as an example to create other bridged Ole controls.</p>" + LS +
        "    <p>It is highly probable that this library allows to integrate other platform specific controls.</p>" + LS +
        "  </body>" + LS +
        "</html");
    editorPane.setEditable(false);
    add(new JScrollPane(editorPane));
  }
  
}
