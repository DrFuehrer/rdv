package org.nees.buffalo.rdv.datapanel;

import org.nees.buffalo.rdv.DataPanelManager;

/**
 * This interface defines a data panel for displaying data from channels in a UI component.
 * 
 * The application opens data panels and adds or removes channels to/from them
 * to control the data displayed.
 * 
 * @author  Jason P. Hanley
 * @see     AbstractDataPanel
 * @since   1.0
 */
public interface DataPanel {
	/**
	 * Initializes the data panel with the given data panel manager.
	 * 
	 * This method is called shortly after the data panel has been
	 * instantiated by the data panel manager.
	 * 
	 * Clients must not call this method.
	 * 
	 * @param dataPanelManager  the data panel manager
	 * @since                   1.2
	 */
	public void openPanel(DataPanelManager dataPanelManager);
	
	/**
	 * Tell if the data panel can support viewing more than one
	 * at a time.
	 * 
	 * Depending on the type of data and the way it is presented, not
	 * all data panels support the notion of displaying more than
	 * one channel at a time. 
	 * 
	 * @return  true if the data panels support more than 1 channel, false otherwise
	 * @since   1.0
	 */
	public boolean supportsMultipleChannels();
	
	/**
	 * Tell the data panel to view this channel. If any other channels
	 * are currently being viewed, they are removed.
	 * 
	 * If the data panel can not view the channel for any reason,
	 * it should return false. 
	 * 
	 * @param channelName  The name of the channel to be displayed by the data panel
	 * @return             true if the data panel can display the channel, false otherwise
	 * @since              1.0
	 */
	public boolean setChannel(String channelName);
	
	/**
	 * Tell the data panel to view this channel. If other channels
	 * are currently being viewed, this channel is also viewed with
	 * them, unless the data panel support only one channel.
	 * 
	 * If the data panel can not view the channel for any reason,
	 * it should return false. 
	 * 
	 * @param channelName  The name of the channel to be displayed by the data panel
	 * @return             true if the data panel can display the channel, false otherwise
	 * @see                #supportsMultipleChannels
	 * @since              1.0
	 */
	public boolean addChannel(String channelName);
	
	/**
	 * Remove the channel from the data panel.
	 * 
	 * @param channelName  The channel to be removed from the data panel display
	 * @return             true if the channel is removed, false otherwise
	 * @since              1.0
	 */
	public boolean removeChannel(String channelName);
	
	/**
	 * Close the data panel and release all associated resources.
	 * 
	 * Clients must not call this method.
	 * 
	 * @see    DataPanelManager#closeDataPanel(DataPanel)
	 * @since  1.0
	 */
	public void closePanel();
}
