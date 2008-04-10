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

import junit.framework.TestCase;

/**
 * Unit test for the channel manager.
 * 
 * @author Jason P. Hanley
 */
public class ChannelManagerTest extends TestCase {
  /** the channel manager to test */
  private ChannelManager channelManager;
  
  /** channels to test with */
  private String channel1, channel2;
  
  /** data listeners to test with */
  private DummyDataListener dataListener1, dataListener2;

  /**
   * Setup the channel manager, the test channels, and dummy data listeners
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    channelManager = new ChannelManager();
    
    channel1 = "channel01";
    channel2 = "channel02";
    
    dataListener1 = new DummyDataListener();
    dataListener2 = new DummyDataListener();
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.subscribe(String, DataListener)'
   */
  public void testSubscribe() {
    assertTrue(channelManager.subscribe(channel1, dataListener1));
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.unsubscribe(String, DataListener)'
   */
  public void testUnsubscribe() {
    assertFalse(channelManager.unsubscribe(channel1, dataListener1));
    
    channelManager.subscribe(channel1, dataListener1);
    assertTrue(channelManager.unsubscribe(channel1, dataListener1));
    assertFalse(channelManager.unsubscribe(channel1, dataListener2));
    assertFalse(channelManager.unsubscribe(channel2, dataListener1));
    assertFalse(channelManager.unsubscribe(channel2, dataListener2));
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.isChannelSubscribed(String)'
   */
  public void testIsChannelSubscribed() {
    assertFalse(channelManager.isChannelSubscribed(channel1));
    assertFalse(channelManager.isChannelSubscribed(channel2));    
    
    channelManager.subscribe(channel1, dataListener1);
    assertTrue(channelManager.isChannelSubscribed(channel1));
    assertFalse(channelManager.isChannelSubscribed(channel2));
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.hasSubscribedChannels()'
   */
  public void testHasSubscribedChannels() {
    assertFalse(channelManager.hasSubscribedChannels());
    channelManager.subscribe(channel1, dataListener1);
    assertTrue(channelManager.hasSubscribedChannels());
    channelManager.unsubscribe(channel1, dataListener1);
    assertFalse(channelManager.hasSubscribedChannels());
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.isListenerSubscribedToChannel(String, DataListener)'
   */
  public void testIsListenerSubscribedToChannel() {
    assertFalse(channelManager.isListenerSubscribedToChannel(channel1, dataListener1));
    assertFalse(channelManager.isListenerSubscribedToChannel(channel2, dataListener1));
    
    channelManager.subscribe(channel1, dataListener1);
    assertTrue(channelManager.isListenerSubscribedToChannel(channel1, dataListener1));
    assertFalse(channelManager.isListenerSubscribedToChannel(channel1, dataListener2));
    assertFalse(channelManager.isListenerSubscribedToChannel(channel2, dataListener1));
    assertFalse(channelManager.isListenerSubscribedToChannel(channel2, dataListener2));
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.isChannelTabularOnly(String)'
   */
  public void testIsChannelTabularOnly() {
    assertFalse(channelManager.isChannelTabularOnly(channel1));
    assertFalse(channelManager.isChannelTabularOnly(channel2));
    
    channelManager.subscribe(channel1, dataListener1);
    assertFalse(channelManager.isChannelTabularOnly(channel1));
    assertFalse(channelManager.isChannelTabularOnly(channel2));    
  }

  /**
   * Test method for
   * 'org.nees.buffalo.rdv.rbnb.ChannelManager.postData(ChannelMap)'
   */
  public void testPostData() {
    channelManager.subscribe(channel1, dataListener1);
    channelManager.subscribe(channel2, dataListener2);
    channelManager.postData(null);
    assertTrue(dataListener1.gotPostData());
    assertTrue(dataListener2.gotPostData());
    
    channelManager.unsubscribe(channel1, dataListener1);
    
    dataListener1.clearPostData();
    dataListener2.clearPostData();
    channelManager.postData(null);
    assertFalse(dataListener1.gotPostData());
    assertTrue(dataListener2.gotPostData());
  }
  
  /**
   * A data listener that tells us if post data was called.
   */
  private class DummyDataListener implements DataListener {
    private boolean dataPosted;
    
    public void postData(ChannelMap channelMap) {
      dataPosted = true;
    }
    
    public boolean gotPostData() {
      return dataPosted;
    }
    
    public void clearPostData() {
      dataPosted = false;
    }
  }
}