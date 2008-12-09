/*
 * Created on Mar 25, 2005
 */
package org.nees.buffalo.rdv.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBImport;
import org.nees.buffalo.rdv.rbnb.RBNBImportListener;

/**
 * @author Jason P. Hanley
 */
public class ImportDialog extends JDialog implements KeyEventDispatcher, RBNBImportListener {
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
		super(owner);
		
		this.owner = owner;
		this.rbnb = rbnb;

		dialog = this;

		String rbnbHostName = rbnb.getRBNBHostName();
		int rbnbPortNumber = rbnb.getRBNBPortNumber();
		rbnbImport = new RBNBImport(rbnbHostName, rbnbPortNumber);
		importing = false;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				bindKeys();
			}
			public void windowDeactivated(WindowEvent e) {
				unbindKeys();
			}
		});

		setTitle("Data file import");
		
		initComponents();
	}
	
	private void initComponents() {
		getContentPane().setLayout(new GridBagLayout());
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
		getContentPane().add(headerLabel, c);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new java.awt.Insets(0,10,10,5);
		getContentPane().add(new JLabel("Source name: "), c);
		
		sourceNameTextField = new JTextField();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new java.awt.Insets(0,0,10,10);
		getContentPane().add(sourceNameTextField, c);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new java.awt.Insets(0,10,10,5);
		getContentPane().add(new JLabel("Data file: "), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;		
		dataFileTextField = new JTextField(20);
		dataFileTextField.setEditable(false);
		c.insets = new java.awt.Insets(0,0,10,5);
		getContentPane().add(dataFileTextField, c);
		
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
		getContentPane().add(dataFileButton, c);
		
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
		getContentPane().add(importProgressBar, c);		
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String sourceName = sourceNameTextField.getText();
				importProgressBar.setVisible(true);
				disableUI();
				pack();
				importing = true;
				rbnbImport.startImport(sourceName, dataFile, dialog);
			}
		});	
		panel.add(importButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (importing) {
					rbnbImport.cancelImport();
				} else {
					dispose();
				}
			}
		});		
		panel.add(cancelButton);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new java.awt.Insets(0,0,10,10);
		getContentPane().add(panel, c);
		
		pack();
		centerOnOwner();
		setVisible(true);
	}

	/**
	 * Center the dialog box on the owner frame.
	 *
	 * @since  1.2
	 */
	private void centerOnOwner() {
		int frameX = owner.getX();
		int frameY = owner.getY();
		int frameWidth = owner.getWidth();
		int frameHeight = owner.getHeight();
		int dialogWidth = getWidth();
		int dialogHeight = getHeight();
		
		int dialogX = frameX + (frameWidth/2) - (dialogWidth/2);
		if (dialogX < 0) {
			dialogX = 0;
		}
		int dialogY = frameY + (frameHeight/2) - (dialogHeight/2);
		if (dialogY < 0) {
			dialogY = 0;
		}
		setLocation(dialogX, dialogY);
	}

	
	private void bindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.addKeyEventDispatcher(this);		
	}
	
	private void unbindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.removeKeyEventDispatcher(this);
	}

 	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
 		int keyCode = keyEvent.getKeyCode();
 		
 		if (keyCode == KeyEvent.VK_ENTER) {
 			
 			return true;
 		} else if (keyCode == KeyEvent.VK_ESCAPE) {
 			dispose();
 			return true;
 		} else {
 			return false;
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
		rbnb.updateMetadataBackground();
		dispose();
		JOptionPane.showMessageDialog(owner, "Import complete.", "Import complete", JOptionPane.INFORMATION_MESSAGE);
	}

	public void postError(String errorMessage) {
		importing = false;
		rbnb.updateMetadataBackground();
		importProgressBar.setValue(0);
		enableUI();
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
