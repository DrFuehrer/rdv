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
 * $URL: svn+ssh://jphanley@code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/ui/DataPanelContainer.java $
 * $Revision: 375 $
 * $Date: 2006-01-02 20:04:37 -0500 (Mon, 02 Jan 2006) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A container to hold the UI components for the data panels. They may add and
 * remove UI components as needed.
 * 
 * @author  Jason P. Hanley
 * @since   1.1
 */
public class DataPanelContainer extends JPanel implements DragGestureListener, DragSourceListener {

	/**
	 * The logger for this class.
	 * 
	 * @since  1.1
	 */
	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	/**
	 * A list of docked UI components.
	 * 
	 * @since  1.1
	 */
	ArrayList dataPanels;
	
	/**
	 * Display the data panels horizontally.
	 * 
	 * @since  1.1
	 */
	public static int HORIZONTAL_LAYOUT = 0;
	
	/**
	 * Display the data panels vertically.
	 * 
	 * @since  1.1
	 */
	public static int VERTICAL_LAYOUT = 1;
	
	/**
	 * The current layout.
	 * 
	 * @since  1.1
	 */
	private int layout;
  
  /**
   * The layout manager.
   * 
   * @since  1.3
   */
  private GridLayout gridLayout;
  
  /**
   * The drag gesture recognizers for the components.
   * 
   * @since  1.3
   */
  private HashMap dragGestures;
	
	/** 
	 * Create the container and set the default layout to horizontal.
	 * 
	 * @since  1.1
	 */
	public DataPanelContainer() {
    super();
    
    setBorder(null);
    
    gridLayout = new GridLayout(1, 1, 8, 8);
    setLayout(gridLayout);
    
		dataPanels = new ArrayList();
    dragGestures =  new HashMap();
		
		layout = HORIZONTAL_LAYOUT;
	}
	
	/**
	 * Add a data panel UI component to this container.
	 * 
	 * @param component  the UI component to add
	 * @since            1.1
	 */
	public void addDataPanel(JComponent component) {
		dataPanels.add(component);
    
    DragSource dragSource = DragSource.getDefaultDragSource();
    DragGestureRecognizer dragGesture = dragSource.createDefaultDragGestureRecognizer(component, DnDConstants.ACTION_MOVE, this);
    dragGestures.put(component, dragGesture);
    
		layoutDataPanels();
		
		log.info("Added data panel to container (total=" + dataPanels.size() + ").");
	}

	/**
	 * Remove the data panel UI component from this container.
	 * 
	 * @param component  the UI component to remove.
	 * @since            1.1
	 */
	public void removeDataPanel(JComponent component) {
    DragGestureRecognizer dragGesture = (DragGestureRecognizer)dragGestures.remove(component);
    dragGesture.setComponent(null);
    
		dataPanels.remove(component);
		layoutDataPanels();
		
		log.info("Removed data panel container (total=" + dataPanels.size() + ").");
	}
	
	/**
	 * Set the layout for the data panels.
	 * 
	 * @param layout  the layout to use
	 * @since         1.1
	 */
	public void setLayout(int layout) {
    if (this.layout != layout) {
      this.layout = layout;
      layoutDataPanels();
    }
	}
	
	/**
	 * Layout the data panel acording the layout setting and in the order in which
	 * they were added to the container.
	 * 
	 * @since  1.1
	 */
	private void layoutDataPanels() {
		int numberOfDataPanels = dataPanels.size();
		if (numberOfDataPanels > 0) {
			int gridDimension = (int)Math.ceil(Math.sqrt(numberOfDataPanels));
			int rows = gridDimension;
      
      int columns;
      if (numberOfDataPanels > Math.pow(gridDimension, 2)*(gridDimension-1)/gridDimension) {
        columns = gridDimension;
      } else {
        columns = gridDimension-1;
      }

			if (layout == HORIZONTAL_LAYOUT) {
        gridLayout.setRows(columns);
        gridLayout.setColumns(rows);
			} else {
        gridLayout.setRows(rows);
        gridLayout.setColumns(columns);
      }
    }
      
    removeAll();
		JComponent component;
		for (int i=0; i<numberOfDataPanels; i++) {
			component = (JComponent)dataPanels.get(i);
			add(component);
		}
		
		validate();
		repaint();
	}
  
  private void moveBefore(Component moveComponent, Component beforeComponent) {
    int beforeComponentIndex = getComponentIndex(beforeComponent);
    if (beforeComponentIndex != -1) {
      dataPanels.remove(moveComponent);
      dataPanels.add(beforeComponentIndex, moveComponent);
      layoutDataPanels();
    }
  }
  
  private int getComponentIndex(Component c) {
    for (int i=0; i<dataPanels.size(); i++) {
      Component component = (Component)dataPanels.get(i);
      if (c == component) {
        return i;
      }
    }    
    return -1;
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    e.startDrag(DragSource.DefaultMoveDrop, new StringSelection(""), this);    
  }

  public void dragEnter(DragSourceDragEvent e) {}

  public void dragOver(DragSourceDragEvent e) {
    Point dragPoint = e.getLocation();
    Point containerLocation = getLocationOnScreen();
    dragPoint.translate(-containerLocation.x, -containerLocation.y);

    Component overComponent = getComponentAt(dragPoint);
    Component dragComponent = e.getDragSourceContext().getComponent();
    
    if (overComponent != null && overComponent != dragComponent) {
      moveBefore(dragComponent, overComponent);
    }
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {}

  public void dragExit(DragSourceEvent dse) {}

  public void dragDropEnd(DragSourceDropEvent dsde) {}
}
