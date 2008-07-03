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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.rbnb.DataTurbine;
import org.rdv.rbnb.EventMarker;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * A unit test to validate the usage of the DataTurbine wrapper class.
 * 
 * @author Lawrence J. Miller
 * @see org.rdv.rbnb.DataTurbine
 */
public class DataTurbineTest extends TestCase {
  
  static Log log = LogFactory.getLog (DataTurbineTest.class.getName ());
  private DataTurbine turban;
  private ChannelMap cmap; 
  private String dtName = "dev-neestpm.sdsc.edu";
  private Sink turbanSink;
  private String testString = "markerDTjunit";
  
  public DataTurbineTest (String nme) {
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
    // indicates that some data was indeed fetched
    assertTrue (0 < testIndex);
    turbanSink.CloseRBNBConnection ();
   
  } // testDT ()
} // class