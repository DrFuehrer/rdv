/*
 * Created on Nov 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.nees.buffalo.rbnb.dataviewer;

import javax.swing.JApplet;

/**
 * @author jphanley
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataViewerApplet extends JApplet {
		
	DataViewer dataViewer;
	
	public void init() {
		dataViewer = new DataViewer("rbnb.nees.buffalo.edu", true);
		this.setContentPane(dataViewer.getContentPane());
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}

	public void destroy() {
		dataViewer.exit();
	}
}
