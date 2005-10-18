/*
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  o The above copyright notice and this permission notice shall be included
 *    in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nees.buffalo.rdv.data;

/**
 * Thrown when an extention is not found. This may happen if:
 * 
 * <ul>
 *   <li>the extension name doesn't exist</li>
 *   <li>the extension class can't be found</li>
 * </ul>
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class ExtensionNotFoundException extends Exception {
  public ExtensionNotFoundException() {
    super();
  }
  
  public ExtensionNotFoundException(String message) {
    super(message);
  }
  
  public ExtensionNotFoundException(Throwable cause) {
    super(cause);
  }
  
  public ExtensionNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
