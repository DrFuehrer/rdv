package org.nees.buffalo.rbnb.dataviewer;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jason P. Hanley
 */
public class DataPanelContainer extends JPanel {

	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	ArrayList dataPanels;
	ArrayList listeners;
		
	public static int HORIZONTAL_LAYOUT = 0;
	public static int VERTICAL_LAYOUT = 1;
	
	int layout;
	
	public DataPanelContainer() {
		dataPanels = new ArrayList();
		listeners = new ArrayList();
		
		layout = HORIZONTAL_LAYOUT;
	}
	
	public void addDataPanel(DataPanel2 dataPanel) {
		dataPanels.add(dataPanel);
		layoutDataPanels();
		
		log.info("Added data panel to container (total=" + dataPanels.size() + ").");
		
		fireDataPanelAdded(dataPanel);
	}

	public void removeDataPanel(DataPanel2 dataPanel) {
		remove(dataPanel.getComponent());
		dataPanels.remove(dataPanel);
		layoutDataPanels();
		
		log.info("Removed data panel container (total=" + dataPanels.size() + ").");
		
		fireDataPanelRemoved(dataPanel);
	}
	
	public void setLayout(int layout) {
		this.layout = layout;
		layoutDataPanels();
	}
	
	private void layoutDataPanels() {
		int numberOfDataPanels = dataPanels.size();
		if (numberOfDataPanels > 0) {
			int gridDimension = (int)Math.ceil(Math.sqrt(numberOfDataPanels));
			int rows = gridDimension;
			int columns = gridDimension;
			
			if (layout == HORIZONTAL_LAYOUT && numberOfDataPanels == 2) {
				rows = 1;
			}
			
			setLayout(new GridLayout(rows, columns));
			
			DataPanel2 dataPanel;
			int channelIndex = 0;
			for (int i=0; i<numberOfDataPanels; i++) {
				dataPanel = (DataPanel2)dataPanels.get(i);
				remove(dataPanel.getComponent());
				add(dataPanel.getComponent());			
				channelIndex++;			
			}
		}
		
		validate();
		repaint();
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
