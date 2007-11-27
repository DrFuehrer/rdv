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

package org.nees.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class to interact from the Windows registry. This class requires that the
 * command line utility 'reg.exe' is on the path.
 * 
 * @author Jason P. Hanley
 */
public class Registry {
  /** the command line registry tool */
  private static final String REGISTRY_TOOL = "reg";
  
  /**
   * Reads the string (REG_SZ) for key using the default entry.
   * 
   * @param key           the registry key
   * @return              the value, or null if the key is not found
   * @throws IOException  if there is an error reading the registry
   */
  public static String readString(String key) throws IOException {
    return readString(key, null);
  }

  /**
   * Reads the string (REG_SZ) for key and entry name.
   * 
   * @param key           the registry key
   * @param name          the entry name
   * @return              the value, or null if the key/entry is not found
   * @throws IOException  if there is an error reading the registry
   */
  public static String readString(String key, String name) throws IOException {
    String command = REGISTRY_TOOL + " query \"" + key + "\" ";
    if (name != null) {
      command += "/v " + name;
    } else {
      command += "/ve";
    }
    
    String result = exec(command);
    
    String type = "REG_SZ";
    int p = result.indexOf(type);
    if (p == -1) {
      return null;
    }
    
    return result.substring(p + type.length()).trim();    
  }
  
  /**
   * Execute the command and return the results. This will not return until the
   * command has completed.
   * 
   * @param command     the command to execute
   * @return            the output of the command
   * @throws Exception  if there is an error executing the command
   */
  private static String exec(String command) throws IOException {
    StringBuilder result = new StringBuilder();

    Process process = Runtime.getRuntime().exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    String line;
    while ((line = reader.readLine()) != null) {
      result.append(line);
    }
    
    return result.toString();
  }
}