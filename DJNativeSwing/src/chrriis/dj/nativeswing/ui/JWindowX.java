/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.NativeInterfaceHandler;

/**
 * @author Christopher Deckers
 */
public class JWindowX implements RootPaneContainer {

  public static int CLOSABLE_STYLE_MASK = SWT.CLOSE;
  public static int MAXIMIZABLE_STYLE_MASK = SWT.MAX;
  public static int MINIMIZABLE_STYLE_MASK = SWT.MIN;
  public static int BORDER_STYLE_MASK = SWT.BORDER;
  public static int TITLE_BAR_STYLE_MASK = SWT.TITLE;
  public static int TOOL_WINDOW_STYLE_MASK = SWT.TOOL;
  
  public static int WINDOW_STYLE = SWT.SHELL_TRIM;
  public static int DIALOG_STYLE = SWT.DIALOG_TRIM;
  
  protected Shell shell;
  protected JRootPane rootPane;
  protected Window parent;
  
  public JWindowX() {
    this(null);
  }
  
  public JWindowX(Window parent) {
    this.parent = parent;
    rootPane = new JRootPane();
    rootPane.setDoubleBuffered(true);
    if(Utils.IS_JAVA_6_OR_GREATER) {
      modalityType = ModalityType.MODELESS;
    }
  }
  
  protected Window modalWindow;
  
  protected void showShell() {
    if(parent instanceof Dialog) {
      modalWindow = new JDialog((Dialog)parent);
      ((JDialog)modalWindow).setUndecorated(true);
    } else {
      if(parent == null) {
        modalWindow = new JFrame();
        ((JFrame)modalWindow).setUndecorated(true);
      } else {
        modalWindow = new JDialog((Frame)parent);
        ((JDialog)modalWindow).setUndecorated(true);
      }
    }
    modalWindow.setLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
    if(modalWindow instanceof JDialog) {
      ((JDialog)modalWindow).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      if(isModal()) {
        if(Utils.IS_JAVA_6_OR_GREATER) {
          ((JDialog)modalWindow).setModalityType(getModalityType());
        } else {
          ((JDialog)modalWindow).setModal(true);
        }
      }
    } else {
      ((JFrame)modalWindow).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    modalWindow.setFocusableWindowState(focusableWindowState);
    modalWindow.addWindowFocusListener(new WindowFocusListener() {
      public void windowGainedFocus(WindowEvent e) {
        if(focusableWindowState) {
          if(shell != null && !shell.isDisposed()) {
            NativeInterfaceHandler.invokeSWT(new Runnable() {
              public void run() {
                if(shell != null && !shell.isDisposed()) {
                  shell.forceFocus();
                }
              }
            });
          }
        }
      }
      public void windowLostFocus(WindowEvent e) {
      }
    });
    final Canvas canvas = new Canvas();
    ((RootPaneContainer)modalWindow).getContentPane().add(canvas, BorderLayout.CENTER);
    // Realize the component and the canvas
    modalWindow.addNotify();
    new Thread() {
      @Override
      public void run() {
        setVisible_(true);
      }
    }.start();
    modalWindow.setVisible(true);
//    if(isModal()) {
//      modalWindow.dispose();
//      modalWindow = null;
//    }
  }
  
  protected void createShell() {
    if(shell != null) {
      return;
    }
    int style = windowStyleMask;
    if(isResizable) {
      style |= SWT.RESIZE;
    }
    if(isAlwaysOnTop) {
      style |= SWT.ON_TOP;
    }
    if(isUndecorated || region != null) {
      isUndecorated = true;
      style |= SWT.NO_TRIM;
    }
    if(modalWindow != null) {
      Shell parentShell = SWT_AWT.new_Shell(NativeInterfaceHandler.getDisplay(), (Canvas)((RootPaneContainer)modalWindow).getContentPane().getComponent(0));
      shell = new Shell(parentShell, style);
    } else {
      shell = new Shell(NativeInterfaceHandler.getDisplay(), style);
    }
    shell.setLayout(new FillLayout());
    Composite composite = new Composite(shell, SWT.NO_BACKGROUND | SWT.EMBEDDED);
    final Frame frame = SWT_AWT.new_Frame(composite);
//    frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    Panel panel = new Panel(new BorderLayout(0, 0)) {
      @Override
      public void update(Graphics g) {
        paint(g);
      }
    };
    panel.add(rootPane, BorderLayout.CENTER);
    frame.add(panel, BorderLayout.CENTER);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        frame.setSize(rootPane.getSize());
      }
    });
    rootPane.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        final Dimension size = rootPane.getSize();
        frame.setSize(size);
        NativeInterfaceHandler.invokeSWT(new Runnable() {
          public void run() {
            org.eclipse.swt.graphics.Rectangle trim = shell.computeTrim(0, 0, size.width, size.height);
            shell.setSize(trim.width, trim.height);
          }
        });
      }
    });
    shell.setRegion(region);
    setIconImages(iconList);
    setTitle(title);
    if(location != null) {
      shell.setLocation(location.x, location.y);
    }
  }
  
  public void setVisible(final boolean isVisible) {
    if(shell == null && !isVisible) {
      return;
    }
    if(isVisible) {
      showShell();
    } else {
      setVisible_(isVisible);
    }
  }
  
  protected void setVisible_(final boolean isVisible) {
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        if(isVisible && shell == null) {
          createShell();
        }
        if(isVisible) {
          Dimension size = rootPane.getSize();
          org.eclipse.swt.graphics.Rectangle trim = shell.computeTrim(0, 0, size.width, size.height);
          shell.setSize(trim.width, trim.height);
        }
        shell.setVisible(isVisible);
        if(isVisible) {
          org.eclipse.swt.graphics.Point point = shell.getLocation();
          setLocation(point.x, point.y);
          shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  modalWindow.dispose();
                  modalWindow = null;
                }
              });
            }
          });
          if(isModal()) {
            while(shell != null && !shell.isDisposed()) {
              NativeInterfaceHandler.dispatch();
            }
          }
        } else {
          shell.setVisible(false);
          shell.dispose();
          shell = null;
        }
      }
    });
  }
  
  public boolean isVisible() {
    final boolean[] result = new boolean[1];
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        result[0] = shell.isVisible();
      }
    });
    return result[0];
  }
  
  public Container getContentPane() {
    return rootPane.getContentPane();
  }
  
  public Component getGlassPane() {
    return rootPane.getGlassPane();
  }
  
  public JLayeredPane getLayeredPane() {
    return rootPane.getLayeredPane();
  }
  
  public JRootPane getRootPane() {
    return rootPane.getRootPane();
  }
  
  public void setContentPane(Container contentPane) {
    rootPane.setContentPane(contentPane);
  }
  
  public void setGlassPane(Component glassPane) {
    rootPane.setGlassPane(glassPane);
  }
  
  public void setLayeredPane(JLayeredPane layeredPane) {
    rootPane.setLayeredPane(layeredPane);
  }
  
  public void setLocation(Point location) {
    setLocation(location.x, location.y);
  }
  
  protected Point location;
  
  public void setLocation(final int x, final int y) {
    if(location == null) {
      location = new Point(x, y);
    } else {
      location.x = x;
      location.y = y;
    }
    if(shell != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          shell.setLocation(x, y);
        }
      });
    }
  }
  
  public void setSize(Dimension size) {
    setSize(size.width, size.height);
  }
  
  public void setSize(int width, int height) {
    rootPane.setSize(width, height);
  }
  
  public void setBounds(final int x, final int y, final int width, final int height) {
    setLocation(x, y);
    setSize(width, height);
  }
  
  /**
   * If the window is not showing, then this method may not take into account the decorations of the window.
   */
  public Dimension getSize() {
    if(shell == null) {
      return rootPane.getSize();
    }
    final Dimension[] result = new Dimension[1];
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        org.eclipse.swt.graphics.Point size = shell.getSize();
        result[0] = new Dimension(size.x, size.y);
      }
    });
    return result[0];
  }
  
  public Point getLocation() {
    return location == null? new Point(0, 0): location;
  }
  
  public Dimension getPreferredSize() {
    return rootPane.getPreferredSize();
  }
  
  public void setPreferredSize(Dimension preferredSize) {
    rootPane.setPreferredSize(preferredSize);
  }
  
  public Dimension getMinimumSize() {
    return rootPane.getMinimumSize();
  }
  
  public void setMinimumSize(Dimension minimumSize) {
    rootPane.setMinimumSize(minimumSize);
  }
  
  public Dimension getMaximumSize() {
    return rootPane.getMaximumSize();
  }
  
  public void setMaximumSize(Dimension maximumSize) {
    rootPane.setMaximumSize(maximumSize);
  }
  
  public void pack() {
    rootPane.setSize(getPreferredSize());
  }
  
  public void add(Component component) {
    rootPane.getContentPane().add(component);
  }
  
  public void add(Component component, int index) {
    rootPane.getContentPane().add(component, index);
  }
  
  public void add(Component component, Object constraints) {
    rootPane.getContentPane().add(component, constraints);
  }
  
  public void add(Component component, Object constraints, int index) {
    rootPane.getContentPane().add(component, constraints, index);
  }
  
  public void add(String name, Component component) {
    rootPane.getContentPane().add(name, component);
  }
  
  protected Region region;
  
  /**
   * The window needs to be undecorated for this to work.
   * If the window is not explicitely set to be undecorate, then this method needs to be invoked before the window becomes visible. Then it can be called again when the window is visible.
   */
  public void setShape(final Shape shape) {
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        if(shape == null) {
          region = null;
        } else {
          region = new Region(NativeInterfaceHandler.getDisplay());
          Rectangle bounds = shape.getBounds();
          region.add(0, 0, bounds.width, bounds.height);
          for(int x=0; x<bounds.width; x++) {
            for(int y=0; y<bounds.height; y++) {
              if(!shape.contains(x, y)) {
                region.subtract(x, y, 1, 1);
              }
            }
          }
        }
        if(shell != null) {
          shell.setRegion(region);
        }
      }
    });
  }
  
  protected List<? extends Image> iconList;
  
  public void setIconImages(final List<? extends Image> iconList) {
    this.iconList = iconList;
    if(shell != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          if(iconList == null) {
            shell.setImages(new org.eclipse.swt.graphics.Image[0]);
          } else {
            List<org.eclipse.swt.graphics.Image> imageList = new ArrayList<org.eclipse.swt.graphics.Image>(iconList.size());
            for(Image image: iconList) {
              imageList.add(new org.eclipse.swt.graphics.Image(NativeInterfaceHandler.getDisplay(), Utils.convertImage(image)));
            }
            shell.setImages(imageList.toArray(new org.eclipse.swt.graphics.Image[0]));
          }
        }
      });
    }
  }
  
  public void setIconImage(Image image) {
    if(image != null) {
      List<Image> iconList = new ArrayList<Image>();
      iconList.add(image);
      setIconImages(iconList);
    } else {
      setIconImages(null);
    }
  }
  
  public List<? extends Image> getIconImages() {
    return new ArrayList<Image>(iconList);
  }
  
  protected String title = "";
  
  public void setTitle(final String title) {
    this.title = title == null? "": title;
    if(shell != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          shell.setText(title);
        }
      });
    }
  }
  
  public String getTitle() {
    return title;
  }
  
  public void toFront() {
    if(shell != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          shell.forceActive();
        }
      });
    }
  }
  
  public void setJMenuBar(JMenuBar menuBar) {
    rootPane.setJMenuBar(menuBar);
  }
  
  public JMenuBar getJMenuBar() {
    return rootPane.getJMenuBar();
  }
  
  protected boolean isUndecorated;
  
  /**
   * This method can only be called when the component is not visible.
   */
  public void setUndecorated(boolean isUndecorated) {
    if(shell != null) {
      throw new IllegalStateException("This method can only be called when the component is not visible!");
    }
    this.isUndecorated = isUndecorated;
  }
  
  public boolean isUndecorated() {
    return isUndecorated;
  }
  
  protected int windowStyleMask = WINDOW_STYLE;
  
  /**
   * This method can only be called when the component is not visible.
   */
  public void setWindowStyle(int styleMask) {
    if(shell != null) {
      throw new IllegalStateException("This method can only be called when the component is not visible!");
    }
    this.windowStyleMask = styleMask;
  }
  
  public int getWindowStyleMask() {
    return windowStyleMask;
  }
  
  protected boolean isResizable = true;
  
  public void setResizable(boolean isResizable) {
    this.isResizable = isResizable;
  }
  
  public boolean isResizable() {
    return isResizable;
  }
  
  protected boolean isAlwaysOnTop;
  
  /**
   * This method can only be called when the component is not visible.
   */
  public void setAlwaysOnTop(boolean isAlwaysOnTop) {
    if(shell != null) {
      throw new IllegalStateException("This method can only be called when the component is not visible!");
    }
    this.isAlwaysOnTop = isAlwaysOnTop;
  }
  
  public boolean isAlwaysOnTop() {
    return isAlwaysOnTop;
  }
  
  protected boolean isModal;
  
  protected void setModal(boolean isModal) {
    if(Utils.IS_JAVA_6_OR_GREATER) {
      setModalityType(isModal? ModalityType.APPLICATION_MODAL: ModalityType.MODELESS);
    } else {
      this.isModal = isModal;
    }
  }
  
  protected boolean isModal() {
    if(Utils.IS_JAVA_6_OR_GREATER) {
      return modalityType != ModalityType.MODELESS;
    }
    return isModal;
  }

  protected Object modalityType;
  
  protected void setModalityType(ModalityType modalityType) {
    if(modalityType == null) {
      modalityType = ModalityType.MODELESS;
    }
    this.modalityType = modalityType;
  }

  protected ModalityType getModalityType() {
    return (ModalityType)modalityType;
  }
  
  /**
   * If the window is not showing, then this method may not take into account the decorations of the window.
   */
  public void setLocationRelativeTo(Component c) {
    Dimension size = getSize();
    if(c == null || !c.isShowing()) {
      Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
      setLocation(centerPoint.x - size.width / 2, centerPoint.y - size.height / 2);
    } else {
      Point cLocation = c.getLocationOnScreen();
      Dimension cSize = c.getSize();
      setLocation(cLocation.x + (cSize.width - size.width) / 2, cLocation.y + (cSize.height - size.height) / 2);
    }
  }
  
  protected boolean focusableWindowState = true;
  
  public boolean getFocusableWindowState() {
    return focusableWindowState;
  }
  
  /**
   * The focusable state affects whether the window should get focus from non explicit focus requests (like when it is first shown).
   */
  public void setFocusableWindowState(boolean focusableWindowState) {
    this.focusableWindowState = focusableWindowState;
    if(modalWindow != null) {
      modalWindow.setFocusableWindowState(focusableWindowState);
    }
  }
  
}
