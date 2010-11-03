/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.core;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Christopher Deckers
 */
class NativeJSBrowserDetection {

  public final String browserName;
  public final String browserVersion;

  public NativeJSBrowserDetection(Browser browser) {
    Shell shell = new Shell(browser.getDisplay());
    shell.setLayout(new FillLayout());
    Browser browser_ = new Browser(shell, browser.getStyle());
    browser_.setText("<html></html>");
    String browserName = null;
    String versionSearch = null;
    String userAgent = (String)browser_.evaluate("return navigator.userAgent");
    String navigatorVendor = (String)browser_.evaluate("return navigator.vendor");
    if(browserName == null && userAgent != null && userAgent.indexOf("Chrome") != -1) {
      versionSearch = browserName = "Chrome";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("OmniWeb") != -1) {
      browserName = "OmniWeb";
      versionSearch = "OmniWeb/";
    }
    if(browserName == null && navigatorVendor != null && navigatorVendor.indexOf("Apple") != -1) {
      browserName = "Safari";
      // "Version" is probably more user friendly but is not always accessible.
      versionSearch = "AppleWebKit";
    }
    if(browserName == null && (String)browser_.evaluate("return window.opera") != null) {
      versionSearch = browserName = "Opera";
    }
    if(browserName == null && navigatorVendor != null && navigatorVendor.indexOf("iCab") != -1) {
      versionSearch = browserName = "iCab";
    }
    if(browserName == null && navigatorVendor != null && navigatorVendor.indexOf("KDE") != -1) {
      versionSearch = browserName = "Konqueror";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("Firefox") != -1) {
      versionSearch = browserName = "Firefox";
    }
    if(browserName == null && navigatorVendor != null && navigatorVendor.indexOf("Camino") != -1) {
      versionSearch = browserName = "Camino";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("Netscape") != -1) {
      versionSearch = browserName = "Netscape";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("MSIE") != -1) {
      browserName = "IE";
      versionSearch = "MSIE";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("Gecko") != -1) {
      browserName = "Mozilla";
      versionSearch = "rv";
    }
    if(browserName == null && userAgent != null && userAgent.indexOf("Mozilla") != -1) {
      browserName = "Netscape";
      versionSearch = "Mozilla";
    }
    String browserVersion = null;
    if(browserName != null) {
      if(userAgent != null) {
        int index = userAgent.indexOf(versionSearch);
        if(index >= 0) {
          browserVersion = userAgent.substring(index + versionSearch.length() + 1);
        }
      }
      if(browserVersion == null) {
        String appVersion = (String)browser_.evaluate("return navigator.appVersion");
        if(appVersion != null) {
          int index = appVersion.indexOf(versionSearch);
          if(index >= 0) {
            browserVersion = appVersion.substring(index + versionSearch.length() + 1);
          }
        }
      }
      if(browserVersion != null) {
        int index = -1;
        for(int i=0; i<browserVersion.length(); i++) {
          char c = browserVersion.charAt(i);
          if(!Character.isDigit(c) && c != '.' && c != '_' && c != '-' && (c < 'a' || c > 'z') && c < 'A' && c < 'Z') {
            index = i;
            break;
          }
        }
        if(index > 0) {
          browserVersion = browserVersion.substring(0, index);
        }
      }
    }
    this.browserName = browserName;
    this.browserVersion = browserVersion;
  }

}
