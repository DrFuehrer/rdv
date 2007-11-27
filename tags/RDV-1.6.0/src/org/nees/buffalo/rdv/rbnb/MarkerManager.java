/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.rbnb;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nees.rbnb.DataTurbine;
import org.nees.rbnb.marker.EventMarker;

import com.rbnb.sapi.ChannelMap;

/**
 * A class to manage available event markers and put new markers on the server.
 * 
 * @author Jason P. Hanley
 */
public class MarkerManager implements DataListener {
  private String rbnbSourceName = "_Events";
  private String rbnbChannel = "EventsChannel";
  
  private RBNBController rbnbController;
  
  private List<EventMarker> markers;
  
  private List<EventMarkerListener> markerListeners;

  /**
   * Create the marker manager and starts listening for new markers from the
   * metadata manager.
   * 
   * @param rbnbController  the rbnb interface
   */
  public MarkerManager(RBNBController rbnbController) {
    super();
    
    try {
      InetAddress addr = InetAddress.getLocalHost();
      String hostname = addr.getHostName();
      rbnbSourceName += "@" + hostname;
    } catch (UnknownHostException e) {}
    
    this.rbnbController = rbnbController;
    
    markers = new ArrayList<EventMarker>();
    
    markerListeners = new ArrayList<EventMarkerListener>();
    
    rbnbController.getMetadataManager().addMarkerListener(this);
  }

  /**
   * Called when new markers are available.
   * 
   * @param channelMap  the channel map containing the marker data.
   */
  public void postData(ChannelMap channelMap) {
    if (channelMap == null) {
      markers.clear();
      fireMarkersCleared();
      return;
    }
    
    if (channelMap.NumberOfChannels() == 0) {
      return;
    }
    
    for (int i = 0; i < channelMap.NumberOfChannels(); i++) {
      String channelName = channelMap.GetName(i);
      int channelIndex = channelMap.GetIndex(channelName);
      String[] markerData = channelMap.GetDataAsString(channelIndex);
      for (String markerString : markerData) {
        EventMarker marker = new EventMarker();
        try {
          marker.setFromEventXml(markerString);
        } catch (Exception e) {
          continue;
        }
        
        if (!markers.contains(marker)) {
          markers.add(marker);
          Collections.sort(markers);
          
          fireNewMarker(marker);
        }
      }
    }    
  }
  
  /**
   * Retruns a list of all event markers.
   * 
   * @return  a list of event markers
   */
  public List<EventMarker> getMarkers() {
    return Collections.unmodifiableList(markers);
  }
  
  /**
   * Puts the event marker on the server.
   * 
   * @param eventMarker  the event marker to put
   * @throws Exception   if the marker could not be sent to the server
   */
  public void putMarker(EventMarker eventMarker) throws Exception {    
    DataTurbine markerSource = new DataTurbine (rbnbSourceName);
    markerSource.setServerName(rbnbController.getRBNBHostName());
    markerSource.open();
    markerSource.putMarker(eventMarker, rbnbChannel);
    markerSource.closeAndKeep();
    
    rbnbController.updateMetadata();
  }
  
  /**
   * Add a listener for new event markers.
   * 
   * @param listener  the event marker listener to add
   */
  public void addMarkerListener(EventMarkerListener listener) {
    markerListeners.add(listener);
  }
  
  /**
   * Remove a listener for new event markers.
   * 
   * @param listener  the event marker listener to remove
   */
  public void removeMarkerListener(EventMarkerListener listener) {
    markerListeners.remove(listener);
  }
  
  /**
   * Send the new event marker to the registered listeners.
   * 
   * @param marker  the new event marker to send
   */
  protected void fireNewMarker(EventMarker marker) {
    for (EventMarkerListener listener : markerListeners) {
      listener.eventMarkerAdded(marker);
    }
  }
  
  /**
   * Send the markers cleared signal to the registered listeners.
   */
  protected void fireMarkersCleared() {
    for (EventMarkerListener listener : markerListeners) {
      listener.eventMarkersCleared();
    }
  }
}
