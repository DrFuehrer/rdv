package org.nees.buffalo.rdv;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

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

/**
 * @author Jason P. Hanley
 */
public class DataViewer {
	
	static Log log = LogFactory.getLog(DataViewer.class.getName());
	
	private RBNBController rbnb;
	private DataPanelManager dataPanelManager;
	private ApplicationFrame applicationFrame;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
	
	public DataViewer() {
		rbnb = new RBNBController();
		dataPanelManager = new DataPanelManager(rbnb);
		applicationFrame = new ApplicationFrame(this, rbnb, dataPanelManager);
	}		
		
	public void setTimeScale(double timeScale) {
		/* rbnb.setTimeScale(timeScale); */
	}

	public void exit() {
		applicationFrame.dispose();
		if (rbnb != null) {
			rbnb.exit();
		}
		log.info("Exiting.");		
	}
 	
 	public Player getPlayer() {
 		return rbnb;
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
		return DATE_FORMAT.format(new Date(((long)(date*1000))));
	}
	
	public static String formatDate(long date) {
		return DATE_FORMAT.format(new Date(date));
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
 		} else {
 			secondsString = Double.toString(round(seconds/(60*60*24))) + " d";
 		}
 		return secondsString;
 	}
 	
 	public static String formatBytes(int bytes) {
 		String bytesString;
 		if (bytes < 1024) {
 			bytesString = Integer.toString(bytes) + " B";
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
 		return (long)(f*100)/100f;
 	}
 	 	
 	public static double round(double d) {
 		return (long)(d*100)/100d;
 	}

	public static void main(String[] args) {
		//enable dynamic layout during application resize
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (!toolkit.isDynamicLayoutActive()) {
			toolkit.setDynamicLayout(true);
		}
		
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
		options.addOption(timeScaleOption);
		options.addOption(playOption);
		options.addOption(realTimeOption);			
		options.addOption(helpOption);
				
		String hostName = null;
		int portNumber = -1;
		String[] channels = null;
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
		
		if (timeScale != -1) {
			dataViewer.setTimeScale(timeScale);
		}
		
		RBNBController rbnbController = dataViewer.getRBNBController();
				
		if (portNumber != -1) {
			rbnbController.setRBNBPortNumber(portNumber);
		}
		
		if (hostName != null) {
			rbnbController.setRBNBHostName(hostName);
			rbnbController.connect();
		}
		
		//FIXME need to wait for metadata before adding channels
		
		if (channels != null && hostName != null) {
			for (int i=0; i<channels.length; i++) {
				String channel = channels[i];
				log.info("Viewing channel " + channel + ".");
				if (!dataViewer.getDataPanelManager().viewChannel(channel)) {
					log.error("Failed to view channel " + channel + ".");
				}
			}
		}
		
		Player player = dataViewer.getPlayer();
		if (play) {
			log.info("Starting data playback.");
			player.play();
		} else if (realTime) {
			log.info("Viewing data in real time.");
			player.monitor();
		}
	}
}
