package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

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
		
		panel = new JPanel();
		panel.setBorder(new EtchedBorder());
		
		panel.setLayout(new BorderLayout());
		
		title = new JLabel("EMPTY");
		panel.add(title, BorderLayout.NORTH);
		
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		panel.add(dataPanel, BorderLayout.CENTER);
		
		labels = new Hashtable();

		setDetach(true);
		setDropTarget(true);
	}

	public JComponent getComponent() {
		return panel;
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
	
	String getTitle() {
		String titleString = "";
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			titleString += i.next() + ", ";
		}

		return titleString;
	}
	
	public String[] getSupportedMimeTypes() {
		return new String[] { "text/plain" };
	}
}