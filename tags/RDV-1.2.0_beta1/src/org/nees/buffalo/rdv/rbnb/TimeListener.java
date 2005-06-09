package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to post the current time of the player.
 * 
 * @author  Jason P. Hanley
 * @see     Player#addTimeListener(TimeListener)
 * @see     Player#removeTimeListener(TimeListener)
 * @since   1.0
 */
public interface TimeListener {
	/**
	 * Post the current time of the player.
	 * 
	 * @param time  the current time of the player
	 * @since       1.0
	 */
	public void postTime(double time);
}
