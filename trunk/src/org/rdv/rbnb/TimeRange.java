/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

/**
 * A time range. This is a range in time specified by a start and end time.
 */
public class TimeRange implements Comparable<TimeRange> {
  /** The start of the time range */
  public double start;
  
  /** The end of the time range */
  public double end;
  
  /**
   * Create a time range.
   * 
   * @param start  the start of the time range
   * @param end    the end of the time range
   */
  public TimeRange(double start, double end) {
    this.start = start;
    this.end = end;
  }
  
  /**
   * The length of the time range. Simply end-start.
   * 
   * @return  the length of the time range
   */
  public double length() {
    return end-start;
  }
  
  /**
   * See if the time is within the time range.
   * 
   * @param time  the time to check
   * @return      true if the time is within the time range, false otherwise
   */
  public boolean contains(double time) {
    return ((time >= start) && (time <= end));
  }
  
  /**
   * Compare time ranges. This is based first on their start, and then on
   * their end (if needed).
   * 
   * @param d  the time range to compare with
   * @return   0 if they are the same, -1 if this is less than the other, and
   *           1 if this is greater than the other.
   */
  public int compareTo(TimeRange t) {
    if (start == t.start) {
      if (end == t.end) {
        return 0;
      } else if (end < t.end) {
        return -1;
      } else {
        return 1;
      }
    } else if (start < t.start) {
      return -1;
    } else {
      return 1;
    }
  }
}