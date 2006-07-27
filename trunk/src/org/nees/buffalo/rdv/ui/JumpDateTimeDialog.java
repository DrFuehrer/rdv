
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
import java.text.ParseException;

public class JumpDateTimeDialog extends JDialog {

 	static Log log = LogFactory.getLog(JumpDateTimeDialog.class.getName());
	
	RBNBController rbnb;
	DataPanelManager dataPanelManager;
  
  Date defaultDate;
	
	JLabel headerLabel;
	
	JLabel dateLable;
	JTextField dateTextField;
	
	JLabel timeLabel;
	JTextField timeTextField;
	
	JButton jumpButton;
	JButton cancelButton;
	
	JLabel dateLabelExample;
	JLabel timeLabelExample;
	
	JLabel errorLabel;
	
	/** Format to use to display the date property. */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");
	
	public JumpDateTimeDialog(JFrame owner, RBNBController rbnbController, DataPanelManager dataPanelManager) {
		super(owner, true);
		
		this.rbnb = rbnbController;
		this.dataPanelManager = dataPanelManager;
    
    defaultDate = new Date(((long)(rbnb.getLocation()*1000)));
		
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

		errorLabel = new JLabel();
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		container.add(errorLabel, c);

		
		dateLabelExample = new JLabel("Date Format: mm-dd-yyyy");
		dateLabelExample.setForeground(Color.BLUE);
		dateLabelExample.setOpaque(true);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,10,5);
		container.add(dateLabelExample, c);

		dateLable = new JLabel("Date:");
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 3;
		container.add(dateLable, c);
		
		dateTextField = new JTextField(DATE_FORMAT.format(defaultDate), 10);
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 3;
		container.add(dateTextField, c);
		
		timeLabelExample = new JLabel("Time Format: hh:mm:ss PDT(EDT or GMT)");
		timeLabelExample.setForeground(Color.BLUE);
		timeLabelExample.setOpaque(true);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		container.add(timeLabelExample, c);
		
		timeLabel = new JLabel("Time:");
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		container.add(timeLabel, c);

		timeTextField = new JTextField(TIME_FORMAT.format(defaultDate), 10);
		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
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
		c.gridy = 6;
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
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			df.setLenient(false);  // this is important!
			
			strDateLocation = dateTextField.getText().trim();
			dateLocation = df.parse(strDateLocation);				

			df = new SimpleDateFormat("HH:mm:ss z");
			df.setLenient(false);
			
			strTimeLocation = timeTextField.getText().trim();
			dateLocation = df.parse(strTimeLocation);	

			df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");  //create a formatter to append time to date with space in between
			dateAndTime = strDateLocation + " " + strTimeLocation;

			dateLocation = df.parse(dateAndTime);
			double secondsLocation = dateLocation.getTime() / 1000;  // convert to seconds
			
			rbnb.setLocation(secondsLocation);
			dispose();
			
		} catch (Exception e) {
			errorLabel.setForeground(Color.RED);
			errorLabel.setText("Error: invalid date. Please try again!");

			timeLabel.setForeground(Color.RED);
			timeTextField.setText(TIME_FORMAT.format(defaultDate));
			
			dateLable.setForeground(Color.RED);
			dateTextField.setText(DATE_FORMAT.format(defaultDate));
			dateTextField.requestFocusInWindow();
		}

			
	}
	
	private void cancel() {
		dispose();		
	}
	
}
