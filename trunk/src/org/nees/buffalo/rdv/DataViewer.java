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

package org.nees.buffalo.rdv;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
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
import org.nees.buffalo.rdv.action.ActionFactory;
import org.nees.buffalo.rdv.rbnb.LocalServer;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.ApplicationFrame;
import org.nees.buffalo.rdv.ui.ControlPanel;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * @author Jason P. Hanley
 */
public class DataViewer {
	
	static Log log = LogFactory.getLog(DataViewer.class.getName());
	
	private final RBNBController rbnb;
	private final DataPanelManager dataPanelManager;
	private final ApplicationFrame applicationFrame;
  private final ConfigurationManager configurationManager;

	private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
  private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm.ss.SSS a");
  private static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("EEEE h:mm.ss.SSS a");
  private static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("h:mm:ss.SSS a");
  
  /** global cache for icons */
  private static final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<String, ImageIcon>();;
	
	public DataViewer(boolean isApplet) {
		rbnb = RBNBController.getInstance();
		dataPanelManager = new DataPanelManager(rbnb);
    configurationManager = new ConfigurationManager(this);
		applicationFrame = new ApplicationFrame(this, rbnb, dataPanelManager, isApplet);
	}

	public void exit() {
		applicationFrame.dispose();
    
		if (rbnb != null) {
			rbnb.exit();
		}
    
    try {
      LocalServer.getInstance().stopServer();
    } catch (Exception e) {}
    
		log.info("Exiting.");

		System.exit(0);
	}
 	
 	public RBNBController getRBNBController() {
 		return rbnb;
 	}

 	public DataPanelManager getDataPanelManager() {
 		return dataPanelManager;
 	}
  
  public ConfigurationManager getConfigurationManager() {
    return configurationManager;
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
 		 	secondsString = Double.toString(round(seconds)) + " s";
 		} else if (seconds < 60*60) {
			secondsString = Double.toString(round(seconds/60)) + " m";
 		} else if (seconds < 60*60*24){
 			secondsString = Double.toString(round(seconds/(60*60))) + " h";
 		} else if (seconds < 60*60*24*7){
 			secondsString = Double.toString(round(seconds/(60*60*24))) + " d";
    } else {
      secondsString = Double.toString(round(seconds/(60*60*24*7))) + " w";
 		}
    
 		return secondsString;
 	}

  /**
   * Returns a double representing the amount of seconds represented by the
   * specified string.
   * 
   * The string can be a number, optionally followed by a unit
   * of time. Valid units are 'ns' (nanosecond), 'us' (microsecond), 'ms'
   * (millisecond), 's' (second), 'm' (minute), 'h' (hour), 'd' (day), and 'w'
   * (week). If no unit is specified, it is assume to be seconds. There may be
   * whitespace around the number and the unit. 
   * 
   * @param t                          the time formatted string
   * @return                           the time in seconds represented by the
   *                                   string
   * @throws IllegalArgumentException  if the string is formatted incorrectly
   */
  public static double parseTime(String t) throws IllegalArgumentException {
    double time;
    
    t = t.trim().toLowerCase();

    if (t.length() == 0) {
      throw new IllegalArgumentException("Empty input string.");
    } else if (t.endsWith("ns")) {
      time = Double.parseDouble(t.substring(0, t.length()-2).trim()) / 1000;
    } else if (t.endsWith("us")) {
      time = Double.parseDouble(t.substring(0, t.length()-2).trim()) / 1000000;
    } else if (t.endsWith("ms")) {
      time = Double.parseDouble(t.substring(0, t.length()-2).trim()) / 1000000000;      
    } else if (t.endsWith("s")) {
      time = Double.parseDouble(t.substring(0, t.length()-1).trim());
    } else if (t.endsWith("m")) {
      time = Double.parseDouble(t.substring(0, t.length()-1).trim()) * 60;
    } else if (t.endsWith("h")) {
      time = Double.parseDouble(t.substring(0, t.length()-1).trim()) * 60 * 60;
    } else if (t.endsWith("d")) {
      time = Double.parseDouble(t.substring(0, t.length()-1).trim()) * 60 * 60 * 24;
    } else if (t.endsWith("w")) {
      time = Double.parseDouble(t.substring(0, t.length()-1).trim()) * 60 * 60 * 24 * 7;
    } else {
      time = Double.parseDouble(t);
    }
    
    return time;
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
  
  public static InputStream getResourceAsStream(String name) {
    InputStream resource = null;
    if (name != null) {
      resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }
    return resource;
  }
  
  public static Image getImage(String imageFileName) {
    ImageIcon icon = getIcon(imageFileName);
    if (icon != null) {
      return icon.getImage();
    } else {
      return null;
    }
  }
  
  /**
   * Loads the given file as an icon and returns it. Previously loaded icon's
   * are cached, so subsequent calls to this method with the same icon file name
   * will return the same icon.
   * 
   * @param iconFileName  the name of the icon file
   * @return              the icon, or null if the icon doesn't exist
   */
  public static ImageIcon getIcon(String iconFileName) {
    if (iconFileName == null) {
      return null;
    }
    
    // see if the icon is in the cache
    ImageIcon icon = iconCache.get(iconFileName);
    if (icon != null) {
      return icon;
    }
    
    
    URL iconURL = Thread.currentThread().getContextClassLoader().getResource(iconFileName);    
    if (iconURL != null) {
      icon = new ImageIcon(iconURL);
      
      // cache the icon for future requests
      iconCache.put(iconFileName, icon);
    }
    
    return icon;
  }  

  public void alertError(String errorMessage) {
    JOptionPane.showMessageDialog(applicationFrame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Open the URL in an external browser. 
   * 
   * @param url         the url to open
   * @throws Exception  if there is an error opening the browser
   */
  public static void browse(URL url) throws Exception {
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Mac OS")) {
      Class fileMgr = Class.forName("com.apple.eio.FileManager");
      Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
      openURL.invoke(null, new Object[] {url});
    } else if (osName.startsWith("Windows")) {
      Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
    } else { //assume Unix or Linux
      String[] browsers = { "sensible-browser", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
      String browser = null;
      for (int count = 0; count < browsers.length && browser == null; count++)
        if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
          browser = browsers[count];
      if (browser == null)
        throw new Exception("Could not find web browser");
      else Runtime.getRuntime().exec(new String[] {browser, url.toString()});
    }
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

		String configFile = null;
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

		String[] leftOverOptions = line.getArgs();
		if (leftOverOptions != null && leftOverOptions.length > 0) {
			configFile = leftOverOptions[0];
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
		DataViewer dataViewer = new DataViewer(false);
		
    RBNBController rbnbController = dataViewer.getRBNBController();
    ControlPanel controlPanel = dataViewer.getApplicationFrame().getControlPanel();

    if (configFile != null) {
      URL configURL;      
      try {
        if (configFile.matches("^[a-zA-Z]+://.*")) {
          configURL = new URL(configFile);
        } else {
          configURL = new File(configFile).toURL();
        }
        
        dataViewer.getConfigurationManager().loadConfiguration(configURL);
        return;
      } catch (MalformedURLException e) {
        dataViewer.alertError("\"" + configFile + "\" is not a valid configuration file URL.");
      }
    }
    
		if (playbackRate != -1) {
			rbnbController.setPlaybackRate(playbackRate);
		}
		
		if (timeScale != -1) {
      controlPanel.setTimeScale(timeScale);
			rbnbController.setTimeScale(timeScale);
		}
				
		if (portNumber != -1) {
			rbnbController.setRBNBPortNumber(portNumber);
		}
		
		if (hostName != null) {
			rbnbController.setRBNBHostName(hostName);
			
      if (rbnbController.connect(true)) {
  			if (channels != null) {
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
		} else {
		  ActionFactory.getInstance().getOfflineAction().goOffline();
    }
	}
}
