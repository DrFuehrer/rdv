package org.nees.buffalo.rdv.ui;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.SubscriptionListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;

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
 	
	private double startTime;
	private double endTime;
	private double location;
	private double playbackRate;
	private double timeScale;
	
	int sliderLocation;
	
	int playerState;
	
	ChannelMap channelMap;

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
		
		JLabel locationLabel = new JLabel("Time");
		c.fill = GridBagConstraints.NONE;
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
		container.add(locationLabel, c);
		
		locationScrollBar = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, 1);
		locationScrollBar.addAdjustmentListener(this);
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
		container.add(locationScrollBar, c);

		JLabel timeScaleLabel = new JLabel("Time Scale");
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
		container.add(timeScaleLabel, c);			

		int timeScaleIndex = getTimeScaleIndex(timeScale);
		timeScaleScrollBar = new JScrollBar(Adjustable.HORIZONTAL, timeScaleIndex, 1, 0, timeScales.length);
 		timeScaleScrollBar.addAdjustmentListener(this);
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
		container.add(timeScaleScrollBar, c);
		
		JLabel playbackRateLabel = new JLabel("Playback Rate");
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
		container.add(playbackRateLabel, c);		
		
		int playbackRateIndex = getPlaybackRateIndex(playbackRate);
 		playbackRateScrollBar = new JScrollBar(Adjustable.HORIZONTAL, playbackRateIndex, 1, 0, playbackRates.length);
		playbackRateScrollBar.addAdjustmentListener(this);
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
        
    SimpleInternalFrame sif = new SimpleInternalFrame("Control Panel");
    sif.add(p);

    add(sif, BorderLayout.CENTER);        
		
		log.info("Initialized control panel.");
	}
	
	public void channelListUpdated(ChannelMap channelMap) {
		this.channelMap = channelMap;
		
		updateTimeBoundaries();
		
		log.info("Received updated channel metatdata.");
	}
	
	private void updateTimeBoundaries() {
		double startTime = -1;
		double endTime = -1;
		
		String[] channels = channelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			if (rbnbController.isSubscribed(channelName)) {
				int channelIndex = channelMap.GetIndex(channelName);
				double channelStart = channelMap.GetTimeStart(channelIndex);
				double channelDuration = channelMap.GetTimeDuration(channelIndex);
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
