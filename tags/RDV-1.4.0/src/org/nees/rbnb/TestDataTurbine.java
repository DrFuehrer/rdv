package org.nees.rbnb;
/**
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * Please see copywrite information at the end of this file.
 * @since 060120
 */
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.SAPIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.DataTurbine;
import org.nees.rbnb.marker.EventMarker;

import junit.framework.TestCase;
import junit.framework.TestResult;

/** A class that is a junit unit test to validate
* the usage of
* @see org.nees.rbnb.marker.NeesEvent.java
* and the DataTurbine wrapper class
* @see org.nees.rbnb.DataTurbine */
public class TestDataTurbine extends TestCase {
  
  static Log log = LogFactory.getLog (TestDataTurbine.class.getName ());
  private DataTurbine turban;
  private ChannelMap turbanCmap, cmap; 
  private String dtName = "dev-neestpm.sdsc.edu";
  private Sink turbanSink;
  private String testString = "markerDTjunit";
  
  public TestDataTurbine (String nme) {
    this.turban = new DataTurbine (testString);
    this.turban.setServerName (this.dtName);
    try  {
      this.turban.open ();
    } catch (SAPIException se) {
      log.error ("Couldn't open the test source");
    }
    this.turbanSink = new Sink ();
    this.cmap = new ChannelMap ();
  } // constructor

  public void runTest () {
    testDT ();
  }
  
  /** method that willdo the actual work by creating an XML marker, putting it
    * into the DataTurbine, and then getting it and examining it as the test. */
  public void testDT () {
    EventMarker testEvent = new EventMarker ();
    testEvent.setProperty ("annotation", testString);
    try {
      this.turban.putMarker (testEvent, this.testString);
    } catch (Exception e) {
      log.error ("Problem emitting XML");
    }
    try {
      this.turbanSink.OpenRBNBConnection (this.dtName, this.testString);
    } catch (SAPIException se) {
      log.error ("Couldn't open the test sink");
    }
    
    try {
      turbanSink.RequestRegistration (cmap);
    } catch (SAPIException se) {
      log.error ("Couldn't request registrations");
    }
    
    ChannelMap gotMap = null;
    // useful variable for initial unit test driven development
    boolean isMarkerChannel = false;
    try {
      String[] inChannels = cmap.GetChannelList ();
      gotMap = turbanSink.Fetch (100, cmap);
      String[] gotChannels = gotMap.GetChannelList ();
      log.debug ("fetched " + gotChannels.length + " channels from the test DT");
      
      boolean foundMarkerChannel = false;
      for (int i=0; i<gotChannels.length; i++) {
        foundMarkerChannel =
          gotChannels[i].compareTo (testString + "/" + testString) == 0;
          isMarkerChannel = isMarkerChannel || foundMarkerChannel;
      } // for
    } catch (SAPIException se) {
      log.error ("Couldn't get the marker data");
    }
    
    int testIndex = gotMap.GetIndex (testString+ "/" + testString);
    String [] gotArray = gotMap.GetDataAsString (testIndex);
    // indicates that some data was indeed fetched
    assertTrue (0 < testIndex);
    turbanSink.CloseRBNBConnection ();
   
  } // testDT ()
} // class

 /** Copyright (c) 2005, Lawrence J. Miller and NEESit
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 *   * Neither the name of the San Diego Supercomputer Center nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
