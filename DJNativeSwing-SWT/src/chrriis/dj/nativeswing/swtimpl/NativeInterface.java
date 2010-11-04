/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.io.PrintStream;
import java.io.PrintWriter;

import chrriis.dj.nativeswing.swtimpl.internal.ISWTNativeInterface;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * The native interface, which establishes the link between the peer VM (native side) and the local side.
 * @author Christopher Deckers
 */
public abstract class NativeInterface {

  private static ISWTNativeInterface swtNativeInterface = NativeCoreObjectFactory.create(ISWTNativeInterface.class, "chrriis.dj.nativeswing.swtimpl.core.SWTNativeInterface", new Class<?>[0], new Object[0]);

  protected static NativeInterface getInstance() {
    return (NativeInterface)swtNativeInterface;
  }

  /**
   * Indicate whether the native interface is open.
   * @return true if the native interface is open, false otherwise.
   */
  public static boolean isOpen() {
    return swtNativeInterface.isOpen_();
  }

  /**
   * Close the native interface, which destroys the native side (peer VM). Note that the native interface can be re-opened later.
   */
  public static void close() {
    swtNativeInterface.close_();
  }

  /**
   * Get the configuration, which allows to modify some parameters.
   */
  public static NativeInterfaceConfiguration getConfiguration() {
    return swtNativeInterface.getConfiguration_();
  }

  /**
   * Indicate whether the native interface is initialized.
   * @return true if the native interface is initialized, false otherwise.
   */
  public static boolean isInitialized() {
    return swtNativeInterface.isInitialized_();
  }

  public static boolean isInProcess() {
    return swtNativeInterface.isInProcess_();
  }

  static boolean isOutProcessNativeSide() {
    return swtNativeInterface.isOutProcessNativeSide_();
  }

  /**
   * Initialize the native interface, but do not open it. This method sets some properties and registers a few listeners to keep track of certain states necessary for the good functioning of the framework.<br/>
   * This method is automatically called if open() is used. It should be called early in the program, the best place being as the first call in the main method.
   */
  public static void initialize() {
    swtNativeInterface.initialize_();
  }

  /**
   * Print the stack traces to system err, including the ones from the peer VM when applicable.
   */
  public static void printStackTraces() {
    swtNativeInterface.printStackTraces_();
  }

  /**
   * Print the stack traces to a print stream, including the ones from the peer VM when applicable.
   */
  public static void printStackTraces(PrintStream printStream) {
    swtNativeInterface.printStackTraces_(printStream);
  }

  /**
   * Print the stack traces to a print writer, including the ones from the peer VM when applicable.
   */
  public static void printStackTraces(PrintWriter printWriter) {
    swtNativeInterface.printStackTraces_(printWriter);
  }

  /**
   * Open the native interface, which creates the peer VM that handles the native side of the native integration.<br/>
   * Initialization takes place if the interface was not already initialized. If initialization was not explicitely performed, this method should be called early in the program, the best place being as the first call in the main method.
   */
  public static void open() {
    swtNativeInterface.open_();
  }

  static Object syncSend(boolean isTargetNativeSide, Message message) {
    return swtNativeInterface.syncSend_(isTargetNativeSide, message);
  }

  static void asyncSend(boolean isTargetNativeSide, final Message message) {
    swtNativeInterface.asyncSend_(isTargetNativeSide, message);
  }

  /**
   * Indicate if the current thread is the user interface thread.
   * @return true if the current thread is the user interface thread.
   * @throws IllegalStateException when the native interface is not alive.
   */
  public static boolean isUIThread(boolean isNativeSide) {
    return swtNativeInterface.isUIThread_(isNativeSide);
  }

  /**
   * Run the native event pump. Certain platforms require this method call at the end of the main method to function properly, so it is suggested to always add it.
   */
  public static void runEventPump() {
    swtNativeInterface.runEventPump_();
  }

  /**
   * Indicate if events are being pumped (by a call to runEventPump).
   * @return true if events are being pumped.
   */
  public static boolean isEventPumpRunning() {
    return swtNativeInterface.isEventPumpRunning_();
  }

  /**
   * Add a native interface listener.
   * @param listener the native listener to add.
   */
  public static void addNativeInterfaceListener(NativeInterfaceListener listener) {
    swtNativeInterface.addNativeInterfaceListener_(listener);
  }

  /**
   * Remove a native interface listener.
   * @param listener the native listener to remove.
   */
  public static void removeNativeInterfaceListener(NativeInterfaceListener listener) {
    swtNativeInterface.removeNativeInterfaceListener_(listener);
  }

  /**
   * Get all the native interface listeners.
   * @return the native interface listeners.
   */
  public static NativeInterfaceListener[] getNativeInterfaceListeners() {
    return swtNativeInterface.getNativeInterfaceListeners_();
  }

  protected static NativeInterfaceConfiguration createConfiguration() {
    return new NativeInterfaceConfiguration();
  }

  protected static Object runMessageCommand(LocalMessage commandMessage) {
    return commandMessage.runCommand();
  }

  protected static Object runMessageCommand(CommandMessage commandMessage) throws Exception {
    return commandMessage.runCommand();
  }

  protected static boolean isMessageSyncExec(Message message) {
    return message.isSyncExec();
  }

  protected static void setMessageSyncExec(Message message, boolean isSyncExec) {
    message.setSyncExec(isSyncExec);
  }

  protected static String[] getPeerVMParams(NativeInterfaceConfiguration nativeInterfaceConfiguration) {
    return nativeInterfaceConfiguration.getPeerVMParams();
  }

  protected static Class<?>[] getNativeClassPathReferenceClasses(NativeInterfaceConfiguration nativeInterfaceConfiguration) {
    return nativeInterfaceConfiguration.getNativeClassPathReferenceClasses();
  }

  protected static String[] getNativeClassPathReferenceResources(NativeInterfaceConfiguration nativeInterfaceConfiguration) {
    return nativeInterfaceConfiguration.getNativeClassPathReferenceResources();
  }

  protected static int getMessageID(Message message) {
    return message.getID();
  }

  protected static boolean isMessageValid(Message message) {
    return message.isValid();
  }

  protected static void setMessageArgs(CommandMessage message, Object... args) {
    message.setArgs(args);
  }

  protected static void computeMessageID(Message message, boolean isTargetNativeSide) {
    message.computeID(isTargetNativeSide);
  }

  protected static void setMessageUI(Message message, boolean isUI) {
    message.setUI(isUI);
  }

  protected static boolean isMessageUI(Message message) {
    return message.isUI();
  }

  /**
   * The main method that is called by the native side (peer VM).
   * @param args the arguments that are passed to the peer VM.
   */
  public static void main(String[] args) throws Exception {
    ((ISWTNativeInterface)getInstance()).main_(args);
  }

}
