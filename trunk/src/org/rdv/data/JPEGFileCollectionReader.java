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

package org.rdv.data;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A data reader for a collection of timestamped JPEG files.
 * 
 * @author Jason P. Hanley
 */
public class JPEGFileCollectionReader {
  /** the JPEG files to read from */
  private List<File> files;

  /** the current index for the reader */
  private int index;
  
  /** a file filter for JPEG files and directories */
  private static final FileFilter filter = new JPEGFileFilter();
  
  /** the timestamp format for the file names */
  private static final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSS'Z'");
  
  /** the timestamp format for the file names (JpgSaverSink format) */
  private static final SimpleDateFormat SHORT_ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  
  // set timezone to UTC
  static {
    ISO_8601_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    SHORT_ISO_8601_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }  
  
  /**
   * Creates a new reader for the collection of JPEG files in the specified
   * directory.
   * 
   * @param directory               the directory to look for JPEG files
   * @throws FileNotFoundException  if the directory doesn't exist
   */
  public JPEGFileCollectionReader(File directory) throws FileNotFoundException {
    this(directory, true);
  }
  
  /**
   * Creates a new reader for the collection of JPEG files in the specified
   * directory.
   * 
   * @param directory               the directory to look for JPEG files
   * @param recurse                 if true, look in subdirectories for files
   * @throws FileNotFoundException  if the directory doesn't exist
   */
  public JPEGFileCollectionReader(File directory, boolean recurse) throws FileNotFoundException {
    if (!directory.exists()) {
      throw new FileNotFoundException(directory.toString());
    }
    
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("The specified directory is not a directory: " + directory.toString());
    }
    
    files = scanDirectory(directory, recurse);
    Collections.sort(files);
  }
  
  /**
   * Get the number of JPEG files found.
   * 
   * @return  the number of JPEG files in this collection
   */
  public int getFileCount() {
    return files.size();
  }
  
  /**
   * Read a sample from the collection of JPEG files. Successive calls to this
   * will iterate through the collection. At the end this will return null.
   * 
   * @return                 a JPEG data sample, or null if there are no more
   * @throws ParseException  if current JPEG file timestamp can't be parsed
   * @throws IOException     if the current JPEG file can't be read
   */
  public JPEGFileDataSample readSample() throws ParseException, IOException {
    if (files.size() == index) {
      return null;
    }
    
    File file = files.get(index++);
    
    double timestamp = getTimestamp(file);
    byte[] data = getData(file);
    
    JPEGFileDataSample sample = new JPEGFileDataSample(timestamp, data, file);
    return sample;
  }
  
  /**
   * Get a list of JPEG files in the given directory (optionally recursing into
   * subdirectories). A file is detected as a JPEG file it's extension is jpg or
   * jpeg.
   * 
   * @param directory  the directory to scan
   * @param recurse    if true, look in subdirectories for files
   * @return           a list of JPEG files found.
   */
  private static List<File> scanDirectory(File directory, boolean recurse) {
    List<File> files = new ArrayList<File>();
    
    File[] fileListing = directory.listFiles(filter);
    for (File file : fileListing) {
      if (!file.isDirectory()) {
        files.add(file);
      } else if (recurse) {
        files.addAll(scanDirectory(file, true));
      }
    }
    
    return files;
  }
  
  /**
   * Get the timestamp from the name of the file. This will look for a timestamp
   * in a file following these formats:
   * 
   * NAME_YYYY-MM-DDTHH.MM.SS.NNNZ.jpg
   * 
   * or
   * 
   * NAME_YYYYMMDDTHHMMSSNNN.jpg
   * 
   * where the NAME is optional and will be ignored.
   *   
   * @param file             the file to look at
   * @return                 a timestamp in seconds since the epoch
   * @throws ParseException  if the timestamp can't be parsed
   */
  private double getTimestamp(File file) throws ParseException {
    String name = file.getName();
    
    Pattern pattern = Pattern.compile("_?([0-9TZ\\-\\.]+).(?i)jpe?g$");
    Matcher matcher = pattern.matcher(name);
    if (!matcher.find()) {
      throw new ParseException("Can't find a timestamp in this file name.", 0);
    }
    
    String timeString = matcher.group(1);
    double timestamp;
    if (timeString.length() == 17) {
      timestamp = SHORT_ISO_8601_DATE_FORMAT.parse(timeString).getTime()/1000d;
    } else {
      timestamp = ISO_8601_DATE_FORMAT.parse(timeString).getTime()/1000d;
    }
    return timestamp;
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
  
  /**
   * A filter for JPEG file. This will accept directories and files that have
   * an extension of jpg or jpeg.
   */
  private static class JPEGFileFilter implements FileFilter {
    /**
     * See if the file is a JPEG file. This will return true for any JPEG file
     * or a directory.
     * 
     * @param file  the file to test
     * @return      true if the file is a JPEG file or a directory, false
     *              otherwise
     */
    public boolean accept(File file) {
      String name = file.getName().toLowerCase();
      return file.isDirectory() ||
             name.endsWith(".jpg") ||
             name.endsWith(".jpeg");
    }    
  }
}