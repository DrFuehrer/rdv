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

package org.rdv.ui;

import javax.swing.JOptionPane;

/**
 * A class to show popup messages in the UI.
 * 
 * @author Jason P. Hanley
 */
public class MessagePopup {
  /** the single instance of this class */
  protected static MessagePopup instance;
  
  protected MessagePopup() {
    super();
  }
  
  /**
   * Get the single instance of this class.
   * 
   * @return  the instance of this class
   */
  public static MessagePopup getInstance() {
    if (instance == null) {
      instance = new MessagePopup();
    }
    
    return instance;
  }
  
  /**
   * Show an error in a popup window.
   * 
   * @param error  the error message
   */
  public void showError(String error) {
    JOptionPane.showMessageDialog(
        null,
        error,
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }
}