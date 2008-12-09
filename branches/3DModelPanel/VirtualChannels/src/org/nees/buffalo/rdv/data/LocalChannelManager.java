/*
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  o The above copyright notice and this permission notice shall be included
 *    in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nees.buffalo.rdv.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rbnb.sapi.ChannelMap;

/**
 * 
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class LocalChannelManager {
  
  static Log log = LogFactory.getLog(LocalChannelManager.class.getName());
  
  private static String extensionsConfigFileName = "config/local_channels.xml";
  
  RBNBController rbnb;
  
  List localChannelExtensions;
  
  Map localChannels;

  public LocalChannelManager(RBNBController rbnb) {
    this.rbnb = rbnb;
    
    localChannelExtensions = new ArrayList();
    loadExtensionManifest();
    
    localChannels = new HashMap();
  }
  
  private void loadExtensionManifest() {
    DOMParser xmlParser = new DOMParser();
    try {
      ClassLoader cl = getClass().getClassLoader();
      xmlParser.parse(cl.getResource(extensionsConfigFileName).toExternalForm());
      Document xmlDocument = xmlParser.getDocument();
      NodeList extensionNodes = xmlDocument.getElementsByTagName("extension");
      for (int i=0; i<extensionNodes.getLength(); i++) {
        Node extensionNode = extensionNodes.item(i);
        String extensionID = null;
        String extensionType = null;
        String extensionName = null;
        String extensionDescription = null;
        NodeList parameters = extensionNode.getChildNodes();
        for (int j=0; j<parameters.getLength(); j++) {
          Node parameterNode = parameters.item(j);
          String parameterName = parameterNode.getNodeName();
          if (parameterName.equals("id")) {
            extensionID = parameterNode.getFirstChild().getNodeValue();
          } else if (parameterName.equals("type")) {
            extensionType = parameterNode.getFirstChild().getNodeValue();
          } else if (parameterName.equals("name")) {
            extensionName = parameterNode.getFirstChild().getNodeValue();
          } else if (parameterName.equals("description")) {
            extensionDescription = parameterNode.getFirstChild().getNodeValue();                  
          }
        }
        
        LocalChannelExtension extension = new LocalChannelExtension(extensionID, extensionName, extensionDescription);
        localChannelExtensions.add(extension);
        log.info("Registered local channel extension " + extensionName + " (" + extensionID + ").");
      }
    } catch (Exception e) {
      log.error("Failed to fully load the local channel extension manifest.");
      e.printStackTrace();
    }    
  }
  
  /**
   * Create a <code>LocalChannel</code> with the specified name and extension
   * class.
   * 
   * @param name                         the name of the new channel
   * @param extension                    the class of the extension
   * @return                             the local channel
   * @throws ExtensionNotFoundException  if the extension class isn't found
   * @throws ExtensionFailedException    if the extension couldn't be loaded
   * @since                              1.3
   */
  public LocalChannel createLocalChannel(String name, LocalChannelExtension extension)
              throws ExtensionNotFoundException, ExtensionFailedException {
    Class localChannelClass;
    try {
      localChannelClass = Class.forName(extension.getID());
    } catch (ClassNotFoundException e) {
        throw new ExtensionNotFoundException("The extension class " + extension.getID() + " could not be loaded.", e);
    }
    
    LocalChannel localChannel;
    try {
      localChannel = (LocalChannel)localChannelClass.newInstance();
    } catch (Exception e) {
      throw new ExtensionFailedException("Can't not instantiate extension " + extension.getID() + ".", e);
    }
    
    localChannel.initialize(name, rbnb);
    
    localChannels.put(name, localChannel);
    
    return localChannel;
  }
  
  /**
   * Dispose of the channel.
   * 
   * @param name  the channel name
   * @since       1.3
   */
  public void dispose(String name) {
    LocalChannel localChannel = (LocalChannel)localChannels.get(name);
    if (localChannel != null) {
      dispose(localChannel);
    }
  }
  
  public void dispose(LocalChannel localChannel) {
    if (localChannel != null) {
      localChannels.remove(localChannel.getName());
      localChannel.dispose();
    }        
  }
  
  public List getLocalChannelExtensions() {
    return localChannelExtensions;
  }
  
  public void updateMetadata(ChannelMap cmap) {
    log.info("Updating metadata for local channels.");
    
    Iterator i = localChannels.values().iterator();
    while (i.hasNext()) {
      LocalChannel localChannel = (LocalChannel)i.next();
      localChannel.updateMetadata(cmap);
    }
  }
}
