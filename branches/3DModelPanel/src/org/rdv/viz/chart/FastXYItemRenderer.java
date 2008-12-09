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

package org.rdv.viz.chart;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;

/**
 * Optimized XY item renderer from JFreeChart forums.
 * 
 * @author Jason P. Hanley
 */
public class FastXYItemRenderer extends StandardXYItemRenderer {

  /** serialization version identifier */
  private static final long serialVersionUID = 4976826552487720209L;

  /** a flag to control whether the cursor visibility */
  private boolean cursorVisible;
  
  /** the size of the cursor */
  private static final int DEFAULT_CURSOR_SIZE = 10;

  /**
   * A counter to prevent unnecessary Graphics2D.draw() events in drawItem()
   */
  private int previousDrawnItem = 1;

  public FastXYItemRenderer() {
    this(LINES, null);
  }
  
  public FastXYItemRenderer(int type) {
    this(type, null);
  }    
  
  public FastXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
    this(type, toolTipGenerator, null);
  }

  public FastXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
      super(type, toolTipGenerator, urlGenerator);
      
      this.cursorVisible = false;
  }

  /**
   * Gets the cursor visibility flag. This defaults to false.
   * 
   * @return  true if the cursor is visible, false otherwise
   */
  public boolean getCursorVisible() {
    return this.cursorVisible;
  }
  
  /**
   * Sets the cursor visibility flag. If set, a cursor will be drawn to
   * indicate the position of the last data item in each series.
   * 
   * @param cursorVisible  the flag to control cursor visibility
   */
  public void setCursorVisible(boolean cursorVisible) {
    if (this.cursorVisible != cursorVisible) {
      this.cursorVisible = cursorVisible;
      fireChangeEvent();
    }
  }
  
  @Override
  public void drawItem(Graphics2D g2, XYItemRendererState state,
      Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
      ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
      int series, int item, CrosshairState crosshairState, int pass) {

    boolean itemVisible = getItemVisible(series, item);
    
    // setup for collecting optional entity info...
    Shape entityArea = null;
    EntityCollection entities = null;
    if (info != null) {
      entities = info.getOwner().getEntityCollection();
    }

    PlotOrientation orientation = plot.getOrientation();
    Paint paint = getItemPaint(series, item);
    Stroke seriesStroke = getItemStroke(series, item);
    g2.setPaint(paint);
    g2.setStroke(seriesStroke);

    // get the data point...
    double x1 = dataset.getXValue(series, item);
    double y1 = dataset.getYValue(series, item);
    if (Double.isNaN(x1) || Double.isNaN(y1)) {
      itemVisible = false;
    }

    RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
    RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
    double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
    double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

    if (getPlotLines()) {
      if (item == 0) {
        previousDrawnItem = 1;
      }

      if (getDrawSeriesLineAsPath()) {
        State s = (State) state;
        if (s.getSeriesIndex() != series) {
          // we are starting a new series path
          s.seriesPath.reset();
          s.setLastPointGood(false);
          s.setSeriesIndex(series);
        }

        // update path to reflect latest point
        if (itemVisible && !Double.isNaN(transX1) && !Double.isNaN(transY1)) {
          float x = (float) transX1;
          float y = (float) transY1;
          if (orientation == PlotOrientation.HORIZONTAL) {
            x = (float) transY1;
            y = (float) transX1;
          }
          if (s.isLastPointGood()) {
            // TODO: check threshold
            s.seriesPath.lineTo(x, y);
          } else {
            s.seriesPath.moveTo(x, y);
          }
          s.setLastPointGood(true);
        } else {
          s.setLastPointGood(false);
        }
        if (item == dataset.getItemCount(series) - 1) {
          if (s.getSeriesIndex() == series) {
            // draw path
            g2.setStroke(lookupSeriesStroke(series));
            g2.setPaint(lookupSeriesPaint(series));
            g2.draw(s.seriesPath);
          }
        }
      }

      else if (item != 0 && itemVisible) {
        // get the previous data point...
        double x0 = dataset.getXValue(series, item - previousDrawnItem);
        double y0 = dataset.getYValue(series, item - previousDrawnItem);
        if (!Double.isNaN(x0) && !Double.isNaN(y0)) {
          boolean drawLine = true;
          if (getPlotDiscontinuous()) {
            // only draw a line if the gap between the current and
            // previous data point is within the threshold
            int numX = dataset.getItemCount(series);
            double minX = dataset.getXValue(series, 0);
            double maxX = dataset.getXValue(series, numX - 1);
            if (getGapThresholdType() == UnitType.ABSOLUTE) {
              drawLine = Math.abs(x1 - x0) <= getGapThreshold();
            } else {
              drawLine = Math.abs(x1 - x0) <= ((maxX - minX) / numX * getGapThreshold());
            }
          }
          if (drawLine) {
            double transX0 = domainAxis.valueToJava2D(x0, dataArea,
                xAxisLocation);
            double transY0 = rangeAxis.valueToJava2D(y0, dataArea,
                yAxisLocation);

            // only draw if we have good values
            if (Double.isNaN(transX0) || Double.isNaN(transY0)
                || Double.isNaN(transX1) || Double.isNaN(transY1)) {
              return;
            }

            // Only draw line if it is more than a pixel away from the
            // previous one
            if ((transX1 - transX0 > 2 || transX1 - transX0 < -2
                || transY1 - transY0 > 2 || transY1 - transY0 < -2)) {
              previousDrawnItem = 1;

              if (orientation == PlotOrientation.HORIZONTAL) {
                state.workingLine.setLine(transY0, transX0, transY1, transX1);
              } else if (orientation == PlotOrientation.VERTICAL) {
                state.workingLine.setLine(transX0, transY0, transX1, transY1);
              }

              if (state.workingLine.intersects(dataArea)) {
                g2.draw(state.workingLine);
              }
            } else {
              // Increase counter for the previous drawn item.
              previousDrawnItem++;
            }
          }
        }
      }
    }

    // we needed to get this far even for invisible items, to ensure that
    // seriesPath updates happened, but now there is nothing more we need
    // to do for non-visible items...
    if (!itemVisible) {
      return;
    }
    
    // add a cursor to indicate the position of the last data item
    if (getCursorVisible() && item == dataset.getItemCount(series) - 1) {
      Line2D cursorX = new Line2D.Double(transX1 - DEFAULT_CURSOR_SIZE, transY1, transX1 + DEFAULT_CURSOR_SIZE, transY1);
      g2.draw(cursorX);       
      Line2D cursorY = new Line2D.Double(transX1, transY1 - DEFAULT_CURSOR_SIZE, transX1, transY1 + DEFAULT_CURSOR_SIZE); 
      g2.draw(cursorY);
    }
    
    if (getBaseShapesVisible()) {

      Shape shape = getItemShape(series, item);
      if (orientation == PlotOrientation.HORIZONTAL) {
        shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
      } else if (orientation == PlotOrientation.VERTICAL) {
        shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
      }
      if (shape.intersects(dataArea)) {
        if (getItemShapeFilled(series, item)) {
          g2.fill(shape);
        } else {
          g2.draw(shape);
        }
      }
      entityArea = shape;

    }

    if (getPlotImages()) {
      Image image = getImage(plot, series, item, transX1, transY1);
      if (image != null) {
        Point hotspot = getImageHotspot(plot, series, item, transX1, transY1,
            image);
        g2.drawImage(image, (int) (transX1 - hotspot.getX()),
            (int) (transY1 - hotspot.getY()), null);
        entityArea = new Rectangle2D.Double(transX1 - hotspot.getX(), transY1
            - hotspot.getY(), image.getWidth(null), image.getHeight(null));
      }

    }

    double xx = transX1;
    double yy = transY1;
    if (orientation == PlotOrientation.HORIZONTAL) {
      xx = transY1;
      yy = transX1;
    }

    // draw the item label if there is one...
    if (isItemLabelVisible(series, item)) {
      drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
          (y1 < 0.0));
    }

    int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
    int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
    updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
        rangeAxisIndex, transX1, transY1, orientation);

    // add an entity for the item...
    if (entities != null && dataArea.contains(xx, yy)) {
      addEntity(entities, entityArea, dataset, series, item, xx, yy);
    }
  }
}