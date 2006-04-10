/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL: https://svn.nees.org/svn/telepresence/RDVeventMarkers/src/org/nees/buffalo/rdv/DataPanelManager.java $
 * $Revision: 319 $
 * $Date: 2005-11-16 12:13:34 -0800 (Wed, 16 Nov 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBUtilities;
import org.nees.buffalo.rdv.ui.DataPanelContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rbnb.sapi.ChannelTree;

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
	 * The name of the extensions configuration file
	 */
	private static String extensionsConfigFileName = "config/extensions.xml";
	
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
			ClassLoader cl = getClass().getClassLoader();
			xmlParser.parse(cl.getResource(extensionsConfigFileName).toExternalForm());
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
	 * Return the registered extension specified by the given class.
	 * 
	 * @param extensionClass  the class of the desired extension
	 * @return                the extension, or null if the class wasn't found
	 */
	public Extension getExtension(Class extensionClass) {
		for (int i=0; i<extensions.size(); i++) {
			Extension extension = (Extension)extensions.get(i);
			if (extension.getID().equals(extensionClass.getName())) {
				return extension;
			}
		}
		
		return null;
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
   * Return a list of extension that support the channel.
   * 
   * @param channelName  the channel to support
   * @return             a list of supported extensions
   * @since              1.3
   */
  public ArrayList getExtensions(String channelName) {
    Channel channel = rbnbController.getChannel(channelName);
    
    String mime = null;
    if (channel != null) {
      mime = channel.getMetadata("mime");
    }
    mime = RBNBUtilities.fixMime(mime, channelName);
    
    ArrayList usefulExtensions = new ArrayList();
    for (int i=0; i<extensions.size(); i++) {
      Extension extension = (Extension)extensions.get(i);
      ArrayList mimeTypes = extension.getMimeTypes();
      for (int j=0; j<mimeTypes.size(); j++) {
        String mimeType = (String)mimeTypes.get(j);
        if (mimeType.equals(mime)) {
          usefulExtensions.add(extension);
        }
      }
    }
    
    return usefulExtensions;
  }
  
  /**
   * Return the default extension for the channel or null if there is none.
   * 
   * @param channelName  the channel to use 
   * @return             the default extension or null if there is none
   * @since              1.3
   */
  public Extension getDefaultExtension(String channelName) {
    Channel channel = rbnbController.getChannel(channelName);
    String mime = null;
    if (channel != null) {
      mime = channel.getMetadata("mime");
    }

    mime = RBNBUtilities.fixMime(mime, channelName);

    return findExtension(mime);
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
		for (int i=dataPanels.size()-1; i>=0; i--) {
			dataPanel = (DataPanel)dataPanels.get(i);
			closeDataPanel(dataPanel);
		}
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
	 * @param channelName  the name of the channel to view
	 * @since              1.2
	 */
	public void viewChannel(String channelName) {
    viewChannel(channelName, getDefaultExtension(channelName));
	}
  
  /**
   * View the channel given by the channel name with the specified extension.
   * 
   * @param channelName  the name of the channel to view
   * @param extension    the extension to view the channel with
   * @since              1.3
   */
  public void viewChannel(String channelName, Extension extension) {
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
   * View the list of channels with the specified extension. If the channel
   * supports multiple channels, all the channels will be viewed in the same
   * instance. Otherwise a new instance will be created for each channel.
   * 
   * If adding a channel to the same instance of an extension fails, a new
   * instance of the extension will be created and the channel will be added
   * this this.
   * 
   * @param channels   the list of channels to view
   * @param extension  the extension to view these channels with
   * @since            1.3
   */
  public void viewChannels(List channels, Extension extension) {
    DataPanel dataPanel = null;
    try {
      dataPanel = createDataPanel(extension);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    boolean supportsMultipleChannels = dataPanel.supportsMultipleChannels();
    
    Iterator it = channels.iterator();
    while (it.hasNext()) {
      String channelName = ((ChannelTree.Node)it.next()).getFullName();
      if (supportsMultipleChannels) {
        boolean subscribed = dataPanel.addChannel(channelName);

        if (!subscribed) {
          try {
            dataPanel = createDataPanel(extension);
          } catch (Exception e) {
            e.printStackTrace();
            return;
          }
          
          dataPanel.addChannel(channelName);
        }
      } else {
        if (dataPanel == null) {
          try {
            dataPanel = createDataPanel(extension);
          } catch (Exception e) {
            e.printStackTrace();
            return;
          }
        }
        
        dataPanel.setChannel(channelName);
        dataPanel = null;
      }
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
  
  /**
   * See if any data panels are subscribed to channels of this source.
   * 
   * @param sourceName  the source to check
   * @return            true if a data panel is subscribed to a channel of this
   *                    source, false otherwise
   * @since             1.3
   */
  public boolean isSourceSubscribed(String sourceName) {
    boolean subscribed = false;
    ChannelTree ctree = rbnbController.getMetadataManager().getMetadataChannelTree();
    ChannelTree.Node source = ctree.findNode(sourceName);
    if (source != null) {
      Iterator i = source.getChildren().iterator();
      while (i.hasNext()) {
        ChannelTree.Node node = (ChannelTree.Node)i.next(); 
        if (node.getType() == ChannelTree.CHANNEL) {
          String channelName = node.getFullName();
          if (isChannelSubscribed(channelName)) {
            subscribed = true;
            break;
          }
        }
      }
    }
    return subscribed;
  }
  
  /**
   * See if any data panel is subscribed to this channel
   * 
   * @param channelName  the channel name to check with
   * @return             true if any data panel is subscribed to the channel,
   *                     false otherwise
   * @since              1.3
   */
  public boolean isChannelSubscribed(String channelName) {
    boolean subscribed = false;
    Iterator i = dataPanels.iterator();    
    while (i.hasNext()) {
      DataPanel dp = (DataPanel)i.next();
      if (dp.isChannelSubscribed(channelName)) {
        subscribed = true;
        break;
      }
    }
    return subscribed;
  }
  
  public void unsubscribeSource(String sourceName) {
    unsubscribeSource(sourceName, false);
  }
  
  /**
   * Unsubscribe data panels from all channels that are a part of this sorce. If
   * closeIfEmpty is set, data panels that aren't subscribed to any channels
   * will be closed.
   * 
   * @param sourceName    the source containing the channels to unsubscribe from
   * @param closeIfEmpty  close a data panel if it contains no more channels
   * @since               1.3
   */
  public void unsubscribeSource(String sourceName, boolean closeIfEmpty) {
    ChannelTree ctree = rbnbController.getMetadataManager().getMetadataChannelTree();
    ChannelTree.Node source = ctree.findNode(sourceName);
    if (source != null) {
      Iterator i = source.getChildren().iterator();
      while (i.hasNext()) {
        ChannelTree.Node node = (ChannelTree.Node)i.next(); 
        if (node.getType() == ChannelTree.CHANNEL) {
          String channelName = node.getFullName();
          unsubscribeChannel(channelName, false);
        }
      }
    }   
    
    if (closeIfEmpty) {
      closeEmptyDataPanels();
    }    
  }
  
  /**
   * Unsubscribe all data panels from this channel. If closeIfEmpty is set, data
   * panels that aren't subscribed to any channels will be closed.
   * 
   * @param channelName  the channel to unsubscribe
   * @since              1.3
   */
  public void unsubscribeChannel(String channelName) {
    unsubscribeChannel(channelName, false);
  }
  
  /**
   * Unsubscribe all data panels from this channel. If closeIfEmpty is set, the
   * data panel will be closed if it is subscribed to no additional channels.
   * 
   * @param channelName   the channel to unsubscribe
   * @param closeIfEmpty  close a data panel if it contains no more channels
   * @since               1.3
   */
  public void unsubscribeChannel(String channelName, boolean closeIfEmpty) {
    Iterator i = dataPanels.iterator();  
    while (i.hasNext()) {
      DataPanel dp = (DataPanel)i.next();
      dp.removeChannel(channelName);
    }
    
    if (closeIfEmpty) {
      closeEmptyDataPanels();
    }
  }
  
  /**
   * Close data panels that aren't subscribed to any channels.
   * 
   * @since  1.3
   */
  private void closeEmptyDataPanels() {
    for (int i=dataPanels.size()-1; i>=0; i--) {
      DataPanel dp = (DataPanel)dataPanels.get(i);
      if (dp.subscribedChannelCount() == 0) {
        closeDataPanel(dp);
      }
    }
  }
}
