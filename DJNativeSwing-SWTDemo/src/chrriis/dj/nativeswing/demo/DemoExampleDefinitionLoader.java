/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo;

import java.util.ArrayList;
import java.util.List;

import chrriis.dj.nativeswing.demo.examples.additionalfeatures.AlphaBlendingSimulation;
import chrriis.dj.nativeswing.demo.examples.additionalfeatures.ConstrainVisibility;
import chrriis.dj.nativeswing.demo.examples.additionalfeatures.DeferredDestruction;
import chrriis.dj.nativeswing.demo.examples.additionalfeatures.HierarchyProxying;
import chrriis.dj.nativeswing.demo.examples.additionalfeatures.ThumbnailCreation;
import chrriis.dj.nativeswing.demo.examples.flashplayer.FunctionCalls;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.demo.examples.flashplayer.VariablesAndFlow;
import chrriis.dj.nativeswing.demo.examples.htmleditor.CustomConfiguration;
import chrriis.dj.nativeswing.demo.examples.htmleditor.SimpleHTMLEditorExample;
import chrriis.dj.nativeswing.demo.examples.introduction.Codewise;
import chrriis.dj.nativeswing.demo.examples.introduction.NativeIntegration;
import chrriis.dj.nativeswing.demo.examples.introduction.TheSolution;
import chrriis.dj.nativeswing.demo.examples.utilities.FileAssociations;
import chrriis.dj.nativeswing.demo.examples.vlcplayer.SimpleVLCPlayerExample;
import chrriis.dj.nativeswing.demo.examples.webbrowser.ClasspathPages;
import chrriis.dj.nativeswing.demo.examples.webbrowser.JavascriptExecution;
import chrriis.dj.nativeswing.demo.examples.webbrowser.NavigationControl;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SendingCommands;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SettingContent;
import chrriis.dj.nativeswing.demo.examples.webbrowser.SimpleWebBrowserExample;
import chrriis.dj.nativeswing.demo.examples.win32.multimediaplayer.SimpleWMediaPlayerExample;

/**
 * @author Christopher Deckers
 */
public class DemoExampleDefinitionLoader {

  public static List<ExampleGroup> getExampleGroupList() {
    List<ExampleGroup> exampleGroupList = new ArrayList<ExampleGroup>();
    exampleGroupList.add(new ExampleGroup("Introduction", new Example[] {
        new Example("Native Integration", NativeIntegration.class, "First, some background information on the problems of native integration in a Swing-based application.", false),
        new Example("The Solution", TheSolution.class, "The DJ Project - NativeSwing.", false),
        new Example("Codewise", Codewise.class, "How hard is it to code using this library?", false),
    }));
    exampleGroupList.add(new ExampleGroup("JWebBrowser", new Example[] {
        new Example("Simple Example", SimpleWebBrowserExample.class, "This is a simple example that shows the basic configuration of an embedded web browser component.", true),
        new Example("Setting Content", SettingContent.class, "Set any HTML content to the web browser.", true),
        new Example("Javascript Execution", JavascriptExecution.class, "Execute some Javascript code in the current web browser page.", true),
        new Example("Navigation Control", NavigationControl.class, "Control the navigation happening in the web browser from the Java application.\nThis allows to block certain links and/or the creation of new windows, or to open links and/or new windows elsewhere.", true),
        new Example("Sending Commands", SendingCommands.class, "Use static links or simple Javascript to send some commands with arguments to the application:\n    function sendCommand(command) {\n      var s = 'command://' + encodeURIComponent(command);\n      for(var i=1; i<arguments.length; s+='&'+encodeURIComponent(arguments[i++]));\n      window.location = s;\n    }", true),
        new Example("Classpath pages", ClasspathPages.class, "Load web pages from the classpath with the help of the embedded simple web server.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JFlashPlayer", new Example[] {
        new Example("Simple Example", SimpleFlashExample.class, "Display a Flash application.", true),
        new Example("Variables and Flow", VariablesAndFlow.class, "Control the flow of a Flash animation, and get/set global variables.", true),
        new Example("Function Calls", FunctionCalls.class, "Invoke functions, with or without waiting for a result, and listen to Flash commands.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JVLCPlayer", new Example[] {
        new Example("Simple Example", SimpleVLCPlayerExample.class, "Load a movie/sound file to an embedded VLC player.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JHTMLEditor", new Example[] {
        new Example("Simple Example", SimpleHTMLEditorExample.class, "Graphically edit some HTML, get and set the HTML content.", true),
        new Example("Custom Configuration", CustomConfiguration.class, "Modify default behaviors with custom configuration script.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JMultiMediaPlayer (win32)", new Example[] {
        new Example("Simple Example", SimpleWMediaPlayerExample.class, "Load a movie/sound file to an embedded multimedia player.", true, System.getProperty("os.name").startsWith("Windows"), "This example is only available on a Windows operating system."),
    }));
    exampleGroupList.add(new ExampleGroup("Additional Features", new Example[] {
        new Example("Constrain Visibility", ConstrainVisibility.class, "Constrain the visibility to superimpose Swing and native components.", true),
        new Example("Deferred Destruction", DeferredDestruction.class, "Defer destruction until finalization to add/remove the same component.\nIt is not destroyed when removed but on disposeNativePeer() or garbage collection.", true),
        new Example("Hierarchy Proxying", HierarchyProxying.class, "Use a proxied component hierarchy for the native components to allow re-parenting and change of component Z-order.", true),
        new Example("Thumbnail Creation", ThumbnailCreation.class, "Create a thumbnail by painting a native component to an image.", true),
        new Example("Pseudo Transparency", AlphaBlendingSimulation.class, "Simulate alpha blending of a Swing component over a native component. This works rather well over static content.", true),
    }));
    exampleGroupList.add(new ExampleGroup("Utilities", new Example[] {
        new Example("File Associations", FileAssociations.class, "Get the file type associations, and use them to launch files.", true),
    }));
    return exampleGroupList;
  }
  
}
