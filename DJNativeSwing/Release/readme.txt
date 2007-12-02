The DJ project - NativeSwing
http://djproject.sourceforge.net
Christopher Deckers (chrriis@nextencia.net)
Licence terms: LGPL (see licence.txt)


1. What is the DJ Project - NativeSwing?

The DJ Project is a set of tools and libraries to enhance the user experience of
Java on the Desktop.

The NativeSwing library allows an easy integration of some native components
into Swing applications, and provides some native utilities to enhance Swing's
APIs.

The key components are of course a rich web browser and a flash player.


2. How to use it?

Simply place the NativeSwing.jar library in your classpath, as well as the SWT
library 3.4 corresponding to your platform (swt.jar, see
http://www.eclipse.org/swt)

Then, you need to add the following to your main method:

public static void main(String[] args) {
  NativeInterfaceHandler.init();
  // The rest of the initialization of the program
  NativeInterfaceHandler.runEventPump();
}

Note that the last call is blocking, and it assumes that the Swing application
exits explicitely with a System.exit(int) call, which is generally the rule.


3. Any demo?

The DJ NativeSwing Demo presents all the features of the NativeSwing library,
along with the code that was required. Simply launch DJNativeSwingDemo.jar.

By default, the Windows version of SWT is provided and in the demo's classpath.
If you wish to try on a different platform, simply place the corresponding SWT
library, (re-)named swt.jar alongside DJNativeSwingDemo.jar.


4. What is the development status?

The library is tested on Windows, and logically works on all the platforms where
SWT supports placing SWT components in a Swing application.
The library's main role is to solve the common integration issues:
- Lightweight and heavyweight components produce visual glitches, like Swing
  popup menus, tooltips and combo drop menu to appear behind the native
  components.
- Hidden heavyweight components added to the user interface steal the focus, or
  mess it up.
- Swing modality works for Swing components, but the embedded native component
  are not blocked.
- The threading of the user interface is different in Swing and the native
  components, as each have their own event pump. Deadlocks occur easily.

For information about the current implementation status, visit the DJ Project's
website.


5. Sources?

The sources are part of the distribution, both for the DJ NativeSwing library
and the demo.

There is of course some access to the CVS tree, from the Sourceforge website.


6. How to contribute?

If you are interested in helping the project, simply send me an e-mail. Friendly
e-mails are always welcome!
