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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.ui;

import javax.swing.JApplet;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * @author jphanley, bkirschner
 */
public class ApplicationApplet extends JApplet {
	public static String CHANNEL_SPLIT_CHAR = "&";
	protected DataViewer dataViewer;
	
	public void init() {
		System.setProperty("sun.java2d.ddscale","true");	

		dataViewer = new DataViewer(true);

		RBNBController rbnbController = dataViewer.getRBNBController();
    ControlPanel controlPanel = dataViewer.getApplicationFrame().getControlPanel();

		String hostName = getParameter("host");
		String portString = getParameter("port");
		String channelsString = getParameter("channels");
		String playbackRateString = getParameter("playback-rate");
		String timeScaleString = getParameter("time-scale");
		boolean play = Boolean.parseBoolean(getParameter("play"));
		boolean realTime = Boolean.parseBoolean(getParameter("real-time"));

		if (playbackRateString != null && !playbackRateString.equals("")) {
      double playbackRate = Double.parseDouble(playbackRateString);
      controlPanel.setPlaybackRate(playbackRate);
      rbnbController.setPlaybackRate(playbackRate);
		}

		if (timeScaleString != null && !timeScaleString.equals("")) {
      double timeScale = Double.parseDouble(timeScaleString);
			controlPanel.setTimeScale(timeScale);
      rbnbController.setTimeScale(timeScale);
    }

		if (portString != null && !portString.equals("")) {
			rbnbController.setRBNBPortNumber(Integer.parseInt(portString));
    }

		String[] channels = null;
		if (channelsString != null && !channelsString.equals("")) {
			channels = channelsString.split(CHANNEL_SPLIT_CHAR);
    }

		if (hostName != null && !hostName.equals("")) {
			rbnbController.setRBNBHostName(hostName);
      int state = rbnbController.setState(RBNBController.STATE_STOPPED);

      if (state == RBNBController.STATE_STOPPED) {
  			if (channels != null) {
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
