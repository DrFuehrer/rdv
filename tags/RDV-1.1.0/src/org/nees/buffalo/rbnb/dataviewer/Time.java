/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

/**
 * @author Jason P. Hanley
 */
public class Time {

	public double location;
	public double duration;
	
	public Time(double location, double duration) {
		this.location = location;
		this.duration = duration;
	}
	
	public boolean isUnspecified() {
		return (location == -1 && duration == -1);
	}
	
	public boolean equals(Time time) {
		return (time.location == location && time.duration == duration);
	}
	
}
