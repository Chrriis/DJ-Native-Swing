/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Christopher Deckers
 */
public class DemoTree extends JTree {

  protected static final Icon EXAMPLE_GROUP_ICON = new ImageIcon(DemoTree.class.getResource("resource/fldr_obj.gif"));
  protected static final Icon EXAMPLE_ICON = new ImageIcon(DemoTree.class.getResource("resource/brkp_obj.gif"));

  public DemoTree() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demo");
    for(ExampleGroup exampleGroup: DemoExampleDefinitionLoader.getExampleGroupList()) {
      DefaultMutableTreeNode parent = new DefaultMutableTreeNode(exampleGroup);
      for(Example example: exampleGroup.getExamples()) {
        parent.add(new DefaultMutableTreeNode(example));
      }
      root.add(parent);
    }
    setModel(new DefaultTreeModel(root));
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if(c instanceof JLabel) {
          Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
          if(userObject instanceof ExampleGroup) {
            ((JLabel)c).setIcon(EXAMPLE_GROUP_ICON);
          } else if(userObject instanceof Example) {
            ((JLabel)c).setIcon(EXAMPLE_ICON);
          }
        }
        return c;
      }
    });
  }

}
