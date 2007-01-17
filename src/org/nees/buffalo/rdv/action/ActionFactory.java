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

package org.nees.buffalo.rdv.action;

/**
 * A factory class to manage actions.
 * 
 * @author Jason P. Hanley
 */
public class ActionFactory {
  /** the instance of this class */
  private static ActionFactory instance;

  /** the action to control offline mode */
  private OfflineAction offlineAction;
  
  /**
   * Creates the action factory. 
   */
  protected ActionFactory() {
    super();
  }

  /**
   * Gets the instance of the action factory.
   * 
   * @return  the action factory
   */
  public static ActionFactory getInstance() {
    if (instance == null) {
      instance = new ActionFactory();
    }
    
    return instance;
  }
  
  /**
   * Gets the action to control offline mode.
   * 
   * @return  the offline action
   */
  public OfflineAction getOfflineAction() {
    if (offlineAction == null) {
      offlineAction = new OfflineAction();
    }
    
    return offlineAction;
  }
}
