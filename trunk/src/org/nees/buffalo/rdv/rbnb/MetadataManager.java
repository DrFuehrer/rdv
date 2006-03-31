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

/**
 * A class to fetch metadata from the server and post it to listeners. Methods
 * are also included to access metadata for individual channels.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class MetadataManager implements StateListener {

  static Log log = LogFactory.getLog(MetadataManager.class.getName());
    
  private RBNBController rbnbController;

  /**
   * Listeners for metadata updates.
   */
  private ArrayList metadataListeners;
  
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

    metadataListeners =  new ArrayList();
    addMetadataListener(rbnbController);
        
    channels = new HashMap();
		
    ctree = ChannelTree.EMPTY_TREE;
    
    update = false;
    updateThread = null;
  }   

  /**
   * Triggers a metadata update. This will return immediately and the metadata
   * will be posted to the listeners when available.
   */
  public void updateMetadataBackground() {
    if (update) {
      updateThread.interrupt();
    }
  }

  /**
   * Start the thread that periodically updates the metadata.
   */
  private void startUpdating() {
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

    updateThread = new Thread("MetadataManager") {
      public void run() {
        log.info("RBNB Metadata thread is starting.");
        
        try {
          updateMetadataThread(metadataSink);
        } catch (Exception e) {
          log.error("The metadata updater died: " + e.getMessage());
          update = false;
        }
        
        metadataSink.CloseRBNBConnection();
        
        log.info("RBNB Metadata thread is stopping.");        
      }
    };
    updateThread.start();
    update = true;
  }
  
  /**
   * Stops the metadata update thread.
   */
  private void stopUpdating() {
    update = false;
    if (updateThread != null) {
      updateThread.interrupt();
    }
  }  
  
  /**
   * Updates the metadata and sleeps <code>updateRate</code>. The
   * <code>Sink</code> must be opened and will not be closed when this method
   * exits.
   * 
   * @param metadataSink The sink connection to the RBNB server
   */
  private void updateMetadataThread(Sink metadataSink) {
    while (update) {
      if (rbnbController.isConnected()) {
        log.info("Updating channel listing.");

        synchronized (this) {
          //clear channel metadata objects
          channels.clear();
      
          //create metadata channel tree
          ctree = getChannelTree(metadataSink);
        }

        fireMetadataUpdated(ctree);
      }

      try { Thread.sleep(updateRate); } catch (InterruptedException e) {}
    }
  }

  /**
   * Get the metadata channel tree for the whole server.
   * 
   * @param sink the sink connection to the RBNB server
   * @return the metadata channel tree
   */
  private ChannelTree getChannelTree(Sink sink) {
    return getChannelTree(sink, null);
  }

  /**
   * Get the metadata channel tree for the given <code>path</code>.
   * 
   * @param sink sink the sink connection to the RBNB server
   * @param path the path for the desired metadata
   * @return the metadata channel tree for the given path
   */
  private ChannelTree getChannelTree(Sink sink, String path) {
    ChannelTree ctree = ChannelTree.EMPTY_TREE;
    try {
      ChannelMap cmap = new ChannelMap();
      if (path != null) {
        cmap.Add(path + "/...");
      }
      sink.RequestRegistration(cmap);

      cmap = sink.Fetch(-1, cmap);
      ctree = ChannelTree.createFromChannelMap(cmap);
      
      //store user metadata in channel objects
      String[] channelList = cmap.GetChannelList();
      for (int i=0; i<channelList.length; i++) {
        int channelIndex = cmap.GetIndex(channelList[i]);
        if (channelIndex != -1) {
          ChannelTree.Node node = ctree.findNode(channelList[i]);
          String userMetadata = cmap.GetUserInfo(channelIndex);
          Channel channel = new Channel(node, userMetadata);
          channels.put(channelList[i], channel);
        }            
      }
      
      // look for child servers and get their channels
      Iterator it = ctree.iterator();
      while (it.hasNext()) {
        ChannelTree.Node node = (ChannelTree.Node)it.next();
        if (node.getType() == ChannelTree.SERVER &&
           (path == null || !path.startsWith(node.getFullName()))) {
          ChannelTree childChannelTree = getChannelTree(sink, node.getFullName());
          ctree = childChannelTree.merge(ctree);
        }
      }
    
    } catch (SAPIException e) {
      log.error("Error retrieving metadata: " + e.getMessage());
    }

    return ctree;
  }
  
  /**
   * Return the latest metadata channel tree.
   * 
   * @return the metadata channel tree.
   */
  public synchronized ChannelTree getMetadataChannelTree() {
    return ctree;
  }
	
  /**
   * Return a channel object for the given <code>channelName</code>.
   * 
   * @param channelName the desired channel
   * @return the channel object for the channel name, or null if the channel is
   *         not found
   */
  public synchronized Channel getChannel(String channelName) {
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
   * Remove a listener for metadata updates.
   * 
   * @param listener the metadata listener
   */
  public void removeMetadataListener(MetadataListener listener) {
    metadataListeners.remove(listener);
  }

  /**
   * Post metadata updates to the subscribed listeners.
   * 
   * @param channelTree the new metadata channel tree
   */
  private void fireMetadataUpdated(ChannelTree channelTree) {
    MetadataListener listener;
    for (int i=0; i<metadataListeners.size(); i++) {
      listener = (MetadataListener)metadataListeners.get(i);
      listener.channelTreeUpdated(channelTree);
    }
  }

  /**
   * Receives state notifications from the RBNB controller.
   * 
   * This controls the starting and stopping of the periodic metadata update
   * thread. 
   */
  public void postState(int newState, int oldState) {
    if (oldState == RBNBController.STATE_DISCONNECTED &&
        newState != RBNBController.STATE_EXITING &&
        newState != RBNBController.STATE_DISCONNECTED) {
      startUpdating();
    } else if (newState == RBNBController.STATE_DISCONNECTED ||
               newState == RBNBController.STATE_EXITING) {
      stopUpdating();
    }
  }
}
