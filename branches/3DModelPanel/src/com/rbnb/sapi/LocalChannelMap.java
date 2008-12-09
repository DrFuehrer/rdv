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

package com.rbnb.sapi;

import com.rbnb.api.Rmap;

/**
 * A class that extends <code>ChannelMap</code> to allow data to be put into it
 * after it has been filled with data from the server with a call to
 * <code>Sink.Fetch</code>.
 * <p>
 * The local data must be added after the call to <code>Fetch</code>. When all
 * the local data has been added, call {{@link #mergeLocalData()} to combine it.
 * 
 * @author  Jason P. Hanley
 * @see     ChannelMap
 * @see     Sink#Fetch(long)
 */
public class LocalChannelMap extends ChannelMap {

  /** serialization version identifier */
  private static final long serialVersionUID = 2270823913191443480L;

  /**
   * Merges data fetched by the server with data put afterwards.
   * 
   * @throws Exception  if there is an error merging the data
   */
  public void mergeLocalData() throws Exception {
    Rmap rmap = produceOutput();
    
    Clear();
    
    processResult(rmap, true, false);
  }

}