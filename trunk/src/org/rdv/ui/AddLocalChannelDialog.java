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

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.matheclipse.parser.client.SyntaxError;
import org.rdv.RDV;
import org.rdv.data.LocalChannel;
import org.rdv.data.LocalChannelManager;
import org.rdv.rbnb.MetadataManager;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBUtilities;

/**
 * A dialog to collect the information necessary to create a local channel.
 * 
 * @author Jason P. Hanley
 */
public class AddLocalChannelDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = -7945306695595024914L;
  
  /** the name text field */
  private JTextField nameTextField;
  
  /** the unit text field */
  private JTextField unitTextField;
  
  /** the model for the variables table */
  private DefaultTableModel variablesTableModel;
  
  /** the table for the variables */
  private JTable variablesTable;
  
  /** the remove variable button */
  private JButton removeVariableButton;  

  /** the formula text area */
  private JTextArea formulaTextArea;
  
  /** the add channel button */
  private JButton addChannelButton;
  
  /**
   * Creates a add local channel dialog.
   * 
   * @param owner  the dialog's owner frame
   */
  public AddLocalChannelDialog(JFrame owner) {
    super(owner);
    
    setName("addLocalChannelDialog");
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    initComponents();
  }
  
  /**
   * Initialize the UI components.
   */
  private void initComponents() {
    RDV rdv = RDV.getInstance(RDV.class);
    ApplicationContext context = rdv.getContext();
    
    ResourceMap resourceMap = context.getResourceMap(getClass());
    
    JPanel container = new JPanel();
    setContentPane(container);
    
    container.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0;
    c.weighty = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    
    JLabel headerLabel = new JLabel();
    headerLabel.setName("headerLabel");
    headerLabel.setBackground(Color.white);
    headerLabel.setOpaque(true);
    headerLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.gray),
        BorderFactory.createEmptyBorder(10,10,10,10)));    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.insets = new java.awt.Insets(0,0,0,0);    
    container.add(headerLabel, c);

    JLabel nameLabel = new JLabel();
    nameLabel.setName("nameLabel");
    c.fill = GridBagConstraints.NONE;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(10,10,10,5);
    container.add(nameLabel, c);
    
    // a listener for changes to text fields
    DocumentListener documentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { checkFields(); }
      public void insertUpdate(DocumentEvent e) { checkFields(); }
      public void removeUpdate(DocumentEvent e) { checkFields(); }
    };
    
    nameTextField = new JTextField();
    nameTextField.getDocument().addDocumentListener(documentListener);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(10,0,10,10);
    container.add(nameTextField, c);
    
    JLabel unitLabel = new JLabel();
    unitLabel.setName("unitLabel");
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,5);
    container.add(unitLabel, c);
    
    unitTextField = new JTextField();
    unitTextField.getDocument().addDocumentListener(documentListener);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,0,10,10);
    container.add(unitTextField, c);
    
    JLabel variablesLabel = new JLabel();
    variablesLabel.setName("variablesLabel");
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth =1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(variablesLabel, c);
    
    // creates a table model with 2 columns for variables channels and names
    String[] variableTableColumnNames = new String[] {
        resourceMap.getString("variablesTableChannelColumnText"),
        resourceMap.getString("variablesTableNameColumnText")
    };
    variablesTableModel = new DefaultTableModel(new Object[][] {}, variableTableColumnNames);
    variablesTableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        variablesTableChanged(e);
      }      
    });
    
    variablesTable = new JTable(variablesTableModel);
    variablesTable.setPreferredScrollableViewportSize(new Dimension(200,100));
    variablesTable.setFillsViewportHeight(true);
    variablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        variablesTableSectionChanged();
      }
    });
    
    
    // get a list of server channels that are numeric data channels
    boolean showHidden = rdv.getMainPanel().isHiddenChannelsVisible();
    List<String> serverChannels = RBNBUtilities.getServerChannels(
        "application/octet-stream", showHidden);
    
    // set the channels column editor to a combo box filled with the server channels
    JComboBox channelComboBox = new JComboBox(serverChannels.toArray());
    TableColumn channelColumn = variablesTable.getColumnModel().getColumn(0);
    channelColumn.setCellEditor(new DefaultCellEditor(channelComboBox));
    variablesTable.setRowHeight(channelComboBox.getPreferredSize().height);

    JPanel variablesPanel = new JPanel();
    variablesPanel.setLayout(new BorderLayout());
    
    JScrollPane variablesScrollPane = new JScrollPane(variablesTable);
    variablesPanel.add(variablesScrollPane, BorderLayout.CENTER);

    Box variablesButtonsPanel = Box.createVerticalBox();
    variablesButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
    
    JButton addVariableButton = new JButton();
    addVariableButton.setName("addVariableButton");
    variablesButtonsPanel.add(addVariableButton);
    
    variablesButtonsPanel.add(Box.createVerticalStrut(5));

    removeVariableButton = new JButton();
    removeVariableButton.setName("removeVariableButton");
    variablesButtonsPanel.add(removeVariableButton);
    
    variablesPanel.add(variablesButtonsPanel, BorderLayout.EAST);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(variablesPanel, c);
    
    JLabel formulaLabel = new JLabel();
    formulaLabel.setName("formulaLabel");
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(formulaLabel, c);
    
    formulaTextArea = new JTextArea(5, 50);
    formulaTextArea.getDocument().addDocumentListener(documentListener);
    JScrollPane formulaScrollPane = new JScrollPane(formulaTextArea);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(formulaScrollPane, c);
    
    JPanel footerPanel = new JPanel();
    
    addChannelButton = new JButton();
    addChannelButton.setName("addChannelButton");
    footerPanel.add(addChannelButton);
    
    JButton cancelButton = new JButton();
    cancelButton.setName("cancelButton");
    footerPanel.add(cancelButton);
    
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.5;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 7;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(footerPanel, c);
    
    // set the actions for the components
    ActionMap actionMap = context.getActionMap(this);
    container.setActionMap(actionMap);
    addVariableButton.setAction(actionMap.get("addVariable"));
    removeVariableButton.setAction(actionMap.get("removeVariable"));
    addChannelButton.setAction(actionMap.get("addChannel"));
    cancelButton.setAction(actionMap.get("cancel"));
    
    // bind keystrokes
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "addChannel");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    
    // inject resources from the properties for this component
    resourceMap.injectComponents(this);
    
    // initially disable buttons
    removeVariableButton.setEnabled(false);
    addChannelButton.setEnabled(false);

    pack();

    setLocationByPlatform(true);
    setVisible(true);
  }
  
  /**
   * Checks the UI fields to see if they are valid. If they are, the add channel
   * button is enabled. This is called when the contents of any of the fields
   * changes.
   */
  private void checkFields() {
    String name = nameTextField.getText();
    
    // check for invalid name
    boolean nameMatches = name.matches("^[[a-zA-Z0-9\\.\\(\\)\\- ]+/]*[a-zA-Z0-9\\.\\(\\)\\- ]+$");
    
    // check for existing channel
    MetadataManager metadataManager = RBNBController.getInstance().getMetadataManager();
    boolean nameFree = metadataManager.getMetadataChannelTree().findNode(name) == null;
    
    boolean nameValid = nameMatches && nameFree;
    
    String unit = unitTextField.getText();
    
    // check for invalid unit
    boolean unitValid = !unit.contains("=");
    
    // check each variable
    boolean variablesValid = true;
    List<String> variables = new ArrayList<String>();
    for (int row=0; row<variablesTableModel.getRowCount(); row++) {
      String variableChannel = (String) variablesTableModel.getValueAt(row, 0);
      String variableName = (String) variablesTableModel.getValueAt(row, 1);
      
      // skip empty columns
      if ((variableChannel == null || variableChannel.isEmpty()) &&
          (variableName == null || variableName.isEmpty())) {
        continue;
      }
      
     // check for invalid variable name
      if (!variableName.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
        variablesValid = false;
        break;
      }
      
     // check for duplicate variable name
      if (variables.contains(variableName)) {
        variablesValid = false;
        break;
      }
      variables.add(variableName);
    }
    
    // see if we kept any variables
    if (variables.isEmpty()) {
      variablesValid = false;
    }
    
    String formula = formulaTextArea.getText();
    
    // check if a formula has been entered
    boolean formulaExists = !formula.isEmpty();

    // check if all the variables are in the formula
    boolean formulaHasAllVariables = true;
    for (String variable : variables) {
      if (!formula.contains(variable)) {
        formulaHasAllVariables = false;
        break;
      }
    }
    boolean formulaValid = formulaExists && formulaHasAllVariables;
    
    boolean enable = nameValid && unitValid && variablesValid && formulaValid;
    addChannelButton.setEnabled(enable);
  }
  
  /**
   * Called when the variables table has a change. This will try to fill in the
   * name if a new channel is added.
   * 
   * @param e  the table model event
   */
  private void variablesTableChanged(TableModelEvent e) {
    // channel column
    if (e.getColumn() == 0) {
      for (int row=e.getFirstRow(); row<=e.getLastRow(); row++) {
        String channel = (String) variablesTableModel.getValueAt(row, 0);
        String name = (String) variablesTableModel.getValueAt(row, 1);
        
        if (channel != null && (name == null || name.isEmpty())) {
          // create variable name from the channel name
          int index = channel.lastIndexOf('/');
          if (index != -1 && channel.length() > index+1) {
            name = channel.substring(index+1);
            
            // remove invalid characters
            name = name.replaceAll("[^a-zA-Z0-9]", "");
            
            // make sure it starts with a letter
            if (name.matches("^\\d")) {
              name = "x" + name;
            }
            
            // set the name if any characters are left
            if (!name.isEmpty()) {
              variablesTableModel.setValueAt(name, row, 1);
            }
          }
        }
      }
    }
    
    // check the fields for invalid input
    checkFields();
  }
  
  /**
   * Called when the selection changes in the variables table. This enables or
   * disables the remove variable button.
   */
  private void variablesTableSectionChanged() {
    boolean hasSelectedRows = variablesTable.getSelectedRows().length > 0;
    removeVariableButton.setEnabled(hasSelectedRows);
  }
  
  /**
   * Adds a row the variables table and pops up the channel selector combo box.
   */
  @Action
  public void addVariable() {
    int row = variablesTableModel.getRowCount();
    variablesTableModel.insertRow(row, (Object[])null);
    
    if (variablesTable.editCellAt(row, 0)) {
      variablesTable.scrollRectToVisible(variablesTable.getCellRect(row, 0, true));
      
      DefaultCellEditor chanelCellEditor = (DefaultCellEditor) variablesTable.getCellEditor(row, 0);
      JComboBox channelComboBox = (JComboBox) chanelCellEditor.getComponent();
      channelComboBox.requestFocusInWindow();
      try { channelComboBox.showPopup(); } catch (IllegalComponentStateException e) {}
    }
  }
  
  /**
   * Removes the select rows from the variables table.
   */
  @Action
  public void removeVariable() {
    int[] rows = variablesTable.getSelectedRows();
    if (rows.length == 0) {
      return;
    }
    
    Arrays.sort(rows);
    
    for (int i=rows.length-1; i>=0; i--) {
      variablesTableModel.removeRow(rows[i]);
    }
  }
  
  /**
   * Creates and adds the channel. If there is an error doing this, an error
   * message will be displayed.
   */
  @Action
  public void addChannel() {
    String name = nameTextField.getText();
    String unit = unitTextField.getText();
    
    // get variables from the rows that aren't empty
    Map<String,String> variables = new HashMap<String,String>();
    for (int row=0; row<variablesTableModel.getRowCount(); row++) {
      String variableChannel = (String) variablesTableModel.getValueAt(row, 0);
      String variableName = (String) variablesTableModel.getValueAt(row, 1);
      if (variableChannel != null && variableChannel.length() > 0 &&
          variableName != null && variableName.length() > 0) {
        variables.put(variableName, variableChannel);
      }
    }
    
    String formula = formulaTextArea.getText();
    
    // try to create and add the channel
    try {
      LocalChannel channel = new LocalChannel(name, unit, variables, formula);
      LocalChannelManager.getInstance().addChannel(channel);
    } catch (Exception e) {
      handleLocalChannelException(e);
      return;
    }
    
    dispose();
  }
  
  /**
   * Handles an exception thrown when creating a local channel. An error dialog
   * will display with the nature of the error.
   * 
   * @param e  the exception
   */
  private void handleLocalChannelException(Exception e) {
    Application application = RDV.getInstance();
    ApplicationContext context = application.getContext();
    ResourceMap resourceMap = context.getResourceMap(getClass());
    
    String errorTitle = resourceMap.getString("formulaErrorTitle");
    
    String errorMessage = "<html><body><font size=+1><b>" +
                          resourceMap.getString("formulaErrorMessage") +
                          "</b></font><br><br>";
    
    if (e instanceof ArithmeticException) {
      ArithmeticException ae = (ArithmeticException)e;
      
      String message = ae.getMessage();
      if (message.contains(": ")) {
        message = message.substring(message.indexOf(": ")+2);
      }
      
      errorMessage += resourceMap.getString("arithmeticErrorMessage", message);
    } else if (e instanceof SyntaxError) {
      SyntaxError se = (SyntaxError)e;
      
      int row = se.getRowIndex()+1;
      int column = se.getColumnIndex();
      String line = se.getCurrentLine();
      if (column > line.length()) column = line.length();
      
      errorMessage += resourceMap.getString("syntaxErrorMessage", row, column) + ":<br><pre>" +
                      line + "<br>";
      
      for (int i=0; i<column-1; i++) {
        errorMessage += "&nbsp;";
      }
      errorMessage += "^</pre>";
    } else {
      errorMessage += e.getMessage();
    }
    
    errorMessage += "</body></html>";

    JOptionPane.showMessageDialog(this,
        errorMessage,
        errorTitle,
        JOptionPane.ERROR_MESSAGE);    
  }
  
  /**
   * Disposes the dialog.
   */
  @Action
  public void cancel() {
    dispose();
  }

}