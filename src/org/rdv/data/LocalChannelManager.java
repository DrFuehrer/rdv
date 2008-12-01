/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2008 Palta Software
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

package org.rdv.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.rbnb.RBNBController;

import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.LocalChannelMap;

/**
 * A class to manage instances of <code>LocalChannel</code>.
 * 
 * @author Jason P. Hanley
 * @see    LocalChannel
 */
public class LocalChannelManager {
  
  /** the log for this class */
  private static Log log = LogFactory.getLog(LocalChannelManager.class.getName());
  
  /** the single instance for this class */
  private static LocalChannelManager instance;
  
  /** the list of local channels, iterators over this must be synchronized */
  private final List<LocalChannel> channels;
  
  /**
   * Gets the instance of this class
   * 
   * @return  the instance of this class
   */
  public static LocalChannelManager getInstance() {
    if (instance == null) {
      instance = new LocalChannelManager();
    }
    
    return instance;
  }
  
  /**
   * Creates the local channel manager.
   */
  private LocalChannelManager() {
    channels = Collections.synchronizedList(new ArrayList<LocalChannel>());
  }
  
  /**
   * Determines whether any channels have been added.
   * 
   * @return  true if there are channels, false otherwise
   */
  public boolean hasChannels() {
    return !channels.isEmpty();
  }
  
  /**
   * Gets the local channel named <code>channelName</code>. If no such channel
   * with this name exists, null is returned.
   * 
   * @param channelName  the channel name to get
   * @return             the channel with this name or null if none is found
   */
  public LocalChannel getChannel(String channelName) {
    synchronized(channels) {
      for (LocalChannel channel : channels) {
        if (channel.getName().equals(channelName)) {
          return channel;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Gets a list of <code>LocalChannel</code>'s being managed by this class.
   * 
   * @return  a list of local channels
   */
  public List<LocalChannel> getChannels() {
    return Collections.unmodifiableList(channels);
  }
  
  /**
   * Adds <code>channel</code> to this class and starts posting metadata and
   * data to it.
   * 
   * @param channel  the channel to add
   */
  public void addChannel(LocalChannel channel) {
    channels.add(channel);
    
    RBNBController.getInstance().updateMetadata();
  }
  
  /**
   * Removes the <code>LocalChannel</code>'s with <code>channelNames</code> from
   * this class.
   * 
   * @param channelNames  the list of channel to remove
   * @see             LocalChannel#dispose()
   */
  public void removeChannels(List<String> channelNames) {
    synchronized(channels) {
      Iterator<LocalChannel> i = channels.iterator();
      while (i.hasNext()) {
        LocalChannel channel = i.next();
        if (channelNames.contains(channel.getName())) {
          i.remove();
        }
      }
    }
    
    RBNBController.getInstance().updateMetadata();
  }
  
  /**
   * Removes all the <code>LocalChannel</code>'s from this class.
   */
  public void removeAllChannels() {
    channels.clear();
    
    RBNBController.getInstance().updateMetadata();
  }
  
  /**
   * Updates the metadata for all <code>LocalChannel</code>'s being managed by
   * this class.
   * 
   * @param channelMap         the channel map to post the metadata to
   * @param serverChannelTree  the channel tree of server channels
   * @return                   a map of channels to their user data
   */
  public ChannelTree getMetadata(ChannelTree serverChannelTree) {
    LocalChannelMap channelMap = new LocalChannelMap();
    
    synchronized(channels) {
      for (LocalChannel channel : channels) {
        try {
          channel.updateMetadata(channelMap, serverChannelTree);
        } catch (Exception e) {
          log.warn("Error updating metadata for local channel " + channel.getName() + ": " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
    
    try {
      channelMap.mergeLocalData();
    } catch (Exception e) {
      log.warn("Failed to merge local channel metadata: " + e.getMessage());
      e.printStackTrace();
    }
    
    ChannelTree channelTree = ChannelTree.createFromChannelMap(channelMap);

    return channelTree;
  }
  
  /**
   * Updates data for all <code>LocalChannel</code>'s being managed by this
   * class.
   * 
   * @param channelMap  the channel map to post data to
   */
  public void updateData(LocalChannelMap channelMap) {
    synchronized(channels) {
      for (LocalChannel channel : channels) {
        try {
          channel.updateData(channelMap);
        } catch (Exception e) {
          log.warn("Error updating data for local channel " + channel.getName() + ": " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
    
    try {
      channelMap.mergeLocalData();
    } catch (Exception e) {
      log.warn("Failed to merge local channel data with server data: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
}