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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/datapanel/HelloWorldDataPanel.java $
 * $Revision: 316 $
 * $Date: 2005-10-19 20:11:00 -0700 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.datapanel;

import javax.swing.JLabel;

import org.nees.buffalo.rdv.DataViewer;

/**
 * A simple data panel to show a message and the current time of the
 * RBNBController.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class HelloWorldDataPanel extends AbstractDataPanel {

	JLabel label;
	
	/**
	 * Construct a Hello World data panel with the default message.
	 * 
	 * @since  1.2
	 */
	public HelloWorldDataPanel() {
		super();
		
		label = new JLabel("Hello World!");
		setDataComponent(label);
	}

	/**
	 * Set the message to the default
	 * 
	 * @since  1.2
	 */
	void clearData() {
		label.setText("Hello World!");
	}

	/**
	 * Return false indicating we don't support multiple channels.
	 * 
	 * @since  1.2
	 */
	public boolean supportsMultipleChannels() {
		return false;
	}
	
	/**
	 * Update the message to include the current time.
	 * 
	 * @since  1.2
	 */
	public void postTime(double time) {
		super.postTime(time);
		label.setText("Hello World! The time is " + DataViewer.formatDate(time));
	}
}