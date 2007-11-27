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

package org.nees.buffalo.rdv.rbnb;

import junit.framework.TestCase;

/**
 * Unit tests for the local RBNB server singleton class.
 * 
 * @author Jason P. Hanley
 */
public class LocalServerTest extends TestCase {
  /**
   * Test getting the singleton instace of this class.
   */
  public void testGetInstance() {
    LocalServer instance1 = LocalServer.getInstance();
    assertNotNull(instance1);
    
    LocalServer instance2 = LocalServer.getInstance();
    assertNotNull(instance2);
    
    assertSame(instance1, instance2);
  }

  /**
   * Test starting the server.
   */
  public void testStartServer() throws Exception {
    LocalServer server = LocalServer.getInstance();
    
    server.startServer();
    assertTrue(server.isServerRunning());
  }

  /**
   * Test stopping the server.
   */
  public void testStopServer() throws Exception {
    LocalServer server = LocalServer.getInstance();
    
    server.startServer();
    
    server.stopServer();
    assertFalse(server.isServerRunning());
  }

  /**
   * Test to see if the server is running.
   */
  public void testIsServerRunning() throws Exception {
    LocalServer server = LocalServer.getInstance();
    assertFalse(server.isServerRunning());
    
    server.startServer();
    assertTrue(server.isServerRunning());

    server.stopServer();
    assertFalse(server.isServerRunning());
  }
}