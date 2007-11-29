/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.ui;

import javax.swing.JApplet;

import org.rdv.DataViewer;
import org.rdv.rbnb.RBNBController;

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

      if (rbnbController.connect(true)) {
  			if (channels != null) {
  				for (int i=0; i<channels.length; i++) {
  					String channel = channels[i];
  					System.out.println("Viewing channel " + channel + ".");
  					org.rdv.rbnb.Channel channelTest = rbnbController.getChannel(channel);
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
