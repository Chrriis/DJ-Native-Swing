/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

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
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import chrriis.common.Utils;

/**
 * A utility class responsible for showing a native modal dialog and processing its result.
 * @author Christopher Deckers
 */
public abstract class NativeModalDialogHandler {

  private static class NativeModalComponent extends NativeComponent {

    @SuppressWarnings("unused")
    protected static Control createControl(Composite parent, Object[] parameters) {
      return new Composite(parent, SWT.NONE);
    }

    @Override
    protected Component createEmbeddableComponent(Map<Object, Object> optionMap) {
      return super.createEmbeddableComponent(optionMap);
    }

  }

  /**
   * Show the native modal dialog.
   * @param component The component to use as a parent.
   * @param message The message invoked to actually show the native modal dialog.
   * The message could perform any action, but by contract with this API is supposed to show a native modal dialog.
   * @param args The arguments of the message.
   */
  public void showModalDialog(Component component, final ControlCommandMessage message, final Object... args) {
    Window window = component instanceof Window? (Window)component: SwingUtilities.getWindowAncestor(component);
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
        NativeModalComponent nativeModalComponent = new NativeModalComponent();
        dialog.getContentPane().add(nativeModalComponent.createEmbeddableComponent(new HashMap<Object, Object>()), BorderLayout.CENTER);
        nativeModalComponent.initializeNativePeer();
        try {
          processResult(message.syncExec(nativeModalComponent, args));
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

  /**
   * Process the result of the message.
   */
  protected abstract void processResult(Object result);

}
