/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.nees.buffalo.rdv.DataViewer;
import org.nees.rbnb.marker.EventMarker;

/**
 * A component that lets the user select a specific time or time range between a
 * bounded time interval by sliding knobs along a timeline. It also allows one
 * to display an event indicator at a specific time in the time line.
 *  
 * @author Jason P. Hanley
 */
public class TimeSlider extends JComponent implements MouseListener, MouseMotionListener {
  /** The minimum time. */
  double minimum;
  
  /** The start of the selected time range. */
  double start;
  
  /** The selected time. */
  double value;
  
  /** The end of the selected time range. */
  double end;
  
  /** The maximum time. */
  double maximum;
  
  /** Indicates if a time range may be selected. */ 
  boolean useRange;
  
  /** Indicates if the time value is adjusting. */
  boolean isAdjusting;
  
  /** The starting offset of a click (for buttons). */
  int clickStart;
  
  /** List of event marker.s */
  List<EventMarker> markers;  
  
  /** List of time discontinuities. */
  List<Discontinuity> discontinuities;
  
  /** List of time adjustment listeners. */
  List<TimeAdjustmentListener> adjustmentListeners;

  /** The button used to indicate the start time. */
  JButton startButton;
  
  /** The button used to indicate the time. */
  JButton valueButton;
  
  /** The button used to indicate the end time. */
  JButton endButton;
  
  /** The image used to show a marker. */
  Image markerImage;

  /**
   * Creates a time slider with the maximum available range.
   */
  public TimeSlider() {
    super();

    minimum = Double.MIN_VALUE;
    start = Double.MIN_VALUE;
    value = 0;
    end = Double.MAX_VALUE;
    maximum = Double.MAX_VALUE;
    
    useRange = true;
    
    isAdjusting = false;
    
    markers = new ArrayList<EventMarker>();
    
    discontinuities = new ArrayList<Discontinuity>();
    
    adjustmentListeners = new ArrayList<TimeAdjustmentListener>();

    setBorder(null);
    setLayout(null);
    
    setToolTipText("");
    
    addMouseListener(this);

    valueButton = new JButton(DataViewer.getIcon("icons/time.gif")) {
      public JToolTip createToolTip() {
        return TimeSlider.this.createToolTip();
      }
      public String getToolTipText(MouseEvent me) {
        return TimeSlider.this.getToolTipText(me);
      }
    };
    valueButton.setToolTipText("");
    valueButton.setOpaque(false);
    valueButton.setBorder(null);
    valueButton.addMouseMotionListener(this);
    valueButton.addMouseListener(this);
    add(valueButton);    

    startButton = new JButton(DataViewer.getIcon("icons/left_bound.gif"));
    startButton.setOpaque(false);
    startButton.setBorder(null);
    startButton.addMouseListener(this);
    startButton.addMouseMotionListener(this);    
    add(startButton);

    endButton = new JButton(DataViewer.getIcon("icons/right_bound.gif"));
    endButton.setOpaque(false);
    endButton.setBorder(null);
    endButton.addMouseListener(this);
    endButton.addMouseMotionListener(this);
    add(endButton);
    
    markerImage = DataViewer.getImage("icons/marker.gif");
  }
  
  /**
   * Sets if the time value of the slider can be changed by the UI element.
   * 
   * @param changeable  if true, the value can be changed, false otherwise.
   */
  public void setValueChangeable(boolean changeable) {
    if (changeable) {
      addMouseListener(this);
      valueButton.addMouseMotionListener(this);
    } else {
      removeMouseListener(this);
      valueButton.removeMouseMotionListener(this);
    }
  }
  
  /**
   * Set if a time range can be specified via the UI elements
   * 
   * @param enabled  if true, the time range can be changed, false otherwsie
   */
  public void setRangeEnabled(boolean enabled) {
    if (useRange == enabled) {
      return;
    }
    
    useRange = enabled;
    
    if (useRange) {
      add(startButton);
      add(endButton);
    } else {
      remove(startButton);
      remove(endButton);
    }
    
    doLayout();
  }
  
  /**
   * Gets the time value set by the slider.
   * 
   * @return  the time set by the slider
   */
  public double getValue() {
    return value;
  }

  /**
   * Sets the time value of the slider.
   * 
   * @param value  the time value to set
   */
  public void setValue(double value) {
    if (this.value != value) {
      this.value = value;
      doLayout();
      fireValueChanged();
    }
  }

  /**
   * Sets the maximum and minimum values.
   * 
   * @param minimum  the minimum allowed time value
   * @param maximum  the maximum allowed time value
   */
  public void setValues(double minimum, double maximum) {
    setMinimum(minimum);
    setMaximum(maximum);
  }  
  
  /**
   * Sets the time value, and the maximum and minimum time values.
   * 
   * @param value    the time value
   * @param minimum  the minimum allowed time value
   * @param maximum  the maximum allowed time value
   */
  public void setValues(double value, double minimum, double maximum) {
    setMinimum(minimum);
    setMaximum(maximum);
    setValue(value);
  }

  /**
   * Sets the maximum and minimum time value, and the time range.
   *  
   * @param minimum  the minimum allowed time value
   * @param maximum  the maximum allowed time value
   * @param start    the start of the time range
   * @param end      the end of the time range
   */
  public void setValues(double minimum, double maximum, double start, double end) {
    setStart(start);
    setEnd(end);
    setMinimum(minimum);
    setMaximum(maximum);
  }

  /**
   * Sets the maximum and minimum time value, the time range, and the time
   * value.
   *  
   * @param value    the time value
   * @param minimum  the minimum allowed time value
   * @param maximum  the maximum allowed time value
   * @param start    the start of the time range
   * @param end      the end of the time range
   */
  public void setValues(double value, double minimum, double maximum, double start, double end) {
    setStart(start);
    setEnd(end);
    setMinimum(minimum);
    setMaximum(maximum);
    setValue(value);    
  }  

  /**
   * Gets the start of the selected time range.
   * 
   * @return  the start of the time range
   */
  public double getStart() {
    return start;
  }
  
  /**
   * Sets the start of the time range.
   * 
   * @param start  the start of the time range
   */
  public void setStart(double start) {
    if (this.start != start) {
      this.start = start;
      doLayout();
      fireRangeChanged();
    }
  }
  
  /**
   * Gets the end of the selected time range.
   * 
   * @return  the end of the time range
   */
  public double getEnd() {
    return end;
  }
  
  /**
   * Sets the end of the time range.
   * 
   * @param end  the end of the time range
   */
  public void setEnd(double end) {
    if (this.end != end) {
      this.end = end;
      doLayout();
      fireRangeChanged();
    }
  }
  
  /**
   * Gets the minimum allowed time value.
   * 
   * @return  the minimum allowed time value
   */
  public double getMinimum() {
    return minimum;
  }
  
  /**
   * Sets the minimum allowed time value. If this is greater than the selected
   * time range, it will be set to be equal to this value.
   * 
   * @param minimum  the minimum allowed time value
   */
  public void setMinimum(double minimum) {
    if (this.minimum != minimum) {
      this.minimum = minimum;
      if (minimum > start) {
        start = minimum;
        fireRangeChanged();
      }
      if (minimum > end) {
        end = minimum;
        fireRangeChanged();
      }
      fireBoundsChanged();
      doLayout();
    }
  }
  
  /**
   * Gets the maximum allowed time value.
   * 
   * @return  the maximum allowed time value.
   */
  public double getMaximum() {
    return maximum;
  }
  
  /**
   * Sets the maximum allowed time value. If this is less than the selected time
   * range, it will be set to be equal to this value.
   * 
   * @param maximum  the maximym allowed time value
   */
  public void setMaximum(double maximum) {
    if (this.maximum != maximum) {
      this.maximum = maximum;
      if (maximum < end) {
        end = maximum;
        fireRangeChanged();
      }
      if(maximum < start) {
        start = maximum;
        fireRangeChanged();
      }
      fireBoundsChanged();
      doLayout();
    }
  }
  
  /**
   * Add a marker to the slider.
   * 
   * @param marker  the marker
   */
  public void addMarker(EventMarker marker) {
    markers.add(marker);
    repaint();
  }
  
  /**
   * Get the event marker that is closest to the given time.
   * 
   * @param time  the time to search around
   * @return      the event marker closest to the time, or null if there are
   *              none
   */
  private EventMarker getMarkerClosestToTime(double time) {
    EventMarker eventMarker = null;
    double distance = Double.MAX_VALUE;

    for (EventMarker marker : markers) {
      double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
      double thisDistance = Math.abs(time-markerTime);
      if (eventMarker == null || thisDistance < distance) {
        eventMarker = marker;
        distance = thisDistance;
      }
    }

    return eventMarker;
  }
  
  /**
   * Get markers around the specified time. This will look before and after
   * the time according to the offset.
   * 
   * @param time    the time to look for markers at
   * @param offset  the offset from the time to look
   * @return        a list of markers within this time range
   */
  private List<EventMarker> getMarkersAroundTime(double time, double offset) {
    double lowerBound = time-offset;
    double upperBound = time+offset;
    
    List<EventMarker> markersAround = new ArrayList<EventMarker>();
    
    for (EventMarker marker : markers) {
      double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
      if (markerTime >= lowerBound && markerTime <= upperBound) {
        markersAround.add(marker);
      }
    }
    
    return markersAround;
  }
  
  /**
   * Remove all marker.
   */
  public void cleareMarkers() {
    markers.clear();
    repaint();
  }
  
  /**
   * Add a time discontinuity.
   * 
   * @param start  the start of the discontinuity
   * @param end    the end of the discontinuity
   */
  public void addDiscontinuity(double start, double end) {
    Discontinuity discontinuity = new Discontinuity(start, end);

    for (Discontinuity d : discontinuities) {
      if (d.overlaps(discontinuity)) {
        d.join(discontinuity);
        checkDiscontinuity(d);
        return;
      }
    }
    
    discontinuities.add(discontinuity);
  }
  
  /**
   * Checks the discontinuity to make sure it doesn't overlap the one following
   * it. If they do overlap, the two are joined, and the the following
   * discontinuity is removed.
   * 
   * @param discontinuity  the discontinuity to check
   */
  private void checkDiscontinuity(Discontinuity discontinuity) {
    int index = discontinuities.indexOf(discontinuity);
    if (index >= 0 && index+1 < discontinuities.size()) {
      Discontinuity d = discontinuities.get(index+1);
      if (discontinuity.end >= d.start) {
        discontinuity.end = d.end;
        discontinuities.remove(d);
      }
    }
  }
  
  /**
   * Remove all time discontinuities. 
   */
  public void clearDiscontinuities() {
    discontinuities.clear();
  }
  
  /**
   * Indicates if the value is being continuously changed in the UI.
   * 
   * @return  true if the value is changing, false otherwise
   */
  public boolean isValueAdjusting() {
    return isAdjusting;
  }
  
  /**
   * Return the time corresponding to the x coordinate of the time slider.
   * 
   * @param x  the horizontal point on the slider component
   * @return   the time
   */
  private double getTimeFromX(int x) {
    Insets insets = getInsets();
    return ((double)(x-6-insets.left))/(getWidth()-13-insets.left-insets.right)*(maximum-minimum)+minimum;
  }
  
  /**
   * Get the horizontal point on the slider component corresponding to the given
   * time.
   * 
   * @param time  the time to get the point from
   * @return      the horizontal (x) point of the time
   */
  private int getXFromTime(double time) {
    Insets insets = getInsets();
    int width = getWidth() -  13 - insets.left - insets.right;
    return (int)Math.round((time-minimum)/(maximum-minimum)*width) + 6;
  }
  
  /**
   * Gets the amount of time one pixel on the time slider represends.
   * 
   * @return  the amount of time represented by one pixel, in seconds
   */
  private double getPixelTime() {
    Insets insets = getInsets();
    int width = getWidth() -  13 - insets.left - insets.right;    
    return (maximum-minimum)/width;
  }
  
  /**
   * Layout the buttons.
   */
  public void doLayout() {
    Insets insets = getInsets();
    
    if (maximum == minimum) {
      startButton.setVisible(false);
      valueButton.setVisible(false);
      endButton.setVisible(false);
    } else {
      startButton.setVisible(true);
      valueButton.setVisible(true);
      endButton.setVisible(true);      
    }
    
    if (useRange) {
      startButton.setBounds(insets.left + getXFromTime(start)-6, insets.top, 6, 11);
      endButton.setBounds(insets.left + getXFromTime(end)+1, insets.top, 6, 11);
    }
    
    valueButton.setBounds(insets.left + getXFromTime(value)-3, insets.top+2, 7, 7);
    
    repaint();
  }
  
  /**
   * Paint the components. Also paint the slider and the markers.
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    Insets insets = getInsets();
    
    g.setColor(Color.lightGray);
    g.fillRect(insets.left+6, insets.top+4, getWidth()-insets.left-12-insets.right, 3);
    
    if (isEnabled() && useRange) {
      g.setColor(Color.gray);
      int startX = this.getXFromTime(start);
      int endX = this.getXFromTime(end);
      g.fillRect(insets.left+startX, insets.top+4, insets.left+(endX-startX), 3);      
    }
    
    for (EventMarker marker : markers) {
      double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
      if (markerTime >= minimum && markerTime <= maximum) {
        int x = getXFromTime(markerTime);
        g.drawImage(markerImage, x-1, insets.top, null);
      }
    }
  }
  
  /**
   * Get the minimum dimensions for this component.
   * 
   * @return  the minimum dimensions
   */
  public Dimension getMinimumSize() {
    Insets insets = getInsets();
    return new Dimension(insets.left + 6 + 6 + 1 + insets.right, insets.top + 11 + insets.bottom);
  }
  
  /**
   * Get the preferred size for this component.
   * 
   * @return  the preferred dimensions
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }
  
  /**
   * Creates the tooltip for the component. This changes the default tooltip by
   * setting a different border.
   * 
   * @return  the tooltip created
   */
  public JToolTip createToolTip() {
    JToolTip toolTip = super.createToolTip();
    toolTip.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(5,5,5,5)));
    return toolTip;
  }
  
  /**
   * Gets the text for the tooltip. This will return a description of the
   * markers the mouse pointer is around. If there are no markers, this returns
   * null.
   * 
   * @param me  the mouse event that triggered this
   * @return    text describing the markers around the mouse pointer
   */
  public String getToolTipText(MouseEvent me) {
    int x = me.getX();
    if (me.getSource() == valueButton) {
      x += valueButton.getX();
    }

    double time = getTimeFromX(x);
    double offset = 2*getPixelTime();
    
    List<EventMarker> markersOver = getMarkersAroundTime(time, offset);

    String text = new String("<html><font size=\"5\">");
    
    int numberOfMarkers = markersOver.size();
    if (numberOfMarkers > 0) {
      text += DataViewer.formatDateSmart(time) + "<br>" +
              "Found " + numberOfMarkers + " event" +
              (numberOfMarkers==1?"":"s") +
              " (&plusmn;" + DataViewer.formatSeconds(offset) + ")<br><br>";
    } else {
      text += DataViewer.formatDateSmart(time);
    }
    
    for (EventMarker marker: markersOver) {
      String date = DataViewer.formatDate(Double.parseDouble(marker.getProperty("timestamp")));
      String source = marker.getProperty("source");
      String label = marker.getProperty("label");
      String content = marker.getProperty("content");
      
      text += "<font color=gray>" + date + "</font> ";
      if (source != null && source.length() > 0) {
        text += "<b>" + source + "</b>: ";
      }
      if (label != null && label.length() > 0) {
        text += "<u>" + label + "</u> ";
      }      
      if (content != null && content.length() > 0) {
        int maxLineLength = 75;
        int maxLines = 10;
        
        String[] words = content.split(" ");
        int lineLength = 0;
        int lines = 0;
        for  (String word : words) {          
          if (lineLength + word.length() < (lines==0?(maxLineLength-32):maxLineLength)) {
            text += " " + word;
            lineLength += word.length() + 1;
          } else {
            if (++lines == maxLines) {
              text += "...";
              break;
            }            
            text += "<br>" + word;
            lineLength = word.length();
          }
        }
      }
      text += "<br>";
    }
    text += "</font></html>";
    
    return text ;
  }  

  /**
   * Called when the mouse is dragged. This deals dragging the time value and
   * range controls.
   * 
   * @param me  the mosue event that triggered this 
   */
  public void mouseDragged(MouseEvent me) {
    if (!isEnabled()) {
      return;
    }
    
    JButton button = (JButton)me.getSource();
    int x = me.getX();
    if (button == startButton) {
      x += endButton.getWidth() - clickStart;
    } else if (button == valueButton) {
      x += Math.round(valueButton.getWidth()/2d) - clickStart;
    } else if (button == endButton) {
      x -= clickStart;
    }
    double time = getTimeFromX(button.getX() + x);
    
    if (button == startButton) {
      if (time < minimum) {
        time = minimum;
      } else if (time >= end) {
        time = end;
      }
      
      setStart(time);
    } else if (button == valueButton) {
      if (useRange) {
        if (time < start) {
          time = start;
        } else if (time > end) {
          time = end;
        }
      } else {
        if (time < minimum) {
          time = minimum;
        } else if (time > maximum) {
          time = maximum;
        }
      }
      
      setValue(time);
    } else if (button == endButton) {
      if (time < start) {
        time = start;
      } else if (time > maximum) {
        time = maximum;
      }
      
      setEnd(time);
    }
  }
  
  /**
   * Called when the mouse is pressed. Used to tell when the value control is
   * being adjusted.
   * 
   * @param me  the mouse event that triggered this
   */
  public void mousePressed(MouseEvent me) {
    clickStart = me.getX();
    
    if (me.getSource() == valueButton) {
      isAdjusting = true;
    }
  }
  
  
  /**
   * Called whn the mouse is released. Used to tell when the value control is
   * being adjusted.
   * 
   * @param me  the mouse event that triggered this
   */
  public void mouseReleased(MouseEvent me) {
    if (me.getSource() == valueButton) {
      isAdjusting = false;
    }
  }

  public void mouseMoved(MouseEvent me) {}
  
  /**
   * Called when the mouse is clicked. Used to set the time value when there is
   * a click on the time slider.
   * 
   * @param me  the mouse event that triggered this
   */
  public void mouseClicked(MouseEvent me) {
    double time;
    if (me.getSource() == this) {
      time = getTimeFromX(me.getX());      
    } else if (me.getSource() == valueButton) {
      time = getTimeFromX(valueButton.getX() + me.getX());
    } else {
      return;
    }

    if (me.getButton() == MouseEvent.BUTTON1) {
      setValue(time);
    } else if (me.getButton() == MouseEvent.BUTTON3) {
      EventMarker eventMarker = getMarkerClosestToTime(time);
      if (eventMarker != null) {
        double markerTime = Double.parseDouble(eventMarker.getProperty("timestamp"));
        setValue(markerTime);
      }
    }
  }

  public void mouseEntered(MouseEvent me) {}
  public void mouseExited(MouseEvent me) {}
  
  /**
   * Add a listener for time adjustments.
   * 
   * @param l  the listener to add
   */
  public void addTimeAdjustmentListener(TimeAdjustmentListener l) {
    adjustmentListeners.add(l);
  }
  
  /**
   * Remove a listener for time adjustments.
   * 
   * @param l   the listener to remove
   */
  public void removeTimeAdjustmentListener(TimeAdjustmentListener l) {
    adjustmentListeners.remove(l);
  }
  
  /**
   * Fires a value changed event to all adjustment listeners.
   */
  protected void fireValueChanged() {
    TimeEvent event = new TimeEvent(this);
    for (TimeAdjustmentListener l : adjustmentListeners) {
      l.timeChanged(event);
    }
  }

  /**
   * Fires a range changed event to all adjustment listeners.
   */
  protected void fireRangeChanged() {
    TimeEvent event = new TimeEvent(this);
    for (TimeAdjustmentListener l : adjustmentListeners) {
      l.rangeChanged(event);
    }
  }

  /**
   * Fires a bounds changed event to all adjustment listeners.
   */
  protected void fireBoundsChanged() {
    TimeEvent event = new TimeEvent(this);
    for (TimeAdjustmentListener l : adjustmentListeners) {
      l.boundsChanged(event);
    }
  }
  
  /**
   * Sets if this component is enabled. If the component is not enabled, the
   * time value and range controls will not respond to user input.
   * 
   * @param enabled true if this component should be enabled, false otherwise
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    startButton.setEnabled(enabled);
    valueButton.setEnabled(enabled);
    endButton.setEnabled(enabled);
    
    repaint();
  }
  
  /**
   * A time discontinuity. This is a range in time specified by a start and end.
   */
  private class Discontinuity implements Comparable<Discontinuity> {
    /** The start of the discontinuity */
    public double start;
    
    /** the end of the discontinuity */
    public double end;
    
    /**
     * Create a time discontinuity.
     * 
     * @param start  the start of the discontinuity
     * @param end    the end of the discontinuity
     */
    public Discontinuity(double start, double end) {
      this.start = start;
      this.end = end;
    }
    
    /**
     * See if the discontinuitie overlaps with this one.
     * 
     * @param d  the discontinuity to check
     * @return   true if they overlap, false if they do not
     */
    public boolean overlaps(Discontinuity d) {
      if (start < d.start && end >= d.start) {
        return true;
      } else if (start > d.start && start <= d.end) {
        return true;
      }
      
      return false;
    }
    
    /**
     * If the discontinuitie overlaps with this one, union with it.
     * 
     * @param d  the discontinutity to join with
     */
    public void join(Discontinuity d) {
      if (start < d.start && end >= d.start) {
        end = d.end;
      } else if (start > d.start && start <= d.end) {
        start = d.start;
      }      
    }

    /**
     * Compare discontinuities. This is based first on their start, and then on
     * their end (if needed).
     * 
     * @param d  the discontinuity to compare with
     * @return   0 if they are the same, -1 if this is less than the other, and
     *           1 if this is greater than the other.
     */
    public int compareTo(Discontinuity d) {
      if (start == d.start) {
        if (end == d.end) {
          return 0;
        } else if (end < d.end) {
          return -1;
        } else {
          return 1;
        }
      } else if (start < d.start) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}
