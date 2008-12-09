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

/**
 * A channel and its metadata.
 * 
 * @author Jason P. Hanley
  */
public class DataChannel {
  /** The name of the channel */
  private final String name;
  
  /** The unit of the channel */
  private String unit;

  /**
   * Create a channel.
   * 
   * @param name  the name of the channel
   */
  public DataChannel(String name) {
    this(name, null);
  }
  
  /**
   * Create a channel.
   * 
   * @param name  the name of the channel
   * @param unit         the unit for the channel
   */
  public DataChannel(String name, String unit) {
    if (name == null) {
      throw new IllegalArgumentException("Null channel name argument.");
    }
    
    this.name = name;
    this.unit = unit;
  }
  
  /**
   * Get the name of the channel.
   * 
   * @return  the name of the channel
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get the unit for the channel.
   * 
   * @return  the unit for the channel, or null if no unit was set
   */
  public String getUnit() {
    return unit;
  }
  
  /**
   * Set the unit for the channel.
   * 
   * @param unit  the unit for the channel
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }
  
  /**
   * Return a string representation of this channel.
   * 
   * @return  the channel name
   */
  public String toString() {
    return name;
  }
}
