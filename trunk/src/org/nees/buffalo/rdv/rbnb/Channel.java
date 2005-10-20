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

package org.nees.buffalo.rdv.rbnb;

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
