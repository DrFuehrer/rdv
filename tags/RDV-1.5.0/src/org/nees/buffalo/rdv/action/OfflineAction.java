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

package org.nees.buffalo.rdv.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.nees.buffalo.rdv.rbnb.LocalServer;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.MessagePopup;

/**
 * Action to control offline mode.
 * 
 * @author Jason P. Hanley
 */
public class OfflineAction extends DataViewerAction {
  public OfflineAction() {
    super(
        "Work offline",
        "View data locally",
        KeyEvent.VK_W);
  }
  
  /**
   * Respond to an event for this action. This will start or stop the local RBNB
   * server.
   */
  public void actionPerformed(ActionEvent ae) {
    if (isSelected()) {
      stopOffline();
    } else {
      goOffline();
    }
  }
  
  /**
   * Start the local server and connect to it.
   */
  public void goOffline() {
    RBNBController rbnb = RBNBController.getInstance();
    LocalServer server = LocalServer.getInstance();
    
    rbnb.disconnect(true);
    
    try {
      server.startServer();
    } catch (Exception e) {
      e.printStackTrace();
      MessagePopup.getInstance().showError("Failed to start local data server for offline usage.");
      return;
    }
    
    rbnb.setRBNBHostName("localhost");
    rbnb.setRBNBPortNumber(server.getPort());
    rbnb.connect();    

    setSelected(true);
  }
  
  /**
   * Disconnect from the local server.
   */
  public void stopOffline() {
    RBNBController rbnb = RBNBController.getInstance();
    rbnb.disconnect(true);
    
    setSelected(false);
  }
}
