package org.nees.buffalo.rdv.rbnb;

import com.rbnb.sapi.ChannelMap;

/**
 * A listener interface to receive the channel list and the associated metadata.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface ChannelListListener {
	
	/**
	 * Invoked when the channel list is updated. This will contain
	 * an up-to-date list of channels and their associated metadata.
	 * This information is encapsulated in the <code>ChannelMap</code>.
	 * 
	 * @param channelMap  The channel map
	 * @since           1.0
	 */
	public void channelListUpdated(ChannelMap channelMap);
}
