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

import java.util.ArrayList;
import java.util.List;

/**
 * A class to manage an authentication.
 * 
 * @author Jason P. Hanley
 */
public class AuthenticationManager {
  /** the instance of this class */
  private static AuthenticationManager instance;
  
  /** the authentication */
  private Authentication authentication;
  
  /** a list of authentication listeners */
  private final List<AuthenticationListener> listeners;
  
  /**
   * Creates the authentication manager.
   */
  protected AuthenticationManager() {
    listeners = new ArrayList<AuthenticationListener>();
  }
  
  /**
   * Returns the instance of the authentication manager.
   * 
   * @return  the authentication manager
   */
  public static AuthenticationManager getInstance() {
    if (instance == null) {
      instance = new AuthenticationManager();
    }
    
    return instance;
  }

  /**
   * Get the authentication. This will return null if there is none.
   * 
   * @return  the current authentication
   */
  public Authentication getAuthentication() {
    return authentication;
  }
  
  /**
   * Set the current authentication. If an authentication is already set, this
   * will log it out. A null authentication will set the current authentication
   * to nothing.
   * 
   * @param authentication  the new authentication
   */
  public void setAuthentication(Authentication authentication) {
    if (this.authentication != authentication) {
      if (this.authentication != null) {
        this.authentication.logout();
      }

      this.authentication = authentication;
      fireAuthenticationChanged();
    }
  }
  
  /**
   * Inform authentication listeners that the authentication has changed.
   */
  protected void fireAuthenticationChanged() {
    AuthenticationEvent event = new AuthenticationEvent(this, authentication);
    
    for (AuthenticationListener listener : listeners) {
      listener.authenticationChanged(event);
    }
  }
  
  /**
   * Adds a listener for authentication changes.
   * 
   * @param listener  the authentication listener
   */
  public void addAuthenticationListener(AuthenticationListener listener) {
    if (listeners.contains(listener)) {
      return;
    }
    
    listeners.add(listener);
  }
  
  /**
   * Removes a listener for authenitcation changes.
   * 
   * @param listener  the authentication listener
   */
  public void removeAuthenticationListener(AuthenticationListener listener) {
    listeners.remove(listener);
  }
}