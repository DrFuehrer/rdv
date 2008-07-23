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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.jdesktop.application.ResourceMap;
import org.rdv.RDV;

/**
 * @author Jason P. Hanley
 */
public class AboutDialog extends JDialog {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 2530934125867161659L;

  LicenseDialog licenseDialog;
		
  public AboutDialog(JFrame owner) {
    super(owner);
    
    setName("aboutDialog");
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();

    // get the frame image and convert it to an icon
    Image logoImage = RDV.getInstance(RDV.class).getMainFrame().getIconImage();
    Icon logoIcon = new ImageIcon(logoImage);
    
    JLabel logoLabel = new JLabel(logoIcon);
    logoLabel.setName("logoLabel");
    logoLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
    logoLabel.setVerticalAlignment(SwingConstants.TOP);
    container.add(logoLabel, BorderLayout.WEST);

    JTextArea aboutTextArea = new JTextArea();
    aboutTextArea.setName("aboutTextArea");
    aboutTextArea.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        BorderFactory.createEmptyBorder(5,5,5,5)));
    aboutTextArea.setBackground(Color.white);
    aboutTextArea.setFont(logoLabel.getFont());
    aboutTextArea.setEditable(false);      
    aboutTextArea.setLineWrap(true);
    aboutTextArea.setWrapStyleWord(true);
    aboutTextArea.setPreferredSize(new Dimension(256,256));
    container.add(aboutTextArea, BorderLayout.CENTER);
    
    Action disposeAction = new AbstractAction() {
      /** serialization version identifier */
      private static final long serialVersionUID = -6076510093659918139L;

      public void actionPerformed(ActionEvent arg0) {
        dispose();
      }
    };
    disposeAction.putValue(Action.NAME, "OK");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "dispose");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
    actionMap.put("dispose", disposeAction);
       
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    buttonPanel.setLayout(new BorderLayout());

    JButton licenseButton = new JButton("License");
    licenseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        showLicense();
      }
    });
    buttonPanel.add(licenseButton, BorderLayout.WEST);
    
    JButton okButton = new JButton(disposeAction);
    buttonPanel.add(okButton, BorderLayout.EAST);

    container.add(buttonPanel, BorderLayout.SOUTH);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
		
    pack();

    okButton.requestFocusInWindow();

    setLocationByPlatform(true);
    setVisible(true);
  }
  
  private void showLicense() {
    if (licenseDialog == null) {
      licenseDialog = new LicenseDialog(this);
    } else {
      licenseDialog.setVisible(true);
    }
  }
}
