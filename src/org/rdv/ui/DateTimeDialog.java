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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

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
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.rdv.DataViewer;

import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A dialog to select a data and time.
 * 
 * @author Jason P. Hanley
 */
public class DateTimeDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = -1199147835851699381L;

  /** the initial date and time to display */
  private Date initialDate;
  
  /** the selected date and time */
  private double dateTime;

  /** the minimum time allowed */
  private double minimumDateTime;
  
  /** the maximum time allowed */
  private double maximumDateTime;
  
  /** the textfield for the date */
  private JTextField dateTextField;
  
  /** the textfield for the time */
  private JTextField timeTextField;
  
  /** the label used to display errors in parsing the date and time */
  private JLabel errorLabel;
	
	/** format to use to display the date */
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  
  /** format to use to display the time */
  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");

  /** format used to parse the date and time */
  private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
  
  /** parse dates strict */
  static {
    DATE_FORMAT.setLenient(false);
    TIME_FORMAT.setLenient(false);
    DATE_TIME_FORMAT.setLenient(false);
  }
  
  /**
   * Show a dialog to select a date and time.
   * 
   * @param frameComponent   the component to use to find the parent frame
   * @param initialDateTime  the inital date and time to use
   * @return                 the selected date and time, or -1 if the dialog was
   *                         canceled
   */
  public static double showDialog(Component frameComponent, double initialDateTime) {
    return showDialog(frameComponent, initialDateTime, 0, Double.MAX_VALUE);
  }
    
  /**
   * Show a dialog to select a date and time. The selected time will be bounded
   * by the minimum and maximum times.
   * 
   * @param frameComponent   the component to use to find the parent frame
   * @param initialDateTime  the inital date and time to use
   * @param minimumDateTime  the minimum date and time allowed to be selected
   * @param maximumDateTime  the maximum date and time allowed to be selected
   * @return                 the selected date and time, or -1 if the dialog was
   *                         canceled
   */
  public static double showDialog(Component frameComponent, double initialDateTime, double minimumDateTime, double maximumDateTime) {
    Frame frame = JOptionPane.getFrameForComponent(frameComponent);
    DateTimeDialog dialog = new DateTimeDialog(frame, initialDateTime, minimumDateTime, maximumDateTime);
    dialog.setVisible(true);
    return dialog.getDateTime();
  }
  
  /**
   * Creates a dialog to selected a date and time.  The selected time will be
   * bounded by the minimum and maximum times.
   * 
   * @param owner            the parent frame
   * @param initialDateTime  the initial date and time to use
   * @param minimumDateTime  the minimum date and time allowed to be selected
   * @param maximumDateTime  the maximum date and time allowed to be selected
   */
  private DateTimeDialog(Frame owner, double initialDateTime, double minimumDateTime, double maximumDateTime) {
		super(owner, true);
		
    initialDate = new Date(((long)(initialDateTime*1000)));

    // set the selected date and time to unselected
    dateTime = -1;
    
    this.minimumDateTime = minimumDateTime;
    this.maximumDateTime = maximumDateTime;
    
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setTitle("Select date and time");
    
		JPanel container = new JPanel();
		setContentPane(container);
    
		InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = container.getActionMap();
			
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
    c.weighty = 0;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		
    JLabel headerLabel = new JLabel("<html>Please enter the date and time. " +
        "Enter the date using the<br>yyyy-mm-dd format and the time using " +
        "the hh:mm:ss format.<br>You may optionally specify a time" +
        "zone, otherwise your local<br>time zone is used.</html>");
		headerLabel.setBackground(Color.white);
		headerLabel.setOpaque(true);
		headerLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(10,10,10,10)));
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;    
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.insets = new Insets(0,0,0,0);
		container.add(headerLabel, c);
		
		errorLabel = new JLabel();
    errorLabel.setForeground(Color.RED);
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
    c.insets = new Insets(10,10,0,10);
		container.add(errorLabel, c);
    
    c.gridwidth = 1;
    
		JLabel dateLable = new JLabel("Date:");
    c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new Insets(10,10,10,5);
		container.add(dateLable, c);
		
		dateTextField = new JTextField(DATE_FORMAT.format(initialDate), 10);
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(10,0,10,10);    
		container.add(dateTextField, c);
		
		JLabel timeLabel = new JLabel("Time:");
    c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 3;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new Insets(0,10,10,5);    
		container.add(timeLabel, c);

		timeTextField = new JTextField(TIME_FORMAT.format(initialDate), 10);
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 3;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new Insets(0,0,10,10);        
		container.add(timeTextField, c);

		JPanel buttonPanel = new JPanel();
    
    Action jumpLocationAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ok();
      }      
    };    
    jumpLocationAction.putValue(Action.NAME, "Select date and time");
    
		JButton jumpButton = new JButton(jumpLocationAction);
    getRootPane().setDefaultButton(jumpButton);
		buttonPanel.add(jumpButton);
		
    Action cancelAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }      
    };
    cancelAction.putValue(Action.NAME, "Cancel");
    
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    actionMap.put("cancel", cancelAction);
    
    JButton cancelButton = new JButton(cancelAction);
  	buttonPanel.add(cancelButton);
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
    c.weighty = 1;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0,10,10,5);
		container.add(buttonPanel, c);
    
    pack();
    setLocationByPlatform(true);
	}
  
  /**
   * Returns the date and time entered by the user. If there is an error parsing
   * this, an exception will be thrown.
   * 
   * @return                 the date and time entered by the user
   * @throws ParseException  if there is an error parsing the user enter date
   *                         and time
   */
  private double parseDateTime() throws ParseException {
    // try and parse the date to see if it is ok
    String dateString = dateTextField.getText().trim();
    DATE_FORMAT.parse(dateString);       

    // try and parse the time to see if it is ok
    String timeString = timeTextField.getText().trim();
    try {
      TIME_FORMAT.parse(timeString);
    } catch (ParseException e) {
      // try and parse with the local time zone
      TimeZone tz = TimeZone.getDefault();
      String tzString = tz.getDisplayName(tz.inDaylightTime(new Date()), TimeZone.SHORT);
      timeString += " " + tzString;
      TIME_FORMAT.parse(timeString);
    }

    String dateTimeString = dateString + " " + timeString;

    // parse the full date and time
    Date dateTime = DATE_TIME_FORMAT.parse(dateTimeString);
    double dateTimeInSeconds = dateTime.getTime() / 1000;  // convert to seconds
    
    return dateTimeInSeconds;
  }
  
  /**
   * Gets the date and time entered by the user. If the dialog was canceled, -1
   * will be returned.
   * 
   * @return  the date and time entered by the user, or -1 if the dialog was
   *          canceled
   */
  public double getDateTime() {
    return dateTime;
  }
  
  public void setVisible(boolean visible) {
    // make sure we reset the selection if the dialog becomes visible again
    if (visible && !isVisible()) {
      dateTime = -1;
      
      errorLabel.setText(null);
      pack();
    }

    super.setVisible(visible);    
  }
  
  /**
   * Show the error message in the UI.
   * 
   * @param message  the error message text
   */
  private void showError(String message) {
    errorLabel.setText(message);

    timeTextField.setText(TIME_FORMAT.format(initialDate));
    
    dateTextField.setText(DATE_FORMAT.format(initialDate));
    dateTextField.requestFocusInWindow();
    
    pack();    
  }

  /**
   * Parse the data and time. If the date and time are not within
   * the range of available data, an error will be displayed. If the date and
   * time are not formatted correctly, an error will be displayed. If the date
   * and time are fine, the dialog will be closed.
   */
	private void ok() {
    double dateTime;
    try {
      dateTime = parseDateTime();
    } catch (ParseException e) {
      showError("<html>The date and time entered are invalid. Please use the " +
      "format specified<br>above.</html>");
      return;
    }
    
    if (dateTime < minimumDateTime || dateTime > maximumDateTime) {
      showError("<html>The specified date and time is outside the range of " +
          "available data. Please<br>specify a date and time between " +
          DataViewer.formatDate(minimumDateTime) + " and <br>" +
          DataViewer.formatDate(maximumDateTime) + ".</html>");
      return;
    }
    
    this.dateTime = dateTime;
    
    dispose();
	}
  
  /**
   * Cancel the dialog operation. This will close the dialog.
   */
	private void cancel() {
    dispose();
	}
}