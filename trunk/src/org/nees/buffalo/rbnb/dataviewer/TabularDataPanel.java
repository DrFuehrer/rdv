/*
 * Created on Mar 23, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import javax.swing.JPanel;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class TabularDataPanel extends AbstractDataPanel {

	private JPanel tabularDataPanel;
	
	public TabularDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
		
		initComponents();
		
		setDataComponent(tabularDataPanel);
		setControlBar(true);
		setDropTarget(true);

	}
	
	private void initComponents() {
		tabularDataPanel = new JPanel();
	}

	void clearData() {
	}

	public String[] getSupportedMimeTypes() {
		return new String[] {"application/octet-stream"};
	}

	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public boolean addChannel(Channel channel) {
		if (!super.addChannel(channel)) {
			return false;
		}
		
		String channelName = channel.getName();
		String unit = channel.getUnit();
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
		return true;
	}
	
	public void postData(ChannelMap channelMap) {
		super.postData(channelMap);
	}
	
	public void postTime(double time) {
		super.postTime(time);
	}
	
	public String toString() {
		return "Tabular Data Panel";
	}
}
