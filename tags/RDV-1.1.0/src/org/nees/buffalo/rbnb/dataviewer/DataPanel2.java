package org.nees.buffalo.rbnb.dataviewer;

import javax.swing.JComponent;

/**
 * @author Jason P. Hanley
 */
public interface DataPanel2 {
	public JComponent getComponent();
	public String[] getSupportedMimeTypes();
	public boolean supportsMultipleChannels();
	public boolean setChannel(Channel channel);
	public boolean addChannel(Channel channel);
	public boolean removeChannel(String channelName);
	public void setDomain(double domain);
	public void closePanel();
}
