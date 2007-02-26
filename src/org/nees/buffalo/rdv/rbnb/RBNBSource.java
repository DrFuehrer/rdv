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

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

/**
 * A class to create and manage an RBNB source.
 * 
 * @author Jason P. Hanley
 */
public class RBNBSource {
  /** the default host name of the RBNB server */
  private static final String DEFAULT_HOST = "localhost";
  
  /** the default port number of the RBNB server */
  private static final int DEFAULT_PORT = 3333;
  
  /** the name of the source */
  private final String name;
  
  /** the host name of the RBNB server */
  private final String host;
  
  /** the port number of the RBNB server */
  private final int port;
  
  /** the RBNB source */
  private final Source source;
  
  /** the channel map used to post data */
  private final ChannelMap cmap;
  
  /** flag to see if we have registered the channel metadata */
  private boolean registered;
  
  /** the timestamp of the last piece of data posted */
  private double lastTimestamp;
  
  /**
   * Creates an RBNBSource object with the given name and archive.
   * 
   * @param name            the name of the RBNB source
   * @param archive         the archive size
   * @throws RBNBException  if there is an error creating the source
   */
  public RBNBSource(String name, int archive) throws RBNBException {
    this(name, archive, DEFAULT_HOST);
  }
  
  /**
   * Creates an RBNBSource object with the given name on the specified host.
   * 
   * @param name            the name of the RBNB source
   * @param archive         the archive size
   * @param host            the host name of the RBNB server
   * @throws RBNBException  if there is an error creating the source
   */
  public RBNBSource(String name, int archive, String host) throws RBNBException {
    this(name, archive, host, DEFAULT_PORT);
  }
  
  /**
   * Creates an RBNBSource object with the given name on the specified host and
   * port.
   * 
   * @param name            the name of the RBNB source
   * @param archive         the archive size
   * @param host            the host name of the RBNB server
   * @param port            the port number of the RBNB server
   * @throws RBNBException  if there is an error creating the source
   */
  public RBNBSource(String name, int archive, String host, int port) throws RBNBException {
    this.name = name;
    this.host = host;
    this.port = port;
    
    source = new Source(1, "create", archive);
    cmap = new ChannelMap();
    registered = false;
    lastTimestamp = -1;
    
    try {
      open();
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }
  }
  
  /**
   * Gets the name of the RBNB source.
   * 
   * @return  the name of the RBNB source
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the host name of the RBNB server.
   * 
   * @return  the host name of the RBNB server
   */
  public String getHost() {
    return host;
  }
  
  /**
   * Gets the port number of the RBNB server.
   * 
   * @return  the port number of the RBNB server
   */
  public int getPort() {
    return port;
  }
  
  /**
   * Opens the connection to the RBNB server.
   * 
   * @throws SAPIException  if there is an error connecting to the server
   */
  private void open() throws SAPIException {
    source.OpenRBNBConnection(host + ":" + port, name);
  }
  
  /**
   * Closes the connection to the server. The source will continue to exist on
   * the server.
   *
   */
  public void close() {
    source.Detach();
  }
  
  /**
   * Adds a channel to the source.
   * 
   * @param channel         the name of the channel
   * @throws RBNBException  if there is an error adding the channel
   */
  public void addChannel(String channel) throws RBNBException {
    addChannel(channel, null);
  }
  
  /**
   * Adds a channel to the source. This channel is given a mime type.
   * 
   * @param channel         the name of the channel
   * @param mime            the mime type of the channel
   * @throws RBNBException  if there is an error adding the channel
   */
  public void addChannel(String channel, String mime) throws RBNBException {
    addChannel(channel, mime, null);
  }
  
  /**
   * Adds a channel to the source. This channel is given a mime type and a unit.
   * 
   * @param channel         the name of the channel
   * @param mime            the mime type of the channel
   * @param unit            the unit for the channel
   * @throws RBNBException  if there is an error adding the channel
   */
  public void addChannel(String channel, String mime, String unit) throws RBNBException {
    try {
      int cindex = cmap.Add(channel);
      
      if (mime == null) {
        mime = "application/octet-stream";
      }
      cmap.PutMime(cindex, mime);
      
      if (unit != null) {
        cmap.PutUserInfo(cindex, "units=" + unit);
      }
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }    
  }
  
  /**
   * Registers the channel metadata for all the added channels.
   * 
   * @throws RBNBException  if there is an error registering the metadata
   */
  private void registerChannels() throws RBNBException {
    if (registered) {
      return;
    }
    
    try {
      source.Register(cmap);
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }
    
    registered = true;
  }
  
  /**
   * Puts data in the souce. The data will not be uploaded to the server till
   * {@link #flush()} is called.
   * 
   * @param channel         the name of the channel
   * @param timestamp       the timestamp for the data
   * @param data            the value for the data
   * @throws RBNBException  if there is an error putting the data.
   */
  public void putData(String channel, double timestamp, double data) throws RBNBException {
    registerChannels();
    
    putTime(timestamp);
    
    int cindex = cmap.GetIndex(channel);
    
    try {
      cmap.PutDataAsFloat64(cindex, new double[] {data});
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }
  }
  
  /**
   * Puts data in the souce as a byte array. The data will not be uploaded to
   * the server till {@link #flush()} is called.
   * 
   * @param channel         the name of the channel
   * @param timestamp       the timestamp for the data
   * @param data            the value for the data
   * @throws RBNBException  if there is an error putting the data.
   */
  public void putData(String channel, double timestamp, byte[] data) throws RBNBException {
    registerChannels();
    
    putTime(timestamp);
    
    int cindex = cmap.GetIndex(channel);
    
    try {
      cmap.PutDataAsByteArray(cindex, data);
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }
  }
  
  /**
   * Sets the timestamp for the subsequent data added.
   *  
   * @param timestamp  the timestamp for the data
   */
  private void putTime(double timestamp) {
    if (timestamp == lastTimestamp) {
      return;
    }
    
    cmap.PutTime(timestamp, 0);
    
    lastTimestamp = timestamp;
  }
  
  /**
   * Uploads all the data added with {@link #putData} to the server.
   * 
   * @throws RBNBException  if there is an error uploading the data
   */
  public void flush() throws RBNBException {
    try {
      source.Flush(cmap, true);
    } catch (SAPIException e) {
      throw new RBNBException(e);
    }
  }
}