package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to post the current state of the player.
 * 
 * @author  Jason P. Hanley
 * @see     Player#addStateListener(PlayerStateListener)
 * @see     Player#removeStateListener(PlayerStateListener)
 * @since   1.0
 */
public interface PlayerStateListener {
	/**
	 * Posts the a state change in the player.
	 * 
	 * @param newState  the new player state
	 * @param oldState  the old player state
	 * @since           1.0
	 */
	public void postState(int newState, int oldState);
}
