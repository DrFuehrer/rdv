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
	public static final int STATE_REALTIME = 6;

	public void monitor();
	public void play();
	public void pause();
	public void exit();
	
	public double getLocation();
	public void setLocation(final double location);
	
	public double getTimeScale();
	public void setTimeScale(final double timeScale);
	
	public boolean subscribe(String channelName, PlayerChannelListener channelListener);
	
	public boolean unsubscribe(String channelName, PlayerChannelListener channelListener);
	
	public boolean isSubscribed(String channelName);
	
	public void addStateListener(PlayerStateListener stateListener);
	public void removeStateListener(PlayerStateListener stateListener);
	
	public void addTimeListener(PlayerTimeListener timeListener);
	public void removeTimeListener(PlayerTimeListener timeListener);
}
