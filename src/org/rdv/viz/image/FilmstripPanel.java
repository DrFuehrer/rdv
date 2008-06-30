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

package org.rdv.viz.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.rdv.DataViewer;
import org.rdv.rbnb.RBNBController;

/**
 * A class to display images like a filmstrip.
 * 
 * @author Jason P. Hanley
 *
 */
public class FilmstripPanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = 6924475674504241229L;
  
  /** the current time */
  private double time;
  
  /** the time scale */
  private double timescale;
  
  /** the mouse listener for clicks on an image */
  private final MouseListener mouseListener;
  
  /**
   * Creates a filmstrip panel with a time scale of 1.
   */
  public FilmstripPanel() {
    super();
    
    timescale = 1;
    
    setLayout(new FilmstripLayout());
    
    mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
          ImagePanel imagePanel = (ImagePanel) e.getComponent();
          double timestamp = imagePanel.getTimestamp();
          if (timestamp >= 0) {
            RBNBController.getInstance().setLocation(timestamp);
          }
        }
      }
    };
  }
  
  /**
   * Adds an image to the filmstrip.
   * 
   * @param image      the image to add
   * @param timestamp  the timestamp for the image
   */
  public void addImage(BufferedImage image, double timestamp) {
    int componentCount = getComponentCount();
    if (componentCount > 0) {
      ImagePanel imagePanel = (ImagePanel)getComponent(componentCount-1);
      if (imagePanel.getTimestamp() == timestamp) {
        return;
      }
    }
    
    ImagePanel imagePanel = new ImagePanel(false);
    imagePanel.setBackground(Color.black);    
    imagePanel.setImage(image, timestamp);
    imagePanel.setToolTipText(DataViewer.formatDateSmart(timestamp));
    imagePanel.setComponentPopupMenu(getComponentPopupMenu());
    imagePanel.addMouseListener(mouseListener);
    
    for (MouseListener listener : getMouseListeners()) {
      imagePanel.addMouseListener(listener);
    }
    
    add(imagePanel);
    
    ageImages();
  }
  
  /**
   * Removes all the images from the filmstrip.
   */
  public void clearImages() {
    removeAll();
  }
  
  /**
   * Sets the time for the filmstrip.
   * 
   * @param time  the new time
   */
  public void setTime(double time) {
    this.time = time;
    
    ageImages();
  }
  
  /**
   * Sets the time scale. Images older than the time scale will be removed.
   * 
   * @param timescale  the new time scale
   */
  public void setTimescale(double timescale) {
    this.timescale = timescale;
    
    ageImages();
  }
  
  /**
   * Ages images older than the time scale, relative to the current time.
   */
  private void ageImages() {
    int componentCount = getComponentCount();
    if (componentCount == 0) {
      return;
    }
    
    for (int i=componentCount-1; i>=0 ; i--) {
      ImagePanel imagePanel = (ImagePanel)getComponent(i);
      if (imagePanel.getTimestamp() <= time-timescale || imagePanel.getTimestamp() > time) {
        remove(i);
        revalidate();
      }
    }
  }
  
  /**
   * A simple layout manager that's layouts the components horizontally. The
   * components width will be the same based on the widht of the container.
   * Their height will be the height on the container.
   * 
   * @author Jason P. Hanley
   */
  class FilmstripLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component component) {}
    
    public void removeLayoutComponent(Component component) {}

    /**
     * Lays out the container like a filmstrip. Each component will be of equal
     * size and layed out next to each other left-to-right.
     */
    public void layoutContainer(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return;
      }
      
      Dimension dimensions = container.getSize();
      Insets insets = container.getInsets();
      dimensions.width -= insets.left + insets.right;
      dimensions.height -= insets.top + insets.bottom;
      
      int width = dimensions.width / componentCount;
      
      for (int i = 0; i < componentCount; i++) {
        Component component = container.getComponent(i);
        component.setBounds(i*width, 0, width, dimensions.height);
      }
    }

    /**
     * Gets the minimum layout size which is 0,0.
     */
    public Dimension minimumLayoutSize(Container container) {
      return new Dimension(0, 0);
    }

    /**
     * Gets the preferred layout size based on the preferred size of the first
     * component.
     */
    public Dimension preferredLayoutSize(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return new Dimension(0, 0);
      }
      
      Dimension dimension = container.getComponent(0).getPreferredSize();
      return new Dimension(componentCount*dimension.width, dimension.height);
    }

  }

}