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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.nees.buffalo.rdv.ui.ImportDialog;

/**
 * An action to import a data file.
 * 
 * @author Jason P. Hanley
 *
 */
public class DataImportAction extends DataViewerAction {
  public DataImportAction() {
    super("Import data file",
          "Import local data to RBNB server");
  }
  
  /**
   * Prompts the user for the data file and uploads it to the RBNB server.
   */
  public void actionPerformed(ActionEvent ae) {
    File dataFile = getFile();
    
    if (dataFile == null) {
      return;
    }
    
    if (!dataFile.exists()) {
      return;
    }
    
    if (!dataFile.isFile()) {
      return;
    }
    
    String sourceName = getDefaultSourceName(dataFile);
    
    new ImportDialog(dataFile, sourceName);
  }
  
  /**
   * Prompts the user for the file to import data from.
   * 
   * @return  the data file, or null if none is selected
   */
  private File getFile() {
    JFileChooser fileChooser = new JFileChooser();
    
    int returnVal = fileChooser.showOpenDialog(null);
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }
  
  /**
   * Gets the default name of the source for the given the file. This will be
   * the name of the file without the extension.
   * 
   * @param dataFile  the data file
   * @return          the source name
   */
  private String getDefaultSourceName(File dataFile) {
    String sourceName = dataFile.getName();
    
    int dotIndex = sourceName.lastIndexOf('.');
    if (dotIndex != -1) {
      sourceName = sourceName.substring(0, dotIndex);
    }
    
    return sourceName;
  }
}