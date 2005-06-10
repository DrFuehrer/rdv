package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class StringDataPanel extends AbstractDataPanel {

	static Log log = LogFactory.getLog(StringDataPanel.class.getName());
		
	JPanel panel;
	JTextArea messages;
	JScrollPane scrollPane;
	
	double lastTimeDisplayed;
	
	public StringDataPanel() {
		super();
		
		lastTimeDisplayed = -1;
				
		initPanel();
		setDataComponent(panel);
	}
	
	private void initPanel() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
				
		messages = new JTextArea();
		messages.setEditable(false);
		messages.setDropTarget(null);
		scrollPane = new JScrollPane(messages);
		panel.add(scrollPane, BorderLayout.CENTER);
	}
	
	public boolean supportsMultipleChannels() {
		return false;
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
				postDataText(channelName, channelIndex);
			}
		}
	}

	private void postDataText(String channelName, int channelIndex) {
    //We only know how to display strings
    if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_STRING) {
      return;   
    }
        
		String[] data = channelMap.GetDataAsString(channelIndex);
		double[] times = channelMap.GetTimes(channelIndex);

		int startIndex = -1;
		
		for (int i=0; i<times.length; i++) {
			if (times[i] > lastTimeDisplayed && times[i] <= time) {
				startIndex = i;
				break;
			}
		}
		
		//see if there is no data in the time range we are loooking at
		if (startIndex == -1) {
			return;
		}		

		int endIndex = startIndex;
		
		for (int i=times.length-1; i>startIndex; i--) {
			if (times[i] <= time) {
				endIndex = i;
				break;
			}
		}
				
		for (int i=startIndex; i<=endIndex; i++) {
			messages.append(channelName + " (" + DataViewer.formatDate(times[i])+ ") : " + data[i] + "\n");
		}
		
		int maximum = scrollPane.getHorizontalScrollBar().getMaximum();
		scrollPane.getHorizontalScrollBar().setValue(maximum);
		
		lastTimeDisplayed = times[endIndex];
	}
	
	void clearData() {
		messages.setText(null);
		lastTimeDisplayed = -1;
	}
	
	public String toString() {
		return "Text Data Panel";
	}
}