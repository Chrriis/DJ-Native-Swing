/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;

class ModalDialogUtils {

  static class NativeModalComponent extends NativeComponent {
    protected Component createEmbeddableComponent() {
      return super.createEmbeddableComponent(new HashMap<Object, Object>());
    }
  }

  static void showModalDialog(Component component, final NativeModalComponent nativeModalComponent, final Runnable runnable) {
    Window window = SwingUtilities.getWindowAncestor(component);
    final JDialog dialog;
    if(Utils.IS_JAVA_6_OR_GREATER) {
      dialog = new JDialog(window, ModalityType.APPLICATION_MODAL);
    } else {
      if(window instanceof Dialog) {
        dialog = new JDialog((Dialog)window, true);
      } else {
        dialog = new JDialog((Frame)window, true);
      }
    }
    dialog.setUndecorated(true);
    dialog.setSize(0, 0);
    if(Utils.IS_WINDOWS) {
      Point location = component.getLocationOnScreen();
      location.x += component.getWidth() / 2 - 280;
      location.y += component.getHeight() / 2 - 200;
      dialog.setLocation(location);
    } else {
      dialog.setLocationRelativeTo(window);
    }
    dialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        dialog.getContentPane().add(nativeModalComponent.createEmbeddableComponent(), BorderLayout.CENTER);
        nativeModalComponent.initializeNativePeer();
        try {
          runnable.run();
        } finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              dialog.dispose();
            }
          });
        }
      }
    });
    dialog.setVisible(true);
  }

}
