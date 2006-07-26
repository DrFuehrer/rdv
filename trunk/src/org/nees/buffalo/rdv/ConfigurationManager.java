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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.ControlPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
  
  
  DataViewer dataViewer;
  
  public ConfigurationManager(DataViewer dataViewer) {
    this.dataViewer = dataViewer;
  }

  /**
   * Save the application configuration to the specified file.
   * 
   * @param configFile  the file to save the configuration to
   * @since             1.3
   */
  public void saveConfiguration(File configFile) {
    PrintWriter out;
    try {
      out = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
    } catch (IOException e) {
      return;
    }
    
    out.println("<?xml version=\"1.0\"?>");
    out.println("<rdv>");
    
    RBNBController rbnb = dataViewer.getRBNBController();
    out.println("  <rbnb>");
    out.println("    <host>" + rbnb.getRBNBHostName() + "</host>");
    out.println("    <port>" + rbnb.getRBNBPortNumber() + "</port>");
    out.println("    <state>" + RBNBController.getStateName(rbnb.getState()) + "</state>");
    out.println("    <timeScale>" + rbnb.getTimeScale() + "</timeScale>");
    out.println("    <playbackRate>" + rbnb.getPlaybackRate() + "</playbackRate>");
    out.println("  </rbnb>");
    
    List dataPanels = dataViewer.getDataPanelManager().getDataPanels();
    Iterator it = dataPanels.iterator();
    while (it.hasNext()) {
      DataPanel dataPanel = (DataPanel)it.next();
      out.println("  <dataPanel id=\"" + dataPanel.getClass().getName() + "\">");
      
      if (dataPanel.subscribedChannelCount() > 0) {
        out.println("    <channels>");
        Iterator channels = dataPanel.subscribedChannels().iterator();
        while (channels.hasNext()) {
          String channel = (String)channels.next();
          out.println("      <channel>" + channel + "</channel>");
        }     
        out.println("    </channels>");
      }

      Properties properties = dataPanel.getProperties();
      if (properties.size() > 0) {
        out.println("    <properties>");
        for (Enumeration keys = properties.propertyNames(); keys.hasMoreElements() ;) {
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
   * Load the configuration from the specified file and configure the
   * application.
   * 
   * @param configFile  the file to load the configuration from
   * @since             1.3
   */
  public void loadConfiguration(final File configFile) {
    new Thread() {
      public void run() {
        loadConfigurationWorker(configFile);
      }
    }.start();
  }
  
  private void loadConfigurationWorker(File configFile) {
    if (configFile == null) {
      dataViewer.alertError("The configuration file does not exist.");
      return;
    } else if (!configFile.exists()) {
      dataViewer.alertError("The configuration file \"" + configFile.getName() + "\" does not exist.");
      return;
    } else if (!configFile.isFile()) {
      dataViewer.alertError("\"" + configFile.getName() + "\" is not a file.");
      return;
    }
    
    RBNBController rbnb = dataViewer.getRBNBController();
    DataPanelManager dataPanelManager = dataViewer.getDataPanelManager();
    ControlPanel controlPanel = dataViewer.getApplicationFrame().getControlPanel();
    
    if (rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
      rbnb.pause();
    }
    dataPanelManager.closeAllDataPanels();
    
    Document document;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = documentBuilder.parse(configFile);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return;
    } catch (SAXException e) {
      e.printStackTrace();
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
      }
      
      double timeScale = Double.parseDouble(findChildNodeText(rbnbNodes, "timeScale"));
      controlPanel.setTimeScale(timeScale);
      rbnb.setTimeScale(timeScale);
      
      double playbackRate = Double.parseDouble(findChildNodeText(rbnbNodes, "playbackRate"));
      rbnb.setPlaybackRate(playbackRate);
      
      int state = RBNBController.getState(findChildNodeText(rbnbNodes, "state"));
      if (state != RBNBController.STATE_DISCONNECTED) {
        if (!rbnb.connect(true)) {
          return;
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
