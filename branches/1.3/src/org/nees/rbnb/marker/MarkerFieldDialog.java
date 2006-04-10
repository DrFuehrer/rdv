package org.nees.rbnb.marker;
/**
* @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * Please see copywrite information at the end of this file.
 * @since 051108
 * $LastChangedDate: 2006-03-28 16:10:01 -0800 (Tue, 28 Mar 2006) $
 * $LastChangedRevision: 773 $
 * $LastChangedBy: ljmiller $
 * $HeadURL: https://svn.nees.org/svn/sandbox/ljmiller/boilerplate.java $
 */
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.rbnb.marker.SendMarkerRDVPanel;
public class MarkerFieldDialog extends JDialog implements KeyEventDispatcher {
  
 	static Log log = LogFactory.getLog (MarkerFieldDialog.class.getName());
	
  /////////// Event Markers
	JLabel headerLabel;
  JLabel markerTimeLabel;
  
	JLabel markerLabelLabel;
	JTextField markerLabelTextField;
  SendMarkerRDVPanel myCaller;
	JLabel markerContentLabel;
	JTextField markerContentTextField;
  /////////// Event Markers
  
	JButton connectButton;
	JButton cancelButton;
	
	/** constructor */
  public MarkerFieldDialog (JFrame owner, double markerTimeStamp, SendMarkerRDVPanel markerSender) {
		super (owner, true);
		
    this.myCaller = markerSender;
    setDefaultCloseOperation (DISPOSE_ON_CLOSE);
    addWindowListener (new WindowAdapter () {
			public void windowActivated (WindowEvent e) {
				bindKeys ();
			}
			public void windowDeactivated (WindowEvent e) {
				unbindKeys ();
			}
		}); // addWindowListener
		
		setTitle ("Submit Event Marker");
    
		getContentPane ().setLayout (new GridBagLayout ());
		GridBagConstraints c = new GridBagConstraints ();
		c.weighty    = 1;
		c.gridwidth  = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets (10, 10, 10, 10);
		
		headerLabel = new JLabel ("Please specify the label and content of your " +
                              "event marker for time:");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
		getContentPane ().add (headerLabel, c);
    
    markerTimeLabel = new JLabel (DataViewer.formatDate (markerTimeStamp));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
    getContentPane ().add (markerTimeLabel, c);
    
		c.gridwidth = 1;
		
		markerLabelLabel = new JLabel ("Label");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx   = 0;
		c.gridy   = 2;
		c.anchor  = GridBagConstraints.NORTHEAST;
		getContentPane ().add (markerLabelLabel, c);
		
		markerLabelTextField = new JTextField ();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx   = 1;
		c.gridy   = 2;
		c.anchor  = GridBagConstraints.NORTHWEST;
		getContentPane ().add (markerLabelTextField, c);
		
		markerContentLabel = new JLabel ("Content");
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx   = 0;
		c.gridy   = 3;
		c.anchor  = GridBagConstraints.NORTHEAST;
		getContentPane ().add (markerContentLabel, c);
    
		markerContentTextField = new JTextField ();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx   = 1;
		c.gridy   = 3;
		c.anchor  = GridBagConstraints.NORTHWEST;
		getContentPane ().add (markerContentTextField, c);
		
		JPanel buttonPanel = new JPanel ();
				
		connectButton = new JButton ("Submit");
		connectButton.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				submitMarker ();
			}
		}); // connectButton
		buttonPanel.add (connectButton);
		
		cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				cancel ();
			}
		});
		buttonPanel.add (cancelButton);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0.5;
		c.gridx     = 0;
		c.gridy     = 4;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane ().add (buttonPanel, c);
		
		pack ();
		setVisible (true);
	} // constructor ()
  
	
	public void setVisible (boolean visible) {
		if (visible) {
			markerLabelTextField.requestFocusInWindow ();
	 		//markerLabelTextField.setSelectionStart (0);
	 		//markerLabelTextField.setSelectionEnd (markerLabelTextField.getText ().length ());			
		}
		super.setVisible (visible);
	} // setVisible ()
  
	
	private void submitMarker () {
		String markerLabel   = null;
    String markerContent = null;
    
    markerLabel   = markerLabelTextField.getText ();
    markerContent = markerContentTextField.getText ();
    
    myCaller.myEvent.setProperty ("label", markerLabel);
    myCaller.myEvent.setProperty ("content", markerContent);
    super.dispose ();
	} // submitMarker ()
	
  
	private void cancel () {
		super.dispose ();		
	} // cancel ()
  
	
	private void bindKeys () {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager ();
 		focusManager.addKeyEventDispatcher (this);		
	} // bindKeys ()
	
  
	private void unbindKeys () {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager ();
 		focusManager.removeKeyEventDispatcher (this);
	} // unbindKeys ()
  
  
 	public boolean dispatchKeyEvent (KeyEvent keyEvent) {
 		int keyCode = keyEvent.getKeyCode ();
 		
 		if (keyCode == KeyEvent.VK_ENTER) {
 			submitMarker ();
 			return true;
 		} else if (keyCode == KeyEvent.VK_ESCAPE) {
 			cancel ();
 			return true;
 		} else {
 			return false;
 		}
 	} //  dispatchKeyEvent ()
  
} // class

/** Copyright (c) 2005, 2006, Lawrence J. Miller and NEESit
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*   * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*   * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the 
* documentation and/or other materials provided with the distribution.
*   * Neither the name of the San Diego Supercomputer Center nor the names of
* its contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
                         * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
                         * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/

