/*
 * Created on Apr 3, 2005
 */
package org.nees.buffalo.rdv;

import java.util.ArrayList;

/**
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class Extension {
	private String ID;
	private String name;
	private ArrayList mimeTypes;
	
	public Extension(String ID, String name, ArrayList mimeTypes) {
		this.ID = ID;
		this.name = name;
		this.mimeTypes = mimeTypes;
	}
	
	public String getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList getMimeTypes() {
		return mimeTypes;
	}
}
