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

import org.rdv.rbnb.Channel;

import com.rbnb.api.Server;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelTree.Node;

import junit.framework.TestCase;

/**
 * Unit test for a data channel.
 * 
 * @author Jason P. Hanley
 */
public class ChannelTest extends TestCase {
  /** the channel object to test */
  private Channel channel;
  
  /** the source (parent) of the channel */
  private static final String sourceName = "TestSource";
  
  /** the name of the channel */
  private static final String channelName = "channel01";
  
  /** the full name of the channel */
  private static final String fullName = sourceName + "/" + channelName;

  /** the channel unit */
  private static final String unit = "in";
  
  /**
   * Create the channel and give it metadata. This starts up a local RBNB
   * server, registers a channel with the server, and then creates the channel
   * object from this.
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    String[] serverArgs = {};
    Server server = Server.launchNewServer(serverArgs);
    
    Source source = new Source();
    source.OpenRBNBConnection("localhost", sourceName);
    
    ChannelMap channelMap = new ChannelMap();
    int channelIndex = channelMap.Add(channelName);
    
    String metadata = "units=" + unit;
    channelMap.PutUserInfo(channelIndex, metadata);
    
    source.Register(channelMap);
    
    Sink sink = new Sink();
    sink.OpenRBNBConnection();
    sink.RequestRegistration();
    channelMap = sink.Fetch(10000);
    
    channelIndex = channelMap.GetIndex(fullName);
    metadata = channelMap.GetUserInfo(channelIndex);
    
    ChannelTree channelTree = ChannelTree.createFromChannelMap(channelMap);
    Node node = channelTree.findNode(fullName);
    
    sink.CloseRBNBConnection();
    source.CloseRBNBConnection();
    server.stop();
    
    channel = new Channel(node, metadata);
  }

  /**
   * Test getting the (short) name of the channel.
   */
  public void testGetShortName() {
    assertEquals(channelName, channel.getShortName());
  }

  /**
   * Test getting the (full) name of the channel.
   */
  public void testGetName() {
    assertEquals(fullName, channel.getName());
  }

  /**
   * Test getting the parent (source) of the channel.
   */
  public void testGetParent() {
    assertEquals(sourceName, channel.getParent());
  }

  /**
   * Test getting the metadata for the channel. This tests the unit of the
   * channel.
   */
  public void testGetMetadata() {
    assertEquals(unit, channel.getMetadata("units"));
  }

  /**
   * Test getting the string representation of the channel.
   */
  public void testToString() {
    assertEquals(fullName, channel.getName());
  }
}