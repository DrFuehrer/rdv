/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.event.MouseInputAdapter;

/**
 * @author Jason P. Hanley
 */
public abstract class AbstractDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {
	DataPanelContainer dataPanelContainer;
	Player player;
	
	HashSet channels;
	Hashtable units;
	
	double time;
	double domain;
	
	JFrame frame;
	boolean attached;
	
	JWindow window;
	boolean maximized;
	
 	static boolean iconsLoaded = false;
 	static Image windowPinImage;
 	static Image windowSnapshotImage;
 	static Image windowDetachImage;
 	static Image windowMaximizeImage;
 	static Image windowCloseImage;
 	
 	static String windowPinFileName = "icons/window_pin.gif";
 	static String windowSnapshotFileName = "icons/window_snapshot.gif";
 	static String windowDetachFileName = "icons/window_detach.gif";
 	static String windowMaximizeFileName = "icons/window_maximize.gif";
 	static String windowCloseFileName = "icons/window_close.gif";
 	
 	boolean hasFocus;
 	
 	boolean pinned;
 	boolean paused;

	public AbstractDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
			
		channels = new HashSet();
		units = new Hashtable();
		
		time = 0;
		domain = 1;
		
		attached = true;
		
		maximized = false;

		hasFocus = false;
		
		pinned = false;
		paused = false;

		if (!iconsLoaded) {
			loadIcons();
		}
		
		player.addTimeListener(this);
		player.addStateListener(this);
	}

	public void setChannel(String channelName, String unit) {
		if (channels.size() == 1 && channels.contains(channelName)) {
			return;
		}
		
		removeAllChannels();
		addChannel(channelName, unit);
	}
	
	public void addChannel(String channelName, String unit) {
		if (channels.contains(channelName)) {
			return;
		}
		
		player.subscribe(channelName, this);

		channels.add(channelName);
		if (unit != null) {
			units.put(channelName, unit);
		}
		
		if (!attached) {
			frame.setTitle(getTitle());
		}
	}
	
	public void removeChannel(String channelName) {
		if (!channels.contains(channelName)) {
			return;
		}
		
		channels.remove(channelName);
		units.remove(channelName);
		
		player.unsubscribe(channelName, this);

		if (!attached) {
			frame.setTitle(getTitle());
		}		
	}
	
	void removeAllChannels() {
		Object[] channelNames = channels.toArray();
		for (int i=0; i<channelNames.length; i++) {
			removeChannel((String)channelNames[i]);
		}
	}
	
	/*
	 * Clear the data displayed on the data panel.
	 */
	abstract void clearData();
	
	/*
	 * Get the title of the data panel
	 */
	abstract String getTitle();
	
	public void postTime(double time) {
		this.time = time;
	}
	
	public void setDomain(double domain) {
		this.domain = domain;
	}
	
	public void postState(int newState, int oldState) {
		switch (newState) {
			case Player.STATE_LOADING:
			case Player.STATE_MONITORING:
				clearData();
				break;
		}
	}
	
	static void loadIcons() {
		windowPinImage = new ImageIcon(windowPinFileName).getImage();
		windowSnapshotImage = new ImageIcon(windowSnapshotFileName).getImage();
		windowDetachImage = new ImageIcon(windowDetachFileName).getImage();
		windowMaximizeImage = new ImageIcon(windowMaximizeFileName).getImage();
		windowCloseImage = new ImageIcon(windowCloseFileName).getImage();
		
		iconsLoaded = true;
	}
	
	void setDetach(boolean enable) {
		if (enable) {
			final JComponent panel = getComponent();
			panel.addMouseListener(new MouseInputAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getX() >= panel.getWidth()-10 && e.getY() <= 12) {
							closePanel();
						} else {
							toggleDetach();
						}
					}
				}
			});
		}
	}
	
	public void toggleDetach() {
		if (attached) {
			detachPanel();
		} else {
			attachPanel();
		}
	}
	
	public void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(this);
		
		frame = new JFrame();
		if (!attached) {
			frame.setTitle(getTitle());
		}
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attachPanel();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		
		frame.getContentPane().add(getComponent());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void attachPanel() {
		frame.setVisible(false);
		frame.getContentPane().remove(getComponent());
		frame.dispose();
		frame = null;			

		dataPanelContainer.addDataPanel(this);
		attached = true;	
	}
	
	public void closePanel() {
		removeAllChannels();
		
		if (attached) {
			dataPanelContainer.removeDataPanel(this);
		} else  if (frame != null) {
			frame.setVisible(false);
			frame.getContentPane().remove(getComponent());
			frame.dispose();
			frame = null;			
		}
		
		player.removeStateListener(this);
		player.removeTimeListener(this);
	}	
	
	public void setDropTarget(boolean enable) {
		if (enable) {
			new DropTarget(getComponent(), DnDConstants.ACTION_LINK, this);
		} else {
			
		}
	}

	public void dragEnter(DropTargetDragEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}
	
	public void dropActionChanged(DropTargetDragEvent e) {}
	
	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();
			if(e.isDataFlavorSupported(stringFlavor)) {
				String data = (String)tr.getTransferData(stringFlavor);
				String[] tokens = data.split("\t");
				String channelName = tokens[0];
				String unit = null;
				if (tokens.length == 2) {
					unit = tokens[1];
				}
				e.acceptDrop(DnDConstants.ACTION_LINK);
				e.dropComplete(true);
				
				try {
					if (supportsMultipleChannels()) {
						addChannel(channelName, unit);
					} else {
						setChannel(channelName, unit);
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
