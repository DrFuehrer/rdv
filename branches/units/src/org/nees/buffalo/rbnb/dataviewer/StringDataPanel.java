package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class StringDataPanel extends AbstractDataPanel {

	static Log log = LogFactory.getLog(StringDataPanel.class.getName());
		
	JPanel panel;
	JLabel title;
	JPanel dataPanel;
	
	Hashtable labels;
	
	public StringDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
				
		labels = new Hashtable();
		
		initPanel();
		setDataComponent(panel);
		
		setControlBar(true);
		setDropTarget(true);
	}
	
	JComponent initPanel() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
		
		title = new JLabel("EMPTY");
		panel.add(title, BorderLayout.NORTH);
		
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		panel.add(dataPanel, BorderLayout.CENTER);
		
		return panel;
	}
	
	public String[] getSupportedMimeTypes() {
		return new String[] { "text/plain" };
	}

	public boolean supportsMultipleChannels() {
		return true;
	}

	public void addChannel(String channelName, String unit) {
		super.addChannel(channelName, unit);
		
		JLabel label = new JLabel(channelName + ": ");
		dataPanel.add(label);
		labels.put(channelName, label);
		
		title.setText(getTitle());
	}

	public void removeChannel(String channelName) {
		super.removeChannel(channelName);
		
		JLabel label = (JLabel)labels.remove(channelName);
		dataPanel.remove(label);
		
		title.setText(getTitle());
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
		//FIXME this doesn't look at the startTime and duration
		JLabel dataLabel = (JLabel)labels.get(channelName);
		String[] data = channelMap.GetDataAsString(channelIndex);
		StringBuffer text = new StringBuffer(channelName + ": ");
		for (int i=0; i<data.length; i++) {
			text.append(data[i] + ", ");
		}
		dataLabel.setText(text.toString());
	}
	
	void clearData() {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			JLabel dataLabel = (JLabel)labels.get(channelName);
			dataLabel.setText(null);
		}
	}
}