package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
	JEditorPane messages;
	JScrollPane scrollPane;
  StringBuffer messageBuffer;
  
  String[] AVAILABLE_COLORS = {"blue", "green", "maroon", "purple", "red", "olive"};
  Hashtable colors;
	
	double lastTimeDisplayed;
	
	public StringDataPanel() {
		super();
		
		lastTimeDisplayed = -1;
    messageBuffer = new StringBuffer();
    
    colors = new Hashtable();
				
		initPanel();
		setDataComponent(panel);
	}
	
	private void initPanel() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
				
		messages = new JEditorPane();
		messages.setEditable(false);
    messages.setContentType("text/html");
		scrollPane = new JScrollPane(messages,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane, BorderLayout.CENTER);
	}
  
  public boolean addChannel(String channelName) {
    if (super.addChannel(channelName)) {
      colors.put(channelName, AVAILABLE_COLORS[(channels.size()-1)%AVAILABLE_COLORS.length]);
      return true;
    } else {
      return false;
    }
  }
  
  public boolean removeChannel(String channelName) {
    if (super.removeChannel(channelName)) {
     colors.remove(channelName);
     return true;
    } else {
      return false;
    }
  }
	
	public boolean supportsMultipleChannels() {
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
        postDataText(channelName, channelIndex);
			}
		}
    
    lastTimeDisplayed = time;
	}

	private void postDataText(String channelName, int channelIndex) {
    //We only know how to display strings
    if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_STRING) {
      return;   
    }

    String shortChannelName = channelName.substring(channelName.lastIndexOf('/')+1);
    String channelColor = (String)colors.get(channelName);
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
			messageBuffer.append("<strong style=\"color: " + channelColor + "\">" + shortChannelName + "</strong> (<em>" + DataViewer.formatDateSmart(times[i])+ "</em>): " + data[i] + "<br>");
		}
    messages.setText(messageBuffer.toString());

    int max = scrollPane.getVerticalScrollBar().getMaximum();
    scrollPane.getVerticalScrollBar().setValue(max);
	}
	
	void clearData() {
		messages.setText(null);
    messageBuffer.delete(0, messageBuffer.length());
		lastTimeDisplayed = -1;
	}
	
	public String toString() {
		return "Text Data Panel";
	}
}