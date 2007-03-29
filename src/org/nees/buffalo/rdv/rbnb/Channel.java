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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.rbnb.sapi.ChannelTree;

/**
 * A class to describe a channel containing data and the metadata associated with it.
 * 
 * @author  Jason P. Hanley
 * @since   1.1
 */
public class Channel {

	/**
	 * The full name of the channel
	 */
	String name;
	
    HashMap metadata;
	
	/**
	 * Construct a channel with a name and assigning the mime type and
	 * unit as metadata.
	 * 
	 * @param node          The channel tree metadata node
	 * @param userMetadata  The user metadata string (tab or comma delimited)
	 * @since               1.3
	 */
	public Channel(ChannelTree.Node node, String userMetadata) {
		this.name = node.getFullName();
        
        metadata = new HashMap();
        
        metadata.put("mime", node.getMime());
        metadata.put("start", Double.toString(node.getStart()));
        metadata.put("duration", Double.toString(node.getDuration()));
        metadata.put("size", Integer.toString(node.getSize()));
        
        if (userMetadata.length() > 0) {
          String[] userMetadataTokens = userMetadata.split("\t|,");
          for (int j=0; j<userMetadataTokens.length; j++) {
            String[] tokens = userMetadataTokens[j].split("=");
            if (tokens.length == 2) {
              metadata.put(tokens[0].trim(), tokens[1].trim());
            }
          }
        }

	}
	
	/**
	 * Get the short name of the channel.
	 * 
	 * This is the part after the final forward slash (/).
	 * 
	 * @return  the short name of the channel
	 * @since   1.1
	 */
	public String getShortName() {
		return name.substring(name.lastIndexOf("/")+1);
	}
	
	/**
	 * Get the full name of the channel.
	 * 
	 * @return  the full name of the channel.
	 * @since   1.1
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the parent of the channel.
	 * 
	 * This is the part before the final forward slash (/).
	 * 
	 * @return  the parent of the channel
	 * @since   1.1
	 */
	public String getParent() {
		return name.substring(0, name.lastIndexOf("/"));
	}
	
    /**
     * Return the metatadata string associated with the given key.
     * 
     * @param key  the key corresponding to the desired metadata string
     * @return     the metadata string or null if the key was not found
     * @since      1.3
     */
    public String getMetadata(String key) {
      return (String)metadata.get(key);
    }
    
    /**
     * Return a string with the channel name and all metadata.
     * 
     * @return  a string representation of the channel and its metadata
     */
    public String toString() {
      StringBuilder string = new StringBuilder(name);
      if (metadata.size() > 0) {
        string.append(": ");
        Set keys = metadata.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
          String key = (String)it.next();
          string.append(key + "=" + metadata.get(key));
          if (it.hasNext()) {
            string.append(", ");
          }
        }
      }
      return string.toString();
    }
}
