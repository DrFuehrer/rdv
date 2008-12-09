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

import org.jfree.chart.axis.DateAxis;

/**
 * A date axis with a fixed range used for auto adjusting.
 * 
 * @author Jason P. Hanley
 */
public class FixedAutoAdjustRangeDateAxis extends DateAxis {

  /** serialization version identifier */
  private static final long serialVersionUID = -333924991463680879L;
  
  /** the lower range for fixed auto adjusting */
  private double lowerAutoAdjustRange;
  
  /** the upper range for fixed auto adjusting */
  private double upperAutoAdjustRange;

  /**
   * Creates a fixed auto adjust date axis with no fixed auto adjust range.
   */
  public FixedAutoAdjustRangeDateAxis() {
    super();
    
    lowerAutoAdjustRange = -1;
    upperAutoAdjustRange = -1;
  }

  /**
   * Auto adjust the range. If a fixed auto adjust range is set, this is used.
   * Otherwise the range is set to ensure that all data is visible. 
   */
  @Override
  protected void autoAdjustRange() {
    if (lowerAutoAdjustRange >= 0 && upperAutoAdjustRange > 0) {
      setRange(lowerAutoAdjustRange, upperAutoAdjustRange);
    } else {
      super.autoAdjustRange();      
    }
  }
  
  /**
   * Gets the lower fixed auto adjust range.
   * 
   * @return  the lower range
   */
  public double getLowerAutoAdjustRange() {
    return lowerAutoAdjustRange;
  }
  
  /**
   * Gets the upper fixed auto adjust range.
   * 
   * @return  the upper range
   */
  public double getUpperAutoAdjustRange() {
    return upperAutoAdjustRange;
  }
  
  /**
   * Sets the fixed auto range values. To disable the fixed auto adjust range,
   * set either lower or upper to a negative value.
   * 
   * @param lower  the lower range
   * @param upper  the upper range
   */
  public void setAutoAdjustRange(double lower, double upper) {
    if (lower < 0 || upper < 0) {
      lower = -1;
      upper = -1;
    } else if (lower >= upper) {
      throw new IllegalArgumentException("Requires 'lower' < 'upper'.");
    }
    
    if (lower == lowerAutoAdjustRange && upper == upperAutoAdjustRange) {
      return;
    }

    lowerAutoAdjustRange = lower;
    upperAutoAdjustRange = upper;
    
    if (isAutoRange()) {
      autoAdjustRange();
    }
  }

}