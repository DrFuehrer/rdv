/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.rbnb;

import java.util.Iterator;

import com.rbnb.sapi.ChannelTree;

/**
 * A collection of RBNB helper methods.
 * 
 * @author Jason P.Hanley
 */
public class RBNBHelper {
  /**
   * Calculate the start and end time based on the currently subscribed channels
   * time bounds. If no channels are subscribed all available channels will be
   * used for this calculation.
   */
  public static TimeRange getChannelsTimeRange() {
    double startTime = -1;
    double endTime = -1;
    
    RBNBController rbnb = RBNBController.getInstance();
    ChannelTree channelTree = rbnb.getChannelTree();
    
    if (channelTree == null) {
      startTime = 0;
      endTime = Double.MAX_VALUE;
    } else {
      boolean hasSubscribedChannels = rbnb.hasSubscribedChannels();
      
      startTime = -1;
      endTime = -1;
  
      // get the time bounds for all channels
      Iterator<?> it = channelTree.iterator();
      while (it.hasNext()) {
        ChannelTree.Node node = (ChannelTree.Node)it.next();
        ChannelTree.NodeTypeEnum type = node.getType();
        if (type != ChannelTree.CHANNEL) {
          continue;
        }
        
        String channelName = node.getFullName();
        if (rbnb.isSubscribed(channelName) || !hasSubscribedChannels) {
          double channelStart = node.getStart();
          double channelDuration = node.getDuration();
          double channelEnd = channelStart+channelDuration;
          if (startTime == -1 || channelStart < startTime) {
            startTime = channelStart;
          }
          if (endTime == -1 || channelEnd > endTime) {
            endTime = channelEnd;
          }
        }
      }
      
      if (startTime == -1 || endTime == -1) {
        startTime = 0;
        endTime = Double.MAX_VALUE;      
      }
    }
    
    return new TimeRange(startTime, endTime);
  }
}