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

package org.nees.buffalo.rdv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A class to access run-time properties loaded from the rdv.properties file.
 * 
 * @author Jason P. Hanley
 */
public final class AppProperties {
  static {
    load();
  }
  
  /** the properties */
  private static Properties appProperties;
  
  /**
   * Get a property.
   * 
   * @param key  the property key
   * @return     the property value, or null if it was not found
   */
  public static String getProperty(String key) {
    return getProperty(key, null);
  }

  /**
   * Get a property, passing in a default value.
   * 
   * @param key           the property key
   * @param defaultValue  the value of the porperty if it is not found
   * @return              the property value, or the default value if it is not
   *                      found
   */
  public static String getProperty(String key, String defaultValue) {
    String value = System.getProperty(key);
    if (value != null) {
      return value;
    }
    
    return appProperties.getProperty(key, defaultValue);
  }
  
  /**
   * Load the properties from the rdv.property file.
   */
  private static void load() {
    appProperties = new Properties();
    
    InputStream rdvPropertiesStream = DataViewer.getResourceAsStream("rdv.properties");
    if (rdvPropertiesStream == null) {
      return;
    }
    
    try {
      appProperties.load(rdvPropertiesStream);
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
}
