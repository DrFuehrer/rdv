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

package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.data.LocalChannelExtension;
import org.nees.buffalo.rdv.data.LocalChannelManager;
import org.nees.buffalo.rdv.data.ExtensionFailedException;
import org.nees.buffalo.rdv.data.ExtensionNotFoundException;
import org.nees.buffalo.rdv.data.LocalChannel;

/**
 * A wizard to guide in the creation and configuration of a Local Channel. 
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class LocalChannelWizard implements WizardActionListener {
  
  private LocalChannelManager localChannelManager;
  
  private IntroPanel introPanel;
  private ExtensionPanel extensionPanel;
  
  private LocalChannel localChannel;
  
  public LocalChannelWizard(ApplicationFrame appFrame, LocalChannelManager localChannelManager) {
    super();
    
    this.localChannelManager = localChannelManager;
    
    Wizard wizard = new Wizard("Add a Local Channel");
    wizard.addWizardActionListener(this);
    
    introPanel = new IntroPanel(localChannelManager.getLocalChannelExtensions());
    wizard.addWizardPanel(introPanel);
    
    extensionPanel = new ExtensionPanel();
    wizard.addWizardPanel(extensionPanel);
    
    wizard.setCurrentWizardPanel(introPanel.getID());
    
    wizard.showWizard();
  }
  
  public void wizardBack(String oldID, String newID) {}
  
  public void wizardNext(String oldID, String newID) {
    if (newID.equals(extensionPanel.getID())) {
      setupExtensionPanel();
    }
    
  }

  public void wizardFinish(String id) {
    if (localChannel != null) {
      localChannel.applyConfiguration();
    }
  }

  public void wizardCancel(String id) {
    if (localChannel != null) {
      localChannel.discardConfiguration();
      localChannelManager.dispose(localChannel);
    }
  }  
  
  private void setupExtensionPanel() {
    try {
      localChannel = localChannelManager.createLocalChannel(introPanel.getChannelName(), introPanel.getExtension());
    } catch (ExtensionNotFoundException e) {
      e.printStackTrace();
      return;
    } catch (ExtensionFailedException e) {
      e.printStackTrace();
      return;
    }
    
    JComponent configurationComponent = localChannel.getConfigurationComponent();
    extensionPanel.setComponent(configurationComponent);
  }
  
  class IntroPanel extends JPanel implements WizardPanel, ActionListener {
    JTextField channelNameTextField;
    JComboBox extensionComboBox;
    JLabel extensionDescriptionLabel;
    
    public IntroPanel(List extensionList) {
      super();

      initComponents(extensionList);
    }
    
    private void initComponents(List extensionList) {
      
      JPanel controls = new JPanel();
      controls.setLayout(new SpringLayout());
      
      controls.add(new JLabel("Channel Name:", JLabel.TRAILING));
      
      channelNameTextField = new JTextField();
      controls.add(channelNameTextField);
      
      controls.add(new JLabel("Channel Type:", JLabel.TRAILING));
      
      extensionComboBox = new JComboBox();
      Iterator i = extensionList.iterator();
      while (i.hasNext()) {
        extensionComboBox.addItem(i.next());
      }
      extensionComboBox.addActionListener(this);
      controls.add(extensionComboBox);
      
      SpringUtilities.makeCompactGrid(controls,
                                      2, 2,  //rows, cols
                                      6, 6,  //initX, initY
                                      6, 6); //xPad, yPad
      
      extensionDescriptionLabel = new JLabel(DataViewer.getIcon("icons/info.gif"), JLabel.LEADING);
      extensionDescriptionLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      updateDescriptionText();
      
      setLayout(new BorderLayout());
      add(controls, BorderLayout.CENTER);
      add(extensionDescriptionLabel, BorderLayout.SOUTH);
    }
    
    public String getChannelName() {
      return channelNameTextField.getText();
    }
    
    public LocalChannelExtension getExtension() {
      return (LocalChannelExtension)extensionComboBox.getSelectedItem();
    }    
    
    public String getID() {
      return "Intro";
    }

    public String getTitle() {
      return "Create a local channel";
    }

    public String getDescription() {
      return "Create a local channel from a type in the list.";
    }

    public Icon getIcon() {
      return DataViewer.getIcon("icons/channel_wizard.gif");
    }

    public JComponent getComponent() {
      return this;
    }

    public String getNextPanelID() {
      return "Extension";
    }

    public String getPreviousPanelID() {
      return null;
    }
    
    public void showing() {
      
    }
    
    public void hiding() {}

    public void actionPerformed(ActionEvent e) {
      updateDescriptionText();
    }
    
    private void updateDescriptionText() {
      LocalChannelExtension extension = (LocalChannelExtension)extensionComboBox.getSelectedItem();
      extensionDescriptionLabel.setText(extension.getDescription());      
    }
  }
  
  class ExtensionPanel implements WizardPanel {
    JComponent component;
    
    public ExtensionPanel() {
      super();
      
      component = null;
    }
    
    public void setComponent(JComponent component) {
      this.component = component;
    }

    public String getID() {
      return "Extension";
    }

    public String getTitle() {
      return "Configure extension";
    }

    public String getDescription() {
      return "Configure the extension to create the local channel.";
    }

    public Icon getIcon() {
      return null;
    }

    public JComponent getComponent() {
      return component;
    }

    public String getNextPanelID() {
      return null;
    }

    public String getPreviousPanelID() {
      return "Intro";
    }

    public void showing() {}

    public void hiding() {}
  }
}
