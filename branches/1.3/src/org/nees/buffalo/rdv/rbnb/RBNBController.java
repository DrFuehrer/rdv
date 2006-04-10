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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/rbnb/RBNBController.java $
 * $Revision: 410 $
 * $Date: 2006-02-16 20:39:09 -0800 (Thu, 16 Feb 2006) $
 * $Author: ljmiller $
 */

package org.nees.buffalo.rdv.rbnb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * @author Jason P. Hanley
 */
public class RBNBController implements Player, MetadataListener {

	static Log log = LogFactory.getLog(RBNBController.class.getName());

	private final String rbnbSinkName = "RDV";

 	private int state;
 	
 	private Sink sink;
 	
 	private String rbnbHostName;
 	private int rbnbPortNumber;
 	
	private static final String DEFAULT_RBNB_HOST_NAME = "localhost";
	private static final int DEFAULT_RBNB_PORT_NUMBER = 3333; 	
	
	private boolean requestIsMonitor;	
	
	private ChannelMap requestedChannels;
  private ChannelTree metaDataChannelTree;

	private ChannelManager channelManager;
  private MetadataManager metadataManager;
	
	private Vector timeListeners;
	private Vector stateListeners;
	private Vector subscriptionListeners;
	private ArrayList playbackRateListeners;
	private ArrayList timeScaleChangeListeners;
	private ArrayList messageListeners;
	private ArrayList connectionListeners;
	
	private ChannelMap preFetchChannelMap;
	private Object preFetchLock = new Object();
	private boolean preFetchDone;
		
	private double location;
	private double playbackRate;
	private double timeScale;
	
	private double updateLocation = -1;
  private Object updateLocationLock = new Object();
  
  private double updateTimeScale = -1;
  private Object updateTimeScaleLock = new Object();
  
	private double updatePlaybackRate = -1;
  private Object updatePlaybackRateLock = new Object();
    
  private ArrayList stateChangeRequests = new ArrayList();	
	private ArrayList updateSubscriptionRequests = new ArrayList();

 	private boolean dropData;
	
	private final double PLAYBACK_REFRESH_RATE = 0.05;
	
	public RBNBController(double location, double playbackRate, double timeScale) {
		state = STATE_DISCONNECTED;
		
		rbnbHostName = DEFAULT_RBNB_HOST_NAME;
		rbnbPortNumber = DEFAULT_RBNB_PORT_NUMBER;
		
		requestIsMonitor = false;
		
		dropData = true;
		
    this.location = location;
		this.playbackRate = playbackRate;
		this.timeScale = timeScale;

    requestedChannels = new ChannelMap();
    metaDataChannelTree = ChannelTree.EMPTY_TREE;

		channelManager = new ChannelManager();
    metadataManager = new MetadataManager(this);

		timeListeners = new Vector();
		stateListeners = new Vector();
		subscriptionListeners = new Vector();
		playbackRateListeners = new ArrayList();
		timeScaleChangeListeners = new ArrayList();
		messageListeners = new ArrayList();
		connectionListeners = new ArrayList();

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
		
		while (state != STATE_EXITING) {
			
			processSubscriptionRequests();
      processLocationUpdate();       
      processTimeScaleUpdate();
      processPlaybackRateUpdate();
      processStateChangeRequests();
					
			switch (state) {
				case STATE_LOADING:
					loadAllData();
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
		//skip lock if no subscription requests
		if (updateSubscriptionRequests.size() == 0) {
			return;
		}
		
		synchronized (updateSubscriptionRequests) {
			//loop of subscription requests
			for (int i=0; i<updateSubscriptionRequests.size(); i++) {
				SubscriptionRequest subscriptionRequest = (SubscriptionRequest)updateSubscriptionRequests.get(i);
				String channelName = subscriptionRequest.getChannelName();
				DataListener listener = subscriptionRequest.getListener();		
				if (subscriptionRequest.isSubscribe()) {
					subscribeSafe(channelName, listener);
				} else {
					unsubscribeSafe(channelName, listener);
				}
			}
			updateSubscriptionRequests.clear();
		}
	}

	private void processLocationUpdate() {
    if (updateLocation != -1) {
      synchronized (updateLocationLock) {
        location = updateLocation;
        updateLocation = -1;
      }

      log.info("Setting location to " + DataViewer.formatDate(location) + ".");

      if (requestedChannels.NumberOfChannels() > 0) {
        changeStateSafe(STATE_LOADING);
      }
    }
	}
    
  private void processTimeScaleUpdate() {
    if (updateTimeScale != -1 && timeScale != updateTimeScale) {
      synchronized (updateTimeScaleLock) {
        timeScale = updateTimeScale;
        updateTimeScale = -1;
      }
      
      log.info("Setting time scale to " + timeScale + ".");
    
      //TODO make this loading smarter
      if (state == STATE_STOPPED && requestedChannels.NumberOfChannels() > 0) {
        changeStateSafe(STATE_LOADING);
      }
      
      fireTimeScaleChanged(timeScale);
    }    
  }
	
	private void processPlaybackRateUpdate() {
    if (updatePlaybackRate != -1 && playbackRate != updatePlaybackRate) {
      synchronized (updatePlaybackRateLock) {
      	playbackRate = updatePlaybackRate;
        updatePlaybackRate = -1;
      }

      log.info("Setting playback rate to " + playbackRate + " seconds.");
    
      if (state == STATE_PLAYING) {
        if (!preFetchDone) {
          //wait for last prefetch to finish
          getPreFetchChannelMap(-1);
        }
        preFetchData(location, playbackRate);
      }
    
      firePlaybackRateChanged(playbackRate);
    }     
	}
    
  private void processStateChangeRequests() {
    if (stateChangeRequests.size() > 0 && state != STATE_LOADING) {
      synchronized (stateChangeRequests) {
        for (int i=0; i<stateChangeRequests.size(); i++) {
          int updateState = ((Integer)stateChangeRequests.get(i)).intValue();
          changeStateSafe(updateState);
        }
        stateChangeRequests.clear();
      }
    }
  }
		
	private void requestStateChange(int state) {
		synchronized (stateChangeRequests) {
      stateChangeRequests.add(new Integer(state));
    }
	}
	
	private boolean changeStateSafe(int newState) {
		int oldState = state;
			
		if (oldState == STATE_EXITING) {
			log.error("Can not transition out of exiting state to " + getStateName(state) + " state.");
			return false;
 		} else if (oldState == STATE_DISCONNECTED && newState != STATE_EXITING && newState != STATE_DISCONNECTED) {
 			fireConnecting();
			if (!metadataManager.updateMetadata()) {
        fireErrorMessage("Failed to connect to the RBNB server.");
				fireConnectionFailed();
				return false;
			}
			if (!initRBNB()) {
				//FIXME we need to clear the channel list in this case
				fireConnectionFailed();
				return false;
			}
			fireConnected();
		}
		
		switch (newState) {
			case STATE_MONITORING:
				monitorData();
				break;		
			case STATE_PLAYING:
			case STATE_REALTIME:
				if (oldState == STATE_MONITORING || oldState == STATE_STOPPED) {
					preFetchData(location, playbackRate);
				}
				break;
			case STATE_LOADING:
			case STATE_STOPPED:
			case STATE_EXITING:
				break;
			case STATE_DISCONNECTED:
				closeRBNB(false);
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
			sink.OpenRBNBConnection(rbnbHostName + ":" + rbnbPortNumber, rbnbSinkName);
		} catch (SAPIException e) {
			log.error("Failed to connect to the RBNB server.");
			fireErrorMessage("Failed to connect to the RBNB server.");
			closeRBNB(true);
			return false;	
		}
		
		log.info("Connected to RBNB server.");
		
		return true;
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
	
	private boolean subscribeSafe(String channelName, DataListener panel) {
		//subscribe to channel
		try {
			requestedChannels.Add(channelName);
		} catch (SAPIException e) {
			log.error("Failed to add channel " + channelName + ".");
			e.printStackTrace();
			return false;
		}
		
		//update metadata
		metadataManager.updateMetadataBackground();

		//notify channel manager
		channelManager.subscribe(channelName, panel);
		
		log.info("Subscribed to " + channelName + " for listener " + panel + ".");
		
		switch (state) {
			case STATE_STOPPED:
				loadData(channelName);
				break;
			case STATE_MONITORING:
				monitor();
				break;
		}
		
		fireSubscriptionNotification(channelName);
		
		return true;
	}
		
	private boolean unsubscribeSafe(String channelName, DataListener panel) {
		channelManager.unsubscribe(channelName, panel);
		
		if (!channelManager.isChannelSubscribed(channelName)) {
			//unsubscribe from the channel
			ChannelMap newRequestedChannels = new ChannelMap();		
			String[] channelList = requestedChannels.GetChannelList();
			for (int i=0; i<channelList.length; i++) {
				if (!channelName.equals(channelList[i])) {
					try {
						newRequestedChannels.Add(channelList[i]);
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
	
	
	// Load Methods
	
	private boolean loadData(String channelName) {
		ChannelMap realRequestedChannels = requestedChannels;
		
		requestedChannels = new ChannelMap();
		try {
			requestedChannels.Add(channelName);
		} catch (SAPIException e) {
			log.error("Failed to add channel " + channelName + ".");
			requestedChannels = realRequestedChannels;
			e.printStackTrace();
			return false;
		}
		
		double requestTimeScale;
		if (isVideo(metaDataChannelTree, channelName)) {
			requestTimeScale = 0;
		} else {
			requestTimeScale = timeScale;
		}
		if (!requestData(location-requestTimeScale, requestTimeScale)) {
			requestedChannels = realRequestedChannels;
			return false;
		}
		
		updateDataMonitoring();
		updateTimeListeners(location);
		
		log.info("Loaded " + DataViewer.formatSeconds(timeScale) + " of data for channel " + channelName + " at " + DataViewer.formatDate(location) + ".");
		
		requestedChannels = realRequestedChannels;
		
		return true;
	}
	
	private void loadAllData() {
		ChannelMap realRequestedChannels = requestedChannels;
		
		String[] allSubscribedChannels = requestedChannels.GetChannelList();
		
		ChannelMap imageChannels = new ChannelMap();
		ChannelMap otherChannels = new ChannelMap();
		
		for (int i=0; i<allSubscribedChannels.length; i++) {
			String channelName = allSubscribedChannels[i];
			try {
				if (isVideo(metaDataChannelTree, channelName)) {
					imageChannels.Add(channelName);
				} else {
					otherChannels.Add(channelName);
				}
			} catch (SAPIException e) {
				log.error("Failed to add channel " + channelName + ".");
				e.printStackTrace();
			}			
		}
		
		if (imageChannels.NumberOfChannels() > 0) {
			requestedChannels = imageChannels;
			double smallTimeScale = 0;
			if (!requestData(location-smallTimeScale, smallTimeScale)) {
				requestedChannels = realRequestedChannels;
				return;
			}
			updateDataMonitoring();
			updateTimeListeners(location);
		}
		
		if (otherChannels.NumberOfChannels() > 0) {
			requestedChannels = otherChannels;
			if (!requestData(location-timeScale, timeScale)) {
				requestedChannels = realRequestedChannels;
				return;
			}
			updateDataMonitoring();
			updateTimeListeners(location);
		}
		
		requestedChannels = realRequestedChannels;
		changeStateSafe(STATE_STOPPED);
		
		log.info("Loaded " + DataViewer.formatSeconds(timeScale) + " of data for all channels at " + DataViewer.formatDate(location) + ".");
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

		//stop if no data in fetch and past end time, most likely end of data
 		if (channelList.length == 0 && !moreData(requestedChannels.GetChannelList(), metaDataChannelTree, location)) {
			log.warn("Received no data. Assuming end of channel.");
			changeStateSafe(STATE_STOPPED);
			return;
 		}
 		
 		preFetchData(location+playbackRate, playbackRate);
 		
		channelManager.postData(getmap);					
		
		//log.debug("Playing back " + playbackRate + " seconds of data for " + channelList.length + " channels at location " + formatDate(location) + ".");
		
		double playbackRate = this.playbackRate; //FIXME not really needed anymore
		double playbackDuration = playbackRate;
		double playbackRefreshRate = PLAYBACK_REFRESH_RATE;
		double playbackStepTime = playbackRate * playbackRefreshRate;
		long playbackSteps = (long)(playbackDuration / playbackStepTime);
		
		double locationStartTime = location;
		long playbackStartTime = System.currentTimeMillis();
		
		int i = 0;
		while (i<playbackSteps && stateChangeRequests.size() == 0 && updateLocation == -1 && updateTimeScale == -1 && updatePlaybackRate == -1) {			
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
	
  // LJM made public to prefetch from ControlPanel. 
	public void preFetchData(final double location, final double duration) {
		preFetchChannelMap = null;
		preFetchDone = false;

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
				changeStateSafe(STATE_STOPPED);
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
			TimeListener timeListener = (TimeListener)timeListeners.get(i);
			try {
				timeListener.postTime(location);
			} catch (Exception e) {
				log.error("Failed to post time to " + timeListener + ".");
				e.printStackTrace();
			}
		}
	}	
			
	private void notifyStateListeners(int state, int oldState) {
		for (int i=0; i<stateListeners.size(); i++) {
			StateListener stateListener = (StateListener)stateListeners.get(i);
			stateListener.postState(state, oldState);
		}		
	}
	
	private void fireSubscriptionNotification(String channelName) {
		SubscriptionListener subscriptionListener;
		for (int i=0; i<subscriptionListeners.size(); i++) {
			subscriptionListener = (SubscriptionListener)subscriptionListeners.get(i);
			subscriptionListener.channelSubscribed(channelName);
		}
	}
	
	private void fireUnsubscriptionNotification(String channelName) {
		SubscriptionListener subscriptionListener;
		for (int i=0; i<subscriptionListeners.size(); i++) {
			subscriptionListener = (SubscriptionListener)subscriptionListeners.get(i);
			subscriptionListener.channelUnsubscribed(channelName);
		}		
	}
	
	private void firePlaybackRateChanged(double playbackRate) {
		PlaybackRateListener listener;
		for (int i=0; i<playbackRateListeners.size(); i++) {
			listener = (PlaybackRateListener)playbackRateListeners.get(i);
			listener.playbackRateChanged(playbackRate);
		}
	}	
	
	private void fireTimeScaleChanged(double timeScale) {
		TimeScaleListener listener;
		for (int i=0; i<timeScaleChangeListeners.size(); i++) {
			listener = (TimeScaleListener)timeScaleChangeListeners.get(i);
			listener.timeScaleChanged(timeScale);
		}
	}

	
	// Utility (Static) Methods
	
	private static boolean moreData(String[] channels, ChannelTree ctree, double time) {
		double endTime = -1;
		
        Iterator it = ctree.iterator();
		while (it.hasNext()) {
            ChannelTree.Node node = (ChannelTree.Node)it.next();
			String channelName = node.getFullName();
			double channelEndTime = node.getStart() + node.getDuration();
			if (channelEndTime != -1) {
				endTime = Math.max(endTime, channelEndTime);
			}
		}
		
		if (endTime == -1 || time >= endTime) {
			return false;
		} else {
			return true;
		}
	}

	private static boolean isVideo(ChannelTree channelTree, String channelName) {
		if (channelName == null) {
			log.error("Channel name is null for. Can't determine if is video.");
			return false;
		} else if (channelTree == null) {
			log.warn("Haven't received metadata yet, can't determine channel type.");
			return false;
		}
		
		ChannelTree.Node node = channelTree.findNode(channelName);
		if (node == null) {
			log.error("Unable to find channel in metadata.");
			return false;
		} else {
			String mime = node.getMime();
			if (mime != null && mime.equals("image/jpeg")) {
				return true;
			} else if (channelName.endsWith(".jpg")){
				return true;
			} else {
				return false;
			}
		}
	}	
	
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
	
	public int getState() {
		return state;
	}

 	public void monitor() {
    requestStateChange(STATE_MONITORING);
	}
	
	public void play() {
		requestStateChange(STATE_PLAYING);
	}
	
	public void pause() {
		requestStateChange(STATE_STOPPED);
	}
	
	public void exit() {
		requestStateChange(STATE_EXITING);
		
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
    synchronized (updateLocationLock) {
    	updateLocation = location;
    }
	}
    
  public double getRequestedLocation() {
    return updateLocation; 
  }
	
	public double getPlaybackRate() {
		return playbackRate;
	}

	public void setPlaybackRate(final double playbackRate) {
    synchronized (updatePlaybackRateLock) {
    	updatePlaybackRate = playbackRate;
    }
	}
	
	public double getTimeScale() {
		return timeScale;
	}
	
	public void setTimeScale(double timeScale) {
    synchronized (updateTimeScaleLock) {
      updateTimeScale = timeScale;
    }
	}
	
	public boolean subscribe(String channelName, DataListener listener) {
		synchronized (updateSubscriptionRequests) {
			updateSubscriptionRequests.add(new SubscriptionRequest(channelName, listener, true));
		}
		
		return true;
	}

	public boolean unsubscribe(String channelName, DataListener listener) {
		synchronized (updateSubscriptionRequests) {
			updateSubscriptionRequests.add(new SubscriptionRequest(channelName, listener, false));
		}
		
		return true;	
	}

	public boolean isSubscribed(String channelName) {
		return channelManager.isChannelSubscribed(channelName);
	}

	public void addStateListener(StateListener stateListener) {
		stateListener.postState(state, state);
		stateListeners.add(stateListener);
	}

	public void removeStateListener(StateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	public void addTimeListener(TimeListener timeListener) {
		timeListeners.add(timeListener);
		timeListener.postTime(location);
	}
	
	public void removeTimeListener(TimeListener timeListener) {
		timeListeners.remove(timeListener);
	}	

	public void addPlaybackRateListener(PlaybackRateListener listener) {
		listener.playbackRateChanged(playbackRate);
		playbackRateListeners.add(listener);
	}
	
	public void removePlaybackRateListener(PlaybackRateListener listener) {
		playbackRateListeners.remove(listener);
	}

	public void addTimeScaleListener(TimeScaleListener listener) {
		listener.timeScaleChanged(timeScale);
		timeScaleChangeListeners.add(listener);
	}
	
	public void removeTimeScaleListener(TimeScaleListener listener) {
		timeScaleChangeListeners.remove(listener);
	}

	
	// Public Methods
	
 	public void dropData(boolean dropData) {
 		this.dropData = dropData;
 	}
 	
 	public String getRBNBHostName() {
 		return rbnbHostName;
 	}
 	
 	public void setRBNBHostName(String rbnbHostName) {
 		this.rbnbHostName = rbnbHostName;
 	}
 	
 	public int getRBNBPortNumber() {
 		return rbnbPortNumber;
 	}
 	
 	public void setRBNBPortNumber(int rbnbPortNumber) {
 		this.rbnbPortNumber = rbnbPortNumber;
 	}
 	
 	public String getRBNBConnectionString() {
 		return rbnbHostName + ":" + rbnbPortNumber;
 	}

	public boolean isConnected() {
		return sink != null;	
	}
		
	public void connect() {
		requestStateChange(STATE_STOPPED);
	}
	
	public void disconnect() {
		requestStateChange(STATE_DISCONNECTED);
	}
	
	public void reconnect() {
		requestStateChange(STATE_DISCONNECTED);
    requestStateChange(STATE_STOPPED);
	}
	
	public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
		subscriptionListeners.add(subscriptionListener);
	}

	public void removeSubscriptionListener(SubscriptionListener subscriptionListener) {
		subscriptionListeners.remove(subscriptionListener);
	}
  
  /**
    * LJM An accessor method to get the private @see CahnnelMap variable.
    * implemented for use by the markerPanel in ControlPanel.
    */
  public ChannelMap getChannelMap () {
    return requestedChannels;
  }
  
  /**
    * LJM An accessor method to get the @see com.rbnb.sapi.Sink
    * @return sink private variable.
    */
  public Sink getSink () {
    return this.sink;
  }

	
	//Message Methods
	
	private void fireErrorMessage(String errorMessage) {
		for (int i=0; i<messageListeners.size(); i++) {
			MessageListener messageListener = (MessageListener)messageListeners.get(i);
			messageListener.postError(errorMessage);
		}
	}
	
	private void fireStatusMessage(String statusMessage) {
		for (int i=0; i<messageListeners.size(); i++) {
			MessageListener messageListener = (MessageListener)messageListeners.get(i);
			messageListener.postStatus(statusMessage);
		}		
	}
	
	public void addMessageListener(MessageListener messageListener) {
		messageListeners.add(messageListener);
	}
	
	public void removeMessageListener(MessageListener messageListener) {
		messageListeners.remove(messageListener);
	}
	
	// Connection Listener Methods
	
	private void fireConnecting() {
		for (int i=0; i<connectionListeners.size(); i++) {
			ConnectionListener connectionListener = (ConnectionListener)connectionListeners.get(i);
			connectionListener.connecting();
		}
	}
	
	private void fireConnected() {
		for (int i=0; i<connectionListeners.size(); i++) {
			ConnectionListener connectionListener = (ConnectionListener)connectionListeners.get(i);
			connectionListener.connected();
		}		
	}
	
	private void fireConnectionFailed() {
		for (int i=0; i<connectionListeners.size(); i++) {
			ConnectionListener connectionListener = (ConnectionListener)connectionListeners.get(i);
			connectionListener.connectionFailed();
      log.info("Sent error to listener.");
		}		
	}
	
	public void addConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.add(connectionListener);
	}
	
	public void removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}
    
    
  //Public Metadata Methods
    
  public MetadataManager getMetadataManager() {
    return metadataManager; 
  }
  
  public Channel getChannel(String channelName) {
    return metadataManager.getChannel(channelName); 
  }
  
  public void updateMetadata() {
    metadataManager.updateMetadataBackground(); 
  }
  
  public void channelTreeUpdated(ChannelTree ctree) {
    metaDataChannelTree = ctree;
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
				stateString = "real time";
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
	
	class SubscriptionRequest {
		private String channelName;
		private DataListener listener;
		private boolean isSubscribe;
		
		public SubscriptionRequest(String channelName, DataListener listener, boolean isSubscribe) {
			this.channelName = channelName;
			this.listener = listener;
			this.isSubscribe = isSubscribe;
		}
		
		public String getChannelName() {
			return channelName;
		}
		
		public DataListener getListener() {
			return listener;
		}
		
		public boolean isSubscribe() {
			return isSubscribe;
		}
	}
}

