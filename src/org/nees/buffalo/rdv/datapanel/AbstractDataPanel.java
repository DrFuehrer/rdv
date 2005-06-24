/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

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
import org.nees.buffalo.rdv.ui.ToolBarButton;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;

/**
 * A default implementation of the DataPanel interface. This class manages
 * add and remove channel requests, and handles subscription to the
 * RBNBController for time, data, state, and posting. It also provides a toolbar
 * placed at the top of the UI component to enable the detach and fullscreen
 * features along with a close button.
 * <p>
 * Data panels extending this class must have a no argument constructor and call
 * setDataComponent with their UI component in this constructor. 
 * 
 * @since   1.1
 * @author  Jason P. Hanley
 */
public abstract class AbstractDataPanel implements DataPanel, DataListener, TimeListener, TimeScaleListener, StateListener, DropTargetListener {

	/**
	 * The logger for this class.
	 * 
	 * @since  1.1
	 */
	static Log log = LogFactory.getLog(AbstractDataPanel.class.getName());
	
	/**
	 * The data panel manager for callbacks to the main application.
	 * 
	 * @since  1.2
	 */
	DataPanelManager dataPanelManager;
	
	/**
	 * The data panel container for docking in the UI.
	 * 
	 * @since  1.2
	 */
	DataPanelContainer dataPanelContainer;
	
	/**
	 * The RBNB controller for receiving data.
	 * 
	 * @since 1.2
	 */
	RBNBController rbnbController;
	
	/**
	 * A list of subscribed channels.
	 * 
	 * @since  1.1
	 */
	HashSet channels;
	
	/**
	 * A list of units for channels.
	 * 
	 * @since  1.1
	 */
	Hashtable units;
	
	/**
	 * The last posted time.
	 * 
	 * @since  1.1
	 */
	double time;
	
	/**
	 * The last posted time scale.
	 * 
	 * @since  1.1
	 */
	double timeScale;

	/**
	 * The UI component with toolbar.
	 * 
	 * @since  1.1
	 */
	JPanel component;
	
	/**
	 * The subclass UI component.
	 * 
	 * @since  1.1
	 */
	JComponent dataComponent;
		
	/**
	 * The frame used when the UI component is detached or full screen.
	 * 
	 * @since  1.1
	 */
	JFrame frame;
	
	/**
	 * Indicating if the UI component is docked.
	 * 
	 * @since  1.1
	 */
	boolean attached;
	
	/**
	 * Indicating if the UI component is in fullscreen mode.
	 * 
	 * @since  1.1
	 */
	boolean maximized;
	
 	static String detachIconFileName = "icons/detach.gif";
 	static String closeIconFileName = "icons/close.gif";
 	
 	/**
 	 * Indicating if the data panel has been paused by the snaphsot button in the
 	 * toolbar.
 	 * 
 	 * @since  1.1
 	 */
 	boolean paused;

 	/**
 	 * A data channel map from the last post of data.
 	 * 
 	 * @since  1.1
 	 */
	ChannelMap channelMap;

	/**
	 * Initialize the list of channels and units. Set parameters to defaults.
	 * 
	 * @since  1.1
	 */
	public AbstractDataPanel() {		
		channels = new HashSet();
		units = new Hashtable();
		
		time = 0;
		timeScale = 1;
		
		attached = true;
		
		maximized = false;

		paused = false;
	}
	
	public void openPanel(final DataPanelManager dataPanelManager) {
		this.dataPanelManager = dataPanelManager;
		this.dataPanelContainer = dataPanelManager.getDataPanelContainer();
		this.rbnbController = dataPanelManager.getRBNBController();    
        
    component = new SimpleInternalFrame("", createToolBar(), dataComponent);
		
		dataPanelContainer.addDataPanel(component);
		
		initDropTarget();

		rbnbController.addTimeListener(this);
		rbnbController.addStateListener(this);
		rbnbController.addTimeScaleListener(this);
	}
  
  private JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    
    final DataPanel dataPanel = this;
    
    JButton button = new ToolBarButton(detachIconFileName, "Detach data panel");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        toggleDetach();
      }
    });
    toolBar.add(button);
    button = new ToolBarButton(closeIconFileName, "Close data panel");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        dataPanelManager.closeDataPanel(dataPanel);
      }
    });    
    toolBar.add(button);    

    return toolBar;
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
        
    ((SimpleInternalFrame)component).setTitle(getTitle());
		
		String unit = channel.getUnit();
		if (unit != null) {
			units.put(channelName, unit);
		}
		
		if (!attached) {
			frame.setTitle(getTitle());
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
		}
		
		return true;
	}
	
	/**
	 * Calls removeChannel for each subscribed channel.
	 * 
	 * @see    removeChannel(String)
	 * @since  1.1
	 *
	 */
	void removeAllChannels() {
		Object[] channelNames = channels.toArray();
		for (int i=0; i<channelNames.length; i++) {
			removeChannel((String)channelNames[i]);
		}
	}
		
	/**
	 * Set the UI component to be used for displaying data. This method must be 
	 * called from the constructor of the subclass.
	 * 
	 * @param dataComponent  the UI component
	 * @since                1.1
	 */
	void setDataComponent(JComponent dataComponent) {
		this.dataComponent = dataComponent;
	}
	
	/*
	 * Clear the data displayed on the data panel. This is called when the
	 * RBNBController is loading data from a new time period or when starting to
	 * view data in realtime.
	 * 
	 * @since  1.1
	 */
	abstract void clearData();
	
	/*
	 * Get the title of the data panel. Override this method if you want something
	 * different than a comma separated list of subscribed channel names. This is
	 * used in the UI for the title of the data panel.
	 * 
	 * @since  1.1
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
    if (newState == Player.STATE_LOADING || (newState == Player.STATE_MONITORING && oldState != Player.STATE_MONITORING)) {
      clearData();  
    }
	}
	
	/**
	 * Toggle pausing of the data panel. Pausing is freezing the data display and
	 * stopping listeners for data. When a channel is unpaused it will again
	 * subscribe to data for the subscribed channels.
	 *
	 * @since  1.1
	 */
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
	
	/**
	 * Toggle detaching the UI component from the data panel container.
	 * 
	 * @since  1.1
	 */
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
	
	/**
	 * Detach the UI component from the data panel container.
	 * 
	 * @since  1.1
	 */
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
	
	/** Dispose of the frame for the UI component. Dock the UI component if
	 * addToContainer is true.
	 * 
	 * @param addToContainer  whether to dock the UI component
	 * @since                 1.1
	 */
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
	
	/**
	 * Toggle maximizing the data panel UI component to fullscreen.
	 * 
	 * @since  1.1
	 */
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

	/**
	 * Undock the UI component and display fullscreen.
	 * 
	 * @since  1.1
	 */
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
	
	/**
	 * Leave fullscreen mode and dock the UI component if addToContainer is true.
	 * 
	 * @param addToContainer  whether to dock the UI component
	 * @since                 1.1
	 */
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
	 * Setup the drop target for channel subscription via drag-and-drop.
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

				boolean status;
				if (supportsMultipleChannels()) {
					status = addChannel(channelName);
				} else {
					status = setChannel(channelName);
				}
				if (!status) {
					//TODO display an error in the UI
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