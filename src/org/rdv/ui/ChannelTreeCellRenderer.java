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

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    
    if (value == tree.getModel().getRoot()) {
      return c;
    }
    
    ChannelTree.Node node = (ChannelTree.Node)value;
    ChannelTree.NodeTypeEnum type = node.getType();
    String mime = RBNBUtilities.fixMime(node.getMime(), node.getFullName());
    
    setText(node.getName());
    
    if (type == ChannelTree.SERVER) {
      setIcon(DataViewer.getIcon("icons/server.gif"));
    } else if (type == ChannelTree.FOLDER ||
               type == ChannelTree.SOURCE ||
               type == ChannelTree.PLUGIN) {
      if (expanded) {
        setIcon(DataViewer.getIcon("icons/folder_open.gif"));
      } else {
        setIcon(DataViewer.getIcon("icons/folder.gif"));
      }
    } else if (type == ChannelTree.CHANNEL) {
      if (mime.equals("application/octet-stream")) {
        setIcon(DataViewer.getIcon("icons/data.gif"));
      } else if (mime.equals("image/jpeg") || mime.equals("video/jpeg")) {
        setIcon(DataViewer.getIcon("icons/jpeg.gif"));
      } else if (mime.equals("text/plain")) {
        setIcon(DataViewer.getIcon("icons/text.gif"));
      } else {
        setIcon(DataViewer.getIcon("icons/file.gif"));
      }
    }

    return c;
  }
}