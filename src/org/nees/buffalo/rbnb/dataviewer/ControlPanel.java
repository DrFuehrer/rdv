package org.nees.buffalo.rbnb.dataviewer;

import java.awt.Adjustable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EtchedBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class ControlPanel extends JPanel implements AdjustmentListener, PlayerTimeListener, PlayerStateListener, PlayerSubscriptionListener, ChannelListListener {

	static Log log = LogFactory.getLog(ControlPanel.class.getName());

	private DataViewer dataViewer;
	private Player player;

	private JButton monitorButton;
	private JButton startButton;
	private JButton pauseButton;
	private JButton beginButton;
	private JButton endButton;

	private JScrollBar locationScrollBar;
	private JScrollBar durationScrollBar;
	private JScrollBar domainScrollBar;

 	private double domains[] = {1e-6, 2e-6, 5e-6, 1e-5, 2e-5, 5e-5, 1e-4, 2e-4, 5e-4, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0, 600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0, 172800.0, 432000.0};
 	private int defaultDomainIndex = 18;

	private double startTime;
	private double endTime;
	private double location;
	private double timeScale;
	private double domain;
	
	int sliderLocation;
	
	int playerState;
	
	ChannelMap channelMap;
	
	ArrayList timeScaleChangeListeners;
	ArrayList domainChangeListeners;

	public ControlPanel(DataViewer dataViewer, Player player) {
		super();
		
		this.dataViewer = dataViewer;
		this.player = player;
		
		startTime = -1;
		endTime = -1;
		location = -1;
		timeScale = 1;
		domain = 1;
		
		initPanel();
		
		timeScaleChangeListeners = new ArrayList();
		domainChangeListeners = new ArrayList();
		
		locationScrollBar.removeAdjustmentListener(this);
		locationScrollBar.setVisibleAmount((int)(timeScale*1000));
		locationScrollBar.setUnitIncrement((int)(timeScale*1000));			
		locationScrollBar.setBlockIncrement((int)(timeScale*1000));
		locationScrollBar.addAdjustmentListener(this);
	}
	
	private void initPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		setBorder(new EtchedBorder());
		
 		monitorButton = new JButton("RT");
 		monitorButton.setToolTipText("Real Time");
 		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
 				player.monitor();
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
 		add(monitorButton, c);		
		
 		beginButton = new JButton("<<");
 		beginButton.setToolTipText("Go to beginning");
 		beginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
 				setLocationBegin();
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
 		add(beginButton, c);		
		
		pauseButton = new JButton("=");
 		pauseButton.setToolTipText("Pause");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player.pause();
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
		add(pauseButton, c);
		
		startButton = new JButton(">");
 		startButton.setToolTipText("Play");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player.play();
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
		add(startButton, c);
		
		endButton = new JButton(">>");
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
		add(endButton, c);

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

		JLabel domainLabel = new JLabel("Time Scale");
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
		container.add(domainLabel, c);			
		
		domainScrollBar = new JScrollBar(Adjustable.HORIZONTAL, defaultDomainIndex, 1, 0, domains.length);
 		domainScrollBar.addAdjustmentListener(this);
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
		container.add(domainScrollBar, c);
		
		JLabel timeScaleLabel = new JLabel("Playback Rate");
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
		
 		durationScrollBar = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, -9, 10);
		durationScrollBar.addAdjustmentListener(this);
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
		container.add(durationScrollBar, c);
		
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
		add(container, c);		
		
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
			if (player.isSubscribed(channelName)) {
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
			
			double location = player.getLocation();
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
		if (!locationScrollBar.getValueIsAdjusting()) {
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
				locationScrollBar.removeAdjustmentListener(this);
				locationScrollBar.setValue(sliderLocation);
				locationScrollBar.addAdjustmentListener(this);
			}
		}
	}
	
	public void setLocationBegin() {
		log.info("Setting location to begining.");
		player.setLocation(startTime);
	}
	
	public void setLocationEnd() {
		log.info("Setting location to end");
		player.setLocation(endTime);
	}
			
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == durationScrollBar) {
			durationChange();
		} else if (e.getSource() == locationScrollBar) {
			locationChange();
		} else if (e.getSource() == domainScrollBar) {
			domainChange();
		}
	}

	private void locationChange() {	
		int sliderLocation = locationScrollBar.getValue();
		
		if (this.sliderLocation != sliderLocation) {
			this.sliderLocation = sliderLocation;
			double location = (((double)sliderLocation)/1000)+startTime;
			if (this.location != location) {
				this.location = location;
				player.setLocation(location);
			}
		}
	}
	
	private void durationChange() {
		double oldTimeScale = timeScale;
		
		int value = durationScrollBar.getValue();
			
		if (value >= 0) {
			int quotient = value / 3;
			int mod = value % 3;
			
			if (mod == 0) {
				timeScale = 1*Math.pow(10, quotient);
			} else if (mod == 1) {
				timeScale = 2*Math.pow(10, quotient);
			} else if (mod == 2) {
				timeScale = 5*Math.pow(10, quotient);
			}				
		} else {
			value = value - 2;
			int quotient = value / 3;
			int mod = value % 3;			
			
			if (mod == 0) {
				timeScale = 5*Math.pow(10, quotient);
			} else if (mod == -1) {
				timeScale = 2*Math.pow(10, quotient);
			} else if (mod == -2) {
				timeScale = 1*Math.pow(10, quotient);
			}				
		}

		if (timeScale != oldTimeScale) {
			fireTimeScaleChanged(timeScale);
			
			log.debug("Time scale slider changed to " + timeScale + ".");
			
			locationScrollBar.removeAdjustmentListener(this);
			locationScrollBar.setVisibleAmount((int)(timeScale*1000));
			locationScrollBar.setUnitIncrement((int)(timeScale*1000));			
			locationScrollBar.setBlockIncrement((int)(timeScale*10000));
			locationScrollBar.addAdjustmentListener(this);
		}
	}
	
	public double getDomain() {
		return domain;
	}
	
	public double setDomain(double domain) {
		double oldDomain = this.domain;
		int index = -1;
		if (domain < domains[0]) {
			this.domain = domains[0];
			index = 0;
		} else if (domain > domains[domains.length-1]) {
			this.domain = domains[domains.length-1];
			index = domains.length-1;
		} else {	
			for (int i=0; i<domains.length-1; i++) {
				if (domain >= domains[i] && domain <= domains[i+1]) {
					double down = domain - domains[i];
					double up = domains[i+1] - domain;
					if (up <= down) {
						this.domain = domains[i+1];
						index = i+1;
					} else {
						this.domain = domains[i];
						index = i;
					}
				}
			}
		}
		
		if (this.domain != oldDomain && index != -1) {
			log.info("Setting time scale to " + this.domain);
			domainScrollBar.setValue(index);
			fireDomainChanged(this.domain);
		} else if (index == -1) {
			log.error("Did not set proper domain.");
		}
		
		return this.domain;
	}

	private void domainChange() {
		double oldDomain = domain;
	
		int value = domainScrollBar.getValue();
		
		domain = domains[value];
		
		if (domain != oldDomain) {
			fireDomainChanged(domain);
			
			log.debug("Domain slider changed to " + domain + ".");
		}
	}
	
	
	// Time Scale Listener Methods

	public void addTimeScaleListener(TimeScaleListener listener) {
		listener.timeScaleChanged(timeScale);
		timeScaleChangeListeners.add(listener);
	}
	
	public void removeTimeScaleListener(TimeScaleListener listener) {
		timeScaleChangeListeners.remove(listener);
	}
	
	private void fireTimeScaleChanged(double timeScale) {
		TimeScaleListener listener;
		for (int i=0; i<timeScaleChangeListeners.size(); i++) {
			listener = (TimeScaleListener)timeScaleChangeListeners.get(i);
			listener.timeScaleChanged(timeScale);
		}
	}	
	
	
	// Domain Listener Methods
	
	public void addDomainListener(DomainListener listener) {
		listener.domainChanged(domain);
		domainChangeListeners.add(listener);
	}
	
	public void removeDomainListener(DomainListener listener) {
		domainChangeListeners.remove(listener);
	}
	
	private void fireDomainChanged(double domain) {
		DomainListener listener;
		for (int i=0; i<domainChangeListeners.size(); i++) {
			listener = (DomainListener)domainChangeListeners.get(i);
			listener.domainChanged(domain);
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
			durationScrollBar.setEnabled(false);
		} else if (oldState == Player.STATE_MONITORING) {
			durationScrollBar.setEnabled(true);
		}
	}
	
	private void disbaleUI() {
		monitorButton.setEnabled(false);
		startButton.setEnabled(false);
		pauseButton.setEnabled(false);
		beginButton.setEnabled(false);
		endButton.setEnabled(false);

		locationScrollBar.setEnabled(false);
		durationScrollBar.setEnabled(false);
		domainScrollBar.setEnabled(false);
		
		log.info("Control Panel UI disbaled.");
	}
	
	private void enableUI() {
		monitorButton.setEnabled(true);
		startButton.setEnabled(true);
		pauseButton.setEnabled(true);
		beginButton.setEnabled(true);
		endButton.setEnabled(true);

		locationScrollBar.setEnabled(true);
		durationScrollBar.setEnabled(true);
		domainScrollBar.setEnabled(true);
		
		log.info("Control Panel UI enabled.");
	}

	
	// Player Subscription Methods
	
	public void channelSubscribed(String channelName) {
		updateTimeBoundaries();
	}

	public void channelUnsubscribed(String channelName) {
		updateTimeBoundaries();
	}

	public void channelChanged(String unsubscribedChannelName, String subscribedChannelName) {
		updateTimeBoundaries();
	}
}
