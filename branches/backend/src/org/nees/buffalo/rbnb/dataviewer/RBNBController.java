package org.nees.buffalo.rbnb.dataviewer;

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

 	private int state;
 	
 	private Sink sink;
	
	private boolean requestIsMonitor;	
	
	private ChannelMap requestedChannels;
	
	private ChannelManager channelManager;
	
	private Vector timeListeners;
	private Vector stateListeners;
	private Vector subscriptionListeners;
	
	private ChannelMap preFetchChannelMap;
	private Object preFetchLock = new Object();
	private boolean preFetchDone;
	
	ChannelMap metaDataChannelMap;
	
	//FIXME should be -1, but breaks stuff
	private double location = System.currentTimeMillis()/1000d;
	private double timeScale = 1;
	private double domain = 1;
	
	private double updateLocation = -1;
	private double updateTimeScale = -1;
	private int updateState = -1;

 	private boolean dropData;
	
	static final double PLAYBACK_REFRESH_RATE = 0.05;
	
	public RBNBController() {
		//initial state is disconnected
		state = STATE_DISCONNECTED;
		
		requestIsMonitor = false;
		
		dropData = true;
		
		channelManager = new ChannelManager(); 
		
		timeListeners = new Vector();
		stateListeners = new Vector();
		subscriptionListeners = new Vector();
		
		run();
	}
	
	private void run() {
		new Thread(new Runnable() {
			public void run() {
				runRBNB();
			}
		}, "RBNB").start();
	}
	
	private void runRBNB() {
		log.info("RBNB data thread has started.");
		
		requestedChannels = new ChannelMap();
		
		while (state != STATE_EXITING) {
			
			processSubscriptionRequests();
			
			processPlayerRequests();
					
			switch (state) {
				case STATE_LOADING:
					requestData(location-domain, domain);
					updateDataMonitoring();
					updateTimeListeners(location);					
					changeStateSafe(STATE_STOPPED);
					break;
				case STATE_PLAYING:
					updateDataPlaying();
					break;
				case STATE_MONITORING:
					updateDataMonitoring();
					break;
				case STATE_REALTIME:
					updateDataRealTime();
					break;
				case STATE_STOPPED:
					initRBNB();
				case STATE_DISCONNECTED:
					try { Thread.sleep(50); } catch (Exception e) {}
					break;
			}
		}
			
 		closeRBNB(false);

		log.info("RBNB data thread is exiting.");
	}
	
	
	// State Processing Methods
	
	private void processSubscriptionRequests() {
		//TODO get subscription requests into here
		//     so they are thread safe
	}

	private synchronized void processPlayerRequests() {
		if (updateLocation != -1) {
			setLocationSafe(updateLocation);
			updateLocation = -1;
		}
		
		if (updateTimeScale != -1) {
			setTimeScaleSafe(updateTimeScale);
			updateTimeScale = -1;
		}
		
		if (updateState != -1) {
			changeStateSafe(updateState);
			updateState = -1;
		}		
	}
	
	private void setLocationSafe(double location) {
		log.info("Setting location to " + DataViewer.formatDate(location) + ".");
	
		this.location = location;

		if (requestedChannels.NumberOfChannels() > 0) {
			changeStateSafe(STATE_LOADING);
		}
	}
	
	private void setTimeScaleSafe(double timeScale) {
		if (this.timeScale == timeScale) {
			//the time scale value hasn't changed
			return;
		}
		
		log.info("Setting time scale to " + timeScale + " seconds.");
		
		this.timeScale = timeScale;
		
		if (state == STATE_PLAYING) {
			preFetchData(location, timeScale);
		}
	}	
		
	private void changeState(int newState) {
		updateState = newState;
	}
	
	private boolean changeStateSafe(int newState) {
		int oldState = state;
			
		if (oldState == STATE_EXITING) {
			log.error("Can not transition out of exiting state to " + getStateName(state) + " state.");
			return false;
		} else if (oldState == newState) {
			log.error("Transitioning to same state (" + getStateName(state) + ").");
			return false;
 		} else if (oldState == STATE_DISCONNECTED && newState != STATE_EXITING) {
			if (!initRBNB()) {
				return false;
			}
		}
		
		switch (newState) {
			case STATE_MONITORING:		    
				monitorData();
				break;		
			case STATE_PLAYING:
			case STATE_REALTIME:
				if (oldState == STATE_MONITORING || oldState == STATE_STOPPED) {
					preFetchData(location, timeScale);
				}
				break;
			case STATE_LOADING:
			case STATE_STOPPED:
			case STATE_EXITING:
			case STATE_DISCONNECTED:				
				break;
			default:
				log.error("Unknown state: " + state + ".");
				return false;
		}
		
		state = newState;
		
		notifyStateListeners(state, oldState);

		log.info("Transitioned from state " + getStateName(oldState) + " to " + getStateName(state) + ".");
		
		return true;
	}
	
	
	// RBNB Methods

	private boolean initRBNB() {
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
		
		log.info("Connected to RBNB server.");
		
		return true;
	}

	private boolean closeRBNB() {
 		return closeRBNB(true);
 	}
 	
 	private boolean closeRBNB(boolean changeState) {
		if (sink == null) return true;
			
		sink.CloseRBNBConnection();
		sink = null;

 		if (changeState) {
			changeStateSafe(STATE_DISCONNECTED);
		}

		log.info("Connection to RBNB server closed.");
		
		return true;
	}
	
	private boolean reInitRBNB() {
		if (!closeRBNB(false)) {
			return false;
		}
		
		if (!initRBNB()) {
			return false;
		}
		
		return true;
	}
	
	
	// Subscription Methods
	
	private boolean subscribeSafe(String channelName, PlayerChannelListener panel) {
		//subscribe to channel
		try {
			requestedChannels.Add(channelName);
		} catch (SAPIException e) {
			log.error("Failed to add channel " + channelName + ".");
			e.printStackTrace();
			return false;
		}

		//notify channel manager
		channelManager.subscribe(channelName, panel);
		
		log.info("Subscribed to " + channelName + " for listener " + panel + ".");
		
		double loadLocation;
		if (location == -1) {
			//set initial location to end time for channel
			loadLocation = getEndTime(metaDataChannelMap, channelName);
		} else {
			loadLocation = location;
		}
		
		switch (state) {
			case STATE_STOPPED:
			//case STATE_LOADING:
				//TODO we should really only load the data we need
				setLocationSafe(loadLocation);
				break;
			case STATE_MONITORING:
				monitor();
				break;
		}
		
		fireSubscriptionNotification(channelName);
		
		return true;
	}
		
	private boolean unsubscribeSafe(String channelName, PlayerChannelListener panel) {
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
						e.printStackTrace();
						return false;
					}
				}
			}
			requestedChannels = newRequestedChannels;
		}
		
		log.info("Unsubscribed from " + channelName + " for listener " + panel + ".");
		
		if (state == STATE_MONITORING) {
			monitor();
		}
		
		fireUnsubscriptionNotification(channelName);
		
		return true;
	}
	
	private boolean unsubscribeAllSafe(PlayerChannelListener channelListener) {
		boolean anyUnsubscribes = false;
		
		//unsubscribe listener from all channels it is listening to
		String[] channels = requestedChannels.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			if (channelManager.isListenerSubscribedToChannel(channelName, channelListener)) {
				unsubscribeSafe(channelName, channelListener);
				anyUnsubscribes = true;
			}
		}
		
		if (anyUnsubscribes && state == STATE_MONITORING) {
			monitor();
		}
		
		return true;
	}	
	
	// Playback Methods
	
	private boolean requestData(double location, double duration) {
		if (requestedChannels.NumberOfChannels() == 0) {
			return false;
		}
		
		if (requestIsMonitor) {
			reInitRBNB();
			requestIsMonitor = false;
		}

		log.debug("Requesting data at location " + DataViewer.formatDate(location) + " for " + duration + " seconds.");
		
		try {
			sink.Request(requestedChannels, location, duration, "absolute");
		} catch (SAPIException e) {
 			log.error("Failed to request channels at " + DataViewer.formatDate(location) + " for " + DataViewer.formatSeconds(duration) + ".");
 			e.printStackTrace();
			return false;
		}			
		
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
 		
		channelManager.postData(getmap);					
		
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
			double timeDifference = (playbackRefreshRate*(i+1)) - ((System.currentTimeMillis() - playbackStartTime)/1000d);
 			if (dropData && timeDifference < -playbackRefreshRate) {
				int stepsToSkip = (int)((timeDifference*-1) / playbackRefreshRate);
 				//log.debug("Skipping " + (long)(timeDifference*-1000) + " ms of data.");
				i += stepsToSkip;
 			} else if (timeDifference > playbackRefreshRate) {
				//log.debug("Sleeping for " + ((long)(timeDifference*1000)) + " ms.");
				try { Thread.sleep((long)(timeDifference*1000)); } catch (Exception e) { e.printStackTrace(); }
			}
			
			i++;
			
			location = locationStartTime + (playbackStepTime) * i;
			updateTimeListeners(location);
		}
		
		//long playbackEndTime = System.currentTimeMillis();
		//log.debug("Actual duration " + (playbackEndTime-playbackStartTime)/1000d + ", seconds desired duration " + playbackDuration/playbackRate + " seconds.");
	}
	
	private void preFetchData(final double location, final double duration) {
		preFetchChannelMap = null;
		preFetchDone = false;
		
		log.debug("Pre-fetching data at location " + DataViewer.formatDate(location) + " for " + duration + " seconds.");
		
		final long preFetchStartTime = System.currentTimeMillis();
		
		new Thread(new Runnable() {
			public void run() {
				boolean requestStatus = false;
				if (state == STATE_PLAYING) {
					requestStatus = requestData(location, duration);
				} else if (state == STATE_REALTIME) {
					requestStatus = requestDataRealTime();
				}
				
				if (requestStatus) {
					try {
						preFetchChannelMap = sink.Fetch(5000);
					} catch (Exception e) {
 						log.error("Failed to fetch data.");
 						e.printStackTrace();
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
	
	private ChannelMap getPreFetchChannelMap(long timeOut) {
		synchronized(preFetchLock) {
			if (!preFetchDone) {
				log.debug("Waiting for pre-fetch channel map.");
				try {
					if (timeOut == -1) {
						preFetchLock.wait();
					} else {
						preFetchLock.wait(timeOut);
					}
 				} catch (Exception e) {
 					log.error("Failed to wait for channel map.");
 					e.printStackTrace();
 				}
 				log.debug("Done waiting for pre-fetch channel map.");
			}
		}
		
		preFetchDone = false;
		
		ChannelMap fetchedMap = preFetchChannelMap;
		preFetchChannelMap = null;
		
		return fetchedMap;
	}
	
	
	// Monitor Methods
	
	private boolean monitorData() {					
		if (requestedChannels.NumberOfChannels() == 0) {
			return false;
		}
		
		if (requestIsMonitor) {		
			reInitRBNB();
		}
		
		log.debug("Monitoring data after location " + DataViewer.formatDate(location) + ".");
	
		try {
			sink.Monitor(requestedChannels, 5);
			requestIsMonitor = true;
			log.info("Monitoring selected data channels.");
		} catch (SAPIException e) {
			log.error("Failed to monitor channels.");
			return false;
		}
		
		return true;
	}	

	private void updateDataMonitoring() {	
		//stop monitoring if no channels selected
		//TODO see if this should be posible or indicates an error in the program
		if (requestedChannels.NumberOfChannels() == 0) {
			log.warn("No channels subscribed to monitor.");
			changeStateSafe(STATE_STOPPED);
			return;
		}
		
		ChannelMap getmap = null;

		int timeout;
		if (state == STATE_MONITORING) {
			timeout = 500;
		} else {
			timeout = 5000;
		}
		
		try {
			getmap = sink.Fetch(timeout);
		} catch (SAPIException e) {
 			log.error("Failed to fetch data.");
 			e.printStackTrace();
			changeStateSafe(STATE_STOPPED);
			return;
		}

		if (getmap.GetIfFetchTimedOut()) {
			if (state == STATE_MONITORING) {
				//no data was received, this is not an error and we should go on
				//to see if more data is recieved next time around
				//TODO see if we should sleep here
				log.debug("Fetch timed out for monitor.");
				return;
				
			} else {
				log.error("Failed to fetch data.");
				changeState(STATE_STOPPED);
				return;
			}
		}

		String[] channelList = getmap.GetChannelList();
		
		//received no data
		if (channelList.length == 0) {
			return;			
		} 

		//post data to listeners
		channelManager.postData(getmap);
		
		if (state == STATE_MONITORING) {
			//update current location
			location = getLastTime(getmap);
			updateTimeListeners(location);
		}
	}
	
	
	// Real Time Methods
	
	private boolean requestDataRealTime() {
		if (requestedChannels.NumberOfChannels() == 0) {
			return false;
		}
		
		if (requestIsMonitor) {
			reInitRBNB();
			requestIsMonitor = false;
		}
	
		try {
			sink.Request(requestedChannels, PLAYBACK_REFRESH_RATE, PLAYBACK_REFRESH_RATE, "newest");
		} catch (SAPIException e) {
 			log.error("Failed to request channels (RT) at " + DataViewer.formatDate(location) + ".");
 			e.printStackTrace();
			return false;
		}			
		
		return true;
	}
	
	private void updateDataRealTime() {	
		//stop monitoring if no channels selected
		//TODO see if this should be posible or indicates an error in the program
		if (requestedChannels.NumberOfChannels() == 0) {
			log.debug("No channels subscribed to monitor.");
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
		
		preFetchData(-1, -1);

		String[] channelList = getmap.GetChannelList();
		
		//received no data
		if (channelList.length == 0) {
			return;			
		}
		
		//save previous location
		double time = location;

		//update current location
		location = getLastTime(getmap);
		updateTimeListeners(location);

		//post data to listeners
		channelManager.postData(getmap);
		
		long sleepTime = 0;
		
		//find min and max times
		double minTime = Double.MAX_VALUE;
		double maxTime = 0;
		for (int j=0; j<channelList.length; j++) {
			String channelName = channelList[j];
			int channelIndex = getmap.GetIndex(channelName);
			double[] times = getmap.GetTimes(channelIndex);
			for (int k=0; k<times.length; k++) {
				minTime = Math.min(times[k], minTime);
				maxTime = Math.max(times[k], maxTime);
			}
		}
		
		//figure out how much time to sleep
		double offset = (time==-1 || minTime == Double.MAX_VALUE) ? 0.005 : time - minTime;
		if (offset > 0) {
			sleepTime += offset*1000/4;
		} else if (offset == 0) {
			sleepTime -= 5;
		} else if (sleepTime > -1*offset*1000){
			sleepTime += offset*1000;
		} else {
			sleepTime = 0;
		}
		
		if (sleepTime >= 5) {
			//log.debug("Sleeping " + sleepTime + " ms. Offset is " + ((long)(offset*1000)) + " (" + DataViewer.formatDate(time) + " , " + DataViewer.formatDate(minTime) +").");
			try { Thread.sleep(sleepTime); } catch (Exception e) {}
		}
	}
	
	
	// Listener Methods
	
	private void updateTimeListeners(double location) {
		for (int i=0; i<timeListeners.size(); i++) {
			PlayerTimeListener timeListener = (PlayerTimeListener)timeListeners.get(i);
			timeListener.postTime(location);
		}
	}	
			
	private void notifyStateListeners(int state, int oldState) {
		for (int i=0; i<stateListeners.size(); i++) {
			PlayerStateListener stateListener = (PlayerStateListener)stateListeners.get(i);
			stateListener.postState(state, oldState);
		}		
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
	
	
	// Utility (Static) Methods
	
	private static double getLastTime(ChannelMap channelMap) {
		double lastTime = -1;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			int channelIndex = channelMap.GetIndex(channelName);
			double[] times = channelMap.GetTimes(channelIndex);
			double endTime = times[times.length-1];
			if (endTime > lastTime) {
				lastTime = endTime;
			}
		}
		
		return lastTime;
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
	
	
 	// Player Methods

 	public void monitor() {
		changeState(STATE_MONITORING);
 		//changeState(STATE_REALTIME);
	}
	
	public void play() {
		changeState(STATE_PLAYING);
	}
	
	public void pause() {
		changeState(STATE_STOPPED);
	}
	
	public void exit() {
		changeState(STATE_EXITING);
		
		//wait for thread to finish
 		int count = 0;
 		while (sink != null && count++ < 20) {
 			try { Thread.sleep(50); } catch (Exception e) {}
 		}
	}
	
	public double getLocation() {
		return location;
	}
		
	public void setLocation(final double location) {
		updateLocation = location;
	}
	
	public double getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(final double timeScale) {
		updateTimeScale = timeScale;
	}
	
	public boolean subscribe(String channelName, PlayerChannelListener panel) {
		// FIXME make me thread safe
		return subscribeSafe(channelName, panel);
	}

	public boolean unsubscribe(String channelName, PlayerChannelListener panel) {
		// FIXME make me thread safe
		return unsubscribeSafe(channelName, panel);	
	}

	public boolean unsubscribeAll(PlayerChannelListener channelListener) {
		// FIXME make me thread safe
		return unsubscribeAllSafe(channelListener);
	}
	
	public boolean isSubscribed(String channelName) {
		return channelManager.isChannelSubscribed(channelName);
	}

	public void addStateListener(PlayerStateListener stateListener) {
		stateListener.postState(state, state);
		stateListeners.add(stateListener);
	}

	public void removeStateListener(PlayerStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	public void addTimeListener(PlayerTimeListener timeListener) {
		timeListeners.add(timeListener);
		//FIXME listners expect data now when this is called
		//timeListener.postTime(location);
	}
	
	public void removeTimeListener(PlayerTimeListener timeListener) {
		timeListeners.remove(timeListener);
	}	
	
	
	// Public Methods
	
 	public void dropData(boolean dropData) {
 		this.dropData = dropData;
 	}

	public boolean isConnected() {
		return sink != null;	
	}
		
	public void connect() {
		changeState(STATE_STOPPED);
	}
	
	public void disconnect() {
		changeState(STATE_DISCONNECTED);
	}
	
	public void reconnect() {
		disconnect();
		connect();
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
		metaDataChannelMap = channelMap;
	}	
	
	public void addSubscriptionListener(PlayerSubscriptionListener subscriptionListener) {
		subscriptionListeners.add(subscriptionListener);
	}

	public void removeSubscriptionListener(PlayerSubscriptionListener subscriptionListener) {
		subscriptionListeners.remove(subscriptionListener);
	}

	
	//Public Static Methods

	public static String getStateName(int state) {
		String stateString;
		
		switch (state) {
			case STATE_LOADING:
				stateString = "loading";
				break;
			case STATE_PLAYING:
				stateString = "playing";
				break;
			case STATE_MONITORING:
				stateString = "monitoring";
				break;
			case STATE_REALTIME:
				stateString = "real time";
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
	
	public static double getEndTime(ChannelMap channelMap) {
		double end = -1;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			Math.max(getEndTime(channelMap, channelName), end);
		}
		
		return end;
	}
	
}