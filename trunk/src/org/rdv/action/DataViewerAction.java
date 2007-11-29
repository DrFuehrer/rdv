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

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.rdv.DataViewer;

/**
 * An class for UI actions.
 * 
 * @author Jason P. Hanley
 */
public class DataViewerAction extends AbstractAction {
	boolean selected = false;
  
	public DataViewerAction(String text) {
		this(text, null, -1, null, null);
	}

	public DataViewerAction(String text, String desc) {
		this(text, desc, -1, null, null);
	}
	
	public DataViewerAction(String text, int mnemonic) {
		this(text, null, mnemonic, null, null);
	}
	
	public DataViewerAction(String text, String desc, int mnemonic) {
		this(text, desc, mnemonic, null, null);
	}
      
  public DataViewerAction(String text, String desc, int mnemonic, String iconFileName) {
    this(text, desc, mnemonic, null, iconFileName);
  }      
	
	public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator) {
		this(text, desc, mnemonic, accelerator, null);
	}

  public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator, String iconFileName) {
    super(text);
    putValue(SHORT_DESCRIPTION, desc);
    putValue(MNEMONIC_KEY, new Integer(mnemonic));
    putValue(ACCELERATOR_KEY, accelerator);
    putValue(SMALL_ICON, DataViewer.getIcon(iconFileName));
  }
    
	public void actionPerformed(ActionEvent ae) {}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {     
    if (this.selected == selected) {
      return;
    }
    
    this.selected = selected;
    firePropertyChange("selected", null, Boolean.valueOf(selected));
	}
}