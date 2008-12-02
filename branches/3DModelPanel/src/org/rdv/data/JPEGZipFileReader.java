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
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A reader for a collection of JPEG files stored in a zip file.
 * 
 * @author Jason P. Hanley
 */
public class JPEGZipFileReader extends JPEGFileCollectionReader {
  
  /** the name of the reader */
  private String name;
  
  /** the zip file */
  private final ZipFile zipFile;
  
  /** the number of JPEG files in the zip file */
  private int fileCount;
  
  /** an enumeration of the entries in the zip file */
  private Enumeration<? extends ZipEntry> entries;
  
  /**
   * Creates a reader for the collection of JPEG files in the zip file. Files
   * that don't end in .jpg or .jpeg will be ignored.
   * 
   * @param file          the zip file
   * @throws IOException  if there is an error reading the zip file
   */
  public JPEGZipFileReader(File file) throws IOException {
    // get a friendly name for this reader
    name = file.getName();
    if (name.toLowerCase().endsWith(".zip")) {
      name = name.substring(0, name.length()-4);
    }
    
    zipFile = new ZipFile(file);
    
    // go through all the entries and count the JPEG files
    entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (isJPEG(entry)) {
        fileCount++;
      }
    }
    
    entries = zipFile.entries();
  }
  
  /**
   * Gets the name of the reader.
   * 
   * @return  the name of the reader
   */
  @Override
  public String getName() {
    return name;
  }
  
  /**
   * Get the number of JPEG files found in the zip file.
   * 
   * @return  the number of JPEG files
   */
  @Override
  public int getSize() {
    return fileCount;
  }

  /**
   * Reads the next JPEG file sample. Successive calls to this will iterate
   * through the JPEG files in the specified zip file. When there are no more
   * JPEG files, this will return null.
   * 
   * @return                 a JPEG data sample, or null if there are no more
   * @throws ParseException  if current JPEG file timestamp can't be parsed
   * @throws IOException     if there is an error reading the zip file
   */
  @Override
  public JPEGFileDataSample readSample() throws ParseException, IOException {
    ZipEntry entry;
    
    // get the next zip entry which is a JPEG file
    do {
      if (!entries.hasMoreElements()) {
        zipFile.close();
        return null;
      }
      
      entry = entries.nextElement();
    } while (!isJPEG(entry));
    
    String name = entry.getName();
    double timestamp = getTimestamp(name);
    byte[] data = getData(entry);

    JPEGFileDataSample sample = new JPEGFileDataSample(name, timestamp, data);
    return sample;
  }
  
  /**
   * Get the contents of the zip entry.
   * 
   * @param entry         the zip entry to read
   * @return              the contents of the zip entry as a byte array
   * @throws IOException  if there is an error reading the file
   */
  private byte[] getData(ZipEntry entry) throws IOException {
    int size = (int) entry.getSize();
    byte[] data = new byte[size];
    
    InputStream in = zipFile.getInputStream(entry);
    int totalBytesRead = 0;
    while (totalBytesRead < size) {
      int bytesRead = in.read(data, totalBytesRead, size-totalBytesRead);
      if (bytesRead == -1) break;
      totalBytesRead += bytesRead;
    }
    in.close();
    
    return data;
  }

  /**
   * Test if the zip entry is a JPEG file. It is a JPEG file if it ends with
   * .jpg or .jpeg.
   * 
   * @param entry  the zip entry
   * @return       true if the entry is a zip file, false otherwise
   */
  private static boolean isJPEG(ZipEntry entry) {
    if (entry.isDirectory()) {
      return false;
    }
    
    String name = entry.getName().toLowerCase();
    return name.endsWith(".jpg") || name.endsWith(".jpeg");
  }
  
}