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
 * $LastChangedDate: 2006-07-06 17:09:26 -0700 (Thu, 06 Jul 2006) $
 * $LastChangedRevision: 2774 $
 * $LastChangedBy: msoltani $
 * $HeadURL: https://svn.nees.org/svn/telepresence/RDV/branches/marker/src/org/nees/rbnb/marker/NeesEventRDVTimelinePanel.java $ 
 */


import com.rbnb.sapi.ChannelMap;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.ControlPanel;
import org.nees.rbnb.marker.NeesEvent;
import org.nees.buffalo.rdv.rbnb.MarkerDataListener;
import java.util.Map;

public class MarkerEventsPanel extends JPanel implements MarkerDataListener   {
  static Log log = LogFactory.getLog (MarkerEventsPanel.class.getName ());
  
  /** A variable to hold the RDV DataTurbine controller
  * @see org.nees.buffalo.rdv.rbnb.RBNBController
  */
  private RBNBController rbnbctl = null;
  /** data structures from the DataTurbine
    * @see com.rbnb.sapi.ChannelMap
    * @see com.rbnb.sapi.ChannelTree
    */
  private ChannelMap channelMap;
  
  /** Variables to store and manage events that are found in the DataTurbine.*/
  protected NeesEvent[] theEvents = null;

  /** variables to track the time interval of the events channel only, as
    * opposed to that of the entire ring buffer.
    * furthermore, this range is to be determined by (possibly delayed) marker
    * generation times. */ 
  public double eventsChannelStartTime = 0.0, eventsChannelEndTime = 0.0;
  /** an ArrayList to track multiple event sources as they are discovered.
    * this iseffectively a merge of different NeesEvent sources */

 
  private ControlPanel rdvControlPanel = null;
  /** A variable that will be paseed into the constructor that will dsiplay
    * messages from this panel. */
  private JLabel controlPanelLabel = null;
  /** A variable to offset the coordinates used to draw marker blips.
    * Needs to offset the scroll bar arrows on the location slider. */
  public static int FUDGE_FACTOR = 10;
  /** A variable that guards the scanPastMarkers () function */
  public boolean doScanPastMarkers = true;

  private ArrayList<NeesEvent> markerEvents;
  
  protected Map<Double, String> toolTipMap;
  
  /** constructor */
  public MarkerEventsPanel(ControlPanel cpanel) {
    super ();

    this.channelMap = null;
    /** required if there is preexisting data to be looked at upon startup */
    this.rdvControlPanel = cpanel;
    this.rbnbctl = this.rdvControlPanel.rbnbController;
    this.controlPanelLabel = this.rdvControlPanel.markerLabel;
  }
  
  
  /** A mutator to set the text displayed on a @param controlPanelLabel JLabel. */
  public void setControlPanelLabelText (String newText) {
    if (this.controlPanelLabel != null) {
      this.controlPanelLabel.setText (newText);
    } // if !null
  } // setControlPanelLabel ()
  
  
  
  private void setMarkerMinMaxTimes(double newMarkerTime) {
    
    if (this.eventsChannelStartTime == 0)
      this.eventsChannelStartTime = newMarkerTime;
    
    if (this.eventsChannelEndTime < newMarkerTime)
      this.eventsChannelEndTime = newMarkerTime;
    
  }
  
   
  public void clearDisplay () {
    Graphics mpg = this.getGraphics ();
    if (mpg != null) {
      mpg.fillRect (0, 0, this.getWidth (), this.getHeight ());
    }
  } // clearDisplay ()
  
  
  public void paintComponent (Graphics gra) {
    log.debug("paintComponent() - start");
    
    if (!this.rbnbctl.isConnected ()) return;
    if (this.channelMap == null) return;
    
    /* Following the gui guidelines in http://java.sun.com/docs/books/tutorial/uiswing/14painting/concepts2.html */
    Graphics2D markerPanelG = (Graphics2D)gra.create ();
    super.paintComponent (markerPanelG);
  
    log.debug("paintComponent() - number of events: " + markerEvents.size());
    String markerType = "";
    double markerTime;
    int markerXcoordinate;
    
    double markerScaleFactor = this.getMarkerScaleFactor();
    
    toolTipMap = new HashMap<Double, String>();
   
    for(NeesEvent marker : this.markerEvents) {

      markerType = marker.getProperty("type");
      if (markerType.compareToIgnoreCase ("start") == 0 ) {
        markerPanelG.setColor(NeesEvent.startColor); 
      } else if ( markerType.compareToIgnoreCase ("stop") == 0 ) {
        markerPanelG.setColor(NeesEvent.stopColor);
      } else {
        markerPanelG.setColor(Color.black);
      }

//      log.debug("Marker data: - " + marker.getProperty("label") + " - " + marker.getProperty("content"));
      markerTime = Double.parseDouble(marker.getProperty("timestamp"));

      markerXcoordinate = (int)((markerTime - this.eventsChannelStartTime) * markerScaleFactor);

      if (markerXcoordinate <= 0) {
        markerXcoordinate = FUDGE_FACTOR;
      } else if ((this.getWidth ()) <= markerXcoordinate) {
        markerXcoordinate = this.getWidth () - FUDGE_FACTOR;
      } else if ((this.getWidth ()) - FUDGE_FACTOR < markerXcoordinate && markerXcoordinate < this.getWidth ()) {
        markerXcoordinate = markerXcoordinate - FUDGE_FACTOR;
      }
      
      log.debug(".getW() " + this.getWidth() + " markerCoordinate: " + markerXcoordinate);
      markerPanelG.fillRect (markerXcoordinate, 0, 1, 10);
     
      markerTime = (double)markerXcoordinate / (double)this.getWidth();
      toolTipMap.put(markerTime, marker.getProperty("label"));
    }

    markerPanelG.dispose ();
  } 
  

  /** A method to keep the marker display panel time scale factor in sync with 
    * the time interval being displayed.
    */
   public double getMarkerScaleFactor () {

    double scaleFactor = 0.0;
    
    try {

      scaleFactor =  this.getWidth() / ((this.eventsChannelEndTime -
          this.eventsChannelStartTime) *
          this.rdvControlPanel.getTimeScale());
          
      
    } catch (Exception e) {
      log.error("calculation exception: " + e.getMessage());
    }
    
    return scaleFactor;
  }
  
  /** A method that @return a custom tool tip message. */
  public String getToolTipText(MouseEvent e) {
    super.getToolTipText();

    String tipLabel = "";
    
    if (this.toolTipMap == null)
      return tipLabel;
    
    double mouseX = (double)e.getX() / (double)this.getWidth();

    if (this.toolTipMap.get(mouseX) != null) {
      tipLabel = (String)this.toolTipMap.get(mouseX);
    }
    
    return tipLabel;
  }
  
  public void updateMarkerChannels(ChannelMap cMap) {
    
    log.debug("updateMarkerChannels() - start");
    if (cMap == null || cMap.NumberOfChannels() == 0)
      return;
    
    this.channelMap = cMap;
    
    String channelName;
    int chanIndex;

    log.debug("updateMarkerChannels() - num channels: " + channelMap.NumberOfChannels());
    
    this.markerEvents = new ArrayList<NeesEvent>();

    for (int i = 0; i < channelMap.NumberOfChannels(); i++) {
      
      channelName = this.channelMap.GetName(i);
      chanIndex = channelMap.GetIndex(channelName);
      NeesEvent[] markers = getChannelMarkers(this.channelMap, chanIndex);
      for (int j = 0; j < markers.length; j++)
        markerEvents.add(markers[j]);
    }

    this.repaint();
    
  }
  
  private NeesEvent[] getChannelMarkers(ChannelMap markerMap, int chan) {
    
    NeesEvent[] neesMarkers = null;
    String[] channelData = null;
    
    try {
      
      channelData = markerMap.GetDataAsString(chan);

      neesMarkers = new NeesEvent[channelData.length];
      
      for (int i=0; i<channelData.length; i++) {
        log.debug("data: " + channelData[i]);
        neesMarkers[i] = new NeesEvent();
        neesMarkers[i].setFromEventXml(channelData[i]);
        log.debug("content: " + neesMarkers[i].getProperty("content"));
        setMarkerMinMaxTimes(Double.parseDouble(neesMarkers[i].getProperty("timestamp")));

      }
      
    } catch (TransformerException se) {
    
      log.error("getChannelMarkers() - TransformerException: " + se.getMessage());
    } catch (Exception e) {
      log.error("getChannelMarkers() - Exception: " + e.getMessage());      
    }
    
    return neesMarkers;
    
  }
  
}  
