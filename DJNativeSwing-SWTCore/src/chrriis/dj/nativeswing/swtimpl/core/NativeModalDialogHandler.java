/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import chrriis.common.Utils;

/**
 * A utility class responsible for showing a native modal dialog and processing its result.
 * @author Christopher Deckers
 */
public abstract class NativeModalDialogHandler {

  private static class NativeModalComponent extends SWTNativeComponent {

    @SuppressWarnings("unused")
    protected static Control createControl(Composite parent, Object[] parameters) {
      return new Composite(parent, SWT.NONE);
    }

    @Override
    protected Component createEmbeddableComponent(Map<Object, Object> optionMap) {
      return super.createEmbeddableComponent(optionMap);
    }

  }

  private static class CMN_openDialog extends ControlCommandMessage {
    private transient volatile Object result;
    @Override
    public Object run(final Object[] args) throws Exception {
      Control control = getControl();
      if(control.isDisposed()) {
        return null;
      }
      Display display = control.getDisplay();
      if(display.getThread() != Thread.currentThread()) {
        try {
          display.syncExec(new Runnable() {
            public void run() {
              try {
                result = CMN_openDialog.this.run(args);
              } catch(Throwable t) {
                throw new RuntimeException(t);
              }
            }
          });
        } catch(RuntimeException e) {
          throw (Exception)e.getCause();
        }
        return result;
      }
      ControlCommandMessage commandMessage = (ControlCommandMessage)args[0];
      commandMessage.setControl(control);
      return commandMessage.run((Object[])args[1]);
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
    if(Utils.IS_MAC) {
      NativeModalComponent nativeModalComponent = new NativeModalComponent();
      dialog.getContentPane().add(nativeModalComponent.createEmbeddableComponent(new HashMap<Object, Object>()), BorderLayout.CENTER);
      nativeModalComponent.initializeNativePeer();
      processResult(message.syncExec(nativeModalComponent, args));
      dialog.dispose();
      return;
    }
    // We will show a dialog that is 0x0 with no decorations: it is modal but cannot be seen.
    // This will ensure dialogs are modaliy blocked without having to hack into the AWT event pumping.
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
    // We send an artificial event to clear the default lightweight target for mouse events.
    // Without this code, if a mouse down closes the native dialog, then the corresponding mouse up is retargeted to the previous lightweight target.
    MouseEvent mouseEvent;
    if(Utils.IS_JAVA_6_OR_GREATER) {
      mouseEvent = new MouseEvent(window, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, false, 0);
    } else {
      mouseEvent = new MouseEvent(window, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, false, 0);
    }
    window.dispatchEvent(mouseEvent);
    dialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        final NativeModalComponent nativeModalComponent = new NativeModalComponent();
        dialog.getContentPane().add(nativeModalComponent.createEmbeddableComponent(new HashMap<Object, Object>()), BorderLayout.CENTER);
        nativeModalComponent.initializeNativePeer();
        // We start a new thread which invokes an intermediate internal message (not the user message).
        new Thread("Modal dialog handler") {
          @Override
          public void run() {
            try {
              // This internal message runs outside the UI thread, so the AWT thread is not blocked on a sync call.
              // The purpose of this internal message is to invoke the user message in the SWT UI thread on the native side only.
              final Object result = new CMN_openDialog().syncExec(nativeModalComponent, message, args);
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  processResult(result);
                }
              });
            } finally {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  dialog.dispose();
                }
              });
            }
          }
        }.start();
      }
    });
    dialog.setVisible(true);
  }

  /**
   * Process the result of the message.
   */
  protected abstract void processResult(Object result);

}
