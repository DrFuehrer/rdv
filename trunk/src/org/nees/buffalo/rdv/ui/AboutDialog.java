/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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
import java.awt.event.ActionEvent;

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
import javax.swing.KeyStroke;

import org.nees.buffalo.rdv.Version;

/**
 * @author Jason P. Hanley
 */
public class AboutDialog extends JDialog {
		
	public AboutDialog(JFrame owner) {
		super(owner);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTitle("About RDV");
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
            		
		JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		
    aboutPanel.add(new JLabel("RDV - Realtime Data Viewer"));
    aboutPanel.add(new JLabel("by Jason P. Hanley <jphanley@buffalo.edu>"));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("Version " + Version.major + "." + Version.minor + "." + Version.release));
    aboutPanel.add(new JLabel("Build " + Version.build));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("Copyright \251 2005-2006 University at Buffalo"));
    aboutPanel.add(new JLabel("Visit http://nees.buffalo.edu/"));
    aboutPanel.add(new JLabel(" "));
    aboutPanel.add(new JLabel("This work is supported in part by the"));
    aboutPanel.add(new JLabel("George E. Brown, Jr. Network for Earthquake"));
    aboutPanel.add(new JLabel("Engineering Simulation (NEES) Program of the"));
    aboutPanel.add(new JLabel("National Science Foundation under Award"));
    aboutPanel.add(new JLabel("Numbers CMS-0086611 and CMS-0086612."));
    
    container.add(aboutPanel, BorderLayout.CENTER);

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
    buttonPanel.setLayout(new BorderLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
    buttonPanel.add(new JButton(disposeAction), BorderLayout.CENTER);
    container.add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
    setLocationByPlatform(true);
		setVisible(true);
	}	
}
