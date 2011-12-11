/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32;

/**
 * @author Christopher Deckers
 */
public class ShellExplorerDocumentCompleteEvent {

  private JWShellExplorer shellExplorer;
  private String location;
  
  public ShellExplorerDocumentCompleteEvent(JWShellExplorer shellExplorer, String location) {
    this.shellExplorer = shellExplorer;
    this.location = location;
  }
  
  public JWShellExplorer getShellExplorer() {
    return shellExplorer;
  }
  
  public String getLocation() {
    return location;
  }
  
}
