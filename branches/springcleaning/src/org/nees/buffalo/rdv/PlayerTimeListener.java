package org.nees.buffalo.rdv;

/**
 * A listener interface to post the current time of the player.
 * 
 * @author  Jason P. Hanley
 * @see     Player#addTimeListener(PlayerTimeListener)
 * @see     Player#removeTimeListener(PlayerTimeListener)
 * @since   1.0
 */
public interface PlayerTimeListener {
	/**
	 * Post the current time of the player.
	 * 
	 * @param time  the current time of the player
	 * @since       1.0
	 */
	public void postTime(double time);
}
