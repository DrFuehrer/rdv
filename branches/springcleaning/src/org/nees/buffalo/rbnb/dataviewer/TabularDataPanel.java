/*
 * Created on Mar 23, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class TabularDataPanel extends AbstractDataPanel {

	private JPanel tabularDataPanel;
	private GridLayout gridLayout;
	
	private Hashtable rows;
	
	public TabularDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
		
		rows = new Hashtable();
		
		initComponents();
		
		setDataComponent(tabularDataPanel);
		setControlBar(true);
		setDropTarget(true);
	}
	
	private void initComponents() {
		tabularDataPanel = new JPanel();
		gridLayout = new GridLayout(1, 1);
		tabularDataPanel.setLayout(gridLayout);
		
		/* TabularDataRow headerRow = new TabularDataRow("Channel Name", "Value");
		tabularDataPanel.add(headerRow); */
	}

	void clearData() {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			TabularDataRow row = (TabularDataRow)rows.get(channelName);
			row.clearData();
		}
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
		
		String labelText = channelName;
		if (unit != null) {
			labelText += "(" + unit + ")";
		}
		
		gridLayout.setRows(gridLayout.getRows()+1);
		TabularDataRow row = new TabularDataRow(labelText);
		tabularDataPanel.add(row);
		rows.put(channelName, row);
		
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
		TabularDataRow row = (TabularDataRow)rows.get(channelName);
		rows.remove(channelName);
		tabularDataPanel.remove(row);
		
		return true;
	}
	
	public void postData(ChannelMap channelMap) {
		super.postData(channelMap);
	}
	
	public void postTime(double time) {
		super.postTime(time);
		
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postDataTabular(channelName, channelIndex);
			}
		}
	}
	
	private void postDataTabular(String channelName, int channelIndex) {
		int dataIndex = -1;
		
		double[] times = channelMap.GetTimes(channelIndex);
		for (int i=times.length-1; i>=0; i--) {
			// TODO we could add a check for the duration as
			//      as a lower bound here
			if (times[i] <= time) {
				dataIndex = i;
				break;
			}
		}
		
		if (dataIndex == -1) {
			//no data in this time for us to display
			return;
		}
		
		double data;
		
		int typeID = channelMap.GetType(channelIndex);
		
		switch (typeID) {
		case ChannelMap.TYPE_FLOAT64:					
			data = channelMap.GetDataAsFloat64(channelIndex)[dataIndex];
			break;
		case ChannelMap.TYPE_FLOAT32:
			data = channelMap.GetDataAsFloat32(channelIndex)[dataIndex];
			break;					
		case ChannelMap.TYPE_INT64:
			data = channelMap.GetDataAsInt64(channelIndex)[dataIndex];
			break;
		case ChannelMap.TYPE_INT32:
			data = channelMap.GetDataAsInt32(channelIndex)[dataIndex];
			break;
		case ChannelMap.TYPE_INT16:
			data = channelMap.GetDataAsInt16(channelIndex)[dataIndex];
			break;					
		case ChannelMap.TYPE_INT8:					
			data = channelMap.GetDataAsInt8(channelIndex)[dataIndex];
			break;					
		case ChannelMap.TYPE_STRING:
		case ChannelMap.TYPE_UNKNOWN:
		case ChannelMap.TYPE_BYTEARRAY:
		default:
			return;
		}
		
		TabularDataRow row = (TabularDataRow)rows.get(channelName);
		row.setData(data);
	}
	
	public String toString() {
		return "Tabular Data Panel";
	}
	
	class TabularDataRow extends JPanel {
		JLabel label;
		JLabel data;
		JLabel min;
		JLabel max;
		
		double minData;
		double maxData;
		
		boolean gotData;
		
		public TabularDataRow(String labelText) {
			this(labelText, null);
		}
			
		public TabularDataRow(String labelText, String dataText) {
			label = new JLabel(labelText);
			data = new JLabel(dataText);
			min = new JLabel();
			max = new JLabel();
			
			setLayout(new GridLayout(1,4));
			add(label);
			add(data);
			add(min);
			add(max);
			
			gotData = false;
		}
		
		public void setLabel(String labelText) {
			label.setText(labelText);
		}
		
		public void setData(double value) {
			if (!gotData) {
				gotData = true;
				minData = maxData = value;
			} else {
				minData = Math.min(minData, value);
				maxData = Math.max(maxData, value);
			}
			
			String dataText = Double.toString(value);
			data.setText(dataText);
			min.setText(Double.toString(minData));
			max.setText(Double.toString(maxData));
		}
		
		public void clearData() {
			gotData = false;
			data.setText(null);
			min.setText(null);
			max.setText(null);
		}
	}
}
