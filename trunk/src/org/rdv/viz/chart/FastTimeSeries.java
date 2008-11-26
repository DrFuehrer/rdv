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

package org.rdv.viz.chart;

import java.util.ArrayList;
import java.util.Collections;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * This is an optimizied version of the TimeSeries class. It adds methods to
 * support fast loading of large amounts of data.
 * 
 * @author Jason P. Hanley
 */
public class FastTimeSeries extends TimeSeries {

  /** serialization version identifier */
  private static final long serialVersionUID = -8280730280322014742L;

  public FastTimeSeries(String name, Class<?> timePeriodClass) {
    super(name, timePeriodClass);
  }
  
  /**
   * Set the maximum item age. Age items according to the latest time.
   * 
   * @param periods  the maximum item age
   * @param latest   the latest time to age by
   */
  public void setMaximumItemAge(long periods, long latest) {
    setMaximumItemAge(periods);
    removeAgedItems(latest, true);
  }
  
  /**
   * Does nothing. This class only supports aging items with an explicit
   * latest time.
   * 
   * @param  notify listeners
   */
  public void removeAgedItems(boolean notify) {}
  
  /**
   * Remove items that are older than the maximum item age relative to the
   * latest time provided. Optionally notify listeners that the series has
   * changed.
   * 
   * Note: This overides the version in the base class because it is not
   *       correct and throws exceptions when it shouldn't.
   * 
   * @param latest  the time to age items by
   * @param notify  if true, notify listeners if the series changes, otherwise
   *                don't notify listeners
   */
  public void removeAgedItems(long latest, boolean notify) {
    long minAge = latest - this.getMaximumItemAge();
    
    boolean removed = false;
    while (data.size() > 0 && getTimePeriod(0).getSerialIndex() <= minAge) {
      data.remove(0);
      removed = true;
    }
    
    if (removed && notify) {
      fireSeriesChanged();
    }
  }

  /**
   * Signal that a number of items will be added to the series.
   * 
   * This increases the capacity of this series to ensure it hold at least the
   * number of elements specified plus the current number of elements.
   * 
   * @param items  the number of items to be added
   */
  @SuppressWarnings("unchecked")
  public void startAdd(int items) {
    ((ArrayList)data).ensureCapacity(data.size()+items);
  }
  
  /**
   * Adds a data item to the series. If the time period is less than the last
   * time period, the item will not be added.
   *
   * @param period  the time period to add (<code>null</code> not permitted).
   * @param value  the new value.
   */
  @SuppressWarnings("unchecked")
  public void add(RegularTimePeriod period, double value) {
    TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
    int count = getItemCount();
    if (count == 0) {
      data.add(item);
    } else {
      RegularTimePeriod last = getTimePeriod(count-1);
      if (period.compareTo(last) > 0) {
        data.add(item);
      } else {
        int index = Collections.binarySearch(data, item);
        if (index < 0) {
          data.add(-index - 1, item);
        }
      }
    }      
  }
}