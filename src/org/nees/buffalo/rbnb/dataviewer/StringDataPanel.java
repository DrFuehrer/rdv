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
public class StringDataPanel implements DataPanel2, PlayerChannelListener, DropTargetListener {

	static Log log = LogFactory.getLog(StringDataPanel.class.getName());
	
	DataPanelContainer dataPanelContainer;
	Player player;
	
	JPanel panel;
	JLabel title;
	JPanel dataPanel;
	
	ArrayList channels;
	ArrayList labels;
	
	public StringDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
		
		panel = new JPanel();
		panel.setBorder(new EtchedBorder());
		
		panel.setLayout(new BorderLayout());
		
		title = new JLabel("EMPTY");
		panel.add(title, BorderLayout.NORTH);
		
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		panel.add(dataPanel, BorderLayout.CENTER);
		
		channels = new ArrayList();
		labels = new ArrayList();
		
		new DropTarget(panel, DnDConstants.ACTION_LINK, this);
	}

	public JComponent getComponent() {
		return panel;
	}

	public String[] getSupportedMimeTypes() {
		return null;
	}

	public boolean supportsMultipleChannels() {
		return true;
	}

	public void setChannel(String channelName) {
		player.unsubscribeAll(this);
		channels.clear();
		addChannel(channelName);
	}
	public void addChannel(String channelName) {
		if (channels.contains(channelName)) return;
		
		log.debug("Adding channel: " + channelName + ".");
		
		channels.add(channelName);
		setTitle();
				
		player.subscribe(channelName, this);
		
		JLabel label = new JLabel(channelName + ": ");
		dataPanel.add(label);
		labels.add(channels.indexOf(channelName), label);
	}

	public void removeChannel(String channelName) {
		if (!channels.contains(channelName)) return;
		
		log.debug("Removing channel: " + channelName + ".");
		
		channels.remove(channelName);
		setTitle();
		
		player.unsubscribe(channelName, this);
		
		JLabel label = (JLabel)labels.remove(channels.indexOf(channelName));
		dataPanel.remove(label);
	}

	public void setDomain(double domain) {}

	public void closePanel() {
		removeAllChannels();
		
		dataPanelContainer.removeDataPanel(this);
	}
	
	private void removeAllChannels() {
		player.unsubscribeAll(this);
		channels.clear();
	}


	
	public void postData(ChannelMap channelMap, int channelIndex, String channelName) {
		postData(channelMap, channelIndex, channelName, -1, -1);
	}

	public void postData(ChannelMap channelMap, int channelIndex, String channelName, double location, double duration) {
		JLabel dataLabel = (JLabel)labels.get(channels.indexOf(channelName));
		String[] data = channelMap.GetDataAsString(channelIndex);
		StringBuffer text = new StringBuffer(channelName + ": ");
		for (int i=0; i<data.length; i++) {
			text.append(data[i] + ", ");
		}
		dataLabel.setText(text.toString());
	}
	
	private void setTitle() {
		String titleString = "";
		for(int i=0; i < channels.size(); i++) {
			titleString += channels.get(i) + (i==channels.size()-1?"" : ", ");
		}
		
		title.setText(titleString);
	}

	public void dragEnter(DropTargetDragEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}
	
	public void dropActionChanged(DropTargetDragEvent e) {}
	
	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();
			if(e.isDataFlavorSupported(stringFlavor)) {
				String channelName = (String)tr.getTransferData(stringFlavor);
				e.acceptDrop(DnDConstants.ACTION_LINK);
				e.dropComplete(true);
				
				try {
					if (supportsMultipleChannels()) {
						addChannel(channelName);
					} else {
						setChannel(channelName);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				e.rejectDrop();
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}	
	}
	
	public void dragExit(DropTargetEvent e) {}
}
