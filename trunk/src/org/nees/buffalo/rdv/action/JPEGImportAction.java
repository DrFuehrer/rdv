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
import javax.swing.filechooser.FileFilter;

import org.nees.buffalo.rdv.data.JPEGFileDataSample;
import org.nees.buffalo.rdv.data.JPEGFileCollectionReader;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBException;
import org.nees.buffalo.rdv.rbnb.RBNBSource;
import org.nees.buffalo.rdv.ui.ProgressWindow;

import java.util.List;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
/**
 * A class to import a collection of JPEG images into an RBNB server. 
 * 
 * @author Jason P. Hanley
 */
public class JPEGImportAction extends DataViewerAction {
  /** the window to show the progress of the import */
  private ProgressWindow progressWindow;
  
  public JPEGImportAction() {
    super("Import JPEG files",
          "Import a folder that contains JPEG files");
  }
  
  /** Helper class to delete a temporary folder created on file system */
  private static DirectoryDeleter deleterThread;
  /** Thread to delete folder on Exit */
  static
  {
      deleterThread = new DirectoryDeleter();
      Runtime.getRuntime().addShutdownHook(deleterThread);
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
    
    startImportThread(directory);

  }
  
  private void startImportThread(final File directory) {

    new Thread() {
      public void run() {        
        try {
          progressWindow = new ProgressWindow("Importing directory " + directory.getName());
          progressWindow.setVisible(true);    
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
        
        progressWindow.dispose();
        
        RBNBController.getInstance().updateMetadata();
        
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
    directoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    

    directoryChooser.setFileFilter(new FileFilter() {
      public boolean accept(File f) {
        // accept only Zip files and Folders
        return (f.isDirectory() || f.getName().toLowerCase().endsWith(".zip"));
      }
      public String getDescription() {
        return "Directory or Zip file";
      }
    });
    
    int returnVal = directoryChooser.showDialog(null, "Import");
    
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      // check to see if a zip file selected
      if (!directoryChooser.getSelectedFile().isDirectory()) {
        try {
          File zipDirectory = createZipDirectory(directoryChooser.getSelectedFile());
          return zipDirectory;
          
        } catch (FileNotFoundException fe) {
          fe.printStackTrace();
          return null;
        } catch (IOException ie) {
          ie.printStackTrace();
          return null;
        }
      }
      
      return directoryChooser.getSelectedFile();
      
    } else {
      return null;
    }
  }
  
  /** Create a temporary folder to store zip extracted files
   * 
   * @param  zipFile  the zip file File source to extract files from
   * @return folder containing extracted files
   * @throws FileNotFoundException
   * @throws IOException
   */
  private File createZipDirectory(File zipFile) throws FileNotFoundException, IOException {

    String dirName = zipFile.getName().substring(0, zipFile.getName().indexOf("."));
    
    File zipDir = new File(System.getProperty("java.io.tmpdir"), dirName);
    zipDir.deleteOnExit();

    if (!zipDir.exists()) {
      zipDir.mkdir();
    }

    // create a zip input stream for extraction
    ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile));
      
    createZipDirectory(zipDir, zipStream);
    
    // helper to empty and delete the temporary folder
    deleterThread.add(zipDir);
    
    return zipDir;
  }
  
  /**
   * Create a temporary folder to store zip extracted files
   * 
   * @param  zipFile  the zip file URL source to extract files from
   * @return folder containing extracted files
   * @throws FileNotFoundException
   * @throws IOException
   */
  private File createZipDirectory(URL zipFile) throws FileNotFoundException, IOException {

    String dirName = getFileName(zipFile);
    File zipDir = new File(System.getProperty("java.io.tmpdir"), dirName);
    zipDir.deleteOnExit();

    if (!zipDir.exists()) {
      zipDir.mkdir();
    }

    InputStream inputStream = zipFile.openStream();
    // create a zip input stream for extraction
    ZipInputStream zipStream = new ZipInputStream(inputStream);
      
    createZipDirectory(zipDir, zipStream);
    
    // helper to empty and delete the temporary folder
    deleterThread.add(zipDir);
    
    return zipDir;
  }
  
  /**
   * Gets the name of the file from the URL.
   * 
   * @param file  the file URL
   * @return      the name of the file
   */
  private static String getFileName(URL file) {
    String fileName = file.getPath();

    // fix for annoying NEEScentral links
    if (fileName.endsWith("/content")) {
      fileName = fileName.substring(0, fileName.length()-8);
    }
    
    int lastPathIndex = fileName.lastIndexOf('/');
    if (fileName.length() > lastPathIndex+1) {
      fileName = fileName.substring(lastPathIndex+1);
    }
    
    // fix for possible non-ascii characters encoded in file name
    try {
      fileName = URLDecoder.decode(fileName, "UTF-8");
    } catch (UnsupportedEncodingException ue) {  }
    
    return fileName;    
  }


  /** Extract and store the zip files to the destination zip directory
   * 
   * @param zipDirectory directory to store extracted files
   * @param zipStream zip input stream for reading zip files
   */
  private void createZipDirectory(File zipDirectory, ZipInputStream zipStream) throws IOException {
    
   
    final int BUFFER = 2048;
    ZipEntry entry;
    BufferedOutputStream dest = null;
    String extractedName;
    
    while ((entry = zipStream.getNextEntry()) != null) {
      int count;
      byte data[] = new byte[BUFFER];
      
      extractedName = entry.getName();
      // get ride off the zip files' path to make them all in one level 
      if (extractedName.indexOf("\\") > 0) {
        extractedName = extractedName.substring((extractedName.lastIndexOf("\\")),  extractedName.length());        
      }
      // write the files to the disk
      OutputStream fos = new FileOutputStream(zipDirectory.getPath() + "/" + extractedName);
      dest = new BufferedOutputStream(fos, BUFFER);
      while ((count = zipStream.read(data, 0, BUFFER)) != -1) {
         dest.write(data, 0, count);
      }
      dest.flush();
      dest.close();
    }

    zipStream.close();
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
    JPEGFileCollectionReader reader = new JPEGFileCollectionReader(directory);
    int samples = reader.getFileCount();
    
    String name = directory.getName();
    RBNBController rbnb = RBNBController.getInstance();
    RBNBSource source = new RBNBSource(name, samples,
        rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());
    
    String channel = "video.jpg";
    String mime = "image/jpeg";
    source.addChannel(channel, mime);
    
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
  }
  
  /**
   * Imports URL zip files containing video images to RBNB
   *  
   * @param zipURLs list of URL zip files
   */
  public void importZipVideo(List<URL> zipURLs) {
    
    for (URL zipUrl : zipURLs) {
      try {
        final File zipDir = createZipDirectory(zipUrl);
        startImportThread(zipDir);        
      } catch (IOException ie) {
        ie.printStackTrace();
      }

    }
  }
  
}