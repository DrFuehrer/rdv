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

package org.nees.buffalo.rdv.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import org.nees.buffalo.rdv.rbnb.ProgressListener;

/**
 * @author Jason P. Hanley
 */
public class ImportDialog extends JDialog implements ProgressListener {
	static Log log = LogFactory.getLog(ImportDialog.class.getName());
	
	JTextField sourceNameTextField;
	
	File dataFile;
	
	JButton importButton;
	JButton cancelButton;
	
	JProgressBar importProgressBar;
	
	RBNBImport rbnbImport;
	boolean importing;
	
  public ImportDialog(File dataFile, String sourceName) {
		super();
    
    this.dataFile = dataFile;
		
    RBNBController rbnb = RBNBController.getInstance();
		String rbnbHostName = rbnb.getRBNBHostName();
		int rbnbPortNumber = rbnb.getRBNBPortNumber();
    
		rbnbImport = new RBNBImport(rbnbHostName, rbnbPortNumber);
		importing = false;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTitle("Data file import");
		
		initComponents();
    
    sourceNameTextField.setText(sourceName);
    sourceNameTextField.selectAll();
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

		JLabel headerLabel = new JLabel("Please specify the desired source name for the data.");
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
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new java.awt.Insets(10,10,10,5);
    container.add(new JLabel("Source name: "), c);
		
		sourceNameTextField = new JTextField();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new java.awt.Insets(10,0,10,10);
    container.add(sourceNameTextField, c);
		
		importProgressBar = new JProgressBar(0, 100000);
		importProgressBar.setStringPainted(true);
		importProgressBar.setValue(0);
		importProgressBar.setVisible(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;;
		c.anchor = GridBagConstraints.CENTER;
    c.insets = new java.awt.Insets(0,10,10,10);
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
    getRootPane().setDefaultButton(importButton);
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
		c.gridy = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new java.awt.Insets(0,0,10,5);
    container.add(panel, c);
		
		pack();
    
    sourceNameTextField.requestFocusInWindow();
    
    setLocationByPlatform(true);
		setVisible(true);
	}
  
  private void importData() {
    String sourceName = sourceNameTextField.getText();
    importProgressBar.setVisible(true);
    disableUI();
    pack();
    importing = true;
    rbnbImport.startImport(sourceName, dataFile, this);
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
 	}
 	
 	private void enableUI() {
 		importButton.setEnabled(true);
 		sourceNameTextField.setEnabled(true);
 	}
	
	public void postProgress(double progress) {
		if (progress > 1) {
			progress = 1;
		}
 		importProgressBar.setValue((int)(progress*100000));		
	}

	public void postCompletion() {
		importing = false;
		RBNBController.getInstance().updateMetadata();
		dispose();
		JOptionPane.showMessageDialog(this, "Import complete.", "Import complete", JOptionPane.INFORMATION_MESSAGE);
	}

	public void postError(String errorMessage) {
		importing = false;
    RBNBController.getInstance().updateMetadata();
		importProgressBar.setValue(0);
    importProgressBar.setVisible(false);
		enableUI();
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
