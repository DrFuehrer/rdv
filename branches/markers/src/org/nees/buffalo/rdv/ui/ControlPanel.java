/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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
 * $URL: https://code.nees.buffalo.edu/repository/RDV/branches/markers/src/org/nees/buffalo/rdv/ui/ControlPanel.java $
 * $Revision: 332 $
 * $Date: 2005-11-23 15:03:01 -0800 (Wed, 23 Nov 2005) $
 * $Author: ljmiller $
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Adjustable;
import java.awt.BorderLayout;

//LJM
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Vector;
import javax.xml.transform.TransformerException;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Iterator;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

//LJM
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.JScrollPane;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
//LJM
import org.nees.buffalo.rdv.rbnb.NeesEventDataListener;

import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.SubscriptionListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;

//LJM
import org.nees.rbnb.marker.NeesEvent;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

/**
 * @author Jason P. Hanley
 */
public class ControlPanel extends JPanel implements AdjustmentListener, TimeListener, StateListener, SubscriptionListener, MetadataListener {

	static Log log = LogFactory.getLog(ControlPanel.class.getName());

	private RBNBController rbnbController;

	private JButton monitorButton;
	private JButton startButton;
	private JButton pauseButton;
	private JButton beginButton;
	private JButton endButton;

	private JScrollBar locationScrollBar;
	private JScrollBar playbackRateScrollBar;
	private JScrollBar timeScaleScrollBar;

 	private double timeScales[] = {1e-6, 2e-6, 5e-6, 1e-5, 2e-5, 5e-5, 1e-4, 2e-4, 5e-4, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0, 600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0, 172800.0, 432000.0};
 	private double playbackRates[] = {1e-3, 2e-3, 5e-3, 1e-2, 2e-2, 5e-2, 1e-1, 2e-1, 5e-1, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000}; 
//////////////////////////////////////////////////////////////////////////// LJM
  /** A variable to switch on the usage of synthetic marker data. */
  public boolean usingFakeEvents = true; 
  /** A pair of variables that will be used to make a synthetic data source. */
  long timeNow = System.currentTimeMillis ();
  double cannedTimeBase = (double)timeNow / 1000.0;
  //double cannedTimeBase = 1.137111194512E9;
  double cannedTimeInterval = 5.0;
  private double markerTimes[] = {
    cannedTimeBase + 1*cannedTimeInterval, cannedTimeBase + 2*cannedTimeInterval,
    cannedTimeBase + 3*cannedTimeInterval, cannedTimeBase + 4*cannedTimeInterval,
    cannedTimeBase + 5*cannedTimeInterval, cannedTimeBase + 6*cannedTimeInterval
   };
   /* Need these to have global scope in this class to interact with it between
     * methods. Perhaps the thing to do is make a trivial data structure class?
   */
   protected JPanel markerPanel = null;
   private JLabel markerLabel = null;
   /* Marker panel interval limits */
   private double markerPanelStart, markerPanelEnd, markerPanelScaleFactor;
   private int markerXcoordinate;
   /** An array of event markers generated from an appropriate channel in
     * in the DataTurbine to which this RDV is connected. */
   protected NeesEvent[] theEvents = null;
   /** A variable to indicate whether or not an events channel has been found */
   private boolean foundEventsChannel = false;
   /** The name of an event channel, if found. */
   private String eventsChannelName = "Not Found.";
   private int eventsChannelIndex = -1;
   private  NeesEventDataListener neesEventListener = null;
//////////////////////////////////////////////////////////////////////////// LJM
   
	private double startTime;
	private double endTime;
	private double location;
	private double playbackRate;
	private double timeScale;
	
	int sliderLocation;
	
	int playerState;
	
	ChannelTree ctree;

	public ControlPanel(RBNBController rbnbController) {
		super();

		this.rbnbController = rbnbController;
//////////////////////////////////////////////////////////////////////////// LJM
    this.neesEventListener = new NeesEventDataListener ();
//////////////////////////////////////////////////////////////////////////// LJM
    
    
		startTime = -1;
		endTime = -1;
		location = -1;
		playbackRate = rbnbController.getPlaybackRate();
		
		timeScale = rbnbController.getTimeScale();
		
		initPanel();
		
		locationScrollBar.removeAdjustmentListener(this);
		locationScrollBar.setVisibleAmount((int)(playbackRate*1000));
		locationScrollBar.setUnitIncrement((int)(playbackRate*1000));			
		locationScrollBar.setBlockIncrement((int)(playbackRate*1000));
		locationScrollBar.addAdjustmentListener(this);
	}
	
	private void initPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    
    ClassLoader cl = getClass().getClassLoader();
    
    JPanel p = new JPanel();
    p.setBorder(null);
    p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
    beginButton = new JButton("Begining", new ImageIcon(cl.getResource("icons/begin.gif")));
    beginButton.setToolTipText("Go to beginning");
    beginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLocationBegin();
      }
    });
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
 		p.add(beginButton, c);		
		
    pauseButton = new JButton("Pause", new ImageIcon(cl.getResource("icons/pause.gif")));
    pauseButton.setToolTipText("Pause");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rbnbController.pause();
      }
    });    
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
 		p.add(pauseButton, c);		
		
    monitorButton = new JButton("Real Time", new ImageIcon(cl.getResource("icons/rt.gif")));
    monitorButton.setToolTipText("Real Time");
    monitorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rbnbController.monitor();
      }
    });
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		p.add(monitorButton, c);
		
		startButton = new JButton("Play", new ImageIcon(cl.getResource("icons/play.gif")));
 		startButton.setToolTipText("Play");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbnbController.play();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		p.add(startButton, c);
		
		endButton = new JButton("End", new ImageIcon(cl.getResource("icons/end.gif")));
 		endButton.setToolTipText("Go to end");
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLocationEnd();
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		p.add(endButton, c);

		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());		
		
//////////////////////////////// LJM - various Y gridbag coordinates incremented
    markerLabel = new JLabel ("Event Markers");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		container.add (markerLabel, c);
    
    /** A variable that defines how the marker display panel displays itself
      * graphically.
     */
    markerPanel = new JPanel () {
      public void paintComponent (Graphics markerPanelG) {
        super.paintComponent (markerPanelG);
        /** Necessary to limit this scale factor to 1 as imprecision in
          * double arithmetic adversely effects the integral JPanel X
          * coordinate for drawing the scaled position of markers.
         */
        theEvents = getNeesEvents (ctree);
        //log.debug ("returned from getNeesEvents ()");
        // if we have some events to look at, display them.
        if (theEvents != null) {
          String markerType = null;
          for (int i=0; i<theEvents.length; i++) {
            markerType = theEvents[i].getProperty ("EventType");
            if ( markerType.compareToIgnoreCase ("Start") == 0 ) {
              markerPanelG.setColor (NeesEvent.startColor); 
            } else if ( markerType.compareToIgnoreCase ("Stop") == 0 ) {
              markerPanelG.setColor (NeesEvent.stopColor);
            } else {
              markerPanelG.setColor (Color.black);
            } // else
            updateMarkerPanelScaleFactor ();
            // x=t*width/(e-s)
            markerXcoordinate =
              (int)( (Double.parseDouble (theEvents[i].getProperty ("TimeStamp")) - startTime ) * markerPanelScaleFactor);
            markerPanelG.fillRect (markerXcoordinate, 0, 3, 10);
            log.debug ("Drew a " + markerType + " marker at x: " +
                       Integer.toString (markerXcoordinate) + "\n" +
                       "at time (from XML): " +
                        theEvents[i].getProperty ("TimeStamp") + "\n" +
                       "at nice time (from XML): " +
                        DataViewer.formatDate
                          (Double.parseDouble
                           (theEvents[i].getProperty ("TimeStamp"))) + "\n" +
                       "With scale factor: " + markerPanelScaleFactor
                       );
          } // for
        } else { // no markers
          log.debug ("No Marker Data - null.");
          markerLabel.setText ("");
          markerPanelG.drawString ("No Markers Detected", 1, 1);
        } // else
      } // paintComponent ()
    }; // markerPanel
    markerPanel.setBorder (BorderFactory.createEtchedBorder ());
    markerPanel.addMouseListener (new MouseAdapter () {
      public void mouseClicked (MouseEvent e) {
        // scale factor is pixels/time
        double guiTime = ((e.getX () / markerPanelScaleFactor) + startTime);
        // TODO set the location to the GUI click time.
        setSliderLocation (guiTime);
        log.debug ("\nGUI Click Time: " + Double.toString (guiTime) + "\n" +
                   "Nice GUI Click Time: " + DataViewer.formatDate (guiTime)
                   );
        markerPanel.repaint ();
      } // mouseClicked ()
      public void mouseDragged   (MouseEvent e) {}
      public void mouseEntered   (MouseEvent e) {}
      public void mouseExited    (MouseEvent e) {}
      public void mousePressed   (MouseEvent e) {}
      public void mouseReleased  (MouseEvent e) {}
    }
                                  );
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;	
    container.add (markerPanel, c);
    log.info ("Added Marker Panel to Control Panel.");
//////////////////////////////////////////////////////////////////////////// LJM      
      
		JLabel locationLabel = new JLabel("Time");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		container.add(locationLabel, c);
		
      locationScrollBar = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, 1);
		locationScrollBar.addAdjustmentListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;		
      container.add(locationScrollBar, c);

		JLabel timeScaleLabel = new JLabel("Time Scale");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
		container.add(timeScaleLabel, c);			

		int timeScaleIndex = getTimeScaleIndex(timeScale);
		timeScaleScrollBar = new JScrollBar(Adjustable.HORIZONTAL, timeScaleIndex, 1, 0, timeScales.length);
 		timeScaleScrollBar.addAdjustmentListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;		
		container.add(timeScaleScrollBar, c);
		
		JLabel playbackRateLabel = new JLabel("Playback Rate");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
		container.add(playbackRateLabel, c);		
		
		int playbackRateIndex = getPlaybackRateIndex(playbackRate);
 		playbackRateScrollBar = new JScrollBar(Adjustable.HORIZONTAL, playbackRateIndex, 1, 0, playbackRates.length);
		playbackRateScrollBar.addAdjustmentListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;		
		container.add(playbackRateScrollBar, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(0,0,0,0);
		c.anchor = GridBagConstraints.NORTHWEST;
		p.add(container, c);
        
    SimpleInternalFrame sif = new SimpleInternalFrame(
        DataViewer.getIcon("icons/control.gif"),
        "Control Panel",
        null,
        p);

    add(sif, BorderLayout.CENTER);        
		log.info("Initialized control panel.");
	}
   
/////////////////////////////////////////////////////////////////////////////LJM
  /**
    * A method to get event markers from the DataTurbine source keyed on the
    * NEESEvent mime type.
    * @return org.nees.rbnb.marker.NeesEvent
   */
  public NeesEvent[] getNeesEvents (ChannelTree ceetree) {
    NeesEvent[] retval = null;
    if (usingFakeEvents) {
      log.info ("Making fake events.");
      return makeFakeEvents ();
    } else if (ceetree != null) {
      Iterator it = ceetree.iterator ();
      ChannelTree.Node node;
      String channelName = null;
      String channelMime = null;
      while (it.hasNext ()) {
        node = (ChannelTree.Node)it.next ();
        channelName = node.getFullName ();
        channelMime = node.getMime ();
        if (channelMime != null &&
            channelMime.compareToIgnoreCase (NeesEvent.MIME_TYPE) == 0)
        {
          foundEventsChannel = true;
          eventsChannelName = channelName;
          log.debug ("*** Marker Channel Found: " + channelName + "\n");
          markerLabel.setText ("Event Markers.");
          if (this.rbnbController.subscribe (channelName, neesEventListener)) {
            retval = (neesEventListener == null)? null : 
              neesEventListener.getEventData (eventsChannelName);
          } else {
            log.error ("Couldn't subscribe to channel \"" + channelName + "\"");
          }
        } // if
      } // while
    } else { // ceetree is null
      log.debug ("Channel tree is null.");
    }
    log.info ("Got updated NeesEvents channels.");
    
    if (retval == null) {
      log.debug ("Got an array of events of size 0");
    } else {
      log.debug ("Got an array of events of size " + 
                 Integer.toString (retval.length));
    } // else
    
    return retval;
  } // getNeesEvents ()
  
  /**
    * A method to generate synthetic marker data.
    * @return org.nees.rbnb.marker.NeesEvent
   */
  NeesEvent[] makeFakeEvents () {
    Vector eventVector = new Vector ();
    NeesEvent eventTemp = null;
    for (int i=0; i<markerTimes.length; i++) {
      eventTemp = new NeesEvent ();
      eventTemp.setProperty ("TimeStamp", Double.toString (markerTimes[i]));
      eventTemp.setProperty ("SourceType", "Other");
      eventTemp.setProperty ("Annotation", "Synthetic Marker");
      if (i%3 == 0) {
        eventTemp.setProperty ("EventType", "Start");
      } else if (i%3 == 2) {
        eventTemp.setProperty ("EventType", "Stop");
      } else {
        eventTemp.setProperty ("EventType", "Annotation");
      }
      eventVector.add (eventTemp);
    } // for
    Object [] retValTmp = eventVector.toArray ();
    NeesEvent[] retVal= new NeesEvent [retValTmp.length];
    
    for (int j=0; j<retValTmp.length; j++) {
      retVal[j] = (NeesEvent)retValTmp[j];
    }
    //log.debug ("Made fake events.");
    markerLabel.setText ("Event Markers*.");
    return retVal;
  } // makeFakeEvents ()
  
  /** A method to keep the marker display panel time scale factor in sync with 
    * the time interval being displayed.
   */
  private void updateMarkerPanelScaleFactor () {
    markerPanelScaleFactor =
    (markerPanel.getWidth () / (endTime - startTime));
    //log.debug ("End-Start: " + Double.toString (endTime - startTime));
    /*log.debug ("Start: " + Double.toString (startTime) + "\n" +
               "Nice start time: " + DataViewer.formatDate (startTime) + "\n" +
               "End: " + Double.toString (endTime) + "\n" +
               "Nice end time: " + DataViewer.formatDate (endTime)
               );*/
  }
   
	public void channelTreeUpdated(ChannelTree ctree) {
		this.ctree = ctree;
		updateTimeBoundaries();
/////////////////////////////////////////////////////////////////////////////LJM
    updateMarkerPanelScaleFactor ();
    markerPanel.repaint ();
/////////////////////////////////////////////////////////////////////////////LJM
		log.info("Received updated channel metatdata.");
	}
	
	private void updateTimeBoundaries() {
		double startTime = -1;
		double endTime = -1;
		
		Iterator it = ctree.iterator();
		while (it.hasNext()) {
            ChannelTree.Node node = (ChannelTree.Node)it.next();
			String channelName = node.getFullName();
			if (rbnbController.isSubscribed(channelName)) {
				double channelStart = node.getStart();
				double channelDuration = node.getDuration();
				double channelEnd = channelStart+channelDuration;
				if (startTime == -1 || channelStart < startTime) {
					startTime = channelStart;
				}
				if (endTime == -1 || channelEnd > endTime) {
					endTime = channelEnd;
				}
			}
		}
		
		if (startTime == -1) {
			return;
		}
		
		setSliderBounds(startTime, endTime);
/////////////////////////////////////////////////////////////////////////////LJM
    updateMarkerPanelScaleFactor ();
    markerPanel.repaint ();
/////////////////////////////////////////////////////////////////////////////LJM
	}	
		
	private void setSliderBounds(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;

		log.info("Setting time to start at " + DataViewer.formatDate(startTime) + " and end at " + DataViewer.formatDate(endTime) + " seconds.");
		
		if (playerState != Player.STATE_MONITORING && playerState != Player.STATE_PLAYING) {
			refreshSliderBounds();
			
			double location = rbnbController.getLocation();
			if (location < startTime) {
				log.warn("Current time (" + DataViewer.formatDate(location) + ") is before known start time (" + DataViewer.formatDate(endTime) + ").");
				setLocationBegin();
			} else if (location > endTime) {
				log.warn("Current time (" + DataViewer.formatDate(location) + ") is past known end time (" + DataViewer.formatDate(endTime) + ").");
				setLocationEnd();
			}
		}
/////////////////////////////////////////////////////////////////////////////LJM
    updateMarkerPanelScaleFactor ();
    markerPanel.repaint ();
/////////////////////////////////////////////////////////////////////////////LJM
  }
	
	private void refreshSliderBounds() {
		int intDurationTime = (int)((endTime-startTime)*1000);
		
		locationScrollBar.removeAdjustmentListener(this);
		locationScrollBar.setMinimum(0);
		locationScrollBar.setMaximum(intDurationTime);
		locationScrollBar.addAdjustmentListener(this);
	}
		
	public void setSliderLocation(double location) {
		if (rbnbController.getRequestedLocation() == -1 && !locationScrollBar.getValueIsAdjusting()) {
			int sliderLocation = (int)((location-startTime)*1000);
			if (this.sliderLocation != sliderLocation) {
				this.location = location;
				if (location < startTime) {
					startTime = location;
					refreshSliderBounds();
				} else if (location > endTime) {
					endTime = location;
					refreshSliderBounds();
				}
						
				this.sliderLocation = sliderLocation;
        while (locationScrollBar.getAdjustmentListeners().length != 0) {
        	locationScrollBar.removeAdjustmentListener(this);
        }
				locationScrollBar.setValue(sliderLocation);
				locationScrollBar.addAdjustmentListener(this);
			}
		}
/////////////////////////////////////////////////////////////////////////////LJM
    updateMarkerPanelScaleFactor ();
    markerPanel.repaint ();
/////////////////////////////////////////////////////////////////////////////LJM    
   }
	
	public void setLocationBegin() {
		log.info("Setting location to begining.");
		rbnbController.setLocation(startTime);
	}
	
	public void setLocationEnd() {
		log.info("Setting location to end");
		rbnbController.setLocation(endTime);
	}
			
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == playbackRateScrollBar) {
			playbackRateChange();
		} else if (e.getSource() == locationScrollBar) {
			locationChange();
		} else if (e.getSource() == timeScaleScrollBar) {
			timeScaleChange();
		}
	}

	private void locationChange() {	
		int sliderLocation = locationScrollBar.getValue();

		if (this.sliderLocation != sliderLocation) {
			this.sliderLocation = sliderLocation;
			double location = (((double)sliderLocation)/1000)+startTime;
			if (this.location != location) {
				this.location = location;
				rbnbController.setLocation(location);
			}
      }
   }
	
	private int getPlaybackRateIndex(double playbackRate) {
    int index = -1;
    if (playbackRate < playbackRates[0]) {
        this.playbackRate = playbackRates[0];
        index = 0;
    } else if (playbackRate > playbackRates[playbackRates.length-1]) {
        this.playbackRate = playbackRates[playbackRates.length-1];
        index = playbackRates.length-1;
    } else {    
        for (int i=0; i<playbackRates.length-1; i++) {
            if (playbackRate >= playbackRates[i] && playbackRate <= playbackRates[i+1]) {
                double down = playbackRate - playbackRates[i];
                double up = playbackRates[i+1] - playbackRate;
                if (up <= down) {
                    this.playbackRate = playbackRates[i+1];
                    index = i+1;
                } else {
                    this.playbackRate = playbackRates[i];
                    index = i;
                }
            }
        }
    }

    return index;
	}
	
	private void playbackRateChange() {
		double oldPlaybackRate = playbackRate;
		
		int value = playbackRateScrollBar.getValue();
			
		playbackRate = playbackRates[value];
        
		if (playbackRate != oldPlaybackRate) {
			rbnbController.setPlaybackRate(playbackRate);
			
			log.debug("Playback rate slider changed to " + playbackRate + ".");
			
			locationScrollBar.removeAdjustmentListener(this);
			locationScrollBar.setVisibleAmount((int)(playbackRate*1000));
			locationScrollBar.setUnitIncrement((int)(playbackRate*1000));			
			locationScrollBar.setBlockIncrement((int)(playbackRate*10000));
			locationScrollBar.addAdjustmentListener(this);
		}
	}

	private int getTimeScaleIndex(double timeScale) {
		int index = -1;
		if (timeScale < timeScales[0]) {
			this.timeScale = timeScales[0];
			index = 0;
		} else if (timeScale > timeScales[timeScales.length-1]) {
			this.timeScale = timeScales[timeScales.length-1];
			index = timeScales.length-1;
		} else {	
			for (int i=0; i<timeScales.length-1; i++) {
				if (timeScale >= timeScales[i] && timeScale <= timeScales[i+1]) {
					double down = timeScale - timeScales[i];
					double up = timeScales[i+1] - timeScale;
					if (up <= down) {
						this.timeScale = timeScales[i+1];
						index = i+1;
					} else {
						this.timeScale = timeScales[i];
						index = i;
					}
				}
			}
		}

		return index;
	}
	
	private void timeScaleChange() {
		double oldTimeScale = timeScale;
	
		int value = timeScaleScrollBar.getValue();
		
		timeScale = timeScales[value];
		
		if (timeScale != oldTimeScale) {
			rbnbController.setTimeScale(timeScale);         
/////////////////////////////////////////////////////////////////////////////LJM
      this.markerPanel.repaint ();
/////////////////////////////////////////////////////////////////////////////LJM
			log.debug("Time scale slider changed to " + timeScale + ".");
		}
	}
	
	// Player Time Methods
	public void postTime(double time) {
		setSliderLocation(time);
	}

	
	// Player State Methods

	public void postState(int newState, int oldState) {
		playerState = newState;
		
		if (newState == Player.STATE_DISCONNECTED) {
			disbaleUI();
		} else if (oldState == Player.STATE_DISCONNECTED && newState != Player.STATE_EXITING) {
			enableUI();
		} else if (newState == Player.STATE_MONITORING) {
			playbackRateScrollBar.setEnabled(false);
		} else if (oldState == Player.STATE_MONITORING) {
			playbackRateScrollBar.setEnabled(true);
		}
	}
	
	private void disbaleUI() {
		monitorButton.setEnabled(false);
		startButton.setEnabled(false);
		pauseButton.setEnabled(false);
		beginButton.setEnabled(false);
		endButton.setEnabled(false);
/////////////////////////////////////////////////////////////////////////////LJM
    markerPanel.setEnabled (false);
/////////////////////////////////////////////////////////////////////////////LJM
    locationScrollBar.setEnabled(false);
		playbackRateScrollBar.setEnabled(false);
		timeScaleScrollBar.setEnabled(false);
		
		log.info("Control Panel UI disbaled.");
	}
	
	private void enableUI() {
		monitorButton.setEnabled(true);
		startButton.setEnabled(true);
		pauseButton.setEnabled(true);
		beginButton.setEnabled(true);
		endButton.setEnabled(true);
/////////////////////////////////////////////////////////////////////////////LJM
    markerPanel.setEnabled (true);
/////////////////////////////////////////////////////////////////////////////LJM
		locationScrollBar.setEnabled(true);
    playbackRateScrollBar.setEnabled(true);
		timeScaleScrollBar.setEnabled(true);
		
		log.info("Control Panel UI enabled.");
	}

	
	// Player Subscription Methods
	
	public void channelSubscribed(String channelName) {
		updateTimeBoundaries();
	}

	public void channelUnsubscribed(String channelName) {
		updateTimeBoundaries();
	}
}
