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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.rdv.RDV;

/**
 * UI utility methods.
 * 
 * @author Jason P. Hanley
 */

public class UIUtilities {
  
  /** the current directory from the last file chooser */
  private static File currentDirectory;

  /**
   * This class can not be instantiated and it's constructor always throws an
   * exception.
   */
  private UIUtilities() {
    throw new UnsupportedOperationException(
        "This class can not be instantiated.");
  }
  
  /**
   * Gets the current directory used by the last file chooser.
   * 
   * @return  the current directory
   */
  public static File getCurrentDirectory() {
    return currentDirectory;
  }
  
  /**
   * Shows the open file dialog to let the user choose a file.
   * 
   * @param fileFilter  the file filter for the dialog
   * @return            the file the user choose, or null if they canceled out
   */
  public static File openFile(FileFilter fileFilter) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    
    return getFile(fileChooser, fileFilter, null);
  }

  /**
   * Shows the save file dialog to let the user choose a file.
   * 
   * @param fileFilter  the file filter for the dialog
   * @return            the file the user choose, or null if they canceled out
   */
  public static File saveFile(FileFilter fileFilter) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    
    return getFile(fileChooser, fileFilter, null);
  }
  
  /**
   * Shows a file dialog to let the user choose a file.
   * 
   * @param approveButtonText  the text for the "ok" button
   * @return                   the file the user choose, or null if they
   *                           canceled out
   */
  public static File getFile(String approveButtonText) {
    return getFile(null, approveButtonText);
  }
  
  /**
   * Shows a file dialog to let the user choose a file.
   * 
   * @param fileFilter         the file filter for the dialog
   * @param approveButtonText  the text for the "ok" button
   * @return                   the file the user choose, or null if they
   *                           canceled out
   */
  public static File getFile(FileFilter fileFilter, String approveButtonText) {
    return getFile(null, fileFilter, approveButtonText);
  }
  
  /**
   * Shows a file dialog to let the user choose a file or a directory.
   * 
   * @param fileFilter         the file filter for the dialog
   * @param approveButtonText  the text for the "ok" button
   * @return                   the file or directory the user choose, or null if
   *                           they canceled out
   */
  public static File getFileOrDirectory(FileFilter fileFilter, String approveButtonText) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    
    return getFile(fileChooser, fileFilter, approveButtonText);
  }
  
  /**
   * Shows a file dialog to let the user choose a file.
   * 
   * @param fileChooser        the file chooser component
   * @param fileFilter         the file filter for the dialog
   * @param approveButtonText  the text for the "ok" button
   * @return                   the file the user choose, or null if they
   *                           canceled out
   */
  private static File getFile(JFileChooser fileChooser, FileFilter fileFilter, String approveButtonText) {
    if (fileChooser == null) {
      fileChooser = new JFileChooser();
    }
    
    if (fileFilter != null) {
      fileChooser.setFileFilter(fileFilter);
    }
    
    if (currentDirectory != null) {
      fileChooser.setCurrentDirectory(currentDirectory);
    }
    
    int returnVal = fileChooser.showDialog(getMainFrame(), approveButtonText);
    
    currentDirectory = fileChooser.getCurrentDirectory();
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }
  
  /**
   * Gets the main frame for the application.
   * 
   * @return  the main frame
   */
  public static JFrame getMainFrame() {
    return RDV.getInstance(RDV.class).getMainFrame();
  }
  
}