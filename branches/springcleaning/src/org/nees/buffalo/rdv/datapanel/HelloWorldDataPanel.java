/*
 * Created on Apr 5, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import javax.swing.JLabel;

import org.nees.buffalo.rdv.DataViewer;

/**
 * A simple data panel to show a message and the current time of the
 * RBNBController.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class HelloWorldDataPanel extends AbstractDataPanel {

	JLabel label;
	
	/**
	 * Construct a Hello World data panel with the default message.
	 * 
	 * @since  1.2
	 */
	public HelloWorldDataPanel() {
		super();
		
		label = new JLabel("Hello World!");
		setDataComponent(label);
	}

	/**
	 * Set the message to the default
	 * 
	 * @since  1.2
	 */
	void clearData() {
		label.setText("Hello World!");
	}

	/**
	 * Return false indicating we don't support multiple channels.
	 * 
	 * @since  1.2
	 */
	public boolean supportsMultipleChannels() {
		return false;
	}
	
	/**
	 * Update the message to include the current time.
	 * 
	 * @since  1.2
	 */
	public void postTime(double time) {
		super.postTime(time);
		label.setText("Hello World! The time is " + DataViewer.formatDate(time));
	}
}