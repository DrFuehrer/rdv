package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to notify listeners when the time scale
 * has changed.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface TimeScaleListener {
	/**
	 * Called when the time scale has been changed
	 * 
	 * @param timeScale  the new value of the time scale
	 * @since            1.0
	 */
	public void timeScaleChanged(double timeScale);
}
