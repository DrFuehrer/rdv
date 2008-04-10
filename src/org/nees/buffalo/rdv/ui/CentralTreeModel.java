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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.nees.data.Central;
import org.nees.data.DataFile;
import org.nees.data.Experiment;
import org.nees.data.Project;
import org.nees.data.Repetition;
import org.nees.data.Trial;

/**
 * A tree model for NEEScentral.
 * 
 * @author Jason P. Hanley
 *
 */
public class CentralTreeModel implements TreeModel {
  /** the root of the tree */
  private final Central root;
  
  /** the tree model listeners */
  private List<TreeModelListener> listeners;
  
  /**
   * Creates the cnetral tree model with the given root.
   * 
   * @param root  the root of the tree
   */
  public CentralTreeModel(Central root) {
    this.root = root;
    
    listeners = new ArrayList<TreeModelListener>();
  }

  public Object getChild(Object parent, int index) {
    return getChildren(parent).get(index);
  }

  public int getChildCount(Object parent) {
    return getChildren(parent).size();
  }

  public int getIndexOfChild(Object parent, Object child) {
    return getChildren(parent).indexOf(child);
  }

  public Object getRoot() {
    return root;
  }

  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }
  
  /**
   * Gets a list of children for the given node.
   * 
   * @param node  the parent node
   * @return      the children of the parent node
   */
  private List getChildren(Object node) {
    ArrayList children = new ArrayList();
    
    if (node instanceof Central) {
      Central central = (Central)node;
      children.addAll(central.getProject());
    } else if (node instanceof Project) {
      Project project = (Project)node;
      children.addAll(project.getExperiment());
      children.addAll(project.getDataFile());
    } else if (node instanceof Experiment) {
      Experiment experiment = (Experiment)node;
      children.addAll(experiment.getTrial());
      children.addAll(experiment.getDataFile());
    } else if (node instanceof Trial) {
      Trial trial = (Trial)node;
      children.addAll(trial.getRepetition());
      children.addAll(trial.getDataFile());
    } else if (node instanceof Repetition) {
      Repetition repetition = (Repetition)node;
      children.addAll(repetition.getDataFile());
    } else if (node instanceof DataFile) {
      DataFile dataFile = (DataFile)node;
      children.addAll(dataFile.getDataFile());
    }
    
    return children;
  }
  
  /**
   * Sets the list of projects.
   * 
   * @param projects  the projects list
   */
  public void setProjects(List<Project> projects) {
    root.getProject().clear();
    root.getProject().addAll(projects);

    TreePath path = new TreePath(root);
    fireTreeStructureChanged(path);
  }
  
  /**
   * Updates the project.
   * 
   * @param project  the project to update
   */
  public void setProject(Project project) {
    List<Project> projects = root.getProject();
    int projectIndex = -1;
    
    for (int i=0; i<projects.size(); i++) {
      Project p = projects.get(i);
      if (project.getId().equals(p.getId())) {
        projectIndex = i;
        break;
      }
    }
    
    if (projectIndex == -1) {
      return;
    }
    
    projects.set(projectIndex, project);
    
    TreePath path = new TreePath(new Object[] {root, project});
    fireTreeStructureChanged(path);    
  }
  
  /**
   * Updates the experiment.
   * 
   * @param projectId  the project id for the experiment
   * @param experiment the experiment to update
   */
  public void setExperiment(int projectId, Experiment experiment) {
    Project project = null;
    for (Project p : root.getProject()) {
      if (projectId == p.getId()) {
        project = p;
        break;
      }
    }
    
    if (project == null) {
      return;
    }

    List<Experiment> experiments = project.getExperiment();
    int experimentIndex = -1;
    
    for (int i=0; i<experiments.size(); i++) {
      Experiment e = experiments.get(i);
      if (e.getId().equals(experiment.getId())) {
        experimentIndex = i;
        break;
      }
    }
    
    if (experimentIndex == -1) {
      return;
    }
    
    project.getExperiment().set(experimentIndex, experiment);
    
    TreePath path = new TreePath(new Object[] {root, project, experiment});
    fireTreeStructureChanged(path);    
  }
  
  /**
   * Updates the trial.
   * 
   * @param projectId     the project id for the trial
   * @param experimentId  the experiment id for the trial
   * @param trial         the trial to update
   */
  public void setTrial(int projectId, int experimentId, Trial trial) {
    Project project = null;
    for (Project p : root.getProject()) {
      if (projectId == p.getId()) {
        project = p;
        break;
      }
    }
    
    if (project == null) {
      return;
    }

    Experiment experiment = null;
    for (Experiment e : project.getExperiment()) {
      if (experimentId == e.getId()) {
        experiment = e;
        break;
      }
    }
    
    if (experiment == null) {
      return;
    }
    
    List<Trial> trials = experiment.getTrial();
    int trialIndex = -1;
    
    for (int i=0; i<trials.size(); i++) {
      Trial t = trials.get(i);
      if (t.getId().equals(trial.getId())) {
        trialIndex = i;
        break;
      }
    }
    
    if (trialIndex == -1) {
      return;
    }
    
    experiment.getTrial().set(trialIndex, trial);
    
    TreePath path = new TreePath(new Object[] {root, project, experiment, trial});
    fireTreeStructureChanged(path);
  }
  
  /**
   * Updates the repetition.
   * 
   * @param projectId     the project id for the repetition
   * @param experimentId  the experiment id for the repetition
   * @param trialId       the trial id for the repetition
   * @param repetition    the repetition id to update
   */
  public void setRepetition(int projectId, int experimentId, int trialId, Repetition repetition) {
    Project project = null;
    for (Project p : root.getProject()) {
      if (projectId == p.getId()) {
        project = p;
        break;
      }
    }
    
    if (project == null) {
      return;
    }

    Experiment experiment = null;
    for (Experiment e : project.getExperiment()) {
      if (experimentId == e.getId()) {
        experiment = e;
        break;
      }
    }
    
    if (experiment == null) {
      return;
    }

    Trial trial = null;
    for (Trial t : experiment.getTrial()) {
      if (trialId == t.getId()) {
        trial = t;
        break;
      }
    }
    
    if (trial == null) {
      return;
    }
    
    List<Repetition> repetitions = trial.getRepetition();
    int repetitionIndex = -1;
    
    for (int i=0; i<repetitions.size(); i++) {
      Repetition r = repetitions.get(i);
      if (r.getId().equals(repetition.getId())) {
        repetitionIndex = i;
        break;
      }
    }
    
    if (repetitionIndex == -1) {
      return;
    }
    
    trial.getRepetition().set(repetitionIndex, repetition);
    
    TreePath path = new TreePath(new Object[] {root, project, experiment, trial, repetition});
    fireTreeStructureChanged(path);    
  }
  
  /**
   * Updates the data file.
   * 
   * @param dataFile  the data file to update
   */
  public void setDataFile(DataFile dataFile) {
    List path = null;
    
    for (Project project : root.getProject()) {
      path = setDataFile(project.getDataFile(), dataFile);
      if (path != null) {        
        path.add(0, project);
        break;
      }
      
      for (Experiment experiment : project.getExperiment()) {
        path = setDataFile(experiment.getDataFile(), dataFile);
        if (path != null) {
          path.add(0, experiment);
          path.add(0, project);
          break;
        }
        
        for (Trial trial : experiment.getTrial()) {
          path = setDataFile(trial.getDataFile(), dataFile);
          if (path != null) {
            path.add(0, trial);
            path.add(0, experiment);
            path.add(0, project);
            break;
          }          
          
          for (Repetition repetition : trial.getRepetition()) {
            path = setDataFile(repetition.getDataFile(), dataFile);
            if (path != null) {
              path.add(0, repetition);
              path.add(0, trial);
              path.add(0, experiment);
              path.add(0, project);
              break;
            }            
          }
          
          if (path != null) {
            break;
          }
        }
        
        if (path != null) {
          break;
        }
      }
      
      if (path != null) {
        break;
      }
    }
    
    if (path != null) {
      path.add(0, root);
      TreePath treePath = new TreePath(path.toArray());
      fireTreeStructureChanged(treePath);
    }
  }
  
  /**
   * Searches the data files for the given data file and returns it path.
   * 
   * @param dataFiles  the data files to search
   * @param dataFile   the data file to search for
   * @return           the path to the data file, or null if it was not found
   */
  private List setDataFile(List<DataFile> dataFiles, DataFile dataFile) {
    for (int i=0; i<dataFiles.size(); i++) {
      DataFile df = dataFiles.get(i);
      if (dataFile.getPath().equals(df.getPath()) &&
          dataFile.getName().equals(df.getName())) {
        dataFiles.set(i, dataFile);

        List path = new ArrayList();
        path.add(dataFile);
        return path;
      }
    }
    
    for (DataFile df : dataFiles) {
      List path = setDataFile(df.getDataFile(), dataFile);
      if (path != null) {
        path.add(0, df);
        return path;
      }
    }
    
    return null;
  }
  
  /**
   * Notifies listeners that the tree structure has changed.
   * 
   * @param path  the path that has changed
   */
  private void fireTreeStructureChanged(TreePath path) {
    TreeModelEvent event = new TreeModelEvent(this, path);
    
    for (TreeModelListener l : listeners) {
      l.treeStructureChanged(event);
    }    
  }
  
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  public void valueForPathChanged(TreePath path, Object newValue) {}
}