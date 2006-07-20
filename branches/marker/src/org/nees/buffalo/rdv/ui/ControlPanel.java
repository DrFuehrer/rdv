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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;
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
 * The UI to act as the control panel for data playback.
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class ControlPanel extends JPanel implements AdjustmentListener, TimeListener, StateListener, SubscriptionListener, MetadataListener, PlaybackRateListener {

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

	private JScrollBar locationScrollBar;

 	public double timeScales[] = {0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0, 600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0, 172800.0, 432000.0};
 	private double playbackRates[] = {1e-3, 2e-3, 5e-3, 1e-2, 2e-2, 5e-2, 1e-1, 2e-1, 5e-1, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000}; 

  protected MarkerEventsPanel markerPanel = null;
  public JLabel markerLabel = null;
 	
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
    timeScaleComboBox.setToolTipText("The amount of data to display");
    timeScaleComboBox.setPreferredSize(new Dimension(64,16));
    for (int i=0; i<timeScales.length; i++) {
      timeScaleComboBox.addItem(DataViewer.formatSeconds(timeScales[i]));
    }
    timeScaleComboBox.setSelectedIndex(getTimeScaleIndex(timeScale));
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
		
		locationScrollBar = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, 1);
		locationScrollBar.addAdjustmentListener(this);
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
		container.add(locationScrollBar, c);
   
    markerPanel = new MarkerEventsPanel(this);    
    markerPanel.setBorder(BorderFactory.createEtchedBorder());
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(8,8,8,8);
		c.anchor = GridBagConstraints.NORTHWEST;
    container.add (markerPanel, c);
    rbnbController.getMetadataManager().addMarkerListener(markerPanel); 
    
    add(container, BorderLayout.CENTER);        
		
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
    if (e.getSource() == locationScrollBar) {
			locationChange();
		}
	}
  
  public void setTimeScale(double timeScale) {
    int index = Arrays.binarySearch(timeScales, timeScale);
    if (index >= 0) {
      timeScaleComboBox.setSelectedIndex(index);
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
	
  public void playbackRateChanged(double playbackRate) {
    String playbackRateText;
    if (playbackRate < 1) {
      playbackRateText = Double.toString(playbackRate);
    } else {
      playbackRateText = Long.toString(Math.round(playbackRate));
    }
    
    /* while (playbackRateText.length() < 6) {
      playbackRateText = " " + playbackRateText;
    } */
    
    playbackRateLabel.setText(playbackRateText);
  }    
  
  private void decreasePlaybackRate() {
    double playbackRate = rbnbController.getPlaybackRate();
    int index = Arrays.binarySearch(playbackRates, playbackRate);
    if (index > 0) {
      rbnbController.setPlaybackRate(playbackRates[index-1]);
    }
  }
  
  private void increasePlaybackRate() {
    double playbackRate = rbnbController.getPlaybackRate();
    int index = Arrays.binarySearch(playbackRates, playbackRate);
    if (index < playbackRates.length-1) {
      rbnbController.setPlaybackRate(playbackRates[index+1]);
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
  
    int value = timeScaleComboBox.getSelectedIndex();
    
    timeScale = timeScales[value];
    
    if (timeScale != oldTimeScale) {
      rbnbController.setTimeScale(timeScale);
      
      log.debug("Time scale slider changed to " + timeScale + ".");
    }
  }
	
	
	// Player Time Methods
	
	public void postTime(double time) {
		setSliderLocation(time);
    
    locationLabel.setText(DataViewer.formatDate(time));
	}

	
	// Player State Methods

	public void postState(int newState, int oldState) {
		playerState = newState;
		
    monitorButton.setSelected(newState == Player.STATE_MONITORING);    
    playButton.setSelected(newState == Player.STATE_PLAYING);
    
    if (newState == Player.STATE_MONITORING) {
      slowerButton.setEnabled(false);
			fasterButton.setEnabled(false);
		} else if (oldState == Player.STATE_MONITORING) {
      slowerButton.setEnabled(true);
      fasterButton.setEnabled(true);
		}
	}
  
  public void setEnabled(boolean enabled) {
    monitorButton.setEnabled(enabled);
    playButton.setEnabled(enabled);
    beginButton.setEnabled(enabled);
    slowerButton.setEnabled(enabled);
    playbackRateLabel.setEnabled(enabled);
    fasterButton.setEnabled(enabled);    
    endButton.setEnabled(enabled);
    timeScaleComboBox.setEnabled(enabled);
    locationLabel.setEnabled(enabled);
    locationScrollBar.setEnabled(enabled);
    markerPanel.setEnabled(enabled);
    if (!enabled) {
      markerPanel.clearDisplay();
    }
  }


	// Player Subscription Methods
	
	public void channelSubscribed(String channelName) {
		updateTimeBoundaries();
	}

	public void channelUnsubscribed(String channelName) {
		updateTimeBoundaries();
	}
}
