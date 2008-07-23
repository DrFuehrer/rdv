/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 * Copyright (c) 2008 Palta Software
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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv.datapanel;

import java.util.Iterator;

import javax.swing.JComponent;

/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

	/**
	 * The UI component to display the data in
	 */
	JComponent dataComponent;
	
	/**
	 * Initialize the object and UI
	 */
	public TemplateDataPanel() {
		super();

		initDataComponent();
	}
	
	/**
	 * Initialize the UI component and pass it too the abstract class.
	 */
	private void initDataComponent() {
		// TODO create data component
		
		setDataComponent(dataComponent);
	}

	public boolean supportsMultipleChannels() {
		// TODO change if this data panel supports multiple channels
		return false;
	}

	public void postTime(double time) {
		super.postTime(time);
		
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				// TODO display the data in your data component
			}
		}
	}
}
