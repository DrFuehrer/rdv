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
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A dialog to display the progress of a task.
 * 
 * @author Jason P. Hanley
 */
public class ProgressWindow extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = 2629422248834179713L;
  
  /** the header label */
  private JLabel headerLabel;

  /** the progress bar */
  private JProgressBar progressBar;
  
  /** the status label */
  private JLabel statusLabel;
  
  /**
   * Creates a ProgressWindow with the supplied title.
   * 
   * @param frame  the parent frame
   * @param title  the title for the frame
   */
  public ProgressWindow(JFrame frame, String title) {
    super(frame, title);
    
    initProgressWindow();
  }
  
  /**
   * Creates a window to show progress. This creates a progress bar with an
   * area for status text below it and an area for header text above it.
   */
  private void initProgressWindow() {
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    setContentPane(container);
    
    headerLabel = new JLabel();
    headerLabel.setBackground(Color.white);
    headerLabel.setOpaque(true);
    headerLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(10,10,10,10)));
    container.add(headerLabel, BorderLayout.NORTH);
    
    Box bodyPanel = new Box(BoxLayout.PAGE_AXIS);
    bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    progressBar = new JProgressBar(0, 1000);
    progressBar.setAlignmentX(LEFT_ALIGNMENT);
    progressBar.setPreferredSize(new Dimension(400, progressBar.getPreferredSize().height));
    progressBar.setStringPainted(true);
    progressBar.setValue(0);
    bodyPanel.add(progressBar);
    
    bodyPanel.add(Box.createVerticalStrut(10));
    
    statusLabel = new JLabel(" ");
    statusLabel.setAlignmentX(LEFT_ALIGNMENT);
    bodyPanel.add(statusLabel);

    container.add(bodyPanel, BorderLayout.CENTER);

    setHeader(getTitle());
    
    pack();
    setLocationByPlatform(true);
  }
  
  /**
   * Sets the header text.
   * 
   * @param header  the header text
   */
  public void setHeader(String header) {
    headerLabel.setText("<html><font size=+1>" + header + "</font></html>");    
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