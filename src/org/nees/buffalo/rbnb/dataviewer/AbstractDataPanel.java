/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
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
import java.util.Iterator;

import javax.swing.border.AbstractBorder;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jason P. Hanley
 */
public abstract class AbstractDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {

	static Log log = LogFactory.getLog(AbstractDataPanel.class.getName());
	
	DataPanelContainer dataPanelContainer;
	Player player;
	
	HashSet channels;
	Hashtable units;
	
	double time;
	double domain;

	JPanel component;
	JComponent dataComponent;
	ControlBarBorder controlBarBorder;
	
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
		
		component = new JPanel();
		
		controlBarBorder = new ControlBarBorder();
		
		attached = true;
		
		maximized = false;

		hasFocus = false;
		
		pinned = true;
		
		paused = false;

		if (!iconsLoaded) {
			loadIcons();
		}
						
		player.addTimeListener(this);
		player.addStateListener(this);
	}

	public void setChannel(Channel channel) {
		String channelName = channel.getName();
		String unit = channel.getUnit();
		
		if (channels.size() == 1 && channels.contains(channelName)) {
			return;
		}
		
		removeAllChannels();
		addChannel(channel);
	}
	
	public void addChannel(Channel channel) {
		String channelName = channel.getName();
		String unit = channel.getUnit();
		
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
		} else if (pinned) {
			component.repaint();
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
		} else if (pinned) {
			component.repaint();
		}
	}
	
	void removeAllChannels() {
		Object[] channelNames = channels.toArray();
		for (int i=0; i<channelNames.length; i++) {
			removeChannel((String)channelNames[i]);
		}
	}
		
	public void setDataComponent(JComponent dataComponent) {
		this.dataComponent = dataComponent;
		
		if (pinned) {
			dataComponent.setBorder(controlBarBorder);
		}
		
		component.setBorder(new EtchedBorder());
				
		component.setLayout(new BorderLayout());
		component.add(dataComponent, BorderLayout.CENTER);
	}
	
	public JComponent getComponent() {
		return component;
	}	
	
	/*
	 * Clear the data displayed on the data panel.
	 */
	abstract void clearData();
	
	/*
	 * Get the title of the data panel
	 */
	String getTitle() {
		String titleString = "";
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			titleString += i.next();
			if (i.hasNext()) {
				titleString += ", ";
			}
		}

		return titleString;
	}
	
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
	
	void setControlBar(boolean enable) {
		if (enable) {
			component.addMouseListener(new MouseInputAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
	 					int x = e.getX();
	 					int y = e.getY();
	 
	 					int componentWidth = component.getWidth();
	 					int componentHeight = component.getHeight();
	 					
	 					if (x < 16 && y < 16) {
	 						pinned = !pinned;
	 					} else if (x >= 16 && x < 32 && y < 16) {
	 						togglePause();
	 					} else if (x >= componentWidth-16 && y < 16) {
							closePanel();
	 					} else if (x <  componentWidth-16 && x >= componentWidth-32 && y < 16) {
							toggleMaximize();
						} else if (x <  componentWidth-32 && x >= componentWidth-48 && y < 16) {
							toggleDetach(); 												
						}
					}
				}
				
				public void mouseEntered(MouseEvent e) {
					hasFocus = true;
					if (!pinned) {
						dataComponent.setBorder(controlBarBorder);
					}
				}
				
				public void mouseExited(MouseEvent e) {
					hasFocus = false;
					if (!pinned) {
						dataComponent.setBorder(null);
					}
				}
			});
		}
	}

	public void togglePause() {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			
			if (paused) {
				player.subscribe(channelName, this);
			} else {
				player.unsubscribe(channelName, this);
			}
		}
		
		paused = !paused;
	}
	
	public void closePanel() {
		removeAllChannels();

 		if (maximized) {
 			restorePanel(false);
		} else if (!attached) {
			attachPanel(false);		 			
 		} else if (attached) {
			dataPanelContainer.removeDataPanel(this);
 		}
 		
		player.removeStateListener(this);
		player.removeTimeListener(this);
	}
	
	public void toggleDetach() {
		if (maximized) {
			window.setVisible(false);
			window.getContentPane().remove(component);
			window.dispose();
			window = null;
			maximized = false;
		}
		
		if (attached) {
			detachPanel();
		} else {
			attachPanel(true);
		}
	}
	
	public void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(this);
		
		frame = new JFrame(getTitle());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closePanel();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		

		frame.getContentPane().add(component);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void attachPanel(boolean addToContainer) {
		if (frame != null) {
			frame.setVisible(false);
			frame.getContentPane().remove(component);
			frame.dispose();
			frame = null;
		}
		
		if (addToContainer) {
			attached = true;
			dataPanelContainer.addDataPanel(this);
		}
	}
	
	public void toggleMaximize() {	
		if (maximized) {
			restorePanel(attached);
			if (!attached) {
				detachPanel();
			}
		} else {
			if (!attached) {
				attachPanel(false);
			}
			maximizePanel();
		}
	}
	
	public void maximizePanel() {
		maximized = true;
		dataPanelContainer.removeDataPanel(this);
		
		window = new JWindow();
		window.getContentPane().add(component);
		window.setSize(getScreenDimensions());
		window.setVisible(true);
	}
	
	public void restorePanel(boolean addToContainer) {
		window.setVisible(false);
		window.getContentPane().remove(component);
		window.dispose();
		window = null;

		maximized = false;
		
		if (addToContainer) {
			dataPanelContainer.addDataPanel(this);
		}
	}	
	
	Dimension getScreenDimensions() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		    
		for (int i=0; i<gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			int screenWidth = dm.getWidth();
			int screenHeight = dm.getHeight();
			return new Dimension(screenWidth, screenHeight);
		}
		
		return null;
	}
	
	public void setDropTarget(boolean enable) {
		if (enable) {
			new DropTarget(component, DnDConstants.ACTION_LINK, this);
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
					Channel channel = new Channel(channelName, null, unit);
					if (supportsMultipleChannels()) {
						addChannel(channel);
					} else {
						setChannel(channel);
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
	
	class TimeIndex {
		public int startIndex;
		public int endIndex;
		
		public TimeIndex(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}
	
	public TimeIndex getTimeIndex(double[] times, Time time) {
		double startTime = time.location;
		double duration = time.duration;
		
		int startIndex = -1;
		int endIndex = -1;

		if (!time.isUnspecified()) {
			for (int i=0; i<times.length; i++) {
				if (times[i] >= startTime) {
					startIndex = i;
					break;
				}
			}
			
			if (startIndex != -1) {
				double endTime = startTime + duration;
				for (int i=startIndex; i<times.length; i++) {
					if (times[i] < endTime) {
						endIndex = i;
					} else {
						break;
					}
				}
			}
		} else {
			startIndex = 0;
			endIndex = times.length-1;
		}
		
		return new TimeIndex(startIndex, endIndex);
	}
			
	class ControlBarBorder extends AbstractBorder {
		
		public boolean isBorderOpaque() {
			return true;
		}
		
		public Insets getBorderInsets(Component c) {
				return new Insets(16, 0, 0, 0);			
		}
		
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			int componentWidth = c.getWidth();
			int componentHeight = c.getHeight();
			
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, componentWidth, 16);
			g.setColor(Color.WHITE);
			g.drawString(getTitle(), 36, 12);
			g.drawImage(windowPinImage, 0, 0, null);
			g.drawImage(windowSnapshotImage, 16, 0, null);
			g.drawImage(windowDetachImage, componentWidth-48, 0, null);
			g.drawImage(windowMaximizeImage, componentWidth-32, 0, null);
			g.drawImage(windowCloseImage, componentWidth-16, 0, null);
		}
	}
}