package org.nees.buffalo.rbnb.dataviewer;

/**
 * Post the current time scale of the player.
 * 
 * @author  Jason P. Hanley
 * @see     Player#getTimeScale()
 * @since   1.0
 */
public interface TimeScaleListener {
	/**
	 * Post the new time scale.
	 * 
	 * @param timeScale  the new time scale
	 * @since            1.0
	 */
	public void timeScaleChanged(double timeScale);
}
