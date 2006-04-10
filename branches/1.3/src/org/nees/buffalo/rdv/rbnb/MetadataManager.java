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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/rbnb/MetadataManager.java $
 * $Revision: 319 $
 * $Date: 2005-11-16 12:13:34 -0800 (Wed, 16 Nov 2005) $
 * $Author: jphanley $
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
public class MetadataManager {
    
  static Log log = LogFactory.getLog(MetadataManager.class.getName());
    
  private RBNBController rbnbController;

  private ArrayList metadataListeners;
  private boolean updatingMetadata;
  
  private HashMap channels;
  
  private ChannelTree ctree;
    
  public MetadataManager(RBNBController rbnbController) {
    this.rbnbController = rbnbController;

    metadataListeners =  new ArrayList();
    addMetadataListener(rbnbController);
        
    updatingMetadata = false;
		
    channels = new HashMap();
		
    ctree = ChannelTree.EMPTY_TREE;
  }   
	
  public boolean updateMetadataBackground() {
    if (updatingMetadata) return true;
    
    new Thread(new Runnable() {
      public void run() {
        updateMetadata();
      }
    }, "MetadataManager").start();

    return true;
  }
	
  public synchronized boolean updateMetadata() {
    if (updatingMetadata) return true;

    updatingMetadata = true;

    log.info("Updating channel listing.");

    Sink metadataSink = new Sink();
    try {
      metadataSink.OpenRBNBConnection(rbnbController.getRBNBConnectionString(), "RDVMetadata");
    } catch (SAPIException e) {
      log.error("Failed to connect to RBNB server.");
      metadataSink.CloseRBNBConnection();
      updatingMetadata = false;
      return false;   
    }

    //clear channel metadata objects
    channels.clear();

    //create metadata channel tree
    ctree = getChannelTree(metadataSink);

    metadataSink.CloseRBNBConnection();

    fireMetadataUpdated(ctree);
		
    updatingMetadata = false;
		
    return true;
  }
    
  private ChannelTree getChannelTree(Sink sink) {
    return getChannelTree(sink, null);
  }

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
  
  public synchronized ChannelTree getMetadataChannelTree() {
    return ctree;
  }
	
  public synchronized Channel getChannel(String channelName) {
    return (Channel)channels.get(channelName);
  }   

  public void addMetadataListener(MetadataListener listener) {
    metadataListeners.add(listener);
  }

  public void removeMetadataListener(MetadataListener listener) {
    metadataListeners.remove(listener);
  }

  private void fireMetadataUpdated(ChannelTree channelTree) {
    MetadataListener listener;
    for (int i=0; i<metadataListeners.size(); i++) {
      listener = (MetadataListener)metadataListeners.get(i);
      listener.channelTreeUpdated(channelTree);
    }
  }
}
