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

package org.rdv.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.rdv.rbnb.RBNBUtilities;

/**
 * A data file writer to write data to a tab delimited ASCII file.
 * 
 * @author  Jason P. Hanley
 * @see     DataFileWriter
 */
public class ASCIIDataFileWriter implements DataFileWriter {
  
  /** the data file */
  private File file;
  
  /** the writer for the data file */
  private BufferedWriter fileWriter;
  
  /** the time of the first data sample */
  private double firstSampleTime;
  
  public void init(List<Channel> channels, double startTime, double endTime, File file) throws IOException {
    this.file = file;
    
    fileWriter = new BufferedWriter(new FileWriter(file));
    
    writeHeader(channels, startTime, endTime);
  }
  
  private void writeHeader(List<Channel> channels, double startTime, double endTime) throws IOException {
    // write the times for the start, end, and export
    fileWriter.write("Start time: " + RBNBUtilities.secondsToISO8601(startTime) + "\r\n");
    fileWriter.write("End time: " + RBNBUtilities.secondsToISO8601(endTime) + "\r\n");
    fileWriter.write("Export time: " + RBNBUtilities.millisecondsToISO8601(System.currentTimeMillis()) + "\r\n");
    fileWriter.write("\r\n");
    
    // write channel names
    fileWriter.write("Time\t");
    for (int i=0; i<channels.size(); i++) {
      Channel channel = channels.get(i); 
      String[] channelParts = channel.getName().split("/");
      fileWriter.write(channelParts[channelParts.length-1]);
      if (i != channels.size()-1) {
        fileWriter.write('\t');
      }
    }
    fileWriter.write("\r\n");

    // write units
    fileWriter.write("Seconds\t");
    for (int i=0; i<channels.size(); i++) {
      Channel channel = channels.get(i); 
      if (channel.getUnit() != null) {
        fileWriter.write(channel.getUnit());
      }
      if (i != channels.size()-1) {
        fileWriter.write('\t');
      }
    }
    fileWriter.write("\r\n");
  }

  public void writeSample(NumericDataSample sample) throws IOException {
    if (firstSampleTime == 0) {
      firstSampleTime = sample.getTimestamp();
    }
    
    // write the time
    fileWriter.write(Double.toString(sample.getTimestamp() - firstSampleTime) + "\t");
    
    // write the values
    Number[] values = sample.getValues();
    for (int i=0; i<values.length; i++) {
      fileWriter.write(values[i].toString());
      if (i != values.length-1) {
        fileWriter.write('\t');
      }
    }
    fileWriter.write("\r\n");
  }

  public void abort() {
    try {
      fileWriter.close();
      
      file.delete();
    } catch (Exception e) {}
  }

  public void close() throws IOException {
    fileWriter.close();
  }
  
}