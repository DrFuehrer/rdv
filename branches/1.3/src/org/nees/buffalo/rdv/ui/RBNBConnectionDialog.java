/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/ui/RBNBConnectionDialog.java $
 * $Revision: 316 $
 * $Date: 2005-10-19 20:11:00 -0700 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

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
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * @author Jason P. Hanley
 */
public class RBNBConnectionDialog extends JDialog implements KeyEventDispatcher {

 	static Log log = LogFactory.getLog(RBNBConnectionDialog.class.getName());
	
	RBNBController rbnb;
  DataPanelManager dataPanelManager;
	
	JLabel headerLabel;
	
	JLabel rbnbHostNameLabel;
	JTextField rbnbHostNameTextField;
	
	JLabel rbnbPortLabel;
	JTextField rbnbPortTextField;
	
	JButton connectButton;
	JButton cancelButton;
	
	public RBNBConnectionDialog(JFrame owner, RBNBController rbnbController, DataPanelManager dataPanelManager) {
		super(owner, true);
		
		this.rbnb = rbnbController;
    this.dataPanelManager = dataPanelManager;
		
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
	
	public void setVisible(boolean visible) {
		if (visible) {
			rbnbHostNameTextField.requestFocusInWindow();
	 		rbnbHostNameTextField.setSelectionStart(0);
	 		rbnbHostNameTextField.setSelectionEnd(rbnbHostNameTextField.getText().length());			
		}
		super.setVisible(visible);
	}
	
	private void connect() {
		String rbnbHostName;
		int rbnbPortNumber;
		
		try {
			rbnbPortNumber = Integer.parseInt(rbnbPortTextField.getText());
		} catch (NumberFormatException e) {
			rbnbPortLabel.setForeground(Color.RED);
			rbnbPortTextField.setText(Integer.toString(rbnb.getRBNBPortNumber()));
			rbnbPortTextField.requestFocusInWindow();
			return;
		}
		
		rbnbPortLabel.setForeground(Color.BLACK);
		
		rbnbHostName = rbnbHostNameTextField.getText();
		
		dispose();
		
		if (!(rbnbHostName.equals(rbnb.getRBNBHostName()) && rbnbPortNumber == rbnb.getRBNBPortNumber() && rbnb.isConnected())) {
      if (rbnb.isConnected()) {
        dataPanelManager.closeAllDataPanels(); 
      }
            
			rbnb.setRBNBHostName(rbnbHostName);
			rbnb.setRBNBPortNumber(rbnbPortNumber);
			
			if (rbnb.isConnected()) {
				rbnb.reconnect();
			} else {
				rbnb.connect();
			}
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
