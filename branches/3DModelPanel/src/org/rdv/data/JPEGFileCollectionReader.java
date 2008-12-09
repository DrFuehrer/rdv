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

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rdv.DataViewer;

/**
 * A data reader for a collection of timestamped JPEG files.
 * 
 * @author Jason P. Hanley
 */
public abstract class JPEGFileCollectionReader {

  /**
   * Gets the name of the reader.
   * 
   * @return  the name of the reader
   */
  public abstract String getName();
  
  /**
   * Gets the number of JPEG files in this collection.
   * 
   * @return  the number of JPEG files
   */
  public abstract int getSize();
  
  /**
   * Reads the next JPEG file sample. Successive calls to this will iterate
   * through the collection of JPEG files. When there are no more JPEG files,
   * this will return null.
   * 
   * @return                 a JPEG data sample, or null if there are no more
   * @throws ParseException  if current JPEG file timestamp can't be parsed
   * @throws IOException     if the is an error reading from the collection
   */
  public abstract JPEGFileDataSample readSample() throws ParseException, IOException;
  
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
   * @param name             the name of the file to look at
   * @return                 a timestamp in seconds since the epoch
   * @throws ParseException  if the timestamp can't be parsed
   */
  protected static double getTimestamp(String name) throws ParseException {
    Pattern pattern = Pattern.compile("_?([0-9a-zA-Z\\-\\.]+).(?i)jpe?g$");
    Matcher matcher = pattern.matcher(name);
    if (!matcher.find()) {
      throw new ParseException("Can't find a timestamp in this file name: " + name + ".", 0);
    }
    
    String timeString = matcher.group(1).replaceAll("[\\-\\.]", "");
    double timestamp = DataViewer.parseTimestamp(timeString);
    return timestamp;
  }
  
}