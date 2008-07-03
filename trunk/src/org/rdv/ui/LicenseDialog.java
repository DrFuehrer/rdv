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
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.jdesktop.application.ResourceMap;
import org.rdv.DataViewer;
import org.rdv.RDV;

/**
 * @author Jason P. Hanley
 */
class LicenseDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = 7319178973244961918L;

  public LicenseDialog(JDialog owner) {
    super(owner);
    
    setName("licenseDialog");
    
    setDefaultCloseOperation(AboutDialog.DISPOSE_ON_CLOSE);

    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();

    Action disposeAction = new AbstractAction() {
      /** serialization version identifier */
      private static final long serialVersionUID = 5818337446139393403L;

      public void actionPerformed(ActionEvent arg0) {
        dispose();
      }
    };

    disposeAction.putValue(Action.NAME, "OK");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "dispose");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
    actionMap.put("dispose", disposeAction);     
    
    JTextArea textArea = new JTextArea();
    textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    textArea.setBackground(Color.white);
    textArea.setEditable(false);      
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = 
        new JScrollPane(textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(500, 300));
    container.add(scrollPane, BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BorderLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    
    JButton okButton = new JButton(disposeAction);
    buttonPanel.add(okButton, BorderLayout.EAST);      
    
    container.add(buttonPanel, BorderLayout.SOUTH);
    
    loadLicense(textArea);
    textArea.setCaretPosition(0);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);

    pack();

    okButton.requestFocusInWindow();

    setLocationByPlatform(true);
    setVisible(true);      
  }
  
  private void loadLicense(JTextArea textArea) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(DataViewer.getResourceAsStream("LICENSE.txt")));

      String s = null;
      while ((s = reader.readLine()) != null) {
        textArea.append(s);
        textArea.append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }      
  }
}