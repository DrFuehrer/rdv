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

import org.jfree.data.xy.AbstractXYDataset;

/**
 * A collection of XYTimeSeries objects that form a dataset.
 * 
 * @author Jason P. Hanley
 */
public class XYTimeSeriesCollection extends AbstractXYDataset implements Serializable {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 4352896561682820035L;
  
  /** List of series */
  private List<XYTimeSeries> data;
  
  /**
   * Create an empty dataset.
   * @param chartViz TODO
   */
  public XYTimeSeriesCollection() {
    super();
    
    data = new ArrayList<XYTimeSeries>();
  }

  /**
   * Get the number of data series in this collection
   * 
   * @return  the number of data series
   */
  public int getSeriesCount() {
    return data.size();
  }
  
  /**
   * Get a list of all data series in this collection. This list is read-only.
   * 
   * @return  a list of data series
   */
  public List<XYTimeSeries> getSeries() {
    return Collections.unmodifiableList(this.data);
  }    
  
  /**
   * Get the data series at the specified index.
   * 
   * @param series  the index of the series
   * @return        if found, the series, null otherwise
   */
  public XYTimeSeries getSeries(int series) {
    if ((series < 0) || (series >= getSeriesCount())) {
      throw new IllegalArgumentException("The 'series' argument is out of bounds (" + series + ").");
    }

    return data.get(series);      
  }
  
  /**
   * Get the data series with the specified key.
   * 
   * @param key  the key to the data series
   * @return     if foud, the data series, null otherwise
   */
  public XYTimeSeries getSeries(String key) {
    for (XYTimeSeries xyTimeSeries : data) {
      Comparable k = xyTimeSeries.getKey();
      if (k != null && k.equals(key)) {
        return xyTimeSeries;
      }
    }
    
    return null;
  }

  /**
   * Get the key for the specified series index.
   * @param key  the index to the data series
   * @return     if found, the key for the series, null otherwise
   */
  public Comparable getSeriesKey(int series) {
    return getSeries(series).getKey();
  }
  
  /**
   * Add the series to the collection.
   * 
   * @param series  the series to add
   */
  public void addSeries(XYTimeSeries series) {
    addSeries(getSeriesCount(), series);
  }
  
  /**
   * Add the series to the collection at the specified index.
   * 
   * @param index   the index at which to add the series
   * @param series  the series to add
   */
  public void addSeries(int index, XYTimeSeries series) {
    if (series == null) {
      throw new IllegalArgumentException("Null 'series' argument.");
    }
    data.add(index, series);
    series.addChangeListener(this);
    fireDatasetChanged();      
  }
  
  /**
   * Remove the series from the collection.
   * 
   * @param series  the series to remove
   */
  public void removeSeries(XYTimeSeries series) {
    if (series == null) {
      throw new IllegalArgumentException("Null 'series' argument.");
    }
    data.remove(series);
    series.removeChangeListener(this);
    fireDatasetChanged();      
  }
  
  /**
   * Remove the series, specified by the, index from the collection.
   * 
   * @param index  the index of the series
   */
  public void removeSeries(int index) {
    XYTimeSeries series = getSeries(index);
    if (series != null) {
      removeSeries(series);
    }
  }

  /**
   * Get the number of data items in the specified series
   * 
   * @param series  the index to the data series
   */
  public int getItemCount(int series) {
    return getSeries(series).getItemCount();
  }

  /**
   * Get the x value of the series at the specified data item.
   * 
   * @param series  the index of the data series
   * @param item    the index of the data item
   * @return        the x value
   */
  public Number getX(int series, int item) {
    XYTimeSeries xyTimeSeries = getSeries(series);
    return xyTimeSeries.getDataItem(item).getX();
  }

  /**
   * Get the x yalue of the series at the specified data item.
   * 
   * @param series  the index of the data series
   * @param item    the index of the data item
   * @return        the y value
   */
  public Number getY(int series, int item) {
    XYTimeSeries xyTimeSeries = getSeries(series);
    return xyTimeSeries.getDataItem(item).getY();
  }
}