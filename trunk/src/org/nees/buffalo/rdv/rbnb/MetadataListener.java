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
 * $URL: svn+ssh://jphanley@code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/rbnb/MetadataListener.java $
 * $Revision: 319 $
 * $Date: 2005-11-16 15:13:34 -0500 (Wed, 16 Nov 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.rbnb;

import com.rbnb.sapi.ChannelTree;

/**
 * A listener interface to receive the channel tree and the associated metadata.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface MetadataListener {
	
	/**
	 * Invoked when the channel list is updated. This will contain
	 * an up-to-date list of channels and their associated metadata.
	 * This information is encapsulated in the <code>ChannelTree</code>.
	 * 
	 * @param channelTree  The channel tree
	 * @since              1.3
	 */
	public void channelTreeUpdated(ChannelTree channelTree);
}
