/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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

package org.nees.buffalo.rdv.rbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;

/**
 * A class to import data into data turbine.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class RBNBImport {
	/**
	 * The logger for this class.
	 * 
	 * @since  1.2
	 */
	static Log log = LogFactory.getLog(RBNBImport.class.getName());
	
	/**
	 * The RBNB host name to connect too.
	 * 
	 * @since  1.2
	 */
	private String rbnbHostName;
	
	/**
	 * The RBNB port number to connect too.
	 * 
	 * @since  1.2
	 */
	private int rbnbPortNumber;
	
	/**
	 * Tell the import thread to cancel
	 * 
	 * @since  1.2
	 */
	private boolean cancelImport;
	
	/**
	 * Initialize the class with the RBNB server to import data too.
	 * 
	 * @param rbnbHostName    the host name of the RBNB server
	 * @param rbnbPortNumber  the port number of the RBNB server
	 * @since                 1.2
	 */
	public RBNBImport(String rbnbHostName, int rbnbPortNumber) {
		this.rbnbHostName = rbnbHostName;
		this.rbnbPortNumber = rbnbPortNumber;
		
		cancelImport = false;
	}
	
	/**
	 * Start the import of the data file to the RBNB server specified in the
	 * constructor using the source name supplied.
	 * <p>
	 * This methods will spawn the import in another thread and return before it
	 * is completed. 
	 * 
	 * @param sourceName  the source name for the RBNB server
	 * @param dataFile    the file containing the data to import
	 * @since             1.2
	 */
	public void startImport(final String sourceName, final File dataFile) {
		startImport(sourceName, dataFile, null);
	}
	
	/**
	 * Start the import of the data file to the RBNB server specified in the
	 * constructor using the source name supplied. The status of the import will
	 * be posted too the listener.
	 * <p>
	 * This methods will spawn the import in another thread and return before it
	 * is completed.
	 * 
	 * @param sourceName  the source name for the RBNB server
	 * @param dataFile    the file containing the data to import
	 * @param listener    the listener to post status too
	 * @since             1.2
	 */
	public void startImport(final String sourceName, final File dataFile, final ProgressListener listener) {
		new Thread() {
			public void run() {
				importData(sourceName, dataFile, listener);
			}
		}.start();		
	}
	
	/**
	 * Import specified data file into RBNB server with the specified source name.
	 * If the listener is not null, status ofthe import will be posted.
	 * 
	 * @param sourceName  the source name for the data channels
	 * @param dataFile    the file containing the data channels
	 * @param listener    the listener to post status too, can be null
	 * @since             1.2
	 */
	private synchronized void importData(String sourceName, File dataFile, ProgressListener listener) {
    if (dataFile == null) {
      listener.postError("Data file not specified.");
      return;
    }
    
		String delimiters = "[\t,;]";
		int[] channels;
		int numberOfChannels;
		
		int bufferCapacity = 64;
		int bufferSize = 0;
		
		int archiveSize = bufferCapacity * 1000;
		int flushes = 0;
		
		long fileLength = dataFile.length();
		long bytesRead = 0;
    
    int dataChannelOffset = 1;
    boolean hasTimeStampChannel = false;
		
		try {
			ChannelMap cmap = new ChannelMap();
			
			BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
      
      // Skip header until channel names row. This assumes the first channel
      // name starts with 'time' (case insensitive).
			String line = null;;
      while ((line = fileReader.readLine()) != null) {
        bytesRead += line.length() + 2;
        if (line.matches("^\"?(?i:time).*")) {
          break;
        }
      }
      
			if (line != null) {
				bytesRead += line.length() + 2;
				line = line.trim();
				String[] tokens = line.split(delimiters);
        if (tokens.length == 1) {
          delimiters = " +";
          tokens = line.split(delimiters);
        }

				if (tokens.length == 1) {
					listener.postError("Unable to read data file header");
					return;
				}

        if (tokens.length >= 2 && stripCell(tokens[1]).toLowerCase().equals("timestamp")) {
          numberOfChannels = tokens.length-2;
          dataChannelOffset++;
          hasTimeStampChannel = true;
        } else {
          numberOfChannels = tokens.length-1;          
        }

				channels = new int[numberOfChannels];
				for (int i=0; i<numberOfChannels; i++) {
					String channelName = stripCell(tokens[i+dataChannelOffset]);
					channels[i] = cmap.Add(channelName);
					cmap.PutMime(channels[i], "application/octet-stream");
				}
			} else {
				listener.postError("Unable to read data file header");
				return;
			}
      
      line = fileReader.readLine();
      if (line != null) {
        bytesRead += line.length() + 2;
        line = line.trim();
        String[] tokens = line.split(delimiters);
        if ((numberOfChannels+dataChannelOffset == tokens.length) && stripCell(tokens[0]).toLowerCase().startsWith("sec")) {
          for (int i=0; i<numberOfChannels; i++) {
            String unit = stripCell(tokens[i+dataChannelOffset]);
            cmap.PutUserInfo(channels[i], "units=" + unit);
          }
        }
      } else {
        listener.postError("Unable to read data file headers");
        return;
      }
      
      Source source = new Source(1, "create", archiveSize);
      source.OpenRBNBConnection(rbnbHostName + ":" + rbnbPortNumber, sourceName);      
      source.Register(cmap);
      
      double startTime = System.currentTimeMillis()/1000d;
						
			while ((line = fileReader.readLine()) != null && !cancelImport) {
				bytesRead += line.length() + 2;
				line = line.trim();
				String[] tokens = line.split(delimiters);
				if (tokens.length != numberOfChannels+dataChannelOffset) {
					log.info("Skipping this line of data: " + line);
					continue;
				}
				
				try {
					double timestamp;
          if (hasTimeStampChannel) {
            timestamp = RBNBUtilities.ISO8601ToSeconds(tokens[1].trim());
          } else {
            timestamp = startTime + Double.parseDouble(tokens[0].trim());
          }
					cmap.PutTime(timestamp, 0);

					for (int i=0; i<numberOfChannels; i++) {
            String dataString = tokens[i+dataChannelOffset].trim();
            if (dataString.length() > 0) {
  						double[] data = {Double.parseDouble(dataString) };
  						cmap.PutDataAsFloat64(channels[i], data);
            }
					}
					
					if (++bufferSize == bufferCapacity) {
						source.Flush(cmap, true);
						bufferSize = 0;
						if (++flushes == archiveSize) {
							source.CloseRBNBConnection();
							listener.postError("The file is too large for the archive size.");
							return;
						}
					}
				} catch (NumberFormatException nfe) {
					log.warn("Skipping this line of data: " + line);
					continue;
				}
				
				double statusRatio = ((double)bytesRead)/((double)fileLength);
				if (listener != null) {
					listener.postProgress(statusRatio);
				}
			}
			
			if (bufferSize > 0) {
				source.Flush(cmap, true);
			}
			
			if (listener != null) {
				if (!cancelImport) {
					listener.postProgress(1);
				}
			}
			
			fileReader.close();
			
			if (cancelImport) {
				source.CloseRBNBConnection();
			} else {
				source.Detach();
			}
		} catch (Exception e) {
			e.printStackTrace();
			listener.postError("Error importing data file: " + e.getMessage());
			return;
		}
		
		if (listener != null) {
			if (cancelImport) {
				listener.postError("The import was canceled.");
			} else {
				listener.postCompletion();
			}
		}
		
		cancelImport = false;

		return;
	}
	
	public void cancelImport() {
		cancelImport = true;
	}
  
  /**
   * Removes leading and trailing white space from a string. If the string is
   * quoted, the quotes are also stripped.
   * 
   * @param cell  the string to strip
   * @return      the stripped string
   */
  private String stripCell(String cell) {
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
