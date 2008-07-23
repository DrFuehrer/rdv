/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 * Copyright (c) 2008 Palta Software
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.rdv.DataViewer;
import org.rdv.auth.Authentication;
import org.rdv.auth.AuthenticationManager;
import org.rdv.rbnb.EventMarker;
import org.rdv.rbnb.RBNBController;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * A panel that contains UI elements to collect the data needed to create an
 * event marker.
 * 
 * @author Lawrence J. Miller
 * @author Jason P. Hanley
 */
public class MarkerSubmitPanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = 7833670559216668914L;

  /**
   * The rbnb controller to interface with the server
   */
  private RBNBController rbnbController;
  
  /**
   * The combo box to select the marker type
   */
  private JComboBox markerTypeComboBox;
  
  /**
   * The text field to collect the content of the marker
   */
  private JTextField markerContentField;
  
  /**
   * The button to submit the marker with
   */
  private JButton markerSubmitButton;
  
  /**
   * A list of pre-defined marker types
   */
  private static final String[] markerTypes = {"annotation", "min", "max",
                                               "start", "stop"};
  
  /**
   * The time at which the user started to describe the event
   */
  double startTime;

  /**
   * Creates the marker submit panel with a content field and a submit button.
   * 
   * @param rbnbController  the rbnb controller to use for sending the marker
   */
  public MarkerSubmitPanel(RBNBController rbnbController) {
    super();
    
    this.rbnbController = rbnbController;
    
    initPanel();
    
    startTime = -1;
  }
  
  /**
   * Create the UI.
   */
  private void initPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
        
    JPanel p = new JPanel();
    p.setBorder(new EmptyBorder(5,5,5,5));
    p.setLayout(new BorderLayout(5, 5));
    
    markerTypeComboBox = new JComboBox(markerTypes);
    markerTypeComboBox.setEditable(true);
    markerTypeComboBox.setToolTipText("The type of event");
    markerTypeComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        markerContentField.requestFocus();
      }      
    });    
    p.add(markerTypeComboBox, BorderLayout.WEST);

    markerContentField = new JTextField();
    markerContentField.setToolTipText("Describe the event");
    markerContentField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        submitMarker();
      }      
    });
    
    // See when text is first entered so we can get an accurate timestamp for
    // marker submission.
    markerContentField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent de) {
        if (startTime == -1) {
          startTime = rbnbController.getLocation();
        }        
      }
      public void removeUpdate(DocumentEvent de) {
        // reset the start time if all text is removed
        if (de.getDocument().getLength() == 0) {
          startTime = -1;
        }
      }
      public void changedUpdate(DocumentEvent de) {}
    });
    p.add(markerContentField, BorderLayout.CENTER);
    
    markerSubmitButton = new JButton("Submit");
    markerSubmitButton.setToolTipText("Mark this event");
    markerSubmitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        submitMarker();
      }      
    });
    p.add(markerSubmitButton, BorderLayout.EAST);

    SimpleInternalFrame sif = new SimpleInternalFrame(
        DataViewer.getIcon("icons/info.gif"),
        "Event Marker Panel",
        null,
        p);

    add(sif, BorderLayout.CENTER);
  }
  
  /**
   * Submit an event marker with the text in the text field as it's content. If
   * an error occurs, a dialog will be shown describing the error.
   */
  private void submitMarker() {
    // make sure type is valid
    String type = markerTypeComboBox.getSelectedItem().toString();
    if (type == null || !type.matches("\\w{1,16}")) {
      JOptionPane.showMessageDialog(this,
          "The event marker was not submitted since the type is invalid.\n" +
          "The type must be at most 16 characters long and only contain alpha numeric characters.",
          "Marker Not Submitted",
          JOptionPane.WARNING_MESSAGE);
      markerTypeComboBox.requestFocus();
      return;
    }
    
    // only submit marker in contents is not empty
    String content = markerContentField.getText();
    if (type.equals("annotation") && (content == null || content.length() == 0)) {
      JOptionPane.showMessageDialog(this,
          "The event marker was not submitted since there was no content.\n" +
          "Please describe the event using the text area in the marker panel.",
          "Marker Not Submitted",
          JOptionPane.WARNING_MESSAGE);
      markerContentField.requestFocus();
      return;
    }
    
    EventMarker marker = new EventMarker();
    
    marker.setProperty("type", type);
    marker.setProperty("content", content);
    
    if (startTime == -1) {
      startTime = rbnbController.getLocation();
    }
    marker.setProperty("timestamp", Double.toString(startTime));
    
    Authentication authentication = AuthenticationManager.getInstance().getAuthentication();
    if (authentication != null) {
      String username = authentication.get("username");
      if (username != null || username.length() > 0) {
        marker.setProperty("source", username);
      }
    }
    
    try {
      rbnbController.getMarkerManager().putMarker(marker);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Failed to submit event marker.", "Marker Submission Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }

    markerContentField.setText(null);
  }
  
  /**
   * Enable or disable the component. When disabled no user input can be made.
   * 
   * @param enabled  if true, enable the component, otherwise disable the
   *                 component
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    markerTypeComboBox.setEnabled(enabled);
    markerContentField.setEnabled(enabled);
    markerSubmitButton.setEnabled(enabled);
  }
}