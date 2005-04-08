package org.nees.buffalo.rdv.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * @author Jason P. Hanley
 */
public class RBNBConnectionDialog extends JDialog implements KeyEventDispatcher {

 	static Log log = LogFactory.getLog(RBNBConnectionDialog.class.getName());
	
	RBNBController rbnb;
	
	JLabel headerLabel;
	
	JLabel rbnbHostNameLabel;
	JTextField rbnbHostNameTextField;
	
	JLabel rbnbPortLabel;
	JTextField rbnbPortTextField;
	
	JButton connectButton;
	JButton cancelButton;
	
	public RBNBConnectionDialog(JFrame owner, RBNBController rbnbController) {
		super(owner);
		
		this.rbnb = rbnbController;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				bindKeys();
			}
			public void windowDeactivated(WindowEvent e) {
				unbindKeys();
			}
		});
		
		setTitle("Select RBNB Server");
			
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(10,10,10,10);
		
		headerLabel = new JLabel("Please specify your RBNB connection.");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
		getContentPane().add(headerLabel, c);
		
		c.gridwidth = 1;
		
		rbnbHostNameLabel = new JLabel("Host");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		getContentPane().add(rbnbHostNameLabel, c);
		
		rbnbHostNameTextField = new JTextField(rbnb.getRBNBHostName(), 25);
		rbnbHostNameTextField.requestFocusInWindow();
 		rbnbHostNameTextField.setSelectionStart(0);
 		rbnbHostNameTextField.setSelectionEnd(rbnbHostNameTextField.getText().length());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		getContentPane().add(rbnbHostNameTextField, c);
		
		rbnbPortLabel = new JLabel("Port");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
		getContentPane().add(rbnbPortLabel, c);

		rbnbPortTextField = new JTextField(Integer.toString(rbnb.getRBNBPortNumber()));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHWEST;
		getContentPane().add(rbnbPortTextField, c);
		
		JPanel buttonPanel = new JPanel();
				
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		buttonPanel.add(connectButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		buttonPanel.add(cancelButton);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(buttonPanel, c);
		
		pack();
		
		setVisible(true);
	}
	
	private void connect() {
		try {
			rbnb.setRBNBPortNumber(Integer.parseInt(rbnbPortTextField.getText()));
		} catch (NumberFormatException e) {
			rbnbPortLabel.setForeground(Color.RED);
			rbnbPortTextField.setText(Integer.toString(rbnb.getRBNBPortNumber()));
			rbnbPortTextField.requestFocusInWindow();
			return;
		}
		
		rbnb.setRBNBHostName(rbnbHostNameTextField.getText());
		
		dispose();
		
		if (rbnb.isConnected()) {
			rbnb.reconnect();
		} else {
			rbnb.connect();
		}
	}
	
	private void cancel() {
		dispose();		
	}
	
	private void bindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.addKeyEventDispatcher(this);		
	}
	
	private void unbindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.removeKeyEventDispatcher(this);
	}

 	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
 		int keyCode = keyEvent.getKeyCode();
 		
 		if (keyCode == KeyEvent.VK_ENTER) {
 			connect();
 			return true;
 		} else if (keyCode == KeyEvent.VK_ESCAPE) {
 			cancel();
 			return true;
 		} else {
 			return false;
 		}
 	}
}
