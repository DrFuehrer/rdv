package org.nees.buffalo.rdv.rbnb;

/**
 * A listener interface to notify listeners when the playback
 * rate has changed.
 * 
 * @author  Jason P. Hanley
 * @see     Player#getPlaybackRate()
 * @since   1.0
 */
public interface PlaybackRateListener {
	/**
	 * Post the new playback rate.
	 * 
	 * @param playbackRate  the new playback rate
	 * @since               1.0
	 */
	public void playbackRateChanged(double playbackRate);
}
