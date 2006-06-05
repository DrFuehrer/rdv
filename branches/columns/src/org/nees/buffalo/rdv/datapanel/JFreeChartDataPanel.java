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

package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;
import org.nees.buffalo.rdv.rbnb.Channel;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JFreeChartDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());
	
	JFreeChart chart;
  XYPlot xyPlot;
  ValueAxis domainAxis;
  NumberAxis rangeAxis;
	ChartPanel chartPanel;
	ChannelMapCollection dataCollection;
  LegendTitle rangeLegend;
	
	JPanel chartPanelPanel;
	
	final boolean xyMode;
	
  /**
   * Plot colors for each channel (series)
   */
  HashMap<String,Color> colors;
  
  /**
   * Colors used for the series
   */
  final static Color[] seriesColors = {Color.decode("#FF0000"), Color.decode("#0000FF"),
                    Color.decode("#009900"), Color.decode("#FF9900"),
                    Color.decode("#9900FF"), Color.decode("#FF0099"),
                    Color.decode("#0099FF"), Color.decode("#990000"),
                    Color.decode("#000099"), Color.black};
    
	public JFreeChartDataPanel() {
		this(false);
	}
		
	public JFreeChartDataPanel(boolean xyMode) {
		super();
		
		this.xyMode = xyMode;
		
    colors = new HashMap<String,Color>();
		
		initChart();
		
		setDataComponent(chartPanelPanel);
	}
		
	private void initChart() {
		if (xyMode) {
			dataCollection = new ChannelMapCollection();
      
      NumberAxis domainAxis = new NumberAxis();
      domainAxis.setAutoRangeIncludesZero(true);
      domainAxis.addChangeListener(new AxisChangeListener() {
        public void axisChanged(AxisChangeEvent ace) {
          boundsChanged();
        }        
      });
      this.domainAxis = domainAxis;
      
      rangeAxis = new NumberAxis();
      rangeAxis.setAutoRangeIncludesZero(true);
      
      StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("{1} , {2}",
          new DecimalFormat(),
          new DecimalFormat());
      StandardXYItemRenderer renderer = new FastXYItemRenderer(StandardXYItemRenderer.LINES,
          toolTipGenerator);
      renderer.setDefaultEntityRadius(6);      
      
      xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
      
      chart = new JFreeChart(null, null, xyPlot, false);
		} else {
      dataCollection = new ChannelMapCollection();
      
      domainAxis = new DateAxis();
      domainAxis.setLabel("Time");
      
      rangeAxis = new NumberAxis();
      rangeAxis.setAutoRangeIncludesZero(true);
      
      StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("{0}: {1} , {2}",
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
          new DecimalFormat());
      StandardXYItemRenderer renderer = new FastXYItemRenderer(StandardXYItemRenderer.LINES,
          toolTipGenerator);
      renderer.setDefaultEntityRadius(6);

      xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
      
      chart = new JFreeChart(xyPlot);
		}
    
    rangeAxis.addChangeListener(new AxisChangeListener() {
      public void axisChanged(AxisChangeEvent ace) {
        boundsChanged();
      }        
    });    
    
		chart.setAntiAlias(false);

		chartPanel = new ChartPanel(chart, true);
    chartPanel.setInitialDelay(0);

    // get the chart panel standard popup menu
    JPopupMenu popupMenu = chartPanel.getPopupMenu();
    
    // create a popup menu item to copy an image to the clipboard
    final JMenuItem copyChartMenuItem = new JMenuItem("Copy");
    copyChartMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        copyChart();
      }
    });
    popupMenu.insert(copyChartMenuItem, 2);

    popupMenu.insert(new JPopupMenu.Separator(), 3);

		chartPanelPanel = new JPanel();
		chartPanelPanel.setLayout(new BorderLayout());
		chartPanelPanel.add(chartPanel, BorderLayout.CENTER);
	}
  
  /**
   * Takes the chart and puts it on the clipboard as an image.
   */
  private void copyChart() {
    // get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    // create an image of the chart with the preferred dimensions
    Dimension preferredDimension = chartPanel.getPreferredSize();
    Image image = chart.createBufferedImage((int)preferredDimension.getWidth(), (int)preferredDimension.getHeight());
    
    // wrap image in the transferable and put on the clipboard
    ImageSelection contents = new ImageSelection(image);
    clipboard.setContents(contents, null);
  }
  
  private void boundsChanged() {
    if (xyMode) {
      if (domainAxis.isAutoRange()) {
        properties.remove("domainLowerBound");
        properties.remove("domainUpperBound");        
      } else {
        properties.setProperty("domainLowerBound", Double.toString(domainAxis.getLowerBound()));
        properties.setProperty("domainUpperBound", Double.toString(domainAxis.getUpperBound()));        
      }
    }
    
    if (rangeAxis.isAutoRange()) {
      properties.remove("rangeLowerBound");
      properties.remove("rangeUpperBound");
    } else {
      properties.setProperty("rangeLowerBound", Double.toString(rangeAxis.getLowerBound()));
      properties.setProperty("rangeUpperBound", Double.toString(rangeAxis.getUpperBound()));
    }
  }
		
	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public boolean addChannel(String channelName) {
		if (xyMode && !channels.contains(channelName) && channels.size() == 2) {
			log.warn("We don't support more than 2 channels.");
			return false;			
		}
		
		return super.addChannel(channelName);
	}
  
  void channelAdded(String channelName) {
    String seriesName = getSeriesName(channelName);
    
    if (!xyMode) {
      if (channels.size() == 1) {
        rangeLegend = chart.getLegend();
        chart.removeLegend();
        rangeAxis.setLabel(seriesName);
      } else {
        if (chart.getLegend() == null) {
          chart.addLegend(rangeLegend);
        }
        rangeAxis.setLabel(null);
      }
      
      // find the least used color
      int usage = -1;
      Color color = null;
      for (int i=0; i<seriesColors.length; i++) {
        int seriesUsingColor = seriesUsingColor(seriesColors[i], channelName);
        if (usage == -1 || seriesUsingColor < usage) {
          usage = seriesUsingColor;
          color = seriesColors[i]; 
        }
      }      
      
      // set the series color
      colors.put(channelName, color);
			setSeriesColors();
		}
		
		setAxisName();    
  }
	
  void channelRemoved(String channelName) {
		if (!xyMode) {
      if (channels.size() == 1) {
        rangeLegend = chart.getLegend();
        chart.removeLegend();
        Object[] channelsArray = channels.toArray();
        rangeAxis.setLabel(getSeriesName((String)channelsArray[0]));
      } else {
        if (chart.getLegend() == null) {
          chart.addLegend(rangeLegend);
        }
        rangeAxis.setLabel(null);
      }
      
      colors.remove(channelName);
      setSeriesColors();
		}
	}
  
  /**
   * Count the number of channels using the specified color for their series
   * plot. The count will exclude the specified channel from the count.
   * 
   * @param color           the color to find
   * @param excludeChannel  the channel to skip
   * @return                the number of channels using this color
   */
  private int seriesUsingColor(Color color, String excludeChannel) {
    if (color == null) {
      return 0;
    }
    
    int count = 0;
    
    for (int i=0; i<channels.size(); i++) {
      String channel = (String)channels.get(i);
      Paint p = (Color)xyPlot.getRenderer().getSeriesPaint(i);
      if (p.equals(color) && !channel.equals(excludeChannel)) {
        count++;
      }
    }
    
    return count;
  }
  
  /**
   * Set the series color for all the channels.
   */
  private void setSeriesColors() {
    Iterator i = channels.iterator();
    int index = 0;
    while (i.hasNext()) {
      xyPlot.getRenderer().setSeriesPaint(index++, colors.get(i.next()));
    }
  }
	
	String getTitle() {
		if (xyMode && channels.size() == 2) {
			Object[] channelsArray = channels.toArray();
			return channelsArray[0] + " vs. " + channelsArray[1];
		} else {
			return super.getTitle();
		}
	}
  
  JComponent getChannelComponent() {
    if (xyMode && channels.size() == 2) {
      JPanel titleBar = new JPanel();
      titleBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      titleBar.setOpaque(false);
      
      Iterator i = channels.iterator();
      titleBar.add(new ChannelTitle((String)i.next()));
      
      JLabel label = new JLabel("vs.");
      label.setBorder(new EmptyBorder(0, 0, 0, 5));
      label.setForeground(SimpleInternalFrame.getTextForeground(true));
      titleBar.add(label);
      
      titleBar.add(new ChannelTitle((String)i.next()));
      
      return titleBar;
    } else {
      return super.getChannelComponent();
    }
  }  
	
	private void setAxisName() {	
		if (xyMode) {
			Object[] channelsArray = channels.toArray();
			
			if (channels.size() == 1) {
				String channelName = (String)channelsArray[0];
				String seriesName = getSeriesName(channelName);
				domainAxis.setLabel(seriesName);
			} else if (channels.size() == 2) {
				String channelName = (String)channelsArray[1];
				String seriesName = getSeriesName(channelName);
				rangeAxis.setLabel(seriesName);
			}
		}
	}
	
	private String getSeriesName(String channelName) {
		String seriesName = channelName;
    Channel channel = rbnbController.getChannel(channelName);
    if (channel != null) {
      String unit = channel.getMetadata("units");
  		if (unit != null) {
  			seriesName += " (" + unit + ")";
  		}
    }
		return seriesName;
	}
		
	public void timeScaleChanged(double timeScale) {
    if (timeScale < this.timeScale) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {      
          dataCollection.age();
        }
      });
    }
    
		super.timeScaleChanged(timeScale);
			
		if (!xyMode) {
			setTimeAxis();
		}		
	}
	
	public void postData(final ChannelMap channelMap) {
		super.postData(channelMap);
		
		SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
        dataCollection.addChannelMap(channelMap);
		  }
		});
	}

	public void postTime(double time) {
		super.postTime(time);
		
		if (!xyMode) {
			setTimeAxis();
		}		
	}

	private void setTimeAxis() {
		if (chart == null) {
			log.warn("Chart object is null. This shouldn't happen.");
			return;
		}
		
    domainAxis.setRange((time-timeScale)*1000, time*1000);
	}	
	
	void clearData() {
		if (chart == null) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
          dataCollection.clear();
			  }
		});		
	}
  
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key != null && value != null) {
      if (key.equals("domainLowerBound")) {
        domainAxis.setLowerBound(Double.parseDouble(value));
      } else if (key.equals("domainUpperBound")) {
        domainAxis.setUpperBound(Double.parseDouble(value));
      } else if (key.equals("rangeLowerBound")) {
        rangeAxis.setLowerBound(Double.parseDouble(value));
      } else if (key.equals("rangeUpperBound")) {
        rangeAxis.setUpperBound(Double.parseDouble(value));
      }      
    }
  }
	
	public String toString() {
		return "JFreeChart Data Panel";
	}
  
  /**
   * Optimized XY item renderer from JFreeChart forums.
   */
  public class FastXYItemRenderer extends StandardXYItemRenderer {
    /**
     * A counter to prevent unnecessary Graphics2D.draw() events in drawItem()
     */
    private int previousDrawnItem = 0;

    public FastXYItemRenderer() {
      super();
    }
    
    public FastXYItemRenderer(int type) {
      super(type);
    }    
    
    public FastXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
      super(type, toolTipGenerator);
    }
   
    public FastXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
        super(type, toolTipGenerator, urlGenerator);
    }
    
    public void drawItem(Graphics2D g2, XYItemRendererState state,
        Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
        ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
        int series, int item, CrosshairState crosshairState, int pass) {

      if (!getItemVisible(series, item)) {
        return;
      }
      // setup for collecting optional entity info...
      boolean bAddEntity = false;
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
        return;
      }

      RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
      RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
      double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
      double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

      if (getPlotLines()) {
        if (item == 0) {
          if (getDrawSeriesLineAsPath()) {
            State s = (State) state;
            s.seriesPath.reset();
            s.setLastPointGood(false);
          }
          previousDrawnItem = 0;
        }

        if (getDrawSeriesLineAsPath()) {
          State s = (State) state;
          // update path to reflect latest point
          if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
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
            // draw path
            g2.setStroke(getSeriesStroke(series));
            g2.setPaint(getSeriesPaint(series));
            g2.draw(s.seriesPath);
          }
        }

        else if (item != 0) {
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
                  || transY1 - transY0 > 2 || transY1 - transY0 < -2)
                  || 0 == previousDrawnItem) {
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
                bAddEntity = false;
              }
            }
          }
        }
      }

      if (getBaseShapesVisible()) {

        Shape shape = getItemShape(series, item);
        if (orientation == PlotOrientation.HORIZONTAL) {
          shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
        } else if (orientation == PlotOrientation.VERTICAL) {
          shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
        }
        if (shape.intersects(dataArea)) {
          bAddEntity = true;
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

      // draw the item label if there is one...
      if (isItemLabelVisible(series, item)) {
        double xx = transX1;
        double yy = transY1;
        if (orientation == PlotOrientation.HORIZONTAL) {
          xx = transY1;
          yy = transX1;
        }
        drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
            (y1 < 0.0));
      }

      updateCrosshairValues(crosshairState, x1, y1, transX1, transY1,
          orientation);

      // add an entity for the item...
      if (entities != null && bAddEntity) {
        addEntity(entities, entityArea, dataset, series, item, transX1, transY1);
      }
    }
  }
  
  /**
   * A JFreeChart XY data set backed by RBNB channel maps. This supports both
   * timeseries plots as well as XY plots.
   * 
   * This class is linked to it's outer class by directly using it's list of
   * channels and time bounds.
   * 
   * @author Jason P. Hanley
   */
  class ChannelMapCollection extends AbstractXYDataset {
    /**
     * A list of channel maps that contain the data.
     */
    List<ChannelMap> channelMaps;
    
    /**
     * Instantiate the data set with no data.
     */
    public ChannelMapCollection() {
      super();
      
      channelMaps = new ArrayList<ChannelMap>();
    }
    
    /**
     * Add a channel map to this data set. Data contained in this channel map
     * will be available through the data access methods. 
     * 
     * @param channelMap  the channel map to add to this data set
     */
    public void addChannelMap(ChannelMap channelMap) {
      age(false);
      channelMaps.add(channelMap);
      fireDatasetChanged();
    }
    
    /**
     * Remove all channel maps in this data set.
     */
    public void clear() {
      channelMaps.clear();
      fireDatasetChanged();
    }
    
    /**
     * Remove any channel maps that do not contain data for the current time
     * range and channels.
     * 
     * @return  true if it contains data we need, false otherwise
     */
    public boolean age() {
      return age(true);
    }

    /**
     * Remove any channel maps that do not contain data for the current time
     * range and channels.

     * @param   fireListener if true, fire a data set changed event if necessary
     * @return  true if at least one channel map was removed, false otherwise
     */
    private boolean age(boolean fireListener) {
      boolean aged = true;
      for (int i=channelMaps.size()-1; i>=0; i--) {
        ChannelMap channelMap = channelMaps.get(i);
        if (!keep(channelMap)) {
          channelMaps.remove(channelMap);
          aged = true;
        }
      }
      
      if (fireListener && aged) {
        fireDatasetChanged();
      }
      
      return aged;
    }
    
    /**
     * See if we should keep this channel map based on the current channels and
     * time range.
     * 
     * @param channelMap  the channel map to inspect
     * @return            true if it contains data we need, false otherwise
     */
    private boolean keep(ChannelMap channelMap) {
      for (int i=0; i<channels.size(); i++) {
        String channelName = (String)channels.get(i);
        int channelIndex = channelMap.GetIndex(channelName);
        if (channelIndex != -1 && keep(channelMap, channelIndex)) {
          return true;
        }
      }
      return false;
    }
    
    /**
     * See if we should keep this channel in the channel map based on the time
     * range
     * 
     * @param channelMap    the channel map containing the channel
     * @param channelIndex  the index of the channel to inspect
     * @return              true if there is data in the time range, false
     *                      otherwise
     */
    private boolean keep(ChannelMap channelMap, int channelIndex) {
      double[] times = channelMap.GetTimes(channelIndex);
      for (int i=0; i<times.length; i++) {
        if (times[i] > time-timeScale && times[i] <= time) {
          return true;
        }
      }
      return false;
    }
    
    /**
     * Get the number of data series
     * 
     * @return  the number of data series
     */
    public int getSeriesCount() {
      if (xyMode) {
        return channels.size()==2?1:0;
      } else {
        return channels.size();
      }
    }

    /**
     * Return the key for the series. This is the name of the channel.
     * 
     * @return  the key for the series
     */
    public Comparable getSeriesKey(int series) {
      if (xyMode) {
        return null;
      } else {
        return getSeriesName((String)channels.get(series));
      }
    }

    /**
     * Get the number of data points in the series.
     * 
     * @return  the number of data points
     */
    public int getItemCount(int series) {
      int count = 0;
      String channelName = (String)channels.get(series);
      for (ChannelMap cmap : channelMaps) {
        int channelIndex = cmap.GetIndex(channelName);
        if (channelIndex != -1) {
          count += cmap.GetTimes(channelIndex).length;
        }
      }
      return count;
    }

    /**
     * Get the X value for the given series and index item. If this is a time
     * series plot, this is a time value.
     * 
     * @param series  the series number
     * @param item    the data point index
     * @return        the X value, or null if no data was found
     */
    public Number getX(int series, int item) {
      if (xyMode && channels.size() != 2) {
        return null;
      }
      
      String channelName;
      if (xyMode) {
        channelName = (String)channels.get(0);
      } else {
        channelName = (String)channels.get(series);
      }
      
      int count = 0;      
      for (ChannelMap cmap : channelMaps) {
        int channelIndex = cmap.GetIndex(channelName);
        if (channelIndex != -1) {
          int points = cmap.GetTimes(channelIndex).length; 
          if (item < count+points) {
            if (xyMode) {
              return getNumber(cmap, channelIndex, item-count);
            } else {
              return (long)(cmap.GetTimes(channelIndex)[item-count]*1000);
            }
          }
          count += points;
        }
      }
      
      return null;
    }

    /**
     * Get the Y value for the given series and index item.
     * 
     * @param series  the series number
     * @param item    the data point index
     * @return        the Y value, or null if no data was found
     */
    public Number getY(int series, int item) {
      if (xyMode && channels.size() != 2) {
        return null;
      }
      
      String channelName;
      if (xyMode) {
        channelName = (String)channels.get(1);
      } else {
        channelName = (String)channels.get(series);
      }      
      
      int count = 0;
      for (ChannelMap cmap : channelMaps) {
        int channelIndex = cmap.GetIndex(channelName);
        if (channelIndex != -1) {
          int points = cmap.GetTimes(channelIndex).length; 
          if (item < count+points) {
            return getNumber(cmap, channelIndex, item-count); 
          }
          count += points;
        }
      }
      
      return null;
    }
    
    /**
     * Get the numeric data point of the specified channel and point from the
     * channel map.
     * 
     * @param channelMap    the channel map containg the data
     * @param channelIndex  the index of the data channel
     * @param dataIndex     the index of the data point
     * @return              the number, or null if the data is not numeric
     */
    private Number getNumber(ChannelMap channelMap, int channelIndex, int dataIndex) {
      int typeID = channelMap.GetType(channelIndex);
      switch (typeID) {
        case ChannelMap.TYPE_FLOAT64:
          return channelMap.GetDataAsFloat64(channelIndex)[dataIndex];
        case ChannelMap.TYPE_FLOAT32:
          return channelMap.GetDataAsFloat32(channelIndex)[dataIndex];         
        case ChannelMap.TYPE_INT64:
          return channelMap.GetDataAsInt64(channelIndex)[dataIndex];
        case ChannelMap.TYPE_INT32:
          return channelMap.GetDataAsInt32(channelIndex)[dataIndex];
        case ChannelMap.TYPE_INT16:
          return channelMap.GetDataAsInt16(channelIndex)[dataIndex];
        case ChannelMap.TYPE_INT8:
          return channelMap.GetDataAsInt8(channelIndex)[dataIndex];
        default:
          return null;
      }
    }
  }
}