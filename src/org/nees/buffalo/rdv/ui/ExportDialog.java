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
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Color;
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
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
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
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBExport;
import org.nees.buffalo.rdv.rbnb.ProgressListener;
import org.nees.buffalo.rdv.rbnb.RBNBUtilities;

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
  
  JList numericChannelList;
  DefaultListModel numericChannelModel;
  
  JTextField dataFileTextField;
  
  JList multimediaChannelList;
  DefaultListModel multimediaChannelModel;
  
  JTextField dataDirectoryTextField;
  JFileChooser dataDirectoryChooser;
  JButton dataDirectoryButton;  
  
  JProgressBar exportProgressBar;
  
  JButton exportButton;
  JButton cancelButton;
  
  List channels;

	public ExportDialog(JFrame owner, RBNBController rbnb, List channels) {
		super(owner);
    
    this.dialog = this;
    
    this.rbnb = rbnb;

    this.channels = channels;
    Collections.sort(channels, new RBNBUtilities.HumanComparator());
    
    export = new RBNBExport(rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());
    exporting = false;
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    setTitle("Export data to disk");
    
    initComponents();
  }

  private void initComponents() {
    numericChannelModel = new DefaultListModel();
    multimediaChannelModel = new DefaultListModel();
    for (int i=0; i<channels.size(); i++) {
      String channelName = (String)channels.get(i);
      Channel channel = rbnb.getChannel(channelName);

      String mime = RBNBUtilities.fixMime(channel.getMetadata("mime"), channelName);
      
      if (mime.equals("application/octet-stream")) {
        numericChannelModel.addElement(new ExportChannel(channelName));
      } else if (mime.equals("image/jpeg")) {
        multimediaChannelModel.addElement(new ExportChannel(channelName));
      }
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

    JLabel headerLabel = new JLabel("Select the data channels to export.");
    headerLabel.setBackground(Color.white);
    headerLabel.setOpaque(true);
    headerLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(10,10,10,10)));    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,0,0,0);    
    container.add(headerLabel, c);
    
    JLabel numericHeaderLabel = new JLabel("Numeric channels:");
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(10,10,10,10);
    container.add(numericHeaderLabel, c);    
    
    numericChannelList = new JList(numericChannelModel);
    numericChannelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    numericChannelList.setCellRenderer(new CheckListRenderer());
    numericChannelList.setVisibleRowCount(10);
    numericChannelList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int index = numericChannelList.locationToIndex(e.getPoint());
        ExportChannel item = (ExportChannel)numericChannelList.getModel().getElementAt(index);
        item.setSelected(!item.isSelected());
        Rectangle rect = numericChannelList.getCellBounds(index, index);
        numericChannelList.repaint(rect);
      }
    });
    JScrollPane scrollPane = new JScrollPane(numericChannelList);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(scrollPane, c);
    
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,10,10,5);
    container.add(new JLabel("Numeric file name: "), c);
    
    dataFileTextField = new JTextField(20);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 3;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,0,10,10);
    container.add(dataFileTextField, c);
    
    JLabel multimediaHeaderLabel = new JLabel("Multimedia channels:");
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(multimediaHeaderLabel, c);    
    
    multimediaChannelList = new JList(multimediaChannelModel);
    multimediaChannelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    multimediaChannelList.setCellRenderer(new CheckListRenderer());
    multimediaChannelList.setVisibleRowCount(10);
    multimediaChannelList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int index = multimediaChannelList.locationToIndex(e.getPoint());
        ExportChannel item = (ExportChannel)multimediaChannelList.getModel().getElementAt(index);
        item.setSelected(!item.isSelected());
        Rectangle rect = multimediaChannelList.getCellBounds(index, index);
        multimediaChannelList.repaint(rect);
      }
    });
    JScrollPane scrollPane2 = new JScrollPane(multimediaChannelList);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    container.add(scrollPane2, c);    
    
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,5);
    container.add(new JLabel("Data directory: "), c);    
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;    
    dataDirectoryTextField = new JTextField(20);
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(dataDirectoryTextField, c);
    
    dataDirectoryChooser = new JFileChooser();
    dataDirectoryChooser.setDialogTitle("Select export directory");
    dataDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    dataDirectoryTextField.setText(dataDirectoryChooser.getCurrentDirectory().getAbsolutePath());
    dataDirectoryButton = new JButton("Browse");
    dataDirectoryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        dataDirectoryChooser.setCurrentDirectory(new File(dataDirectoryTextField.getText()));
        int status = dataDirectoryChooser.showDialog(dialog, "OK");
        if (status == JFileChooser.APPROVE_OPTION) {
          dataDirectoryTextField.setText(dataDirectoryChooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 2;
    c.gridy = 6;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,0,10,10);
    container.add(dataDirectoryButton, c);
    
    exportProgressBar = new JProgressBar(0, 100000);
    exportProgressBar.setStringPainted(true);
    exportProgressBar.setValue(0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 7;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new java.awt.Insets(0,10,10,10);
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
    c.gridy = 9;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(panel, c);    

    pack();
    if (getWidth() < 600) {
      setSize(600, getHeight());
    }
    
    dataFileTextField.requestFocusInWindow();
    
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  private void disableUI() {
    numericChannelList.setEnabled(false);
    dataFileTextField.setEnabled(false);
    multimediaChannelList.setEnabled(false);
    dataDirectoryTextField.setEnabled(false);
    dataDirectoryButton.setEnabled(false);
    exportButton.setEnabled(false);
  }
  
  private void enableUI() {
    numericChannelList.setEnabled(true);
    dataFileTextField.setEnabled(true);
    multimediaChannelList.setEnabled(true);
    dataDirectoryTextField.setEnabled(true);
    dataDirectoryButton.setEnabled(true);
    exportButton.setEnabled(true);    
  }  
  
  private void exportData() {
    disableUI();
    
    List selectedNumericChannels = new ArrayList();
    for (int i=0; i<numericChannelModel.size(); i++) {
      ExportChannel channel = (ExportChannel)numericChannelModel.get(i);
      if (channel.isSelected()) {
        selectedNumericChannels.add(channel.toString());
      }
    }
    
    List selectedMultimediaChannels = new ArrayList();
    for (int i=0; i<multimediaChannelModel.size(); i++) {
      ExportChannel channel = (ExportChannel)multimediaChannelModel.get(i);
      if (channel.isSelected()) {
        selectedMultimediaChannels.add(channel.toString());
      }
    }    
    
    double end = rbnb.getLocation();
    double start = end - rbnb.getTimeScale();
    
    File numericDataFile = new File(dataDirectoryTextField.getText(),
        dataFileTextField.getText());
    File dataDirectory = new File(dataDirectoryTextField.getText());
        
    exporting = true;
    export.startExport(selectedNumericChannels, numericDataFile,
        selectedMultimediaChannels,
        dataDirectory,
        start, end,
        this);
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
    
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean hasFocus) {
      setEnabled(list.isEnabled());
      setSelected(((ExportChannel)value).isSelected());
      if (index % 2 == 0) {
        setBackground(UIManager.getColor("List.textBackground"));
      } else {
        setBackground(new Color(230,230,230));
      }
      setForeground(UIManager.getColor("List.textForeground"));
      setFont(list.getFont());
      setText(value.toString());
      return this;
    }
  }

}
