/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/ui/DataPanelContainer.java $
 * $Revision: 316 $
 * $Date: 2005-10-19 20:11:00 -0700 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.ui;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A container to hold the UI components for the data panels. They may add and
 * remove UI components as needed.
 * 
 * @author  Jason P. Hanley
 * @since   1.1
 */
public class DataPanelContainer extends JPanel {

	/**
	 * The logger for this class.
	 * 
	 * @since  1.1
	 */
	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	/**
	 * A list of docked UI components.
	 * 
	 * @since  1.1
	 */
	ArrayList dataPanels;
	
	/**
   *
	 * @since  1.1
	 */
  public static int HORIZONTAL_LAYOUT = 0;
	
	/**
	 * Display the data panels vertically.
	 * 
	 * @since  1.1
	 */
	public static int VERTICAL_LAYOUT = 1;
	
	/**
	 * The current layout.
	 * 
	 * @since  1.1
	 */
	private int layout;
	
	/** 
	 * Create the container and set the default layout to horizontal.
	 * 
	 * @since  1.1
	 */
	public DataPanelContainer() {
    super();
    
    setBorder(null);
    
		dataPanels = new ArrayList();
		
		layout = HORIZONTAL_LAYOUT;
	}
	
	/**
	 * Add a data panel UI component to this container.
	 * 
	 * @param component  the UI component to add
	 * @since            1.1
	 */
	public void addDataPanel(JComponent component) {
		dataPanels.add(component);
		layoutDataPanels();
		
		log.info("Added data panel to container (total=" + dataPanels.size() + ").");
	}

	/**
	 * Remove the data panel UI component from this container.
	 * 
	 * @param component  the UI component to remove.
	 * @since            1.1
	 */
	public void removeDataPanel(JComponent component) {
		remove(component);
		dataPanels.remove(component);
		layoutDataPanels();
		
		log.info("Removed data panel container (total=" + dataPanels.size() + ").");
	}
	
	/**
	 * Set the layout for the data panels.
	 * 
	 * @param layout  the layout to use
	 * @since         1.1
	 */
	public void setLayout(int layout) {
		this.layout = layout;
		layoutDataPanels();
	}
	
	/**
	 * Layout the data panel acording the layout setting and in the order in which
	 * they were added to the container.
	 * 
	 * @since  1.1
	 */
	private void layoutDataPanels() {
		int numberOfDataPanels = dataPanels.size();
		if (numberOfDataPanels > 0) {
			int gridDimension = (int)Math.ceil(Math.sqrt(numberOfDataPanels));
			int rows = gridDimension;
			int columns = gridDimension;
			
			if (layout == HORIZONTAL_LAYOUT && numberOfDataPanels == 2) {
				rows = 1;
			}
			
			setLayout(new GridLayout(rows, columns, 8, 8));
			
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
