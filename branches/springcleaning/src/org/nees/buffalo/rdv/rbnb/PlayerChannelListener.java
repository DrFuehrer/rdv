package org.nees.buffalo.rdv.rbnb;

import com.rbnb.sapi.ChannelMap;

/**
 * A listener interface to post data from the player.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface PlayerChannelListener {
	/**
	 * Post a slice of data.
	 * 
	 * @param channelMap  the channel map containing the data
	 * @since             1.1
	 */
	public void postData(ChannelMap channelMap);
}
