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

import org.jfree.data.time.RegularTimePeriod;

/**
 * Represents one timestamped (x,y) data item.
 * 
 * @author Jason P. Hanley
 */
public class XYTimeSeriesDataItem implements Cloneable, Comparable<XYTimeSeriesDataItem>, Serializable {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 941152355835111553L;

  /** The time period */
  private final RegularTimePeriod period;
  
  /** The x value */
  private Number x;
  
  /** The y value */
  private Number y;
  
  /**
   * Construct a new data item with the time.
   * 
   * @param period  the time for the data
   */
  public XYTimeSeriesDataItem(RegularTimePeriod period) {
    this(period, null, null);
  }
  
  /**
   * Construct a new data item with timestamped (x,y) values.
   * 
   * @param period  the time for the data
   * @param x       the x value
   * @param y       the y value
   */
  public XYTimeSeriesDataItem(RegularTimePeriod period, Number x, Number y) {
    if (period == null) {
      throw new IllegalArgumentException("Null 'period' argument.");   
    }
    
    this.period = period;
    this.x = x;
    this.y = y;
  }
  
  /**
   * Get a copy of this data item.
   */
  public Object clone() {
    return new XYTimeSeriesDataItem(period, x.doubleValue(), y.doubleValue());
  }

  /**
   * Compare this data item to another. This only compares the time of the
   * data item.
   * 
   * @param o  the data item to compare to
   * @return   an integer indicating the order of this data item relative to
   *           the other
   */
  @SuppressWarnings("unchecked")
  public int compareTo(XYTimeSeriesDataItem d) {
    return period.compareTo(d.getPeriod());
  }
  
  /**
   * Test if this object is equal to another.
   * 
   * @return  true if the object is equal, false otherwise
   */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    if (o instanceof XYTimeSeriesDataItem) {
      XYTimeSeriesDataItem d = (XYTimeSeriesDataItem)o;
      return period.equals(d.getPeriod()) && x.equals(d.getX()) && y.equals(d.getY());
    }
    
    return false;
  }
  
  /**
   * Get the hash code for this object.
   */
  public int hashCode() {
    int result;
    result = period.hashCode();
    result = 29 * result + (x != null ? x.hashCode() : 0);
    result = 29 * result + (y != null ? y.hashCode() : 0);
    return result;
  }
  
  /**
   * Get the time period for this data item.
   * 
   * @return  the time period
   */
  public RegularTimePeriod getPeriod() {
    return period;
  }
  
  /**
   * Get the x value.
   * 
   * @return  the x value
   */
  public Number getX() {
    return x;
  }
  
  /**
   * Set the x value.
   * 
   * @param x  the new value of x
   */
  public void setX(Number x) {
    this.x = x;
  }
  
  /**
   * Get the y value.
   * 
   * @return  the y value
   */
  public Number getY() {
    return y;
  }
  
  /**
   * Set the y value
   * @param y  the new value of y
   */
  public void setY(Number y) {
    this.y = y;
  }
  
  /**
   * Set the x and y value.
   * 
   * @param x  the new value of x
   * @param y  the new value of y
   */
  public void setValue(Number x, Number y) {
    this.x = x;
    this.y = y;
  }
}