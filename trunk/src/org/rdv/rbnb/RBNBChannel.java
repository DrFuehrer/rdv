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

import org.rdv.data.Channel;

import com.rbnb.sapi.ChannelTree.Node;

/**
 * A class to describe a channel containing data and the metadata associated with it.
 * 
 * @author  Jason P. Hanley
 */
public class RBNBChannel extends Channel {

  /**
   * Construct a channel with a name and assigning the mime type and
   * unit as metadata.
   * 
   * @param node          The channel tree metadata node
   * @param userMetadata  The user metadata string (tab or comma delimited)
   * @since               1.3
   */
  public RBNBChannel(Node node, String userMetadata) {
    super(node.getFullName());
    
    // a key to signify that this is a server channel
    setMetadata("server");

    // copy metadata from the node
    copyMetadataFromNode(node);
    
    // parse metadata from the user string
    parseUserMetadata(userMetadata);
    
    // set the units property from the metadata map
    setUnit(getMetadata("units"));
  }

  /**
   * Copies metadata from the <code>node</code>. This includes the mime, start,
   * duration, and size.
   * 
   * @param node  the node to copy metadata from
   */
  private void copyMetadataFromNode(Node node) {
    // set the mime from the node
    setMime(node);

    // set other properties read from the node
    setMetadata("start", Double.toString(node.getStart()));
    setMetadata("duration", Double.toString(node.getDuration()));
    setMetadata("size", Integer.toString(node.getSize()));
  }
  
  /**
   * Sets the mime metadata value to the value from the <code>node</code>.
   * If the node has no mime set, it will be guessed based on channel naming
   * conventions. If no mime can be guessed,
   * <code>application/octet-stream</code> will be assumed.
   * 
   * @param node  the node to set the mime from
   */
  private void setMime(Node node) {
    String mime = node.getMime();
    if (mime == null) {
      String channelName = getName();
      if (channelName.endsWith(".jpg")) {
        mime = "image/jpeg";
      } else if (channelName.contains("_Log/")) {
        mime = "text/plain";
      } else {
        mime = "application/octet-stream";
      }
    }
    
    setMetadata("mime", mime);    
  }
  
  /**
   * Parses the <code>userMetadata</code> string and sets metadata key/value
   * pairs based on it. The entries are separated by a comma and the key/value
   * pairs are separated by an equals sign. Entries without a value will be
   * added with the value set to an empty string. The metadata string should
   * look like this:
   * <p>
   * <pre>key1=value1,key2,key3=value3,...</pre>
   * 
   * @param userMetadata  metadata key/value pairs in a string
   */
  private void parseUserMetadata(String userMetadata) {
    if (userMetadata != null && userMetadata.length() > 0) {
      String[] userMetadataTokens = userMetadata.split(",");
      for (int j = 0; j < userMetadataTokens.length; j++) {
        String[] tokens = userMetadataTokens[j].split("=");
        String key = tokens[0].trim();
        String value;
        
        if (tokens.length == 2) {
          value = tokens[1].trim();
        } else {
          value = "";
        }
        
        setMetadata(key, value);
      }
    }    
  }

}