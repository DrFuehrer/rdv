/*
 * Created on Mar 31, 2005
 */
package org.nees.buffalo.rdv;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.DataPanelContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to manage all the data panels.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class DataPanelManager {
	
	/**
	 * The logger for this class.
	 * 
	 * @since  1.2
	 */
	static Log log = LogFactory.getLog(DataPanelManager.class.getName());
	
	/**
	 * A reference to the RNBN controller for the data panels to use.
	 * 
	 * @since  1.2
	 */
	private RBNBController rbnbController;
	
	/**
	 * A reference to the data panel container for the data panels
	 * to add their ui component too.
	 * 
	 * @since  1.2
	 */
	private DataPanelContainer dataPanelContainer;

	/**
	 * A list of all the data panels.
	 * 
	 * @since  1.2
	 */
	private ArrayList dataPanels;
	
	/**
	 * A list of registered extensions
	 */
	private ArrayList extensions;
	
	/** 
	 * The constructor for the data panel manager. This initializes the
	 * data panel container and the list of registered data panels.
	 * 
	 * 
	 * @param rbnbController  the rbnb controller
	 * @since                 1.2
	 */
	public DataPanelManager(RBNBController rbnbController) {
		this.rbnbController = rbnbController;
		dataPanelContainer = new DataPanelContainer();
		
		dataPanels = new ArrayList();
		extensions = new ArrayList();
		
		loadExtenionManifest();
	}
	
	/**
	 * Load the the  extension manifest file describing all the
	 * extensions and their properties.
	 *
	 * @since  1.2
	 */
	private void loadExtenionManifest() {
		DOMParser xmlParser = new DOMParser();
		try {
			xmlParser.parse("config/extensions.xml");
			Document xmlDocument = xmlParser.getDocument();
			NodeList extensionNodes = xmlDocument.getElementsByTagName("extension");
			for (int i=0; i<extensionNodes.getLength(); i++) {
				Node extensionNode = extensionNodes.item(i);
				String extensionID = null;
				String extensionName = null;
				ArrayList mimeTypes = new ArrayList();
				NodeList parameters = extensionNode.getChildNodes();
				for (int j=0; j<parameters.getLength(); j++) {
					Node parameterNode = parameters.item(j);
					String parameterName = parameterNode.getNodeName();
					if (parameterName.equals("id")) {
						extensionID = parameterNode.getFirstChild().getNodeValue();
					} else if (parameterName.equals("name")) {
						extensionName = parameterNode.getFirstChild().getNodeValue();
					} else if (parameterName.equals("mimeTypes")) {
						NodeList mimeTypeNodes = parameterNode.getChildNodes();
						for (int k=0; k<mimeTypeNodes.getLength(); k++) {
							Node mimeTypeNode = mimeTypeNodes.item(k);
							if (mimeTypeNode.getNodeName().equals("mimeType")) {
								mimeTypes.add(mimeTypeNode.getFirstChild().getNodeValue());
							}
						}
					}
				}
				
				Extension extension = new Extension(extensionID, extensionName, mimeTypes);
				extensions.add(extension);
				log.info("Registered extension " + extensionName + " (" + extensionID + ").");
			}
		} catch (Exception e) {
			log.error("Failed to fully load the extension manifest.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a list of registered extensions.
	 * 
	 * @return  an array list of extensions
	 * @see     Extension
	 * @since   1.2
	 */
	public ArrayList getExtensions() {
		return extensions;
	}
	
	/**
	 * Returns the RBNB controller.
	 * 
	 * @return  the rbnb controller
	 * @since   1.2
	 */
	public RBNBController getRBNBController() {
		return rbnbController;
	}
	
	/**
	 * Returns the data panel container where data panels can
	 * add their ui components too.
	 * 
	 * @return  the data panel container
	 * @since   1.2
	 */
	public DataPanelContainer getDataPanelContainer() {
		return dataPanelContainer;
	}
	
	/**
	 * Calls the closePanel method on the specified data panel.
	 * 
	 * After this method has been called this data panel instance has
	 * reached the end of its life cycle.
	 * 
	 * @param dataPanel  the data panel to be closed
	 * @see              DataPanel#closePanel()
	 * @since            1.2
	 */
	public void closeDataPanel(DataPanel dataPanel) {
		dataPanel.closePanel();
		dataPanels.remove(dataPanel);
	}

	/**
	 * Calls the <code>closePanel</code> method on each registered data panel.
	 * 
	 * @see    DataPanel#closePanel()
	 * @since  1.2
	 */
	public void closeAllDataPanels() {
		DataPanel dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel)dataPanels.get(i);
			dataPanel.closePanel();
		}
		dataPanels.clear();
	}
	
	/**
	 * View the channel specified by the given channel name. This will
	 * cause the data panel manager to look for the default viewer for
	 * the mime type of the viewer.
	 * 
	 * If the channel has no mime type, it is assumed to be numeric data
	 * unless the channel ends with .jpg, in which case it is assumed to
	 * be image or video data.
	 * 
	 * @param channelName  the name of the channe to view
	 * @since              1.2
	 */
	public void viewChannel(String channelName) {
		Channel channel = rbnbController.getChannel(channelName);
		String mime = null;
		if (channel != null) {
			mime = channel.getMimeType();
		}

		if (mime == null && channelName.endsWith(".jpg")) {
			mime = "image/jpeg";
		} else if (mime == null) {
			mime = "application/octet-stream";
		}

		Extension extension = findExtension(mime);
		if (extension != null) {
			log.info("Creating data panel for channel " + channelName + " with extension " + extension.getName() + ".");
			try {
				DataPanel dataPanel = createDataPanel(extension);
				dataPanel.setChannel(channelName);	
			} catch (Exception e) {
				log.error("Failed to create data panel and add channel.");
				e.printStackTrace();
				return;
			}			
		} else {
			log.warn("Failed to find data panel extension for channel " + channelName + ".");
		}
	}
	
	/**
	 * Return an extension that is capable of viewing the
	 * provided mime type.
	 * 
	 * @param mime  the mime type that the extension must support
	 * @return      an extension to view this mime type, or null if none is found
	 */
	private Extension findExtension(String mime) {
		for (int i=0; i<extensions.size(); i++) {
			Extension extension = (Extension)extensions.get(i);
			ArrayList mimeTypes = extension.getMimeTypes();
			for (int j=0; j<mimeTypes.size(); j++) {
				String mimeType = (String)mimeTypes.get(j);
				if (mimeType.equals(mime)) {
					return extension;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Creates the data panel referenced by the supplied extension
	 * ID. This ID is by convention, the name of the class implementing
	 * this extension.
	 * 
	 * @param extension   the extension to create
	 * @throws Exception  
	 * @return            the newly created data panel
	 * @since             1.2
	 */
	public DataPanel createDataPanel(Extension extension) throws Exception {
		Class dataPanelClass;
		try {
			dataPanelClass = Class.forName(extension.getID());
		} catch (ClassNotFoundException e) {
			throw new Exception("Unable to find extension " + extension.getName());
		}
		
		DataPanel dataPanel = (DataPanel)dataPanelClass.newInstance();
		
		dataPanel.openPanel(this);
		dataPanels.add(dataPanel);
		
		return dataPanel;
	}
}