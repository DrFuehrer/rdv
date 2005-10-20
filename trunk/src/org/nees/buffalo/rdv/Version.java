/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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

package org.nees.buffalo.rdv;

/**
 * A class to encapsulate the version of the application.
 * The version is displayed in the format of major.minor.release.build.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public final class Version {
	/**
	 * The major version number.
	 * 
	 * @since  1.0
	 */
	public static final int major = 1;
	
	/**
	 * The minor version number
	 * 
	 * @since  1.0 
	 */
	public static final int minor = 3;
	
	/**
	 * The release number.
	 * 
	 * @since  1.0
	 */
	public static final int release = 0;
	
	/**
	 * The build string.
	 *
	 * @since  1.2
	 */
	public static final String build = " $Revision";
	
	/**
	 * This class can not be instantiated and it's constructor
	 * always throws an exception.
	 */
	private Version() {
		throw new UnsupportedOperationException("This class can not be instantiated.");
	}
}
