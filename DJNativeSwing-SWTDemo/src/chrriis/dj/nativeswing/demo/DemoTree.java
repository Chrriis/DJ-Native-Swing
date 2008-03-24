/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import chrriis.dj.nativeswing.demo.examples.advancedcapabilities.AlphaBlendingSimulation;
import chrriis.dj.nativeswing.demo.examples.advancedcapabilities.DestructionTimeOption;
import chrriis.dj.nativeswing.demo.examples.advancedcapabilities.FiliationTypeOption;
import chrriis.dj.nativeswing.demo.examples.advancedcapabilities.VisibilityConstraintOption;
import chrriis.dj.nativeswing.demo.examples.evolutions.WorkInProgress;
import chrriis.dj.nativeswing.demo.examples.flashplayer.Interactions;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.demo.examples.htmleditor.SimpleHTMLEditorExample;
import chrriis.dj.nativeswing.demo.examples.introduction.Codewise;
import chrriis.dj.nativeswing.demo.examples.introduction.NativeIntegration;
import chrriis.dj.nativeswing.demo.examples.introduction.TheSolution;
import chrriis.dj.nativeswing.demo.examples.utilities.FileAssociations;
import chrriis.dj.nativeswing.demo.examples.vlcplayer.SimpleVLCPlayerExample;
import chrriis.dj.nativeswing.demo.examples.webbrowser.JavascriptExecution;
import chrriis.dj.nativeswing.demo.examples.webbrowser.NavigationControl;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SendingCommands;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SettingContent;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SimpleWebBrowserExample;
import chrriis.dj.nativeswing.demo.examples.win32.multimediaplayer.SimpleMultiMediaPlayerExample;

/**
 * @author Christopher Deckers
 */
public class DemoTree extends JTree {

  protected static final Icon EXAMPLE_GROUP_ICON = new ImageIcon(DemoTree.class.getResource("resource/fldr_obj.gif"));
  protected static final Icon EXAMPLE_ICON = new ImageIcon(DemoTree.class.getResource("resource/brkp_obj.gif"));
  
  public DemoTree() {
    List<ExampleGroup> exampleGroupList = new ArrayList<ExampleGroup>();
    exampleGroupList.add(new ExampleGroup("Introduction", new Example[] {
        new Example("Native Integration", NativeIntegration.class, "First, some background information on the problems of native integration in a Swing-based application.", false),
        new Example("The Solution", TheSolution.class, "The DJ Project - NativeSwing.", false),
        new Example("Codewise", Codewise.class, "How hard is it to code using this library?", false),
    }));
    exampleGroupList.add(new ExampleGroup("JWebBrowser", new Example[] {
        new Example("Simple Example", SimpleWebBrowserExample.class, "This is a simple example that shows the basic configuration of an embedded web browser component.", true),
        new Example("Setting Content", SettingContent.class, "Any HTML content can be set to the web browser.", true),
        new Example("Javascript Execution", JavascriptExecution.class, "Javscript can be executed in the web browser component.", true),
        new Example("Navigation Control", NavigationControl.class, "The Java application can control the navigation happening in the web browser.\nThis feature allows to block certain links and/or the creation of new windows, or to open links and/or new windows elsewhere.", true),
        new Example("Sending Commands", SendingCommands.class, "Use static links or simple Javascript to send commands with arguments to the application:\n    function sendCommand(command) {\n      var s = 'command://' + encodeURIComponent(command);\n      for(var i=1; i<arguments.length; s+='&'+encodeURIComponent(arguments[i++]));\n      window.location = s;\n    }", true),
    }));
    exampleGroupList.add(new ExampleGroup("JFlashPlayer", new Example[] {
        new Example("Simple Example", SimpleFlashExample.class, "Display a flash application.", true),
        new Example("Interactions", Interactions.class, "Control a flash animation, and get/set variables.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JVLCPlayer", new Example[] {
        new Example("Simple Example", SimpleVLCPlayerExample.class, "Load a movie/sound file to an embedded VLC player.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JHTMLEditor", new Example[] {
        new Example("Simple Example", SimpleHTMLEditorExample.class, "Edit HTML, get and set the content of the editor.", true),
    }));
    exampleGroupList.add(new ExampleGroup("Advanced Capabilities", new Example[] {
        new Example("Visibility Constraint", VisibilityConstraintOption.class, "Constrain the visibility to superimpose Swing and native components.", true),
        new Example("Destruction Time", DestructionTimeOption.class, "Defer destruction until finalization to add/remove the same component. It is not destroyed when removed but on explicit dispose() or during garbage collection.", true),
        new Example("Filiation Type", FiliationTypeOption.class, "Use a proxied filiation of native components to allow re-parenting and change of component Z-order. By default, the visibility gets automatically constrained.", true),
        new Example("Alpha Blending Simulation", AlphaBlendingSimulation.class, "Simulate alpha blending of a Swing component over a native component. This works rather well over static content.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JMultiMediaPlayer (win32)", new Example[] {
        new Example("Simple Example", SimpleMultiMediaPlayerExample.class, "Load a movie/sound file to an embedded multimedia player.", true, System.getProperty("os.name").startsWith("Windows"), "This example is only available on a Windows operating system."),
    }));
    exampleGroupList.add(new ExampleGroup("Utilities", new Example[] {
        new Example("File Associations", FileAssociations.class, "Get the file type associations, and use them to launch files.", true),
    }));
    exampleGroupList.add(new ExampleGroup("Evolutions", new Example[] {
        new Example("Work in Progress", WorkInProgress.class, "The foundations of this native integration are solid, which is a good base for future work.", false),
    }));
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demo");
    for(ExampleGroup exampleGroup: exampleGroupList) {
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
