package org.nees.buffalo.rbnb.dataviewer;

import java.util.ArrayList;

/**
 * @author Jason P. Hanley
 */
public class Configuration {

	private String rbnbHostName;
	private int rbnbPort;
	
	private int state;
	private double location;
	private double timeScale;
	private double domain;
	
	private boolean channelListVisible;
	private boolean controlPanelVisible;
	private boolean statusPanelVisible;
	
	private ArrayList viewerConfigurations;
	
	public static Configuration loadConfiguration(String fileName) {
		return null;
	}
	
	public boolean saveConfiguration(String fileName) {
		return saveConfiguration(fileName, this);
	}
	
	public static boolean saveConfiguration(String fileName, Configuration configuration) {
		return false;
	}
	
	class ViewerConfiguration {
		public String type;
		public ArrayList channels;
	}
}
