package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public interface PlayerSubscriptionListener {
	public void channelSubscribed(String channelName);
	public void channelUnsubscribed(String channelName);
}
