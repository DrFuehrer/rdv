package org.nees.buffalo.rdv;

import org.nees.buffalo.rdv.datapanel.DataPanel2;

/**
 * A listener interface to notify listeners when a data
 * panel has been added or removed from the data panel
 * container.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 * @see     DataPanelContainer
 */
public interface DataPanelContainerListener {
	/**
	 * Called when a data panel has been added to the data
	 * panel container. 
	 * 
	 * @param dataPanel  the data panel added to the container
	 * @since            1.0
	 */
	public void dataPanelAdded(DataPanel2 dataPanel);
	
	/**
	 * Called when a data panel has been removed from the data
	 * panel container.
	 * 
	 * @param dataPanel  the data panel removed from the container
	 * @since            1.0
	 */
	public void dataPanelRemoved(DataPanel2 dataPanel);
}