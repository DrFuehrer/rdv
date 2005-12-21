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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;

/**
 * A class to export data from data turbine to disk.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class RBNBExport {
  /**
   * The logger for this class.
   * 
   * @since  1.3
   */
  static Log log = LogFactory.getLog(RBNBImport.class.getName());
  
  /**
   * The RBNB host name to connect too.
   * 
   * @since  1.3
   */
  private String rbnbHostName;
  
  /**
   * The RBNB port number to connect too.
   * 
   * @since  1.3
   */
  private int rbnbPortNumber;
  
  /**
   * Tell the export thread to cancel
   * 
   * @since  1.3
   */
  private boolean cancelExport;
  
  /**
   * Initialize the class with the RBNB server to export data from.
   * 
   * @param rbnbHostName    the host name of the RBNB server
   * @param rbnbPortNumber  the port number of the RBNB server
   * @since                 1.3
   */
  public RBNBExport(String rbnbHostName, int rbnbPortNumber) {
    this.rbnbHostName = rbnbHostName;
    this.rbnbPortNumber = rbnbPortNumber;
    
    cancelExport = false;
  }
  

  /**
   * Start the export of the channels from the RBNB server to the specified
   * file. The data will be exported between the two time bounds provided.
   * <p>
   * This methods will spawn the export in another thread and return before it
   * is completed. Pass a listener for progress information.
   * 
   * @param channels   the list of channels to export
   * @param dataFile   the data file to write the data to
   * @param startTime  the start time of the data to export
   * @param endTime    the end time of the data to export
   * @param deltaT     save a time channel relative to the startTime
   * @param listener   a listener to post progress to
   * @since            1.3
   */
  public void startExport(final List channels,
                          final File dataFile,
                          final double startTime, final double endTime,
                          final boolean deltaT,
                          final ProgressListener listener) {
    new Thread() {
      public void run() {
        exportData(channels, dataFile, startTime, endTime, deltaT, listener);
      }
    }.start();        
  }
  
  /**
   * Export the channels from the RBNB server to the specified file. The data
   * will be exported between the two time bounds provided.
   * 
   * @param channels   the list of channels to export
   * @param dataFile   the data file to write the data to
   * @param startTime  the start time of the data to export
   * @param endTime    the end time of the data to export
   * @param deltaT     save a time channel relative to the startTime
   * @param listener   a listener to post progress to
   * @since            1.3
   */
  private synchronized void exportData(List channels,
                                       File dataFile,
                                       double startTime, double endTime,
                                       boolean deltaT,
                                       ProgressListener listener) {
       
     
    if (listener == null) {
      // create dummy listener if needed
      listener = new ProgressListener() {
        public void postProgress(double progress) {}
        public void postCompletion() {}
        public void postError(String errorMessage) {}
      };
    }
    
    if (channels == null || channels.size() == 0) {
      listener.postError("No channels were specified.");
      return;
    }
     
    if (dataFile == null) {
      listener.postError("Data file not specified.");
      return;
    }
    
    if (startTime > endTime) {
      listener.postError("The start time must be greater than the end time");
      return;
    }
    
    double time = startTime;
        
    try {
      Sink sink = new Sink();
      sink.OpenRBNBConnection(rbnbHostName + ":" + rbnbPortNumber, "RDVExport");
      ChannelMap cmap = new ChannelMap();
      for (int i=0; i<channels.size(); i++) {
        cmap.Add((String)channels.get(i));
      }
      
      BufferedWriter fileWriter = new BufferedWriter(new FileWriter(dataFile));
      
      fileWriter.write("Start time: " + RBNBUtilities.secondsToISO8601(startTime) + "\r\n");
      fileWriter.write("End time: " + RBNBUtilities.secondsToISO8601(endTime) + "\r\n");
      fileWriter.write("Export time: " + RBNBUtilities.millisecondsToISO8601(System.currentTimeMillis()) + "\r\n");
      fileWriter.write("\r\n");

      // write channel names
      fileWriter.write("Time\t");
      if (deltaT) {
        fileWriter.write("Relative Time\t");
      }
      for (int i=0; i<channels.size(); i++) {
        String channel = (String)channels.get(i);
        String[] channelParts = channel.split("/");
        fileWriter.write(channelParts[channelParts.length-1]);
        if (i != channels.size()-1) {
          fileWriter.write('\t');
        }
      }
      fileWriter.write("\r\n");

      // fetch channel metadata and write channel units (if available)
      sink.RequestRegistration(cmap);
      ChannelMap rmap = sink.Fetch(-1);
      fileWriter.write("ISO8601\t");
      if (deltaT) {
        fileWriter.write("Seconds\t");
      }
      for (int i=0; i<channels.size(); i++) {
        String channel = (String)channels.get(i);
        String unit = null;
        int index = rmap.GetIndex(channel);
        String[] metadata = rmap.GetUserInfo(index).split("\t|,");
        for (int j=0; j<metadata.length; j++) {
          String[] elements = metadata[j].split("=");
          if (elements.length == 2 && elements[0].equals("units")) {
            unit = elements[1];
            break;
          }     
        }       
        if (unit != null) {
          fileWriter.write(unit);
        }
        fileWriter.write('\t');
      }
      fileWriter.write("\r\n");
      
      listener.postProgress(0);

      while (time < endTime && !cancelExport) {
        double duration = 5;
        if (time + duration > endTime) {
          duration = endTime - time;
        }
        
        sink.Request(cmap, time, duration, "absolute");
        ChannelMap dmap = sink.Fetch(-1);
        
        ArrayList samples = new ArrayList();
        for (int i=0; i<channels.size(); i++) {
          String channel = (String)channels.get(i);
          int index = dmap.GetIndex(channel);
          if (index != -1) {
            int type = dmap.GetType(index);
            double[] times = dmap.GetTimes(index);
            for (int j=0; j<times.length; j++) {
              Sample sample;
              switch (type) {  
                case ChannelMap.TYPE_INT32:
                  sample = new Sample(channel, dmap.GetDataAsInt32(index)[j], times[j]);
                  break;
                case ChannelMap.TYPE_INT64:
                  sample = new Sample(channel, dmap.GetDataAsInt64(index)[j], times[j]);
                  break;
                case ChannelMap.TYPE_FLOAT32:
                  sample = new Sample(channel, dmap.GetDataAsFloat32(index)[j], times[j]);
                  break;
                case ChannelMap.TYPE_FLOAT64:
                  sample = new Sample(channel, dmap.GetDataAsFloat64(index)[j], times[j]);
                  break;
                default:
                  sample = new Sample(channel, "", times[j]);
              }
              samples.add(sample);
            }
          }
        }
        
        Collections.sort(samples, new SampleTimeComparator());
        
        Iterator it = samples.iterator();
        boolean end = false;
        
        Sample s = null;
        if (it.hasNext()) {
          s = (Sample)it.next();
        } else {
          end = true;
        }
        
        while (!end) {
          double t = s.getTime();
          fileWriter.write(RBNBUtilities.secondsToISO8601(t) + "\t");
          if (deltaT) {
            fileWriter.write(Double.toString(t-startTime) + "\t");
          }
          for (int i=0; i<channels.size(); i++) {
            String c = (String)channels.get(i);
            if (c.equals(s.getChannel()) && t == s.getTime()) {
              fileWriter.write(s.getData());
              if (it.hasNext()) {
                s = (Sample)it.next();
              } else {
                fileWriter.write("\r\n");
                end = true;
                break;
              }
            }
            if (i == channels.size()-1) {
              fileWriter.write("\r\n");
            } else {
              fileWriter.write('\t');
            }
          }
        }
        
        time += duration;
        
        listener.postProgress((time-startTime)/(endTime-startTime));
      }
      
      fileWriter.close();
      
      sink.CloseRBNBConnection();
      
      if (cancelExport) {
        dataFile.delete();
        listener.postError("Export canceled.");
      } else {
        listener.postCompletion();
      }
    } catch (Exception e) {
      e.printStackTrace();
      listener.postError("Error importing data file: " + e.getMessage());
      return;
    }

    cancelExport = false;

    return;
  }
  
  public void cancelExport() {
    cancelExport = true;
  }
  
  class Sample {
    String channel;
    String data;
    double time;
    
    public Sample(String channel, int data, double time) {
      this(channel, Integer.toString(data), time);
    }
    
    public Sample(String channel, long data, double time) {
      this(channel, Long.toString(data), time);
    }
    
    public Sample(String channel, float data, double time) {
      this(channel, Float.toString(data), time);
    }
    
    public Sample(String channel, double data, double time) {
      this(channel, Double.toString(data), time);
    }
    
    public Sample(String channel, String data, double time) {
      this.channel = channel;
      this.data = data;
      this.time = time;
    }
    
    public String getChannel() {
      return channel;
    }
    
    public String getData() {
      return data;
    }
    
    public double getTime() {
      return time;
    }
  }
  
  class SampleTimeComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
      double t1 = ((Sample)arg0).getTime();
      double t2 = ((Sample)arg1).getTime();
      if (t1 == t2) {
        return 0;
      } else if (t1 < t2) {
        return -1;
      } else {
        return 1;
      }      
    }    
  }
}