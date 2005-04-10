/*
 * Created on Apr 8, 2005
 */
package org.nees.buffalo.rdv.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A dialog to to show that the application is busy with a progress bar
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class BusyDialog extends JDialog {
	/**
	 * The owner of the dialog.
	 * 
	 * @since  1.2
	 */
	JFrame frame;
	
	/**
	 * The progress bar to show the indeterminate state.
	 * 
	 * @since  1.2
	 */
	JProgressBar busyProgressBar;
	
	/**
	 * Construct a dialog box showing a progress bar with no progress.
	 * 
	 * @param frame  the owner of the dialog
	 * @since        1.2
	 */
	public BusyDialog(JFrame frame) {
		super(frame);
		this.frame = frame;
		
		initComponents();
	}
	
	/**
	 * Create the UI and display it centered on the owner.
	 * 
	 * @since  1.2
	 */
	private void initComponents() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setTitle("Connecting");
		
		Container container = getContentPane();
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,5,5,5);
		c.gridx = 0;
		c.anchor = GridBagConstraints.CENTER;

		JLabel messageLabel = new JLabel("Connecting to server...");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		container.add(messageLabel, c);
		
		busyProgressBar = new JProgressBar(0, 100);
		busyProgressBar.setPreferredSize(new Dimension(250, 17));
		c.gridy = 1;
		container.add(busyProgressBar, c);
		
		/* JButton cancelButton = new JButton("Cancel");
		c.fill = GridBagConstraints.NONE;
		c.gridy = 2;
		container.add(cancelButton, c); */
		
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
		int frameX = frame.getX();
		int frameY = frame.getY();
		int frameWidth = frame.getWidth();
		int frameHeight = frame.getHeight();
		int dialogWidth = getWidth();
		int dialogHeight = getHeight();
		
		int dialogX = frameX + (frameWidth/2) - (dialogWidth/2);
		int dialogY = frameY + (frameHeight/2) - (dialogHeight/2);
		setLocation(dialogX, dialogY);
	}
	
	/**
	 * Set the progress bar to indeterminate.
	 * 
	 * @since  1.2
	 */
	public void start() {
		
		busyProgressBar.setIndeterminate(true);
	}
	
	/**
	 * Set the progress bar to no status
	 *
	 * @since  1.2
	 */
	public void stop() {
		busyProgressBar.setIndeterminate(true);
	}
	
	/**
	 * Hide the dialog and dispose of it.
	 * 
	 * @since  1.2
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
}
