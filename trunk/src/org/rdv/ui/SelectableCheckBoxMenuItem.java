/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
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

/**
 * A check box menu item that uses the "selected" property from it's action.
 * 
 * @author  Jason P. Hanley
 */
public class SelectableCheckBoxMenuItem extends JCheckBoxMenuItem {

  /** serialization version identifier */
  private static final long serialVersionUID = 831834301317733433L;

  /**
   * Create a check box menu item from the action.
   * 
   * @param a  the action for this menu item
   */
  public SelectableCheckBoxMenuItem(Action a) {
    super(a);
  }
  
  /**
   * Configure from the action properties. This looks for the selected
   * property.
   * 
   * @param a  the action
   */
  protected void configurePropertiesFromAction(Action a) {
    super.configurePropertiesFromAction(a);
    
    Boolean selected = (Boolean)a.getValue("selected");
    if (selected != null && selected != isSelected()) {
      setSelected(selected);
    }      
  }
  
  /**
   * Create an action  property change listener. This listens for the 
   * "selected" property.
   * 
   * @param a  the action to create the listener for
   */
  protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
    final PropertyChangeListener listener = super.createActionPropertyChangeListener(a);
    
    PropertyChangeListener myListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        listener.propertyChange(evt);
        
        if (evt.getPropertyName().equals("selected")) {
          Boolean selected = (Boolean)evt.getNewValue();
          if (selected == null) {
            setSelected(false);
          } else if (selected != isSelected()) {
            setSelected(selected);
          }
        }
      }
    };
    
    return myListener;
  }
}