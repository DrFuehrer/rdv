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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/ui/ApplicationApplet.java $
 * $Revision: 322 $
 * $Date: 2005-11-16 13:24:39 -0800 (Wed, 16 Nov 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.ui;

import javax.swing.JApplet;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * @author jphanley, bkirschner
 */
public class ApplicationApplet extends JApplet {
	public static String CHANNEL_SPLIT_CHAR = "&";
	protected DataViewer dataViewer;
	
	public void init() {
		System.setProperty( "sun.java2d.ddscale","true" );	

		dataViewer = new DataViewer();
		dataViewer.initialize( true );

		RBNBController rbnbController = dataViewer.getRBNBController();

		String hostName = getParameter("host");
		String portStr = getParameter("port");
		String channelStr = getParameter("channels");
		String playbackRate = getParameter("playback-rate");
		String timeScale = getParameter("time-scale");
		boolean play = Boolean.valueOf( getParameter("play") ).booleanValue();
		boolean realTime = Boolean.valueOf( getParameter("real-time") ).booleanValue();

		if (playbackRate != null && !playbackRate.equals("") )
			dataViewer.setPlaybackRate( Double.valueOf(playbackRate).doubleValue() );

		if (timeScale != null && !timeScale.equals("") )
			dataViewer.setTimeScale( Double.valueOf(timeScale).doubleValue() );

		if ( portStr != null && !portStr.equals("") )
			rbnbController.setRBNBPortNumber( Integer.valueOf(portStr).intValue() );

		String[] channels = null;
		if ( channelStr != null && !channelStr.equals("") )
			channels = channelStr.split(CHANNEL_SPLIT_CHAR);

		if ( hostName != null && !hostName.equals("") ) {
			rbnbController.setRBNBHostName(hostName);
			rbnbController.connect();

			//FIXME this needs to be done better
			int tries = 0;
			int maxTries = 20;
			while (rbnbController.getState() == Player.STATE_DISCONNECTED && tries++ < maxTries) {
				try { Thread.sleep(1000); } catch (Exception e) {}
			}

			if (tries < maxTries) {	
				if (channels != null && hostName != null) {
					for (int i=0; i<channels.length; i++) {
						String channel = channels[i];
						System.out.println("Viewing channel " + channel + ".");
						org.nees.buffalo.rdv.rbnb.Channel channelTest = rbnbController.getChannel(channel);
						if ( channelTest == null )
							System.out.println("No such channel: " + channel );
						dataViewer.getDataPanelManager().viewChannel(channel);
					}
				}

				if (play) {
					System.out.println("Starting data playback.");
					rbnbController.play();
				} else if (realTime) {
					System.out.println("Viewing data in real time.");
					rbnbController.monitor();
				}
			}
		}

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
