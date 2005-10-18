/*
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  o The above copyright notice and this permission notice shall be included
 *    in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Wizard extends JDialog {

  static Log log = LogFactory.getLog(Wizard.class.getName());
  
  private Hashtable wizardPanels;
  
  private WizardHeaderPanel header;
  private JPanel content;
  private WizardFooterPanel footer;
  
  private WizardPanel currentWizardPanel;
  
  private List wizardActionListeners;
  
  public Wizard() {
    this(null, null, false);
  }

  public Wizard(Frame owner) {
    this(owner, null, false);
  }
  
  public Wizard(String title) {
    this(null, title, false);
  }

  public Wizard(Frame owner, boolean modal) {
    this(null, null, modal);
  }

  public Wizard(Frame owner, String title) {
    this(owner, title, false);
  }

  public Wizard(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    
    this.addWindowStateListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        cancel();
      }
    });

    wizardPanels = new Hashtable();
    currentWizardPanel = null;
    
    header = new WizardHeaderPanel();
    
    content = new JPanel();
    content.setLayout(new BorderLayout());
    
    footer = new WizardFooterPanel();
    
    setLayout(new BorderLayout());
    add(header, BorderLayout.NORTH);
    add(content, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);
    
    wizardActionListeners = new ArrayList();
  }
  
  public void addWizardPanel(WizardPanel wizardPanel) {
    wizardPanels.put(wizardPanel.getID(), wizardPanel);
  }
  
  private void setPreviousWizardPanel() {
    if (currentWizardPanel != null) {
      String oldWizardPanelID = currentWizardPanel.getID();
      String wizardPanelID = currentWizardPanel.getPreviousPanelID();
      if (wizardPanelID != null) {
        fireBackAction(oldWizardPanelID, wizardPanelID);
        setCurrentWizardPanel(wizardPanelID);
      }
    }
  }
  
  private void setNextWizardPanel() {
    if (currentWizardPanel != null) {
      String oldWizardPanelID = currentWizardPanel.getID();
      String wizardPanelID = currentWizardPanel.getNextPanelID();
      if (wizardPanelID != null) {
        fireNextAction(oldWizardPanelID, wizardPanelID);        
        setCurrentWizardPanel(wizardPanelID);
      }
    }
  }
 
  public void setCurrentWizardPanel(String wizardPanelID) {
    if (wizardPanelID != null) {
      WizardPanel wizardPanel = getWizardPanel(wizardPanelID);
      if (wizardPanel != null) {
        setWizardPanel(wizardPanel);
      } else {
        log.warn("Failed to find WizardPanel for ID " + wizardPanelID + ".");
      }
    }
  }
    
  private void setWizardPanel(WizardPanel wizardPanel) {
    if (currentWizardPanel != null) {
      currentWizardPanel.hiding();
      content.removeAll();
    }
    
    currentWizardPanel = wizardPanel;
    
    wizardPanel.showing();
    
    footer.setBackEnabled(wizardPanel.getPreviousPanelID() != null);
    footer.setNextEnabled(wizardPanel.getNextPanelID() != null);
    footer.setFinishEnabled(wizardPanel.getNextPanelID() == null);
    
    header.setTitle(wizardPanel.getTitle());
    header.setDescription(wizardPanel.getDescription());
    if (wizardPanel.getIcon() != null) {
      header.setIcon(wizardPanel.getIcon());
    }
    
    SpringLayout layout = new SpringLayout();
    content.setLayout(layout);
    
    JComponent component = wizardPanel.getComponent();
    content.add(component);
    
    layout.putConstraint(SpringLayout.WEST, component,
                         0,
                         SpringLayout.WEST, content);
    layout.putConstraint(SpringLayout.NORTH, component,
                         0,
                         SpringLayout.NORTH, content);
    layout.putConstraint(SpringLayout.EAST, content,
                         0,
                         SpringLayout.EAST, component);
    
    content.revalidate();
    content.repaint();
  }
  
  private WizardPanel getWizardPanel(String wizardPanelID) {
    return (WizardPanel)wizardPanels.get(wizardPanelID);
  }
  
  public void showWizard() {
    pack();
    setSize(getWidth()+150, getHeight()+300);
    setVisible(true);
  }
  
  public void back() {
    setPreviousWizardPanel(); 
  }
  
  public void next() {
    setNextWizardPanel(); 
  }
  
  public void finish() {
    dispose();
    
    String id = null;
    if (currentWizardPanel != null) {
      id = currentWizardPanel.getID();
    }
    fireFinishAction(id);
  }
  
  public void cancel() {
    dispose();
    
    String id = null;
    if (currentWizardPanel != null) {
      id = currentWizardPanel.getID();
    }
    fireCancelAction(id);    
  }
  
  public void addWizardActionListener(WizardActionListener e) {
    wizardActionListeners.add(e);
  }
  
  public void removeWizardActionListener(WizardActionListener e) {
    wizardActionListeners.remove(e);
  }  
  
  private void fireBackAction(String oldID, String newID) {
    Iterator i = wizardActionListeners.iterator();
    while (i.hasNext()) {
      WizardActionListener wal = (WizardActionListener)i.next();
      wal.wizardBack(oldID, newID);
    }
  }
  
  private void fireNextAction(String oldID, String newID) {
    Iterator i = wizardActionListeners.iterator();
    while (i.hasNext()) {
      WizardActionListener wal = (WizardActionListener)i.next();
      wal.wizardNext(oldID, newID);
    }    
  }
  
  private void fireFinishAction(String id) {
    Iterator i = wizardActionListeners.iterator();
    while (i.hasNext()) {
      WizardActionListener wal = (WizardActionListener)i.next();
      wal.wizardFinish(id);
    }    
  }
  
  private void fireCancelAction(String id) {
    Iterator i = wizardActionListeners.iterator();
    while (i.hasNext()) {
      WizardActionListener wal = (WizardActionListener)i.next();
      wal.wizardCancel(id);
    }    
  }
  
  class WizardHeaderPanel extends JPanel {
    JLabel titleLabel;
    JLabel descriptionLabel;
    JLabel iconLabel;
    
    public WizardHeaderPanel() {
      super();
      
      initHeader();
    }
    
    private void initHeader() {
      setBorder(new CompoundBorder(new BottomLineBorder(), new EmptyBorder(10, 10, 10, 10)));
      setBackground(Color.WHITE);
      setLayout(new BorderLayout());
      
      titleLabel = new JLabel();
      descriptionLabel = new JLabel();
      descriptionLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
      
      iconLabel = new JLabel();
      
      JPanel textPanel = new JPanel();
      textPanel.setOpaque(false);
      textPanel.setLayout(new BorderLayout());
      textPanel.add(titleLabel, BorderLayout.NORTH);
      textPanel.add(descriptionLabel, BorderLayout.CENTER);
      
      add(textPanel, BorderLayout.CENTER);
      add(iconLabel, BorderLayout.EAST);
    }
    
    public void setTitle(String title) {
      titleLabel.setText("<html><strong style=\"font-size: 13px\">" + title + "</strong></html>");
    }
    
    public void setDescription(String description) {
      descriptionLabel.setText(description);
    }
    
    public void setIcon(Icon icon) {
      iconLabel.setIcon(icon);
    }
  }
  
  class WizardFooterPanel extends JPanel {
    JButton backButton;
    JButton nextButton;
    JButton finishButton;
    JButton cancelButton;
    
    boolean isMultiPage;
    
    public WizardFooterPanel() {
      this(true);
    }
    
    public WizardFooterPanel(boolean isMultiPage) {
      super();
      
      this.isMultiPage = isMultiPage;
      
      initFooter();
    }
    
    private void initFooter() {
      backButton = new JButton("< Back");
      nextButton = new JButton("Next >");
      finishButton = new JButton("Finish");
      cancelButton = new JButton("Cancel");
   
      backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          back();
        }
      });
      
      nextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          next();
        }
      });
      
      finishButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          finish();
        }
      });
      
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancel();
        }
      });

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      
      add(Box.createGlue());
      
      if (isMultiPage) {
        add(backButton);
        add(nextButton);
        add(Box.createHorizontalStrut(6));
      }
      
      add(finishButton);
      add(Box.createHorizontalStrut(6));
      add(cancelButton);
      add(Box.createHorizontalStrut(6));
    }

    public void setBackEnabled(boolean b) {
      backButton.setEnabled(b);
    }
    
    public void setNextEnabled(boolean b) {
      nextButton.setEnabled(b);
    }
    
    public void setFinishEnabled(boolean b) {
      finishButton.setEnabled(b);
    }
    
    public void setCancelEnabled(boolean b) {
      cancelButton.setEnabled(b);
    }
  }
  
  private static class BottomLineBorder extends AbstractBorder {

      private static final Insets INSETS = new Insets(0, 0, 1, 0);

      public Insets getBorderInsets(Component c) { return INSETS; }

      public void paintBorder(Component c, Graphics g,
          int x, int y, int w, int h) {
              
          g.setColor(UIManager.getColor("controlShadow"));
          g.fillRect(x, y+h-1, w, 1);
      }
  }      
}
