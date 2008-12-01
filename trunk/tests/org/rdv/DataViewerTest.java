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

package org.rdv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * A unit test for the RDV application class.
 *  
 * @author Jason P. Hanley
 */
public class DataViewerTest extends TestCase {
  public void testParseTimestamp() throws ParseException {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    
    String[] timestamps = {
        // timestamp to test           // what it should be in UTC
        "2008-01-01T01:01:01.001UTC",  "2008-01-01T01:01:01.001",
        "20081112142239000EST",        "2008-11-12T19:22:39.000",
        "20080912142239000EST",        "2008-09-12T19:22:39.000",
        "20080912142239000EDT",        "2008-09-12T18:22:39.000",
        "20081112142239000PST",        "2008-11-12T22:22:39.000",
        "20081112142239000Z",          "2008-11-12T14:22:39.000",
        "20081112142239000",           "2008-11-12T19:22:39.000",
        "20080912142239000",           "2008-09-12T18:22:39.000"
    };
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    
    for (int i=0; i<timestamps.length; i+=2) {
      double time = DataViewer.parseTimestamp(timestamps[i]);
      Date date = new Date(Math.round(time*1000d));
      String string = df.format(date);
      assertEquals(timestamps[i+1], string);
    }
  }
}