/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;

/**
 * A component to display some content with syntax highlighting. It is a browser-based component, which relies on the SyntaxHighlighter library.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JSyntaxHighlighter extends NSPanelComponent {

  private static final String PACKAGE_PREFIX = "/dp.SyntaxHighlighter/";

  private JWebBrowser webBrowser;

  private static final String LS = Utils.LINE_SEPARATOR;

  /**
   * Construct an HTML editor.
   * @param options the options to configure the behavior of this component.
   */
  public JSyntaxHighlighter(NSOption... options) {
    if(getClass().getResource(PACKAGE_PREFIX + "Styles/SyntaxHighlighter.css") == null) {
      throw new IllegalStateException("The SyntaxHighlighter distribution is missing from the classpath!");
    }
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    webBrowser.setDefaultPopupMenuRegistered(false);
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
  }

  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }

  public static enum ContentLanguage {
    Cpp("c++", "Cpp"),
    CSharp("c#", "CSharp"),
    CSS("css", "Css"),
    Delphi("delphi", "Delphi"),
    Java("java", "Java"),
    Javascript("js", "JScript"),
    PHP("php", "Php"),
    Python("python", "Python"),
    Ruby("ruby", "Ruby"),
    SQL("sql", "Sql"),
    VB("vb", "Vb"),
    XML("xml", "Xml"),
    HTML("html", "Xml"), // html is same as xml
    ;

    private String language;
    private String fileName;

    private ContentLanguage(String language, String fileName) {
      this.language = language;
      this.fileName = fileName;
    }

    String getLanguage() {
      return language;
    }

    String getFileName() {
      return fileName;
    }

  }

  /**
   * Set the content.
   * @param content the content.
   * @param language the type (Java, CSharp, etc.).
   */
  public void setContent(String content, ContentLanguage language) {
    setContent(content, language, null);
  }

  /**
   * Set the content.
   * @param content the content.
   * @param language the type (Java, CSharp, etc.).
   * @param options the options.
   */
  public void setContent(String content, ContentLanguage language, SyntaxHighlighterOptions options) {
    if(language == null) {
      throw new IllegalArgumentException("The language cannot be null!");
    }
    if(options == null) {
      options = new SyntaxHighlighterOptions();
    }
    String hContent =
      "<html>" + LS +
      "  <head>" + LS +
      "    <link type=\"text/css\" rel=\"stylesheet\" href=\"" + WebServer.getDefaultWebServer().getClassPathResourceURL(null, "/dp.SyntaxHighlighter/Styles/SyntaxHighlighter.css") + "\"></link>" + LS +
      "    <script language=\"javascript\" src=\"" + WebServer.getDefaultWebServer().getClassPathResourceURL(null, "/dp.SyntaxHighlighter/Scripts/shCore.js") + "\"></script>" + LS +
      "    <script language=\"javascript\" src=\"" + WebServer.getDefaultWebServer().getClassPathResourceURL(null, "/dp.SyntaxHighlighter/Scripts/shBrush" + language.getFileName() + ".js") + "\"></script>" + LS +
      "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
      "      <!--" + LS +
      "      function init() {" + LS +
      "        dp.SyntaxHighlighter.ClipboardSwf = '" + WebServer.getDefaultWebServer().getClassPathResourceURL(null, "/dp.SyntaxHighlighter/Scripts/clipboard.swf") + "';" + LS +
      "        dp.SyntaxHighlighter.HighlightAll('code');" + LS +
      "      }" + LS +
      "      //-->" + LS +
      "    </script>" + LS +
      "    <style type=\"text/css\">" + LS +
      "      html, body { width: 100%; height: 100%; min-height: 100%; margin: 0; padding: 0; white-space: nowrap; background-color: #FFFFFF; }" + LS +
      // A special div is automatically added, and we have to tweak how scroll bars behave.
      "      div.dp-highlighter { overflow: visible; }" + LS +
      // There is a margin set to 18px that messes up scroll bars. We have to compensate that.
      "      div.wrapper { width: 100%; height: 100%; min-height: 100%; padding: 0; margin: -18px 0; white-space: nowrap; }" + LS +
      "    </style>" + LS +
      "  </head>" + LS +
      "  <body id=\"body\">" +
      "    <div class=\"wrapper\">" +
      "      <pre name=\"code\" class=\"" + language.getLanguage() + "\">" +
      // Do not call "Utils.escapeXML(content)": "&apos;" must not be replaced.
      escapeXML(content) +
      "</pre>" + LS +
      "    </div>" +
      "  </body>" + LS +
      "<script language=\"JavaScript\" type=\"text/javascript\">" + LS +
      "  <!--" + LS +
      "    init();" + LS +
      "  //-->" + LS +
      "</script>" + LS +
      "</html>";
    webBrowser.setHTMLContent(hContent);
  }

  private static String escapeXML(String s) {
    if(s == null || s.length() == 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder((int)(s.length() * 1.1));
    for(int i=0; i<s.length(); i++) {
      char c = s.charAt(i);
      switch(c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
//        case '\'':
//          sb.append("&apos;");
//          break;
        case '\"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
        break;
      }
    }
    return sb.toString();
  }

}
