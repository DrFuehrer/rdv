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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.nees.buffalo.rdv.data.JPEGFileDataSample;
import org.nees.buffalo.rdv.data.JPEGFileCollectionReader;
import org.nees.buffalo.rdv.rbnb.RBNBException;
import org.nees.buffalo.rdv.rbnb.RBNBSource;
import org.nees.buffalo.rdv.ui.ProgressWindow;

/**
 * A class to import a collection of JPEG images into an RBNB server. 
 * 
 * @author Jason P. Hanley
 */
public class JPEGImportAction extends DataViewerAction {
  public JPEGImportAction() {
    super("Import JPEG files",
          "Import a folder that contains JPEG files");
  }
  
  /**
   * Prompts the user for the directory to import the JPEG images from and
   * upload them to the RBNB server.
   */
  public void actionPerformed(ActionEvent ae) {
    final File directory = getDirectory();
    
    if (directory == null) {
      return;
    }
    
    if (!directory.exists()) {
      return;
    }
    
    if (!directory.isDirectory()) {
      return;
    }
    
    new Thread() {
      public void run() {        
        try {
          importDirectory(directory);
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (RBNBException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        JOptionPane.showMessageDialog(null,
            "Import complete.",
            "Import complete",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }.start();        
  }
  
  /**
   * Prompt the user for the directory to import the JPEG files from.
   * 
   * @return  the directory the user choose, or null if they didn't choose one
   */
  private File getDirectory() {
    JFileChooser directoryChooser = new JFileChooser();
    directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    int returnVal = directoryChooser.showDialog(null, "Import");
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return directoryChooser.getSelectedFile();
    } else {
      return null;
    }
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
  private void importDirectory(File directory) throws FileNotFoundException, ParseException, IOException, RBNBException {
    ProgressWindow progressWindow = new ProgressWindow("Importing data...");
    progressWindow.setVisible(true);
    
    JPEGFileCollectionReader reader = new JPEGFileCollectionReader(directory);
    
    String name = directory.getName();
    RBNBSource source = new RBNBSource(name);
    
    String channel = "video.jpg";
    String mime = "image/jpeg";
    source.addChannel(channel, mime);
    
    int samples = reader.getFileCount();
    source.setArchiveSize(samples);
    
    int currentSample = 0;
    
    JPEGFileDataSample sample;
    while ((sample = reader.readSample()) != null) {
      String fileName = sample.getFile().getName();
      progressWindow.setStatus("Importing JPEG file " + fileName);
      
      source.putData(channel, sample.getTimestamp(), sample.getData());
      source.flush();
      
      currentSample++;
      progressWindow.setProgress((float)currentSample/samples);
    }
    
    source.close();
    
    progressWindow.setVisible(false);
  }
}