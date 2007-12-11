/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2007 Palta Software
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

package org.rdv.rbnb;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.rdv.data.NumericDataSample;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * A class to read numeric data from a RBNB server.
 * 
 * @author Jason P. Hanley
 */
public class RBNBReader {

  /** the sink to get data from */
  private final Sink sink;

  /** the list of channels to get data from */
  private List<String> channels;

  /** the start and end times to get data */
  private double startTime, endTime;

  /** the channel map for the sink */
  private ChannelMap cmap;

  /** the time of the last data sample */
  private double time;
  
  /** a buffer of samples read from the server */
  private Queue<Sample> samples;

  /**
   * Connects to the RBNB server and preparse to get data.
   * 
   * @param rbnbServer      the address of the rbnb server
   * @param channels        the channels to get data for
   * @param startTime       the start time for the data
   * @param endTime         the end time of the data
   * @throws SAPIException  if there is an error connecting to the server
   */
  public RBNBReader(String rbnbServer, List<String> channels, double startTime, double endTime)
      throws SAPIException {
    this.channels = channels;
    this.startTime = startTime;
    this.endTime = endTime;

    sink = new Sink();
    sink.OpenRBNBConnection(rbnbServer, "RDVExport");

    cmap = new ChannelMap();
    for (String channel : channels) {
      cmap.Add(channel);
    }
    
    time = startTime;
    
    samples = new PriorityQueue<Sample>();
  }

  /**
   * Read a sample from the RBNB server. When there is no more data, this will
   * return null.
   * 
   * @return                a sample of data
   * @throws SAPIException  if there is an error getting the data
   */
  public NumericDataSample readSample() throws SAPIException {
    // see if we need to fetch more data
    while (samples.isEmpty()) {
      if (time >= endTime) {
        return null;
      }

      fetchDataBlock();
    }
    
    // initialize the data array
    Number[] data = new Number[channels.size()];
    for (int i = 0; i < channels.size(); i++) {
      data[i] = Double.NaN;
    }
    
    double t = samples.peek().getTime();
    
    // fill the array with channels of the same timestamp
    while (!samples.isEmpty() && t == samples.peek().getTime()) {
      Sample sample = samples.remove();
      int channelIndex = channels.indexOf(sample.getChannel());
      data[channelIndex] = sample.getData();
    }

    NumericDataSample dataSample = new NumericDataSample(t, data);
    return dataSample;
  }

  /**
   * Fetches a block of data from the server and adds it to the sample queue.
   * 
   * @throws SAPIException  if there is an error fetching the data
   */
  private void fetchDataBlock() throws SAPIException {
    double duration = 2;
    if (time + duration > endTime) {
      duration = endTime - time;
    }

    sink.Request(cmap, time, duration, "absolute");
    ChannelMap dmap = sink.Fetch(-1);

    for (int i = 0; i < channels.size(); i++) {
      String channel = channels.get(i);
      int index = dmap.GetIndex(channel);
      if (index != -1) {
        int type = dmap.GetType(index);
        double[] times = dmap.GetTimes(index);
        for (int j = 0; j < times.length; j++) {
          Sample sample;

          /* Skip data that isn't in the requested time bounds. This is due
           * to overlap between requests for data. 
           */
          if (times[j] > startTime && times[j] <= time) {
            continue;
          }

          switch (type) {
          case ChannelMap.TYPE_INT32:
            sample = new Sample(channel, dmap.GetDataAsInt32(index)[j],
                times[j]);
            break;
          case ChannelMap.TYPE_INT64:
            sample = new Sample(channel, dmap.GetDataAsInt64(index)[j],
                times[j]);
            break;
          case ChannelMap.TYPE_FLOAT32:
            sample = new Sample(channel, dmap.GetDataAsFloat32(index)[j],
                times[j]);
            break;
          case ChannelMap.TYPE_FLOAT64:
            sample = new Sample(channel, dmap.GetDataAsFloat64(index)[j],
                times[j]);
            break;
          default:
            sample = new Sample(channel, null, times[j]);
          }
          
          samples.offer(sample);
        }
      }
    }

    time += duration;
  }
  
  /**
   * Closes the connection to the RBNB server.
   */
  public void close() {
    sink.CloseRBNBConnection();
  }

  /**
   * A class to hold a data sample with its name, sample time, and value.
   */
  class Sample implements Comparable<Sample> {
    String channel;

    Number data;

    double time;

    public Sample(String channel, Number data, double time) {
      this.channel = channel;
      this.data = data;
      this.time = time;
    }

    public String getChannel() {
      return channel;
    }

    public Number getData() {
      return data;
    }

    public double getTime() {
      return time;
    }

    public int compareTo(Sample sample2) {
      double t1 = getTime();
      double t2 = sample2.getTime();
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