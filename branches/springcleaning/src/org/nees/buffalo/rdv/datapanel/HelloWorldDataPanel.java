/*
 * Created on Apr 5, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import javax.swing.JLabel;

import org.nees.buffalo.rdv.DataViewer;

/**
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class HelloWorldDataPanel extends AbstractDataPanel {

	JLabel label;
	
	/**
	 * 
	 */
	public HelloWorldDataPanel() {
		super();
		
		label = new JLabel();
		clearData();
		setDataComponent(label);
	}

	void clearData() {
		label.setText("Hello World!");
	}

	public boolean supportsMultipleChannels() {
		return false;
	}
	
	public void postTime(double time) {
		super.postTime(time);
		label.setText("Hello World! The time is " + DataViewer.formatDate(time));
	}
}
