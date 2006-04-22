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
import com.rbnb.sapi.SAPIException;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.rbnb.marker.SendMarkerRDVPanel;

public class MarkerFieldDialog extends JDialog {
  
 	static Log log = LogFactory.getLog (MarkerFieldDialog.class.getName());
	
	private JLabel headerLabel;
  private JLabel markerTimeLabel;
  private JLabel markerLabelLabel;
	private JTextField markerLabelTextField;
  private SendMarkerRDVPanel myCaller;
	private JLabel markerContentLabel;
	private JTextField markerContentTextField;
  private double markerTimeStamp;
	private JButton submitButton;
	private JButton cancelButton;
	
	/** constructor */
  public MarkerFieldDialog (JFrame owner, double markerTime, SendMarkerRDVPanel markerSender) {
		super (owner, true);
		
    this.myCaller = markerSender;
    this.markerTimeStamp = markerTime;
    setDefaultCloseOperation (DISPOSE_ON_CLOSE);
		
		setTitle ("Submit Event Marker");
    JPanel container = new JPanel ();
    setContentPane (container);

    InputMap inputMap = container.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap ();
    
		container.setLayout (new GridBagLayout ());
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
		
		Action submitAction = new AbstractAction () {
      public void actionPerformed (ActionEvent e) {
        submitMarker ();
      }      
    };
    submitAction.putValue (Action.NAME, "Submit");
    inputMap.put (KeyStroke.getKeyStroke ("ENTER"), "submit");
    actionMap.put("submit", submitAction);
		submitButton = new JButton (submitAction);
		buttonPanel.add (submitButton);
		
    Action cancelAction = new AbstractAction () {
      public void actionPerformed (ActionEvent e) {
        cancel ();
      }      
    };
    cancelAction.putValue (Action.NAME, "Cancel");
    inputMap.put (KeyStroke.getKeyStroke ("ESCAPE"), "cancel");
    actionMap.put ("cancel", cancelAction);    
    cancelButton = new JButton (cancelAction);
    buttonPanel.add (cancelButton);
    
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx   = 0.5;
		c.gridx     = 0;
		c.gridy     = 4;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane ().add (buttonPanel, c);
		
		pack ();
    setLocationByPlatform(true);
		setVisible (true);
	} // constructor ()
  
	
	public void setVisible (boolean visible) {
		if (visible) {
			markerLabelTextField.requestFocusInWindow ();
		}
		super.setVisible (visible);
	} // setVisible ()
  
	
	private void submitMarker () {
		String markerLabel   = null;
    String markerContent = null;
    
    markerLabel   = markerLabelTextField.getText ();
    markerContent = markerContentTextField.getText ();
    
    this.myCaller.myEvent.setProperty ("label", markerLabel);
    this.myCaller.myEvent.setProperty ("content", markerContent);
    this.myCaller.myEvent.setProperty ("timestamp", Double.toString (this.markerTimeStamp));
    
    /* This check has been supplanted by the connect () method in
      @see org.nees.rbnb.marker.SendMarkerRDVPanel - left here for future reference.
    if (this.myCaller.rbnbServerName.compareTo (this.myCaller.rdvRbnb.getRBNBHostName ()) != 0) {
      // then the DataTurbine server has changed; we need to change to keep with it
      try {
        this.myCaller.changeTurbine (this.myCaller.rdvRbnb.getRBNBHostName ());
      } catch (SAPIException sae) {
        log.error ("Couldn't change RBNB servers: " + sae);
      }
    } // if
    */
    
    try {
      this.myCaller.myTurban.putMarker (this.myCaller.myEvent, this.myCaller.myEvent.rbnbChannel);
    } catch (Exception ex) {
      log.error ("Error putting the XML into the Turbine: " + ex);
      ex.printStackTrace ();
    }
    
    dispose ();
	} // submitMarker ()
	
  
	private void cancel () {
    dispose ();		
	} // cancel ()
  
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

