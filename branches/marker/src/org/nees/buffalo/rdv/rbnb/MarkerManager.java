/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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
import java.util.List;

import org.nees.rbnb.DataTurbine;
import org.nees.rbnb.marker.NeesEvent;

import com.rbnb.sapi.ChannelMap;

/**
 * A class to manage available event markers and put new markers on the server.
 * 
 * @author Jason P. Hanley
 */
public class MarkerManager implements MarkerDataListener {
  private String rbnbSourceName = "_Events";
  private String rbnbChannel = "EventsChannel";
  
  private RBNBController rbnbController;

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
    
    rbnbController.getMetadataManager().addMarkerListener(this);
  }

  /**
   * Called when new markers are available.
   * 
   * @param markerChannelMap  the channel map containing the marker data.
   */
  public void updateMarkerChannels(ChannelMap markerChannelMap) {}
  
  /**
   * Retruns a list of all event markers.
   * 
   * @return  a list of event markers
   */
  public List<NeesEvent> getMarkers() {
    return null;
  }
  
  /**
   * Puts the event marker on the server.
   * 
   * @param eventMarker  the event marker to put
   * @throws Exception   if the marker could not be sent to the server
   */
  public void putMarker(NeesEvent eventMarker) throws Exception {    
    DataTurbine markerSource = new DataTurbine (rbnbSourceName);
    markerSource.setServerName(rbnbController.getRBNBHostName());
    markerSource.open();
    markerSource.putMarker(eventMarker, rbnbChannel);
    markerSource.closeAndKeep();
    
    rbnbController.updateMetadata();
  }

}
