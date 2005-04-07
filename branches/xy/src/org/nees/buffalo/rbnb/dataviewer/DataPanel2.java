package org.nees.buffalo.rbnb.dataviewer;

import javax.swing.JComponent;

/**
 * @author Jason P. Hanley
 */
public interface DataPanel2 {
	public JComponent getComponent();
	public String[] getSupportedMimeTypes();
	public boolean supportsMultipleChannels();
	public void setChannel(String channelName);
	public void addChannel(String channelName);
	public void removeChannel(String channelName);
	public void setDomain(double domain);
	public void closePanel();
}