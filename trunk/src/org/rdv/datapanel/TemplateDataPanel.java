/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.datapanel;

import javax.swing.JComponent;

import com.rbnb.sapi.ChannelMap;

/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

  /** the last time displayed */
  private double lastTimeDisplayed;

  /**
   * Create the data panel.
   */
  public TemplateDataPanel() {
    super();

    initDataComponent();
  }

  /**
   * Initialize the UI component and pass it too the abstract class.
   */
  private void initDataComponent() {
    // TODO create data component
    JComponent myComponent = null;
    setDataComponent(myComponent);
  }

  public boolean supportsMultipleChannels() {
    // TODO change if this data panel supports multiple channels
    return false;
  }

  public void postTime(double time) {
    if (time < this.time) {
      lastTimeDisplayed = -1;

      // TODO clear data in your data component
    }

    super.postTime(time);

    if (channelMap == null) {
      //no data to display yet
      return;
    }

    //loop over all channels and see if there is data for them
    for (String channelName : channels) {
      int channelIndex = channelMap.GetIndex(channelName);

      //if there is data for channel, post it
      if (channelIndex != -1) {
        displayData(channelName, channelIndex);
      }
    }
  }

  private void displayData(String channelName, int channelIndex) {
    if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_FLOAT64) {
      return;
    }
    
    double[] times = channelMap.GetTimes(channelIndex);

    int startIndex = -1;

    // determine what time we should load data from
    double dataStartTime;
    if (lastTimeDisplayed == time) {
      dataStartTime = time - timeScale;
    } else {
      dataStartTime = lastTimeDisplayed;
    }

    for (int i = 0; i < times.length; i++) {
      if (times[i] > dataStartTime && times[i] <= time) {
        startIndex = i;
        break;
      }
    }

    //see if there is no data in the time range we are loooking at
    if (startIndex == -1) {
      return;
    }

    int endIndex = startIndex;

    for (int i = times.length - 1; i > startIndex; i--) {
      if (times[i] <= time) {
        endIndex = i;
        break;
      }
    }

    double[] datas = channelMap.GetDataAsFloat64(channelIndex);
    for (int i=startIndex; i<=endIndex; i++) {
      double time = times[i];
      double data = datas[i];
      
      // TODO display the data at this timestamp
    }

    lastTimeDisplayed = times[endIndex];
  }
}