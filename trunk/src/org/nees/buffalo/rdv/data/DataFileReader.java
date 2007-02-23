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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
public class DataFileReader {
  /** The data file */
  private URL file;
  
  /** The reader for the data file */
  private BufferedReader reader;
  
  /** The properties for the data file */
  private Map<String,String> properties;
  
  /** The channels in the data file */
  private List<DataFileChannel> channels;
  
  private String propertyDelimiter = ":";
  
  /** The delimiters to try for the data items. Tab, comma, and semi-colon */
  private String delimiters = "[\t,;]";
  
  /** The last line read in the file. */
  private String line;
  
  /** The start time for the data */
  private double startTime;
  
  /** Indicates if there is a time column */
  private boolean hasTimeColumn;
  
  /** Indicates that the time column is in ISO8601 format */
  private boolean timeIsISO8601;
  
  /** The number of samples read */
  private int samples;
  
  /** The date format for ISO8601 */
  private static final SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /** The keys that can denote a list of channels */
  private static final String[] channelPropertyKeys = {"channel", "channels", "channel name", "channel names", "active channel", "active channels"};
  
  /** The keys that can denote a list of units */
  private static final String[] unitPropertyKeys = {"unit", "units", "channel unit", "channel units"};
  
  /** The keys that can have the start time */
  private static final String[] startTimePropertyKeys = {"start time"};
  
  /** The number of lines to parse before giving up on understanding the file */
  private static final int MAX_HEADER_LINES = 100;

  /**
   * Create a new data file reader and try to read the data file's header.
   * 
   * @param file          the data file
   * @throws IOException  if there is an error opening or reading the data file
   */
  public DataFileReader(File file) throws IOException {
    this(file.toURL());
  }

  /**
   * Create a new data file reader and try to read the data file's header.
   * 
   * @param file          the data file URL
   * @throws IOException  if there is an error opening or reading the data file
   */
  public DataFileReader(URL file) throws IOException {
    this.file = file;
    
    reader = new BufferedReader(new InputStreamReader(file.openStream()));
    
    properties = new Hashtable<String,String>();
    channels = new ArrayList<DataFileChannel>();
    
    hasTimeColumn = true;
    
    try {
      readHeader();
    } catch (IOException e) {
      // try parsing with a space delimiter
      reader = new BufferedReader(new InputStreamReader(file.openStream()));
      delimiters = " +";
      readHeader();
    }
    
    // get start time from header for data files using an elapsed time column
    startTime = 0;
    if (getProperty(startTimePropertyKeys) != null) {
      try {
        startTime = ISO8601_DATE_FORMAT.parse(getProperty(startTimePropertyKeys)).getTime()/1000d;
      } catch (ParseException e) {}
    }
    
    samples = 0;
    
    parseUnitProperty();
    
    if (properties.get("samples") == null) {
      int samples = scanData();
      properties.put("samples", Long.toString(samples));
    }
  }
  
  /**
   * Get the property value for the specified key.
   * 
   * @param key  the property key
   * @return     the property value, or null if there is no property for the key
   */
  public String getProperty(String key) {
    return properties.get(key);
  }
  
  /**
   * Get the properties in the data file header.
   * 
   * @return  the data file properties
   */
  public Map<String,String> getProperties() {
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
   * Reads and returns a data sample from the data file. Null will be returned
   * when the end of the file is reached.
   * 
   * @return              the data sample, or null if the end of the file is
   *                      reached
   * @throws IOException  if there is an error reading the data file
   */
  public DoubleDataSample readSample() throws IOException {
    if (line == null) {
      return null;
    }
    
    int firstDataIndex;
    if (hasTimeColumn) {
      firstDataIndex = 1;
    } else {
      firstDataIndex = 0;
    }    
    
    do {
      line = line.trim();
      String[] tokens = line.split(delimiters);
      
      if (tokens.length != channels.size()+firstDataIndex) {
        continue;
      }

      double timestamp;
      double[] values = new double[channels.size()];
      
      if (!hasTimeColumn) {
        timestamp = samples;
      } else if (timeIsISO8601) {
        try {
          timestamp = ISO8601_DATE_FORMAT.parse(tokens[0].trim()).getTime()/1000d;
        } catch (ParseException e) {
          continue;
        }
      } else {
        try {
          timestamp = startTime + Double.parseDouble(tokens[0].trim());
        } catch (NumberFormatException e) {
          continue;
        }
      }
        
      for (int i=firstDataIndex; i<tokens.length; i++) {
        double value;
        try {
          value = Double.parseDouble(tokens[i].trim());
        } catch (NumberFormatException e) {
          value = Double.NaN;
        }

        values[i-firstDataIndex] = value;
      }
      
      samples++;
      
      line = reader.readLine();
      
      return new DoubleDataSample(timestamp, values);
    } while ((line = reader.readLine()) != null);
    
    return null;
  }
    
  /**
   * Reads the header of the data file. This constructs the data file properties
   * and list of channels.
   * 
   * @throws IOException  if there is an error reading the data file
   */
  private void readHeader() throws IOException {
    int lines = 0;
    
    while ((line = reader.readLine()) != null && lines++ < MAX_HEADER_LINES) {
      // time whitespace around line
      line = line.trim();
      
      // skip blank lines
      if (line.length() == 0) {
        continue;
      }
      
      // look for properties
      String[] property = line.split(propertyDelimiter, 2);
      if (property.length == 2) {
        properties.put(stripString(property[0]).toLowerCase(), stripString(property[1]));
        continue;
      }
      
      // try to split line
      String[] tokens = line.split(delimiters);
      String firstToken = stripString(tokens[0]);
      
      // look for the line with the channel names
      if (channels.size() == 0 && (firstToken.compareToIgnoreCase("time") == 0 || isKey(firstToken, channelPropertyKeys))) {        
        // go over every channel name
        for (int i=1; i<tokens.length; i++) {
          DataFileChannel channel = new DataFileChannel(stripString(tokens[i]));
          channels.add(channel);
        }
        
        // if no channels are found, we don't know how to parse this
        if (channels.size() == 0) {
          throw new IOException("No channels found in data file.");
        }
        
      // look for the start of data or the unit information
      } else if (channels.size() == 0 || tokens.length == channels.size()+1) {
        // see if this line contains data
        if (isNumber(firstToken)) {
          if (channels.size() == 0) {
            hasTimeColumn = false;
            generateFakeChannels(tokens.length);
          }
          return;
        } else if (isTimestamp(firstToken)) {
          timeIsISO8601 = true;
          if (channels.size() == 0) {
            generateFakeChannels(tokens.length-1);
          }          
          return;
        } else if (channels.size() > 0 && (firstToken.toLowerCase().startsWith("sec") || isKey(firstToken, unitPropertyKeys))) {
          for (int i=1; i<tokens.length; i++) {
            DataFileChannel channel = channels.get(i-1);
            channel.setUnit(stripString(tokens[i]));
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
   * Scan the data and determine how many samples there are in the file. Note
   * that this will only return an approximation.
   * 
   * @return              an approximate number of samples in the file
   * @throws IOException  if there is an error reading the file
   */
  private int scanData() throws IOException {
    int samples = 1;
    
    while (reader.readLine() != null) {
      samples++;
    }
    
    reader.close();
    reader = new BufferedReader(new InputStreamReader(file.openStream()));
    readHeader();    
    
    return samples;
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
   * Populate the list of channels. The channels will be named by their index.
   * 
   * @param numberOfChannels  the number of channels to create.
   */
  private void generateFakeChannels(int numberOfChannels) {
    for (int i=0; i<numberOfChannels; i++) {
      DataFileChannel channel = new DataFileChannel(Integer.toString(i+1));
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
  
  /**
   * Removes leading and trailing white space from a string. If the string is
   * quoted, the quotes are also stripped.
   * 
   * @param cell  the string to strip
   * @return      the stripped string
   */
  private static String stripString(String cell) {
    cell = cell.trim();
    if (cell.startsWith("\"") && cell.endsWith("\"")) {
      if (cell.length() > 2) {
        cell = cell.substring(1, cell.length()-1).trim();
      } else {
        cell = "";
      }
    }
    return cell;
  }
  
}
