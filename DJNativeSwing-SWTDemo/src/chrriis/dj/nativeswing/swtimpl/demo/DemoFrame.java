/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JSyntaxHighlighter;
import chrriis.dj.nativeswing.swtimpl.components.JSyntaxHighlighter.ContentLanguage;

/**
 * @author Christopher Deckers
 */
public class DemoFrame extends JFrame {
  
  protected static final Font DESCRIPTION_FONT = new Font("Dialog", Font.PLAIN, 14);
  
  public DemoFrame() {
    super("The DJ Project - NativeSwing (SWT)");
    Class<DemoFrame> clazz = DemoFrame.class;
    if(System.getProperty("java.version").compareTo("1.6") >= 0) {
      setIconImages(Arrays.asList(new Image[] {
          new ImageIcon(clazz.getResource("resource/DJIcon16x16.png")).getImage(),
          new ImageIcon(clazz.getResource("resource/DJIcon24x24.png")).getImage(),
          new ImageIcon(clazz.getResource("resource/DJIcon32x32.png")).getImage(),
          new ImageIcon(clazz.getResource("resource/DJIcon48x48.png")).getImage(),
          new ImageIcon(clazz.getResource("resource/DJIcon256x256.png")).getImage(),
      }));
    } else {
      setIconImage(new ImageIcon(clazz.getResource("resource/DJIcon32x32Plain.png")).getImage());
    }
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationByPlatform(true);
    Container contentPane = getContentPane();
    final JPanel displayArea = new JPanel(new BorderLayout()) {
      @Override
      public Dimension getMinimumSize() {
        return new Dimension(0, 0);
      }
    };
    JPanel leftPane = new JPanel(new BorderLayout());
    final DemoTree demoTree = new DemoTree();
    TreeSelectionModel selectionModel = demoTree.getSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    selectionModel.addTreeSelectionListener(new TreeSelectionListener() {
      protected Example selectedExample;
      protected JComponent component;
      public void valueChanged(TreeSelectionEvent e) {
        TreePath selectionPath = demoTree.getSelectionPath();
        if(selectionPath == null) {
          return;
        }
        Object userObject = ((DefaultMutableTreeNode)selectionPath.getLastPathComponent()).getUserObject();
        if(userObject instanceof Example) {
          final Example example = (Example)userObject;
          if(selectedExample != example) {
            new Thread("NativeSwingDemo Example Loader") {
              @Override
              public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    JComponent c;
                    Class<? extends JComponent> componentClass = example.getComponentClass();
                    if(!example.isAvailable()) {
                      GridBagConstraints cons = new GridBagConstraints();
                      cons.anchor = GridBagConstraints.WEST;
                      cons.gridy = 0;
                      JPanel panel = new JPanel(new GridBagLayout());
                      for(String notAvailableMessage: example.getNotAvailableMessage().split("\n")) {
                        if(notAvailableMessage.length() == 0) {
                          notAvailableMessage = " ";
                        }
                        panel.add(new JLabel(notAvailableMessage), cons);
                        cons.gridy++;
                      }
                      c = panel;
                    } else {
                      if(componentClass == null) {
                        c = new JPanel();
                      } else {
                        try {
                          c = componentClass.newInstance();
                        } catch(Throwable t) {
                          t.printStackTrace();
                          return;
                        }
                      }
                    }
                    component = c;
                    selectedExample = example;
                    displayArea.removeAll();
                    JPanel contentPane = new JPanel(new BorderLayout());
                    String description = example.getDescription();
                    if(description != null) {
                      JPanel descriptionPanel = new JPanel(new BorderLayout());
                      JEditorPane descriptionEditorPane = new JEditorPane(description.startsWith("<html>")? "text/html": "text/plain", description) {
                        public EditorKit getEditorKitForContentType(String type){
                          if("text/plain".equalsIgnoreCase(type)) {
                            StyledEditorKit styledEditorKit = new StyledEditorKit();
                            MutableAttributeSet inputAttributes = styledEditorKit.getInputAttributes();
                            StyleConstants.setFontFamily(inputAttributes, DESCRIPTION_FONT.getFamily());
                            StyleConstants.setFontSize(inputAttributes, DESCRIPTION_FONT.getSize());
                            return styledEditorKit;
                          }
                          return super.getEditorKitForContentType(type);
                        }
                      };
                      descriptionEditorPane.setFont(DESCRIPTION_FONT);
                      descriptionEditorPane.setEditable(false);
                      descriptionPanel.add(descriptionEditorPane, BorderLayout.CENTER);
                      descriptionPanel.add(new JSeparator(), BorderLayout.SOUTH);
                      contentPane.add(descriptionPanel, BorderLayout.NORTH);
                    }
                    contentPane.add(component, BorderLayout.CENTER);
                    if(componentClass != null && example.isShowingSources()) {
                      final JTabbedPane tabbedPane = new JTabbedPane();
                      tabbedPane.addTab("Demo", contentPane);
                      final JPanel sourcePanel = new JPanel(new BorderLayout());
                      tabbedPane.addTab("Source", sourcePanel);
                      tabbedPane.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                          if(tabbedPane.getSelectedComponent() == sourcePanel) {
                            tabbedPane.removeChangeListener(this);
                            Class<? extends JComponent> componentClass = selectedExample.getComponentClass();
                            try {
                              InputStreamReader reader;
                              try {
                                reader = new InputStreamReader(DemoFrame.class.getResourceAsStream("/src/" + componentClass.getName().replace('.', '/') + ".java"), "UTF-8");
                              } catch(Exception ex) {
                                reader = new InputStreamReader(new BufferedInputStream(new FileInputStream("src/" + componentClass.getName().replace('.', '/') + ".java")), "UTF-8");
                              }
//                              sourcePanel.add(new JScrollPane(new SourcePane(reader)), BorderLayout.CENTER);
                              StringBuilder sb = new StringBuilder();
                              char[] chars = new char[1024];
                              for(int i; (i=reader.read(chars)) != -1; sb.append(chars, 0, i));
                              JSyntaxHighlighter syntaxHighlighter = new JSyntaxHighlighter();
                              syntaxHighlighter.setContent(sb.toString(), ContentLanguage.Java);
                              sourcePanel.add(syntaxHighlighter, BorderLayout.CENTER);
                              sourcePanel.revalidate();
                              sourcePanel.repaint();
                              reader.close();
                            } catch(Exception ex) {
                              ex.printStackTrace();
                            }
                          }
                        }
                      });
                      displayArea.add(tabbedPane, BorderLayout.CENTER);
                    } else {
                      displayArea.add(contentPane, BorderLayout.CENTER);
                    }
                    displayArea.revalidate();
                    displayArea.repaint();
                  }
                });
              }
            }.start();
          }
        }
      }
    });
    leftPane.add(new JScrollPane(demoTree), BorderLayout.CENTER);
    JCheckBox nativeInterfaceOpenCheckBox = new JCheckBox("Native Interface Open", true);
    nativeInterfaceOpenCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          NativeInterface.open();
        } else {
          NativeInterface.close();
        }
      }
    });
    leftPane.add(nativeInterfaceOpenCheckBox, BorderLayout.SOUTH);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPane, displayArea);
    contentPane.add(splitPane, BorderLayout.CENTER);
    setSize(800, 600);
    splitPane.setDividerLocation(170);
    demoTree.expandRow(0);
    demoTree.setSelectionRow(1);
  }
  
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.getConfiguration().setNativeSideRespawnedOnError(false);
    NativeInterface.open();
    Toolkit.getDefaultToolkit().setDynamicLayout(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new DemoFrame().setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
