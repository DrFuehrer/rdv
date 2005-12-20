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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

/**
 * RBNBUtilities is a utility class to provide static methods for dealing with RBNB.
 * <p>
 * Methods are included for dealing with times, channels, and channel maps.
 * 
 * @author   Jason P. Hanley
 * @since    1.2
 */
public final class RBNBUtilities {	
	static Log log = LogFactory.getLog(RBNBUtilities.class.getName());

	
	/**
	 * This class can not be instantiated and it's constructor
	 * always throws an exception.
	 */
	private RBNBUtilities() {
		throw new UnsupportedOperationException("This class can not be instantiated.");
	}
	
	/**
	 * Using the given channel map, finds the start time for the specified channel.
	 * If the channel is not found, -1 is returned.
	 * 
	 * @param channelMap   the <code>ChannelMap</code> containing the times
	 * @param channelName  the name of the channel
	 * @return             the start time for the channel
	 * @since              1.2
	 */
	public static double getStartTime(ChannelMap channelMap, String channelName) {
		int channelIndex = channelMap.GetIndex(channelName);
		if (channelIndex != -1) {
			double start = channelMap.GetTimeStart(channelIndex);
			return start;
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the start time for the given channel map. If the channel map
	 * is empty, -1 is returned.
	 * 
	 * @param channelMap  the <code>ChannelMap</code> containing the times
	 * @return            the start time for all the channels
	 * @see               #getStartTime(ChannelMap, String)
	 * @since             1.2
	 */
	public static double getStartTime(ChannelMap channelMap) {
		double start = Double.MAX_VALUE;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			double channelStart = getStartTime(channelMap, channelName);
			if (channelStart != -1) {
				start = Math.min(channelStart, start);
			}
		}
		
		if (start != Double.MAX_VALUE) {
			return start;
		} else {
			return -1;
		}
	}
	
	/**
	 * Using the given channel map, finds the end time for the specified channel.
	 * If the channel is not found, -1 is returned.
	 * 
	 * @param channelMap   the <code>ChannelMap</code> containing the times
	 * @param channelName  the name of the channel
	 * @return             the end time for the channel
	 * @since              1.2
	 */
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
	
	/**
	 * Returns the end time for the given channel map. If the channel map
	 * is empty, -1 is returned.
	 * 
	 * @param channelMap  the <code>ChannelMap</code> containing the times
	 * @return            the end time for all the channels
	 * @see               #getEndTime(ChannelMap, String)
	 * @since             1.2
	 */
	public static double getEndTime(ChannelMap channelMap) {
		double end = -1;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			double channelEnd = getEndTime(channelMap, channelName);
			if (channelEnd != -1) {
				end = Math.max(channelEnd, end);
			}
		}
		
		return end;
	}
  
  /**
   * Returns a list of sorted children for the root of the Channel Tree.
   * 
   * @param ctree  the chanel tree to find the children in
   * @return       a sorted list of children of the root element
   * @since        1.3
   */
  public static List getSortedChildren(ChannelTree ctree) {
    return getSortedChildren(ctree, true);
  }
  
  /**
   * Returns a list of sorted children for the root of the Channel Tree. If
   * showHiddenChildren is set, children starting with '_' will be omitted.
   * 
   * @param ctree               the chanel tree to find the children in
   * @param showHiddenChildren  include/discard hidden children
   * @return                    a sorted list of children of the root element
   * @since                     1.3
   */
  public static List getSortedChildren(ChannelTree ctree, boolean showHiddenChildren) {
    return getSortedChildren(ctree.rootIterator(), showHiddenChildren);
  }
  
  /**
   * Returns a list of sorted children for this node.
   * 
   * @param node  the parent to find the children
   * @return      a sorted list of children
   * @since       1.3
   */
  public static List getSortedChildren(ChannelTree.Node node) {
    return getSortedChildren(node, true);
  }
  
  /**
   * Returns a list of sorted children for this node. If showHiddenChildren is
   * set, children starting with '_' will be omitted.
   * 
   * @param node                the parent to find the children
   * @param showHiddenChildren  include/discard hidden children
   * @return                    a sorted list of children
   * @since                     1.3
   */
  public static List getSortedChildren(ChannelTree.Node node, boolean showHiddenChildren) {
    return getSortedChildren(node.getChildren().iterator(), showHiddenChildren);
  }  
  
  private static List getSortedChildren(Iterator it, boolean showHiddenChildren) {
    List list = new ArrayList();

    while (it.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)it.next();
      boolean isHidden = node.getName().startsWith("_");
      ChannelTree.NodeTypeEnum nodeType = node.getType();
      if ((showHiddenChildren || !isHidden) &&
          (nodeType == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER ||
           nodeType == ChannelTree.SERVER || nodeType == ChannelTree.SOURCE ||
           nodeType == ChannelTree.PLUGIN)) {
        list.add(node);       
      }
    }
    
    Collections.sort(list, new HumanComparator());

    return list;
  }
  
  private static class HumanComparator implements Comparator {
    private Pattern p;
    
    public HumanComparator() {
      p = Pattern.compile("(\\D*)(\\d+)(\\D*)");  
    }
    
    public int compare(Object o1, Object o2) {      
      String s1 = ((ChannelTree.Node)o1).getName().toLowerCase();
      String s2 = ((ChannelTree.Node)o2).getName().toLowerCase();
      
      if (s1.equals(s2)) {
        return 0;  
      }
      
      Matcher m1 = p.matcher(s1);
      Matcher m2 = p.matcher(s2);
      
      if (m1.matches() && m2.matches() &&
          m1.group(1).equals(m2.group(1)) &&
          m1.group(3).equals(m2.group(3))) {
        long l1 = Long.parseLong(m1.group(2));
        long l2 = Long.parseLong(m2.group(2));
        return l1<l2?-1:1;
      } else {
        return s1.compareTo(s2);
      }
    }    
  }  

  /**
   * Make a guess at the mime type for a channel that has not specified one.
   * 
   * @param mime         the original mime type
   * @param channelName  the name of the channel
   * @return             the (possibly) modified mime type
   * @since              1.3
   */
  public static String fixMime(String mime, String channelName) {
    if (mime != null) {
      return mime;
    }
    
    if (channelName.endsWith(".jpg")) {
      mime = "image/jpeg";
    } else if (channelName.startsWith("_Log")) {
      mime = "text/plain";
    } else {
      mime = "application/octet-stream";
    }
    return mime;
  }
  
  /**
   * Returns a list of all the names of the channels in the channel tree.
   * 
   * @param ctree  the channel tree
   * @return       a list of channel names
   * @sicne        1.3
   */
  public static List getAllChannels(ChannelTree ctree) {
    ArrayList channels = new ArrayList();
    Iterator it = ctree.iterator();
    while (it.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)it.next();
      if (node.getType() == ChannelTree.CHANNEL) {
        channels.add(node.getFullName());
      }
    }
    return channels;
  }
  
  /**
   * Returns all the names of children of this node that are channels.
   * 
   * @param source  the source node
   * @return        a list of channel names
   * @since         1.3
   */
  public static List getChildChannels(ChannelTree.Node source) {
    ArrayList channels = new ArrayList();
    Iterator children = source.getChildren().iterator();
    while (children.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)children.next();
      if (node.getType() == ChannelTree.CHANNEL) {
        channels.add(node.getFullName());
      }
    }
    return channels;
  }

}
