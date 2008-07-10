/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * A transferable for a list of channels.
 * 
 * @author Jason P. Hanley
 */
public class ChannelListTransferable implements Transferable {
  /** the data flavor */
  private final DataFlavor dataFlavor;
  
  /** the list of data files */
  private final List<String> channels;
  
  /**
   * Create a transferable from a list of channels.
   * 
   * @param channels  the list of channels
   */
  public ChannelListTransferable(List<String> channels) {
    this.channels = channels;
    
    dataFlavor = new ChannelListDataFlavor();
  }

  /**
   * Gets the transfer data.
   * 
   * @param df  the data flavor of the transferable
   */
  public Object getTransferData(DataFlavor df) throws IOException, UnsupportedFlavorException {
    if (!dataFlavor.match(df)) {
      throw new UnsupportedFlavorException(df);
    }
    
    return channels;
  }

  /**
   * Gets a list of data flavors this transferable supports.
   */
  public DataFlavor[] getTransferDataFlavors() {
    DataFlavor[] dataFlavors = { dataFlavor };
    return dataFlavors;
  }

  /**
   * Returns whether or not the specified data flavor is supported by this
   * transferable.
   * 
   * @param df  the data flavor to check
   */
  public boolean isDataFlavorSupported(DataFlavor df) {
    return dataFlavor.match(df);
  }
}