package org.nees.buffalo.rbnb.dataviewer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * @author Jason P. Hanley
 */
public class RBNBController implements Player, TimeScaleListener, DomainListener, ChannelListListener {

	static Log log = LogFactory.getLog(RBNBController.class.getName());

	private final String rbnbSinkName = "RBNBDataViewer";

 	private int state = Player.STATE_DISCONNECTED;
 	
 	private Sink sink = null;
	
	private boolean requestIsMonitor = false;	
	
	private ChannelMap requestedChannels;
	
	private ChannelManager channelManager;
	
	private Vector timeListeners;
	private Vector stateListeners;
	private Vector subscriptionListeners;
	
	private ChannelMap preFetchChannelMap;
	private Object preFetchLock = new Object();
	private boolean preFetchDone;
	
	String[] availableChannels;
	
	private double location = System.currentTimeMillis()/1000d;
	private double timeScale = 1;
	private double domain = 1;
	
	private double updateLocation = -1;
	private double updateTimeScale = -1;
	private int updateState = -1;

 	private boolean dropData = false;
	
	static final double PLAYBACK_REFRESH_RATE = 0.05;
	
	public RBNBController() {
		channelManager = new ChannelManager(); 
		
		timeListeners = new Vector();
		stateListeners = new Vector();
		subscriptionListeners = new Vector();
		
		availableChannels = new String[0];
		
		run();
	}
	
	private void run() {
		new Thread(new Runnable() {
			public void run() {
				runRBNB();
			}
		}, "RBNB").start();
	}
	
	public void monitor() {
		changeState(STATE_MONITORING);
	}
	
	public void play() {
		changeState(STATE_PLAYING);
	}
	
	public void pause() {
		changeState(STATE_STOPPED);
	}
	
	public void exit() {
		changeState(STATE_EXITING);
 		int count = 0;
 		while (sink != null && count++ < 20) {
 			try { Thread.sleep(50); } catch (Exception e) {}
 		}
	}
	
	private void changeState(int newState) {
		updateState = newState;
	}
	
	private synchronized boolean changeStateSafe(int newState) {
		int oldState = state;
			
		if (oldState == STATE_EXITING) {
			log.error("Can not transition out of exiting state to " + getStateName(state) + " state.");
			return false;
 		} else if (oldState == STATE_DISCONNECTED && !(newState == STATE_EXITING || newState == STATE_DISCONNECTED)) {
			if (!initRBNB()) {
				return false;
			}
		}
		
		switch (newState) {
			case STATE_MONITORING:		    
				state = newState;
				monitorData();
				break;
			case STATE_LOADING:
				state = newState;
				break;
			case STATE_PLAYING:
				if (oldState == STATE_MONITORING || oldState == STATE_STOPPED) {
					preFetchData(location, timeScale);
				}
				
				state = newState;
				break;
			case STATE_STOPPED:
				state = newState;
				break;
			case STATE_EXITING:
				state = newState;
				break;
			case STATE_DISCONNECTED:
				state = newState;
				break;
		}
		
		notifyStateListeners(state, oldState);

		log.debug("Transitioned from state " + getStateName(oldState) + " to " + getStateName(state) + ".");
		
		return true;
	}

	public void setLocation(final double location) {
		updateLocation = location;
	}
	
	private synchronized void setLocationSafe(double location) {
		log.debug("Setting location to " + DataViewer.formatDate(location) + ".");
	
		this.location = location;

		if (requestedChannels.NumberOfChannels() > 0) {
			changeStateSafe(STATE_LOADING);
		}
	}
	
	public void setTimeScale(final double timeScale) {
		updateTimeScale = timeScale;
	}

	private synchronized void setTimeScaleSafe(double timeScale) {
		log.debug("Setting time scale to " + timeScale + " seconds.");
		
		this.timeScale = timeScale;
		
		if (state == STATE_PLAYING) {
			preFetchData(location, timeScale);
		}
	}
	
	private void runRBNB() {
		log.debug("RBNB data thread has started.");
		
		requestedChannels = new ChannelMap();
		
		while (state != STATE_EXITING) {
			synchronized (this) {
				
				if (updateState != -1) {
					changeStateSafe(updateState);
					updateState = -1;
				}
				if (updateLocation != -1) {
					setLocationSafe(updateLocation);
					updateLocation = -1;
				} else if (updateTimeScale != -1) {
					setTimeScaleSafe(updateTimeScale);
					updateTimeScale = -1;
				}
				
				if (state == STATE_LOADING) {
					location -= domain;
					requestData(location, domain);
					updateDataMonitoring();
					location += domain;
					updateTimeListeners(location);
					changeStateSafe(STATE_STOPPED);
				} else if (state == STATE_PLAYING) {
					updateDataPlaying();
				} else if (state == STATE_MONITORING) {
					updateDataMonitoring();
				} else if (state == STATE_STOPPED) {
					initRBNB();
				}
				
			}
			
			if (state == STATE_STOPPED || state == STATE_DISCONNECTED) {
				try { Thread.sleep(50); } catch (Exception e) {}
			}
			
			Thread.yield();
		}
			
 		closeRBNB(false);

		log.debug("RBNB data thread is exiting.");
	}
		
	private synchronized boolean initRBNB() {
		if (sink == null) {			
			sink = new Sink();
		}  else {
			return true;
		}
		
		try {
			sink.OpenRBNBConnection(DataViewer.getRBNBHostName() + ":" + DataViewer.getRBNBPort(), rbnbSinkName);
		} catch (SAPIException e) {
			log.error("Failed to connect to RBNB server.");
			changeStateSafe(STATE_DISCONNECTED);
			return false;	
		}
		
		log.debug("Connected to RBNB server.");
		
		return true;
	}

	private synchronized boolean closeRBNB() {
 		return closeRBNB(true);
 	}
 	
 	private synchronized boolean closeRBNB(boolean changeState) {
		if (sink == null) return true;
			
		sink.CloseRBNBConnection();
		sink = null;

 		if (changeState) {
			changeStateSafe(STATE_DISCONNECTED);
		}

		log.debug("Connection to RBNB server closed.");
		
		return true;
	}
	
	private synchronized boolean reInitRBNB() {
		if (!closeRBNB(false)) {
			return false;
		}
		
		if (!initRBNB()) {
			return false;
		}
		
		return true;
	}
		
	public static String getStateName(int state) {
		String stateString;
		
		switch (state) {
			case STATE_MONITORING:
				stateString = "monitoring";
				break;
			case STATE_LOADING:
				stateString = "loading";
				break;
			case STATE_PLAYING:
				stateString = "playing";
				break;
			case STATE_STOPPED:
				stateString = "stopped";
				break;
			case STATE_EXITING:
				stateString = "exiting";
				break;
			case STATE_DISCONNECTED:
				stateString = "disconnected";
				break;
			default:
				stateString = "UNKNOWN";
		}
		
		return stateString;		
	}
	
	public boolean subscribe(String channelName, PlayerChannelListener panel) {	
		return subscribe(channelName, panel, true, true);
	}
	
	public boolean subscribe(String channelName, PlayerChannelListener panel, boolean loadData, boolean alertListeners) {
		//subscribe to channel
		try {
			requestedChannels.Add(channelName);
		} catch (SAPIException e) {
			log.error("Failed to add channel " + channelName + ".");
			return false;
		}

		channelManager.subscribe(channelName, panel);
		
		if (loadData) {
			switch (state) {
				case STATE_STOPPED:
				//case STATE_LOADING:
					setLocation(location);
					break;
				case STATE_MONITORING:
					monitor();
					break;
			}
		}
		
		if (alertListeners) {
			fireSubscriptionNotification(channelName);
		}
		
		return true;
	}
		
	public boolean unsubscribe(String channelName, PlayerChannelListener panel) {
		return unsubscribe(channelName, panel, true, true);	
	}
		
	public boolean unsubscribe(String channelName, PlayerChannelListener panel, boolean loadData, boolean alertListeners) {
		channelManager.unsubscribe(channelName, panel);
		
		if (!channelManager.isChannelSubscribed(channelName)) {
			//unsubscribe from the channel
			ChannelMap newRequestedChannels = new ChannelMap();		
			String[] channelList = requestedChannels.GetChannelList();
			for (int i=0; i<channelList.length; i++) {
				if (!channelName.equals(channelList[i])) {
					int channelIndex = -1;
					try {
						channelIndex = newRequestedChannels.Add(channelList[i]);
					} catch (SAPIException e) {
						log.error("Failed to remove to channel " + channelName + ".");
						return false;
					}
				}
			}
			requestedChannels = newRequestedChannels;
		}
		
		if (loadData) {
			switch (state) {
				case STATE_STOPPED:
				//case STATE_LOADING:
					if (requestedChannels.NumberOfChannels() > 0) {
						setLocation(location);
					}
					break;
				case STATE_MONITORING:
					monitor();
					break;
			}
		}
		
		if (alertListeners) {
			fireUnsubscriptionNotification(channelName);
		}
		
		return true;
	}
	
	public boolean subscribeAll(PlayerChannelListener channelListener) {
		String[] channels = requestedChannels.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			subscribe(channels[i], channelListener, (i==channels.length-1)?true:false, true);
		}
		
		return true;
	}

	public boolean unsubscribeAll(PlayerChannelListener channelListener) {
		boolean anyUnsubscribes = false;
		
		//unsubscribe listener from all channels it is listening to
		String[] channels = requestedChannels.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			if (channelManager.isListenerSubscribedToChannel(channelName, channelListener)) {
				unsubscribe(channelName, channelListener, false, true);
				anyUnsubscribes = true;
			}
		}
		
		//only load data if there was an actual unsubscribe
		if (anyUnsubscribes) {
			switch (state) {
				case STATE_STOPPED:
				//case STATE_LOADING:
					setLocation(location);
					break;
				case STATE_MONITORING:
					monitor();
					break;
			}
		}
		
		return true;
	}
	
	public boolean unsubscribeAndSubscribe(String unsubscribeChannelName, String subscribeChannelName, PlayerChannelListener channelListener) {
		if (!unsubscribe(unsubscribeChannelName, channelListener, false, false)) {
			return false;
		}
		
		boolean returnValue = subscribe(subscribeChannelName, channelListener, true, false);
		
		fireChangeNotification(unsubscribeChannelName, subscribeChannelName);
		
		return returnValue;
	}	
	
	public void addTimeListener(PlayerTimeListener timeListener) {
		timeListeners.add(timeListener);
		timeListener.postTime(location);
	}
	
	public void removeTimeListener(PlayerTimeListener timeListener) {
		timeListeners.remove(timeListener);
	}
	
	private void updateTimeListeners(double location) {
		for (int i=0; i<timeListeners.size(); i++) {
			PlayerTimeListener timeListener = (PlayerTimeListener)timeListeners.get(i);
			timeListener.postTime(location);
		}
	}
	
	private synchronized boolean monitorData() {					
		if (requestedChannels.NumberOfChannels() == 0) {
			return false;
		}
		
		if (requestIsMonitor) {		
			reInitRBNB();
		}
	
		try {
			sink.Monitor(requestedChannels, 5);
			requestIsMonitor = true;
			log.debug("Monitoring selected data channels.");
		} catch (SAPIException e) {
			log.error("Failed to monitor channels.");
			return false;
		}
		
		return true;
	}
	
	private boolean requestData() {
		return requestData(location, timeScale);
	}
	
	private boolean requestData(double location, double duration) {
		if (requestedChannels.NumberOfChannels() == 0) {
			return false;
		}
		
		if (requestIsMonitor) {
			reInitRBNB();
		}
	
		try {
			sink.Request(requestedChannels, location, duration, "absolute");
			requestIsMonitor = false;
		} catch (SAPIException e) {
 			log.error("Failed to request channels: " + e.getMessage() + ".");
			return false;
		}
		
		//log.debug("Requested selected channels at location " + formatDate(location) + " for " + duration + " seconds.");			
		
		return true;
	}
	
	private synchronized void updateDataPlaying() {			
		if (requestedChannels.NumberOfChannels() == 0) {
			log.warn("No channels selected for data playback.");
			changeStateSafe(STATE_STOPPED);		
			return;
		}
		
		ChannelMap getmap = null;
		
		getmap = getPreFetchChannelMap(5000);
		if (getmap == null) {
			log.error("Failed to get pre-fetched data.");
			changeStateSafe(STATE_STOPPED);
			return;
		} else if (getmap.GetIfFetchTimedOut()) {
			log.error("Fetch timed out.");
			changeStateSafe(STATE_STOPPED);
			return;
		}
		
		String[] channelList = getmap.GetChannelList();

		//stop if no data in fetch, most likely end of data
		//FIXME this can stop with a small duration, figure out a way to stop at end of data
		//if this happens maybe check the metadata length
 		/* if (channelList.length == 0) {
 			log.error("Received no data.");
 			changeStateSafe(STATE_STOPPED);
 			return;			
 		} */
 		
 		preFetchData(location+timeScale, timeScale);		
		
		//printChannelMap(getmap);
		
		//log.debug("Playing back " + timeScale + " seconds of data for " + channelList.length + " channels at location " + formatDate(location) + ".");
		
		double playbackRate = timeScale;
		double playbackDuration = timeScale;
		double playbackRefreshRate = PLAYBACK_REFRESH_RATE;
		double playbackStepTime = playbackRate * playbackRefreshRate;
		long playbackSteps = (long)(playbackDuration / playbackStepTime);
		
		double locationStartTime = location;
		long playbackStartTime = System.currentTimeMillis();
		
		int i = 0;
		while (i<playbackSteps && updateState == -1 && updateLocation == -1 && updateTimeScale == -1) {
			updateTimeListeners(location);
			
			channelManager.postData(getmap, location, playbackStepTime);					
			
			double timeDifference = (playbackRefreshRate*(i+1)) - ((System.currentTimeMillis() - playbackStartTime)/1000d);
 			if (dropData && timeDifference < -playbackRefreshRate) {
				int stepsToSkip = (int)((timeDifference*-1) / playbackRefreshRate);
 				log.debug("Skipping " + (long)(timeDifference*-1000) + " ms of data.");
				i += stepsToSkip;
 			} else if (timeDifference > playbackRefreshRate) {
				//log.debug("Sleeping for " + ((long)(timeDifference*1000)) + " ms.");
				try { Thread.sleep((long)(timeDifference*1000)); } catch (Exception e) { e.printStackTrace(); }
			}
			
			i++;
			
			location = locationStartTime + (playbackStepTime) * i;
		}
		
		updateTimeListeners(location);
		
		//long playbackEndTime = System.currentTimeMillis();
		//log.debug("Actual duration " + (playbackEndTime-playbackStartTime)/1000d + ", seconds desired duration " + playbackDuration/playbackRate + " seconds.");
	}
	
	private void preFetchData(final double location, final double duration) {
		preFetchChannelMap = null;
		preFetchDone = false;
		
		//log.debug("Pre-fetching data at location " + DataViewer.formatDate(location) + " for " + duration + " seconds.");
		
		final long preFetchStartTime = System.currentTimeMillis();
		
		new Thread(new Runnable() {
			public void run() {
				if (requestData(location, duration)) {
					try {
						preFetchChannelMap = sink.Fetch(5000);
					} catch (Exception e) {
 						log.error("Failed to fetch data: " + e.getMessage() + ".");
						changeStateSafe(STATE_STOPPED);
						return;
					}
				} else {
					preFetchChannelMap = null;
				}
					
				//log.debug("Pre-fetching data took " + (System.currentTimeMillis() - preFetchStartTime)/1000d + " seconds.");
				
				synchronized(preFetchLock) {
					preFetchDone = true;
					preFetchLock.notify();
				}				
			}
		}, "prefetch").start();
		
				
	}
	
	/* private ChannelMap getPreFetchChannelMap() {
		return getPreFetchChannelMap(-1);
	} */
	
	private ChannelMap getPreFetchChannelMap(long timeOut) {
		synchronized(preFetchLock) {
			if (!preFetchDone) {
				log.warn("Waiting for channel map.");
				try {
					if (timeOut == -1) {
						preFetchLock.wait();
					} else {
						preFetchLock.wait(timeOut);
					}
 				} catch (Exception e) {
 					log.error("Failed to wit for channel map: " + e.getMessage() + ".");
 				}
 				log.debug("Done waiting for channel map.");
			}
		}
		
		preFetchDone = false;
		
		ChannelMap fetchedMap = preFetchChannelMap;
		preFetchChannelMap = null;
		
		return fetchedMap;
	}

	private synchronized void updateDataMonitoring() {	
		//stop monitoring if no channels selected
		//TODO see if this should be posible or indicates an error in the program
		if (requestedChannels.NumberOfChannels() == 0) {
			log.warn("No channels selected for data monitor.");
			changeStateSafe(STATE_STOPPED);
			return;
		}
		
		ChannelMap getmap = null;

		try {
			getmap = sink.Fetch(5000);
		} catch (SAPIException e) {
 			log.error("Failed to fetch data: " + e.getMessage() + ".");
			changeStateSafe(STATE_STOPPED);
			return;
		}

		if (getmap.GetIfFetchTimedOut()) {
			log.warn("Fetch timed out.");
			return;
		}

		String[] channelList = getmap.GetChannelList();
		
		//received no data
		if (channelList.length == 0) {
			return;			
		} 

		//update current location
		//TODO see if we should instead do this at the end for the latest location
		if (state == STATE_MONITORING) {
			location = getmap.GetTimeStart(0);
			updateTimeListeners(location);
		}
		
		channelManager.postData(getmap);		
	}
				
	private static void printChannelMap(ChannelMap cmap) {
		String[] channels = cmap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			log.debug("Channel " + channels[i] + ": " + DataViewer.formatDate(cmap.GetTimeStart(i)) + " (" + cmap.GetTimeDuration(i) + ").");
			double[] times = cmap.GetTimes(i);
			for (int j=0; j<times.length; j++) {
				log.debug(" location = " + DataViewer.formatDate(times[j]) + ".");
			}
		}
	}
	
	public String getRBNBHostName() {
		return DataViewer.getRBNBHostName();
	}
	
	public boolean isConnected() {
		return sink != null;	
	}
	
	public void reconnect() {
		disconnect();
		connect();
	}
	
	public void connect() {
		changeState(STATE_STOPPED);
	}
	
	public boolean disconnect() {
		return closeRBNB();
	}
	
	public int getState() {
		return state;
	}
		
	public double getLocation() {
		return location;
	}
	
	public double getTimeScale() {
		return timeScale;
	}

	public String[] getAvailableChannels() {
		return availableChannels;
	}

	public void addStateListener(PlayerStateListener stateListener) {
		stateListener.postState(state, state);
		stateListeners.add(stateListener);
	}

	public void removeStateListener(PlayerStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	private void notifyStateListeners(int state, int oldState) {
		for (int i=0; i<stateListeners.size(); i++) {
			PlayerStateListener stateListener = (PlayerStateListener)stateListeners.get(i);
			stateListener.postState(state, oldState);
		}		
	}
	
	public void addSubscriptionListener(PlayerSubscriptionListener subscriptionListener) {
		subscriptionListeners.add(subscriptionListener);
	}

	public void removeSubscriptionListener(PlayerSubscriptionListener subscriptionListener) {
		subscriptionListeners.remove(subscriptionListener);
	}

	private void fireSubscriptionNotification(String channelName) {
		PlayerSubscriptionListener subscriptionListener;
		for (int i=0; i<subscriptionListeners.size(); i++) {
			subscriptionListener = (PlayerSubscriptionListener)subscriptionListeners.get(i);
			subscriptionListener.channelSubscribed(channelName);
		}
	}
	
	private void fireUnsubscriptionNotification(String channelName) {
		PlayerSubscriptionListener subscriptionListener;
		for (int i=0; i<subscriptionListeners.size(); i++) {
			subscriptionListener = (PlayerSubscriptionListener)subscriptionListeners.get(i);
			subscriptionListener.channelUnsubscribed(channelName);
		}		
	}
	
	private void fireChangeNotification(String unsubscribedChannelName, String subscribedChannelName) {
		PlayerSubscriptionListener subscriptionListener;
		for (int i=0; i<subscriptionListeners.size(); i++) {
			subscriptionListener = (PlayerSubscriptionListener)subscriptionListeners.get(i);
			subscriptionListener.channelChanged(unsubscribedChannelName, subscribedChannelName);
		}		
	}
		
	public String[] getSubscribedChannels(PlayerChannelListener channelListener) {
		return requestedChannels.GetChannelList(); 
	}
	
	public boolean isSubscribed(String channelName) {
		return channelManager.isChannelSubscribed(channelName);
	}

	public void timeScaleChanged(double timeScale) {
		setTimeScale(timeScale);
	}

	public void domainChanged(double domain) {
		this.domain = domain;
		
		if (state == STATE_STOPPED && requestedChannels.NumberOfChannels() > 0) {
 			changeState(STATE_LOADING);
		}
	}

	public void channelListUpdated(ChannelMap channelMap) {
		availableChannels = channelMap.GetChannelList();
	}
 
 	public void dropData(boolean dropData) {
 		this.dropData = dropData;
 	}
}