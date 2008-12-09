/*
 * Created on Apr 7, 2005
 */
package org.nees.buffalo.rdv.rbnb;

/**
 * An interface to post status and error messages from the RBNBController.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public interface MessageListener {
	/**
	 * Post the error and its associated text.
	 * 
	 * @param errorMessage  the error message
	 * @since               1.2
	 */
	public void postError(String errorMessage);
	
	/**
	 * Post the status and its associated text.
	 * 
	 * @param statusMessage  the status message
	 * @since                1.2
	 */
	public void postStatus(String statusMessage);
}
