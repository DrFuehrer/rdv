package org.nees.buffalo.rdv.datapanel;

import javax.swing.JComponent;

import org.nees.buffalo.rdv.Channel;

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
public interface DataPanel2 {
	/**
	 * Get the UI component that will display the data
	 * 
	 * @return  the UI component
	 * @since   1.0
	 */
	public JComponent getComponent();
	
	/**
	 * Get the MIME types that this data panel supports.
	 * 
	 * A data panel should not assume that it will only be asked
	 * to display this type of data, and should handle this situation
	 * accordingly.
	 * 
	 * @return  an array of supported MIME types
	 * @since   1.0
	 */
	public String[] getSupportedMimeTypes();
	
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
	 * @param channel  The channel to be displayed by the data panel
	 * @return         true if the data panel can display the channel, false otherwise
	 * @since          1.0
	 */
	public boolean setChannel(Channel channel);
	
	/**
	 * Tell the data panel to view this channel. If other channels
	 * are currently being viewed, this channel is also viewed with
	 * them, unless the data panel support only one channel.
	 * 
	 * If the data panel can not view the channel for any reason,
	 * it should return false. 
	 * 
	 * @param channel  The channel to be displayed by the data panel
	 * @return         true if the data panel can display the channel, false otherwise
	 * @see            #supportsMultipleChannels
	 * @since          1.0
	 */
	public boolean addChannel(Channel channel);
	
	/**
	 * Remove the channel from the data panel.
	 * 
	 * @param channelName  The channel to be removed from the data panel display
	 * @return             true if the channel is removed, false otherwise
	 * @since              1.0
	 */
	public boolean removeChannel(String channelName);
	
	/**
	 * Tell the data panel the length of time the user has requested
	 * the application to display.
	 * 
	 * The data panel does not have to use this value, and only needs
	 * to take it into consideration. 
	 * 
	 * @param domain  The length of time to display (in seconds)
	 * @since         1.0
	 */
	public void setDomain(double domain);
	
	/**
	 * Close the data panel and release all associated resources.
	 * 
	 * @since  1.0
	 */
	public void closePanel();
}
