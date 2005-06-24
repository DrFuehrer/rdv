/*
 * Created on Jun 24, 2005
 */
package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;

/**
 * @author Jason P. Hanley
 */
public class MetadataPanel extends JPanel implements MetadataListener, ChannelSelectionListener {

  private RBNBController rbnb;
  
  private ChannelMap cmap;
  private ChannelTree ctree;
  
  private String channel;
  private int children;
  private String ROOT_CHANNEL = new String();
  
  private JEditorPane infoTextArea;
  
  public MetadataPanel(RBNBController rbnb) {
    this.rbnb = rbnb;
    
    cmap = null;
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
    
    if (channel == null || cmap == null) {
      return;
    } else if (channel == ROOT_CHANNEL) {
      StringBuffer s = new StringBuffer();
      s.append("<strong>" + rbnb.getRBNBHostName() + ":" + rbnb.getRBNBPortNumber() + "</strong><br>");
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
      node = ctree.findNode(node.getFullName());
      String channelName = node.getFullName();
      if (node.getType() == ChannelTree.SOURCE) {
        s.append("<strong>" + channelName + "</strong><br>");        
        s.append("<em>Data Source</em>");
        
        s.append("<p style=\"font-size: 10px\">" + children + " Channel");
        if (children == 0 || children > 1) {
          s.append("s");
        }
        s.append("</p>");        
      } else if (node.getType() == ChannelTree.CHANNEL) {
        int channelIndex = cmap.GetIndex(channelName);
        String mime = node.getMime();
        double start = cmap.GetTimeStart(channelIndex);
        double duration = cmap.GetTimeDuration(channelIndex);
        int size = node.getSize();
        String unit = ((Channel)rbnb.getChannel(channelName)).getUnit();

        s.append("<strong>" + channelName + "</strong>");
        if (unit != null) {
          s.append(" (" + unit + ")");
        }
        
        if (mime != null) {
          s.append("<br>");
          if (mime.equals("application/octet-stream")) {
            s.append("<em>Numeric Data</em>");
          } else if (mime.equals("image/jpeg")) {
            s.append("<em>JPEG Images</em>");
          } else if (mime.equals("video/jpeg")) {
            s.append("<em>JPEG Video</em>");
          }
        }
        
        s.append("<p style=\"font-size: 10px\">Starts at " + DataViewer.formatDateSmart(start) + "<br>");
        s.append("Lasts " + DataViewer.formatSeconds(duration) + "<br>");
        if (size != -1) {
          s.append("Size: " + DataViewer.formatBytes(size));
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

  public void channelListUpdated(ChannelMap cmap) {
    this.cmap = cmap;
    ctree = ChannelTree.createFromChannelMap(cmap);
    
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
