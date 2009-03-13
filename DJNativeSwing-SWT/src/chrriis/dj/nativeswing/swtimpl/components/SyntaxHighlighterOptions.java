/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

/**
 * @author Christopher Deckers
 */
public class SyntaxHighlighterOptions {

  private boolean isGutterVisible = true;

  public void setGutterVisible(boolean isGutterVisible) {
    this.isGutterVisible = isGutterVisible;
  }

  public boolean isGutterVisible() {
    return isGutterVisible;
  }

  private boolean isControlAreaVisible = true;

  public void setControlAreaVisible(boolean isControlAreaVisible) {
    this.isControlAreaVisible = isControlAreaVisible;
  }

  public boolean isControlAreaVisible() {
    return isControlAreaVisible;
  }

  private boolean isBlockCollapsed;

  private int firstLineNumber = 1;

  public void setFirstLineNumber(int firstLineNumber) {
    this.firstLineNumber = firstLineNumber;
  }

  public int getFirstLineNumber() {
    return firstLineNumber;
  }

  private boolean isShowingColumns;

//  public void setShowingColumns(boolean isShowingColumns) {
//    this.isShowingColumns = isShowingColumns;
//  }
//
//  public boolean isShowingColumns() {
//    return isShowingColumns;
//  }

  String getOptionsString() {
    StringBuilder sb = new StringBuilder();
    if(!isGutterVisible) {
      sb.append(":nogutter");
    }
    if(!isControlAreaVisible) {
      sb.append(":nocontrols");
    }
    if(isBlockCollapsed) {
      sb.append(":collapse");
    }
    if(firstLineNumber != 1) {
      sb.append(":firstline[" + firstLineNumber + "]");
    }
    if(isShowingColumns) {
      sb.append(":showcolumns");
    }
    return sb.toString();
  }

}
