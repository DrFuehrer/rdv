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

package org.rdv.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 * A class to format numbers using engineering notation, where the exponent is a
 * multiple of three. A unit can optionally be appended to the formatted number.
 * 
 * @author Jason P. Hanley
 * @see    DecimalFormat
 */
public class EngineeringFormat extends DecimalFormat {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 3656432492843608279L;

  /** the pattern to format the number in engineering format */
  private final static String pattern = "##0.###E0";
  
  /** the unit for the number */
  private String unit;
  
  /**
   * Creates an EngineeringFormat with no unit.
   */
  public EngineeringFormat() {
    super(pattern);
  }

  /**
   * Gets the unit.
   * 
   * @return  the unit, or null if there is none
   * @see     #setUnit(String)
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Sets the unit. To set no unit, use null.
   * 
   * @param unit  the new unit
   * @see         #getUnit()
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }


  @Override
  public StringBuffer format(double number, StringBuffer buffer, FieldPosition fieldPosition) {
    return appendUnit(super.format(number, buffer, fieldPosition));
  }

  @Override
  public StringBuffer format(long number, StringBuffer buffer, FieldPosition fieldPosition) {
    return appendUnit(super.format(number, buffer, fieldPosition));
  }

  /**
   * Appends a unit to the end of the buffer. If there is no unit, this method
   * does nothing.
   * 
   * @param buffer  the buffer to append the unit to
   * @return        the buffer with the unit appened to
   */
  private StringBuffer appendUnit(StringBuffer buffer) {
    if (unit != null && unit.length() > 0) {
      buffer.append(' ').append(unit);
    }
    
    return buffer;
  }

}