package org.nees.buffalo.rbnb.dataviewer;

import java.awt.Adjustable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EtchedBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

/**
 * @author Jason P. Hanley
 */
public class ControlPanel extends JPanel implements AdjustmentListener, PlayerTimeListener, PlayerStateListener, ChannelListListener {

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
	
	ArrayList timeScaleChangeListeners;
	ArrayList domainChangeListeners;

	public ControlPanel(DataViewer dataViewer, Player player) {
		super();
		
		this.dataViewer = dataViewer;
		this.player = player;
		
		startTime = endTime = location = System.currentTimeMillis()/1000d;
		timeScale = 1;
		domain = 1;
		
		initPanel();
		
		timeScaleChangeListeners = new ArrayList();
		domainChangeListeners = new ArrayList();
		
		setSliderBounds(startTime, endTime);
		setSliderLocation(location);
		
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
 		beginButton.setToolTipText("Go to begining");
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
				
		JLabel locationLabel = new JLabel("location");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(10,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
		add(locationLabel, c);
		
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
		c.insets = new java.awt.Insets(10,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;				
		add(locationScrollBar, c);

		JLabel timeScaleLabel = new JLabel("time scale");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(10,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
		add(timeScaleLabel, c);		
		
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
		add(durationScrollBar, c);

		JLabel domainLabel = new JLabel("domain");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(10,5,5,5);
		c.anchor = GridBagConstraints.NORTHWEST;
		add(domainLabel, c);			
		
		domainScrollBar = new JScrollBar(Adjustable.HORIZONTAL, defaultDomainIndex, 1, 0, domains.length);
 		domainScrollBar.addAdjustmentListener(this);
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
		add(domainScrollBar, c);		
	}
	
	public void channelListUpdated(ChannelMap channelMap) {
		ChannelTree ctree = ChannelTree.createFromChannelMap(channelMap);
		
		double startTime = -1;
		double endTime = -1;
		
		Iterator i = ctree.iterator();
		while (i.hasNext()) {
			ChannelTree.Node node = (ChannelTree.Node)i.next();
			if (node.getType() == ChannelTree.CHANNEL && player.isSubscribed(node.getFullName())) {
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
			startTime = System.currentTimeMillis()/1000d;
		}
		
		if (endTime == -1) {
			endTime = startTime;
		}
		
		log.debug("Setting time to start at " + DataViewer.formatDate(startTime) + " and end at " + DataViewer.formatDate(endTime) + " seconds.");
		setSliderBounds(startTime, endTime);
	}	
		
	private void setSliderBounds(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		
		refreshSliderBounds();
		
		if (startTime == endTime) {
			setSliderLocation(startTime);
		} else {
			double location = player.getLocation();
			if (location < startTime || location > endTime) {
				if (playerState != Player.STATE_MONITORING) {
					setLocationBegin();
				}
			} else {
				setSliderLocation(location);
			}
		}
	}
	
	private void refreshSliderBounds() {
		int intStartTime = (int)(startTime*1000);
		int intEndTime = (int)(endTime*1000);
		int intDurationTime = (int)((endTime-startTime)*1000);
		
		locationScrollBar.removeAdjustmentListener(this);
		locationScrollBar.setMinimum(0);
		locationScrollBar.setMaximum(intDurationTime);
		locationScrollBar.addAdjustmentListener(this);
		
		//log.debug("Set bounds of location bar.");
	}
	
	public void postTime(double time) {
		setSliderLocation(time);
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
		player.setLocation(startTime);
	}
	
	public void setLocationEnd() {
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
			location = (((double)sliderLocation)/1000)+startTime;
			log.debug("Location bar moved to " + DataViewer.formatDate(location));
			player.setLocation(location);
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
			
			locationScrollBar.removeAdjustmentListener(this);
			locationScrollBar.setVisibleAmount((int)(timeScale*1000));
			locationScrollBar.setUnitIncrement((int)(timeScale*1000));			
			locationScrollBar.setBlockIncrement((int)(timeScale*10000));
			locationScrollBar.addAdjustmentListener(this);
		}
	}

	private void domainChange() {
		double oldDomain = domain;
	
		int value = domainScrollBar.getValue();
		
		/* if (value >= 0) {
			int quotient = value / 3;
			int mod = value % 3;
			
			if (mod == 0) {
				domain = 1*Math.pow(10, quotient);
			} else if (mod == 1) {
				domain = 2*Math.pow(10, quotient);
			} else if (mod == 2) {
				domain = 5*Math.pow(10, quotient);
			}				
		} else {
			value = value - 2;
			int quotient = value / 3;
			int mod = value % 3;			
			
			if (mod == 0) {
				domain = 5*Math.pow(10, quotient);
			} else if (mod == -1) {
				domain = 2*Math.pow(10, quotient);
			} else if (mod == -2) {
				domain = 1*Math.pow(10, quotient);
			}				
		} */

		domain = domains[value];
		
		if (domain != oldDomain) {
			fireDomainChanged(domain);
		}
	}

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
	
	public void addDomainListener(DomainListener listener) {
		listener.domainChanged(domain);
		domainChangeListeners.add(listener);
	}
	
	public void removeChangeListener(DomainListener listener) {
		domainChangeListeners.remove(listener);
	}
	
	private void fireDomainChanged(double domain) {
		DomainListener listener;
		for (int i=0; i<domainChangeListeners.size(); i++) {
			listener = (DomainListener)domainChangeListeners.get(i);
			listener.domainChanged(domain);
		}
	}

	public void postState(int newState, int oldState) {
		playerState = newState;
		
		if (newState == Player.STATE_DISCONNECTED) {
			disbaleUI();
		} else {
			enableUI();
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
	}
}
