/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv.datapanel;

import java.util.Collection;
import java.util.Properties;

import org.rdv.DataPanelManager;

/**
 * This interface defines a data panel which is a component for getting
 * timestamped data from subscribed channels and displaying it in an optional UI
 * component. Implmentations of this interface are refered to as data panel
 * extensions.
 * <p>
 * The main application opens data panels and adds or removes channels to/from
 * them to control the data displayed. Client must not call the openPanel or
 * closePanel method as they are used by the application to manage the lifecycle
 * of this object.
 * <p>
 * The implementations of this interface must have a no argument constructor
 * that can be called to instantiate the data panel. Immediately after this
 * data panel has been instantiated, the openPanel method will be called. This
 * provides a reference to the DataPanelManager which is the callback to the
 * main application. The object is ready to use after this and the setChannel,
 * addChannel, and removeChannel methods may be called. Once the closePanel
 * method has been called the object may no longer be used and has reached the
 * end of its lifecycle.
 * <p>
 * The DataPanelManager gives access to two important components. The
 * RBNController which provides access to the time and data. The
 * DataPanelContainer which provides a container to dock UI components.
 * <p>
 * To register the data panel add an entry for it in the config/extensions.xml
 * configuration file.
 * 
 * @author  Jason P. Hanley
 * @see     AbstractDataPanel
 * @since   1.0
 */
public interface DataPanel {
	/**
	 * Initializes the data panel with the given data panel manager.
	 * <p>
	 * This method is called shortly after the data panel has been instantiated by
	 * the data panel manager.
	 * <p>
	 * Clients must not call this method.
	 * 
	 * @param dataPanelManager  the data panel manager
	 * @since                   1.2
	 */
	public void openPanel(DataPanelManager dataPanelManager);
	
	/**
	 * Tell if the data panel can support viewing more than one channel at a time.
	 * <p>
	 * Depending on the type of data and the way it is presented, not
	 * all data panels support the notion of displaying more than
	 * one channel at a time. 
	 * 
	 * @return  true if the data panels support more than 1 channel, false
	 *          otherwise
	 * @since   1.0
	 */
	public boolean supportsMultipleChannels();
	
	/**
	 * Tell the data panel to view this channel. If any other channels are
	 * currently being viewed, they are removed.
	 * <p>
	 * If the data panel can not view the channel for any reason, it should return
	 * false. 
	 * 
	 * @param channelName  The name of the channel to be displayed by the data
	 *                     panel
	 * @return             true if the data panel can display the channel, false
	 *                     otherwise
	 * @since              1.0
	 */
	public boolean setChannel(String channelName);
	
	/**
	 * Tell the data panel to view this channel. If other channels are currently
	 * being viewed, this channel is viewed in addition to them. If the data
	 * panel supports only one channel calling this method is an incorrect usage.
	 * <p>
	 * If the data panel can not view the channel for any reason, it should return
	 * false. 
	 * 
	 * @param channelName  The name of the channel to be displayed by the data
	 *                     panel
	 * @return             true if the data panel can display the channel, false
	 *                     otherwise
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
	 * <p>
	 * Clients must not call this method. It is called by the DataPanelManager in
	 * response to a request to close the data panel.
	 * <p>
	 * The data panel has reached the end of its lifecycle after this method has
	 * been called.
	 * 
	 * @see    DataPanelManager#closeDataPanel(DataPanel)
	 * @since  1.0
	 */
	public void closePanel();
  
  /**
   * Returns the number of channels that this data panel is subscribed too.
   * 
   * @return  the number of subscribed channels
   * @since   1.3
   */
  public int subscribedChannelCount();
  
  /**
   * Returns a set of channel names that this data panel is subscribed too.
   * 
   * @return  a set of subscribed channels
   * @since   1.3
   */
  public Collection subscribedChannels();
  
  /**
   * Checks if this data panel is subscribed to the given channel.
   * 
   * @param channelName  the channel to check
   * @return             true if subscribed to the channel, false otherwise
   * @since              1.3
   */
  public boolean isChannelSubscribed(String channelName);
  
  /**
   * Get a list of properties associated with this data panel. These properties
   * describe the configuration specific to this data panel.
   * 
   * @return the properties for this data panel.
   */
  public Properties getProperties();
  
  /**
   * Set a property for this data panel.
   * 
   * @param key    the name of the property
   * @param value  the value of the property
   */
  public void setProperty(String key, String value);
}
