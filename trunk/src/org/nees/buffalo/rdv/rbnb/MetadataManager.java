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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.rbnb.marker.EventMarker;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

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
   * The name of the RBNB sink used to get metadata
   */
  private String rbnbSinkName = "RDVMetadata";

  /**
   * Listeners for metadata updates.
   */
  private ArrayList<MetadataListener> metadataListeners;
  
  /**
   * Listeners for markers.
   */
  private ArrayList<DataListener> markerListeners;
  
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
  public MetadataManager(RBNBController rbnbController) {
    this.rbnbController = rbnbController;

    try {
      InetAddress addr = InetAddress.getLocalHost();
      String hostname = addr.getHostName();
      rbnbSinkName += "@" + hostname;
    } catch (UnknownHostException e) {}

    metadataListeners =  new ArrayList<MetadataListener>();
    addMetadataListener(rbnbController);
        
    channels = new HashMap();
		
    ctree = null;
    
    update = false;
    sleeping = new Boolean(false);
    updateThread = null;
    
    markerListeners = new ArrayList<DataListener>();
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
      metadataSink.OpenRBNBConnection(rbnbController.getRBNBConnectionString(), rbnbSinkName);
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
    fireMarkersUpdated(null);
  }

  
  /**
   * Updates the metadata and posts it to listeners. It also notifies all
   * threads waiting on this object.
   * 
   * @param metadataSink the RBNB sink to use for the server connection
   */
  private synchronized void updateMetadata(Sink metadataSink) {
    log.info("Updating channel listing at " + DataViewer.formatDate(System.currentTimeMillis ()));      

    HashMap newChannels = new HashMap();

    try {
      //create metadata channel tree
      ctree = getChannelTree(metadataSink, newChannels); 
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
    
    channels = newChannels;
    
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
    
    ChannelMap markerChannelMap = new ChannelMap();

    ChannelMap cmap = new ChannelMap();
    if (path != null) {
      cmap.Add(path + "/...");
    }
    sink.RequestRegistration(cmap);

    cmap = sink.Fetch(-1, cmap);

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
        
        //look for marker channels
        mimeType = node.getMime();
        if (mimeType != null && mimeType.compareToIgnoreCase(EventMarker.MIME_TYPE) == 0) {
          markerChannelMap.Add(node.getFullName());         
        }
      }            
    }

    Iterator it = ctree.iterator();
    while (it.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)it.next();
      
      // look for child servers and get their channels      
      if (node.getType() == ChannelTree.SERVER &&
        (path == null || !path.startsWith(node.getFullName()))) {

        ChannelTree childChannelTree = getChannelTree(sink, node.getFullName(), channels);
        ctree = childChannelTree.merge(ctree);
      }
    }
    
    if (markerChannelMap.NumberOfChannels() > 0) {
      double markersDuration = System.currentTimeMillis()/1000d;

      //request from start of marker channels to now
      sink.Request(markerChannelMap, 0, markersDuration, "absolute");
      
      markerChannelMap = sink.Fetch(-1, markerChannelMap);
      
      //notify marker listeners
      fireMarkersUpdated(markerChannelMap);      
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
  
  /**
   * Add a listener for marker data.
   * 
   * @param listener  the marker listener to add
   */
  public void addMarkerListener(DataListener listener) {
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
  
  /**
   * Remove a listener for marker data.
   * 
   * @param listener  the marker listener to remove
   */
  public void removeMarkerListener(DataListener listener) {
    markerListeners.remove(listener);
  }

  /**
   * Post metadata updates to the subscribed listeners.
   * 
   * @param channelTree the new metadata channel tree
   */
  private void fireMetadataUpdated(ChannelTree channelTree) {
    for (MetadataListener listener : metadataListeners) {
      listener.channelTreeUpdated(channelTree);
    }
  }
  
  /**
   * Post marker data to subscribed listeners.
   * 
   * @param cmap  the channel map containing the marker data
   */
  protected void fireMarkersUpdated(ChannelMap cmap) {
    for (DataListener listener : markerListeners) {
      listener.postData(cmap);
    }
  }
}
