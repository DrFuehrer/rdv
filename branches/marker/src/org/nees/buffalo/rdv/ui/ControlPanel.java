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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.SubscriptionListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;
import org.nees.rbnb.marker.NeesEvent;
import org.nees.rbnb.marker.MarkerEventsPanel;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelTree;

/**
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class ControlPanel extends JPanel implements AdjustmentListener, TimeListener, StateListener, SubscriptionListener, MetadataListener {

	static Log log = LogFactory.getLog(ControlPanel.class.getName());

	public RBNBController rbnbController;

	private JButton monitorButton;
	private JButton startButton;
	private JButton pauseButton;
	private JButton beginButton;
	private JButton endButton;

	private JScrollBar locationScrollBar;
	private JScrollBar playbackRateScrollBar;
	private JScrollBar timeScaleScrollBar;

 	public double timeScales[] = {0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0, 600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0, 172800.0, 432000.0};
 	private double playbackRates[] = {1e-3, 2e-3, 5e-3, 1e-2, 2e-2, 5e-2, 1e-1, 2e-1, 5e-1, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000}; 
//////////////////////////////////////////////////////////////////////////// LJM
  protected MarkerEventsPanel markerPanel = null;
  public JLabel markerLabel = null;
  protected JToolTip markerPanelToolTip = null;
  /* Marker panel interval limits */
  /** An array of event markers generated from an appropriate channel in
    * in the DataTurbine to which this RDV is connected. */
//////////////////////////////////////////////////////////////////////////// LJM
 	
	public double startTime;
	private double endTime;
	private double location;
	private double playbackRate;
	private double timeScale;
	
	int sliderLocation;
	
	int playerState;
	
	public ChannelTree ctree;

	public ControlPanel(RBNBController rbnbController) {
		super();

		this.rbnbController = rbnbController;
		
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
    
    JPanel p = new JPanel();
    p.setBorder(null);
    p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
    
      beginButton = new JButton("Begin", DataViewer.getIcon("icons/begin.gif"));
      beginButton.setToolTipText("Begin");
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
		
    pauseButton = new JButton("Pause", DataViewer.getIcon("icons/pause.gif"));
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
		
    monitorButton = new JButton("Real Time", DataViewer.getIcon("icons/rt.gif"));
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
		
		startButton = new JButton("Play", DataViewer.getIcon("icons/play.gif"));
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
		
		endButton = new JButton("End", DataViewer.getIcon("icons/end.gif"));
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

    // marker display panel    
    markerLabel = new JLabel ("Event Marker");
		c.fill = GridBagConstraints.HORIZONTAL;
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
    markerLabel.setVisible(false);
		container.add (markerLabel, c);
    
    /** A panel that displays a time-line-like view of markers present in the turbine */
    markerPanel = new MarkerEventsPanel(this);
    
    markerPanel.setBorder (BorderFactory.createEtchedBorder ());
    markerPanel.setToolTipText ("tipsy");
/*    
    markerPanel.addMouseListener (new MouseAdapter () {
      public void mouseClicked (MouseEvent e) {
        // DOTOO disable the submission panel
        if (! rbnbController.isConnected ()) {
          return;
        }
      
        // Iff its a left-click, else we lose real-time state
        if (e.getButton () ==  1) {
          // scale factor is pixels/time
          double guiTime = ((e.getX () / markerPanel.getMarkerScaleFactor ()) +
                          markerPanel.eventsChannelStartTime);
        
          // Go to the end if the mouse click is close
          if ((markerPanel.getWidth () - markerPanel.FUDGE_FACTOR) < e.getX ()) {
            location = endTime;
            setSliderLocation (endTime);
          } else {
            location = guiTime;
            setSliderLocation (guiTime);
          }
          locationChange ();
          // This updates the whole display
          rbnbController.setLocation (location);
          markerPanel.repaint ();
        } // if click is button 1 ONLY
      
      } // mouseClicked ()
      
      public void mouseDragged   (MouseEvent e) {}
      public void mouseEntered   (MouseEvent e) {}
      public void mouseExited    (MouseEvent e) {}
      public void mousePressed   (MouseEvent e) {
        markerPanel.doPopup (e);
      } // mousePressed ()
      
      public void mouseReleased  (MouseEvent e) {
        markerPanel.doPopup (e);
      } // mouseReleased ()
    } // MouseAdaptor definition
                                  ); // addMouseListener
*/ 
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
    container.add (markerPanel, c);
    rbnbController.getMetadataManager().addMarkerListener(markerPanel);
    log.info ("Added Event Marker Panel to Control Panel.");
//////////////////////////////////////////////////////////////////////////// LJM 
    
		JLabel timeScaleLabel = new JLabel("Time Scale");
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
		container.add(timeScaleLabel, c);			

		int timeScaleIndex = getTimeScaleIndex(timeScale);
		timeScaleScrollBar = new JScrollBar(Adjustable.HORIZONTAL, timeScaleIndex, 1, 0, timeScales.length);
 		timeScaleScrollBar.addAdjustmentListener(this);
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
		container.add(timeScaleScrollBar, c);
		
		JLabel playbackRateLabel = new JLabel("Playback Rate");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 4;
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
		c.gridy = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;		
		container.add(playbackRateScrollBar, c);
		
		// Add into the main overall GUI
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
	
	public void channelTreeUpdated(ChannelTree ctree) {
		this.ctree = ctree;
		
		updateTimeBoundaries();

		log.info("Received updated channel metatdata.");
	}
	
	private void updateTimeBoundaries() {
    // We haven't got the metadata channel tree yet
    if (ctree == null) {
      return;
    }

		double startTime = -1;
		double endTime = -1;
		
		Iterator it = ctree.iterator();
		while (it.hasNext()) {
            ChannelTree.Node node = (ChannelTree.Node)it.next();
			String channelName = node.getFullName();
      
            // don't let event marker channels influence the time bounds
            if (NeesEvent.MIME_TYPE.equals(node.getMime())) {
              continue;
            }
      
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
  
  public void setPlaybackRate(double playbackRate) {
    int index = Arrays.binarySearch(playbackRates, playbackRate);
    if (index >= 0) {
      playbackRateScrollBar.setValue(index);
    }
  }
  
  public void setTimeScale(double timeScale) {
    int index = Arrays.binarySearch(timeScales, timeScale);
    if (index >= 0) {
      timeScaleScrollBar.setValue(index);
    }    
  }

	public void locationChange() {	
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

  // LJM
  public double getTimeScale () {
    return this.timeScale;
  }
  
	
	private void timeScaleChange() {
		double oldTimeScale = timeScale;
	
		int value = timeScaleScrollBar.getValue();
		
		timeScale = timeScales[value];
		
		if (timeScale != oldTimeScale) {
			rbnbController.setTimeScale(timeScale);
			
			log.debug("Time scale slider changed to " + timeScale + ".");
		}
    
    // LJM
    this.markerPanel.repaint ();
	}
	
	
	// Player Time Methods
	
	public void postTime(double time) {
		setSliderLocation(time);
	}

	
	// Player State Methods

	public void postState(int newState, int oldState) {
		playerState = newState;
		
		if (newState == Player.STATE_DISCONNECTED) {
			disableUI();
		} else if (oldState == Player.STATE_DISCONNECTED && newState != Player.STATE_EXITING) {
			enableUI();
		} else if (newState == Player.STATE_MONITORING) {
			playbackRateScrollBar.setEnabled(false);
		} else if (oldState == Player.STATE_MONITORING) {
			playbackRateScrollBar.setEnabled(true);
		}
	}
	
	private void disableUI() {
		monitorButton.setEnabled(false);
		startButton.setEnabled(false);
		pauseButton.setEnabled(false);
		beginButton.setEnabled(false);
		endButton.setEnabled(false);
/////////////////////////////////////////////////////////////////////////////LJM
    markerPanel.setEnabled (false);
    markerPanel.clearDisplay ();
/////////////////////////////////////////////////////////////////////////////LJM
		locationScrollBar.setEnabled(false);
		playbackRateScrollBar.setEnabled(false);
		timeScaleScrollBar.setEnabled(false);
		
		log.info("Control Panel UI disabled.");
	}
	
	private void enableUI() {
		monitorButton.setEnabled(true);
		startButton.setEnabled(true);
		pauseButton.setEnabled(true);
		beginButton.setEnabled(true);
		endButton.setEnabled(true);
/////////////////////////////////////////////////////////////////////////////LJM
    markerPanel.setEnabled (true);
    markerPanel.repaint ();
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
