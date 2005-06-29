/*
 * Created on Jun 24, 2005
 */
package org.nees.buffalo.rdv.ui;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.nees.buffalo.rdv.DataViewer;

/**
 * @author Jason P. Hanley
 */
public class ToolBarButton extends JButton {
  public ToolBarButton(String iconFileName) {
    this(iconFileName, null);
  }
  
  public ToolBarButton(String iconFileName, String toolTip) {      
    super();
    setIcon(DataViewer.getIcon(iconFileName));
    setToolTipText(toolTip);
    setBorder(new EmptyBorder(2, 0, 2, 2));
  }
  
  public void setIcon(String iconFileName) {
    setIcon(DataViewer.getIcon(iconFileName));
  }
}
