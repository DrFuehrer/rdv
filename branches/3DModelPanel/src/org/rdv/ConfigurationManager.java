/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 * Copyright (c) 2008 Palta Software
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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.data.LocalChannel;
import org.rdv.data.LocalChannelManager;
import org.rdv.datapanel.DataPanel;
import org.rdv.rbnb.RBNBController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to manage the application configuration.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class ConfigurationManager {
  /**
   * The logger for this class.
   * 
   * @since  1.3
   */
  static Log log = LogFactory.getLog(ConfigurationManager.class.getName());
  
  /**
   * This class can not be instantiated and it's constructor always throws an
   * exception.
   */
  private ConfigurationManager() {
    throw new UnsupportedOperationException("This class can not be instantiated.");
  }

  /**
   * Save the application configuration to the specified file.
   * 
   * @param configFile  the file to save the configuration to
   * @since             1.3
   */
  public static void saveConfiguration(File configFile) {
    PrintWriter out;
    try {
      out = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
    } catch (IOException e) {
      return;
    }
    
    out.println("<?xml version=\"1.0\"?>");
    out.println("<rdv>");
    
    RBNBController rbnb = RBNBController.getInstance();
    out.println("  <rbnb>");
    out.println("    <host>" + rbnb.getRBNBHostName() + "</host>");
    out.println("    <port>" + rbnb.getRBNBPortNumber() + "</port>");
    out.println("    <state>" + RBNBController.getStateName(rbnb.getState()) + "</state>");
    out.println("    <timeScale>" + rbnb.getTimeScale() + "</timeScale>");
    out.println("    <playbackRate>" + rbnb.getPlaybackRate() + "</playbackRate>");
    out.println("  </rbnb>");
    
    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
    if (localChannelManager.hasChannels()) {
      for (LocalChannel localChannel : localChannelManager.getChannels()) {
        out.print("  <localChannel name=\"" + localChannel.getName() + "\"");
        if (localChannel.getUnit() != null) {
          out.print(" unit=\"" + localChannel.getUnit() + "\"");
        }
        out.println(" formula=\"" + localChannel.getFormula() + "\">");
        for (Entry<String, String> variable : localChannel.getVariables().entrySet()) {
          out.println("    <variable name=\"" + variable.getKey() + "\" channel=\"" + variable.getValue() + "\"/>");
        }
        out.println("  </localChannel>");
      }
    }
    
    List<DataPanel> dataPanels = DataPanelManager.getInstance().getDataPanels();
    Iterator<DataPanel> it = dataPanels.iterator();
    while (it.hasNext()) {
      DataPanel dataPanel = it.next();
      Properties properties = dataPanel.getProperties();
      
      if (isPanelDetached(dataPanel, properties)) {
        if (dataPanel.subscribedChannelCount() == 0)  // A detached panel with no channels subscribed, or a non-existing detached panel 
          continue; // don't add to configuration
      }
      
      out.println("  <dataPanel id=\"" + dataPanel.getClass().getName() + "\">");
      
      if (dataPanel.subscribedChannelCount() > 0) {
        out.println("    <channels>");
        Iterator<String> channels = dataPanel.subscribedChannels().iterator();
        while (channels.hasNext()) {
          String channel = channels.next();
          out.println("      <channel>" + channel + "</channel>");
        }     
        out.println("    </channels>");
      }


      if (properties.size() > 0) {
        out.println("    <properties>");
        for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements() ;) {
           String key = (String)keys.nextElement();
           String value = properties.getProperty(key);
           out.println("      <entry key=\"" + key + "\">" + value + "</entry>");
        }
        out.println("    </properties>");
      }

      out.println("  </dataPanel>");
    }
    
    out.print("</rdv>");
    out.close();
  }
  
  /**
   * Inspect if the given DataPanel is detached from the DataPanelContainer
   * @param dataPanel to check
   * @param properties properties for the DataPanel
   * @return flag indicating detached
   */
  private static boolean isPanelDetached(DataPanel dataPanel, Properties properties) {
    if (properties.size() == 0) {
      return false;
    }
    
    String key, value;
    for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements() ;) {
      key = (String)keys.nextElement();
      if (key == "attached") {
        value = properties.getProperty(key);
        if (value == "false")
          return true;          
      }
    }
    
    return false;
  }

  /**
   * Load the configuration file from the specified URL and configure the
   * application. This spawns a new thread to do this in the background.
   * 
   * @param configURL  the URL of the file to load the configuration from
   */
  public static void loadConfiguration(final URL configURL) {
    new Thread() {
      public void run() {
        loadConfigurationWorker(configURL);
      }
    }.start();
  }

  /**
   * Load the configuration file from the specified URL and configure the
   * application.
   * 
   * @param configURL  the URL of the file to load the configuration from
   */
  private static void loadConfigurationWorker(URL configURL) {
    if (configURL == null) {
      DataViewer.alertError("The configuration file does not exist.");
      return;
    }
    
    RBNBController rbnb = RBNBController.getInstance();
    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
    DataPanelManager dataPanelManager = DataPanelManager.getInstance();
    
    if (rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
      rbnb.pause();
    }
    dataPanelManager.closeAllDataPanels();
    
    Document document;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = documentBuilder.parse(configURL.openStream());
    } catch (FileNotFoundException e) {
      DataViewer.alertError("The configuration file does not exist." +
          System.getProperty("line.separator") +
          configURL);
      return;
    } catch (IOException e) {
      DataViewer.alertError("Error loading configuration file.");
      return;
    } catch (Exception e) {
      DataViewer.alertError("The configuration file is corrupt.");
      return;
    }

    XPath xp = XPathFactory.newInstance().newXPath();
    
    try {
      Node rbnbNode= (Node)xp.evaluate("/rdv/rbnb[1]", document, XPathConstants.NODE);
      NodeList rbnbNodes = rbnbNode.getChildNodes();
      
      String host = findChildNodeText(rbnbNodes, "host");
      int port = Integer.parseInt(findChildNodeText(rbnbNodes, "port"));
      if (!rbnb.getRBNBHostName().equals(host) || rbnb.getRBNBPortNumber() != port) {
        rbnb.disconnect();
        
        int RETRY_LIMIT = 240;
        int tries = 0;
        while (tries++ < RETRY_LIMIT && rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
          try { Thread.sleep(250);  } catch (InterruptedException e) {}
        }
        
        if (tries >= RETRY_LIMIT) {
          return;
        }
        
        rbnb.setRBNBHostName(host);
        rbnb.setRBNBPortNumber(port);
      } else {
        localChannelManager.removeAllChannels();
      }
      
      double timeScale = Double.parseDouble(findChildNodeText(rbnbNodes, "timeScale"));
      rbnb.setTimeScale(timeScale);
      
      double playbackRate = Double.parseDouble(findChildNodeText(rbnbNodes, "playbackRate"));
      rbnb.setPlaybackRate(playbackRate);
      
      int state = RBNBController.getState(findChildNodeText(rbnbNodes, "state"));
      if (state != RBNBController.STATE_DISCONNECTED) {
        if (!rbnb.connect(true)) {
          return;
        }
      }
      
      NodeList localChannelNodes = (NodeList)xp.evaluate("/rdv/localChannel", document, XPathConstants.NODESET);
      for (int i=0; i<localChannelNodes.getLength(); i++) {
        Node localChannelNode = localChannelNodes.item(i);
        String name = localChannelNode.getAttributes().getNamedItem("name").getNodeValue();
        Node unitNode = localChannelNode.getAttributes().getNamedItem("unit");
        String unit = (unitNode != null) ? unitNode.getNodeValue() : null;
        String formula = localChannelNode.getAttributes().getNamedItem("formula").getNodeValue();
        
        Map<String,String> variables = new HashMap<String,String>();
        
        NodeList variableNodes = (NodeList)xp.evaluate("variable", localChannelNode, XPathConstants.NODESET);
        for (int j=0; j<variableNodes.getLength(); j++) {
          Node variableNode = variableNodes.item(j);
          String variableName = variableNode.getAttributes().getNamedItem("name").getNodeValue();
          String variableChannel = variableNode.getAttributes().getNamedItem("channel").getNodeValue();
          variables.put(variableName, variableChannel);
        }
        
        try {
          LocalChannel localChannel = new LocalChannel(name, unit, variables, formula);
          LocalChannelManager.getInstance().addChannel(localChannel);
        } catch (Exception e) {
          log.error("Failed to add local channel: " + name + ".");
        }
      }

      NodeList dataPanelNodes = (NodeList)xp.evaluate("/rdv/dataPanel", document, XPathConstants.NODESET);
      for (int i=0; i<dataPanelNodes.getLength(); i++) {
        Node dataPanelNode = dataPanelNodes.item(i);
        String id = dataPanelNode.getAttributes().getNamedItem("id").getNodeValue();
        DataPanel dataPanel;
        try {
          dataPanel = dataPanelManager.createDataPanel(id);
        } catch (Exception e) {
          continue;
        }

        NodeList entryNodes = (NodeList)xp.evaluate("properties/entry", dataPanelNode, XPathConstants.NODESET);
        for (int j=0; j<entryNodes.getLength(); j++) {
          String key = entryNodes.item(j).getAttributes().getNamedItem("key").getNodeValue();
          String value = entryNodes.item(j).getTextContent();
          dataPanel.setProperty(key, value);
        }        

        NodeList channelNodes = (NodeList)xp.evaluate("channels/channel", dataPanelNode, XPathConstants.NODESET);
        for (int j=0; j<channelNodes.getLength(); j++) {
          String channel = channelNodes.item(j).getTextContent();
          boolean added;
          if (dataPanel.supportsMultipleChannels()) {
            added = dataPanel.addChannel(channel);
          } else {
            added = dataPanel.setChannel(channel);
          }
          
          if (!added) {
            log.error("Failed to add channel " + channel + ".");
          }
        }        
      }
      
      rbnb.setState(state);
    } catch (XPathExpressionException e) {
      e.printStackTrace();
    }    
  }
  
  private static String findChildNodeText(NodeList nodes, String nodeName) {
    for (int i=0; i<nodes.getLength(); i++) {
      Node node = (Node)nodes.item(i);
      if (node.getNodeName().equals(nodeName)) {
        return node.getTextContent();
      }
    }
    return null;
  }
}
