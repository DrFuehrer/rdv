package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public interface Player {
	public static final int STATE_STOPPED = 0;
	public static final int STATE_MONITORING = 1;
	public static final int STATE_LOADING = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_EXITING = 4;
	public static final int STATE_DISCONNECTED = 5;

	public void monitor();
	public void play();
	public void pause();
	public void exit();
	
	public int getState();
	
	public void setLocation(final double location);
	public double getLocation();
	public void setTimeScale(final double timeScale);
	public double getTimeScale();
	
	public String[] getAvailableChannels();
	
	public String[] getSubscribedChannels(PlayerChannelListener channelListener);
	
	public boolean subscribe(String channelName, PlayerChannelListener channelListener);
	public boolean subscribeAll(PlayerChannelListener channelListener);
	
	public boolean unsubscribe(String channelName, PlayerChannelListener channelListener);
	public boolean unsubscribeAll(PlayerChannelListener channelListener);
	
	public boolean unsubscribeAndSubscribe(String unsubscribeChannelName, String subscribeChannelName, PlayerChannelListener channelListener);
	
	public boolean isSubscribed(String channelName);
	
	public void addStateListener(PlayerStateListener stateListener);
	public void removeStateListener(PlayerStateListener stateListener);
	
	public void addTimeListener(PlayerTimeListener timeListener);
	public void removeTimeListener(PlayerTimeListener timeListener);
}
