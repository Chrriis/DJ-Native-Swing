/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.win32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleFunctionDescription;
import org.eclipse.swt.ole.win32.Variant;

import chrriis.dj.nativeswing.ui.NativeComponent;

/**
 * @author Christopher Deckers
 */
public abstract class OleNativeComponent extends NativeComponent {

  public OleNativeComponent() {
  }
  
  protected static void configureOleFrame(OleClientSite site, OleFrame frame) {
    frame.setData("NS_site", site);
  }
  
  protected static OleClientSite getSite(OleFrame frame) {
    OleClientSite oleClientSite = (OleClientSite)frame.getData("NS_site");
    if(oleClientSite == null) {
      throw new IllegalStateException("The OleNativeComponent is not properly initialized! You need to call configureOleFrame() after the site creation.");
    }
    return oleClientSite;
  }
  
  private static class CMN_invokeOleFunction extends ControlCommandMessage {
    @Override
    public Object run() {
      String[] propertyPath = (String[])args[1];
      OleAutomation automation = new OleAutomation(getSite((OleFrame)getControl()));
      int[] ids;
      for(int i=0; i<propertyPath.length; i++) {
        ids = automation.getIDsOfNames(new String[] {propertyPath[i]});
        if(ids == null) {
          automation.dispose();
          return null;
        }
        if(i == propertyPath.length - 1) {
          Object[] vargs = (Object[])args[2];
          Variant[] params = new Variant[vargs.length];
          for(int j=0; j<vargs.length; j++) {
            params[j] = createVariant(vargs[j]);
          }
          Object result;
          if((Boolean)args[0]) {
            result = automation.invoke(ids[0], params);
          } else {
            result = null;
            automation.invokeNoReply(ids[0], params);
          }
          automation.dispose();
          return result;
        }
        OleAutomation newAutomation = automation.getProperty(ids[0]).getAutomation();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return null;
    }
  }
  
  protected void invokeOleFunction(String functionName, Object... args) {
    invokeOleFunction(new String[] {functionName}, args);
  }
  
  protected void invokeOleFunction(String[] functionPath, Object... args) {
    runAsync(new CMN_invokeOleFunction(), false, functionPath, args);
  }
  
  protected Object invokeOleFunctionWithResult(String functionName, Object... args) {
    return invokeOleFunctionWithResult(new String[] {functionName}, args);
  }
  
  protected Object invokeOleFunctionWithResult(String[] functionPath, Object... args) {
    return runSync(new CMN_invokeOleFunction(), true, functionPath, args);
  }
  
  private static class CMN_setOleProperty extends ControlCommandMessage {
    @Override
    public Object run() {
      String[] propertyPath = (String[])args[0];
      OleAutomation automation = new OleAutomation(getSite((OleFrame)getControl()));
      int[] ids;
      for(int i=0; i<propertyPath.length; i++) {
        ids = automation.getIDsOfNames(new String[] {propertyPath[i]});
        if(ids == null) {
          automation.dispose();
          return false;
        }
        if(i == propertyPath.length - 1) {
          Object[] vargs = (Object[])args[1];
          Variant[] params = new Variant[vargs.length];
          for(int j=0; j<vargs.length; j++) {
            params[j] = createVariant(vargs[j]);
          }
          boolean result = automation.setProperty(ids[0], params);
          automation.dispose();
          return result;
        }
        OleAutomation newAutomation = automation.getProperty(ids[0]).getAutomation();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return false;
    }
  }
  
  protected void setProperty(String property, Object... args) {
    setProperty(new String[] {property}, args);
  }
  
  protected void setProperty(String[] propertyPath, Object... args) {
    runAsync(new CMN_setOleProperty(), propertyPath, args);
  }
  
  private static class CMN_getOleProperty extends ControlCommandMessage {
    @Override
    public Object run() {
      String[] propertyPath = (String[])args[0];
      OleAutomation automation = new OleAutomation(getSite((OleFrame)getControl()));
      int[] ids;
      for(int i=0; i<propertyPath.length; i++) {
        ids = automation.getIDsOfNames(new String[] {propertyPath[i]});
        if(ids == null) {
          automation.dispose();
          return null;
        }
        if(i == propertyPath.length - 1) {
          Object[] vargs = (Object[])args[1];
          Variant[] params = new Variant[vargs.length];
          for(int j=0; j<vargs.length; j++) {
            params[j] = createVariant(vargs[j]);
          }
          Object result = getVariantValue(automation.getProperty(ids[0], params));
          automation.dispose();
          return result;
        }
        System.err.println(propertyPath[i] + ", " + automation + ", " + ids.length);
        OleAutomation newAutomation = automation.getProperty(ids[0]).getAutomation();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return null;
    }
  }
  
  protected Object getProperty(String property, Object... args) {
    return getProperty(new String[] {property}, args);
  }
  
  protected Object getProperty(String[] propertyPath, Object... args) {
    return runSync(new CMN_getOleProperty(), propertyPath, args);
  }
  
  protected static Variant createVariant(Object value) {
    if(value instanceof Boolean) {
      return new Variant((Boolean)value);
    }
    if(value instanceof Short) {
      return new Variant((Short)value);
    }
    if(value instanceof Integer) {
      return new Variant((Integer)value);
    }
    if(value instanceof Long) {
      return new Variant((Long)value);
    }
    if(value instanceof Float) {
      return new Variant((Float)value);
    }
    if(value instanceof Double) {
      return new Variant((Double)value);
    }
    if(value instanceof String || value == null) {
      return new Variant((String)value);
    }
    throw new IllegalArgumentException("The value could not be converted to a Variant: " + value);
  }
  
  protected static Object getVariantValue(Variant variant) {
    if(variant == null) {
      return null;
    }
    switch(variant.getType()) {
      case OLE.VT_BOOL: return variant.getBoolean();
      case OLE.VT_I2: return variant.getShort();
      case OLE.VT_I4: return variant.getInt();
      case OLE.VT_I8: return variant.getLong();
      case OLE.VT_R4: return variant.getFloat();
      case OLE.VT_R8: return variant.getDouble();
      case OLE.VT_BSTR: return variant.getString();
    }
    throw new IllegalArgumentException("The value could not be converted from a Variant: " + variant);
  }
  
  private static class CMN_dumpOleProperties extends ControlCommandMessage {
    private void dumpProperties(OleAutomation automation, int index) {
      List<OleFunctionDescription> functionList = new ArrayList<OleFunctionDescription>();
      for(int i=0; ; i++) {
        OleFunctionDescription functionDescription = automation.getFunctionDescription(i);
        if(functionDescription == null) {
          break;
        }
        functionList.add(functionDescription);
      }
      Collections.sort(functionList, new Comparator<OleFunctionDescription>() {
        public int compare(OleFunctionDescription o1, OleFunctionDescription o2) {
          return o1.name.toLowerCase(Locale.ENGLISH).compareTo(o2.name.toLowerCase(Locale.ENGLISH));
        }
      });
      for(OleFunctionDescription functionDescription: functionList) {
        StringBuilder sb = new StringBuilder();
        for(int j=0; j<index; j++) {
          sb.append("  ");
        }
        sb.append(functionDescription.name).append("()");
        System.err.println(sb.toString());
      }
      List<String> propertyList = new ArrayList<String>();
      for(int i=1; ; i++) {
        String name = automation.getName(i);
        if(name == null) {
          break;
        }
        propertyList.add(name);
      }
      Collections.sort(propertyList, new Comparator<String>() {
        public int compare(String o1, String o2) {
          return o1.toLowerCase(Locale.ENGLISH).compareTo(o2.toLowerCase(Locale.ENGLISH));
        }
      });
      for(String propertyName: propertyList) {
        StringBuilder sb = new StringBuilder();
        for(int j=0; j<index; j++) {
          sb.append("  ");
        }
        sb.append(propertyName);
        System.err.println(sb.toString());
        Variant property = automation.getProperty(automation.getIDsOfNames(new String[] {propertyName})[0]);
        if(property != null && property.getType() == OLE.VT_DISPATCH) {
          OleAutomation newAutomation = property.getAutomation();
          dumpProperties(newAutomation, index + 1);
          newAutomation.dispose();
        }
      }
    }
    @Override
    public Object run() {
      OleAutomation automation = new OleAutomation(getSite((OleFrame)getControl()));
      dumpProperties(automation, 0);
      automation.dispose();
      return null;
    }
  }
      
  protected void dumpProperties() {
    runSync(new CMN_dumpOleProperties());
  }
  
}
