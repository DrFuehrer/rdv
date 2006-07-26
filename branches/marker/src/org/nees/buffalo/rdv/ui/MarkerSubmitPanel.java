/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.rbnb.marker.NeesEvent;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * A panel that contains UI elements to collect the data needed to create an
 * event marker.
 * 
 * @author Lawrence J. Miller
 * @author Jason P. Hanley
 */
public class MarkerSubmitPanel extends JPanel {
  /**
   * The rbnb controller to interface with the server
   */
  private RBNBController rbnbController;
  
  /**
   * The text field to collect the content of the marker
   */
  private JTextField markerContentField;
  
  /**
   * The button to submit the marker with
   */
  private JButton markerSubmitButton;
  
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
        "Marker Panel",
        null,
        p);

    add(sif, BorderLayout.CENTER);
  }
  
  /**
   * Submit an event marker with the text in the text field as it's content. If
   * an error occurs, a dialog will be shown describing the error.
   */
  private void submitMarker() {
    String content = markerContentField.getText();
    
    // only submit marker in contents is not empty
    if (content == null || content.length() == 0) {
      return;
    }
    
    NeesEvent marker = new NeesEvent();
    
    marker.setProperty ("type", "annotation");
    
    marker.setProperty("content", content);
    
    if (startTime == -1) {
      startTime = rbnbController.getLocation();
    }
    marker.setProperty("timestamp", Double.toString(startTime));
    
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
    
    markerContentField.setEnabled(enabled);
    markerSubmitButton.setEnabled(enabled);
  }
}