/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.rdv.auth.AuthenticationManager;
import org.rdv.ui.CentralImportDialog;
import org.rdv.ui.LoginDialog;

/**
 * An action to import data from NEEScentral into RDV.
 * 
 * @author Jason P. Hanley
 *
 */
public class CentralImportAction extends DataViewerAction {

  /** serialization version identifier */
  private static final long serialVersionUID = 8163590098771042420L;

  /**
   * Creates the central import action.
   */
  public CentralImportAction() {
    super("Import data from NEEScentral");
  }
  
  /**
   * Displays the central import dialog. If the user is not logged in, it asks
   * them if they would like to.
   */
  public void actionPerformed(ActionEvent ae) {
    if (AuthenticationManager.getInstance().getAuthentication() == null) {
      int ret = JOptionPane.showConfirmDialog(null,
        "To access protected files, you must be logged in to NEEScentral.\n" +
        "Do you want to login now?",
        "Login to NEEScentral?",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
      if (ret == JOptionPane.YES_OPTION) {
        new LoginDialog(null);
      }
    }
    
    new CentralImportDialog();
  }
}