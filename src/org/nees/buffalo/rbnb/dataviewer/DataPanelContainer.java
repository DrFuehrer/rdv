package org.nees.buffalo.rbnb.dataviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jason P. Hanley
 */
public class DataPanelContainer extends JPanel implements DomainListener{

	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	ArrayList dataPanels;
	ArrayList listeners;
	
	double domain;
	
	public DataPanelContainer() {
		dataPanels = new ArrayList();
		listeners = new ArrayList();
		
		setBorder(new EtchedBorder());
		setLayout(new GridBagLayout());
	}
	
	public void addDataPanel(DataPanel2 dataPanel) {
		dataPanel.setDomain(domain);
		dataPanels.add(dataPanel);
		layoutDataPanels();
		
		fireDataPanelAdded(dataPanel);
	}

	public void removeDataPanel(DataPanel2 dataPanel) {
		remove(dataPanel.getComponent());
		dataPanels.remove(dataPanel);
		layoutDataPanels();
		
		fireDataPanelRemoved(dataPanel);
	}
	
	private void layoutDataPanels() {
		GridBagConstraints c = new GridBagConstraints();
		DataPanel2 dataPanel;
		int channelIndex = 0;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel2)dataPanels.get(i);
			remove(dataPanel.getComponent());
			
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = (channelIndex % 2) + 1;
			c.gridy = (channelIndex / 2) + 2;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.ipadx = 0;
			c.ipady = 0;
			c.insets = new java.awt.Insets(5,5,5,5);
			c.anchor = GridBagConstraints.NORTH;
	
			add(dataPanel.getComponent(), c);
			
			channelIndex++;			
		}
		
		validate();
		repaint();
	}
	
	public void domainChanged(double domain) {
		this.domain = domain;
		/* DataPanel2 dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel2)dataPanels.get(i);
			dataPanel.setDomain(domain);
		} */
	}
	
	public void addDataPanelContainerListener(DataPanelContainerListener listener) {
		listeners.add(listener);
	}

	public void removeDataPanelContainerListener(DataPanelContainerListener listener) {
		listeners.remove(listener);
	}

	private void fireDataPanelAdded(DataPanel2 dataPanel) {
		DataPanelContainerListener listener;
		for (int i=0; i<listeners.size(); i++) {
			listener = (DataPanelContainerListener)listeners.get(i);
			listener.dataPanelAdded(dataPanel);
		}
	}
	
	private void fireDataPanelRemoved(DataPanel2 dataPanel) {
		DataPanelContainerListener listener;
		for (int i=0; i<listeners.size(); i++) {
			listener = (DataPanelContainerListener)listeners.get(i);
			listener.dataPanelRemoved(dataPanel);
		}		
	}
	
}
