
package org.nees.buffalo.rdv.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

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
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.rbnb.RBNBController;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JumpDateTimeDialog extends JDialog {

 	static Log log = LogFactory.getLog(JumpDateTimeDialog.class.getName());
	
	RBNBController rbnb;
	DataPanelManager dataPanelManager;
	
	JLabel headerLabel;
	
	JLabel dateLable;
	JTextField dateTextField;
	
	JLabel timeLabel;
	JTextField timeTextField;
	
	JButton jumpButton;
	JButton cancelButton;
	
	/** Format to use to display the date property. */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");
	
	public JumpDateTimeDialog(JFrame owner, RBNBController rbnbController, DataPanelManager dataPanelManager) {
		super(owner, true);
		
		this.rbnb = rbnbController;
		this.dataPanelManager = dataPanelManager;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setTitle("Go to Date Time location");
    
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
		
		headerLabel = new JLabel("Please enter the date and time for location data.");
		
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
		
		dateLable = new JLabel("Date:");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new Insets(10,10,10,5);
		container.add(dateLable, c);
		
		dateTextField = new JTextField(DATE_FORMAT.format(new Date()), 25);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10,0,10,10);
		container.add(dateTextField, c);
		
		timeLabel = new JLabel("Time:");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new Insets(0,10,10,5);
		container.add(timeLabel, c);

		timeTextField = new JTextField(TIME_FORMAT.format(new Date()), 25);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0,0,10,10);
		container.add(timeTextField, c);
		
		JPanel buttonPanel = new JPanel();
    
	    Action jumpLocationAction = new AbstractAction() {
	      public void actionPerformed(ActionEvent e) {
	        jumpLocation();
	      }      
	    };
	    jumpLocationAction.putValue(Action.NAME, "Jump Location");
	    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "jumpLocation");
	    actionMap.put("jumpLocation", jumpLocationAction);
			jumpButton = new JButton(jumpLocationAction);
			buttonPanel.add(jumpButton);
			
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
			dateTextField.requestFocusInWindow();
//	 		dateTextField.setSelectionStart(0);
//	 		dateTextField.setSelectionEnd(dateTextField.getText().length());			
		}
		super.setVisible(visible);
	}
	
	private void jumpLocation() {
		String strDateLocation;
		String strTimeLocation;
		String dateAndTime;
		Date dateLocation;
		
		try {
			
			strDateLocation = dateTextField.getText();
			strTimeLocation = timeTextField.getText();
			dateAndTime = strDateLocation + " " + strTimeLocation;
			DateFormat dateTimeFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");  //create a formatter to append time to date with space in between
			dateLocation = dateTimeFormat.parse(dateAndTime);

			double secondsLocation = dateLocation.getTime() / 1000;  // convert to seconds
			
//			System.out.println("Date Time in seconds: " + secondsLocation);
			rbnb.setLocation(secondsLocation);
			dispose();
			
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			timeLabel.setForeground(Color.RED);
			timeTextField.setText(TIME_FORMAT.format(new Date()));
			timeTextField.requestFocusInWindow();
			
			return;
		}
	}
	
	private void cancel() {
		dispose();		
	}
	
}
