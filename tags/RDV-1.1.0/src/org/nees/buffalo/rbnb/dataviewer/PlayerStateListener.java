package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public interface PlayerStateListener {
	public void postState(int newState, int oldState);
}
