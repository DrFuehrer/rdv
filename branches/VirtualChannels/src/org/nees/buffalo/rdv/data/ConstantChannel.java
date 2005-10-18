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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.ui.SpringUtilities;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.NodeTypeEnum;

/**
 * A <code>LocalChannel</code> to create an alias from another
 * <code>Channel</code>.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class ConstantChannel implements LocalChannel {
  
  private String localChannelName;
  private double constant;
  private double sampleRate;
  
  private ConstantChannelConfigurationPanel component;
  
  public ConstantChannel() {    
    localChannelName = null;
    
    component = null;
  }

  public void initialize(String channelName, RBNBController rbnbController) {
    localChannelName = channelName;
  }

  public JComponent getConfigurationComponent() {
    if (component == null) {
      component = new ConstantChannelConfigurationPanel();
    }
    
    return component;
  }
  
  public void applyConfiguration() {
    constant = component.getConstant();
    sampleRate = component.getSampleRate();
  }
  
  public void discardConfiguration() {
    
  }
  
  public void updateMetadata(ChannelMap cmap) {
    //TODO
  }
  
  public void updateData(ChannelMap cmap, double startTime, double EndTime) {
    //TODO
  }
  
  public void dispose() {
    
  }

  public String getName() {
    return localChannelName;
  }

  public NodeTypeEnum getType() {
    return ChannelTree.CHANNEL;
  }
  
  class ConstantChannelConfigurationPanel extends JPanel {
    JTextField constantTextField;
    JTextField sampleRateTextField;
    
    public ConstantChannelConfigurationPanel() {
      super();
      
      setLayout(new BorderLayout());
      
      JLabel directions = new JLabel("Set the constant for this channel.");
      directions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      add(directions, BorderLayout.NORTH);
      
      JPanel controls = new JPanel();
      SpringLayout layout = new SpringLayout(); 
      controls.setLayout(layout);
            
      JLabel label = new JLabel("Constant:", JLabel.TRAILING);
      controls.add(label);
      
      constantTextField = new JTextField();
      controls.add(constantTextField);
      
      label = new JLabel("Sample rate:", JLabel.TRAILING);
      controls.add(label);
      
      sampleRateTextField = new JTextField("128");
      controls.add(sampleRateTextField);
      
      SpringUtilities.makeCompactGrid(controls,
                                      2, 2,  //rows, cols
                                      6, 6,  //initX, initY
                                      6, 6); //xPad, yPad

      add(controls, BorderLayout.CENTER);
    }
    
    public double getConstant() {
      return Double.parseDouble(constantTextField.getText());
    }
    
    public double getSampleRate() {
      return Double.parseDouble(sampleRateTextField.getText());
    }
  }
}
