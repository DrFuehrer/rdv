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

package org.nees.buffalo.rdv.auth;

import edu.ucsd.auth.GridAuth;

/**
 * A wrapper for GridAuth to conform to the Authentication interface.
 * 
 * @author Jason P. Hanley
 */
public class GridAuthentication implements Authentication {
  /** the GridAuth object being wrapped */
  private final GridAuth gridAuth;
  
  /**
   * Create an authentication object for GridAuth.
   */
  public GridAuthentication() {
    this(null);
  }

  /**
   * Create an authentication object for GridAuth. Using the specified GridAuth
   * instance.
   */
  public GridAuthentication(String hostName) {
    super();
    
    gridAuth = new GridAuth();
    
    if (hostName != null) {
      gridAuth.setServiceHandler("https://" + hostName + "/cgi-bin/handler.cgi");
    }
  }
  
  /**
   * Login with a session hash.
   * 
   * @param session  the session hash
   * @return         true if the login is successful, false otherwise
   */
  public boolean login(String session) {
    return gridAuth.login(session);
  }

  /**
   * Login with a username and password.
   * 
   * @param username  the username
   * @param password  the password for the username
   * @return          true if the login is successful, false otherwise
   */
  public boolean login(String username, String password) {
    return gridAuth.login(username, password);
  }

  /**
   * Logout and destroy the session.
   * 
   * @return  true if the logout is successful, false otherwise
   */
  public boolean logout() {
    return gridAuth.logout();
  }

  /**
   * Get the value associated with the specified key for this authentication.
   * The session hash may be accessed with the "session" key.
   * 
   * @param key  the key to the value
   * @return     the value for the specified key, or null if the key was not
   *             found
   */
  public String get(String key) {
    return gridAuth.get(key);
  }
}