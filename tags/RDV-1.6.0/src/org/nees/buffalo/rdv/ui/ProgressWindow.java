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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.swixml.SwingEngine;

/**
 * A dialog to display the progress of a task.
 * 
 * @author Jason P. Hanley
 */
public class ProgressWindow extends JDialog {
  private JProgressBar progressBar;
  
  private JLabel statusLabel;
  
  /**
   * Creates a ProgressWindow with no header text.
   */
  public ProgressWindow() {
    this(null);
  }

  /**
   * Creates a ProgressWindow with the supplied header text. The header text
   * will be displayed above the progress.
   * 
   * @param headerText  the text for the header
   */
  public ProgressWindow(String headerText) {
    super(SwingEngine.getAppFrame());
    
    initProgressWindow(headerText);
  }
  
  /**
   * Creates the window to shw progress. This creates a progress bar with an
   * area for status text below it and an area for header text above it.
   * 
   * @param headerText  the text for the header
   */
  private void initProgressWindow(String headerText) {
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
    setTitle(headerText);
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout(5, 5));
    setContentPane(container);
    
    if (headerText != null) {
      JLabel headerLabel = new JLabel("<html><font size=+1>" + headerText + "</font></html>");
      headerLabel.setBackground(Color.white);
      headerLabel.setOpaque(true);
      headerLabel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
          BorderFactory.createEmptyBorder(10,10,10,10)));
      container.add(headerLabel, BorderLayout.NORTH);
    }
    
    progressBar = new JProgressBar(0, 1000);
    progressBar.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 10, 5, 10),
      progressBar.getBorder()));
    progressBar.setPreferredSize(new Dimension(400, 40));
    progressBar.setStringPainted(true);
    progressBar.setValue(0);
    container.add(progressBar, BorderLayout.CENTER);
    
    statusLabel = new JLabel(" ");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
    container.add(statusLabel, BorderLayout.SOUTH);
    
    pack();
    setLocationByPlatform(true);
  }
  
  /**
   * Sets the progress of the task. The progress ranges from 0 to 1.
   * 
   * @param progress  the progress of the task
   */
  public void setProgress(float progress) {
    if (progress == -1) {
      progressBar.setIndeterminate(true);
      return;
    }
    
    if (progress < 0) {
      progress = 0;
    } else if (progress > 1) {
      progress = 1;
    }
    
    progressBar.setValue(Math.round(progress*1000));
  }
  
  /**
   * Sets the status text for the task.
   * 
   * @param status  the text of the status
   */
  public void setStatus(String status) {
    statusLabel.setText(status);
  }
}