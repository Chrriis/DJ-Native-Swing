/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import java.util.ArrayList;
import java.util.List;

import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.ComponentLifeCycle;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.ConstrainVisibility;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.DeferredDestruction;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.HierarchyProxying;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.InputEventsExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.PseudoTransparency;
import chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures.ThumbnailCreation;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.FunctionCalls;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.VariablesAndFlow;
import chrriis.dj.nativeswing.swtimpl.demo.examples.htmleditor.CKEditorExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.htmleditor.EditorDirtyExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.htmleditor.FCKEditorExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.htmleditor.TinyMCEExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.introduction.Codewise;
import chrriis.dj.nativeswing.swtimpl.demo.examples.introduction.NativeIntegration;
import chrriis.dj.nativeswing.swtimpl.demo.examples.introduction.TheSolution;
import chrriis.dj.nativeswing.swtimpl.demo.examples.introduction.WebStartAndApplets;
import chrriis.dj.nativeswing.swtimpl.demo.examples.nativedialogs.NativeDialogs;
import chrriis.dj.nativeswing.swtimpl.demo.examples.syntaxhighlighter.SimpleSyntaxHighlighterExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.utilities.FileAssociations;
import chrriis.dj.nativeswing.swtimpl.demo.examples.vlcplayer.OptionsAndPlaylistExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.vlcplayer.SimpleVLCPlayerExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.ClasspathPages;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.ConfirmedDisposal;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.Cookies;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.CustomDecorators;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.FullPageCaptureExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.JavascriptExecution;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.NavigationControl;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.NavigationParameters;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.SendingCommands;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.SettingContent;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.SimpleWebBrowserExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.WebBrowserFunctionsExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.WindowsAsTabs;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.XPCOMDownloadManager;
import chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser.XPCOMToggleEditionMode;
import chrriis.dj.nativeswing.swtimpl.demo.examples.win32.multimediaplayer.SimpleWMediaPlayerExample;
import chrriis.dj.nativeswing.swtimpl.demo.examples.win32.shellexplorer.SimpleWShellExplorerExample;

/**
 * @author Christopher Deckers
 */
public class DemoExampleDefinitionLoader {

  public static List<ExampleGroup> getExampleGroupList() {
    boolean isXULRunnerPresent = System.getProperty("nativeswing.webbrowser.xulrunner.home") != null || System.getenv("XULRUNNER_HOME") != null;
    String xulRunnerErrorMessage = "Mozilla XULRunner is required to run this example.\n\nPlease download it and either set the \"XULRUNNER_HOME\" environment variable\nor the \"nativeswing.webbrowser.xulrunner.home\" system property.\n(Make sure that \"javaxpcom.jar\" is part of your XULRunner install)";
    List<ExampleGroup> exampleGroupList = new ArrayList<ExampleGroup>();
    exampleGroupList.add(new ExampleGroup("Introduction", new Example[] {
        new Example("Native Integration", NativeIntegration.class, "First, some background information on the problems of native integration in a Swing-based application.", false),
        new Example("The Solution", TheSolution.class, "The DJ Project - NativeSwing.", false),
        new Example("Codewise", Codewise.class, "How hard is it to code using this library?", false),
        new Example("WebStart & Applets", WebStartAndApplets.class, "WebStart and Applets support.", false),
    }));
    exampleGroupList.add(new ExampleGroup("JWebBrowser", new Example[] {
        new Example("Simple Example", SimpleWebBrowserExample.class, "This is a simple example that shows the basic configuration of an embedded web browser component.", true),
        new Example("Setting Content", SettingContent.class, "Set any HTML content to the web browser.", true),
        new Example("Javascript Execution", JavascriptExecution.class, "Execute some Javascript code in the current web browser page.", true),
        new Example("Windows as Tabs", WindowsAsTabs.class, "Redirect all window opening actions to a tabbed pane as new tabs.", true),
        new Example("Navigation Control", NavigationControl.class, "Control the navigation happening in the web browser from the Java application.\nThis allows to block certain links and/or the creation of new windows, or to open links and/or new windows elsewhere.", true),
        new Example("Sending Commands", SendingCommands.class, "Use static links or the Javascript sendNSCommand(name, arg1, arg2, ...) function to send commands to the Java application.", true),
        new Example("Cookies", Cookies.class, "Access and modify the cookies set by the various browser instances.", true),
        new Example("Navigation Parameters", NavigationParameters.class, "Define HTTP headers and/or POST data to send along with the HTTP navigation request.", true),
        new Example("Classpath Pages", ClasspathPages.class, "Load web pages from the classpath with the help of the embedded simple web server.", true),
        new Example("Confirmed Disposal", ConfirmedDisposal.class, "Dispose a web browser giving the current page the opportunity to ask for confirmation.", true),
        new Example("Functions", WebBrowserFunctionsExample.class, "Register a Java function that can be invoked from Javascript.", true),
        new Example("Mozilla XPCOM Page Edition", XPCOMToggleEditionMode.class, "Toggle edition mode of a web page by accessing the Mozilla interfaces using XPCOM.", true, isXULRunnerPresent, xulRunnerErrorMessage),
        new Example("Mozilla XPCOM Download Manager", XPCOMDownloadManager.class, "Modify the browser's download manager using Mozilla XPCOM.", true, isXULRunnerPresent, xulRunnerErrorMessage),
        new Example("Custom Decorators", CustomDecorators.class, "Modify the browser's decorator, to add our own menus and buttons.", true),
        new Example("Full-page Capture", FullPageCaptureExample.class, "Capture the image of the page, including the portions that are outside of the viewport of the main document.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JFlashPlayer", new Example[] {
        new Example("Simple Example", SimpleFlashExample.class, "Display a Flash application.", true),
        new Example("Variables and Flow", VariablesAndFlow.class, "Control the flow of a Flash animation, and get/set global variables.", true),
        new Example("Function Calls", FunctionCalls.class, "Invoke functions, with or without waiting for a result, and listen to Flash commands.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JVLCPlayer", new Example[] {
        new Example("Simple Example", SimpleVLCPlayerExample.class, "Load a movie/sound file to an embedded VLC player.", true),
        new Example("Options and Playlist", OptionsAndPlaylistExample.class, "Load a file to VLC player, passing certain options (start time to 30, no audio and no title) using the playlist.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JHTMLEditor", new Example[] {
        new Example("FCKEditor Example", FCKEditorExample.class, "Graphically edit some HTML, get and set the HTML content, using the FCKEditor implementation.", true),
        new Example("CKEditor Example", CKEditorExample.class, "Graphically edit some HTML, get and set the HTML content, using the CKEditor implementation.", true),
        new Example("TinyMCE Example", TinyMCEExample.class, "Graphically edit some HTML, get and set the HTML content, using the TinyMCE implementation.", true),
        new Example("Dirty Indicator", EditorDirtyExample.class, "Track whether the editor was modified since the last time its content was set.", true),
    }));
    exampleGroupList.add(new ExampleGroup("JSyntaxHighlighter", new Example[] {
        new Example("Simple Example", SimpleSyntaxHighlighterExample.class, "Display some content with syntax highlighting from one of the available languages (C++, C#, css, Delphi, Java, JS, PHP, Python, Ruby, SQL, VB, XML, HTML).", true),
    }));
    exampleGroupList.add(new ExampleGroup("JWMediaPlayer (win32)", new Example[] {
        new Example("Simple Example", SimpleWMediaPlayerExample.class, "Load a movie/sound file to an embedded multimedia player.", true, System.getProperty("os.name").startsWith("Windows"), "This example is only available on a Windows operating system."),
    }));
    exampleGroupList.add(new ExampleGroup("JWShellExplorer (win32)", new Example[] {
        new Example("Simple Example", SimpleWShellExplorerExample.class, "Load a file to an embedded shell explorer.", true, System.getProperty("os.name").startsWith("Windows"), "This example is only available on a Windows operating system."),
    }));
    exampleGroupList.add(new ExampleGroup("Native Dialogs", new Example[] {
        new Example("Simple Example", NativeDialogs.class, "Use a file dialog or a directory dialog, and customize them to your needs.", true),
    }));
    exampleGroupList.add(new ExampleGroup("Additional Features", new Example[] {
        new Example("Constrain Visibility", ConstrainVisibility.class, "Constrain the visibility to superimpose Swing and native components.\nNote that Mac only supports shapes resolving to a single rectangle.", true),
        new Example("Deferred Destruction", DeferredDestruction.class, "Defer destruction until finalization to add/remove the same component.\nIt is not destroyed when removed but on disposeNativePeer() or garbage collection.", true),
        new Example("Hierarchy Proxying", HierarchyProxying.class, "Use a proxied component hierarchy for the native components to allow re-parenting and change of component Z-order.\nNote that Mac only supports shapes resolving to a single rectangle.", true),
        new Example("Thumbnail Creation", ThumbnailCreation.class, "Create a thumbnail by painting a native component to an image.", true),
        new Example("Pseudo Transparency", PseudoTransparency.class, "Simulate alpha blending of a Swing component over a native component. This works rather well over static content.\nNote that Mac only supports shapes resolving to a single rectangle.", true),
        new Example("Component Life Cycle", ComponentLifeCycle.class, "Present the life cycle of a component and when method calls happen.\nAlso highlight how runInSequence(Runnable) can be useful, and for special needs how to use initializeNativePeer().", true),
        new Example("Mouse & key events", InputEventsExample.class, "Attach a listener for key and mouse events, and go as far as replacing the web browser popup menu with a Swing one.", true),
    }));
    exampleGroupList.add(new ExampleGroup("Utilities", new Example[] {
        new Example("File Associations", FileAssociations.class, "Get the file type associations, and use them to launch files.", true),
    }));
    return exampleGroupList;
  }

}
