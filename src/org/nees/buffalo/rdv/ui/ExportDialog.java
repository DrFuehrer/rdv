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
 * $URL: svn+ssh://jphanley@code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/ui/ExportDialog.java $
 * $Revision: 363 $
 * $Date: 2005-12-21 10:58:24 -0500 (Wed, 21 Dec 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBExport;
import org.nees.buffalo.rdv.rbnb.ProgressListener;

/**
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class ExportDialog extends JDialog implements ProgressListener {
  static Log log = LogFactory.getLog(ExportDialog.class.getName());
  
  ExportDialog dialog;
  
  RBNBController rbnb;
  RBNBExport export;
  boolean exporting;
  
  JList channelList;
  DefaultListModel channelModel;

  JCheckBox deltaTCheckBox;
  JTextField dataFileTextField;
  JFileChooser dataFileChooser;
  JButton dataFileButton;
  JProgressBar exportProgressBar;
  JButton exportButton;
  JButton cancelButton;
  
  File dataFile;
  
  List channels;

	public ExportDialog(JFrame owner, RBNBController rbnb, List channels) {
		super(owner);
    
    this.dialog = this;
    
    this.rbnb = rbnb;
    this.channels = channels;
    
    export = new RBNBExport(rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());
    exporting = false;
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    setTitle("Export data to disk");
    
    initComponents();
  }

  private void initComponents() {
    channelModel = new DefaultListModel();
    for (int i=0; i<channels.size(); i++) {
      channelModel.addElement(new ExportChannel((String)channels.get(i)));
    }
    
    JPanel container = new JPanel();
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
        
    container.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.weighty = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(10,10,10,10);

    JLabel headerLabel = new JLabel("Select the channels to export:");
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    container.add(headerLabel, c);
    
    channelList = new JList(channelModel);
    channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    channelList.setCellRenderer(new CheckListRenderer());
    channelList.setVisibleRowCount(15);
    channelList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int index = channelList.locationToIndex(e.getPoint());
        ExportChannel item = (ExportChannel)channelList.getModel().getElementAt(index);
        item.setSelected(!item.isSelected());
        Rectangle rect = channelList.getCellBounds(index, index);
        channelList.repaint(rect);
      }
    });
    JScrollPane scrollPane = new JScrollPane(channelList);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(scrollPane, c);
    
    deltaTCheckBox = new JCheckBox("Include relative time channel", true);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    container.add(deltaTCheckBox, c);    
        
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,10,10,5);
    container.add(new JLabel("Data file: "), c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 3;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;    
    dataFileTextField = new JTextField(20);
    dataFileTextField.setEditable(false);
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(dataFileTextField, c);
    
    dataFileChooser = new JFileChooser();
    dataFileButton = new JButton("Browse");
    dataFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int status = dataFileChooser.showSaveDialog(dialog);
        if (status == JFileChooser.APPROVE_OPTION) {
          dataFile = dataFileChooser.getSelectedFile();
          dataFileTextField.setText(dataFile.getAbsolutePath());
        }
      }
    });
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 2;
    c.gridy = 3;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(dataFileButton, c);
    
    exportProgressBar = new JProgressBar(0, 100000);
    exportProgressBar.setStringPainted(true);
    exportProgressBar.setValue(0);
    exportProgressBar.setVisible(false);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.CENTER;
    container.add(exportProgressBar, c);        
       
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());

    Action exportAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        exportData();
      }      
    };
    exportAction.putValue(Action.NAME, "Export");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "export");
    actionMap.put("export", exportAction);    
    exportButton = new JButton(exportAction);
    panel.add(exportButton);
    
    Action cancelAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }      
    };
    cancelAction.putValue(Action.NAME, "Cancel");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    actionMap.put("cancel", cancelAction);    
    cancelButton = new JButton(cancelAction);
    panel.add(cancelButton);
    
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(panel, c);    

    pack();
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  private void disableUI() {
    channelList.setEnabled(false);
    dataFileTextField.setEnabled(false);
    dataFileButton.setEnabled(false);
    exportButton.setEnabled(false);
  }
  
  private void enableUI() {
    channelList.setEnabled(true);
    dataFileTextField.setEnabled(true);
    dataFileButton.setEnabled(true);
    exportButton.setEnabled(true);    
  }  
  
  private void exportData() {
    disableUI();
    
    List selectedChannels = new ArrayList();
    for (int i=0; i<channelModel.size(); i++) {
      ExportChannel channel = (ExportChannel)channelModel.get(i);
      if (channel.isSelected()) {
        selectedChannels.add(channel.toString());
      }
    }
    
    double end = rbnb.getLocation();
    double start = end - rbnb.getTimeScale();
    
    boolean deltaT = deltaTCheckBox.isSelected();
        
    exportProgressBar.setVisible(true);
    pack();
    
    exporting = true;
    export.startExport(selectedChannels, dataFile, start, end, deltaT, this);
  }
  
  private void cancel() {
    if (exporting) {
     export.cancelExport();
    } else {
      dispose();
    }
  }
  
  public void postProgress(double progress) {
    exportProgressBar.setValue((int)(progress*100000));   
  }

  public void postCompletion() {
    exporting = false;
    dispose();
    JOptionPane.showMessageDialog(this, "Export complete.", "Export complete", JOptionPane.INFORMATION_MESSAGE);
  }

  public void postError(String errorMessage) {
    exportProgressBar.setValue(0);
    exportProgressBar.setVisible(false);
    exporting = false;
    enableUI();
    JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
  }  
  
  class ExportChannel {
    String channelName;
    boolean selected;
    
    public ExportChannel(String channelName) {
      this.channelName = channelName;
      selected = true;
    }
    
    public boolean isSelected() {
      return selected;
    }
    
    public void setSelected(boolean selected) {
      this.selected = selected;
    }
    
    public String toString() {
      return channelName;
    }
  }
  
  class CheckListRenderer extends JCheckBox implements ListCellRenderer {
    
    public CheckListRenderer() {
      setBackground(UIManager.getColor("List.textBackground"));
      setForeground(UIManager.getColor("List.textForeground"));
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean hasFocus) {
      setEnabled(list.isEnabled());
      setSelected(((ExportChannel)value).isSelected());
      setFont(list.getFont());
      setText(value.toString());
      return this;
    }
  }

}
