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

package org.rdv.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rdv.DataViewer;
import org.rdv.rbnb.RBNBUtilities;

import com.rbnb.sapi.ChannelTree;

/**
 * Renderer for cells in a channel tree.
 * 
 * @author Jason P. Hanley
 */
public class ChannelTreeCellRenderer extends DefaultTreeCellRenderer {

  /** serialization version identifier */
  private static final long serialVersionUID = -8564821584949049965L;
  
  private static final Icon SERVER_ICON = DataViewer.getIcon("icons/server.gif");
  
  private static final Icon FOLDER_OPEN_ICON = DataViewer.getIcon("icons/folder_open.gif");
  
  private static final Icon FOLDER_ICON = DataViewer.getIcon("icons/folder.gif");
  
  private static final Icon DATA_ICON = DataViewer.getIcon("icons/data.gif");
  
  private static final Icon JPEG_ICON = DataViewer.getIcon("icons/jpeg.gif");
  
  private static final Icon TEXT_ICON = DataViewer.getIcon("icons/text.gif");
  
  private static final Icon FILE_ICON = DataViewer.getIcon("icons/file.gif");
  
  public ChannelTreeCellRenderer() {
    super();
    
    setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    
    if (value == tree.getModel().getRoot()) {
      return this;
    }
    
    ChannelTree.Node node = (ChannelTree.Node)value;
    ChannelTree.NodeTypeEnum type = node.getType();
    String mime = RBNBUtilities.fixMime(node.getMime(), node.getFullName());
    
    setText(node.getName());
    
    if (type == ChannelTree.SERVER) {
      setIcon(SERVER_ICON);
    } else if (type == ChannelTree.FOLDER ||
               type == ChannelTree.SOURCE ||
               type == ChannelTree.PLUGIN) {
      if (expanded) {
        setIcon(FOLDER_OPEN_ICON);
      } else {
        setIcon(FOLDER_ICON);
      }
    } else if (type == ChannelTree.CHANNEL) {
      if (mime.equals("application/octet-stream")) {
        setIcon(DATA_ICON);
      } else if (mime.equals("image/jpeg") || mime.equals("video/jpeg")) {
        setIcon(JPEG_ICON);
      } else if (mime.equals("text/plain")) {
        setIcon(TEXT_ICON);
      } else {
        setIcon(FILE_ICON);
      }
    }

    return this;
  }
  
}