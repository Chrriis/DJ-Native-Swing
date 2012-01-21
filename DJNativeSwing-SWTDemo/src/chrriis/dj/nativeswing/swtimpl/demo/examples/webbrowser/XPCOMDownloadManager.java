/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mozilla.interfaces.nsICancelable;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsIFactory;
import org.mozilla.interfaces.nsILocalFile;
import org.mozilla.interfaces.nsIMIMEInfo;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsITransfer;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.MozillaXPCOM;

/**
 * @author Christopher Deckers
 */
public class XPCOMDownloadManager {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser(JWebBrowser.useXULRunnerRuntime());
    webBrowser.navigate("http://www.eclipse.org/downloads");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    // Create an additional area to see the downloads in progress.
    final JPanel downloadsPanel = new JPanel(new GridLayout(0, 1));
    downloadsPanel.setBorder(BorderFactory.createTitledBorder("Download manager (on-going downloads are automatically added to this area)"));
    contentPane.add(downloadsPanel, BorderLayout.SOUTH);
    // We can only access XPCOM when it is properly initialized.
    // This happens when the web browser is created so we run our code in sequence.
    webBrowser.runInSequence(new Runnable() {
      public void run() {
        try {
          nsIComponentRegistrar registrar = MozillaXPCOM.Mozilla.getComponentRegistrar();
          String NS_DOWNLOAD_CID = "e3fa9D0a-1dd1-11b2-bdef-8c720b597445";
          String NS_TRANSFER_CONTRACTID = "@mozilla.org/transfer;1";
          registrar.registerFactory(NS_DOWNLOAD_CID, "Transfer", NS_TRANSFER_CONTRACTID, new nsIFactory() {
            public nsISupports queryInterface(String uuid) {
              if(uuid.equals(nsIFactory.NS_IFACTORY_IID) || uuid.equals(nsIFactory.NS_ISUPPORTS_IID)) {
                return this;
              }
              return null;
            }
            public nsISupports createInstance(nsISupports outer, String iid) {
              return createTransfer(downloadsPanel);
            }
            public void lockFactory(boolean lock) {}
          });
        } catch(Exception e) {
          JOptionPane.showMessageDialog(webBrowser, "Failed to register XPCOM download manager.\nPlease check your XULRunner configuration.", "XPCOM interface", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    });
    return contentPane;
  }

  private static nsITransfer createTransfer(final JPanel downloadsPanel) {
    return new nsITransfer() {
      public nsISupports queryInterface(String uuid) {
        if(uuid.equals(nsITransfer.NS_ITRANSFER_IID) ||
            uuid.equals(nsITransfer.NS_IWEBPROGRESSLISTENER2_IID) ||
            uuid.equals(nsITransfer.NS_IWEBPROGRESSLISTENER_IID) ||
            uuid.equals(nsITransfer.NS_ISUPPORTS_IID)) {
          return this;
        }
        return null;
      }
      private JComponent downloadComponent;
      private JLabel downloadStatusLabel;
      private String baseText;
      public void init(nsIURI source, nsIURI target, String displayName, nsIMIMEInfo MIMEInfo, double startTime, nsILocalFile tempFile, final nsICancelable cancelable) {
        downloadComponent = new JPanel(new BorderLayout(5, 5));
        downloadComponent.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        JButton cancelDownloadButton = new JButton("Cancel");
        downloadComponent.add(cancelDownloadButton, BorderLayout.WEST);
        final String path = target.getPath();
        cancelDownloadButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            cancelable.cancel(Mozilla.NS_ERROR_ABORT);
            removeDownloadComponent();
            new File(path + ".part").delete();
          }
        });
        baseText = "Downloading to " + path;
        downloadStatusLabel = new JLabel(baseText);
        downloadComponent.add(downloadStatusLabel, BorderLayout.CENTER);
        downloadsPanel.add(downloadComponent);
        downloadsPanel.revalidate();
        downloadsPanel.repaint();
      }
      public void onStateChange(nsIWebProgress webProgress, nsIRequest request, long stateFlags, long status) {
        if((stateFlags & nsIWebProgressListener.STATE_STOP) != 0) {
          removeDownloadComponent();
        }
      }
      private void removeDownloadComponent() {
        downloadsPanel.remove(downloadComponent);
        downloadsPanel.revalidate();
        downloadsPanel.repaint();
      }
      public void onProgressChange64(nsIWebProgress webProgress, nsIRequest request, long curSelfProgress, long maxSelfProgress, long curTotalProgress, long maxTotalProgress) {
        long currentKBytes = curTotalProgress / 1024;
        long totalKBytes = maxTotalProgress / 1024;
        downloadStatusLabel.setText(baseText + " (" + currentKBytes + "/" + totalKBytes + ")");
      }
      public void onStatusChange(nsIWebProgress webProgress, nsIRequest request, long status, String message) {}
      public void onSecurityChange(nsIWebProgress webProgress, nsIRequest request, long state) {}
      public void onProgressChange(nsIWebProgress webProgress, nsIRequest request, int curSelfProgress, int maxSelfProgress, int curTotalProgress, int maxTotalProgress) {}
      public void onLocationChange(nsIWebProgress webProgress, nsIRequest request, nsIURI location) {}
    };
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
