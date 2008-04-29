/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2007 Palta Software
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A class to display an image. This class supports zooming via the mouse wheel
 * or auto scaling and can show a thumbnail of the image for navigation
 * purposes.
 * 
 * This class is based on <a href="http://today.java.net/pub/a/today/2007/03/27/navigable-image-panel.html">Navigable Image Panel</a>.
 * 
 * @author Jason P. Hanley
 */
public class ImagePanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = 4294261901039774761L;

  /** the scale property */
  public static final String SCALE_PROPERTY = "scale";

  /** the auto scaling property */
  public static final String AUTO_SCALING_PROPERTY = "autoScaling";

  /** the image property */
  public static final String IMAGE_PROPERTY = "image";
  
  /** the navigation image enabled property */
  public static final String NAVIGATION_IMAGE_ENABLED_PROPERTY = "navigationImageEnabled";
  
  /** the high quality rendering enabled property */
  public static final String HIGH_QUALITY_RENDERING_ENABLED_PROPERTY = "highQualityRenderingEnabled";

  /** the scaling factor for the navigation image, relative to the panel width */
  private static final float NAVIGATION_IMAGE_FACTOR = 0.15f;
  
  /** the minimum dimension allowed (width or height) for the navigation image */
  private static final int MINIMUM_NAVIGATION_IMAGE_DIMENSION = 50;
  
  /** the maximum dimension allowed (width or height) for the navigation image */
  private static final int MAXIMUM_NAVIGATION_IMAGE_DIMENSION = 150;

  /** the scale threshold for the high quality rendering mode */
  private static final double HIGH_QUALITY_RENDERING_SCALE_THRESHOLD = 1.0;
  
  /** the maximum scale */
  private static final double MAXIMUM_SCALE = 64;
  
  /** the number of increments to use for smooth scaling */
  private static final int SMOOTH_SCALE_INCREMENTS = 10;

  /** the number of increments per page for scrolling */
  private final static int SCROLL_INCREMENTS_PER_PAGE = 10;
  
  /** the number of increments to use for smooth scrolling */
  private final static int SMOOTH_SCROLL_INCREMENTS = 10;
  
  /** the image */
  private BufferedImage image;

  /** the navigation image */
  private BufferedImage navigationImage;

  /** the scale for the image */
  private double scale = 1;

  /** a flag for auto scaling */
  private boolean autoScaling = true;

  /** the origin for the image in the panel */
  private Point origin = new Point();
  
  /** the current mouse position */
  private Point mousePosition;
  
  /** a flag to tell if a mouse drag was started in the navigation panel */
  private boolean draggingInNavigationImage; 

  /** a flag for the navigation image */
  private boolean navigationImageEnabled = true;
  
  /** a flag for the high quality rendering */
  private boolean highQualityRenderingEnabled = true;
  
  /**
   * Creates an image panel with auto zooming and the navigation image enabled.
   */
  public ImagePanel() {
    setFocusable(true);
    
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        if (image != null) {
          // reset the scale and recompute the origins when the panel is resized
          if (isAutoScaling()) {
            autoScale();
          } else {
            setScale(scale, false);
          }

          // recreated the navigation image because its size is relative to the
          // panel size
          if (isNavigationImageEnabled()) {
            createNavigationImage();
          }
        }

        repaint();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (image == null) {
          return;
        }
        
        // center and zoom in on a double click
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          Point p = e.getPoint();
          
          if (isInNavigationImage(p)) {
            p = navigationToPanelPoint(p);
          } else {
            // save the point in scaled image coordinates, since the panel point
            // will be wrong after we center the image on the panel point
            Point scaledImagePoint = panelToScaledImagePoint(p);
            
            centerImageFromPanelPoint(p);
            
            // convert back to panel coordinates
            p = scaledImageToPanelPoint(scaledImagePoint);
          }
          
          if (e.isShiftDown()) {
            zoomOut(p);
          } else {
            zoomIn(p);
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        
        if (image != null && SwingUtilities.isLeftMouseButton(e)) {
          Point p = e.getPoint();
          draggingInNavigationImage = isInNavigationImage(p);
          if (draggingInNavigationImage) {
            centerImageFromNavigationPoint(p);
          }
        }
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        // pan the image by dragging with the mouse
        if (image != null && SwingUtilities.isLeftMouseButton(e)) {
          Point p = e.getPoint();
          if (draggingInNavigationImage) {
            centerImageFromNavigationPoint(p);
          } else {
            moveImageRelativeToMouse(p);
          }
          mousePosition = p;
        }
      }

      public void mouseMoved(MouseEvent e) {
        // track the mouse position for dragging
        mousePosition = e.getPoint();
      }
    });

    // zoom in and out with the mouse wheel
    addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        requestFocusInWindow();
        
        if (image == null) {
          return;
        }

        Point p = e.getPoint();
        
        if (isInNavigationImage(p)) {
          centerImageFromNavigationPoint(p);
          p = navigationToPanelPoint(p);
        }
        
        boolean zoomIn = (e.getWheelRotation() < 0);
        
        if (zoomIn) {
          zoomIn(p);
        } else {
          zoomOut(p);
        }
      }
    });
    
    initKeyBindings();
  }

  /**
   * Creates an image panel with auto zooming and the navigation image enabled.
   * The specified image is initially displayed.
   * 
   * @param image  the initial image to display
   */
  public ImagePanel(BufferedImage image) {
    this();
    setImage(image);
  }

  /**
   * Initialize the key bindings for scrolling and zooming.
   */
  private void initKeyBindings() {
    Action scrollUpAction = new AbstractAction() {
      private static final long serialVersionUID = -6846248967445268823L;

      public void actionPerformed(ActionEvent ae) {
        scrollUp();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "scrollUp");
    getActionMap().put("scrollUp", scrollUpAction);    

    Action pageUpAction = new AbstractAction() {
      private static final long serialVersionUID = -6846248967445268823L;

      public void actionPerformed(ActionEvent ae) {
        pageUp();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), "pageUp");
    getActionMap().put("pageUp", pageUpAction);  
    
    Action scrollLeftAction = new AbstractAction() {
      private static final long serialVersionUID = -6846248967445268823L;

      public void actionPerformed(ActionEvent ae) {
        scrollLeft();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "scrollLeft");
    getActionMap().put("scrollLeft", scrollLeftAction);    

    Action pageLeftAction = new AbstractAction() {
      private static final long serialVersionUID = 1967647647910668664L;

      public void actionPerformed(ActionEvent ae) {
        pageLeft();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), "pageLeft");
    getActionMap().put("pageLeft", pageLeftAction);    

    Action scrollDownAction = new AbstractAction() {
      private static final long serialVersionUID = -3188971384048244029L;

      public void actionPerformed(ActionEvent ae) {
        scrollDown();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "scrollDown");
    getActionMap().put("scrollDown", scrollDownAction);

    Action pageDownAction = new AbstractAction() {
      private static final long serialVersionUID = -1564989825983447908L;

      public void actionPerformed(ActionEvent ae) {
        pageDown();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), "pageDown");
    getActionMap().put("pageDown", pageDownAction);

    Action scrollRightAction = new AbstractAction() {
      private static final long serialVersionUID = 1967647647910668664L;

      public void actionPerformed(ActionEvent ae) {
        scrollRight();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "scrollRight");
    getActionMap().put("scrollRight", scrollRightAction);    

    Action pageRightAction = new AbstractAction() {
      private static final long serialVersionUID = -8409319976290989171L;

      public void actionPerformed(ActionEvent ae) {
        pageRight();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), "pageRight");
    getActionMap().put("pageRight", pageRightAction);    

    Action zoomInAction = new AbstractAction() {
      private static final long serialVersionUID = -1076232416523241048L;

      public void actionPerformed(ActionEvent arg0) {
        zoomIn();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke('+'), "zoomIn");
    getInputMap().put(KeyStroke.getKeyStroke('='), "zoomIn");
    getActionMap().put("zoomIn", zoomInAction);
    
    Action zoomOutAction = new AbstractAction() {
      private static final long serialVersionUID = -3188971384048244029L;

      public void actionPerformed(ActionEvent arg0) {
        zoomOut();
      }
    };
    getInputMap().put(KeyStroke.getKeyStroke('-'), "zoomOut");
    getActionMap().put("zoomOut", zoomOutAction);    
  }

  /**
   * Gets the image displayed in the panel. If no image is displayed, null is
   * returned.
   * 
   * @return  the image displayed in the panel
   */
  public BufferedImage getImage() {
    return image;
  }

  /**
   * Sets the image displayed in the panel. If null is passed, no image is
   * displayed.
   * 
   * @param image  the image to display
   */
  public void setImage(BufferedImage image) {
    BufferedImage oldImage = this.image;
    this.image = image;

    firePropertyChange(IMAGE_PROPERTY, oldImage, image);

    if (image != null) {
      if (oldImage == null ||
          oldImage.getWidth() != image.getWidth() ||
          oldImage.getHeight() != image.getHeight()) {
        if (isAutoScaling()) {
          autoScale();
        } else {
          setScale(scale, false);
        }
      }

      if (isNavigationImageEnabled()) {
        createNavigationImage();
      }
    } else {
      navigationImage = null;
    }

    repaint();
  }
  
  /**
   * Creates the image used for navigation.
   */
  private void createNavigationImage() {
    // Compute the size of the navigation image. This will be a percentage of
    // the panel size, bounded by an absolute minimum and maximum dimension.
    int navImageWidth, navImageHeight;
    if (image.getWidth() >= image.getHeight()) {
      navImageWidth = Math.round(getWidth() * NAVIGATION_IMAGE_FACTOR);
      navImageWidth = Math.max(navImageWidth, MINIMUM_NAVIGATION_IMAGE_DIMENSION);
      navImageWidth = Math.min(navImageWidth, MAXIMUM_NAVIGATION_IMAGE_DIMENSION);
      navImageHeight = navImageWidth * image.getHeight() / image.getWidth();
    } else {
      navImageHeight = Math.round(getHeight() * NAVIGATION_IMAGE_FACTOR);
      navImageHeight = Math.max(navImageHeight, MINIMUM_NAVIGATION_IMAGE_DIMENSION);
      navImageHeight = Math.min(navImageHeight, MAXIMUM_NAVIGATION_IMAGE_DIMENSION);
      navImageWidth = navImageHeight * image.getWidth() / image.getHeight();
    }
    
    navigationImage = new BufferedImage(navImageWidth, navImageHeight, image
        .getType());
    Graphics g = navigationImage.getGraphics();
    g.drawImage(image, 0, 0, navImageWidth, navImageHeight, null);
  }

  /**
   * Converts this panel's coordinates into the original image coordinates.
   * 
   * @param p  the panel point
   * @return   the image point
   */ 
  private Coords panelToImageCoords(Point p) {
    double x = (p.x - origin.x) / scale;
    double y = (p.y - origin.y) / scale;
    return new Coords(x, y);
  }

  /**
   * Converts the original image coordinates into this panel's coordinates
   * 
   * @param p  the image point
   * @return   the panel point
   */
  private Coords imageToPanelCoords(Coords p) {
    double x = (p.x * scale) + origin.x;
    double y = (p.y * scale) + origin.y;
    return new Coords(x, y);
  }

  /**
   * Converts the navigation image point into the zoomed image point.
   * 
   * @param p  the navigation image point
   * @return   the zoomed image point
   */
  private Point navigationToScaledImagePoint(Point p) {
    int x = p.x * getScaledImageWidth() / getNavigationImageWidth();
    int y = p.y * getScaledImageHeight() / getNavigationImageHeight();
    return new Point(x, y);
  }
  
  /**
   * Converts the navigation image point into a panel point.
   * 
   * @param p  the navigation image point
   * @return   the panel point
   */
  private Point navigationToPanelPoint(Point p) {
    int x = origin.x + (p.x * getScaledImageWidth() / getNavigationImageWidth());
    int y = origin.y + (p.y * getScaledImageHeight() / getNavigationImageHeight());
    return new Point(x, y);
  }
  
  /**
   * Converts the panel point to a point in the scaled image.
   * 
   * @param p  the panel point
   * @return   the scaled image point
   */
  public Point panelToScaledImagePoint(Point p) {
    int x = (int) ((p.x - origin.x) / scale);
    int y = (int) ((p.y - origin.y) / scale);
    return new Point(x, y);
  }
  
  /**
   * Converts the scaled image point to a panel point.
   * 
   * @param p  the scaled image point
   * @return   the panel point
   */
  private Point scaledImageToPanelPoint(Point p) {
    int x = (int) ((p.x * scale) + origin.x);
    int y = (int) ((p.y * scale) + origin.y);
    return new Point(x ,y);
  }

  /**
   * Indicates whether a given point in the panel falls within the navigation
   * image boundaries. This will return false if the navigation image is
   * disabled or not visible.
   * 
   * @param p  the point in the panel
   * @return   true if the point is in the navigation image, false otherwise
   */
  private boolean isInNavigationImage(Point p) {
    return (isNavigationImageEnabled() && !scaledImageFitsInPanel() &&
        p.x < getNavigationImageWidth() && p.y < getNavigationImageHeight());
  }

  /**
   * Indicates whether the scaled image boundaries can fit within the panel
   * boundaries.
   * 
   * @return  true if the scaled image fits in the panel
   */
  private boolean scaledImageFitsInPanel() {
    return (origin.x >= 0 && (origin.x + getScaledImageWidth()) <= getWidth()
        && origin.y >= 0 && (origin.y + getScaledImageHeight()) <= getHeight());
  }

  /**
   * Indicates whether the navigation image is enabled.
   * 
   * @return  true when navigation image is enabled, false otherwise
   */
  public boolean isNavigationImageEnabled() {
    return navigationImageEnabled;
  }

  /**
   * Enables or disables navigation with the navigation image.
   * 
   * @param navigationImageEnabled  true to enable the navigation image, false to disable it
   */
  public void setNavigationImageEnabled(boolean navigationImageEnabled) {
    if (this.navigationImageEnabled == navigationImageEnabled) {
      return;
    }
    
    this.navigationImageEnabled = navigationImageEnabled;
    
    firePropertyChange(NAVIGATION_IMAGE_ENABLED_PROPERTY, !navigationImageEnabled, navigationImageEnabled);

    if (navigationImageEnabled && image != null) {
      createNavigationImage();
    } else {
      navigationImage = null;
    }

    repaint();
  }
  
  /**
   * Zooms in by the zoom factor, on the center of the image.
   */
  public void zoomIn() {
    zoomIn(null);
  }

  /**
   * Zooms in by the zoom factor, use the specified point as the center.
   * 
   * @param zoomCenter  the center of the zoom
   */
  private void zoomIn(Point zoomCenter) {
    if (zoomCenter == null) {
      zoomCenter = getPanelCenter();
    }
    
    int power = floorlog2(scale);
    double newScale = Math.pow(2, power+1);
    
    smoothScale(newScale, zoomCenter);
  }

  /**
   * Zooms out by the zoom factor, on the center of the image.
   */
  public void zoomOut() {
    zoomOut(null);
  }

  /**
   * Zooms out by the zoom factor, use the specified point as the center.
   * 
   * @param zoomCenter  the center of the zoom
   */
  private void zoomOut(Point zoomCenter) {
    if (zoomCenter == null) {
      zoomCenter = getPanelCenter();
    }
    
    int power = floorlog2(scale);
    double newScale = Math.pow(2, power-1);

    smoothScale(newScale, zoomCenter);
  }
  
  /**
   * Gets the point at the center of the panel.
   * @return
   */
  private Point getPanelCenter() {
    return new Point(getWidth() / 2, getHeight() / 2);
  }
  
  /**
   * Computes the floor of log base 2 of a.
   * 
   * @param a  the value
   * @return   the floor of log base 2 of a
   */
  private static int floorlog2(double a) {
    return (int) Math.floor(Math.log(a)/Math.log(2));
  }

  /**
   * Gets the scale used to display the image.
   * 
   * @return  the image scale
   */
  public double getScale() {
    return scale;
  }

  /**
   * Sets the zoom level used to display the image.
   * 
   * @param newScale  the zoom level used to display this panel's image
   */
  public void setScale(double newScale) {
    setScale(newScale, true);
  }
  
  /**
   * Sets the zoom level used to display the image.
   * 
   * @param newScale  the zoom level used to display this panel's image
   * @param repaint   if true, call repaint
   */
  private void setScale(double newScale, boolean repaint) {
    Point scaleCenter = new Point(getWidth() / 2, getHeight() / 2);
    setScale(newScale, scaleCenter, repaint);
  }

  /**
   * Sets the zoom level used to display the image, and the zooming center,
   * around which zooming is done.
   * 
   * @param newScale     the zoom level used to display this panel's image
   * @param scaleCenter  the point to scale with respect to
   * @param repaint      if true, call repaint
   */  
  private void setScale(double newScale, Point scaleCenter, boolean repaint) {
    setAutoScaling(false, false);
    
    double oldScale = scale;
    
    if (image == null) {
      scale = newScale;
      firePropertyChange(SCALE_PROPERTY, oldScale, newScale);
      return;
    }

    // get the image coordinates for the scaling center and bound them to the
    // image dimensions
    Coords imageP = panelToImageCoords(scaleCenter);
    if (imageP.x < 0.0) {
      imageP.x = 0.0;
    }
    if (imageP.y < 0.0) {
      imageP.y = 0.0;
    }
    if (imageP.x >= image.getWidth()) {
      imageP.x = image.getWidth() - 1.0;
    }
    if (imageP.y >= image.getHeight()) {
      imageP.y = image.getHeight() - 1.0;
    }

    // limit the maximum scale
    if (newScale > MAXIMUM_SCALE) {
      newScale = MAXIMUM_SCALE;
    }
    
    // limit the scale so image is not too small
    double autoScale = getAutoScale();
    double minimumScale = Math.min(autoScale, 1);
    if (newScale <= minimumScale) {
      newScale = minimumScale;
    }

    if (newScale <= autoScale) {
      setCursor(Cursor.getDefaultCursor());
    } else {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    Coords correctedP = imageToPanelCoords(imageP);
    scale = newScale;
    Coords panelP = imageToPanelCoords(imageP);

    origin.x += (correctedP.getIntX() - (int) panelP.x);
    origin.y += (correctedP.getIntY() - (int) panelP.y);

    boundImageOrigin();

    firePropertyChange(SCALE_PROPERTY, oldScale, newScale);

    if (repaint) {
      repaint();
    }
  }
  
  /**
   * Gets the minimum allowable scale value.
   * 
   * @return  the minimum scale
   */
  private double getMinimumScale() {
    double autoScale = getAutoScale();
    return Math.min(autoScale, 1);
  }
  
  /**
   * Sets the scale value in a smooth way by doing it in several smaller steps.
   * This has the appearances of smoothly zooming in or out.
   * 
   * @param newScale     the zoom level used to display this panel's image
   * @param scaleCenter  the point to scale with respect to
   */
  private void smoothScale(double newScale, Point scaleCenter) {
    double minimumScale = getMinimumScale();
    if (newScale > MAXIMUM_SCALE) {
      newScale = MAXIMUM_SCALE;
    } else if (newScale < minimumScale) {
      newScale = minimumScale;
    }
    
    if (newScale == scale) {
      return;
    }
    
    double oldScale = getScale();
    double scaleIncrement = (newScale - oldScale) / SMOOTH_SCALE_INCREMENTS;
    
    setHighQualityRenderingEnabled(false);
    
    for (int i=1; i<=SMOOTH_SCALE_INCREMENTS; i++) {
      double s = oldScale + (i * scaleIncrement);
      setScale(s, scaleCenter, false);
      if (i == SMOOTH_SCALE_INCREMENTS) setHighQualityRenderingEnabled(true);
      paintImmediately(getBounds());
    }
  }

  /**
   * Indicates whether auto scaling is enabled. When auto scaling is enabled,
   * the scale will be automatically changed to maximize the image dimensions
   * based on the current panel dimensions.
   * 
   * @return  true if auto scaling is enabled, false if it is disabled
   */
  public boolean isAutoScaling() {
    return autoScaling;
  }

  /**
   * Enables or disables auto scaling.
   * 
   * @param autoScaling  true to enabled auto scaling, false to disable it
   */
  public void setAutoScaling(boolean autoScaling) {
    setAutoScaling(autoScaling, true);
  }

  /**
   * Enables or disables auto scaling.
   * 
   * @param autoScaling  true to enabled auto scaling, false to disable it
   * @param repaint      if true, call repaint
   */
  private void setAutoScaling(boolean autoScaling, boolean repaint) {
    if (this.autoScaling == autoScaling) {
      return;
    }

    boolean oldAutoScaling = this.autoScaling;
    this.autoScaling = autoScaling;

    firePropertyChange(AUTO_SCALING_PROPERTY, oldAutoScaling, autoScaling);

    if (autoScaling && image != null && repaint) {
      autoScale();

      repaint();
    }
  }
  
  /**
   * Gets the scale factor that would be used to scale the image with the
   * current panel size. Auto scaling doesn't need to be enabled for this method
   * to return a correct value.
   * 
   * @return  the current auto scale factor
   */
  private double getAutoScale() {
    double xScale = (double) getWidth() / image.getWidth();
    double yScale = (double) getHeight() / image.getHeight();
    double autoScale = Math.min(xScale, yScale);
    return autoScale;
  }
  
  /**
   * Automatically scales the image to maximize its dimensions in the panel. The
   * image is also centered in the panel.
   */
  private void autoScale() {
    double oldScale = scale;
    
    // scale the image to the panel
    scale = getAutoScale();
    
    if (scale != oldScale) {
      firePropertyChange(SCALE_PROPERTY, oldScale, scale);
    }
    
    setCursor(Cursor.getDefaultCursor());
    
    autoCenter();
  }
  
  /**
   * Centers the image in the panel.
   */
  private void autoCenter() {
    // centers the image in the panel
    origin.x = (int) (getWidth() - getScaledImageWidth()) / 2;
    origin.y = (int) (getHeight() - getScaledImageHeight()) / 2;
  }

  /**
   * Gets the image origin in the panel.
   * 
   * @return  the origin of the image in the panel
   */
  public Point getImageOrigin() {
    return origin;
  }

  /**
   * Sets the image origin in the panel.
   * 
   * @param x  the x coordinate of the new image origin
   * @param y  the y coordinate of the new image origin
   */
  public void setImageOrigin(int x, int y) {
    setImageOrigin(x, y, true);
  }

  /**
   * Sets the image origin in the panel. Optionally calls repaint.
   * 
   * @param x        the x coordinate of the new image origin
   * @param y        the y coordinate of the new image origin
   * @param repaint  if true, call repaint
   */
  private void setImageOrigin(int x, int y, boolean repaint) {
    setAutoScaling(false, false);
    
    Point origin = new Point(x, y);

    if (this.origin.equals(origin)) {
      return;
    }
    
    this.origin = origin;
    
    boundImageOrigin();

    if (repaint) {
      repaint();
    }
  }
  
  public void scrollUp() {
    smoothScroll(origin.x, origin.y + (getHeight() / SCROLL_INCREMENTS_PER_PAGE));
  }
  
  public void pageUp() {
    smoothScroll(origin.x, origin.y + getHeight());
  }
  
  public void scrollLeft() {
    smoothScroll(origin.x + (getWidth() / SCROLL_INCREMENTS_PER_PAGE), origin.y);
  }

  public void pageLeft() {
    smoothScroll(origin.x + getWidth(), origin.y);
  }

  public void scrollDown() {
    smoothScroll(origin.x, origin.y - (getHeight() / SCROLL_INCREMENTS_PER_PAGE));
  }
  
  public void pageDown() {
    smoothScroll(origin.x, origin.y - getHeight());
  }

  public void scrollRight() {
    smoothScroll(origin.x - (getWidth() / SCROLL_INCREMENTS_PER_PAGE), origin.y);
  }

  public void pageRight() {
    smoothScroll(origin.x - getWidth(), origin.y);
  }
  
  /**
   * Smoothly scroll to the new origin.
   * 
   * @param x  the x coordinate of the new image origin
   * @param y  the y coordinate of the new image origin
   */
  private void smoothScroll(int x, int y) {
    Point oldOrigin = getImageOrigin();
    int oldX = oldOrigin.x;
    int oldY = oldOrigin.y;
    
    int xIncrement = (x - oldX) / SMOOTH_SCROLL_INCREMENTS;
    int yIncrement = (y - oldY) / SMOOTH_SCROLL_INCREMENTS;
    
    setHighQualityRenderingEnabled(false);
    
    for (int i=1; i<= SMOOTH_SCROLL_INCREMENTS; i++) {
      int xx = oldX + (i * xIncrement);
      int yy = oldY + (i * yIncrement);
      if (i == SMOOTH_SCROLL_INCREMENTS) setHighQualityRenderingEnabled(true);
      setImageOrigin(xx, yy, false);
      paintImmediately(getBounds());
    }
  }

  /**
   * Bounds the images origin so it can not be moved out of view in the panel.
   */
  private void boundImageOrigin() {
    if (origin.x > 0) {
      origin.x = 0;
    }

    if (origin.y > 0) {
      origin.y = 0;
    }

    if (origin.x + getScaledImageWidth() < getWidth()) {
      origin.x = getWidth() - getScaledImageWidth();
    }

    if (origin.y + getScaledImageHeight() < getHeight()) {
      origin.y = getHeight() - getScaledImageHeight();
    }

    if (origin.x >= 0 && (origin.x + getScaledImageWidth()) <= getWidth()) {
      origin.x = (int) (getWidth() - getScaledImageWidth()) / 2;
    }

    if (origin.y >= 0
        && (origin.y + getScaledImageHeight()) <= getHeight()) {
      origin.y = (int) (getHeight() - getScaledImageHeight()) / 2;
    }
  }
  
  /**
   * Centers the image on the point specified in panel coordinates.
   * 
   * @param panelPoint  the point in the panel
   */
  private void centerImageFromPanelPoint(Point panelPoint) {
    int newOriginX = (origin.x - panelPoint.x) + Math.round(getWidth() / 2f);
    int newOriginY = (origin.y - panelPoint.y) + Math.round(getHeight() / 2f);
    
    smoothScroll(newOriginX, newOriginY);
  }

  /**
   * Centers the image using the point from the navigation image.
   * 
   * @param navigationPoint  the point in the navigation image
   */
  private void centerImageFromNavigationPoint(Point navigationPoint) {
    Point scaledImagePoint = navigationToScaledImagePoint(navigationPoint);
    int newOriginX = -(scaledImagePoint.x - getWidth() / 2);
    int newOriginY = -(scaledImagePoint.y - getHeight() / 2);

    smoothScroll(newOriginX, newOriginY);
  }

  /**
   * Moves the image to the specified point, relative to the previous mouse
   * position.
   * 
   * @param p  the point to move relative to
   */
  private void moveImageRelativeToMouse(Point p) {
    int xDelta = p.x - mousePosition.x;
    int yDelta = p.y - mousePosition.y;
    int newOriginX = origin.x + xDelta;
    int newOriginY = origin.y + yDelta;

    setImageOrigin(newOriginX, newOriginY);
  }
  
  /**
   * Gets if high quality rendering is enabled.
   * 
   * @return  true if high quality rendering is enabled, false otherwise
   */
  public boolean isHighQualityRenderingEnabled() {
    return highQualityRenderingEnabled;
  }
  
  /**
   * Enables or disables high quality rendering.
   * 
   * @param enabled  if true, high quality rendering is enabled
   */
  public void setHighQualityRenderingEnabled(boolean enabled) {
    if (highQualityRenderingEnabled == enabled) {
      return;
    }
    
    highQualityRenderingEnabled = enabled;
    
    firePropertyChange(HIGH_QUALITY_RENDERING_ENABLED_PROPERTY, !enabled, enabled);
  }
  
  /**
   * Gets the threshold value under which high quality rendering will be used.
   * 
   * @return  the threshold
   */
  public double getHighQualityRenderingThreshold() {
    return HIGH_QUALITY_RENDERING_SCALE_THRESHOLD;
  }

  /**
   * Gets the bounds of the image area currently displayed in the panel.
   * 
   * @return  the bounds of the image
   */
  private Rectangle getImageClipBounds() {
    Coords startCoords = panelToImageCoords(new Point(0, 0));
    Coords endCoords = panelToImageCoords(new Point(getWidth() - 1,
        getHeight() - 1));
    int panelX1 = startCoords.getIntX();
    int panelY1 = startCoords.getIntY();
    int panelX2 = endCoords.getIntX();
    int panelY2 = endCoords.getIntY();
    // No intersection?
    if (panelX1 >= image.getWidth() || panelX2 < 0
        || panelY1 >= image.getHeight() || panelY2 < 0) {
      return null;
    }

    int x1 = (panelX1 < 0) ? 0 : panelX1;
    int y1 = (panelY1 < 0) ? 0 : panelY1;
    int x2 = (panelX2 >= image.getWidth()) ? image.getWidth() - 1 : panelX2;
    int y2 = (panelY2 >= image.getHeight()) ? image.getHeight() - 1 : panelY2;
    return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
  }

  /**
   * Paints the panel and its image at the current zoom level, location, and
   * interpolation method dependent on the image scale.
   * 
   * @param g  the <code>Graphics</code> context for painting
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (image == null) {
      return;
    }

    if (highQualityRenderingEnabled &&
        scale > HIGH_QUALITY_RENDERING_SCALE_THRESHOLD) {
      Rectangle rect = getImageClipBounds();
      if (rect == null || rect.width == 0 || rect.height == 0) {
        return;
      }

      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);

      int x0 = Math.max(0, origin.x);
      int y0 = Math.max(0, origin.y);
      int width = Math.min((int) (rect.width * scale), getWidth());
      int height = Math.min((int) (rect.height * scale), getHeight());

      g2.drawImage(image, x0, y0, x0 + width - 1, y0 + height - 1, rect.x,
          rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1, null);

    } else {
      g.drawImage(image, origin.x, origin.y, getScaledImageWidth(),
          getScaledImageHeight(), null);
    }

    // draw navigation image
    if (isNavigationImageEnabled() && !scaledImageFitsInPanel()) {
      g.drawImage(navigationImage, 0, 0, getNavigationImageWidth(),
          getNavigationImageHeight(), null);

      g.setColor(Color.black);
      g.drawRect(0, 0, getNavigationImageWidth() - 1,
          getNavigationImageHeight() - 1);

      drawZoomAreaOutline(g);
    }
  }

  /**
   * Paints a white outline over the navigation image indicating the area of the
   * image currently displayed in the panel.
   * 
   * @param g  the <code>Graphics</code> context for painting
   */
  private void drawZoomAreaOutline(Graphics g) {
    int x = -Math.min(0, origin.x) * getNavigationImageWidth()
        / getScaledImageWidth();
    int y = -Math.min(0, origin.y) * getNavigationImageHeight()
        / getScaledImageHeight();

    int width = (getWidth() - Math.max(0, origin.x)) * getNavigationImageWidth()
        / getScaledImageWidth();
    if (x + width > getNavigationImageWidth()) {
      width = getNavigationImageWidth() - x;
    }

    int height = (getHeight() - Math.max(0, origin.y))
        * getNavigationImageHeight() / getScaledImageHeight();
    if (y + height > getNavigationImageHeight()) {
      height = getNavigationImageHeight() - y;
    }

    g.setColor(Color.white);
    g.drawRect(x, y, width - 1, height - 1);
  }

  /**
   * Gets the size of the scaled image.
   * 
   * @return  the scaled image size
   */
  public Dimension getScaledImageSize() {
    return new Dimension(getScaledImageWidth(), getScaledImageHeight());
  }

  /**
   * Gets the width of the scaled image.
   * 
   * @return  the width of the scaled image
   */
  private int getScaledImageWidth() {
    return (int) (scale * image.getWidth());
  }

  /**
   * Gets the height of the scaled image.
   * 
   * @return  the height of the scaled image
   */
  private int getScaledImageHeight() {
    return (int) (scale * image.getHeight());
  }

  /**
   * Gets the width of the navigation image.
   * 
   * @return  the width of the navigation image
   */
  private int getNavigationImageWidth() {
    return navigationImage.getWidth();
  }

  /**
   * Gets the height of the navigation image
   * 
   * @return  the height of the navigation image
   */
  private int getNavigationImageHeight() {
    return navigationImage.getHeight();
  }

  @Override
  public Dimension getPreferredSize() {
    if (image != null) {
      return getScaledImageSize();
    } else {
      return super.getPreferredSize();
    }
  }

}