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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.rbnb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import org.nees.rbnb.marker.NeesEvent;
import org.nees.buffalo.rdv.DataViewer;
/**
 * A class to fetch metadata from the server and post it to listeners. Methods
 * are also included to access metadata for individual channels.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class MetadataManager {

  static Log log = LogFactory.getLog(MetadataManager.class.getName());
    
  private RBNBController rbnbController;

  /**
   * Listeners for metadata updates.
   */
  private ArrayList metadataListeners;
  
  private ArrayList markerListeners;
  
  /**
   * Map of channel objects created from metadata.
   */
  private HashMap channels;
  
  /**
   * Metadata channel tree.
   */
  private ChannelTree ctree;
  
  /**
   * Whether the metadata update thread is running.
   */
  private boolean update;
  
  /**
   * Whether the update thread is sleeping.
   */
  private Boolean sleeping;
  
  /**
   * The thread updating the metadata periodically.
   */
  private Thread updateThread;
  
  /**
   * The time to sleep between each metadata update.
   */
  private static final long updateRate = 10000;

  /**
   * Create the class using the RBNBController for connection information.
   *  
   * @param rbnbController the RBNBController to use
   */
  
  private ChannelMap markerChannelMap;
  
  private static double markersDuration = 0.0; // in seconds
  
  private final double markersStart = 0.0;   // start from begining of time
  
  
  public MetadataManager(RBNBController rbnbController) {
    this.rbnbController = rbnbController;

    metadataListeners =  new ArrayList();
    addMetadataListener(rbnbController);
        
    channels = new HashMap();
		
    ctree = null;
    
    update = false;
    sleeping = new Boolean(false);
    updateThread = null;
    
    markerListeners = new ArrayList();
  }   

  /**
   * Triggers a metadata update. This will return immediately and the metadata
   * will be posted to the listeners when available.
   */
  public void updateMetadataBackground() {
    if (update) {
      synchronized(sleeping) {
        if (sleeping) {
          updateThread.interrupt();   
        }
      }
    }
  }

  /**
   * Start the thread that periodically updates the metadata.
   */
  public void startUpdating() {
    if (update == true) {
      return;
    }
    
    final Sink metadataSink = new Sink();
    try {
      metadataSink.OpenRBNBConnection(rbnbController.getRBNBConnectionString(), "RDVMetadata");
    } catch (SAPIException e) {
      log.error("Failed to connect to RBNB server: " + e.getMessage());
      e.printStackTrace();
      return;
    }    

    updateMetadata(metadataSink);

    updateThread = new Thread("MetadataManager") {
      public void run() {
        log.info("RBNB Metadata thread is starting.");
        
        updateMetadataThread(metadataSink);
        
        metadataSink.CloseRBNBConnection();
        
        channels.clear();
        ctree = null;
        
        log.info("RBNB Metadata thread is stopping.");        
      }
    };
    update = true;    
    updateThread.start();
  }
  
  /**
   * Stops the metadata update thread.
   */
  public void stopUpdating() {
    update = false;
    if (updateThread != null && sleeping) {
      updateThread.interrupt();
    }
  }  
  
  /**
   * Updates the metadata and sleeps <code>updateRate</code>. The
   * <code>Sink</code> must be opened and will not be closed when this method
   * exits.
   * 
   * @param metadataSink The sink connection to the RBNB server
   * @see #updateMetadata(Sink)
   */
  private void updateMetadataThread(Sink metadataSink) {
    while (update) {
      synchronized(sleeping) {
        sleeping = true;
        try { Thread.sleep(updateRate); } catch (InterruptedException e) {}
        sleeping = false;
      }

      if (update) {
        updateMetadata(metadataSink);
      }
    }
    
    fireMetadataUpdated(null);
  }
  
  public void getMarkerEventsTimes(ChannelTree ceeTree) {
      
      log.info("getMarkerEventsTimes()");
      if (ceeTree == null) {
        return;
      }
    
      Iterator it = ceeTree.iterator ();
      log.debug("tree has " + it.getClass());
      ChannelTree.Node node;
      String channelMime = null;
      // Go through the entire channel tree and pick out the events channels
      while (it.hasNext ()) {
        node = (ChannelTree.Node)it.next ();
        channelMime = node.getMime ();
        // get the latest times

        if (channelMime != null &&
            channelMime.compareToIgnoreCase (NeesEvent.MIME_TYPE) == 0) {

          log.info("Marker time for " + node.getFullName() + " is " + DataViewer.formatDate(node.getStart()));
        }
      }
  }
  
  private ChannelMap getMarkerChannels(Sink sink) {

    ChannelMap markerChannels = new ChannelMap();
    
    try {
      ChannelMap cMap = new ChannelMap();
      sink.RequestRegistration(cMap);

      cMap = sink.Fetch(-1, cMap);
      String mimeType;
      for (int i=0; i<cMap.NumberOfChannels(); i++) {
        mimeType = cMap.GetMime(i);
        log.info("mime=" + mimeType + " name=" + cMap.GetName(i) + " type=" + cMap.GetType(i) + " userInfo=" + cMap.GetUserInfo(i) );
        if ( mimeType == NeesEvent.MIME_TYPE) {
          
          markerChannels.Add(cMap.GetName(i));
          log.info(cMap.GetName(i) + " was added to markerchannels");
        }
        
      }
      
    } catch (SAPIException e) {
      log.error("Failed to retrieve Marker channels: " + e.getMessage() + ".");

    }  
    
    return markerChannels;
  }
  
  
  /**
   * Updates the metadata and posts it to listeners. It also notifies all
   * threads waiting on this object.
   * 
   * @param metadataSink the RBNB sink to use for the server connection
   */
  private synchronized void updateMetadata(Sink metadataSink) {
      log.info("Updating channel listing at " + DataViewer.formatDate(System.currentTimeMillis ()));      
    
    //create metadata channel tree
    try {

      if (markerChannelMap != null)
        markerChannelMap.Clear();
      
      markerChannelMap = new ChannelMap();
    
      HashMap newChannels = new HashMap();
      ctree = getChannelTree(metadataSink, newChannels);
 
//      ChannelMap markersMap;
//      markersMap = getMarkerChannels(metadataSink);
      
      channels = newChannels;
    } catch (SAPIException e) {
      log.error("Failed to update metadata: " + e.getMessage() + ".");

      if (!metadataSink.VerifyConnection()) {
    	  log.error("Metadata RBNB connection is severed, try to reconnect to " + rbnbController.getRBNBConnectionString() + ".");
    	  metadataSink.CloseRBNBConnection();
    	  try {
    	    metadataSink.OpenRBNBConnection(rbnbController.getRBNBConnectionString(), "RDVMetadata");
    	  } catch (SAPIException error) {
    	    log.error("Failed to connect to RBNB server: " + error.getMessage());
    	    error.printStackTrace();
    	  }
      }     
      
      return;
    }
    
    //notify metadata listeners
    fireMetadataUpdated(ctree);
    
    //notify methods waiting on initial metadata fetch
    notifyAll();    
  }

  /**
   * Get the metadata channel tree for the whole server. This will populate the
   * channel map with channel objects derived from the metadata.
   * 
   * @param sink the sink connection to the RBNB server
   * @param channels the map to populate with channel objects
   * @return the metadata channel tree
   * @throws SAPIException if a server error occurs
   */
  private ChannelTree getChannelTree(Sink sink, HashMap channels) throws SAPIException {
    return getChannelTree(sink, null, channels);
  }

  /**
   * Get the metadata channel tree for the given <code>path</code>. This will
   * populate the channel map with channel objects derived from the metadata.
   * 
   * @param sink sink the sink connection to the RBNB server
   * @param path the path for the desired metadata
   * @param channels the map to populate with channel objects
   * @return the metadata channel tree for the given path
   * @throws SAPIException if a server error occurs
   */
  private ChannelTree getChannelTree(Sink sink, String path, HashMap channels) throws SAPIException {
    ChannelTree ctree = ChannelTree.EMPTY_TREE;

    ChannelMap cmap = new ChannelMap();
    if (path != null) {
      cmap.Add(path + "/...");
    }
    sink.RequestRegistration(cmap);

//    log.info("getChannelTree() - map " + cmap);
    cmap = sink.Fetch(-1, cmap);

    String[] channelData = null;

    ctree = ChannelTree.createFromChannelMap(cmap);
    
    //store user metadata in channel objects
    String[] channelList = cmap.GetChannelList();
    String mimeType;
    log.info("Number of channels: " + channelList.length);
    for (int i=0; i<channelList.length; i++) {
      int channelIndex = cmap.GetIndex(channelList[i]);
      if (channelIndex != -1) {
        ChannelTree.Node node = ctree.findNode(channelList[i]);
        String userMetadata = cmap.GetUserInfo(channelIndex);
        Channel channel = new Channel(node, userMetadata);
        channels.put(channelList[i], channel);
        /* do the marker stuff */
        mimeType = node.getMime();
        log.info("MIMETYPE: " + mimeType);
        if (mimeType != null && mimeType.compareToIgnoreCase(NeesEvent.MIME_TYPE) == 0) {
          markerChannelMap.Add(node.getFullName());
          log.info("Channel " + node.getFullName() + " added to Marker's channels");          
        }
        
      }            
    }

    Iterator it = ctree.iterator();
    int index;
    while (it.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)it.next();
//      mimeType = node.getMime();
//      if (mimeType != null && mimeType.compareToIgnoreCase(NeesEvent.MIME_TYPE) == 0) {
//          index = markerChannelMap.Add(node.getFullName());
//      }
      
      // look for child servers and get their channels      
      if (node.getType() == ChannelTree.SERVER &&
        (path == null || !path.startsWith(node.getFullName()))) {

        ChannelTree childChannelTree = getChannelTree(sink, node.getFullName(), channels);
        ctree = childChannelTree.merge(ctree);
      }
    }
    
    if (markerChannelMap != null && markerChannelMap.GetChannelList().length > 0) {
      markersDuration = (((double)(System.currentTimeMillis ())) / 1000.0);  // current date/time in seconds

      sink.Request(markerChannelMap, markersStart, markersDuration, "absolute");  // request from start of marker channels to now
      markerChannelMap = sink.Fetch(-1, markerChannelMap);
    }
    
    
    return ctree;
  }
  
  /**
   * Return the latest metadata channel tree.
   * 
   * @return the metadata channel tree.
   */
  public ChannelTree getMetadataChannelTree() {
    if (ctree == null) {
      synchronized (this) {
        do {
          try { wait(250); } catch (InterruptedException e) {}
        } while (ctree == null);
      }
    }
    
    return ctree;
  }
	
  /**
   * Return a channel object for the given <code>channelName</code>.
   * 
   * @param channelName the desired channel
   * @return the channel object for the channel name, or null if the channel is
   *         not found
   */
  public Channel getChannel(String channelName) {
    if (ctree == null) {
      synchronized (this) {
        do {
          try { wait(250); } catch (InterruptedException e) {}
        } while (ctree == null);
      }
    }
    
    return (Channel)channels.get(channelName);
  }   

  /**
   * Add a listener for metadata updates.
   * 
   * @param listener the metadata listener
   */
  public void addMetadataListener(MetadataListener listener) {
    metadataListeners.add(listener);
  }
  
  public void addMarkerListener(MarkerDataListener listener) {
    markerListeners.add(listener);
  }

  /**
   * Remove a listener for metadata updates.
   * 
   * @param listener the metadata listener
   */
  public void removeMetadataListener(MetadataListener listener) {
    metadataListeners.remove(listener);
  }
  
  public void removeMarkerListener(MarkerDataListener listener) {
    markerListeners.remove(listener);
  }

  /**
   * Post metadata updates to the subscribed listeners.
   * 
   * @param channelTree the new metadata channel tree
   */
  private void fireMetadataUpdated(ChannelTree channelTree) {
    MetadataListener listener;
    MarkerDataListener markerListener;
    
    for (int i=0; i<metadataListeners.size(); i++) {
      listener = (MetadataListener)metadataListeners.get(i);
//      log.info("calling listener " + listener + "'s channelTreeUpdated");

      listener.channelTreeUpdated(channelTree);
    }
    
    for (int i=0; i<markerListeners.size(); i++) {
      
      markerListener = (MarkerDataListener)markerListeners.get(i);
      markerListener.updateMarkerChannels(this.markerChannelMap);
      
    }
    
  }
}
