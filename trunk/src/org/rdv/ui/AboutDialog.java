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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.rdv.DataViewer;
import org.rdv.Version;

/**
 * @author Jason P. Hanley
 */
public class AboutDialog extends JDialog {
  
  /** serialization version identifier */
  private static final long serialVersionUID = 2530934125867161659L;

  LicenseDialog licenseDialog;
		
	public AboutDialog(JFrame owner) {
		super(owner);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTitle("About RDV");
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
            		
		JPanel aboutPanel = new JPanel();
    aboutPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        BorderFactory.createEmptyBorder(5,5,5,5)));
    aboutPanel.setBackground(Color.white);
    aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		
    aboutPanel.add(new JLabel("RDV - Realtime Data Viewer"));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("by Wei Deng, Jason P. Hanley, Paul Hubbard,"));
    aboutPanel.add(new JLabel("Mari Masuda, John Michno, Lawrence J. Miller,"));
    aboutPanel.add(new JLabel("Farooque Sayed, Moji Soltani, Lelli Van Den Einde,"));
    aboutPanel.add(new JLabel("and Shannon Whitmore"));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("Version " + Version.major + "." + Version.minor + "." + Version.release));
    aboutPanel.add(new JLabel("Build " + Version.build));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("Copyright \251 2005-2007 University at Buffalo"));
    aboutPanel.add(new JLabel("Copyright \251 2005-2007 NEES Cyberinfrastructure Center"));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("This work is supported by the George E. Brown, Jr."));
    aboutPanel.add(new JLabel("Network for Earthquake Engineering Simulation (NEES)"));
    aboutPanel.add(new JLabel("Program of the National Science Foundation under Award"));
    aboutPanel.add(new JLabel("Numbers CMS-0086611, CMS-0086612, and CMS-0402490."));
    
    container.add(aboutPanel, BorderLayout.CENTER);
    
    JLabel logoLabel = new JLabel(DataViewer.getIcon("icons/RDV.gif"));
    logoLabel.setVerticalAlignment(SwingConstants.TOP);
    logoLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
    container.add(logoLabel, BorderLayout.WEST);

    Action disposeAction = new AbstractAction() {
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

  class LicenseDialog extends JDialog {

    /** serialization version identifier */
    private static final long serialVersionUID = 7319178973244961918L;

    public LicenseDialog(JDialog owner) {
      super(owner);
      
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      setTitle("RDV License");
      
      JPanel container = new JPanel();
      container.setLayout(new BorderLayout());
      container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      setContentPane(container);
      
      InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      ActionMap actionMap = container.getActionMap();

      Action disposeAction = new AbstractAction() {
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
}
