/*
 * Created on Feb 5, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

/**
 * A class to describe a channel containing data and the metadata associated with it.
 * 
 * @author  Jason P. Hanley
 * @since   1.1
 */
public class Channel {

	/**
	 * The full name of the channel
	 */
	String name;
	
	/**
	 * The mime type of the data contained in the channel
	 */
	String mimeType;
	
	/**
	 * The unit of measurement used for the data in the channel
	 */
	String unit;
	
	/**
	 * Construct a channel with a name and no associated metadata.
	 * 
	 * @param name  The full name of the channel
	 * @since       1.1
	 */
	public Channel(String name) {
		this(name, null, null);
	}
	
	/**
	 * Construct a channel with a name and assigning the mime type and
	 * unit as metadata.
	 * 
	 * @param name      The full name of the channel
	 * @param mimeType  The mime type of the data contained in the channel
	 * @param unit      The unit of measurement used for the data in the channel
	 * @since           1.1
	 */
	public Channel(String name, String mimeType, String unit) {
		this.name = name;
		this.mimeType = mimeType;
		this.unit = unit;
	}
	
	/**
	 * Get the short name of the channel.
	 * 
	 * This is the part after the final forward slash (/).
	 * 
	 * @return  the short name of the channel
	 * @since   1.1
	 */
	public String getShortName() {
		return name.substring(name.lastIndexOf("/")+1);
	}
	
	/**
	 * Get the full name of the channel.
	 * 
	 * @return  the full name of the channel.
	 * @since   1.1
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the parent of the channel.
	 * 
	 * This is the part before the final forward slash (/).
	 * 
	 * @return  the parent of the channel
	 * @since   1.1
	 */
	public String getParent() {
		return name.substring(0, name.lastIndexOf("/"));
	}
	
	/**
	 * Get the mime type of the data contained in the channel.
	 * 
	 * @return  the mime type for the channel
	 * @since   1.1
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * Get the unit of measurement used for the data in the channel.
	 * 
	 * @return  the unit for the channel data
	 * @since   1.1
	 */
	public String getUnit() {
		return unit;
	}
}
