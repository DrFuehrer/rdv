/*
 * Created on Feb 4, 2005
 */
package org.nees.buffalo.rdv;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class ChannelManager {
	
	static Log log = LogFactory.getLog(ChannelManager.class.getName());
	
	private HashMap listenerChannelSubscriptions;
	private HashMap channelSubscriptionCounts;
	private ArrayList playerChannelListeners;

	public ChannelManager() {
		listenerChannelSubscriptions = new HashMap();
		channelSubscriptionCounts = new HashMap();
		playerChannelListeners = new ArrayList();
	}

	public boolean subscribe(String channelName, PlayerChannelListener listener) {
		//add channel to channel list for listener
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		if (listenerChannelSubscription == null) {
			listenerChannelSubscription = new ArrayList();
			listenerChannelSubscriptions.put(listener, listenerChannelSubscription);
		}
		listenerChannelSubscription.add(channelName);
		
		//increment the count for this channel
		Integer count = (Integer)channelSubscriptionCounts.get(channelName);
		if (count == null) {
			count = new Integer(1);
		} else {
			count = new Integer(count.intValue()+1);
		}
		channelSubscriptionCounts.put(channelName, count);

		//add the channel listener to the list
		if (!playerChannelListeners.contains(listener)) {
			playerChannelListeners.add(listener);
		}
		
		return true;
	}
	
	public boolean unsubscribe(String channelName, PlayerChannelListener listener) {
		//remove channel from channel list for listener
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		listenerChannelSubscription.remove(channelName);
		if (listenerChannelSubscription.size() == 0) {
			listenerChannelSubscriptions.remove(listener);
		}
		
		//decrement the count for this channel
		int count = ((Integer)channelSubscriptionCounts.get(channelName)).intValue();
		if (count == 1) {
			channelSubscriptionCounts.remove(channelName);
		} else {
			channelSubscriptionCounts.put(channelName, new Integer(--count));
		}
		
		//remove the channel listener from the list
		if (!isListenerSubscribedToAnyChannels(listener)) {
			playerChannelListeners.remove(listener);
		}

		return true;
	}
	
	private boolean isListenerSubscribedToAnyChannels(PlayerChannelListener listener) {
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		return listenerChannelSubscription != null;
	}
		
	public boolean isChannelSubscribed(String channelName) {
		Integer count = (Integer)channelSubscriptionCounts.get(channelName);
		return count != null;
	}
	
	public boolean isListenerSubscribedToChannel(String channelName, PlayerChannelListener listener) {
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		if (listenerChannelSubscription != null) {
			return listenerChannelSubscription.contains(channelName);
		} else {
			return false;
		}
	}
	
	public void postData(ChannelMap channelMap) {
		PlayerChannelListener listener;
		for (int i=0; i < playerChannelListeners.size(); i++) {
			listener = (PlayerChannelListener)playerChannelListeners.get(i);
			try {
				listener.postData(channelMap);
			} catch (Exception e) {
				log.error("Failed to post time to " + listener + ".");
				e.printStackTrace();
			}
		}
	}
}
