/*
 * Created on Mar 28, 2005
 */
package org.nees.buffalo.rdv;

import com.rbnb.sapi.ChannelMap;

/**
 * RBNBUtilities is a utility class to provide static methods for dealing with RBNB.
 * <p>
 * Methods are included for dealing with times, channels, and channel maps.
 * 
 * @author   Jason P. Hanley
 * @since    1.2
 */
public final class RBNBUtilities {
	
	/**
	 * This class can not be instantiated and it's constructor
	 * always throws an exception.
	 */
	private RBNBUtilities() {
		throw new UnsupportedOperationException("This class can not be instantiated.");
	}
	
	/**
	 * Using the given channel map, finds the start time for the specified channel.
	 * If the channel is not found, -1 is returned.
	 * 
	 * @param channelMap   the <code>ChannelMap</code> containing the times
	 * @param channelName  the name of the channel
	 * @return             the start time for the channel
	 * @since              1.2
	 */
	public static double getStartTime(ChannelMap channelMap, String channelName) {
		int channelIndex = channelMap.GetIndex(channelName);
		if (channelIndex != -1) {
			double start = channelMap.GetTimeStart(channelIndex);
			return start;
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the start time for the given channel map. If the channel map
	 * is empty, -1 is returned.
	 * 
	 * @param channelMap  the <code>ChannelMap</code> containing the times
	 * @return            the start time for all the channels
	 * @see               #getStartTime(ChannelMap, String)
	 * @since             1.2
	 */
	public static double getStartTime(ChannelMap channelMap) {
		double start = Double.MAX_VALUE;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			double channelStart = getStartTime(channelMap, channelName);
			if (channelStart != -1) {
				start = Math.min(channelStart, start);
			}
		}
		
		if (start != Double.MAX_VALUE) {
			return start;
		} else {
			return -1;
		}
	}
	
	/**
	 * Using the given channel map, finds the end time for the specified channel.
	 * If the channel is not found, -1 is returned.
	 * 
	 * @param channelMap   the <code>ChannelMap</code> containing the times
	 * @param channelName  the name of the channel
	 * @return             the end time for the channel
	 * @since              1.2
	 */
	public static double getEndTime(ChannelMap channelMap, String channelName) {
		int channelIndex = channelMap.GetIndex(channelName);
		if (channelIndex != -1) {
			double start = channelMap.GetTimeStart(channelIndex);
			double duration = channelMap.GetTimeDuration(channelIndex);
			double end = start+duration;
			return end;
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the end time for the given channel map. If the channel map
	 * is empty, -1 is returned.
	 * 
	 * @param channelMap  the <code>ChannelMap</code> containing the times
	 * @return            the end time for all the channels
	 * @see               #getEndTime(ChannelMap, String)
	 * @since             1.2
	 */
	public static double getEndTime(ChannelMap channelMap) {
		double end = -1;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			double channelEnd = getEndTime(channelMap, channelName);
			if (channelEnd != -1) {
				end = Math.max(channelEnd, end);
			}
		}
		
		return end;
	}
}
