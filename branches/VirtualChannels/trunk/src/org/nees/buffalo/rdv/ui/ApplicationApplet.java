/*
 * Created on Nov 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.nees.buffalo.rdv.ui;

import javax.swing.JApplet;

import org.nees.buffalo.rdv.DataViewer;

/**
 * @author jphanley
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ApplicationApplet extends JApplet {
		
	DataViewer dataViewer;
	
	public void init() {
		dataViewer = new DataViewer();
		this.setContentPane(dataViewer.getApplicationFrame().getContentPane());
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}

	public void destroy() {
		dataViewer.exit();
	}
}
