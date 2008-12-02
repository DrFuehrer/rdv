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

import org.jdesktop.application.Task;
import org.rdv.RDV;
import org.rdv.data.JPEGDirectoryReader;
import org.rdv.data.JPEGFileCollectionReader;
import org.rdv.data.JPEGFileDataSample;
import org.rdv.data.JPEGZipFileReader;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBSource;

/**
 * A class to import a collection of JPEG images into an Data Turbine server.
 * 
 * @author Jason P. Hanley
 * @see    JPEGFileCollectionReader
 * @see    RBNBSource
 */
public class JPEGImportTask extends Task<Void, Void> {
  
  /** the zip file or directory containing the collection */
  private final File file;

  /**
   * Creates an instance of {@code JPEGImportTask}.
   * 
   * @param file  the zip file or directory to import JPEG's from
   */
  public JPEGImportTask(File file) {
    super(RDV.getInstance());
    
    this.file = file;
    
    String title = getResourceMap().getString("title", file.getName());
    setTitle(title);
  }

  /**
   * Imports JPEG's from the file. If the file is a directory it will import all
   * the JPEG's in the directory (recursively). If the file is a file, it will
   * be treated as a zip file and all the JPEG entries will be imported.
   * 
   * The JPEG's will be import into the current Data Turbine server connection.
   */
  @Override
  protected Void doInBackground() throws Exception {
    // get the correct reader
    JPEGFileCollectionReader reader;
    if (file.isDirectory()) {
      reader = new JPEGDirectoryReader(file);
    } else {
      reader = new JPEGZipFileReader(file);
    }

    int samples = reader.getSize();
    
    // if there are no JPEG files, throw an exception
    if (samples == 0) {
      throw new EmptyCollectionException();
    }
    
    String name = reader.getName();
    
    // create the Data Turbine source
    RBNBController rbnb = RBNBController.getInstance();
    RBNBSource source = new RBNBSource(name, samples,
        rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());
    
    // setup the channel and its metadata
    String channel = "video.jpg";
    String mime = "image/jpeg";
    source.addChannel(channel, mime);
    
    int currentSample = 0;
    
    // iterate through each sample and upload it to the server
    JPEGFileDataSample sample;
    while ((sample = reader.readSample()) != null) {
      String fileName = sample.getName();
      message("loadingMessage", fileName);
      
      source.putData(channel, sample.getTimestamp(), sample.getData());
      source.flush();
      
      currentSample++;
      setProgress(currentSample, 0, samples);
    }
    
    // close the Data Turbine connection
    source.close();
    
    // update the metadata to detect the new channel
    rbnb.updateMetadata();
    
    return null;
  }

  
  /**
   * Called when the task is interrupted. Just calls
   * {@link #failed(Throwable) failed}.
   */
  @Override
  protected void interrupted(InterruptedException e) {
    failed(e);
  }

  /**
   * Called when the task failed. This sets the title and message to their error
   * strings.
   */
  @Override
  protected void failed(Throwable cause) {
    setTitle(getResourceMap().getString("errorTitle"));

    if (cause instanceof EmptyCollectionException) {
      message("emptyCollectionMessage");        
    } else {
      message("errorMessage");
    }
  }

  /**
   * Called when the task succeeds. This sets the title and message to their
   * succeed strings.
   */
  @Override
  protected void succeeded(Void result) {
    setTitle(getResourceMap().getString("successTitle"));
    message("successMessage");
  }

  
  /**
   * This exception is thrown when a JPEG file collection is empty.
   * 
   * @author Jason P. Hanley
   */
  public class EmptyCollectionException extends Exception {

    /** serialization version identifier */
    private static final long serialVersionUID = 4438167366518550065L;

    /**
     * Constructs a new instance of {@code EmptyCollectionException}.
     */
    public EmptyCollectionException() {
      super();
    }

  }
  
}