package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.TimeScaleListener;
import org.nees.buffalo.rdv.rbnb.StateListener;
import org.nees.buffalo.rdv.rbnb.TimeListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * @author Jason P. Hanley
 */
public class StatusPanel extends JPanel implements TimeListener, PlaybackRateListener, StateListener, TimeScaleListener {

	private JLabel locationLabel;
	private JLabel playbackRateLabel;
	private JLabel stateLabel;
	private JLabel timeScaleLabel;
	
	public StatusPanel() {
		super();
		
		initPanel();
	}
	
	private void initPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    
    JPanel p = new JPanel();
    p.setBorder(null);
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		locationLabel = new JLabel();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.WEST;		
		p.add(locationLabel, c);	
	
		timeScaleLabel = new JLabel();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.EAST;		
		p.add(timeScaleLabel, c);
				
		stateLabel = new JLabel();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.WEST;		
		p.add(stateLabel, c);
		
		playbackRateLabel = new JLabel();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.anchor = GridBagConstraints.EAST;		
		p.add(playbackRateLabel, c);
        
    SimpleInternalFrame sif = new SimpleInternalFrame(
        DataViewer.getIcon("icons/info.gif"),
        "Status Panel",
        null,
        p);

    add(sif, BorderLayout.CENTER);
	}

	public void postTime(double time) {
		String locationString = DataViewer.formatDate(time);
		locationLabel.setText("Time: " + locationString);		
	}

	public void playbackRateChanged(double playbackRate) {
		playbackRateLabel.setText("Playback Rate: " + Double.toString(playbackRate));		
	}

	public void postState(int newState, int oldState) {
		String stateString = RBNBController.getStateName(newState);
		stateLabel.setText("State: " + stateString);
	}

	public void timeScaleChanged(double timeScale) {
		timeScaleLabel.setText("Time Scale: " + DataViewer.formatSeconds(timeScale));
	}
}
