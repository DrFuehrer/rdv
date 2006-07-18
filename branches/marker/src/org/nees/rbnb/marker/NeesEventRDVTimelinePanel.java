package org.nees.rbnb.marker;

/**
* A class that gets @see org.nees.rbnb.marker.NeesEvent data from a channel
 * whose name is passed in an accessor call. Graph of major functionality:
 * paintComponent () -> findEventsChannel () -> getEventData ()
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Cen
 ter
 * @since 960110
 * @see org.nees.buffalo.rdv.rbnb.RBNBController
 * Copyright (c) 2005, 2006, NEES Cyberinfrastructure Center (NEESit),
 * San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $HeadURL$ 
 */

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.DataListener;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.TimeListener;
import org.nees.buffalo.rdv.ui.ControlPanel;
import org.nees.rbnb.marker.NeesEvent;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.MarkerDataListener;

public class NeesEventRDVTimelinePanel extends JPanel implements DataListener, TimeListener, MetadataListener, MarkerDataListener   {
  static Log log = LogFactory.getLog (NeesEventRDVTimelinePanel.class.getName ());
  
  /** A variable to hold the RDV DataTurbine controller
  * @see org.nees.buffalo.rdv.rbnb.RBNBController
  */
  private RBNBController rbnbctl = null;
  /** data structures from the DataTurbine
    * @see com.rbnb.sapi.ChannelMap
    * @see com.rbnb.sapi.ChannelTree
    */
  private ChannelMap channelMap;
  private ChannelTree ceeTree;
  /** Variables to hold the time limits of internal state of the DataTurbine
    * (i.e. the whole ring buffer) */
  private double startTime = -1;
  private double endTime = -1;
  /** Variables to store and manage events that are found in the DataTurbine.*/
  protected NeesEvent[] theEvents = null;
  protected double[] theEventsTimes = null;
  /** A variable to accumulate incident events. */
  protected HashMap theEventsHistoryHash = null;
  protected Set theEventsHistoryHashKeys = null;
  /** A variable to indicate whether or not an events channel has been found */
  private boolean foundEventsChannel = false;
  /** variables to track the time interval of the events channel only, as
    * opposed to that of the entire ring buffer.
    * furthermore, this range is to be determined by (possibly delayed) marker
    * generation times. */ 
  private double eventsChannelStartTime = -1, eventsChannelEndTime = -1;
  /** an ArrayList to track multiple event sources as they are discovered.
    * this iseffectively a merge of different NeesEvent sources */
  private HashMap eventsChannelNames = null;
  /** A pair of variables that will be used to make a synthetic data source. */
  private long timeNow = System.currentTimeMillis ();
  // This is all in miliseconds
  private double cannedTimeBase = (double)timeNow - 120000.0; // 2 min.
  private double cannedTimeInterval = 10000.0; // 10 sec.
  private double markerTimes[] = {
    cannedTimeBase + 1*cannedTimeInterval, cannedTimeBase + 2*cannedTimeInterval,
    cannedTimeBase + 3*cannedTimeInterval, cannedTimeBase + 4*cannedTimeInterval,
    cannedTimeBase + 5*cannedTimeInterval, cannedTimeBase + 6*cannedTimeInterval
  };
  /** A variable to switch on the usage of synthetic marker data. */
  private boolean usingFakeEvents = false;
  private boolean madeFakeEvents = false;
  private int markerXcoordinate;
  private double markerPanelScaleFactor;
  private ControlPanel rdvControlPanel = null;
  /** A variable that will be paseed into the constructor that will dsiplay
    * messages from this panel. */
  private JLabel controlPanelLabel = null;
  /** A variable to offset the coordinates used to draw marker blips.
    * Needs to offset the scroll bar arrows on the location slider. */
  public static int FUDGE_FACTOR = 10;
  /** A variable that guards the scanPastMarkers () function */
  public boolean doScanPastMarkers = true;
  
  /** constructor */
  public NeesEventRDVTimelinePanel (ControlPanel cpanel) {
    super ();
    // DOTOO make this not get black and ugly when it clearDisplay's
    this.channelMap = null;
    /** required if there is preexisting data to be looked at upon startup */
    this.rdvControlPanel = cpanel;
    this.rbnbctl = this.rdvControlPanel.rbnbController;
    this.ceeTree = this.rdvControlPanel.ctree;
    this.controlPanelLabel = this.rdvControlPanel.markerLabel;
    eventsChannelNames = new HashMap ();
    theEventsHistoryHash = new HashMap ();
    theEventsHistoryHashKeys = theEventsHistoryHash.keySet ();
    
    if (this.rbnbctl.isConnected ()) {
      this.findEventsChannel ();
    }
  } // constructor ()
  
  
  /** A mutator to set the text displayed on a @param controlPanelLabel JLabel. */
  public void setControlPanelLabelText (String newText) {
    if (this.controlPanelLabel != null) {
      this.controlPanelLabel.setText (newText);
    } // if !null
  } // setControlPanelLabel ()
  
  
  /** A mutator method to set @param the start of the time interval of interest in
    * the dataturbine. */
  public void setEventsChannelStartTime (double newStart) {
    if (-1.0 < newStart) {
      this.eventsChannelStartTime = newStart;
      //log.debug ("*** Set marker channel START to: " +  DataViewer.formatDate (eventsChannelStartTime));
      updateMarkerPanelScaleFactor ();
    }
  }
  
  
  /** A mutator method to set @param the end of the time interval of interest in
    * the dataturbine. */
  public void setEventsChannelEndTime (double newEnd) {
    if (this.eventsChannelEndTime < newEnd) {
      this.eventsChannelEndTime = newEnd;
      //log.debug ("*** Set marker channel END to: " +  DataViewer.formatDate (eventsChannelEndTime));
      updateMarkerPanelScaleFactor ();
    }
  }
  
  
  /** An accessor method to get @return the scale factor used to interconvert
    * between pixels and times for this graphics component in units of
    * pixels/time. */
  public double getScaleFactor () {
    return this.markerPanelScaleFactor;
  }
  
  
  /** An accessor method to get @return the start time of the event marker
    * channel */
  public double getEventsChannelStartTime () {
    return this.eventsChannelStartTime;
  }
  
  
  /** An accessor method to get @return the start time of the event marker
    * channel */
  public double getEventsChannelEndTime () {
    return this.eventsChannelEndTime;
  }
  
  
  /**
    * A method to use/not use synthetic marker data.
   */
  public void setSyntheticMarkers (boolean flag) {
    this.usingFakeEvents = flag;
  }
  
  /** A method for the @see org.nees.buffalo.rdv.rbnb.DataListener interface */
  public void postData (ChannelMap cMap) {
      log.debug("postData() - start");
    this.channelMap = cMap;
    log.debug("postData() - map " + this.channelMap);
    // REFACTOR this seems redundant, but is critical to operation
    this.findEventsChannel ();
    this.repaint ();
  } // postData ()
  
  // channel mappy stuff
  public void clearData () {
    this.doScanPastMarkers = true;
    this.channelMap = null;
    this.ceeTree = null;
    // FIXME run through and unsubscibe from channels
    this.theEvents = null;
    this.theEventsHistoryHash.clear();
    this.eventsChannelNames.clear ();
    this.repaint ();
  } // clearData ()
  
 /** A method for the @see org.nees.buffalo.rdv.rbnb.TimeListener interface */ 
  public void postTime (double newTime) {
    setEventsChannelEndTime (newTime);
    updateMarkerPanelScaleFactor ();
    repaint ();
  }
  
  
  /** This method is called from the ControlPanel when its channelTreeUpdated ()
    * method is called. it effectively updates theEvents
    */
  public void updateCtree (ChannelTree newCtree) {
    this.ceeTree = newCtree;
    // REFACTOR this seems redundant, but is critical to operation
    this.findEventsChannel ();
    this.repaint ();
  } // updateCtree ()
  
  
  public void clearDisplay () {
    Graphics mpg = this.getGraphics ();
    if (mpg != null) {
      mpg.fillRect (0, 0, this.getWidth (), this.getHeight ());
    }
  } // clearDisplay ()
  
  
  public void paintComponent (Graphics gra) {
      log.debug("paintComponent() - start");
      if (!this.rbnbctl.isConnected ()) return;
      
    /* Following the gui guidelines in http://java.sun.com/docs/books/tutorial/uiswing/14painting/concepts2.html */
    Graphics2D markerPanelG = (Graphics2D)gra.create ();
    super.paintComponent (markerPanelG);
    
    // if we have some events to look at, display them.
    // this.clearDisplay ();
    //  FIXME
//    if (this.doScanPastMarkers) {
//       //this.scanPastMarkers ();
//    }
    /* This call will find all events channels and call getEventData to 
     * mutate theEvents */
//    this.findEventsChannel ();
    
    if (theEvents != null) {
        log.debug("paintComponent() - number of events: " + theEvents.length);
      String markerType = null;
      // log.debug ("--- Start Draw ---");
      // loop though the events and draw them
      for (int i=0; i<theEvents.length; i++) {
        // log.debug (theEvents[i].toString ());
        markerType = theEvents[i].getProperty ("type");
        if ( markerType.compareToIgnoreCase ("start") == 0 ) {
            log.debug("start event detected at : " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("dtTimestamp"))));
            log.debug("TimeStamp at : " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("timestamp"))));
          markerPanelG.setColor (NeesEvent.startColor); 
        } else if ( markerType.compareToIgnoreCase ("stop") == 0 ) {
            log.debug("stop event detected at: " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("dtTimestamp"))));
            log.debug("TimeStamp at : " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("timestamp"))));
          markerPanelG.setColor (NeesEvent.stopColor);
        } else {
//            log.debug(markerType + " event detected, at: " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("dtTimestamp"))));
            log.debug("Annotation marker at at : " + DataViewer.formatDate(Double.parseDouble(theEvents[i].getProperty ("timestamp"))));
          markerPanelG.setColor (Color.black);
        } // else
        
        /* use the time that the marker was generated with at the source */
        double time2use = (theEvents[i].getProperty ("timestamp") != null)?
          Double.parseDouble (theEvents[i].getProperty ("timestamp")):
          Double.parseDouble (theEvents[i].getProperty ("dtTimestamp"));
//        log.debug("TimeStamp for event" + i + " = " + DataViewer.formatDate(time2use));
        
        markerXcoordinate = (int)((time2use - eventsChannelStartTime) * this.getScaleFactor ());
        log.debug("markerXcoordinate(" + i + ")= (" + time2use + " - " + eventsChannelStartTime + ") * " + this.getScaleFactor ());

        // Fixes to keep display on-scale
        if (markerXcoordinate <= 0) {
          markerXcoordinate = FUDGE_FACTOR;
        } else if ((this.getWidth ()) <= markerXcoordinate) {
            log.debug("this.getWidth (): " + this.getWidth () + " <= markerXcoordinate");
          markerXcoordinate = this.getWidth () - FUDGE_FACTOR;
          log.debug("markerXcoordinate: " + markerXcoordinate + "  after fudgefactor subtracted");
        } else if ((this.getWidth ()) - FUDGE_FACTOR < markerXcoordinate && markerXcoordinate < this.getWidth ()) {
            log.debug("ELSE");
          markerXcoordinate = markerXcoordinate - FUDGE_FACTOR;
        }
        log.debug("will paint at mx = " + markerXcoordinate + " - " + (i*10) + " = " + (markerXcoordinate - (i * 10)));
        markerPanelG.fillRect (markerXcoordinate - (i * 10), 0, 1, 10);
      } // for
    
      //log.debug ("***--- End Draw ---");
    } else { // no markers
      log.debug ("*** No Marker Data - null.");
    } // else
    markerPanelG.dispose ();
  } // paintComponent ()
  
  
  /**
    * A method to generate synthetic marker data.
   * @return org.nees.rbnb.marker.NeesEvent
   */
 private NeesEvent[] makeFakeEvents () {
//    if (this.madeFakeEvents) {
//      return this.theEvents;
//    }
    ArrayList eventArrayList = new ArrayList ();
    NeesEvent eventTemp = null;
    for (int i=0; i<markerTimes.length; i++) {
      eventTemp = new NeesEvent ();
      eventTemp.setProperty ("timestamp", Double.toString (markerTimes[i]/1000.0));
      eventTemp.setProperty ("dtTimestamp", Double.toString (markerTimes[i]));
      eventTemp.setProperty ("label", "Synthetic Marker" + i+1);
      if (i%3 == 0) {
        eventTemp.setProperty ("type", "start");
      } else if (i%3 == 2) {
        eventTemp.setProperty ("type", "stop");
      } else {
        eventTemp.setProperty ("type", "annotation");
      }
      eventArrayList.add (eventTemp);
    } // for
    
//    return eventArrayList;
    
    Object [] retValTmp = eventArrayList.toArray ();
    NeesEvent[] retVal= new NeesEvent [retValTmp.length];
    
    for (int j=0; j<retValTmp.length; j++) {
      retVal[j] = (NeesEvent)retValTmp[j];
    }
    log.debug ("Made fake events.");
    madeFakeEvents = true;
    return retVal;
  
  } // makeFakeEvents ()
  
  
  /** A method to keep the marker display panel time scale factor in sync with 
    * the time interval being displayed.
    */
  // REFACTOR repainting here creates an infinite loop
  public void updateMarkerPanelScaleFactor () {
    markerPanelScaleFactor =
    ( this.getWidth () / ((this.getEventsChannelEndTime () -
                           this.getEventsChannelStartTime ()) *
                           this.rbnbctl.getTimeScale()) );
  }
  
  
  /** A method that @return a custom tool tip message. */
  public String getToolTipText (MouseEvent e) {
    super.getToolTipText();
    double guiTime = ((e.getX () / this.getScaleFactor ()) +
                      this.getEventsChannelStartTime ());
    String targetMarkerLabel = null;
    double closestTime = getClosestTime (theEventsHistoryHash, guiTime);
    
    NeesEvent targetEvent = (NeesEvent)( theEventsHistoryHash.get (closestTime) );
    /* this case is for the boundary case when the mouse is right at the edge
     * of this panel's display */
    if ( e.getX () < this.getWidth () &&
         (this.getWidth () - FUDGE_FACTOR) < e.getX () ) {
      // get the maximum key
      targetEvent = theEvents[theEvents.length-1];
    } // if
    targetMarkerLabel = (targetEvent == null)? "" : (String)(targetEvent.getProperty ("label"));
    return (targetMarkerLabel);
  } // getToolTipText ()
  
  
  /** A method that will search 
    * @param an input @see java.util.HashMap 
    * by simply iterating
    * @return the nearest valid key within the tolerance interval
    * algorithm is to find the element with which
    * @param an imput target
    * has a minmum difference
    * DOTOO This can be optimized to at least O(n/2), rather than O(n)
    */
  public double getClosestTime (HashMap searchSpace, double target) {
    
    Object[] keySetObj = searchSpace.keySet ().toArray ();
    double targetDiff = Double.MAX_VALUE;
    double currTargetDiff = targetDiff;
    
    double closestTime = -1.0;
    int closestTimeDex = -1;
    
    for (int i=0; i<keySetObj.length; i++) {
      currTargetDiff = Math.abs (target - ( (Double)(keySetObj[i]) ).doubleValue () );
      if (currTargetDiff < targetDiff) { // then found a new minmum
        targetDiff = currTargetDiff;
        closestTimeDex = i;
      } // if
    } // for
    
    if (closestTimeDex == -1) {
      return closestTimeDex;
    }
    return ( ( (Double)(keySetObj[closestTimeDex]) ).doubleValue () );
  } // getClosestTime ()
  
  
  /** A method to search a channel tree for an events channel.
    * This also sets the global variable to keep track of the start and end
    * times of this channel, so is important to call on updates from RDV.
    */
  public void findEventsChannel () {
    if (ceeTree == null) {
      return;
    }
  
    Iterator it = ceeTree.iterator ();
    ChannelTree.Node node;
    String sourceChannelName = null;
    String channelMime = null;
    double channelStartTime = -1, channelEndTime = -1;
    // Go through the entire channel tree and pick out the events channels
    while (it.hasNext ()) {
      node = (ChannelTree.Node)it.next ();
      sourceChannelName = node.getFullName ();
      channelMime = node.getMime ();
      // get the latest times
      
      // DOTOO move this call as an optimization
      this.updateMarkerPanelScaleFactor ();
      // then an events channel has been found
      if (channelMime != null &&
          channelMime.compareToIgnoreCase (NeesEvent.MIME_TYPE) == 0)
      {
        this.eventsChannelNames.put (sourceChannelName, sourceChannelName);
        this.foundEventsChannel = true;
        
        //log.debug ("*** FOUND events channel. my list: " + this.eventsChannelNames.keySet());
        
        // reset the time bounds based on the times found in this channel tree
        if (node.getStart() < this.getEventsChannelStartTime ()) {
          setEventsChannelStartTime (node.getStart());
        }
        
        if ( this.getEventsChannelEndTime () < (node.getStart() + node.getDuration()) ) {
          setEventsChannelEndTime (node.getStart() + node.getDuration());
        }
       
        // If this is a newly incident channel, then subscribe to it to get updates
        if (! this.rbnbctl.isSubscribed (sourceChannelName)) {
          if (this.rbnbctl.subscribe (sourceChannelName, this)) {
//            this.getEventData (sourceChannelName);
          } else {
            log.error ("Couldn't subscribe to channel \"" + sourceChannelName + "\"");
          }
        } else { // already subscribed to an events channel
//          this.getEventData (sourceChannelName);
        }
      } // if
    } // while
  } // findEventsChannel ()    
  
  
  /** A method to scan through the ring buffer to find all previous event
    * markers */
  public void scanPastMarkers () {
    if ( (! this.doScanPastMarkers) || (this.rdvControlPanel.startTime < 0) ) {
      log.debug ("%%% scanPastMarkers () short-circuited");
      log.debug ("%%% this.rdvControlPanel.startTime: " + DataViewer.formatDate (this.rdvControlPanel.startTime));
      return;
    } else { // scan the ring buffer
      log.debug ("%%% scanPastMarkers ()");
      // necessary here because the repaint () call makes a loop
      this.doScanPastMarkers = false;
      // backup rbnb controller's state
      int stateTmp        = this.rbnbctl.getState ();
      double locationTmp  = this.rbnbctl.getLocation ();
      double timeScaleTmp = this.rbnbctl.getTimeScale ();
      log.debug ("%%% rbnb state BACKUP:");
      log.debug ("%%% stateTmp: "     + this.rbnbctl.getStateName (stateTmp));
      log.debug ("%%% locationTmp: "  + DataViewer.formatDate (locationTmp));
      log.debug ("%%% timeScaleTmp: " + Double.toString (timeScaleTmp));
      log.debug ("%%% this.rdvControlPanel.startTime: " + DataViewer.formatDate (this.rdvControlPanel.startTime));
      
      // FIXME
      this.rbnbctl.setLocation (this.rdvControlPanel.startTime);
      this.rdvControlPanel.setSliderLocation (this.rdvControlPanel.startTime);
      this.rdvControlPanel.locationChange ();
      this.rbnbctl.setTimeScale (this.rdvControlPanel.timeScales[(this.rdvControlPanel.timeScales.length) - 1]);
      this.rbnbctl.play ();
      
      log.debug ("%%% rbnb state CHANGED:\n");
      log.debug ("%%% rbnb state: "  + this.rbnbctl.getStateName (stateTmp));
      log.debug ("%%% location: "  + DataViewer.formatDate (this.rbnbctl.getLocation ()) );
      log.debug ("%%% timeScale: " + Double.toString (this.rbnbctl.getTimeScale ()) );
          
      // find the events and get them
      this.repaint();
      
      // restore the rbnbctl's beginning state
      this.rbnbctl.setState (stateTmp);
      this.rbnbctl.setLocation (locationTmp);
      this.rbnbctl.setTimeScale (timeScaleTmp);
      log.debug ("%%% rbnbstate RESTORED:\n");
      log.debug ("%%% location: "  + DataViewer.formatDate (this.rbnbctl.getLocation ()) );
      log.debug ("%%% timeScale: " + Double.toString (this.rbnbctl.getTimeScale ()) );
      this.repaint ();
    } // else
  } // scanPastMarkers ()

  public void updateMarkerChannels(ChannelMap cMap) {
    
 //   if (cMap == null)
      return;
/*    
    this.channelMap = cMap;
    
    String channelName;
    int index;
    for (int i = 0; i < channelMap.NumberOfChannels(); i++) {
      
      channelName = this.channelMap.GetName(i);
      index = channelMap.GetIndex(channelName);
      getEventData(index);
    }
  */
  }
  
  /** A method to update theEvents with event data from a @param input rbnb data
    * channel. */
  public void getEventData (int channelIndex) {

//    log.debug("getEventData() - channel index " + channelIndex);

    String[] channelData = null;
    NeesEvent[] eventData = null;
 
    theEventsTimes = channelMap.GetTimes (channelIndex);
    channelData = this.channelMap.GetDataAsString (channelIndex);
    eventData = new NeesEvent[channelData.length];
    for (int i=0; i<channelData.length; i++) {
      eventData[i] = new NeesEvent ();
      try {
        eventData[i].setFromEventXml (channelData[i]);
        if (theEventsTimes.length == 1) {
          eventData[i].setProperty ("dtTimestamp", Double.toString (theEventsTimes[0]));
//          log.debug("got timeStamp " + eventData[i].getProperty("dtTimestamp"));
        }
      } catch (TransformerException te) { 
        log.error ("Java XML Error\n" + te);
        te.printStackTrace ();
      } catch (InvalidPropertiesFormatException ipfe) {
        log.error ("Java XML Error\n" + ipfe);
        ipfe.printStackTrace ();
      } catch (IOException ioe) {
        log.error ("Java IO Error\n" + ioe);
        ioe.printStackTrace ();
      }
    
      // Make sure not to do duplicate entries keyed by timestamp
      if (eventData[i].getProperty ("timestamp") != null) {
        theEventsHistoryHash.put
        (Double.parseDouble (eventData[i].getProperty ("timestamp")), eventData[i]);
      }
    } // for

      
    // Rercast the generic Objects from the toArray call into NEESevents
    Object[] theEventsHistoryTemp = theEventsHistoryHash.values ().toArray ();
    NeesEvent[] theEventsHistory = new NeesEvent[theEventsHistoryTemp.length];
    for (int i=0; i<theEventsHistory.length; i++) {
      theEventsHistory[i] = (NeesEvent)theEventsHistoryTemp[i];
    }
    // @see org.nees.rbnb.marker.NeesEvent
    Arrays.sort (theEventsHistory);
    
    // setting the global events store equal to the local one
    if (theEventsHistory != null) {
      this.theEvents = theEventsHistory;
    }
    
    // this protects divide by zeros in the scale factor - if length =1, then start=end
    if (theEvents != null && 1 <= theEvents.length) {
      // These get start and end because this array is now sorted
      String startStringTemp = theEvents[0].getProperty ("timestamp");
      String endStringTemp = theEvents[theEvents.length-1].getProperty ("timestamp");
      
      /* if there are no generation timestamps, then fall back to embedded
        * dataturbine receipt timestamps */
      if (startStringTemp == null && theEvents != null) {
        startStringTemp = theEvents[0].getProperty ("timestamp");
      }
      if (endStringTemp == null && theEvents != null) {
        endStringTemp = theEvents[theEvents.length-1].getProperty ("timestamp");
      }
      
      /* if this case doesn't get hit, then there are no timestamps at all
        * if start is less if end is more */
      double startTemp =  -1.0;
      double endTemp = -1.0;
      if (startStringTemp != null) {
        startTemp = Double.parseDouble (startStringTemp);
        setEventsChannelStartTime (startTemp);
      }
      if (endStringTemp != null) {
        endTemp = Double.parseDouble (endStringTemp);
        setEventsChannelEndTime (endTemp);
      }
  
      if (this.getEventsChannelStartTime () != startTemp ||
        this.getEventsChannelEndTime () != endTemp) {
        /*log.debug ("*** events channel times set - S: " +
               DataViewer.formatDate (getEventsChannelStartTime ()) +
               " E: " + DataViewer.formatDate (getEventsChannelEndTime ())
               );*/
      }
    } // if

  } // getEventData ()
  
  
  /** A method to display a popup when triggered from ContolPanel */
  public void doPopup (final MouseEvent e) {
    if (! e.isPopupTrigger ()) {
      return;
    }
    final double guiTime = ((e.getX () / this.getScaleFactor ()) +
                      this.getEventsChannelStartTime ());
    JPopupMenu popup = new JPopupMenu (Double.toString (guiTime));
    
    JMenuItem popupItem = new JMenuItem ("View Event");
    popupItem.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent notUsed) {
        JOptionPane.showMessageDialog (rdvControlPanel,
                                       formatMarker4Popup (guiTime, e),
                                       "Event Marker Display",
                                       JOptionPane.PLAIN_MESSAGE,
                                       null);
      } // actionPerformed ()
    } // new ActionListener
                                 ); // addActionListener
    popup.add (popupItem);
    popup.show (this, e.getX (), e.getY ());
    
  } // doPopup
  
  
  /** A method to format event marker content as a verbose string */
  public String formatMarker4Popup (double popupTime, MouseEvent e) {
    String targetMarkerOutput = null;
    double closestTime = getClosestTime (theEventsHistoryHash, popupTime);
    NeesEvent targetEvent = (NeesEvent)( theEventsHistoryHash.get (closestTime) );
    /* this case is for the boundary case when the mouse is right at the edge
      * of this panel's display */
    if ( e.getX () < this.getWidth () &&
         (this.getWidth () - FUDGE_FACTOR) < e.getX () ) {
      // get the maximum key
      targetEvent = theEvents[theEvents.length-1];
    } // if
    
    String tagretMarkerLabel   = (String)(targetEvent.get ("label"));
    String tagretMarkerContent = (String)(targetEvent.get ("content"));
    String tagretMarkerTime    =
        DataViewer.formatDate (
                Double.parseDouble ( (String)(targetEvent.get ("timestamp")) )
                               );
    
    targetMarkerOutput = "Label: " + tagretMarkerLabel + "\n" +
                         "Content: " + tagretMarkerContent + "\n" +
                         "Time: " + tagretMarkerTime;
    
    return targetMarkerOutput;
  } // formatMarker4Popup ()
  
  
  public void channelTreeUpdated(ChannelTree channelTree) {
    // don't so anythng for now..
    return;

/*    
      this.theEvents = makeFakeEvents();
  
      if (this.theEvents.length > 0) {
        for (int i = 0; i < theEvents.length; i++) {
            NeesEvent tmp = theEvents[i];
            log.debug("Fake data timestamp: " + tmp.getProperty("timestamp"));
            log.debug("Fake data dtTimestamp: " + tmp.getProperty("dtTimestamp"));
            log.debug("Fake data type: " + tmp.getProperty("type"));
            log.debug("Fake data label: " + tmp.getProperty("label"));
        }    
      }
*/      
/*      
      log.debug("channelTreeUpdated() - start");
      if (channelTree == null) {
        return;
      }
    
      Iterator it = channelTree.iterator ();
      log.debug("tree has " + it.getClass());
      ChannelTree.Node node;
      String channelMime = null;
      String channelName;
      
      
      // Go through the entire channel tree and pick out the events channels
      while (it.hasNext ()) {
        node = (ChannelTree.Node)it.next ();
        channelMime = node.getMime ();
        channelName = node.getFullName();
        // get the latest times

        if (channelMime != null &&
            channelMime.compareToIgnoreCase (NeesEvent.MIME_TYPE) == 0) {

          if (! this.rbnbctl.isSubscribed (channelName)) {
            log.debug("channel " + channelName + " not subscribed");
            if (this.rbnbctl.subscribe (channelName, this)) {
              this.getEventData (channelName);
            } else {
              log.error ("Couldn't subscribe to channel \"" + channelName + "\"");
            }
          } else { // already subscribed to an events channel
            log.debug("channel " + channelName + " already subscribed");
            this.getEventData (channelName);
          }
          
          this.getEventData(node.getFullName()); 
          log.debug("Marker time for " + node.getFullName() + " is " + DataViewer.formatDate(node.getStart()));
        }
      }
*/      
    }
  
} // class

/* Copyright Notice:
*
* Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
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
*    * Neither the name of the San Diego Supercomputer Center nor the names of
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
