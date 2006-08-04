/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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

package org.nees.buffalo.rdv.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * A class to read data from a data file.
 * 
 * @author Jason P. Hanley
 */
public class DataFileReader extends BufferedReader {
  /** The properties for the data file */
  private Map<String,String> properties;
  
  /** The channels in the data file */
  private List<DataFileChannel> channels;
  
  private String propertyDelimiter = ":";
  
  /** The delimiters to try for the data items */
  private String delimiters = "[\t,;]";
  
  /** The last line read in the file. */
  private String line;
  
  /** Indicates if there is a time column */
  private boolean hasTimeColumn;
  
  /** Indicates that the time column is in ISO8601 format */
  private boolean timeIsISO8601;
  
  /** The date format for ISO8601 */
  private static final SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /** The keys that can denote a list of channels */
  private static final String[] channelPropertyKeys = {"channel", "channels", "channel name", "channel names", "active channel", "active channels"};
  
  /** The keys that can denote a list of units */
  private static final String[] unitPropertyKeys = {"unit", "units", "channel unit", "channel units"};

  /**
   * Create a new data file reader and tries to read it's header.
   * 
   * @param file          the data file
   * @throws IOException  if there is an error opening or reading the data file
   */
  public DataFileReader(File file) throws IOException {
    super(new FileReader(file));
    
    properties = new Hashtable<String,String>();
    channels = new ArrayList<DataFileChannel>();
    
    hasTimeColumn = true;
    
    readHeader();
    
    parseUnitProperty();
  }
  
  /**
   * Get the properties in the data file header.
   * 
   * @return  the data file properties
   */
  public Map getProperties() {
    return properties;
  }

  /**
   * Get a list of channels in the data file.
   * 
   * @return  a list of channels
   */
  public List<DataFileChannel> getChannels() {
    return channels;
  }
    
  /**
   * Start reading data from the file. For each sample time (or line) the
   * listener will be called with the data read.
   * 
   * @param listener      the listener for data
   * @throws IOException  if there is an error reading the data file
   */
  public void readData(DataFileListener listener) throws IOException {
    int currentLine = 0;
    
    do {
      line = line.trim();
      String[] tokens = line.split(delimiters);
      
      if (tokens.length != channels.size()+1) {
        continue;
      }

      double timestamp;
      double[] values = new double[channels.size()];
      
      if (!hasTimeColumn) {
        timestamp = currentLine;
      } else if (timeIsISO8601) {
        try {
          timestamp = ISO8601_DATE_FORMAT.parse(tokens[0].trim()).getTime()/1000d;
        } catch (ParseException e) {
          continue;
        }
      } else {
        try {
          timestamp = Double.parseDouble(tokens[0].trim());
        } catch (NumberFormatException e) {
          continue;
        }
      }
        
      for (int i=1; i<tokens.length; i++) {
        double value;
        try {
          value = Double.parseDouble(tokens[i].trim());
        } catch (NumberFormatException e) {
          value = Double.NaN;
        }

        values[i-1] = value;
      }
      
      listener.postDataSamples(timestamp, values);
      
      currentLine++;
    } while ((line = readLine()) != null);
  }
  
  /**
   * Reads the header of the data file. This constructs the data file properties
   * and list of channels.
   * 
   * @throws IOException  if there is an error reading the data file
   */
  private void readHeader() throws IOException {
    while ((line = readLine()) != null) {
      // time whitespace around line
      line = line.trim();
      
      String[] property = line.split(propertyDelimiter);
      if (property.length == 2) {
        properties.put(property[0].trim().toLowerCase(), property[1].trim());
        continue;
      }
      
      // try to split line
      String[] tokens = line.split(delimiters);
      
      // look for the line with the channel names
      if (channels.size() == 0 && (line.matches("^\"?(?i:time).*") || startsWithKey(line, channelPropertyKeys))) {
        // if the line didn't split, try spaces as a delimiter
        if (tokens.length == 1) {
          delimiters = " +";
          tokens = line.split(delimiters);
        }        
        
        // go over every channel name
        for (int i=1; i<tokens.length; i++) {
          DataFileChannel channel = new DataFileChannel(tokens[i].trim());
          channels.add(channel);
        }
        
        // if no channels are found, we don't know how to parse this
        if (channels.size() == 0) {
          throw new IOException("No channels found in data file.");
        }
        
      // look for the start of data or the unit information
      } else if (channels.size() == 0 || tokens.length == channels.size()+1) {
        // see if this line contains data
        String value = tokens[0].trim();
        if (isNumber(value)) {
          if (channels.size() == 0) {
            generateFakeChannels(tokens.length-1);
          }
          return;
        } else if (isTimestamp(value)) {
          timeIsISO8601 = true;
          if (channels.size() == 0) {
            generateFakeChannels(tokens.length-1);
          }          
          return;
        } else if (channels.size() > 0 && (value.toLowerCase().startsWith("sec") || isKey(value, unitPropertyKeys))) {
          for (int i=1; i<tokens.length; i++) {
            DataFileChannel channel = channels.get(i-1);
            channel.setUnit(tokens[i].trim());
          }            
        }
      }
    }
    
    if (channels.size() == 0) {
      throw new IOException("No channels found in data file.");
    } else {
      throw new IOException("No data found in data file.");
    }
  }
  
  /**
   * Looks for the channel units in the properties and if found, puts them in
   * the <code>DataFileChannel</code>.
   *
   */
  private void parseUnitProperty() {
    String unitsString = getProperty(unitPropertyKeys);
    
    if (unitsString == null) {
      return;
    }
    
    String[] units = unitsString.trim().split(delimiters);
    if (units.length != channels.size()) {
      return;
    }
    
    for (int i=0; i<units.length; i++) {
      DataFileChannel channel = channels.get(i);
      channel.setUnit(units[i]);
    }
  }
  
  /**
   * See if there is a property for any of the keys and return it if there is.
   * This will return the first property found.
   * 
   * @param keys  the keys to look for
   * @return      the property value, or null if not found
   */
  private String getProperty(String[] keys) {
    for (String key : keys) {
      String value = properties.get(key);
      if (value != null) {
        return value;
      }
    }
    
    return null;
  }
  
  /**
   * See if the search key is contained in the keys.
   * 
   * @param searchKey  the key to search for
   * @param keys       the keys to look in
   * @return           true if the search key is found, false otherwise
   */
  private boolean isKey(String searchKey, String[] keys) {
    for (String key : keys) {
      if (key.compareToIgnoreCase(searchKey) == 0) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * See if any of the keys start the search line.
   * 
   * @param searchLine  the line to search in
   * @param keys        the keys to look for
   * @return            true if the line starts with any of the keys, false
   *                    otherwise
   */
  private boolean startsWithKey(String searchLine, String[] keys) {
    for (String key : keys) {
      if (searchLine.toLowerCase().startsWith(key)) {
        return true;
      }
    }
    
    return false;    
  }
  
  /**
   * Populate the list of channels. The channels will be named by their index.
   * 
   * @param numberOfChannels  the number of channels to create.
   */
  private void generateFakeChannels(int numberOfChannels) {
    for (int i=0; i<numberOfChannels; i++) {
      DataFileChannel channel = new DataFileChannel(Integer.toString(i));
      channels.add(channel);
    }
  }
  
  /**
   * Test if this string is a number.
   * 
   * @param value  the value string
   * @return       true if it is a number, false otherwise
   */
  private boolean isNumber(String value) {
    try {
      Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return false;
    }
    
    return true;
  }
  
  /**
   * Test if the string is an ISO8601 timestamp.
   * 
   * @param iso8601  the timestamp string
   * @return         true if it is a timestamp, false otherwise
   */
  private boolean isTimestamp(String iso8601) {
    try {
      ISO8601_DATE_FORMAT.parse(iso8601);
    } catch (ParseException e) {
      return false;
    }
    
    return true;
  }
}
