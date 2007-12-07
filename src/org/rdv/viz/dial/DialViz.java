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
package org.rdv.viz.dial;

import java.util.Iterator;

import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.Channel;
import org.rdv.rbnb.RBNBController;

import com.rbnb.sapi.ChannelMap;

/**
 * A visualization extension to view a numeric data channel on a dial.
 * 
 * @author  Jason P. Hanley
 * @see     DialModel
 * @see     DialPanel
 */
public class DialViz extends AbstractDataPanel {

  /** the model for the dial */
  private DialModel model;
  
  /**
   * Creates a DialViz with no channels.
   */
  public DialViz() {
    super();
    
    model = new DialModel();
    DialPanel panel = new DialPanel(model);

    setDataComponent(panel);
  }

  public boolean supportsMultipleChannels() {
    return false;
  }
  
  @Override
  protected void channelAdded(String channelName) {
    model.setName(channelName);
    
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    String unit = (channel != null) ? channel.getMetadata("units") : null;
    model.setUnit(unit);
  }

  @Override
  protected void channelRemoved(String channelName) {
    model.setValue(null);
    model.setName(null);
    model.setUnit(null);
  }
  
  @Override
  public void postTime(double time) {
    if (time < this.time) {
      model.setValue(null);
    }

    super.postTime(time);
    
    if (channelMap == null) {
      return;
    }
    
    Iterator i = channels.iterator();
    if (!i.hasNext()) {
      return;
    }

    String channelName = (String) i.next();
    int channelIndex = channelMap.GetIndex(channelName);

    // if there is data for channel, post it
    if (channelIndex != -1) {
      postDataDial(channelName, channelIndex);
    }
  }
  
  /**
   * Posts data for the specified channel to the dial. This will get the data
   * point closest to the current timestamp and set value of the dial to this.
   * 
   * @param channelName   the name of the data channel
   * @param channelIndex  the index of the data channel
   */
  private void postDataDial(String channelName, int channelIndex) {
    double[] times = channelMap.GetTimes(channelIndex);
    
    int valueIndex = -1;
    for (int i = times.length - 1; i >= 0; i--) {
      if (times[i] <= time) {
        valueIndex = i;
        break;
      }
    }
    
    if (valueIndex == -1) {
      return;
    }
    
    double value;

    int typeID = channelMap.GetType(channelIndex);

    switch (typeID) {
    case ChannelMap.TYPE_FLOAT64:
      value = channelMap.GetDataAsFloat64(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_FLOAT32:
      value = channelMap.GetDataAsFloat32(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_INT64:
      value = channelMap.GetDataAsInt64(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_INT32:
      value = channelMap.GetDataAsInt32(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_INT16:
      value = channelMap.GetDataAsInt16(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_INT8:
      value = channelMap.GetDataAsInt8(channelIndex)[valueIndex];
      break;
    case ChannelMap.TYPE_STRING:
    case ChannelMap.TYPE_UNKNOWN:
    case ChannelMap.TYPE_BYTEARRAY:
    default:
      return;
    }
    
    model.setValue(value);
  }
  
}