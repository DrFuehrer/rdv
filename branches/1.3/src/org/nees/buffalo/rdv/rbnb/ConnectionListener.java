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
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/rbnb/ConnectionListener.java $
 * $Revision: 316 $
 * $Date: 2005-10-19 20:11:00 -0700 (Wed, 19 Oct 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to provide progress on a connection to a server.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public interface ConnectionListener {
	/**
	 * The client is connecting to the server.
	 * <p>
	 * This will be followed by either a connected event or a connection failed
	 * event.
	 * 
	 * @since  1.2
	 */
	public void connecting();

	/**
	 * The client is connected to the server.
	 *
	 * @since  1.2
	 */
	public void connected();
	
	/**
	 * The attempt to connect to the server failed.
	 * 
	 * @since  1.2
	 */
	public void connectionFailed();	
}
