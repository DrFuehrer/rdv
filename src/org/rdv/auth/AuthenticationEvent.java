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

package org.rdv.auth;

import java.util.EventObject;

/**
 * A class to describe an authentication event.
 * 
 * @author Jason P. Hanley
 *
 */
public class AuthenticationEvent extends EventObject {

  /** serialization version identifier */
  private static final long serialVersionUID = -1045113985286303166L;

  /** the authentication */
  private final Authentication authentication;
  
  /**
   * Create the authentication event.
   * 
   * @param authenticationManager  the authentication manager            
   * @param authentication         the authentication
   */
  public AuthenticationEvent(AuthenticationManager authenticationManager, Authentication authentication) {
    super(authenticationManager);
    
    this.authentication = authentication;
  }
  
  /**
   * Get the source of the authenitcation event. This is the authentication
   * manager that the event is from.
   * 
   * @return  the authentication manager for this event
   */
  public AuthenticationManager getSource() {
    return (AuthenticationManager)super.getSource();
  }
  
  /**
   * Get the authentication object.
   * 
   * @return  the authentication
   */
  public Authentication getAuthentication() {
    return authentication;
  }  
}