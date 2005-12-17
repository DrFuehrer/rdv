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
import java.util.Iterator;
import java.util.List;

import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * A class to manage the application configuration.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class ConfigurationManager {
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
    out.println("    <location>" + rbnb.getLocation() + "</location>");
    out.println("    <timeScale>" + rbnb.getTimeScale() + "</timeScale>");
    out.println("    <playbackRate>" + rbnb.getPlaybackRate() + "</playbackRate>");
    out.println("  </rbnb>");
    
    List dataPanels = dataViewer.getDataPanelManager().getDataPanels();
    Iterator it = dataPanels.iterator();
    while (it.hasNext()) {
      DataPanel dataPanel = (DataPanel)it.next();
      out.println("  <dataPanel id=\"" + dataPanel.getClass().getName() + "\">");
      out.println("    <channels>");
      Iterator channels = dataPanel.subscribedChannels().iterator();
      while (channels.hasNext()) {
        String channel = (String)channels.next();
        out.println("      <channel>" + channel + "</channel>");
      }
      out.println("    </channels>");
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
  public void loadConfiguration(File configFile) {
    
  }
}
