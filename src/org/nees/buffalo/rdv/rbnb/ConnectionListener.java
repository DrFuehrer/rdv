/*
 * Created on Apr 8, 2005
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
