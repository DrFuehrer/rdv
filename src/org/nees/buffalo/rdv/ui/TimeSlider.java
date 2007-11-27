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
import java.awt.Cursor;
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
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
  /** The logger for this class */
  protected static final Log log = LogFactory.getLog(TimeSlider.class.getName());
  
  /** The minimum time. */
  protected double minimum;
  
  /** The start of the selected time range. */
  protected double start;
  
  /** The selected time. */
  protected double value;
  
  /** The end of the selected time range. */
  protected double end;
  
  /** The maximum time. */
  protected double maximum;
  
  /** Indicate if the time value may be changed from the UI. */
  private boolean valueChangeable;
  
  /** Indicates if a time range may be changed from the UI. */ 
  private boolean rangeChangeable;
  
  /** Indicates if the time value is adjusting. */
  private boolean isAdjusting;
  
  /** The starting offset of a click (for buttons). */
  private int clickStart;
  
  /** List of event markers. */
  protected final List<EventMarker> markers;  
  
  /** List of valid time ranges. */
  protected final List<TimeRange> timeRanges;
  
  /** List of valid times ranges, taking into account the minimum and maximum */
  protected final List<TimeRange> actualTimeRanges;
  
  /** List of time adjustment listeners. */
  private final List<TimeAdjustmentListener> adjustmentListeners;

  /** The button used to indicate the start time. */
  protected final JButton startButton;
  
  /** The button used to indicate the time. */
  protected final JButton valueButton;
  
  /** The button used to indicate the end time. */
  protected final JButton endButton;
  
  /** The default image used to show a marker. */
  protected final Image defaultMarkerImage;
  
  /** The image used to show an annotation. */
  protected final Image annotationMarkerImage;
  
  /** The image used to show a start marker. */
  protected final Image startMarkerImage;
  
  /** The image used to show a stop marker. */
  protected final Image stopMarkerImage;

  /**
   * Creates a time slider with the maximum available range.
   */
  public TimeSlider() {
    super();

    minimum = 0;
    start = 0;
    value = 0;
    end = Double.MAX_VALUE;
    maximum = Double.MAX_VALUE;
    
    valueChangeable = true;
    rangeChangeable = true;
    
    isAdjusting = false;
    
    markers = new ArrayList<EventMarker>();
    
    timeRanges = new ArrayList<TimeRange>();
    
    actualTimeRanges = new ArrayList<TimeRange>();
    calculateActualTimeRanges();
    
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
    valueButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    valueButton.setOpaque(false);
    valueButton.setBorder(null);
    valueButton.addMouseMotionListener(this);
    valueButton.addMouseListener(this);
    add(valueButton);    

    startButton = new JButton(DataViewer.getIcon("icons/left_bound.gif"));
    startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    startButton.setOpaque(false);
    startButton.setBorder(null);
    startButton.addMouseListener(this);
    startButton.addMouseMotionListener(this);    
    add(startButton);

    endButton = new JButton(DataViewer.getIcon("icons/right_bound.gif"));
    endButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    endButton.setOpaque(false);
    endButton.setBorder(null);
    endButton.addMouseListener(this);
    endButton.addMouseMotionListener(this);
    add(endButton);
    
    defaultMarkerImage = DataViewer.getImage("icons/marker.gif");
    annotationMarkerImage = DataViewer.getImage("icons/marker-annotation.gif");
    startMarkerImage = DataViewer.getImage("icons/marker-start.gif");
    stopMarkerImage = DataViewer.getImage("icons/marker-stop.gif");
  }
  
  /**
   * Sets if the time value of the slider can be changed by the UI element.
   * 
   * @param changeable  if true, the value can be changed, false otherwise.
   */
  public void setValueChangeable(boolean changeable) {
    if (valueChangeable == changeable) {
      return;
    }
    
    valueChangeable = changeable;
    
    valueButton.setEnabled(valueChangeable);
    
    if (valueChangeable) {
      addMouseListener(this);
      valueButton.addMouseListener(this);
      valueButton.addMouseMotionListener(this);
    } else {
      removeMouseListener(this);
      valueButton.removeMouseListener(this);
      valueButton.removeMouseMotionListener(this);
    }
  }
  
  /**
   * Set if the value is visible in the slider UI.
   * 
   * @param visible  if true the value is shown, if false the value is not shown
   */
  public void setValueVisible(boolean visible) {
    if (valueButton.isVisible() == visible) {
      return;
    }
    
    valueButton.setVisible(visible);
  }
  
  /**
   * Set if a time range can be changed via the UI elements
   * 
   * @param changeable  if true, the time range can be changed, false otherwsie
   */
  public void setRangeChangeable(boolean changeable) {
    if (rangeChangeable == changeable) {
      return;
    }
    
    rangeChangeable = changeable;
    
    startButton.setEnabled(rangeChangeable);
    endButton.setEnabled(rangeChangeable);
    
    if (rangeChangeable) {
      startButton.addMouseMotionListener(this);
      endButton.addMouseMotionListener(this);      
    } else {
      startButton.removeMouseMotionListener(this);
      endButton.removeMouseMotionListener(this);
    }
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
      
      calculateActualTimeRanges();
      
      if (rangeChangeable) {
        if (minimum > start) {
          start = minimum;
          fireRangeChanged();
        }
        if (minimum > end) {
          end = minimum;
          fireRangeChanged();
        }
      } else {
        start = minimum;
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
      
			calculateActualTimeRanges();
      
      if (rangeChangeable) {
        if (maximum < end) {
          end = maximum;
          fireRangeChanged();
        }
        if(maximum < start) {
          start = maximum;
          fireRangeChanged();
        }
      } else {
        end = maximum;
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
  public void addMarker(final EventMarker marker) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        markers.add(marker);
        repaint();
      }
    });
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
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        markers.clear();
        repaint();
      }
    });    
  }
  
  /**
   * Replace the current set of time ranges with this one.
   * 
   * @param newTimeRanges  a list of time ranges
   */
  public void setTimeRanges(List<TimeRange> newTimeRanges) {
    timeRanges.clear();
    timeRanges.addAll(newTimeRanges);
    
    calculateActualTimeRanges();
    
    double newValue = getClosestValidTime(value);
    if (value != newValue) {
      value = newValue;
      fireValueChanged();
    }
    
    double newStart = getClosestValidTime(start);
    double newEnd = getClosestValidTime(end);
    if (start != newStart || end != newEnd) {
      start = newStart;
      end = newEnd;
      fireRangeChanged();
    }
    
    doLayout();    
  }
  
  /**
   * Tell if the time is valid with respect to the current time bounds and
   * ranges.
   * 
   * @param time  the time to check
   * @return      true if the time is valid, false if it is not
   */
  public boolean isTimeValid(double time) {
    for (TimeRange timeRange: actualTimeRanges) {
      if (timeRange.contains(time)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Get a time that is valid and greater than or equal to the current one.
   * 
   * @return  the next valid time, or -1 if there is none
   */
  public double getNextValidTime(double time) {
    for (TimeRange tr : actualTimeRanges) {
      if (tr.contains(time)) {
        return time;
      } else if (tr.start > time) {
        return tr.start;
      }
    }
    
    return -1;
  }
  
  /**
   * Return a time that is valid and the closest possible to the given time. If
   * the provided time is valid, then the same time will be returned. If the
   * time is invalid, the nearest valid time will be returned.
   * 
   * @param time  the time to use
   * @return      the closest valid time to the given time
   */
  private double getClosestValidTime(double time) {
    double possibleTime = 0;
    
    for (int i=0; i<actualTimeRanges.size(); i++) {
      TimeRange tr = actualTimeRanges.get(i);
      
      if (i == 0 && time < tr.start) {
        return tr.start;
      } else if (i == actualTimeRanges.size()-1 && time > tr.end) {
        return tr.end;
      } else if (tr.contains(time)) {
        return time;
      } else if (time > tr.end) {
        possibleTime = tr.end;
      } else if (time-possibleTime < tr.start-time) {
        return possibleTime;
      } else {
        return tr.start;
      }
    }
    
    // this shouldn't be possible
    return time;
  }
  
  /**
   * Remove all time ranges.
   */
  public void clearTimeRanges() {
    timeRanges.clear();
    calculateActualTimeRanges();
    doLayout();
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
   * Updates the list of time ranges represented by the time slider. This
   * enforces the maximum and minimum values on the list of time ranges.
   * 
   * @see #actualTimeRanges
   */
  private void calculateActualTimeRanges() {
    actualTimeRanges.clear();
    
    if (timeRanges.size() == 0) {
      actualTimeRanges.add(new TimeRange(minimum, maximum));
    } else {
      for (TimeRange t : timeRanges) {
        if (t.start > maximum) {
          break;
        }
        
        if (t.end < minimum) {
          continue;
        }
  
        double rangeStart;
        if (t.start >= minimum) {
          rangeStart = t.start;
        } else {
          rangeStart = minimum;
        }
        
        double rangeEnd;
        if (t.end <= maximum) {
          rangeEnd = t.end;
        } else {
          rangeEnd = maximum;
        }

        actualTimeRanges.add(new TimeRange(rangeStart, rangeEnd));
      }
    }
  }
  
  /**
   * Get the minimum value allowed on the time slider. This takes into account
   * the time ranges.
   * 
   * @return  the minimum value allowed on the time slider
   */
  public double getActualMinimum() {
    if (actualTimeRanges.size() == 0) {
      return minimum;
    } else {
      return actualTimeRanges.get(0).start;
    }
  }
  
  /**
   * Get the maximum value allowed on the time slider. This takes into account
   * the time ranges.
   * 
   * @return  the maximum value allowed on the time slider
   */
  public double getActualMaximum() {
    if (actualTimeRanges.size() == 0) {
      return maximum;
    } else {
      return actualTimeRanges.get(actualTimeRanges.size()-1).end;
    }
  }  
  
  /**
   * Get the total length of time represented by the time slider. This removes
   * time contained in the gaps from time ranges.
   * 
   * @return  the total length of time represented by the time slider
   */
  private double getTimeLength() {
    double length = 0;
    
    for (TimeRange t : actualTimeRanges) {
      length += t.length();
    }
    
    return length;
  }
  
  /**
   * Get the width (in pixels) of the time portion of the slider.
   * 
   * @return  the width of the time portion of the slider
   */
  private int getTimeWidth() {
    Insets insets = getInsets();
    return getWidth() -  13 - insets.left - insets.right;    
  }
  
  /**
   * Return the time corresponding to the x coordinate of the time slider.
   * 
   * @param x  the horizontal point on the slider component
   * @return   the time
   */
  private double getTimeFromX(int x) {
    // remove left inset and button width
    x = x-6-getInsets().left;
    
    int width = getTimeWidth();
    
    if (x < 0) {
      return getActualMinimum();
    } else if (x > width) {
      return getActualMaximum();
    }    
    
    double factor = ((double)(x)) / width;
    double length = getTimeLength();    
    double value = factor*length;
    
    double position = 0;
    for (TimeRange t : actualTimeRanges) {
      double startPosition = position;
      position += t.length();
      
      if (value <= position) {
        double startFactor = startPosition / length;
        double endFactor = position / length;
        factor = (factor - startFactor) / (endFactor - startFactor);
        return (t.length()) * factor+ t.start;
      }
    }
    
    return 0;
  }
  
  /**
   * Get the horizontal point on the slider component corresponding to the given
   * time. If the time is not visible on the slider, -1 will be returned.
   * 
   * @param time  the time to get the point from
   * @return      the horizontal (x) point of the time, or -1 if the point is
   *              not visible
   */
  private int getXFromTime(double time) {
    boolean timeVisible = false;
    double position = 0;
    for (TimeRange t : actualTimeRanges) {
      if (t.contains(time)) {
        timeVisible = true;
        position += (time - t.start);
        break;
      }
      position += t.length();
    }
    
    if (!timeVisible) {
      return -1;
    }

    double factor = position / getTimeLength();
    int width = getTimeWidth();

    return (int)Math.round(factor*width) + getInsets().left + 6;
  }
  
  /**
   * Gets the amount of time one pixel on the time slider represends.
   * 
   * @return  the amount of time represented by one pixel, in seconds
   */
  private double getPixelTime() {
    return getTimeLength() / getTimeWidth();
  }
  
  /**
   * Layout the buttons.
   */
  public void doLayout() {
    Insets insets = getInsets();
    
    if (maximum == minimum) {
      startButton.setVisible(false);
      endButton.setVisible(false);
    } else {
      startButton.setVisible(true);
      endButton.setVisible(true);      
    }
    
    startButton.setBounds(getXFromTime(start) - 6, insets.top, 6, 11);
    endButton.setBounds(getXFromTime(end) + 1, insets.top, 6, 11);
    
    valueButton.setBounds(getXFromTime(value) - 3, insets.top + 2, 7, 7);
    
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
    
    if (isEnabled()) {
      g.setColor(Color.gray);
      int startX = getXFromTime(start);
      int endX = getXFromTime(end);
      g.fillRect(startX, insets.top+4, endX-startX, 3);
    }
    
    for (EventMarker marker : markers) {
      double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
      if (markerTime >= minimum && markerTime <= maximum) {
        int x = getXFromTime(markerTime);
        if (x == -1) {
          continue;
        }
        
        Image markerImage;
        
        String markerType = marker.getProperty("type");
        if (markerType.compareToIgnoreCase("annotation") == 0) {
          markerImage = annotationMarkerImage;
        } else if (markerType.compareToIgnoreCase("start") == 0) {
          markerImage = startMarkerImage;
        } else if (markerType.compareToIgnoreCase("stop") == 0) {
          markerImage = stopMarkerImage;
        } else {
          markerImage = defaultMarkerImage;
        } 
        
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
    toolTip.setBackground(Color.decode("#FFFFFC"));
    toolTip.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(5,5,5,5)));
    return toolTip;
  }
  
  /**
   * Gets the text for the tooltip. This will return a description of the
   * markers the mouse pointer is around.
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
    double timeOffset = time-value;
    
    double markerOffset = 2*getPixelTime();
    List<EventMarker> markersOver = getMarkersAroundTime(time, markerOffset);

    String text = new String("<html><font size=\"5\">");

    text += DataViewer.formatDateSmart(time);
    if (timeOffset != 0) {
      text += " (" + (timeOffset<0?"-":"+") + 
              DataViewer.formatSeconds(Math.abs(timeOffset))+ ")";
    }
    
    int numberOfMarkers = markersOver.size();
    if (numberOfMarkers > 0) {
      text += "<br>Found " + numberOfMarkers + " event" +
              (numberOfMarkers==1?"":"s") +
              " (&plusmn;" + DataViewer.formatSeconds(markerOffset) +
              ")<br><br>";
    }
    
    for (EventMarker marker: markersOver) {
      String date = DataViewer.formatDate(Double.parseDouble(marker.getProperty("timestamp")));
      String source = marker.getProperty("source");
      String type = marker.getProperty("type");
      String label = marker.getProperty("label");
      String content = marker.getProperty("content");

      boolean showType = false;
      String dateColor;
      if (type == null) {
        dateColor = "black";
      } else if (type.compareToIgnoreCase("annotation") == 0) {
        dateColor = "blue";
      } else if (type.compareToIgnoreCase("start") == 0) {
        dateColor = "green";
      } else if (type.compareToIgnoreCase("stop") == 0) {
        dateColor = "red";
      } else {
        showType = true;
        dateColor = "#FF9900";
      }      

      text += "<font color=" + dateColor + ">" + date + "</font> ";

      if (showType) {
        text += "<i>" + type + "</i> ";
      }

      if (source != null && source.length() > 0) {
        text += "by " + source + ": ";
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
      if (rangeChangeable) {
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
    if (isEnabled() == enabled) {
      return;
    }
    
    super.setEnabled(enabled);

    valueButton.setEnabled(enabled && valueChangeable);
    
    startButton.setEnabled(enabled && rangeChangeable);
    endButton.setEnabled(enabled && rangeChangeable);
    
    repaint();
  }
  
  /**
   * A time range. This is a range in time specified by a start and end time.
   */
  public static class TimeRange implements Comparable<TimeRange> {
    /** The start of the time range */
    public double start;
    
    /** The end of the time range */
    public double end;
    
    /**
     * Create a time range.
     * 
     * @param start  the start of the time range
     * @param end    the end of the time range
     */
    public TimeRange(double start, double end) {
      this.start = start;
      this.end = end;
    }
    
    /**
     * The length of the time range. Simply end-start.
     * 
     * @return  the length of the time range
     */
    public double length() {
      return end-start;
    }
    
    /**
     * See if the time is within the time range.
     * 
     * @param time  the time to check
     * @return      true if the time is within the time range, false otherwise
     */
    public boolean contains(double time) {
      return ((time >= start) && (time <= end));
    }
    
    /**
     * Compare time ranges. This is based first on their start, and then on
     * their end (if needed).
     * 
     * @param d  the time range to compare with
     * @return   0 if they are the same, -1 if this is less than the other, and
     *           1 if this is greater than the other.
     */
    public int compareTo(TimeRange t) {
      if (start == t.start) {
        if (end == t.end) {
          return 0;
        } else if (end < t.end) {
          return -1;
        } else {
          return 1;
        }
      } else if (start < t.start) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}
