package org.nees.rbnb.marker;
/**
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * Please see copywrite information at the end of this file.
 * @since 060120
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.marker.NeesEvent;

import junit.framework.TestCase;
import junit.framework.TestResult;

/** A class that is a junit unit test to validate
* @see org.nees.rbnb.marker.NeesEvent.java
* and its related applications. */
public class TestNeesEventXML extends TestCase {
  
  static Log log = LogFactory.getLog (TestNeesEventXML.class.getName ());
  private NeesEvent testEvent1;
  private NeesEvent testEvent2;
  public String testString = "junitTest";

  public TestNeesEventXML (String nme) {
    super (nme);
    this.testEvent1 = new NeesEvent ();
    this.testEvent2 = new NeesEvent ();
  } // NeesEventTest ()

  public void runTest () {
    testXML ();
  }
    
  public void testXML () {
    String xmlFrom1 = "";
    String gotFrom2 = "";
    try {
      this.testEvent1.setProperty ("annotation", testString);
      xmlFrom1 = this.testEvent1.toEventXmlString ();
      this.testEvent2.setFromEventXml (xmlFrom1);
      gotFrom2 = this.testEvent2.getProperty ("annotation");
      assertTrue (testString.compareTo (gotFrom2) == 0);
    } catch (Exception e) {
      e.printStackTrace ();
    }
  } // testXML ()
} // class

 /** Copyright (c) 2005, Lawrence J. Miller and NEESit
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 *   * Neither the name of the San Diego Supercomputer Center nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
