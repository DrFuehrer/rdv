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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.rdv.action.DataViewerAction;

/**
 * An extensions of JCheckBoxMenuItem to handle the "selected" property of the
 * menu's action.
 * 
 * @author Jason P. Hanley
 */
public class DataViewerCheckBoxMenuItem extends JCheckBoxMenuItem {

  /** serialization version identifier */
  private static final long serialVersionUID = 1237273770893644022L;

  /**
   * Creates the check box menu item from the given action.
   * 
   * @param action  the action for this menu item
   */
  public DataViewerCheckBoxMenuItem(DataViewerAction action) {
    super(action);
    
    setSelected(action.isSelected());
  }

  /**
   * Creates the property change listener for the action. This wraps the
   * property change listener from the super class and additionally handles
   * the "selected" property.
   * 
   * @param a  the action to listen for property changes
   */
  protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
    final PropertyChangeListener originalListener = super.createActionPropertyChangeListener(a);
    
    PropertyChangeListener wrapperListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        originalListener.propertyChange(pce);
        
        if (pce.getPropertyName().equals("selected")) {
          Boolean selected = (Boolean)pce.getNewValue();
          setSelected(selected);
        }
      }
    };
    
    return wrapperListener;
  }  
}