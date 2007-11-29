/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataPanelManager;
import org.rdv.rbnb.RBNBController;

/**
 * @author Jason P. Hanley
 */
public class RBNBConnectionDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = -6957067662109056521L;

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
		
		setTitle("Connect to RBNB Server");
    
    JPanel container = new JPanel();
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
			
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		
		headerLabel = new JLabel("Please specify the RBNB server connection information.");
    headerLabel.setBackground(Color.white);
    headerLabel.setOpaque(true);
    headerLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(10,10,10,10)));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new Insets(0,0,0,0);
    container.add(headerLabel, c);
		
		c.gridwidth = 1;
		
		rbnbHostNameLabel = new JLabel("Host:");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new Insets(10,10,10,5);
    container.add(rbnbHostNameLabel, c);
		
		rbnbHostNameTextField = new JTextField(25);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(10,0,10,10);
    container.add(rbnbHostNameTextField, c);
		
		rbnbPortLabel = new JLabel("Port:");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new Insets(0,10,10,5);
    container.add(rbnbPortLabel, c);

		rbnbPortTextField = new JTextField();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(0,0,10,10);
    rbnbPortTextField.addFocusListener(
      new FocusListener() {
        public void focusGained(FocusEvent focusEvent) {
          rbnbPortTextField.setSelectionStart(0);
          rbnbPortTextField.setSelectionEnd(rbnbPortTextField.getText().length());
        }
        public void focusLost(FocusEvent focusEvent){}     
      }
    );
    container.add(rbnbPortTextField, c);
		
		JPanel buttonPanel = new JPanel();
    
    Action connectAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        connect();
      }      
    };
    connectAction.putValue(Action.NAME, "Connect");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "connect");
    actionMap.put("connect", connectAction);
		connectButton = new JButton(connectAction);
		buttonPanel.add(connectButton);
		
    Action cancelAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }      
    };
    cancelAction.putValue(Action.NAME, "Cancel");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    actionMap.put("cancel", cancelAction);    
    cancelButton = new JButton(cancelAction);
    buttonPanel.add(cancelButton);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(0,10,10,5);
    container.add(buttonPanel, c);
		
		pack();
    setLocationByPlatform(true);
		setVisible(true);
	}
	
	public void setVisible(boolean visible) {
		if (visible) {
      rbnbHostNameTextField.setText(rbnb.getRBNBHostName());
      rbnbPortTextField.setText(Integer.toString(rbnb.getRBNBPortNumber()));
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
	
}
