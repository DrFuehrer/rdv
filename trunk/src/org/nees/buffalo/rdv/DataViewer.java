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
 * $Name$
 * $Revision: 313 $
 * $Date: 2005-10-19 22:42:45 -0400 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv;

import java.awt.Toolkit;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.ApplicationFrame;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * @author Jason P. Hanley
 */
public class DataViewer {
	
	static Log log = LogFactory.getLog(DataViewer.class.getName());
	
	private RBNBController rbnb;
	private DataPanelManager dataPanelManager;
	private ApplicationFrame applicationFrame;

  private double location;
	private double playbackRate;
	private double timeScale;
	
	private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
  private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a");
  private static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("EEEE h:mm a");
  private static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("h:mm:ss a");
	
	public DataViewer() {
    location = System.currentTimeMillis()/1000d;;
		playbackRate = 1;
		timeScale = 1;
	}
    
  public void setLocation(double location) {
    this.location = location; 
  }
	
	public void setPlaybackRate(double playbackRate) {
		this.playbackRate = playbackRate;
	}
		
	public void setTimeScale(double timeScale) {
		this.timeScale = timeScale;
	}
	
	public void initialize() {
		rbnb = new RBNBController(location, playbackRate, timeScale);
		dataPanelManager = new DataPanelManager(rbnb);
		applicationFrame = new ApplicationFrame(this, rbnb, dataPanelManager);		
	}

	public void exit() {
		applicationFrame.dispose();
		if (rbnb != null) {
			rbnb.exit();
		}
		log.info("Exiting.");		
	}
 	
 	public RBNBController getRBNBController() {
 		return rbnb;
 	}
 	
 	public DataPanelManager getDataPanelManager() {
 		return dataPanelManager;
 	}
 	
 	public ApplicationFrame getApplicationFrame() {
 		return applicationFrame;
 	}

	public static String formatDate(double date) {
		return ISO_DATE_FORMAT.format(new Date(((long)(date*1000))));
	}
	
	public static String formatDate(long date) {
		return ISO_DATE_FORMAT.format(new Date(date));
	}
  
  public static String formatDateSmart(double dateDouble) {
    long dateLong = (long)(dateDouble*1000);
    double difference = (System.currentTimeMillis()/1000d) - dateDouble;
    if (difference < 60*60*24) {
      return TIME_DATE_FORMAT.format(new Date(dateLong));
    } else if (difference < 60*60*24*7) {
      return DAY_DATE_FORMAT.format(new Date(dateLong));
    } else {
      return FULL_DATE_FORMAT.format(new Date(dateLong));
    }
  }

	public static String formatSeconds(double seconds) {
		String secondsString;
		if (seconds < 1e-6) {
			secondsString = Double.toString(round(seconds*1000000000)) + " ns";
 		} else if (seconds < 1e-3) {
			secondsString = Double.toString(round(seconds*1000000)) + " us";
 		} else if (seconds < 1 && seconds != 0) {
			secondsString = Double.toString(round(seconds*1000)) + " ms";
 		} else if (seconds < 60) {
 		 	secondsString = Double.toString(round(seconds)) + " seconds";
 		} else if (seconds < 60*60) {
			secondsString = Double.toString(round(seconds/60)) + " minutes";
 		} else if (seconds < 60*60*24){
 			secondsString = Double.toString(round(seconds/(60*60))) + " hours";
 		} else {
 			secondsString = Double.toString(round(seconds/(60*60*24))) + " days";
 		}
 		return secondsString;
 	}
 	
 	public static String formatBytes(int bytes) {
 		String bytesString;
 		if (bytes < 1024) {
 			bytesString = Integer.toString(bytes) + " bytes";
 		} else if (bytes < 1024*1024) {
 			bytesString = Double.toString(round(bytes/1024d)) + " KB";
 		} else if (bytes < 1024*1024*1024) {
 			bytesString = Double.toString(round(bytes/1024d)) + " MB";
 		} else {
 			bytesString = Double.toString(round(bytes/1024d)) + " GB";
 		}
 		return bytesString;
 	}
 	
 	public static float round(float f) {
 		return (long)(f*10)/10f;
 	}
 	 	
 	public static double round(double d) {
 		return (long)(d*10)/10d;
 	}
  
  public static Icon getIcon(String iconFileName) {
    ImageIcon icon = null;
    if (iconFileName != null) {
      URL iconURL = ClassLoader.getSystemResource(iconFileName);    
      if (iconURL != null) {
        icon = new ImageIcon(iconURL);
      }
    }
    return icon;
  }  

	public static void main(String[] args) {
		//enable dynamic layout during application resize
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (!toolkit.isDynamicLayoutActive()) {
			toolkit.setDynamicLayout(true);
		}
    
    //disable default drop target for swing components
    System.setProperty("suppressSwingDropSupport", "true");
        
    //set L&F
    UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
    try {
      UIManager.setLookAndFeel(new PlasticLookAndFeel());
    } catch (Exception e) {}
		
		Options options = new Options();
		Option hostNameOption = OptionBuilder.withArgName("host name")
											 .hasArg()
											 .withDescription("The host name of the RBNB server")
											 .withLongOpt("host")
											 .create('h');
		Option portNumberOption = OptionBuilder.withArgName("port number")
											   .hasArg()
											   .withDescription("The port number of the RBNB server")
											   .withLongOpt("port")
											   .create('p');
		Option channelsOption = OptionBuilder.withArgName("channels")
											 .hasArgs()
											 .withDescription("Channels to subscribe to")
											 .withLongOpt("channels")
											 .create('c');
		Option playbackRateOption = OptionBuilder.withArgName("playback rate")
											 .hasArg()
											 .withDescription("The playback rate")
											 .withLongOpt("playback-rate")
											 .create('r');
		Option timeScaleOption = OptionBuilder.withArgName("time scale")
											  .hasArg()
											  .withDescription("The time scale in seconds")
											  .withLongOpt("time-scale")
											  .create('s');
		Option playOption = OptionBuilder.withDescription("Start playing back data") 
										 .withLongOpt("play")
										 .create();
		Option realTimeOption = OptionBuilder.withDescription("Start viewing data in real time") 
		 									 .withLongOpt("real-time")
											 .create();
		Option helpOption = new Option("?", "help", false, "Display usage");
		options.addOption(hostNameOption);
		options.addOption(portNumberOption);
		options.addOption(channelsOption);
		options.addOption(playbackRateOption);
		options.addOption(timeScaleOption);
		options.addOption(playOption);
		options.addOption(realTimeOption);			
		options.addOption(helpOption);
				
		String hostName = null;
		int portNumber = -1;
		String[] channels = null;
		double playbackRate = -1;
		double timeScale = -1;
		boolean play = false;
		boolean realTime = false;
		
		CommandLineParser parser = new PosixParser();
		
		try {
	    	CommandLine line = parser.parse(options, args);
	    	
	    	if (line.hasOption('?')) {
	    		HelpFormatter formatter = new HelpFormatter();
	    		formatter.printHelp("DataViewer", options, true);
	    		return;
	    	}
	    	
	    	if (line.hasOption('h')) {
	    		hostName = line.getOptionValue('h');
	    		log.info("Set host name to " + hostName + ".");
	    	}
	    	
	    	if (line.hasOption('p')) {
	    		String value = line.getOptionValue('p');
	    		portNumber = Integer.parseInt(value);
	    		log.info("Set port number to " + portNumber + ".");
	    	}
	    	
	    	if (line.hasOption('c')) {
	    		channels = line.getOptionValues('c');
	    	}
	    	
	    	if (line.hasOption('r')) {
	    		String value = line.getOptionValue('r');
	    		playbackRate = Double.parseDouble(value);
	    	}
	    	
	    	if (line.hasOption('s')) {
	    		String value = line.getOptionValue('s');
	    		timeScale = Double.parseDouble(value);
	    	}
	    	
	    	if (line.hasOption("play")) {
	    		play = true;
	    	} else if (line.hasOption("real-time")) {
	    		realTime = true;
	    	}
		} catch( ParseException e) {
			log.error("Command line arguments invalid: " + e.getMessage());
			return;
	    }
		
		log.info("Starting data viewer in disconnected state.");
		DataViewer dataViewer = new DataViewer();
		
		if (playbackRate != -1) {
			dataViewer.setPlaybackRate(playbackRate);
		}
		
		if (timeScale != -1) {
			dataViewer.setTimeScale(timeScale);
		}
		
		dataViewer.initialize();
		
		RBNBController rbnbController = dataViewer.getRBNBController();
				
		if (portNumber != -1) {
			rbnbController.setRBNBPortNumber(portNumber);
		}
		
		if (hostName != null) {
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
						log.info("Viewing channel " + channel + ".");
						dataViewer.getDataPanelManager().viewChannel(channel);
					}
				}
				
				if (play) {
					log.info("Starting data playback.");
					rbnbController.play();
				} else if (realTime) {
					log.info("Viewing data in real time.");
					rbnbController.monitor();
				}
			}
		}
	}
}
