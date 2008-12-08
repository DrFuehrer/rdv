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

package org.rdv.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A channel and its metadata.
 * 
 * @author Jason P. Hanley
  */
public class Channel {
  /** The name of the channel */
  private final String name;
  
  /** The unit of the channel */
  private String unit;
  
  /** the start time for the data */
  private double start;
  
  /** the duration of the data */
  private double duration;
  
  /** the metadata map for this channel */
  private final Map<String, String> metadata;

  /**
   * Creates a channel.
   * 
   * @param name  the name of the channel
   */
  public Channel(String name) {
    this(name, null);
  }
  
  /**
   * Creates a channel with a unit.
   * 
   * @param name  the name of the channel
   * @param unit  the unit for the channel
   */
  public Channel(String name, String unit) {
    if (name == null) {
      throw new IllegalArgumentException("Null channel name argument.");
    }
    
    this.name = name;
    this.unit = unit;
    
    metadata = new HashMap<String, String>();
  }
  
  /**
   * Gets the name of the channel.
   * 
   * @return  the name of the channel
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the unit for the channel.
   * 
   * @return  the unit for the channel, or null if no unit was set
   */
  public String getUnit() {
    return unit;
  }
  
  /**
   * Sets the unit for the channel.
   * 
   * @param unit  the unit for the channel
   */
  protected void setUnit(String unit) {
    this.unit = unit;
  }
  
  /**
   * Gets the start time for the data.
   * 
   * @return  the start time
   */
  public double getStart() {
    return start;
  }
  
  /**
   * Sets the start time for the data.
   * 
   * @param start  the new start time
   */
  protected void setStart(double start) {
    this.start = start;
  }
  
  /**
   * Gets the duration of the data.
   * 
   * @return  the duration
   */
  public double getDuration() {
    return duration;
  }
  
  /**
   * Sets the duration of the data.
   * 
   * @param duration  the new duration
   */
  protected void setDuration(double duration) {
    this.duration = duration;
  }
  
  /**
   * Gets the metatadata string associated with the given key.
   * 
   * @param key  the key corresponding to the desired metadata string
   * @return     the metadata string or null if the key was not found
   * @since      1.3
   */
  public String getMetadata(String key) {
    return (String) metadata.get(key);
  }
  
  /**
   * Gets the map of metadata for this channel.
   * 
   * @return  the metadata map
   */
  public Map<String,String> getMetadata() {
    return Collections.unmodifiableMap(metadata);
  }
  
  /**
   * Sets the metadata for <code>key</code> with an empty value.
   * 
   * @param key  the key to the metadata
   */
  protected void setMetadata(String key) {
    setMetadata(key, null);
  }
  
  /**
   * Sets the metadata <code>value</code> for the <code>key</code>
   * 
   * @param key    the key to the metadata
   * @param value  the value of the metadata
   */
  protected void setMetadata(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("Metadata key can't be null.");
    }
    
    if (value == null) {
      value = "";
    }
    
    metadata.put(key, value);
  }

  /**
   * Return a string with the channel name and all metadata.
   * 
   * @return  a string representation of the channel and its metadata
   */
  public String toString() {
    StringBuilder string = new StringBuilder(getName());
    
    if (getUnit() != null) {
      string.append(" (");
      string.append(getUnit());
      string.append(")");
    }
    
    if (metadata.size() > 0) {
      string.append(": ");
      Set<String> keys = metadata.keySet();
      Iterator<String> it = keys.iterator();
      while (it.hasNext()) {
        String key = it.next();
        string.append(key);
        
        String value = metadata.get(key);
        if (value.length() > 0) {
          string.append('=');
          string.append(value);
        }
        
        if (it.hasNext()) {
          string.append(", ");
        }
      }
    }
    
    return string.toString();
  }
  
}