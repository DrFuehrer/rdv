/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import java.util.Iterator;

import javax.swing.JComponent;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

	JComponent dataComponent;
	
	/**
	 * @param dataPanelContainer
	 * @param player
	 */
	public TemplateDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);

		initDataComponent();
		
		setControlBar(true);
		setDropTarget(true);
	}
	
	private void initDataComponent() {
		// TODO create data component
		
		setDataComponent(dataComponent);
	}

	void clearData() {
		// TODO Auto-generated method stub
	}

	public String[] getSupportedMimeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean supportsMultipleChannels() {
		// TODO Auto-generated method stub
		return false;
	}

	//FIXME to new interface (change to postTime)
	/* public void postData(ChannelMap channelMap, Time time) {
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
	} */
}
