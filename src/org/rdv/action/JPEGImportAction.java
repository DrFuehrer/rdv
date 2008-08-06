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

package org.rdv.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JOptionPane;

import org.rdv.data.JPEGDirectoryReader;
import org.rdv.data.JPEGFileCollectionReader;
import org.rdv.data.JPEGFileDataSample;
import org.rdv.data.JPEGZipFileReader;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBException;
import org.rdv.rbnb.RBNBSource;
import org.rdv.ui.ProgressWindow;
import org.rdv.ui.UIUtilities;
import org.rdv.util.ZipFileFilter;

/**
 * A class to import a collection of JPEG images into an Data Turbine server. 
 * 
 * @author  Jason P. Hanley
 * @author  Moji Soltani
 */
public class JPEGImportAction extends DataViewerAction {

  /** serialization version identifier */
  private static final long serialVersionUID = -278036295590043978L;

  /** the window to show the progress of the import */
  private ProgressWindow progressWindow;
  
  public JPEGImportAction() {
    super("Import JPEG files",
          "Import a folder that contains JPEG files");
  }
  
  /**
   * Prompts the user for the directory to import the JPEG images from and
   * upload them to the RBNB server.
   */
  public void actionPerformed(ActionEvent ae) {
    File file = UIUtilities.getFileOrDirectory(new ZipFileFilter(),
        "Import");
    
    if (file == null) {
      return;
    }
    
    importJPEGs(file);
  }
  
  /**
   * Imports JPEG's from the file. If the file is a directory it will import all
   * the JPEG's in the directory (recursively). If the file is a file, it will
   * be treated as a zip file and all the JPEG entries will be imported.
   * 
   * @param file  the directory or zip file
   */
  public void importJPEGs(final File file) {
    new Thread() {
      public void run() {
        boolean error = false;
        
        try {
          JPEGFileCollectionReader reader;
          if (file.isDirectory()) {
            reader = new JPEGDirectoryReader(file);
          } else {
            reader = new JPEGZipFileReader(file);
          }
          
          if (reader.getSize() == 0) {
            JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
                "No JPEG files were found in the specified file or directory.",
                "Import failed",
                JOptionPane.ERROR_MESSAGE);

            return;
          }
          
          progressWindow = new ProgressWindow(UIUtilities.getMainFrame(),
              "Importing JPEG's from " + reader.getName());
          progressWindow.setVisible(true);
          
          importJPEGs(reader);
        } catch (Exception e) {
          error = true;
          e.printStackTrace();
        }
        
        progressWindow.dispose();
        
        RBNBController.getInstance().updateMetadata();

        if (!error) {
          JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "Import complete.",
              "Import complete",
              JOptionPane.INFORMATION_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "There was an error importing the JPEG files.",
              "Import failed",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }.start();
  }

  /**
   * Upload the JPEG files in the given directory to the RBNB server.
   * 
   * @param directory               the directory to get the files from
   * @throws FileNotFoundException  if the directory doesn't exist
   * @throws ParseException         if there is an error parsing the data files
   * @throws IOException            if there is an error reading the data files
   * @throws RBNBException          if there is an error communicating with the
   *                                server
   */
  private void importJPEGs(JPEGFileCollectionReader reader) throws FileNotFoundException, ParseException, IOException, RBNBException {   
    int samples = reader.getSize();
    
    String name = reader.getName();
    RBNBController rbnb = RBNBController.getInstance();
    RBNBSource source = new RBNBSource(name, samples,
        rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());
    
    String channel = "video.jpg";
    String mime = "image/jpeg";
    source.addChannel(channel, mime);
    
    int currentSample = 0;
    
    JPEGFileDataSample sample;
    while ((sample = reader.readSample()) != null) {
      String fileName = sample.getName();
      progressWindow.setStatus("Importing JPEG file " + fileName);
      
      source.putData(channel, sample.getTimestamp(), sample.getData());
      source.flush();
      
      currentSample++;
      progressWindow.setProgress((float)currentSample/samples);
    }
    
    source.close();
  }
  
}