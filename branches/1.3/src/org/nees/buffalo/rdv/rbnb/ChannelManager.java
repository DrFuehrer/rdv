/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/rbnb/ChannelManager.java $
 * $Revision: 316 $
 * $Date: 2005-10-19 20:11:00 -0700 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.rbnb;

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

	public boolean subscribe(String channelName, DataListener listener) {
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
	
	public boolean unsubscribe(String channelName, DataListener listener) {
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
	
	private boolean isListenerSubscribedToAnyChannels(DataListener listener) {
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		return listenerChannelSubscription != null;
	}
		
	public boolean isChannelSubscribed(String channelName) {
		Integer count = (Integer)channelSubscriptionCounts.get(channelName);
		return count != null;
	}
	
	public boolean isListenerSubscribedToChannel(String channelName, DataListener listener) {
		ArrayList listenerChannelSubscription = (ArrayList)listenerChannelSubscriptions.get(listener);
		if (listenerChannelSubscription != null) {
			return listenerChannelSubscription.contains(channelName);
		} else {
			return false;
		}
	}
	
	public void postData(ChannelMap channelMap) {
		DataListener listener;
		for (int i=0; i < playerChannelListeners.size(); i++) {
			listener = (DataListener)playerChannelListeners.get(i);
			try {
				listener.postData(channelMap);
			} catch (Exception e) {
				log.error("Failed to post time to " + listener + ".");
				e.printStackTrace();
			}
		}
	}
}
