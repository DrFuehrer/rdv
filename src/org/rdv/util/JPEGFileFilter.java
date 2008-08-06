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

package org.rdv.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A filter for JPEG file. This will accept directories and files that have
 * an extension of jpg or jpeg.
 * 
 * @author Jason P. Hanley
 */
public class JPEGFileFilter implements FileFilter {
  
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