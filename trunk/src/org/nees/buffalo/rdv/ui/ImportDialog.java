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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBImport;
import org.nees.buffalo.rdv.rbnb.RBNBImportListener;

/**
 * @author Jason P. Hanley
 */
public class ImportDialog extends JDialog implements RBNBImportListener {
	static Log log = LogFactory.getLog(ImportDialog.class.getName());
	
	ImportDialog dialog;
	
	JFrame owner;
	RBNBController rbnb;
	
	JTextField sourceNameTextField;
	
	JTextField dataFileTextField;
	JButton dataFileButton;
	JFileChooser dataFileChooser;
	File dataFile;
	
	JButton importButton;
	JButton cancelButton;
	
	JProgressBar importProgressBar;
	
	RBNBImport rbnbImport;
	boolean importing;
	
	public ImportDialog(JFrame owner, RBNBController rbnb) {
    this(owner, rbnb, null);
  }
   
  public ImportDialog(JFrame owner, RBNBController rbnb, String sourceName) {
		super(owner);
		
		this.owner = owner;
		this.rbnb = rbnb;

		dialog = this;

		String rbnbHostName = rbnb.getRBNBHostName();
		int rbnbPortNumber = rbnb.getRBNBPortNumber();
		rbnbImport = new RBNBImport(rbnbHostName, rbnbPortNumber);
		importing = false;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTitle("Data file import");
		
		initComponents();
    
    sourceNameTextField.setText(sourceName);
	}
	
	private void initComponents() {
    JPanel container = new JPanel();
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
    
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(10,10,10,10);

		JLabel headerLabel = new JLabel("Please specify the desired source name and the data file to import.");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTHEAST;
    container.add(headerLabel, c);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new java.awt.Insets(0,10,10,5);
    container.add(new JLabel("Source name: "), c);
		
		sourceNameTextField = new JTextField();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new java.awt.Insets(0,0,10,10);
    container.add(sourceNameTextField, c);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new java.awt.Insets(0,10,10,5);
    container.add(new JLabel("Data file: "), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
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
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new java.awt.Insets(0,10,10,10);
    container.add(dataFileButton, c);
		
		importProgressBar = new JProgressBar(0, 100000);
		importProgressBar.setStringPainted(true);
		importProgressBar.setValue(0);
		importProgressBar.setVisible(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;;
		c.anchor = GridBagConstraints.CENTER;
    container.add(importProgressBar, c);		
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
    
    Action importAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        importData();
      }      
    };
    importAction.putValue(Action.NAME, "Import");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "import");
    actionMap.put("export", importAction);
		importButton = new JButton(importAction);
		panel.add(importButton);
    
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
		c.gridy = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new java.awt.Insets(0,0,10,5);
    container.add(panel, c);
		
		pack();
    setLocationByPlatform(true);
		setVisible(true);
	}
  
  private void importData() {
    String sourceName = sourceNameTextField.getText();
    importProgressBar.setVisible(true);
    disableUI();
    pack();
    importing = true;
    rbnbImport.startImport(sourceName, dataFile, dialog);
  }
  
  private void cancel() {
    if (importing) {
      rbnbImport.cancelImport();
    } else {
      dispose();
    }
  }

 	private void disableUI() {
 		importButton.setEnabled(false);
 		sourceNameTextField.setEnabled(false);
 		dataFileButton.setEnabled(false);
 	}
 	
 	private void enableUI() {
 		importButton.setEnabled(true);
 		sourceNameTextField.setEnabled(true);
 		dataFileButton.setEnabled(true); 		
 	}
	
	public void postProgress(double progress) {
		if (progress > 1) {
			progress = 1;
		}
 		importProgressBar.setValue((int)(progress*100000));		
	}

	public void postCompletion() {
		importing = false;
		rbnb.updateMetadata();
		dispose();
		JOptionPane.showMessageDialog(owner, "Import complete.", "Import complete", JOptionPane.INFORMATION_MESSAGE);
	}

	public void postError(String errorMessage) {
		importing = false;
		rbnb.updateMetadata();
		importProgressBar.setValue(0);
        importProgressBar.setVisible(false);
		enableUI();
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
