/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import java.util.Iterator;

import javax.swing.JComponent;

/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

	/**
	 * The UI component to display the data in
	 */
	JComponent dataComponent;
	
	/**
	 * Initialize the object and UI
	 */
	public TemplateDataPanel() {
		super();

		initDataComponent();
	}
	
	/**
	 * Initialize the UI component and pass it too the abstract class.
	 */
	private void initDataComponent() {
		// TODO create data component
		
		setDataComponent(dataComponent);
	}

	void clearData() {
		// TODO clear your data display
	}

	public boolean supportsMultipleChannels() {
		// TODO change if this data panel supports multiple channels
		return false;
	}

	public void postTime(double time) {
		super.postTime(time);
		
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				// TODO display the data in your data component
			}
		}
	}
}
