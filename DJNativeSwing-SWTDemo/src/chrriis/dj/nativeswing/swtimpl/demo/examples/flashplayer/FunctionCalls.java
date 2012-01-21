/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.FlashPlayerCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.FlashPlayerListener;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;

/**
 * @author Christopher Deckers
 */
public class FunctionCalls {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel flashPlayerPanel = new JPanel(new BorderLayout());
    flashPlayerPanel.setBorder(BorderFactory.createTitledBorder("Native Flash Player component"));
    final JFlashPlayer flashPlayer = new JFlashPlayer();
    // Flash Demo from Paulus Tuerah (www.goldenstudios.or.id)
    flashPlayer.load(FunctionCalls.class, "resource/FlashPlayerInteractions.swf");
    flashPlayerPanel.add(flashPlayer, BorderLayout.CENTER);
    contentPane.add(flashPlayerPanel, BorderLayout.CENTER);
    GridBagLayout gridBag = new GridBagLayout();
    JPanel interactionsPanel = new JPanel(gridBag);
    interactionsPanel.setBorder(BorderFactory.createTitledBorder("Java Interactions"));
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = 0;
    cons.gridy = 0;
    cons.insets = new Insets(1, 1, 1, 1);
    cons.anchor = GridBagConstraints.WEST;
    JLabel functionLabel = new JLabel("Function Call: ");
    gridBag.setConstraints(functionLabel, cons);
    interactionsPanel.add(functionLabel);
    cons.gridy++;
    JLabel commandLabel = new JLabel("Received Command: ");
    gridBag.setConstraints(commandLabel, cons);
    interactionsPanel.add(commandLabel);
    cons.gridx++;
    cons.gridy = 0;
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.weightx = 1;
    FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 2, 0);
    JPanel getterSetterFunctionPanel = new JPanel(flowLayout);
    JButton getterButton = new JButton("Get");
    getterSetterFunctionPanel.add(getterButton);
    final JTextField functionTextField = new JTextField(14);
    getterSetterFunctionPanel.add(functionTextField);
    JButton setterButton = new JButton("Set");
    getterSetterFunctionPanel.add(setterButton);
    gridBag.setConstraints(getterSetterFunctionPanel, cons);
    interactionsPanel.add(getterSetterFunctionPanel);
    cons.gridy++;
    final JLabel commandValueLabel = new JLabel("-");
    gridBag.setConstraints(commandValueLabel, cons);
    interactionsPanel.add(commandValueLabel);
    contentPane.add(interactionsPanel, BorderLayout.SOUTH);
    // Attach the listeners
    getterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        functionTextField.setText((String)flashPlayer.invokeFlashFunctionWithResult("getMessageX"));
      }
    });
    setterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        flashPlayer.invokeFlashFunction("setMessageX", functionTextField.getText());
      }
    });
    flashPlayer.addFlashPlayerListener(new FlashPlayerListener() {
      public void commandReceived(FlashPlayerCommandEvent e) {
        if("sendCommandTest".equals(e.getCommand())) {
          StringBuilder sb = new StringBuilder();
          Object[] parameters = e.getParameters();
          for(int i=0; i<parameters.length; i++) {
            if(i > 0) {
              sb.append(", ");
            }
            sb.append(parameters[i]);
          }
          commandValueLabel.setText(sb.toString());
        }
      }
    });
    return contentPane;
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterface.open();
    UIUtils.setPreferredLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createContent(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
