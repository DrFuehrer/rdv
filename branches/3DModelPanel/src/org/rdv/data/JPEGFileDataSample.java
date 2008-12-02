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

/**
 * A class to represent a data file containing a JPEG image.
 * 
 * @author Jason P. Hanley
 */
public class JPEGFileDataSample extends DataSample{
  
  /** the JPEG image */
  private final byte[] data;
  
  /** the JPEG file name */
  private final String name;
  
  /**
   * Creates the object to represent this JPEG file data sample.
   * 
   * @param name       the file name of the image
   * @param timestamp  the timestamp of the image
   * @param data       the image data
   */
  public JPEGFileDataSample(String name, double timestamp, byte[] data) {
    super(timestamp);
    
    this.data = data;
    this.name = name;
  }

  /**
   * Gets the image data as a byte array.
   * 
   * @return  the image
   */
  public byte[] getData() {
    return data;
  }
  
  /**
   * Gets the file name of the image.
   * 
   * @return  the file name of the image
   */
  public String getName() {
    return name;
  }
  
}