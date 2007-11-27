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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.data.Central;
import org.nees.data.DataFile;
import org.nees.data.Experiment;
import org.nees.data.Project;
import org.nees.data.Repetition;
import org.nees.data.Trial;

/**
 * A class to render a tree cell for the NEEScentral tree.
 * 
 * @author Jason P. Hanley
 */
public class CentralTreeCellRenderer extends DefaultTreeCellRenderer {
  public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, row, hasFocus);
    
    if (node instanceof Central) {
      setText("NEEScentral");
      setIcon(DataViewer.getIcon("icons/folder.gif"));
    } else if (node instanceof Project) {
      Project project = (Project)node;
      if (project.getName() != null) {
        if (project.getNickname() != null) {
          setText(project.getName() + ": " + project.getNickname().getValue());
        } else {
          setText(project.getName());
        }
      }
      setIcon(DataViewer.getIcon("icons/folder.gif"));
    } else if (node instanceof Experiment) {
      Experiment experiment = (Experiment)node;
      if (experiment.getName() != null) {
        if (experiment.getTitle() != null) {
          setText(experiment.getName() + ": " + experiment.getTitle());
        } else {
          setText(experiment.getName());
        }
      }
      setIcon(DataViewer.getIcon("icons/folder.gif"));
    } else if (node instanceof Trial) {
      Trial trial = (Trial)node;
      if (trial.getName() != null) {
        if (trial.getTitle() != null) {
          setText(trial.getName().getValue() + ": " + trial.getTitle());
        } else {
          setText(trial.getName().getValue());
        }
      }
      setIcon(DataViewer.getIcon("icons/folder.gif"));
    } else if (node instanceof Repetition) {
      Repetition repetition = (Repetition)node;
      if (repetition.getName() != null) {
        setText(repetition.getName().getValue());
      }
      setIcon(DataViewer.getIcon("icons/folder.gif"));
    } else if (node instanceof DataFile) {
      DataFile dataFile = (DataFile)node;
      if (dataFile.getName() != null) {
        setText(dataFile.getName());
      }
      if (dataFile.isIsDirectory()) {
        setIcon(DataViewer.getIcon("icons/folder.gif"));
      } else {
        setIcon(DataViewer.getIcon("icons/file.gif"));
      }
    }
    
    return c;
  }
}