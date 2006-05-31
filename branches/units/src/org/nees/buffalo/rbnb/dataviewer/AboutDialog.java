package org.nees.buffalo.rbnb.dataviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Jason P. Hanley
 */
public class AboutDialog extends JDialog {
		
	public AboutDialog(JFrame owner) {
		super(owner);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.addKeyEventDispatcher(new KeyEventDispatcher() {
 			public boolean dispatchKeyEvent(KeyEvent keyEvent) {
 				int keyCode = keyEvent.getKeyCode();
 
 				if (keyCode == KeyEvent.VK_ESCAPE) {
 					dispose();				
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});		

		setTitle("About RBNB Data Viewer");
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new java.awt.Insets(20,20,20,20);
		
		JPanel container = new JPanel();
		getContentPane().add(container, c);
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		container.add(new JLabel("RBNB Data Viewer"));
		container.add(new JLabel("by Jason P. Hanley <jphanley@buffalo.edu>"));
		container.add(new JLabel(" "));
		container.add(new JLabel("Version: " + Version.major + "." + Version.minor + "." + Version.release));
		container.add(new JLabel(" "));
		container.add(new JLabel("Copyright \251 2004 University at Buffalo"));
		container.add(new JLabel("Visit http://nees.buffalo.edu/"));
		container.add(new JLabel(" "));
		container.add(new JLabel("This work is supported in part by the"));
		container.add(new JLabel("George E. Brown, Jr. Network for Earthquake"));
		container.add(new JLabel("Engineering Simulation (NEES) Program of the"));
		container.add(new JLabel("National Science Foundation under Award"));
		container.add(new JLabel("Numbers CMS-0086611 and CMS-0086612."));
		
		pack();
				
		setVisible(true);
	}
}