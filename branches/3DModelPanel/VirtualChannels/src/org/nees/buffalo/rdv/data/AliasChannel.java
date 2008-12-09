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

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBUtilities;
import org.nees.buffalo.rdv.ui.SpringUtilities;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelTree.NodeTypeEnum;

/**
 * A <code>LocalChannel</code> to create an alias from another
 * <code>Channel</code>.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class AliasChannel implements LocalChannel {
  
  private String remoteChannelName;
  private String localChannelName;
  
  private RBNBController rbnbController;
  
  private AliasChannelConfigurationPanel component;  
  public AliasChannel() {    
    remoteChannelName = null;
    localChannelName = null;
    
    component = null;
  }

  public void initialize(String channelName, RBNBController rbnbController) {
    localChannelName = channelName;
    this.rbnbController = rbnbController;
  }

  public JComponent getConfigurationComponent() {
    ChannelTree ctree = rbnbController.getMetadataManager().getMetadataChannelTree();
    List channelList = RBNBUtilities.getChannelList(ctree); 
    
    if (component == null) {
      component = new AliasChannelConfigurationPanel(channelList);
    } else {
      component.updateChannelList(channelList);
    }
    
    return component;
  }
  
  public void applyConfiguration() {
    remoteChannelName = component.getSelectedChannel();
  }
  
  public void discardConfiguration() {
    
  }
  
  public void updateMetadata(ChannelMap cmap) {
    //we haven't configured a channel yet
    if (remoteChannelName == null) {
      return;
    }
    
    int remoteIndex = cmap.GetIndex(remoteChannelName);
    if (remoteIndex != -1) {      
      try {
        int localIndex = cmap.Add(localChannelName);        
        cmap.PutMime(localIndex, cmap.GetMime(remoteIndex));
        cmap.PutTime(cmap.GetTimeStart(remoteIndex), cmap.GetTimeDuration(remoteIndex));        
        if (cmap.GetType(remoteIndex) == ChannelMap.TYPE_STRING) {
          String[] stringData = cmap.GetDataAsString(remoteIndex);
          for (int i=0; i<stringData.length; i++) {
            cmap.PutDataAsString(localIndex, stringData[i]);
          }
        }        
      } catch (SAPIException e) {
        e.printStackTrace();
        return;
      }      
    }
  }
  
  public void updateData(ChannelMap cmap, double startTime, double EndTime) {
    //we haven't configured a channel yet
    if (remoteChannelName == null) {
      return;
    }
    
    int remoteIndex = cmap.GetIndex(remoteChannelName);
    if (remoteIndex != -1) {
      try {
        int localIndex = cmap.Add(localChannelName);
        cmap.PutDataRef(localIndex, cmap, remoteIndex);
      } catch (SAPIException e) {
        e.printStackTrace();
        return;
      }      
    }
  }
  
  public void dispose() {
    
  }

  public String getName() {
    return localChannelName;
  }

  public NodeTypeEnum getType() {
    return ChannelTree.CHANNEL;
  }
  
  class AliasChannelConfigurationPanel extends JPanel {
    JComboBox channelNameComboBox;
    
    public AliasChannelConfigurationPanel(List channelList) {
      super();
      
      setLayout(new BorderLayout());
      
      JLabel directions = new JLabel("Please select the name of the source channel to alias from.");
      directions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      add(directions, BorderLayout.NORTH);
      
      JPanel controls = new JPanel();
      SpringLayout layout = new SpringLayout(); 
      controls.setLayout(layout);
            
      JLabel label = new JLabel("Channel name:", JLabel.TRAILING);
      controls.add(label);
      
      channelNameComboBox = new JComboBox();
      updateChannelList(channelList);
      controls.add(channelNameComboBox);
      
      SpringUtilities.makeCompactGrid(controls,
                                      1, 2,  //rows, cols
                                      6, 6,  //initX, initY
                                      6, 6); //xPad, yPad

      add(controls, BorderLayout.CENTER);
    }
    
    public void updateChannelList(List channelList) {
      channelNameComboBox.removeAllItems();
      Iterator i = channelList.iterator();
      while (i.hasNext()) {
        channelNameComboBox.addItem(i.next());
      }      
    }
    
    public String getSelectedChannel() {
      return (String)channelNameComboBox.getSelectedItem();
    }
  }
}
