/*
 * Created on Mar 31, 2005
 */
package org.nees.buffalo.rdv;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.datapanel.JFreeChartDataPanel;
import org.nees.buffalo.rdv.datapanel.JPEGDataPanel;
import org.nees.buffalo.rdv.datapanel.StringDataPanel;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.DataPanelContainer;

/**
 * A class to manage all the data panels.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class DataPanelManager {
	
	/**
	 * The logger for this class.
	 * 
	 * @since  1.2
	 */
	static Log log = LogFactory.getLog(DataPanelManager.class.getName());
	
	/**
	 * A reference to the RNBN controller for the data panels to use.
	 * 
	 * @since  1.2
	 */
	private RBNBController rbnbController;
	
	/**
	 * A reference to the data panel container for the data panels
	 * to add their ui component too.
	 * 
	 * @since  1.2
	 */
	private DataPanelContainer dataPanelContainer;

	/** 
	 * The constructor for the data panel manager. This initializes the
	 * data panel container and the list of registered data panels.
	 * 
	 * 
	 * @param rbnbController  the rbnb controller
	 * @since                 1.2
	 */
	public DataPanelManager(RBNBController rbnbController) {
		this.rbnbController = rbnbController;
		dataPanelContainer = new DataPanelContainer();
		
		dataPanels = new ArrayList();
	}
	
	/**
	 * Returns the RBNB controller.
	 * 
	 * @return  the rbnb controller
	 * @since   1.2
	 */
	public RBNBController getRBNBController() {
		return rbnbController;
	}
	
	/**
	 * Returns the data panel container where data panels can
	 * add their ui components too.
	 * 
	 * @return  the data panel container
	 * @since   1.2
	 */
	public DataPanelContainer getDataPanelContainer() {
		return dataPanelContainer;
	}
	
	/**
	 * A list of all the data panels.
	 * 
	 * @since  1.2
	 */
	private ArrayList dataPanels;
	
	/**
	 * Register the data panel with the manager.
	 * 
	 * @param dataPanel  the data panel to be registered
	 * @since            1.2
	 */
	public void addDataPanel(DataPanel dataPanel) {
		dataPanels.add(dataPanel);
	}
	
	/**
	 * Unregister the data panel from the manager.
	 * 
	 * @param dataPanel  the data panel to be unregistered
	 * @since            1.2
	 */
	public void removeDataPanel(DataPanel dataPanel) {
		dataPanels.remove(dataPanel);
	}
	
	/**
	 * Calls the closePanel method on the specified data panel.
	 * 
	 * After this method has been called this data panel instance has
	 * reached the end of its life cycle.
	 * 
	 * @param dataPanel  the data panel to be closed
	 * @see              DataPanel#closePanel()
	 * @since            1.2
	 */
	public void closeDataPanel(DataPanel dataPanel) {
		dataPanel.closePanel();
		dataPanels.remove(dataPanel);
	}

	
	/**
	 * Calls the <code>closePanel</code> method on each registered data panel.
	 * 
	 * @see    DataPanel#closePanel()
	 * @since  1.2
	 */
	public void closeAllDataPanels() {
		DataPanel dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel)dataPanels.get(i);
			dataPanel.closePanel();
		}
		dataPanels.clear();
	}
	
	public boolean viewChannel(String channelName) {
		Channel channel = rbnbController.getChannel(channelName);
		if (channel != null) {
			viewChannel(channel);
			return true;
		} else {
			return false;
		}
	}
		
	public void viewChannel(Channel channel) {
		String channelName = channel.getName();
		String mime = channel.getMimeType();
		
		log.info("Cretaing data panel for channel " + channelName + ".");
		
		DataPanel panel = null;
		if (mime == null) {
			log.warn("Unknown data type for channel " + channelName + ".");
			if (channelName.endsWith(".jpg")) {
				panel = new JPEGDataPanel();
			} else {
				panel = new JFreeChartDataPanel();
			}
		} else if (mime.equals("image/jpeg")) {		
			panel = new JPEGDataPanel();
		} else if (mime.equals("application/octet-stream")) {		
			panel = new JFreeChartDataPanel();
		} else  if (mime.equals("text/plain")) {
			panel = new StringDataPanel();
		} else {
			log.error("Unsupported data type for channel " + channelName + ".");
			return;
		}
		
		panel.openPanel(this);
		dataPanels.add(panel);
		
		try {
			panel.setChannel(channel);	
		} catch (Exception e) {
			log.error("Failed to add chanel to data panel.");
			e.printStackTrace();
			return;
		}
	}		
}