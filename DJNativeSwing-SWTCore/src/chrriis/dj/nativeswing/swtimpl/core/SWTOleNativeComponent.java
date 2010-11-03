/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleFunctionDescription;
import org.eclipse.swt.ole.win32.OleParameterDescription;
import org.eclipse.swt.ole.win32.Variant;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.internal.IOleNativeComponent;


/**
 * A convenience class for Windows Ole-based native components.
 * @author Christopher Deckers
 */
public abstract class SWTOleNativeComponent extends SWTNativeComponent implements IOleNativeComponent {

  /**
   * Construct an OLE native component.
   */
  public SWTOleNativeComponent() {
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
    public Object run(Object[] args) {
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
            Variant resultVariant = automation.invoke(ids[0], params);
            result = getVariantValue(resultVariant);
            dispose(resultVariant);
          } else {
            result = null;
            automation.invokeNoReply(ids[0], params);
          }
          for(Variant param: params) {
            dispose(param);
          }
          automation.dispose();
          return result;
        }
        Variant variantProperty = automation.getProperty(ids[0]);
        OleAutomation newAutomation = variantProperty.getAutomation();
        variantProperty.dispose();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return null;
    }
  }

  public void invokeOleFunction(String functionName, Object... args) {
    invokeOleFunction(new String[] {functionName}, args);
  }

  public void invokeOleFunction(String[] functionPath, Object... args) {
    runAsync(new CMN_invokeOleFunction(), false, functionPath, args);
  }

  public Object invokeOleFunctionWithResult(String functionName, Object... args) {
    return invokeOleFunctionWithResult(new String[] {functionName}, args);
  }

  public Object invokeOleFunctionWithResult(String[] functionPath, Object... args) {
    return runSync(new CMN_invokeOleFunction(), true, functionPath, args);
  }

  private static class CMN_setOleProperty extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
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
          for(Variant param: params) {
            dispose(param);
          }
          automation.dispose();
          return result;
        }
        Variant variantProperty = automation.getProperty(ids[0]);
        OleAutomation newAutomation = variantProperty.getAutomation();
        variantProperty.dispose();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return false;
    }
  }

  public void setOleProperty(String property, Object... args) {
    setOleProperty(new String[] {property}, args);
  }

  public void setOleProperty(String[] propertyPath, Object... args) {
    runAsync(new CMN_setOleProperty(), propertyPath, args);
  }

  private static class CMN_getOleProperty extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
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
          Variant propertyVariant = automation.getProperty(ids[0], params);
          for(Variant param: params) {
            dispose(param);
          }
          Object result = getVariantValue(propertyVariant);
          dispose(propertyVariant);
          automation.dispose();
          return result;
        }
        Variant variantProperty = automation.getProperty(ids[0]);
        OleAutomation newAutomation = variantProperty.getAutomation();
        variantProperty.dispose();
        automation.dispose();
        automation = newAutomation;
      }
      automation.dispose();
      return null;
    }
  }

  public Object getOleProperty(String property, Object... args) {
    return getOleProperty(new String[] {property}, args);
  }

  public Object getOleProperty(String[] propertyPath, Object... args) {
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

  private static class CMN_dumpOleInterfaceDefinitions extends ControlCommandMessage {
    private void dumpOleInterfaceDefinitions(StringBuilder sb, OleAutomation automation, int index) {
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
        for(int j=0; j<index; j++) {
          sb.append("  ");
        }
        sb.append(functionDescription.name).append("(");
        for(int i=0; i<functionDescription.args.length; i++) {
          OleParameterDescription param = functionDescription.args[i];
          if(i > 0) {
            sb.append(", ");
          }
          sb.append(getTypeDescription(param.type)).append(' ').append(param.name == null? "arg" + i: param.name);
        }
        sb.append("): ").append(getTypeDescription(functionDescription.returnType)).append(Utils.LINE_SEPARATOR);
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
        for(int j=0; j<index; j++) {
          sb.append("  ");
        }
        sb.append(propertyName).append(Utils.LINE_SEPARATOR);
        Variant variantProperty = automation.getProperty(automation.getIDsOfNames(new String[] {propertyName})[0]);
        if(variantProperty != null && variantProperty.getType() == OLE.VT_DISPATCH) {
          OleAutomation newAutomation = variantProperty.getAutomation();
          dumpOleInterfaceDefinitions(sb, newAutomation, index + 1);
          newAutomation.dispose();
        }
        dispose(variantProperty);
      }
    }
    private Map<Short, String> oleTypeToDescriptionMap;
    private String getTypeDescription(short type) {
      String description = oleTypeToDescriptionMap.get(type);
      if(description == null) {
        description = '[' + String.valueOf(type) + ']';
      }
      return description;
    }
    @Override
    public Object run(Object[] args) {
      oleTypeToDescriptionMap = new HashMap<Short, String>();
      for(Field field: OLE.class.getDeclaredFields()) {
        String fieldName = field.getName();
        Short value = null;
        if(fieldName.startsWith("VT_")) {
          try {
            value = (Short)field.get(null);
          } catch(Exception e) {}
        }
        if(value != null) {
          String fieldDescription = fieldName.substring("VT_".length()).toLowerCase(Locale.ENGLISH);
          oleTypeToDescriptionMap.put(value, fieldDescription);
        }
      }
      OleAutomation automation = new OleAutomation(getSite((OleFrame)getControl()));
      StringBuilder sb = new StringBuilder();
      dumpOleInterfaceDefinitions(sb, automation, 0);
      automation.dispose();
      System.out.print(sb.toString());
      return null;
    }
  }

  private static void dispose(Variant variant) {
    if(variant == null) {
      return;
    }
    variant.dispose();
  }

  /**
   * Dump the definitions of the OLE interface (properties and functions) to the output stream.
   */
  public void dumpOleInterfaceDefinitions() {
    runSync(new CMN_dumpOleInterfaceDefinitions());
  }

}
