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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.RegularTimePeriod;

/**
 * A sequence of (x,y) data items that are timestamped. Only one data item is
 * allowed per time.
 * 
 * @author Jason P. Hanley
 */
public class XYTimeSeries extends Series implements Serializable {
  
  /** serialization version identifier */
  private static final long serialVersionUID = -5092511186726301050L;

  /**
   * The time period of the series.
   */
  private Class<?> timePeriodClass;
  
  /**
   * The list of data items.
   */
  private ArrayList<XYTimeSeriesDataItem> data;
  
  /**
   * The old age of a data item relative to the newest one or a given time.
   */
  private long maximumItemAge;

  /**
   * Creates an empty series.
   * 
   * @param name             the name of the series
   * @param timePeriodClass  the time period
   * @param chartViz TODO
   */
  public XYTimeSeries(String name, Class<?> timePeriodClass) {
    super(name);
    
    this.timePeriodClass = timePeriodClass;
    
    data = new ArrayList<XYTimeSeriesDataItem>();
    maximumItemAge = Long.MAX_VALUE;
  }
  
  /**
   * Get the time period for this series.
   * 
   * @return  the class of the time period
   */
  public Class<?> getTimePeriodClass() {
    return timePeriodClass;
  }
  
  /**
   * Call to optimize the addition of large amount of data items.
   * 
   * @param items  the number of item that will be added.
   */
  public void startAdd(int items) {
    data.ensureCapacity(data.size()+items);
  }
  
  /**
   * Add a data item and notify series change listeners.
   * 
   * @param period  the period of the data item
   * @param x       the x value of the data item
   * @param y       the y value of the data item.
   */
  public void add(RegularTimePeriod period, Number x, Number y) {
   add(period, x, y, true); 
  }

  /**
   * Add a data item. Optionally notify series change listeners.
   * 
   * @param period  the period of the data item
   * @param x       the x value of the data item
   * @param y       the y value of the data item
   * @param notify  if true notify series change listeners
   */
  public void add(RegularTimePeriod period, Number x, Number y, boolean notify) {
    add(new XYTimeSeriesDataItem(period, x, y), notify);
  }
  
  /**
   * Add the data item and notify series change listeners.
   * 
   * @param item  the data item to add
   */
  public void add(XYTimeSeriesDataItem item) {
    add(item, true);
  }
  
  /**
   * Add the data item. Optionally notify series change listeners.
   * 
   * @param item    the data item to add
   * @param notify  if true notify series change listeners
   */
  @SuppressWarnings("unchecked")
  public void add(XYTimeSeriesDataItem item, boolean notify) {
    if (item == null) {
      throw new IllegalArgumentException("Null 'item' argument.");
    }
    
    if (!item.getPeriod().getClass().equals(timePeriodClass)) {
      throw new SeriesException("Invalid time period class for this series.");
    }
    
    boolean added = false;
    int count = getItemCount();
    if (count == 0) {
      data.add(item);
      added = true;
    } else {
      RegularTimePeriod last = getDataItem(count-1).getPeriod();
      if (item.getPeriod().compareTo(last) > 0) {
        data.add(item);
        added = true;
      } else {
        int index = Collections.binarySearch(data, item);
        if (index < 0) {
          data.add(-index - 1, item);
          added = true;
        }          
      }
    }
    
    if (added && notify) {
      fireSeriesChanged();
    }
  }
  
  /**
   * Update the values at the time period.
   * 
   * @param period  the time period to update
   * @param x       the new x value
   * @param y       the new y value
   */
  public void update(RegularTimePeriod period, Number x, Number y) {
    XYTimeSeriesDataItem item = getDataItem(period);
    if (item != null) {
      item.setValue(x, y);
      
      fireSeriesChanged();
    } else {
      throw new SeriesException("Period does not exist.");
    }
  }
  
  /**
   * Delete the data item at the time period.
   * 
   * @param period  the time period at which to delete the data item
   */
  public void delete(RegularTimePeriod period) {
    int index = getIndex(period);
    data.remove(index);
    fireSeriesChanged();
  }

  /**
   * Remove all data items from the series.
   */
  public void clear() {
    if (data.size() > 0) {
      data.clear();
      fireSeriesChanged();
    }
  }
  
  /**
   * Get the data item at the specified index.
   * 
   * @param index  the index at which the data item is located
   * @return       the data item, or null if there is none at the index
   */
  public XYTimeSeriesDataItem getDataItem(int index) {
    return data.get(index);
  }
  
  /**
   * Get the data item at the specified time.
   * 
   * @param period  the time of the data item
   * @return        the data item or null if there is no data item at the time
   */
  public XYTimeSeriesDataItem getDataItem(RegularTimePeriod period) {
    int index = getIndex(period);
    if (index >= 0) {
      return data.get(index);
    } else {
      return null;
    }
  }
  
  /**
   * Get the index of the data item at the specified time.
   * 
   * @param period  the time to look for the data item
   * @return        the index of the data item, or a negative number if not
   */
  public int getIndex(RegularTimePeriod period) {
    if (period == null) {
      throw new IllegalArgumentException("Null 'period' argument");
    }
    
    XYTimeSeriesDataItem dummy = new XYTimeSeriesDataItem(period);
    return Collections.binarySearch(data, dummy);
  }
  
  /**
   * Get the number of data items in this series.
   * 
   * @return  the number of data items in this series
   */
  public int getItemCount() {
    return data.size();
  }
  
  /**
   * Get a read-only list of the data item in this series.
   * 
   * @return  a list of data item in this series
   */
  public List<XYTimeSeriesDataItem> getItems() {
    return Collections.unmodifiableList(data);
  }
  
  /**
   * Get the maximum ago of a data item.
   * 
   * @return  the maximum age
   */
  public long getMaximumItemAge() {
    return maximumItemAge;
  }
  
  /**
   * Set the maximum ago of a data item.
   * 
   * @param periods  the maximum age
   */
  public void setMaximumItemAge(long periods) {
    if (periods < 0) {
      throw new IllegalArgumentException("Negative 'periods' argument.");
    }
    maximumItemAge = periods;
    removeAgedItems();       
  }
  
  /**
   * Set the maximum age of a data item. Age items according to the latest
   * time
   * 
   * @param periods  the maximum age
   * @param latest   the latest time to age by
   */
  public void setMaximumItemAge(long periods, long latest) {
    if (periods < 0) {
      throw new IllegalArgumentException("Negative 'periods' argument.");
    }
    maximumItemAge = periods;
    removeAgedItems(latest);       
  }    
  
  /**
   * Remove data items that exceed the maximum age and notify series change
   * listeners if any data items are aged.
   */
  public void removeAgedItems() {
    removeAgedItems(true);
  }
  
  /**
   * Remove data items that exceed the maximum age. Optionally notify series
   * change listeners if any data items are aged.
   * 
   * @param notify  if true notify series change listeners
   */
  public void removeAgedItems(boolean notify) {
    int items = getItemCount(); 
    if (items > 0) {
      removeAgedItems(getDataItem(items-1).getPeriod().getSerialIndex(), notify);
    }
  }
  
  /**
   * Remove data items that exceed the maximum age starting at the given time.
   * Notify series change listeners if any data items are aged.
   * 
   * @param latest  the time to start at
   */
  public void removeAgedItems(long latest) {
    removeAgedItems(latest, true);
  }
  
  /**
   * Remove data items that exceed the maximum age starting at the given time.
   * Optionally notify series change listeners if any data items are aged.
   * 
   * @param latest  the time to start at
   * @param notify  if true notify series change listenerss
   */
  public void removeAgedItems(long latest, boolean notify) {
    boolean removed = false;
    
    long minimumItemAge = latest - maximumItemAge;
    while (getItemCount() > 0 && getDataItem(0).getPeriod().getSerialIndex() < minimumItemAge) {
      data.remove(0);
      removed = true;
    }
    
    if (notify && removed) {
      fireSeriesChanged();
    }
  }
}