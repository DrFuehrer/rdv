/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
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

package org.rdv.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;

import org.jdesktop.application.Task;
import org.jdesktop.swingworker.SwingWorker.StateValue;
import org.rdv.ui.ProgressWindow;
import org.rdv.ui.UIUtilities;

/**
 * A class to control a ProgressWindow with the properties of a Task.
 * 
 * @author Jason P. Hanley
 */
public class ProgressWindowController implements PropertyChangeListener {

  /** the task to follow */
  private final Task<?, ?> task;
  
  /** the progress window to control */
  private ProgressWindow progressWindow;
  
  /**
   * Creates an instance of this class that will update the progress window with
   * the status of the task.
   * 
   * @param task  the task to follow
   */
  public ProgressWindowController(Task<?, ?> task) {
    this.task = task;

    initProgressWindow();

    task.addPropertyChangeListener(this);
  }
  
  /**
   * Initialize the progress window with the properties of a task and display
   * it.
   */
  private void initProgressWindow() {
    progressWindow = new ProgressWindow(UIUtilities.getMainFrame(),
        task.getTitle());
    progressWindow.setStatus(task.getMessage());
    progressWindow.setProgress(task.getProgress() / 100f);
    progressWindow.setVisible(true);
  }

  /**
   * Listens for property changes in the task and updates the progress window.
   * This will listen to the title, message, progress and state of the task.
   */
  public void propertyChange(PropertyChangeEvent pce) {
    String name = pce.getPropertyName();
    if (name.equals("title")) {
      progressWindow.setHeader((String) pce.getNewValue());
    } else if (name.equals("message")) {
      progressWindow.setStatus((String) pce.getNewValue());
    } else if (name.equals("progress")) {
      float progress = ((Integer) pce.getNewValue()) / 100f;
      progressWindow.setProgress(progress);
    } else if (name.equals("state")) {
      StateValue state = (StateValue) pce.getNewValue();
      if (state == StateValue.DONE) {
        done();
      }
    }
  }

  /**
   * Called when the task is done. This will close and cleanup the progress
   * window.
   * 
   * If the task completed successfully a message dialog will popup. If the task
   * did not complete successfully, and error dialog will popup. These dialogs
   * will display the tasks title and message.
   * 
   * If the tasks message is null or if this task was canceled nothing will be
   * displayed.
   */
  private void done() {
    task.removePropertyChangeListener(this);

    progressWindow.dispose();
    progressWindow = null;

    // see if the task completed successfully
    boolean error = false;
    try {
      task.get();
    } catch (CancellationException ce) {
      return;
    } catch (Exception e) {
      error = true;
    }
    
    // if there is no message, don't display a message dialog
    if (task.getMessage() == null) {
      return;
    }
    
    int dialogType = error ? JOptionPane.ERROR_MESSAGE
                           : JOptionPane.INFORMATION_MESSAGE;

    JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
        task.getMessage(),
        task.getTitle(),
        dialogType);
  }
  
}