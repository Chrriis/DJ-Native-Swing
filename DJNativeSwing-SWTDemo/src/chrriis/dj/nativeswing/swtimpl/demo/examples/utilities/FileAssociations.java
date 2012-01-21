/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import chrriis.common.UIUtils;
import chrriis.common.ui.TableSorter;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.utilities.FileTypeLauncher;

/**
 * @author Christopher Deckers
 */
public class FileAssociations {

  public static JComponent createContent() {
    // Set in code and and not in class to avoid AWT to initialize before Native Swing.
    final Icon EMPTY_ICON = new Icon() {
      public int getIconHeight() {
        return FileTypeLauncher.getIconSize().height;
      }
      public int getIconWidth() {
        return 0;
      }
      public void paintIcon(Component c, Graphics g, int x, int y) {
      }
    };
    final JPanel contentPane = new JPanel(new BorderLayout());
    JPanel loadingPanel = new JPanel(new GridBagLayout());
    loadingPanel.add(new JLabel("Please wait while the full list is being retrieved..."));
    contentPane.add(loadingPanel, BorderLayout.CENTER);
    new Thread("NativeSwingDemo File Association Loader") {
      @Override
      public void run() {
        final FileTypeLauncher[] fileTypeLaunchers = FileTypeLauncher.getLaunchers();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            contentPane.removeAll();
            JPanel mainPane = new JPanel(new BorderLayout());
            final JTable table = new JTable() {
              @Override
              public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if(c instanceof JLabel) {
                  Object value = getValueAt(row, column);
                  if(value instanceof FileTypeLauncher) {
                    FileTypeLauncher fileTypeLauncher = (FileTypeLauncher)value;
                    JLabel label = (JLabel)c;
                    label.setIcon(fileTypeLauncher.getIcon());
                    label.setText(fileTypeLauncher.getName());
                  } else {
                    ((JLabel)c).setIcon(null);
                  }
                }
                return c;
              }
            };
            table.setRowHeight(FileTypeLauncher.getIconSize().height + 2);
            ListSelectionModel selectionModel = table.getSelectionModel();
            selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            Object[][] data = new Object[fileTypeLaunchers.length][];
            for(int i=0; i<fileTypeLaunchers.length; i++) {
              FileTypeLauncher fileTypeLauncher = fileTypeLaunchers[i];
              String[] registeredExtensions = fileTypeLauncher.getRegisteredExtensions();
              StringBuilder sb = new StringBuilder();
              for(int j=0; j<registeredExtensions.length; j++) {
                if(j > 0) {
                  sb.append(", ");
                }
                sb.append(registeredExtensions[j]);
              }
              data[i] = new Object[] {fileTypeLauncher, sb.toString()};
            }
            DefaultTableModel tableModel = new DefaultTableModel(data, new Object[] {"File Type Launcher", "Registered Extensions"}) {
              @Override
              public boolean isCellEditable(int row, int column) {
                return false;
              }
            };
            table.setModel(new TableSorter(tableModel, table.getTableHeader()) {
              @Override
              protected Comparator getComparator(int column) {
                if(column == 0) {
                  return new Comparator<FileTypeLauncher>() {
                    public int compare(FileTypeLauncher o1, FileTypeLauncher o2) {
                      return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                    }
                  };
                }
                return super.getComparator(column);
              }
            });
            mainPane.add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel fileLaunchPanel = new JPanel(new BorderLayout());
            fileLaunchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
            fileLaunchPanel.add(new JLabel("File: "), BorderLayout.WEST);
            final JTextField fileLaunchTextField = new JTextField();
            fileLaunchPanel.add(fileLaunchTextField, BorderLayout.CENTER);
            JButton browseButton = new JButton("...");
            browseButton.addActionListener(new ActionListener() {
              JFileChooser fileChooser;
              public void actionPerformed(ActionEvent e) {
                if(fileChooser == null) {
                  fileChooser = new JFileChooser();
                }
                if(fileChooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                  fileLaunchTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
              }
            });
            fileLaunchPanel.add(browseButton, BorderLayout.EAST);
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            final JButton launchButton = new JButton("Launch");
            launchButton.setEnabled(false);
            launchButton.setIcon(EMPTY_ICON);
            launchButton.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                FileTypeLauncher launcher = (FileTypeLauncher)table.getValueAt(table.getSelectedRow(), table.convertColumnIndexToView(0));
                launcher.launch(fileLaunchTextField.getText());
              }
            });
            buttonPanel.add(launchButton);
            selectionModel.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                launchButton.setEnabled(table.getSelectedRow() != -1);
              }
            });
            final JButton launchAssociatedButton = new JButton("Launch Associated Handler");
            launchAssociatedButton.setEnabled(false);
            launchAssociatedButton.setIcon(EMPTY_ICON);
            launchAssociatedButton.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                String fileName = fileLaunchTextField.getText();
                FileTypeLauncher.getLauncher(fileName).launch(fileName);
              }
            });
            fileLaunchTextField.getDocument().addDocumentListener(new DocumentListener() {
              public void changedUpdate(DocumentEvent e) {
                adjustState();
              }
              public void insertUpdate(DocumentEvent e) {
                adjustState();
              }
              public void removeUpdate(DocumentEvent e) {
                adjustState();
              }
              protected void adjustState() {
                FileTypeLauncher launcher = FileTypeLauncher.getLauncher(fileLaunchTextField.getText());
                launchAssociatedButton.setEnabled(launcher != null);
                launchAssociatedButton.setText(launcher == null? "Launch Associated Handler": "Launch " + launcher.getName());
                launchAssociatedButton.setIcon(launcher == null? EMPTY_ICON: launcher.getIcon());
              }
            });
            buttonPanel.add(launchAssociatedButton);
            fileLaunchPanel.add(buttonPanel, BorderLayout.SOUTH);
            mainPane.add(fileLaunchPanel, BorderLayout.SOUTH);
            contentPane.add(mainPane, BorderLayout.CENTER);
            contentPane.revalidate();
            contentPane.repaint();
          }
        });
      }
    }.start();
    return contentPane;
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
