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

package org.rdv.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A dialog to to show that the application is busy with a progress bar.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class BusyDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = 3682219369836028271L;

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
   * The button to cancel the action
   * 
   * @since  1.3
   */
  JButton cancelButton;
	
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
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setTitle("Connecting");
		
		Container container = getContentPane();
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.weighty = 0.5;		
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
    c.gridy = 0;
		c.gridx = 0;
		c.insets = new java.awt.Insets(5,5,5,5);		

		JLabel messageLabel = new JLabel("Connecting to server...");
    c.gridwidth = 2;
		c.insets = new java.awt.Insets(5,5,5,5);
		container.add(messageLabel, c);
		
		busyProgressBar = new JProgressBar(0, 1);
		busyProgressBar.setPreferredSize(new Dimension(250, 17));
    c.gridwidth = 1;
    c.weightx = 1;
		c.gridy = 1;
		c.insets = new java.awt.Insets(0,5,5,5);
		container.add(busyProgressBar, c);
    
    /* cancelButton = new JButton("Cancel");
    c.gridx = 1;
    c.weightx = 0;
    c.insets = new java.awt.Insets(0,5,5,5);
    container.add(cancelButton, c); */    
		
		pack();
		centerOnOwner();
		setVisible(true);
	}
  
  /**
   * Associate this action listener with a cancel action.
   * 
   * @param al  action listener to use
   * @since     1.2
   */
  public void setCancelActionListener(ActionListener al) {
    // cancelButton.addActionListener(al);
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
	 * Set the progress bar to 0 progress.
	 *
	 * @since  1.2
	 */
	public void stop() {
		busyProgressBar.setIndeterminate(false);
	}
	
	/**
	 * Hide the dialog and dispose of it.
	 * 
	 * @since  1.2
	 */
	public void close() {
		setVisible(false);
    stop();        
		dispose();
	}
}