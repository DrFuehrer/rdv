/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import java.util.Iterator;

import javax.swing.JComponent;

/**
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

	JComponent dataComponent;
	
	public TemplateDataPanel() {
		super();

		initDataComponent();
	}
	
	private void initDataComponent() {
		// TODO create data component
		
		setDataComponent(dataComponent);
	}

	void clearData() {
		// TODO Auto-generated method stub
	}

	public boolean supportsMultipleChannels() {
		// TODO Auto-generated method stub
		return false;
	}

	public void postTime(double time) {
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
