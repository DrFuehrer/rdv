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

package org.rdv.data;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rdv.util.JPEGFileFilter;

/**
 * A reader for a collection of timestamped JPEG files stored in a directory.
 * 
 * @author Jason P. Hanley
 */
public class JPEGDirectoryReader extends JPEGFileCollectionReader {
  
  /** the name of the reader */
  private final String name;
  
  /** the JPEG files to read from */
  private List<File> files;

  /** the current index for the reader */
  private int index;
  
  /**
   * Creates a new reader for the collection of JPEG files in the specified
   * directory.
   * 
   * @param directory               the directory to look for JPEG files
   * @throws FileNotFoundException  if the directory doesn't exist
   */
  public JPEGDirectoryReader(File directory) throws FileNotFoundException {
    if (!directory.exists()) {
      throw new FileNotFoundException(directory.toString());
    }
    
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("The specified directory is not a directory: " + directory.toString());
    }
    
    name = directory.getName();
    
    files = scanDirectory(directory);
  }
  
  @Override
  /**
   * Gets the name of the reader.
   * 
   * @param name  the name of the reader
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get the number of JPEG files found in the directory.
   * 
   * @return  the number of JPEG files
   */
  @Override
  public int getSize() {
    return files.size();
  }
  
  /**
   * Reads the next JPEG file sample. Successive calls to this will iterate
   * through the JPEG files in the specified directory. When there are no more
   * JPEG files, this will return null.
   * 
   * @return                 a JPEG data sample, or null if there are no more
   * @throws ParseException  if current JPEG file timestamp can't be parsed
   * @throws IOException     if the current JPEG file can't be read
   */
  @Override
  public JPEGFileDataSample readSample() throws ParseException, IOException {
    if (files.size() == index) {
      return null;
    }
    
    File file = files.get(index++);
    
    String name = file.getName();
    double timestamp = getTimestamp(file.getName());
    byte[] data = getData(file);
    
    JPEGFileDataSample sample = new JPEGFileDataSample(name, timestamp, data);
    return sample;
  }
  
  /**
   * Get a list of JPEG files in the given directory. A file is detected as a
   * JPEG file it's extension is jpg or jpeg.
   * 
   * @param directory  the directory to scan
   * @return           a list of JPEG files found.
   */
  private static List<File> scanDirectory(File directory) {
    List<File> files = new ArrayList<File>();
    
    FileFilter filter = new JPEGFileFilter();
    File[] fileListing = directory.listFiles(filter);
    
    for (File file : fileListing) {
      if (!file.isDirectory()) {
        files.add(file);
      } else {
        files.addAll(scanDirectory(file));
      }
    }
    
    Collections.sort(files);
    
    return files;
  }
  
  /**
   * Get the contents of the file.
   * 
   * @param file          the file to read
   * @return              the contents of the file as a byte array
   * @throws IOException  if there is an error reading the file
   */
  private byte[] getData(File file) throws IOException {
    if (!file.isFile()) {
      throw new IllegalArgumentException("The file must exist and be a file.");
    }
    
    int fileSize = (int)file.length();
    byte[] fileData = new byte[fileSize];
    
    FileInputStream fin = new FileInputStream(file);
    fin.read(fileData);
    fin.close();
    
    return fileData;
  }

}