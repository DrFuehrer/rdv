package org.nees.buffalo.rdv.ui;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jason P. Hanley
 */
public class DataPanelContainer extends JPanel {

	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	ArrayList dataPanels;
		
	public static int HORIZONTAL_LAYOUT = 0;
	public static int VERTICAL_LAYOUT = 1;
	
	int layout;
	
	public DataPanelContainer() {
		dataPanels = new ArrayList();
		
		layout = HORIZONTAL_LAYOUT;
	}
	
	public void addDataPanel(JComponent component) {
		dataPanels.add(component);
		layoutDataPanels();
		
		log.info("Added data panel to container (total=" + dataPanels.size() + ").");
	}

	public void removeDataPanel(JComponent component) {
		remove(component);
		dataPanels.remove(component);
		layoutDataPanels();
		
		log.info("Removed data panel container (total=" + dataPanels.size() + ").");
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
			
			JComponent component;
			int channelIndex = 0;
			for (int i=0; i<numberOfDataPanels; i++) {
				component = (JComponent)dataPanels.get(i);
				remove(component);
				add(component);			
				channelIndex++;			
			}
		}
		
		validate();
		repaint();
	}
}
