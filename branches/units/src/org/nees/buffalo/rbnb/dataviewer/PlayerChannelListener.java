package org.nees.buffalo.rbnb.dataviewer;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public interface PlayerChannelListener {
	public void postData(ChannelMap channelMap);
	public void postData(ChannelMap channelMap, double location, double duration);
}
