package org.nees.buffalo.rdv.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.DomainListener;
import org.nees.buffalo.rdv.rbnb.PlayerStateListener;
import org.nees.buffalo.rdv.rbnb.PlayerTimeListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;

/**
 * @author Jason P. Hanley
 */
public class StatusPanel extends JPanel implements PlayerTimeListener, PlaybackRateListener, PlayerStateListener, DomainListener {

	private DataViewer dataViewer;

	private JLabel locationLabel;
	private JLabel playbackRateLabel;
	private JLabel stateLabel;
	private JLabel domainLabel;
	
	public StatusPanel(DataViewer dataViewer) {
		super();
		
		this.dataViewer = dataViewer;
		
		initPanel();
	}
	
	private void initPanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		setBorder(new EtchedBorder());
		
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
		add(locationLabel, c);	
	
		domainLabel = new JLabel();
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
		add(domainLabel, c);
				
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
		add(stateLabel, c);
		
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
		add(playbackRateLabel, c);
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
		stateLabel.setText("state: " + stateString);
	}

	public void domainChanged(double domain) {
		domainLabel.setText("Time Scale: " + DataViewer.formatSeconds(domain));
	}
}
