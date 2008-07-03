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

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * A class for port knocking.
 * 
 * @author  Jason P. Hanley
 * @see     <a href="http://www.portknocking.org/">port knocking</a>
 */
public final class PortKnock {

  /** the default delay after a knock */
  private static final int DEFAULT_DELAY = 100;

  /**
   * This class can not be instantiated and it's constructor always throws an
   * exception.
   */
  private PortKnock() {
    throw new UnsupportedOperationException("This class can not be instantiated.");
  }

  /**
   * Knock on the specified ports.
   * 
   * @param host          the host to knock on
   * @param ports         the ports to knock
   * @throws IOException  if there is an error knocking
   */
  public static void knock(String host, int[] ports) throws IOException {
    knock(host, ports, DEFAULT_DELAY);
  }

  /**
   * Knock on the specified ports with the specified delay after each knock.
   * 
   * @param host          the host to knock on
   * @param ports         the ports to knock
   * @param delay         the delay (in milliseconds) after each knock
   * @throws IOException  if there is an error knocking
   */
  public static void knock(String host, int[] ports, int delay) throws IOException {
    for (int port : ports) {
      try {
        Socket socket = new Socket(host, port);
        socket.close();
      } catch (ConnectException e) { e.printStackTrace(); }

      try { Thread.sleep(delay); } catch (InterruptedException e) {}
    }
  }
  
}