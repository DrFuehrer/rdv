package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to notify listeners when a channel has
 * been subscribed too or unsubscribed from in the player.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface SubscriptionListener {
	/**
	 * Called when the player subscribes to the specified channel.
	 * 
	 * @param channelName  the channel subscribed too
	 * @since              1.0
	 */
	public void channelSubscribed(String channelName);
	
	/**
	 * Called when the player unsubscribes from the specified channel.
	 * 
	 * @param channelName  the channel unscubscribed from
	 * @since              1.0
	 */
	public void channelUnsubscribed(String channelName);
}
