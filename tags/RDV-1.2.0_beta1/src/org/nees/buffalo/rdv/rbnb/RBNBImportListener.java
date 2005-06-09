/*
 * Created on Apr 7, 2005
 */
package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface for the progress of an upload to RBNB.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public interface RBNBImportListener {
	public void postProgress(double progress);
	public void postCompletion();
	public void postError(String errorMessage);
}
