/*
 * Created on Mar 31, 2005
 */
package org.nees.buffalo.rdv;

import java.util.ArrayList;
import java.util.Iterator;

import org.nees.buffalo.rdv.datapanel.DataPanel;

/**
 * A class to manage all the data panels.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class DataPanelManager {
	
	public DataPanelManager() {
		dataPanels = new ArrayList();
	}
	
	/**
	 * A list of all the data panels.
	 * 
	 * @since  1.2
	 */
	private ArrayList dataPanels;
	
	/**
	 * Register the data panel with the manager.
	 * 
	 * @param dataPanel  the data panel to be registered
	 * @since            1.2
	 */
	public void addDataPanel(DataPanel dataPanel) {
		dataPanels.add(dataPanel);
	}
	
	/**
	 * Unregister the data panel from the manager.
	 * 
	 * @param dataPanel  the data panel to be unregistered
	 * @since            1.2
	 */
	public void removeDataPanel(DataPanel dataPanel) {
		dataPanels.remove(dataPanel);
	}
	
	/**
	 * Invoke the <code>closePanel</code> method on each registered data panel.
	 * 
	 * @see    DataPanel#closePanel()
	 * @since  1.2
	 */
	public void closeAllDataPanels() {
		DataPanel dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel)dataPanels.get(i);
			dataPanel.closePanel();
		}
		dataPanels.clear();
	}
}