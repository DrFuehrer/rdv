/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.DataListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;
import org.nees.buffalo.rdv.rbnb.TimeScaleListener;
import org.nees.buffalo.rdv.ui.DataPanelContainer;

import com.rbnb.sapi.ChannelMap;

/**
 * @since   1.1
 * @author  Jason P. Hanley
 */
public abstract class AbstractDataPanel implements DataPanel, DataListener, TimeListener, TimeScaleListener, StateListener, DropTargetListener {

	static Log log = LogFactory.getLog(AbstractDataPanel.class.getName());
	
	DataPanelManager dataPanelManager;
	DataPanelContainer dataPanelContainer;
	RBNBController rbnbController;
	
	HashSet channels;
	Hashtable units;
	
	double time;
	double timeScale;

	JPanel component;
	JComponent dataComponent;
	ControlBarBorder controlBarBorder;
	
	JFrame frame;
	boolean attached;
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

	ChannelMap channelMap;

	public AbstractDataPanel() {		
		channels = new HashSet();
		units = new Hashtable();
		
		time = 0;
		timeScale = 1;
		
		attached = true;
		
		maximized = false;

		hasFocus = false;
		
		pinned = true;
		
		paused = false;

		if (!iconsLoaded) {
			loadIcons();
		}
	}
	
	public void openPanel(DataPanelManager dataPanelManager) {
		this.dataPanelManager = dataPanelManager;
		this.dataPanelContainer = dataPanelManager.getDataPanelContainer();
		this.rbnbController = dataPanelManager.getRBNBController();
		
		component = new JPanel();
		controlBarBorder = new ControlBarBorder();
		component.setBorder(new EtchedBorder());		
		component.setLayout(new BorderLayout());
				
		dataComponent.setBorder(controlBarBorder);
		component.add(dataComponent, BorderLayout.CENTER);
		
		dataPanelContainer.addDataPanel(component);
		
		initControlBar();
		initDropTarget();

		rbnbController.addTimeListener(this);
		rbnbController.addStateListener(this);
		rbnbController.addTimeScaleListener(this);
	}

	public boolean setChannel(String channelName) {
		if (channels.size() == 1 && channels.contains(channelName)) {
			return true;
		}
		
		removeAllChannels();
		
		return addChannel(channelName);
	}
	
	public boolean addChannel(String channelName) {
		Channel channel = rbnbController.getChannel(channelName);
				
		if (channels.contains(channelName)) {
			return false;
		}
		
		rbnbController.subscribe(channelName, this);

		channels.add(channelName);
		
		String unit = channel.getUnit();
		if (unit != null) {
			units.put(channelName, unit);
		}
		
		if (!attached) {
			frame.setTitle(getTitle());
		} else if (pinned) {
			component.repaint();
		}
		
		return true;
	}
	
	public boolean removeChannel(String channelName) {		
		if (!channels.contains(channelName)) {
			return false;
		}
		
		channels.remove(channelName);
		units.remove(channelName);
		
		rbnbController.unsubscribe(channelName, this);

		if (!attached) {
			frame.setTitle(getTitle());
		} else if (pinned) {
			component.repaint();
		}
		
		return true;
	}
	
	void removeAllChannels() {
		Object[] channelNames = channels.toArray();
		for (int i=0; i<channelNames.length; i++) {
			removeChannel((String)channelNames[i]);
		}
	}
		
	void setDataComponent(JComponent dataComponent) {
		this.dataComponent = dataComponent;
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
	
	public void postData(ChannelMap channelMap) {
		this.channelMap = channelMap;
	}
	
	public void postTime(double time) {
		this.time = time;
	}
	
	public void timeScaleChanged(double timeScale) {
		this.timeScale = timeScale;
	}
	
	public void postState(int newState, int oldState) {
		switch (newState) {
			case Player.STATE_LOADING:
			case Player.STATE_MONITORING:
			case Player.STATE_REALTIME:
				clearData();
				break;
		}
	}
	
	void loadIcons() {
		ClassLoader cl = getClass().getClassLoader();
		
		windowPinImage = new ImageIcon(cl.getResource(windowPinFileName)).getImage();
		windowSnapshotImage = new ImageIcon(cl.getResource(windowSnapshotFileName)).getImage();
		windowDetachImage = new ImageIcon(cl.getResource(windowDetachFileName)).getImage();
		windowMaximizeImage = new ImageIcon(cl.getResource(windowMaximizeFileName)).getImage();
		windowCloseImage = new ImageIcon(cl.getResource(windowCloseFileName)).getImage();
		
		iconsLoaded = true;
	}

	/**
	 * 
	 *
	 * @since  1.2
	 */
	void initControlBar() {
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

	void togglePause() {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			
			if (paused) {
				rbnbController.subscribe(channelName, this);
			} else {
				rbnbController.unsubscribe(channelName, this);
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
			dataPanelContainer.removeDataPanel(component);
 		}
 		
 		rbnbController.removeStateListener(this);
 		rbnbController.removeTimeListener(this);
 		rbnbController.removeTimeScaleListener(this);
	}
	
	void toggleDetach() {
		if (maximized) {
			restorePanel(false);
		}
		
		if (attached) {
			detachPanel();
		} else {
			attachPanel(true);
		}
	}
	
	void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(component);
		
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
	
	void attachPanel(boolean addToContainer) {
		if (frame != null) {
			frame.setVisible(false);
			frame.getContentPane().remove(component);
			frame.dispose();
			frame = null;
		}
		
		if (addToContainer) {
			attached = true;
			dataPanelContainer.addDataPanel(component);
		}
	}
	
	void toggleMaximize() {	
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
	
	void maximizePanel() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == null) {			
				maximized = true;
				dataPanelContainer.removeDataPanel(component);

				frame = new JFrame(getTitle());
				frame.setUndecorated(true);				
				frame.getContentPane().add(component);
				
				try {
					device.setFullScreenWindow(frame);
				} catch (InternalError e) {
					log.error("Failed to switch to full screen mode: " + e.getMessage() + ".");
					restorePanel(true);
					return;
				}
				
				frame.setVisible(true);
				frame.requestFocus();
								
				return;
			}
		}
		
		log.warn("No screens available or full screen exclusive mode is unsupported on your platform.");
	}
	
	void restorePanel(boolean addToContainer) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == frame) {
				if (frame != null) {
					frame.setVisible(false);
					device.setFullScreenWindow(null);
					frame.getContentPane().remove(component);
					frame.dispose();
					frame = null;
				}
		
				maximized = false;
				
				if (addToContainer) {
					dataPanelContainer.addDataPanel(component);
				}
				
				break;
			}
		}
	}	
	
	/**
	 * 
	 *
	 * @since  1.2
	 */
	void initDropTarget() {
		new DropTarget(component, DnDConstants.ACTION_LINK, this);
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