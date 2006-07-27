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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.EventMarkerListener;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.SubscriptionListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;
import org.nees.rbnb.marker.EventMarker;

import com.rbnb.sapi.ChannelTree;

/**
 * The UI to act as the control panel for data playback.
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class ControlPanel extends JPanel implements TimeListener, StateListener, SubscriptionListener, MetadataListener, PlaybackRateListener, TimeAdjustmentListener, EventMarkerListener {

	static Log log = LogFactory.getLog(ControlPanel.class.getName());

	public RBNBController rbnbController;

	private JButton monitorButton;
	private JButton playButton;

	private JButton beginButton;
  private JButton fasterButton;
  private JLabel playbackRateLabel;
  private JButton slowerButton;
	private JButton endButton;
  
  private JComboBox timeScaleComboBox;
  
  private JLabel locationLabel;
  
  private TimeSlider zoomTimeSlider;
  
  private JLabel zoomMinimumLabel;
  private JLabel zoomRangeLabel;
  private JLabel zoomMaximumLabel;
  
  private TimeSlider globalTimeSlider;

 	private static final double timeScales[] = {0.001, 0.002, 0.005, 0.01, 0.02,
    0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0,
    600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0,
    172800.0, 432000.0, 604800.0};
  
 	private static final double playbackRates[] = {1e-3, 2e-3, 5e-3, 1e-2, 2e-2,
    5e-2, 1e-1, 2e-1, 5e-1, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000}; 

	public ChannelTree ctree;

  /**
   * Construct a control panel to control data playback.
   * 
   * @param rbnbController  the controller for data playback
   */
	public ControlPanel(RBNBController rbnbController) {
		super();

		this.rbnbController = rbnbController;
		
		initPanel();
    
    double location = rbnbController.getLocation();
    globalTimeSlider.setValues(location, 0, location, 0, location);
    
    rbnbController.getMarkerManager().addMarkerListener(this);
	}
	
  /**
   * Setup the UI.
   */
	private void initPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    
		GridBagConstraints c = new GridBagConstraints();
    
    JPanel container = new JPanel();
    container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
    container.setLayout(new GridBagLayout());
    
    JPanel firstRowPanel = new JPanel();
    firstRowPanel.setBorder(null);
    firstRowPanel.setLayout(new BorderLayout());
    
    JPanel controlsPanel = new JPanel();
    controlsPanel.setBorder(null);
    controlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
    
    monitorButton = new JButton(DataViewer.getIcon("icons/rt.gif"));
    monitorButton.setSelectedIcon(DataViewer.getIcon("icons/pause.gif"));
    monitorButton.setToolTipText("View live data");
    monitorButton.setBorder(null);
    monitorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (monitorButton.isSelected()) {
          rbnbController.pause();
        } else {
          rbnbController.monitor();
        }
      }
    });
    controlsPanel.add(monitorButton);
    
    playButton = new JButton(DataViewer.getIcon("icons/play.gif"));
    playButton.setSelectedIcon(DataViewer.getIcon("icons/pause.gif"));
    playButton.setToolTipText("Play");
    playButton.setBorder(null);
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (playButton.isSelected()) {
          rbnbController.pause();
        } else {
          rbnbController.play();
        }
      }
    });
    controlsPanel.add(playButton);
    
    controlsPanel.add(Box.createHorizontalStrut(8));
    
    beginButton = new JButton(DataViewer.getIcon("icons/begin.gif"));
    beginButton.setToolTipText("Go to beginning of data");
    beginButton.setBorder(null);
    beginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLocationBegin();
      }
    });
 		controlsPanel.add(beginButton);
    
    slowerButton = new JButton(DataViewer.getIcon("icons/rew.gif"));
    slowerButton.setToolTipText("Playback data slower");
    slowerButton.setBorder(null);
    slowerButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        decreasePlaybackRate();
      }
    });
    controlsPanel.add(slowerButton);
    
    playbackRateLabel = new JLabel();
    playbackRateLabel.setToolTipText("The current playback rate");
    playbackRateLabel.setHorizontalAlignment(JLabel.CENTER);
    playbackRateLabel.setPreferredSize(new Dimension(24,16));
    controlsPanel.add(playbackRateLabel);    
    
    fasterButton = new JButton(DataViewer.getIcon("icons/ff.gif"));
    fasterButton.setToolTipText("Playback data faster");
    fasterButton.setBorder(null);
    fasterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        increasePlaybackRate();
      }
    });    
    controlsPanel.add(fasterButton);    

		endButton = new JButton(DataViewer.getIcon("icons/end.gif"));
 		endButton.setToolTipText("Go to end of data");
    endButton.setBorder(null);
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLocationEnd();
			}
		});
		controlsPanel.add(endButton);
    
    controlsPanel.add(Box.createHorizontalStrut(8));
    
    timeScaleComboBox = new JComboBox();
    timeScaleComboBox.setEditable(true);
    timeScaleComboBox.setToolTipText("The amount of data to display");
    timeScaleComboBox.setPreferredSize(new Dimension(64,16));
    for (int i=0; i<timeScales.length; i++) {
      timeScaleComboBox.addItem(DataViewer.formatSeconds(timeScales[i]));
    }
    timeScaleComboBox.setSelectedIndex(getTimeScaleIndex(rbnbController.getTimeScale()));
    timeScaleComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        timeScaleChange();
      }
    });
    controlsPanel.add(timeScaleComboBox);
    
    firstRowPanel.add(controlsPanel, BorderLayout.CENTER);
    
    locationLabel = new JLabel();
    firstRowPanel.add(locationLabel, BorderLayout.EAST);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,8,8);
    c.anchor = GridBagConstraints.NORTHWEST;    
    container.add(firstRowPanel, c);    
		
    zoomTimeSlider = new TimeSlider();
    zoomTimeSlider.setRangeEnabled(false);
    zoomTimeSlider.addTimeAdjustmentListener(this);
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(0,8,0,8);
		c.anchor = GridBagConstraints.NORTHWEST;
    container.add(zoomTimeSlider, c);
    
    JPanel zoomTimePanel = new JPanel();
    zoomTimePanel.setLayout(new BorderLayout());
    
    zoomMinimumLabel = new JLabel();
    zoomTimePanel.add(zoomMinimumLabel, BorderLayout.WEST);
    
    zoomRangeLabel = new JLabel();
    zoomRangeLabel.setHorizontalAlignment(JLabel.CENTER);
    zoomTimePanel.add(zoomRangeLabel, BorderLayout.CENTER);
    
    zoomMaximumLabel = new JLabel();
    zoomTimePanel.add(zoomMaximumLabel, BorderLayout.EAST);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,0,8);
    c.anchor = GridBagConstraints.NORTHWEST;
    container.add(zoomTimePanel, c);    
    
    globalTimeSlider = new TimeSlider();
    globalTimeSlider.setValueChangeable(false);
    globalTimeSlider.addTimeAdjustmentListener(this);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,8,8);
    c.anchor = GridBagConstraints.NORTHWEST;
    container.add(globalTimeSlider, c);
    
    add(container, BorderLayout.CENTER);        
		
		log.info("Initialized control panel.");
	}
	
  /**
   * Called when the channel metadata is updated.
   */
	public void channelTreeUpdated(ChannelTree ctree) {
		this.ctree = ctree;
		
		updateTimeBoundaries();
	}
	
  /**
   * Update the boundaries of the time sliders based on the subscribed channel
   * bounds and the event marker bounds.
   */
	private void updateTimeBoundaries() {
    // We haven't got the metadata channel tree yet
    if (ctree == null) {
      return;
    }

    double startTime = -1;
    double endTime = -1;

    // get the time bounds for all channels
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
    
    // get the time bounds for all markers
    List<EventMarker> markers = rbnbController.getMarkerManager().getMarkers();
    for (EventMarker marker : markers) {
      double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
      if (startTime == -1 || markerTime < startTime) {
        startTime = markerTime;
      }
      if (endTime == -1 || markerTime > endTime) {
        endTime = markerTime;
      }
    }

    if (startTime == -1) {
      return;
    }
    
    double location = rbnbController.getLocation();
    if (location < startTime) {
      startTime = location;
    } else if (location > endTime) {
      endTime = location;
    }

    globalTimeSlider.setValues(startTime, endTime);
  }
		
  /**
   * Set the time to the minimum of the zoom time slider.
   */
	public void setLocationBegin() {
		rbnbController.setLocation(zoomTimeSlider.getMinimum());
	}
	
  /**
   * Set the time to the maximum of the zoom time sldier.
   */
	public void setLocationEnd() {
		rbnbController.setLocation(zoomTimeSlider.getMaximum());
	}
  
  /**
   * Set the displayed time scale in the UI.
   * 
   * @param timeScale  the time scale
   */
  public void setTimeScale(double timeScale) {
    int index = Arrays.binarySearch(timeScales, timeScale);
    if (index >= 0) {
      timeScaleComboBox.setSelectedIndex(index);
    }    
  }

  /**
   * Called when the playback rate changes. Update the UI display. 
   * 
   * @param playbackRate  the current playback rate
   */
  public void playbackRateChanged(double playbackRate) {
    String playbackRateText;
    if (playbackRate < 1) {
      playbackRateText = Double.toString(playbackRate);
    } else {
      playbackRateText = Long.toString(Math.round(playbackRate));
    }
    
    playbackRateLabel.setText(playbackRateText);
  }    
  
  /**
   * Called when the playback rate is decreased by the UI. This tells the rbnb
   * controller to decrease the playback rate.
   */
  private void decreasePlaybackRate() {
    double playbackRate = rbnbController.getPlaybackRate();
    int index = Arrays.binarySearch(playbackRates, playbackRate);
    if (index > 0) {
      rbnbController.setPlaybackRate(playbackRates[index-1]);
    }
  }

  /**
   * Called when the playback rate is increased by the UI. This tells the rbnb
   * controller to increase the playback rate.
   */
  private void increasePlaybackRate() {
    double playbackRate = rbnbController.getPlaybackRate();
    int index = Arrays.binarySearch(playbackRates, playbackRate);
    if (index < playbackRates.length-1) {
      rbnbController.setPlaybackRate(playbackRates[index+1]);
    }    
  }  

  /**
   * Returns the index for the given time scale from the array of predefined
   * time scales.
   * 
   * @param timeScale  the time scale to find
   * @return           the index of the time scale in the time scale array
   */
	private int getTimeScaleIndex(double timeScale) {
		int index = -1;
		if (timeScale < timeScales[0]) {
			index = 0;
		} else if (timeScale > timeScales[timeScales.length-1]) {
			index = timeScales.length-1;
		} else {	
			for (int i=0; i<timeScales.length-1; i++) {
				if (timeScale >= timeScales[i] && timeScale <= timeScales[i+1]) {
					double down = timeScale - timeScales[i];
					double up = timeScales[i+1] - timeScale;
					if (up <= down) {
						index = i+1;
					} else {
						index = i;
					}
				}
			}
		}

		return index;
	}

  /**
   * Called when the time scale changes in the UI. This sets the time scale in
   * the rbnb controller.
   */
  private void timeScaleChange() {
    double timeScale;
    
    int value = timeScaleComboBox.getSelectedIndex();
    if (value == -1) {
      String timeScaleString = (String)timeScaleComboBox.getSelectedItem();
      
      try {
        timeScale = DataViewer.parseTime(timeScaleString);
      } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this,
            "The time scale is not formatted correctly.",
            "Invalid time scale",
            JOptionPane.ERROR_MESSAGE);
        timeScale = rbnbController.getTimeScale();
      }
      
      if (timeScale <= 0) {
        JOptionPane.showMessageDialog(this,
            "The time scale must be greater than 0.",
            "Invalid time scale",
            JOptionPane.ERROR_MESSAGE);
        timeScale = rbnbController.getTimeScale();        
      }
      
      int timeScaleIndex = Arrays.binarySearch(timeScales, timeScale);      
      if (timeScaleIndex >= 0) {
        timeScaleComboBox.setSelectedIndex(timeScaleIndex);
      } else {
        timeScaleComboBox.setSelectedItem(DataViewer.formatSeconds(timeScale));
      }
    } else {
      timeScale = timeScales[value];
    }

    rbnbController.setTimeScale(timeScale);
  }
  
  /**
   * Called when the time changes in the UI. This sets the time in the rbnb
   * controller.
   * 
   * @param event  the time event
   */
  public void timeChanged(TimeEvent event) {
    if (event.getSource() == zoomTimeSlider) {
      rbnbController.setLocation(zoomTimeSlider.getValue());
    }
  }

  /**
   * Called whent the time range changes in the UI. This sets up the bounds for
   * the zoom time slider and updates UI displays. If the current time is
   * outside the new range, the time will be changes via the rbnb controller to
   * be within this range.
   * 
   * @param event  the time event
   */
  public void rangeChanged(TimeEvent event) {
    if (event.getSource() == globalTimeSlider) {
      double start = globalTimeSlider.getStart();
      double end = globalTimeSlider.getEnd();
      
      zoomTimeSlider.setValues(start, end);
      
      zoomMinimumLabel.setText(DataViewer.formatDate(start));
      zoomRangeLabel.setText(DataViewer.formatSeconds(end-start));
      zoomMaximumLabel.setText(DataViewer.formatDate(end));
      
      double location = rbnbController.getLocation();
      if (location < start) {
        rbnbController.setLocation(start);
      } else if (location > end) {
        rbnbController.setLocation(end);
      }

    }
  }  

  /**
   * Called when the bounds change in the UI.
   * 
   * @param event  the time event
   */
  public void boundsChanged(TimeEvent event) {}

	
	// Player Time Methods
	
  /**
   * Called when the time changes. This updates the slider and display
   * components in the UI. It will also adjust the bounds and range if needed.
   * 
   * @param time  the new time
   */
	public void postTime(double time) {
    if (time < globalTimeSlider.getMinimum()) {
      globalTimeSlider.setMinimum(time);
    } else if (time > globalTimeSlider.getMaximum()) {
      globalTimeSlider.setMaximum(time);
    }

    if (time < globalTimeSlider.getStart()) {
      globalTimeSlider.setStart(time);
    } else if (time > globalTimeSlider.getEnd()) {
      globalTimeSlider.setEnd(time);
    }
    
    if (!zoomTimeSlider.isValueAdjusting()) {
      zoomTimeSlider.removeTimeAdjustmentListener(this);
      zoomTimeSlider.setValue(time);
      zoomTimeSlider.addTimeAdjustmentListener(this);
    }
    
    globalTimeSlider.setValue(time);

    locationLabel.setText(DataViewer.formatDate(time));    
	}

	
	// Player State Methods

  /**
   * Called when the state of the rbnb controller changes. This updates various
   * UI elements depending on the state.
   * 
   * @param newState  the new controller state
   * @param oldState  the old controller state
   */
	public void postState(int newState, int oldState) {
    monitorButton.setSelected(newState == Player.STATE_MONITORING);    
    playButton.setSelected(newState == Player.STATE_PLAYING);
    
    slowerButton.setEnabled(newState != Player.STATE_MONITORING);
    playbackRateLabel.setEnabled(newState != Player.STATE_MONITORING);
    fasterButton.setEnabled(newState != Player.STATE_MONITORING);
	}
  
  /**
   * Set whether or not this component is enabled. If it is disabled, the UI
   * will not respond to user input. 
   * 
   * @param enabled  true if the component should be enabled, false otherwise
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    monitorButton.setEnabled(enabled);
    playButton.setEnabled(enabled);
    beginButton.setEnabled(enabled);

    if (enabled && rbnbController.getState() != Player.STATE_MONITORING) {
      slowerButton.setEnabled(true);
      playbackRateLabel.setEnabled(true);
      fasterButton.setEnabled(true);
    } else {
      slowerButton.setEnabled(false);
      playbackRateLabel.setEnabled(false);
      fasterButton.setEnabled(false);
    }

    endButton.setEnabled(enabled);
    timeScaleComboBox.setEnabled(enabled);
    locationLabel.setEnabled(enabled);
    zoomTimeSlider.setEnabled(enabled);
    zoomMinimumLabel.setEnabled(enabled);
    zoomRangeLabel.setEnabled(enabled);
    zoomMaximumLabel.setEnabled(enabled);
    globalTimeSlider.setEnabled(enabled);
  }


	// Player Subscription Methods
	
  /**
   * Called when a channel is subscribed to. This updates the time bounds.
   * 
   * @param channelName  the channel being subscribed to
   */
	public void channelSubscribed(String channelName) {
		updateTimeBoundaries();
	}

  /**
   * Called when a channel is unsubscribed to.
   * 
   * @param channelName  the channel being unsubscribed from
   */
	public void channelUnsubscribed(String channelName) {
		updateTimeBoundaries();
	}

  
  // Marker methods
  
  /**
   * Called when there is a new event marker. This adds the event marker to the
   * UI and updates the time bounds.
   * 
   * @param marker  the new event marker
   */
  public void eventMarkerAdded(EventMarker marker) {
    zoomTimeSlider.addMarker(marker);
    globalTimeSlider.addMarker(marker);
    
    updateTimeBoundaries();
  }
  
  /**
   * Called when all the event markers are removed 
   */
  public void eventMarkersCleared() {
    zoomTimeSlider.cleareMarkers();
    globalTimeSlider.cleareMarkers();
    
    updateTimeBoundaries();
  }
}
