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

package org.rdv.viz.spectrum;

import org.jfree.data.xy.XYSeries;

/**
 * A modified {@code XYSeries} with a clear method that won't call the series
 * changed listener.
 * 
 * @author Jason P. Hanley
 */
public class SpectrumXYSeries extends XYSeries {
  
  /** serialization version identifier */
  private static final long serialVersionUID = -2479602262293208904L;

  public SpectrumXYSeries(Comparable<?> key, boolean autoSort,
      boolean allowDuplicateXValues) {
    super(key, autoSort, allowDuplicateXValues);
  }

  /**
   * Clears the series and optionally notify series changed listeners.
   * 
   * @param notify  if true, notify series changed listeners, otherwise don't
   * @see #clear()
   * @see #fireSeriesChanged()
   */
  public void clear(boolean notify) {
    if (this.data.size() > 0) {
      this.data.clear();
      if (notify) {
        fireSeriesChanged();
      }
    }
  }
}