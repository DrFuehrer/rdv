/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public class Channel {

	String name;
	String mimeType;
	String unit;
	
	public Channel(String name) {
		this(name, null, null);
	}
	
	public Channel(String name, String mimeType, String unit) {
		this.name = name;
		this.mimeType = mimeType;
		this.unit = unit;
	}
	
	public String getShortName() {
		return name.substring(name.lastIndexOf("/")+1);
	}
	
	public String getName() {
		return name;
	}
	
	public String getParent() {
		return name.substring(0, name.lastIndexOf("/"));
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getUnit() {
		return unit;
	}
}
