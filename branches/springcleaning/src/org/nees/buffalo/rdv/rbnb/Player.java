package org.nees.buffalo.rdv.rbnb;

/**
 * An interface to define a player which is capable of playback data from the 
 * channels in different states and at different rates.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface Player {
	/**
	 * The player is stopped and producing no data.
	 * 
	 * @since  1.0
	 */
	public static final int STATE_STOPPED = 0;
	
	/**
	 * The player is monitoring data as in comes
	 * into the server.
	 * 
	 * @since  1.0
	 */
	public static final int STATE_MONITORING = 1;
	
	/**
	 * The player is loading data from the server.
	 * 
	 * @since  1.0
	 */
	public static final int STATE_LOADING = 2;
	
	/**
	 * The player is playing back data starting from
	 * the current location
	 * 
	 * @since  1.0
	 */
	public static final int STATE_PLAYING = 3;
	
	/**
	 * The player is exiting.
	 * 
	 * @since  1.0
	 */
	public static final int STATE_EXITING = 4;
	
	/**
	 * The player is connect to the server.
	 * 
	 * @since  1.0
	 */
	public static final int STATE_DISCONNECTED = 5;
	
	/**
	 * The player is viewing the newest data.
	 * 
	 * @since  1.1
	 */
	public static final int STATE_REALTIME = 6;

	/**
	 * Put the player in monitor mode. Get the newest data that is
	 * comming into the server.
	 *
	 * @see    #STATE_MONITORING
	 * @since  1.0
	 */
	public void monitor();
	
	/** 
	 * Put the player in playback mode. Start data playback data from
	 * the specified location and at the specified rate. 
	 * 
	 * @see    #STATE_PLAYING
	 * @since  1.0
	 */
	public void play();
	
	/**
	 * Put the player in stopped mode. Stop any playback of data.
	 *
	 * @see    #STATE_STOPPED
	 * @since  1.0
	 */
	public void pause();
	
	/**
	 * Stop the play and return any resources. The player is no
	 * longer usable after this method has been called.
	 * 
	 * @see    #STATE_EXITING
	 * @since  1.0
	 */
	public void exit();
	
	/**
	 * Get the current location of the player
	 * 
	 * @return  the current location in seconds
	 * @since   1.0
	 */
	public double getLocation();
	
	/**
	 * Set the location of the player.
	 * 
	 * @param location  the location to set the player at
	 * @since           1.0
	 */
	public void setLocation(final double location);
	
	
	/**
	 * Get the time scale that player is using. This is the rate at
	 * which data is played back.
	 * 
	 * @return  the current time scale
	 * @since   1.0
	 */
	public double getTimeScale();
	
	/**
	 * Set the time scale that the player uses for data playback.
	 * This is the rate at which data is played back.
	 * 
	 * @param timeScale  the time scale to set
	 * @since            1.0
	 */
	public void setTimeScale(final double timeScale);
	
	
	/**
	 * Subscribe to the channel and produce data for it.
	 * 
	 * @param channelName      the name of the channel
	 * @param channelListener  the channel listener to post data to
	 * @return                 true if the channel is subscribed, false otherwise
	 * @since                  1.0
	 */
	public boolean subscribe(String channelName, PlayerChannelListener channelListener);
	
	/**
	 * Unsubscribe the channel listener from the channel and stop
	 * posting data to the listener for this channel. If no more
	 * listener are registered for this channel, stop loading data
	 * from the server for this channel.
	 * 
	 * @param channelName      the channel to unsubscribe from
	 * @param channelListener  the channel listenr for this channel
	 * @return                 true if the listener is unsubscribed, false otherwise
	 * @since                  1.0
	 */
	public boolean unsubscribe(String channelName, PlayerChannelListener channelListener);
	
	/**
	 * Tell if the player is getting data from the server for this
	 * channel.
	 * 
	 * @param channelName  the name of the channel to check
	 * @return             true if the channel is subscribed, false otherwise
	 * @since              1.0
	 */
	public boolean isSubscribed(String channelName);
	
	/**
	 * Adds the listener for posting of changes to the player state
	 * 
	 * @param stateListener  the listener to post state changes too
	 * @since                1.0
	 */
	public void addStateListener(PlayerStateListener stateListener);
	
	/**
	 * Stop posting state changes to the specified listener.
	 * 
	 * @param stateListener  the listener to stop posting state changes too
	 * @since                1.0
	 */
	public void removeStateListener(PlayerStateListener stateListener);
	
	
	/**
	 * Adds the listener for posting of the current player time.
	 * 
	 * @param timeListener  the listener to post the time too
	 * @since               1.0
	 */
	public void addTimeListener(PlayerTimeListener timeListener);
	
	/**
	 * Stop posting the current time to the specified listener.
	 * 
	 * @param timeListener  the listener to stop posting the time too
	 * @since               1.0
	 */
	public void removeTimeListener(PlayerTimeListener timeListener);
}
