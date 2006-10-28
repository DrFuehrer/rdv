package org.nees.buffalo.rdv.rbnb;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.ArrayList;

/**
 * Sigleton LogManager class to register interested LogListener classes
 * and also create a Timer thread to fire write to log event
 *
 */

public class LogManager {
	
	protected static LogManager instance;

	private Timer timer;
	
	/**
	 * List of interested LogListener classes 
	 */
	private List<LogListener> logListeners;
	
	
	/**
	 * time interval in miliseconds to fire timer event
	 */
	private static final int INTERVAL = 10000;  //one minute 
	
	/**
	 * Creates a new instance if one is not existing
	 * @return LogManager instance
	 */
	public static LogManager getInstance() {
		
		if (instance == null) {
			
			instance = new LogManager();
		}
		
		return instance;
	}
	
	
	/**
	 * protected constructor
	 */
	protected LogManager() {
		
		logListeners = new ArrayList<LogListener>();
		run();
	}
	
	/**
	 * Create and start a javax.swing.Timer
	 */
	private void run() {
		
		timer = new Timer(INTERVAL, new ActionListener() {
			
			public void actionPerformed(ActionEvent ae) {
				fireWriteToLog();
			}
			
		});
		
		timer.start();
		
	}

	/**
	 * method to call all listeners' writeLog()
	 */
	private void fireWriteToLog() {
	
		for (LogListener listener : logListeners) {
			
			listener.writeLog();
		}
	}
	
	/**
	 * Adds a LogListener class to list (if not existing)
	 * @param listener
	 */
	public void addLogListener(LogListener listener) {
		
		if (!logListeners.contains(listener))
			logListeners.add(listener);
	}

	/**
	 * Removes a LogListener class from the list (if existing)
	 * @param listener
	 */
	public void removeLogListener(LogListener listener) {
		
		if (logListeners.contains(listener))
			logListeners.remove(listener);
	}
	
}
