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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.rbnb.Channel;
import org.rdv.rbnb.ChannelManager;
import org.rdv.rbnb.MetadataListener;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBUtilities;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelTree;

/**
 * @author Jason P. Hanley
 */
public class MetadataPanel extends JPanel implements MetadataListener, ChannelSelectionListener {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 5913041782729190355L;

  static Log log = LogFactory.getLog(ChannelManager.class.getName());
  
  private RBNBController rbnb;
  
  //private ChannelMap cmap;
  private ChannelTree ctree;
  
  private String channel;
  private int children;
  private String ROOT_CHANNEL = new String();
  
  private JEditorPane infoTextArea;
  
  public MetadataPanel(RBNBController rbnb) {
    this.rbnb = rbnb;
    
    ctree = null;
    
    channel = null;
    
    initPanel();
  }
  
  private void initPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    setMinimumSize(new Dimension(130, 27));
    setPreferredSize(new Dimension(150, 150));
    
    infoTextArea = new JEditorPane();
    infoTextArea.setEditable(false);
    infoTextArea.setContentType("text/html");
    infoTextArea.setBorder(new EmptyBorder(4, 4, 4, 4));
    
    SimpleInternalFrame infoViewFrame = new SimpleInternalFrame(
        DataViewer.getIcon("icons/properties.gif"),
        "Properties",
        null,
        infoTextArea);
    
    add(infoViewFrame, BorderLayout.CENTER);
  }
  
  private void updatePanel() {
    clearPanel();
    
    if (channel == null || ctree == null) {
      return;
    } else if (channel == ROOT_CHANNEL) {
      StringBuffer s = new StringBuffer();
      s.append("<strong>" + rbnb.getServerName() + "</strong><br>");
      s.append("<em>Data Server</em>");
      s.append("<p style=\"font-size: 10px\">" + children + " Data Source");
      if (children == 0 || children > 1) {
        s.append("s");
      }
      s.append("</p>");
      infoTextArea.setText(s.toString());
    } else {
      StringBuffer s = new StringBuffer();
      ChannelTree.Node node = ctree.findNode(channel);
      if (node.getType() == ChannelTree.SOURCE) {
        s.append("<strong>" + channel + "</strong><br>");        
        s.append("<em>Data Source</em>");
        
        s.append("<p style=\"font-size: 10px\">" + children + " Channel");
        if (children == 0 || children > 1) {
          s.append("s");
        }
        s.append("</p>");
      } else if (node.getType() == ChannelTree.SERVER) {
        s.append("<strong>" + channel + "</strong><br>");        
        s.append("<em>Child Server</em>");
        
        s.append("<p style=\"font-size: 10px\">" + children + " Source");
        if (children == 0 || children > 1) {
          s.append("s");
        }
        s.append("</p>");        
      } else if (node.getType() == ChannelTree.CHANNEL) {
        String mime = RBNBUtilities.fixMime(node.getMime(), node.getFullName());
        double start = node.getStart();
        double duration = node.getDuration();
        int size = node.getSize();
        Channel channelMetadata = (Channel)rbnb.getChannel(channel);
        String unit = channelMetadata.getMetadata("units");
        String description = channelMetadata.getMetadata("description");

        s.append("<strong>" + channel + "</strong>");
        if (unit != null) {
          s.append(" (" + unit + ")");
        }
        if (description != null) {
          s.append("<br>" + description);
        }
        
        if (mime != null) {
          s.append("<br>");
          if (mime.equals("application/octet-stream")) {
            s.append("<em>Numeric Data</em>");
            String sampleRate = channelMetadata.getMetadata("samplerate");
            if (sampleRate != null) {
              s.append("<br>" + sampleRate +" Hz");
            }
          } else if (mime.equals("image/jpeg")) {
            s.append("<em>JPEG Images</em>");
            String width = channelMetadata.getMetadata("width");
            String height = channelMetadata.getMetadata("height");
            if (width != null && height != null) {
              s.append("<br>" + width + " x " + height);
              String sampleRate = channelMetadata.getMetadata("framerate");
              if (sampleRate != null) {
                s.append(", " + sampleRate + " fps");
              }
            }
          } else if (mime.equals("video/jpeg")) {
            s.append("<em>JPEG Video</em>");            
          } else if (mime.equals("text/plain")) {
            s.append("<em>Text</em>");
          } else if (mime.startsWith("audio/")) {
            s.append("<em>Audio</em>");
          } else {
            s.append("<em>" + mime + "</em>");
          }
        }
        
        s.append("<p style=\"font-size: 10px\">Begins " + DataViewer.formatDateSmart(start) + "<br>");
        s.append("Lasts " + DataViewer.formatSeconds(duration));
        if (size != -1) {
          s.append("<br>" + DataViewer.formatBytes(size));
        }
        if (mime != null) {
          if (mime.startsWith("audio/")) {
            String encoding = channelMetadata.getMetadata("encoding");
            String channels = channelMetadata.getMetadata("channels");
            String sampleRate = channelMetadata.getMetadata("samplerate");
            String sampleSize = channelMetadata.getMetadata("samplesize");
            String signed = channelMetadata.getMetadata("signed").equals("1")?"s":"u";
            String endian = channelMetadata.getMetadata("endian").equals("1")?"be":"le";
            if (encoding != null && channels != null && sampleRate != null && sampleSize != null && endian != null) {
              s.append("<br>" + encoding + " (" + Float.parseFloat(sampleRate)/1000 + "kHz");
              s.append("/" + channels + "ch");
              s.append("/" + sampleSize + "b");
              if (Integer.parseInt(sampleSize) > 1) {
                s.append("/" + endian);
              }
              s.append("/" + signed);
              s.append(")");
            }
          }
        }
        s.append("</p>");
      }
      infoTextArea.setText(s.toString());
    }
    
    infoTextArea.setCaretPosition(0);
  }
    
  private void clearPanel() {
    infoTextArea.setText("");
  }

  public void channelTreeUpdated(ChannelTree ctree) {
    this.ctree = ctree;
    
    updatePanel();
  }

  public void channelSelected(ChannelSelectionEvent e) {
    if (e.isRoot()) {
      channel = ROOT_CHANNEL;
    } else {
      channel = e.getChannelName();
    }
    
    children = e.getChildren();
    
    updatePanel();
  }

  public void channelSelectionCleared() {
    channel = null;
    children = 0;
    
    clearPanel();
  }
}
