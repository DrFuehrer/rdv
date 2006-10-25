/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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

package org.nees.buffalo.rdv.rbnb;

import com.rbnb.api.Server;

/**
 * A singleton class to manage a local RBNB server.
 * 
 * @author Jason P. Hanley
 */
public class LocalServer {
  /** the instance of the class */
  protected static LocalServer instance;
  
  /** the RBNB server */
  private Server server;
  
  /**
   * The default constructor for this singleton class. Declared so constructor
   * is protected.
   */
  protected LocalServer() {
    super();
  }
  
  /**
   * Get the single instance of this class.
   * 
   * @return  the instance of the LocalServer
   */
  public static LocalServer getInstance() {
    if (instance == null) {
      instance = new LocalServer();
    }
    
    return instance;
  }
  
  /**
   * Starts the local RBNB server. If one is already started, this does nothing.
   * 
   * @throws Exception  if there is an error starting the server
   */
  public void startServer() throws Exception {
    if (isServerRunning()) {
      return;
    }
    
    String[] args = {};
    server = Server.launchNewServer(args);
  }
  
  /**
   * Stops the local RBNB server. If no server is runnung, this does nothing.
   * 
   * @throws Exception  if there is an error stopping the server
   */
  public void stopServer() throws Exception {
    if (!isServerRunning()) {
      return;
    }
    
    server.stop();
    server = null;
  }
  
  /**
   * See if the local RBNB server is running.
   * 
   * @return  true if the server is running, false otherwise
   */
  public boolean isServerRunning() {
    return server != null && server.isRunning();
  }
}