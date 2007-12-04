/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import chrriis.dj.nativeswing.ui.event.FlashPlayerListener;
import chrriis.dj.nativeswing.ui.event.FlashPlayerWindowOpeningEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowOpeningEvent;

/**
 * @author Christopher Deckers
 */
public class JFlashPlayer extends JPanel {

  protected final ResourceBundle RESOURCES = ResourceBundle.getBundle(JFlashPlayer.class.getPackage().getName().replace('.', '/') + "/resource/FlashPlayer");

  protected JPanel webBrowserPanel;
  protected JWebBrowser webBrowser;
  
  protected JPanel controlBarPane;
  protected JButton playButton;
  protected JButton pauseButton;
  protected JButton stopButton;
  
  public JFlashPlayer() {
    super(new BorderLayout(0, 0));
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command) {
        if(command.startsWith("getVariableFM:")) {
          getVariableResult = command.substring("getVariableFM:".length());
        }
      }
//      @Override
//      public void urlChanging(WebBrowserNavigationEvent e) {
//        if(url == null || !url.equals(e.getNewURL())) {
//          e.consume();
//        }
//      }
      @Override
      public void windowOpening(WebBrowserWindowOpeningEvent ev) {
        Object[] listeners = listenerList.getListenerList();
        FlashPlayerWindowOpeningEvent e = null;
        for(int i=listeners.length-2; i>=0 && !ev.isConsumed(); i-=2) {
          if(listeners[i] == FlashPlayerListener.class) {
            if(e == null) {
              e = new FlashPlayerWindowOpeningEvent(JFlashPlayer.this, ev.getNewWebBrowser(), ev.getNewURL(), ev.getLocation(), ev.getSize());
            }
            ((FlashPlayerListener)listeners[i + 1]).windowOpening(e);
            if(e.isConsumed()) {
              ev.consume();
            } else {
              ev.setNewWebBrowser(e.getNewWebBrowser());
            }
          }
        }
      }
    });
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    controlBarPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    playButton = new JButton(createIcon("PlayIcon"));
    playButton.setToolTipText(RESOURCES.getString("PlayText"));
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        play();
      }
    });
    controlBarPane.add(playButton);
    pauseButton = new JButton(createIcon("PauseIcon"));
    pauseButton.setToolTipText(RESOURCES.getString("PauseText"));
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pause();
      }
    });
    controlBarPane.add(pauseButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stop();
      }
    });
    controlBarPane.add(stopButton);
    add(controlBarPane, BorderLayout.SOUTH);
    adjustBorder();
  }
  
  protected void adjustBorder() {
    if(isControlBarVisible()) {
      webBrowserPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    } else {
      webBrowserPanel.setBorder(null);
    }
  }
  
  protected Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JWebBrowser.class.getResource(value));
  }
  
  protected static final String LS = System.getProperty("line.separator");

  protected String url;
  
  public String getURL() {
    return url;
  }
  
  @SuppressWarnings("deprecation")
  public void setURL(String url) {
    this.url = url;
    if(url == null) {
      webBrowser.setText("");
      return;
    }
    try {
      new URL(url);
    } catch(Exception e) {
      url = new File(url).toURI().toString();
    }
    File tempJSFile;
    try {
      tempJSFile = File.createTempFile("nsfp", ".js");
      BufferedWriter out = new BufferedWriter(new FileWriter(tempJSFile));
      String escapedURL = escapeXML(url);
      out.write(
          "<!--" + LS +
          "window.document.write('<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"\" id=\"myFlashMovie\">');" + LS +
          "window.document.write('  <param name=\"movie\" value=\"' + decodeURIComponent('" + escapedURL + "') + '\";\">');" + LS +
          "window.document.write('  <embed play=\"" + isAutoStart + "\" swliveconnect=\"true\" name=\"myFlashMovie\" src=\"" + escapedURL + "\" quality=\"high\" type=\"application/x-shockwave-flash\">');" + LS +
          "window.document.write('  </embed>');" + LS +
          "window.document.write('</object>');" + LS +
          "//-->" + LS
      );
      try {
        out.close();
      } catch(Exception e) {}
      tempJSFile.deleteOnExit();
    } catch(Exception e) {
      e.printStackTrace();
      tempJSFile = null;
    }
    String content =
        "<html>" + LS +
        "  <head>" + LS +
        "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
        "      <!--" + LS +
        "      function sendCommand(command) {" + LS +
        "        command = command == null? '': encodeURIComponent(command);" + LS +
        "        window.location = 'command://' + command;" + LS +
        "      }" + LS +
        "      function getFlashMovieObject(movieName) {" + LS +
        "        if(window.document[movieName]) {" + LS +
        "            return window.document[movieName];" + LS +
        "        }" + LS +
        "        if(navigator.appName.indexOf(\"Microsoft Internet\") == -1) {" + LS +
        "          if(document.embeds && document.embeds[movieName]) {" + LS +
        "            return document.embeds[movieName];" + LS +
        "          }" + LS +
        "        } else {" + LS +
        "          return document.getElementById(movieName);" + LS +
        "        }" + LS +
        "      }" + LS +
        "      function playFM() {" + LS +
        "        var flashMovie=getFlashMovieObject(\"myFlashMovie\");" + LS +
        "        flashMovie.Play();" + LS +
        "      }" + LS +
        "      function stopFM() {" + LS +
        "        var flashMovie=getFlashMovieObject(\"myFlashMovie\");" + LS +
        "        flashMovie.Stop();" + LS +
        "      }" + LS +
        "      function rewindFM() {" + LS +
        "        var flashMovie=getFlashMovieObject(\"myFlashMovie\");" + LS +
        "        flashMovie.Rewind();" + LS +
        "      }" + LS +
        "      function setVariableFM(variableName, variableValue) {" + LS +
        "        var flashMovie=getFlashMovieObject(\"myFlashMovie\");" + LS +
        "        flashMovie.SetVariable(decodeURIComponent(variableName), decodeURIComponent(variableValue));" + LS +
        "      }" + LS +
        "      function getVariableFM(variableName) {" + LS +
        "        var flashMovie=getFlashMovieObject(\"myFlashMovie\");" + LS +
        "        try {" + LS +
        "          sendCommand('getVariableFM:' + flashMovie.GetVariable(decodeURIComponent(variableName)));" + LS +
        "        } catch(e) {" + LS +
        "          sendCommand('getVariableFM:');" + LS +
        "        }" + LS +
        "      }" + LS +
        "      //-->" + LS +
        "    </script>" + LS +
        "    <style type=\"text/css\">" + LS +
        "      html, object, embed, div, body { width: 100%; height: 100%; min-height: 100%; margin: 0; padding: 0; overflow: hidden; }" + LS +
        "      div { background-color: #FFFFFF; }" + LS +
        "    </style>" + LS +
        "  </head>" + LS;
    if(tempJSFile == null || !tempJSFile.exists()) {
      String urlXMLEscape = Utils.escapeXML(url);
      content +=
        "  <body height=\"*\">" + LS +
        "    <object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"\" id=\"myFlashMovie\">" + LS +
        "      <param name=\"movie\" value=\"" + urlXMLEscape + "\">" + LS +
        "      <embed" + LS +
        "          play=\"" + isAutoStart + "\" swliveconnect=\"true\" name=\"myFlashMovie\"" + LS + 
        "          src=\"" + urlXMLEscape + "\" quality=\"high\"" + LS +
        "          type=\"application/x-shockwave-flash\">" + LS +
        "      </embed>" + LS +
        "    </object>" + LS +
        "  </body>" + LS +
        "</html>" + LS;
    } else {
      content +=
        "  <body height=\"*\">" + LS +
        "    <script src=\"" + tempJSFile.toURI().toString() + "\"></script>" + LS +
        "  </body>" + LS +
        "</html>" + LS;
    }
    File tempHTMLFile;
    try {
      tempHTMLFile = File.createTempFile("nsfp", ".html");
      BufferedWriter out = new BufferedWriter(new FileWriter(tempHTMLFile));
      out.write(content);
      try {
        out.close();
      } catch(Exception e) {}
      tempHTMLFile.deleteOnExit();
    } catch(Exception e) {
      e.printStackTrace();
      tempHTMLFile = null;
    }
    if(tempHTMLFile != null && tempHTMLFile.exists()) {
      webBrowser.setURL(tempHTMLFile.toURI().toString());
    } else {
      webBrowser.setText(content);
    }
  }

  protected boolean isAutoStart;

  public void setAutoStart(boolean isAutoStart) {
    this.isAutoStart = isAutoStart;
  }
  
  public boolean isAutoStart() {
    return isAutoStart;
  }
  
  public void play() {
    if(url == null) {
      return;
    }
    webBrowser.execute("playFM();");
  }
  
  public void pause() {
    if(url == null) {
      return;
    }
    webBrowser.execute("stopFM();");
  }
  
  public void stop() {
    if(url == null) {
      return;
    }
    webBrowser.execute("rewindFM();");
  }
  
  public void setVariable(String name, String value) {
    if(url == null) {
      return;
    }
    webBrowser.execute("setVariableFM('" + encodeURL(name) + "', '" + encodeURL(value) + "')");
  }
  
  protected String getVariableResult;
  
  /**
   * @return The value, or null or an empty string when th variable is not defined.
   */
  public String getVariable(String name) {
    if(url == null) {
      return null;
    }
    getVariableResult = null;
    webBrowser.execute("getVariableFM('" + encodeURL(name) + "');");
    return getVariableResult;
  }
  
  public boolean isControlBarVisible() {
    return controlBarPane.isVisible();
  }
  
  public void setControlBarVisible(boolean isVisible) {
    controlBarPane.setVisible(isVisible);
    adjustBorder();
  }
  
  public void addFlashPlayerListener(FlashPlayerListener listener) {
    listenerList.add(FlashPlayerListener.class, listener);
  }
  
  public void removeFlashPlayerListener(FlashPlayerListener listener) {
    listenerList.remove(FlashPlayerListener.class, listener);
  }
  
  public FlashPlayerListener[] getFlashPlayerListeners() {
    return listenerList.getListeners(FlashPlayerListener.class);
  }
  
  @SuppressWarnings("deprecation")
  protected static String encodeURL(String s) {
    String encodedString;
    try {
      encodedString = URLEncoder.encode(s, "UTF-8");
    } catch(Exception e) {
      encodedString = URLEncoder.encode(s);
    }
    return encodedString.replaceAll("\\+", "%20");
  }
  
  public static String escapeXML(String s) {
    if(s == null || s.length() == 0) {
      return s;
    }
    StringBuffer sb = new StringBuffer((int)(s.length() * 1.1));
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
        case '\'':
          sb.append("&apos;");
          break;
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
