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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.nees.buffalo.rdv.AppProperties;
import org.nees.buffalo.rdv.action.ActionFactory;
import org.nees.buffalo.rdv.auth.Authentication;
import org.nees.buffalo.rdv.auth.AuthenticationManager;
import org.nees.central.CentralException;
import org.nees.central.CentralClient;
import org.nees.data.Central;
import org.nees.data.DataFile;
import org.nees.data.Experiment;
import org.nees.data.ObjectFactory;
import org.nees.data.Project;
import org.nees.data.Repetition;
import org.nees.data.Trial;
import org.swixml.SwingEngine;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
/**
 * A dialog to browse NEEScentral and select data files to import.
 * 
 * @author Jason P. Hanley
 */
public class CentralImportDialog {
  /** the dialog */
  private JDialog dialog;
  
  /** the NEEScentral tree mode */
  private CentralTreeModel centralTreeModel;
  
  /** the NEEScentral tree */
  private JTree centralTree;
  
  /** the list of data files to import */
  private JList dataFileList;
  
  /** the import button */
  private JButton importButton;
  
  /** the cancel button */
  private JButton cancelButton;
  
  /** the NEEScentral client */
  private CentralClient centralClient;
  
  /** the list of tree path's already populated */
  private List<TreePath> populatedTreePaths;
  
  /**
   * Creates the NEEScentral import dialog.
   */
  public CentralImportDialog() {
    populatedTreePaths = new ArrayList<TreePath>();
    
    setupUI();
    
    spawnInitialPopulationThread();
  }
  
  /**
   * Setup the UI components.
   */
  private void setupUI() {
    try {
      new SwingEngine(this).render("ui/CentralImportDialog.xml");
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    
    setupCentralTree();
    setupDataFileList();
    setupButtons();
    
    dialog.setLocationByPlatform(true);
    dialog.setVisible(true);    
  }
  
  /**
   * Setup the NEEScentral tree UI component.
   */
  public void setupCentralTree() {    
    Central central = new ObjectFactory().createCentral();
    
    centralTreeModel = new CentralTreeModel(central);
    centralTree.setModel(centralTreeModel);
    centralTree.setDragEnabled(true);
    centralTree.setTransferHandler(new TreeDataFileTransferHandler());    
    centralTree.setCellRenderer(new CentralTreeCellRenderer());
    
    centralTree.addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {}
      public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        treeNodeExpanding(e.getPath());
      }
    });
    
    centralTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        treeNodeClicked(e);
      }
    });
  }
  
  /**
   * Setup the data file list UI component.
   */
  private void setupDataFileList() {    
    dataFileList.setModel(new DefaultListModel());
    dataFileList.setCellRenderer(new CentralDataFileListCellRenderer());
    
    dataFileList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteDataFile");
    dataFileList.getActionMap().put("deleteDataFile", new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        deleteSelectedDataFiles();
      }
    });
    
    dataFileList.getModel().addListDataListener(new ListDataListener() {
      public void contentsChanged(ListDataEvent lde) { listContentsChanged(); }
      public void intervalAdded(ListDataEvent lde) { listContentsChanged(); }
      public void intervalRemoved(ListDataEvent lde) { listContentsChanged(); }
    });
    
    new DropTarget(dataFileList, new DataFileListDropTargetListener());
  }
  
  /**
   * Setup the import and cancel buttons.
   */
  private void setupButtons() {
    importButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        startImport();
      }
    });
    
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        dialog.dispose();
      }
    });
  }
  
  /**
   * Sets the visibility of the dialog.
   * 
   * @param visible  if true the dialog is visible, othwise it is not visible
   */
  public void setVisible(boolean visible) {
    dialog.setVisible(visible);
  }
  
  /**
   * Starts a thread that sets up the NEEScentral client and populates the root
   * objects with projects.
   */
  private void spawnInitialPopulationThread() {
    new Thread() {
      public void run() {
        try {
          setupCentralClient();
        } catch (CentralException e) {
          handleCentralException(e);
          return;
        }
        
        populateCentral();
      }
    }.start();    
  }
  
  /**
   * Setup the NEEScentral client.
   * 
   * @throws CentralException  if there is an error creating the client
   */
  private void setupCentralClient() throws CentralException {
    Authentication authentication = AuthenticationManager.getInstance().getAuthentication();
    String session;
    if (authentication != null) {
      session = authentication.get("session");
    } else {
      session = null;
    }
    
    centralClient = new CentralClient(session);
    
    String centralHostName = AppProperties.getProperty("central.hostname", "central.nees.org");
    centralClient.setHostname(centralHostName);
  }
  
  /**
   * Populates the children of the given tree node.
   * 
   * @param treePath  the path to the tree node
   */
  private void populateNodeChildren(TreePath treePath) {
    if (populatedTreePaths.contains(treePath)) {
      return;
    } else {
      populatedTreePaths.add(treePath);
    }

    Object node = treePath.getLastPathComponent();
    
    if (node instanceof Project) {
      Project project = (Project)node;
      populateProject(project);
    } else if (node instanceof Experiment) {
      Project project = (Project)treePath.getParentPath().getLastPathComponent();
      Experiment experiment = (Experiment)node;
      populateExperiment(project, experiment);
    } else if (node instanceof Trial) {
      Project project = (Project)treePath.getPathComponent(treePath.getPathCount()-3);
      Experiment experiment = (Experiment)treePath.getPathComponent(treePath.getPathCount()-2);
      Trial trial = (Trial)node;
      populateTrial(project, experiment, trial);
    } else if (node instanceof Repetition) {
      Repetition repetition = (Repetition)node;
      populateRepetition(repetition);
    } else if (node instanceof DataFile) {
      DataFile dataFile = (DataFile)node;
      populateDataFiles(dataFile);
    }
  }
  
  /**
   * Populate the children of the central node.
   */
  private void populateCentral() {
    List<Project> projects;
    try {
      projects = centralClient.getProjects();
    } catch (CentralException e) {
      handleCentralException(e);
      return;
    }
    
    Collections.sort(projects, new ProjectComparator());
    centralTreeModel.setProjects(projects);
    
    for (Project project : projects) {
      int projectId = project.getId();
      
      try {
        project = centralClient.getProject(projectId);
      } catch (CentralException e) {
        handleCentralException(e);
        continue;
      }
      
      if (project == null) {
        continue;
      }
      
      centralTreeModel.setProject(project);
    }    
  }
  
  /**
   * Populate the children for the given project.
   * 
   * @param project  the project to populate
   */
  private void populateProject(Project project) {
    updateDataFiles(project.getDataFile());
    
    if (project.getExperiment().size() == 0) {
      return;
    }
    
    for (Experiment experiment : project.getExperiment()) {
      int experimentId = experiment.getId();
      
      try {
        experiment = centralClient.getExperiment(project.getId(), experimentId);
      } catch (CentralException e) {
        handleCentralException(e);
        continue;
      }
      
      if (experiment == null) {
        continue;
      }
      
      centralTreeModel.setExperiment(project.getId(), experiment);
    }
  }
  
  /**
   * Populate the children for the given experiment.
   * 
   * @param project     the project for the experiment
   * @param experiment  the experiment to populate
   */
  private void populateExperiment(Project project, Experiment experiment) {
    updateDataFiles(experiment.getDataFile());
    
    if (experiment.getTrial().size() == 0) {
      return;
    }
    
    for (Trial trial : experiment.getTrial()) {
      int trialId = trial.getId();
      
      try {
        trial = centralClient.getTrial(project.getId(), experiment.getId(), trialId);
      } catch (CentralException e) {
        handleCentralException(e);
        continue;
      }
      
      if (trial == null) {
        continue;
      }
      
      centralTreeModel.setTrial(project.getId(), experiment.getId(), trial);
    }
  }
  
  /**
   * Populate the children for the given trial.
   * 
   * @param project     the project for the trial
   * @param experiment  the experiment for the trial
   * @param trial       the trial to populate
   */
  private void populateTrial(Project project, Experiment experiment, Trial trial) {
    updateDataFiles(trial.getDataFile());
    
    if (trial.getRepetition().size() == 0) {
      return;
    }
    
    for (Repetition repetition : trial.getRepetition()) {
      int repetitionId = repetition.getId();
      
      try {
        repetition = centralClient.getRepetition(project.getId(), experiment.getId(), trial.getId(), repetitionId);
      } catch (CentralException e) {
        handleCentralException(e);
        continue;
      }
      
      if (repetition == null) {
        continue;
      }
      
      centralTreeModel.setRepetition(project.getId(), experiment.getId(), trial.getId(), repetition);
    }
  }
  
  /**
   * Populate the children for the given repetition.
   * 
   * @param repetition  the repetition to populate
   */
  private void populateRepetition(Repetition repetition) {
    updateDataFiles(repetition.getDataFile());
  }
  
  /**
   * Populate the children for the given data file.
   * 
   * @param dataFile  the data file to populate
   */
  private void populateDataFiles(DataFile dataFile) {
    updateDataFiles(dataFile.getDataFile());
  }
  
  /**
   * Update the given list of data files.
   * 
   * @param dataFiles  the list of data files to update
   */
  private void updateDataFiles(List<DataFile> dataFiles) {
    for (DataFile dataFile : dataFiles) {
      try {
        String link = dataFile.getLink();
        try {
          link = URLEncoder.encode(link, "UTF-8"); // encode all non-ascii
          link = link.replaceAll("%2F", "/");      // decode back the slash for calling REST
        } catch (UnsupportedEncodingException ue) {
          continue;
        }
        dataFile = centralClient.getDataFile(link);
      } catch (CentralException e) {
        handleCentralException(e);
        continue;
      }
      
      if (dataFile == null) {
        continue;
      }
      
      centralTreeModel.setDataFile(dataFile);
    }    
  }
  
  /**
   * Gets a list of data files selected in the NEEScentral tree.
   * 
   * @return  a list of selected data files
   */
  private List<DataFile> getSelectedDataFilesInTree() {
    List<DataFile> selectedDataFiles = new ArrayList<DataFile>();
    
    TreePath[] selectedTreePaths = centralTree.getSelectionPaths();
    if (selectedTreePaths == null || selectedTreePaths.length == 0) {
      return selectedDataFiles;
    }
    
    for (TreePath selectedTreePath : selectedTreePaths) {
      Object selectedNode = selectedTreePath.getLastPathComponent();
      if (selectedNode instanceof DataFile) {
        DataFile dataFile = (DataFile)selectedNode;
        if (!dataFile.isIsDirectory()) {
          selectedDataFiles.add((DataFile)selectedNode);
        }
      }
    }
    
    return selectedDataFiles;
  }  
  
  /**
   * Displays an error message to the user with the exception error message
   * text.
   * 
   * @param e  the exception to handle
   */
  private void handleCentralException(CentralException e) {
    JOptionPane.showMessageDialog(dialog, e.getMessage(), "NEEScentral Error",
                                  JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Called when a tree node is expanding. This will start a thread to populate
   * the node's children.
   * 
   * @param treePath  the tree path expanding
   */
  private void treeNodeExpanding(final TreePath treePath) {
    new Thread() {
      public void run() {
        populateNodeChildren(treePath);
      }
    }.start();            
  }
  
  /**
   * Adds the list of data files to the data files list. If a data file is
   * already in the list, it will not be added.
   * 
   * @param dataFiles  the list of data files
   */
  private void addToDataFileList(List<DataFile> dataFiles) {
    DefaultListModel listModel = (DefaultListModel)dataFileList.getModel();
    for (DataFile dataFile : dataFiles) {
      if (!listModel.contains(dataFile)) {
        listModel.addElement(dataFile);
      }
    }    
  }
  
  /**
   * Called when a tree node is clicked. If the event is a double click on a
   * data file, it will be added to the data file list.
   * 
   * @param e  the mouse event
   */
  private void treeNodeClicked(MouseEvent e) {
    if (e.getClickCount() != 2) {
      return;
    }
    
    List<DataFile> selectedDataFiles = getSelectedDataFilesInTree();
    if (selectedDataFiles.size() == 0) {
      return;
    }
    
    addToDataFileList(selectedDataFiles);
  }
  
  /**
   * Called when the contents of the data file list changes. This will toggle
   * the enabled property of the import button.
   */
  private void listContentsChanged() {
    importButton.setEnabled(dataFileList.getModel().getSize() > 0);
  }
  
  /**
   * Called when the delete button is pressed in the data file list. This will
   * remove the selected data files from the list.
   */
  private void deleteSelectedDataFiles() {
    Object[] selectedValues = dataFileList.getSelectedValues();
    if (selectedValues == null || selectedValues.length == 0) {
      return;
    }
    
    DefaultListModel model = (DefaultListModel)dataFileList.getModel();
    for (Object selectedValue : selectedValues) {
      model.removeElement(selectedValue);
    }    
  }
  
  /**
   * Starts the import of the choosen data files.
   */
  private void startImport() {
    List<URL> dataFiles = new ArrayList<URL>();
    List<URL> zipVideoFiles = new ArrayList<URL>();
    
    DefaultListModel dataFileListModel = (DefaultListModel)dataFileList.getModel();
    if (dataFileListModel.getSize() == 0) {
      return;
    }
    
    for (int i=0; i<dataFileListModel.getSize(); i++) {
      DataFile dataFile = (DataFile)dataFileListModel.getElementAt(i);
      try {
        dataFile.setContentLink(URLEncoder.encode(dataFile.getContentLink(), "UTF-8"));
        dataFile.setContentLink(dataFile.getContentLink().replaceAll("%2F", "/"));
      } catch (UnsupportedEncodingException ue) {
        continue;
      }

      URL dataFileURL = centralClient.getDataFileURL(dataFile);
      if (dataFile.getName().toLowerCase().endsWith(".jpg.zip")) {
        zipVideoFiles.add(dataFileURL);
      } else {
        dataFiles.add(dataFileURL);        
      }

    }
    
    dialog.dispose();

    dataFileListModel.clear();
    if (dataFiles.size() > 0) {
      ActionFactory.getInstance().getDataImportAction().importData(dataFiles);      
    }
    
    if (zipVideoFiles.size() > 0) {
      ActionFactory.getInstance().getJPEGImportAction().importZipVideo(zipVideoFiles);
    }

  }
  
  /**
   * A transfer handler for the data file in the tree.
   */
  private class TreeDataFileTransferHandler extends TransferHandler {
    /**
     * Gets the action, which is always LINK.
     */
    public int getSourceActions(JComponent c) {
      return DnDConstants.ACTION_LINK;
    }
    
    /**
     * Gets the transferable which contains a list of selected data files.
     * 
     * @param c  the component to create the transferable from
     * @return   the transferable
     */
    protected Transferable createTransferable(JComponent c) {
      List<DataFile> selectedDataFiles = getSelectedDataFilesInTree();
      if (selectedDataFiles.size() == 0) {
        return null;
      }
      
      return new DataFileListTransferable(selectedDataFiles);
    }
    
    /**
     * Sess if the transfer handler can import data. Always returns false.
     * 
     * @return  false, always
     */
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
      return false;
    }
  }

  /**
   * A listener for drops on the data file list.
   */
  private class DataFileListDropTargetListener extends DropTargetAdapter {
    /**
     * Called when something is dropped on the file list. This accepts the link
     * action and expects a list of data file objects. The data files objects
     * will be added to the data file list. Duplicates will be skipped.
     * 
     * @param dtde  the event for the drop target
     */
    public void drop(DropTargetDropEvent dtde) {
      if (dtde.getDropAction() != DnDConstants.ACTION_LINK) {
        dtde.rejectDrop();
        return;
      }
      
      Transferable t = dtde.getTransferable();
      if (t == null) {
        dtde.rejectDrop();
        return;
      }
      
      DataFlavor dataFlavor = null;
      try {
        dataFlavor = new DataFileListDataFlavor();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        dtde.rejectDrop();
        return;
      }
      
      if (!t.isDataFlavorSupported(dataFlavor)) {
        dtde.rejectDrop();
        return;
      }
      
      List<DataFile> dataFiles;
      try {
        dataFiles = (List<DataFile>)t.getTransferData(dataFlavor);
      } catch (UnsupportedFlavorException e) {
        e.printStackTrace();
        dtde.rejectDrop();
        return;
      } catch (IOException e) {
        e.printStackTrace();
        dtde.rejectDrop();
        return;        
      }
      
      addToDataFileList(dataFiles);
      
      dtde.acceptDrop(dtde.getDropAction());
      dtde.dropComplete(true);
    }
  }
  
  /**
   * A transferable for a list of data files.
   */
  private class DataFileListTransferable implements Transferable {
    /** the data flavor */
    private DataFlavor dataFlavor;
    
    /** the list of data files */
    private final List<DataFile> dataFiles;
    
    /**
     * Creates the transferable with the list of data files.
     * 
     * @param dataFiles  the data files to transfer
     */
    public DataFileListTransferable(List<DataFile> dataFiles) {
      this.dataFiles = dataFiles;
      
      try {
        dataFlavor = new DataFileListDataFlavor();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    
    /**
     * Gets the list of data files.
     * 
     * @param df  the data flavor to return
     * @return    the list of data files
     */
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
      if (!dataFlavor.match(df)) {
        throw new UnsupportedFlavorException(df);
      }
      
      return dataFiles;
    }

    /**
     * Gets the array of data files supported.
     * 
     * @return  the data flavors supported
     */
    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] dataFlavors = { dataFlavor };
      return dataFlavors;
    }

    /**
     * Sees if the data flavor is supported.
     * 
     * @param df  the data flavor to check
     * @return    true if it is supported, false otherwise
     */
    public boolean isDataFlavorSupported(DataFlavor df) {
      return dataFlavor.match(df);
    }
  }
  
  /**
   * The data flavor for a data file list.
   */
  private class DataFileListDataFlavor extends DataFlavor {
    public DataFileListDataFlavor() throws ClassNotFoundException {
      super(List.class, "List of data files");
    }
  }
  
  /**
   * A class to compare to projects. This uses the project ID to see if they
   * are equal.
   */
  private class ProjectComparator implements Comparator<Project> {
    public int compare(Project p1, Project p2) {
      return p1.getId().compareTo(p2.getId());
    }
  }
}