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

import java.io.File;

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
  
  /** the archive directory */
  private File archiveDirectory;
  
  /**
   * The default constructor for this singleton class. Declared so constructor
   * is protected.
   */
  protected LocalServer() {
    super();
    
    int i=0;
    do {
      i++;
      archiveDirectory = new File(System.getProperty("java.io.tmpdir"), "RDV_archive-" + i);
    } while (archiveDirectory.exists());
    
    archiveDirectory.deleteOnExit();
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
    
    if (!archiveDirectory.exists()) {
      archiveDirectory.mkdir();
    }
    
    // lock down access to localhost only, set the archive directory, and turn
    // off logging and metrics
    String[] args = { "-L",
                      "-H", archiveDirectory.getCanonicalPath(),
                      "-l", "OFF",
                      "-m", "OFF"};
    
    // start the server
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
    
    removeDirectory(archiveDirectory);
  }
  
  /**
   * See if the local RBNB server is running.
   * 
   * @return  true if the server is running, false otherwise
   */
  public boolean isServerRunning() {
    return server != null && server.isRunning();
  }
  
  /**
   * Remove the directory. If the directory contains any files or directories
   * they will be removed also.
   * 
   * @param directory  the directory to be removed
   */
  private static void removeDirectory(File directory) {
    for (File file: directory.listFiles()) {
      if (file.isDirectory()) {
        removeDirectory(file);
      } else {
        file.delete();
      }
    }
    
    directory.delete();
  }
}