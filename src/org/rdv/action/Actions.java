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

package org.rdv.action;

import java.io.File;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.rdv.ui.AddLocalChannelDialog;
import org.rdv.ui.UIUtilities;
import org.rdv.util.ZipFileFilter;

/**
 * A collection of {@link Action Actions}.
 * 
 * @author Jason P. Hanley
 */
public class Actions {

  /** the instance of this class */
  private static Actions instance;
  
  /**
   * Creates a new instance of {@code Actions}.
   */
  protected Actions() {
    super();
  }

  /**
   * Gets the instance of {@code Actions}.
   * 
   * @return  the {@code Actions} instance
   */
  public static Actions getInstance() {
    if (instance == null) {
      instance = new Actions();
    }
    
    return instance;
  }
  
  /**
   * Shows the add local channel dialog.
   */
  @Action
  public void addLocalChannel() {
    new AddLocalChannelDialog(UIUtilities.getMainFrame());
  }
  
  /**
   * Prompts the user for a file or directory to import JPEGs from and
   * upload them to the current Data Turbine server.
   * 
   * @see  JPEGImportTask
   */
  @Action
  public Task<Void, Void> importJPEGs() {
    File file = UIUtilities.getFileOrDirectory(new ZipFileFilter(),
        "Import");
    
    if (file == null) {
      return null;
    }
    
    Task<Void, Void> task = new JPEGImportTask(file);
    
    new ProgressWindowController(task);

    return task;
  }

}