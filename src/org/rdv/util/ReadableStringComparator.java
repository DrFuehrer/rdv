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

package org.rdv.util;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to compare strings in a human readable way. This will look for
 * numbers embedded in strings and take then into consideration when comparing.
 * 
 * @author Jason P. Hanley
 */
public class ReadableStringComparator implements Comparator<String> {
  
  /** a pattern to match a number in the string */
  private static Pattern p = Pattern.compile("(\\D*)(\\d+)(\\D*)");
  
  public int compare(String s1, String s2) {
    s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();
    
    if (s1.equals(s2)) {
      return 0;  
    }
    
    Matcher m1 = p.matcher(s1);
    Matcher m2 = p.matcher(s2);
    
    if (m1.matches() && m2.matches() &&
        m1.group(1).equals(m2.group(1)) &&
        m1.group(3).equals(m2.group(3))) {
      long l1 = Long.parseLong(m1.group(2));
      long l2 = Long.parseLong(m2.group(2));
      return l1<l2?-1:1;
    } else {
      return s1.compareTo(s2);
    }
  }
  
}