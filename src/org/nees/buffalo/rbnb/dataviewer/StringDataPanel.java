package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class StringDataPanel extends AbstractDataPanel {

	static Log log = LogFactory.getLog(StringDataPanel.class.getName());
		
	JPanel panel;
	JTextArea messages;
	
	public StringDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
				
		initPanel();
		setDataComponent(panel);
		
		setControlBar(true);
		setDropTarget(true);
	}
	
	JComponent initPanel() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
				
		messages = new JTextArea();
		messages.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(messages);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}
	
	public String[] getSupportedMimeTypes() {
		return new String[] { "text/plain" };
	}

	public boolean supportsMultipleChannels() {
		return true;
	}
		
	public void postData(ChannelMap channelMap) {
		postData(channelMap, -1, -1);
	}
	
	public void postData(ChannelMap channelMap, double startTime, double duration) {
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postData(channelMap, channelName, channelIndex, startTime, duration);
			}
		}
	}

	private void postData(ChannelMap channelMap, String channelName, int channelIndex, double startTime, double duration) {
		String[] data = channelMap.GetDataAsString(channelIndex);
		double[] times = channelMap.GetTimes(channelIndex);

		TimeIndex index = getTimeIndex(times, startTime, duration);
		int startIndex = index.startIndex;
		int endIndex = index.endIndex;
		
		//see if there is no data in the time range we are loooking at
		if (startIndex == -1 || endIndex == -1) {
			return;
		}
		
		for (int i=startIndex; i<endIndex; i++) {
			messages.append(channelName + " (" + DataViewer.formatDate(times[i])+ ") : " + data[i] + "\n");
		}
	}
	
	void clearData() {
		messages.setText(null);
	}
}