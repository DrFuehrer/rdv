/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public class TimeSlice {

	public double location;
	public double duration;
	
	public TimeSlice(double location, double duration) {
		this.location = location;
		this.duration = duration;
	}
	
	public boolean isUnspecified() {
		return (location == -1 && duration == -1);
	}
	
	public boolean equals(TimeSlice ts) {
		return (ts.location == location && ts.duration == duration);
	}
	
}
