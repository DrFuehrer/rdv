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

import junit.framework.TestCase;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelTree.Node;

/**
 * Unit test for a RBNB channel.
 * 
 * @author Jason P. Hanley
 */
public class RBNBChannelTest extends TestCase {
  
  /** the channel object to test */
  private RBNBChannel channel;
  
  /** the address for the RBNB server */
  private static final String serverAddress = "localhost";
  
  /** the source (parent) of the channel */
  private static final String sourceName = "TestSource";
  
  /** the name of the channel */
  private static final String channelName = "channel01";
  
  /** the name of the channel */
  private static final String name = sourceName + "/" + channelName;

  /** the channel unit */
  private static final String unit = "in";
  
  /** the start time of the data */
  private static final double start = System.currentTimeMillis()/1000d;
  
  /** the duartion of the data */
  private static final double duration = 12;
  
  /**
   * Create the channel and give it metadata. This starts up a local RBNB
   * server, registers a channel with the server, and then creates the channel
   * object from this.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    Source source = new Source();
    source.OpenRBNBConnection(serverAddress, sourceName);
    
    ChannelMap channelMap = new ChannelMap();
    int channelIndex = channelMap.Add(channelName);
    
    String metadata = "units=" + unit;
    channelMap.PutUserInfo(channelIndex, metadata);
    
    source.Register(channelMap);
    
    channelIndex = channelMap.Add(channelName);
    channelMap.PutTime(start, duration);
    channelMap.PutDataAsFloat64(channelIndex, new double[] { 0 });
    source.Flush(channelMap);
    
    Sink sink = new Sink();
    sink.OpenRBNBConnection();
    sink.RequestRegistration();
    channelMap = sink.Fetch(-1);
    
    channelIndex = channelMap.GetIndex(name);
    metadata = channelMap.GetUserInfo(channelIndex);
    
    ChannelTree channelTree = ChannelTree.createFromChannelMap(channelMap);
    Node node = channelTree.findNode(name);
    
    sink.CloseRBNBConnection();
    source.CloseRBNBConnection();
    
    channel = new RBNBChannel(node, metadata);
  }

  /**
   * Test getting the name of the channel.
   */
  public void testGetName() {
    assertEquals(name, channel.getName());
  }
  
  /**
   * Test getting the unit of the channel.
   */
  public void testGetUnit() {
    assertEquals(unit, channel.getUnit());
  }
  
  /**
   * Tests getting the start of the data for the channel.
   */
  public void testGetStart() {
    assertEquals(start, channel.getStart());
  }
  
  /**
   * Tests getting the duration of the data for the channel.
   */
  public void testGetDuration() {
    assertEquals(duration, channel.getDuration());    
  }

  /**
   * Test getting the metadata for the channel. This tests the unit of the
   * channel.
   */
  public void testGetMetadata() {
    assertEquals("", channel.getMetadata("server"));
    assertEquals("application/octet-stream", channel.getMetadata("mime"));
    assertEquals(unit, channel.getMetadata("units"));
    assertEquals("8", channel.getMetadata("size"));
  }

}