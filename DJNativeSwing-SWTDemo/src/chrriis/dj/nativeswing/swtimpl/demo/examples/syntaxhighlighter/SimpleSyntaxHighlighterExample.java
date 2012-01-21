/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.syntaxhighlighter;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JSyntaxHighlighter;

/**
 * @author Christopher Deckers
 */
public class SimpleSyntaxHighlighterExample {

  private static final String LS = Utils.LINE_SEPARATOR;

  public static JComponent createContent() {
    JSyntaxHighlighter syntaxHighlighter = new JSyntaxHighlighter();
    syntaxHighlighter.setContent(
        "/************************" + LS +
        " * This is some C# code *" + LS +
        " ************************/" + LS +
        "public class Foo" + LS +
        "{" + LS +
        "    /// <summary>A summary of the method.</summary>" + LS +
        "    /// <param name=\"firstParam\">A description of the parameter.</param>" + LS +
        "    /// <remarks>Remarks about the method.</remarks>" + LS +
        "    public static void Bar(int firstParam) {}" + LS +
        "}"
        , JSyntaxHighlighter.ContentLanguage.CSharp);
    return syntaxHighlighter;
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
