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
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
	XYDataset dataCollection;
  LegendTitle rangeLegend;
	
	JPanel chartPanelPanel;
	
	final boolean xyMode;
	
	int lastXYDataIndex;
  
  ChannelMap cachedChannelMap;
  
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
		
		lastXYDataIndex = -1;
    
    colors = new HashMap<String,Color>();
		
		initChart();
		
		setDataComponent(chartPanelPanel);
	}
		
	private void initChart() {
		if (xyMode) {
			dataCollection = new XYSeriesCollection();
      
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
			dataCollection = new TimeSeriesCollection();
      
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
    
    if (xyMode) {
      if (channels.size() == 1) {
        XYSeries data = new FastXYSeries(seriesName);
        ((XYSeriesCollection)dataCollection).addSeries(data);
      }
    } else {
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
      
			TimeSeries data = new FastTimeSeries(seriesName, FixedMillisecond.class);
			data.setMaximumItemAge((int)(timeScale*1000*2));
			((TimeSeriesCollection)dataCollection).addSeries(data);
            
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
		String seriesName = getSeriesName(channelName);
		
		if (xyMode) {
      clearData();
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			//TODO add this functionality
		} else {      
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			TimeSeries data = dataCollection.getSeries(seriesName);
			dataCollection.removeSeries(data);

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
      
      if (showChannelsInTitle) {
        Iterator i = channels.iterator();
        titleBar.add(new ChannelTitle((String)i.next()));
        
        JLabel label = new JLabel("vs.");
        label.setBorder(new EmptyBorder(0, 0, 0, 5));
        label.setForeground(SimpleInternalFrame.getTextForeground(true));
        titleBar.add(label);
        
        titleBar.add(new ChannelTitle((String)i.next()));
      }
      
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
		
	public void timeScaleChanged(double newTimeScale) {
		super.timeScaleChanged(newTimeScale);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {    
    		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
    			if (xyMode) {
    				XYSeriesCollection xySeriesDataCollection = (XYSeriesCollection)dataCollection;
    				XYSeries data = xySeriesDataCollection.getSeries(i);
    				//TODO add correspoding code for XYSeries
    				data.setMaximumItemCount((int)(256*timeScale));
    			} else {
    				TimeSeriesCollection timeSeriesDataCollection = (TimeSeriesCollection)dataCollection;
    				TimeSeries data = timeSeriesDataCollection.getSeries(i);
    				data.setMaximumItemAge((int)(timeScale*1000));
    			}
    		}
      }
    });
		
		if (!xyMode) {
			setTimeAxis();
		}		
	}
	
	public void postData(final ChannelMap channelMap) {
    cachedChannelMap = this.channelMap;
    
		super.postData(channelMap);
		
		if (xyMode) {
			lastXYDataIndex = -1;
		} else {
			SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
			    postDataTimeSeries(channelMap);
			  }
			});
		}
	}

	private void postDataTimeSeries(ChannelMap channelMap) {
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postDataTimeSeries(channelMap, channelName, channelIndex);
			}
		}
	}
	
	private void postDataTimeSeries(ChannelMap channelMap, String channelName, int channelIndex) {
		TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
    FastTimeSeries timeSeriesData = (FastTimeSeries)dataCollection.getSeries(getSeriesName(channelName));
    if (timeSeriesData == null) {
      log.error("We don't have a data collection to post this data.");
      return;
    }
		
		try {		
			double[] times = channelMap.GetTimes(channelIndex);
			
			int typeID = channelMap.GetType(channelIndex);
			
			FixedMillisecond time;
			
			chart.setNotify(false);
      
      timeSeriesData.startAdd(times.length);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:					
					double[] doubleData = channelMap.GetDataAsFloat64(channelIndex);
					for (int i=0; i<doubleData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, doubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] floatData = channelMap.GetDataAsFloat32(channelIndex);
					for (int i=0; i<floatData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, floatData[i]);
					}
				break;					
				case ChannelMap.TYPE_INT64:
					long[] longData = channelMap.GetDataAsInt64(channelIndex);
					for (int i=0; i<longData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, longData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] intData = channelMap.GetDataAsInt32(channelIndex);
					for (int i=0; i<intData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, intData[i]);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] shortData = channelMap.GetDataAsInt16(channelIndex);
					for (int i=0; i<shortData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, shortData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT8:					
					byte[] byteData = channelMap.GetDataAsInt8(channelIndex);
					for (int i=0; i<byteData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, byteData[i]);
					}
					break;					
				case ChannelMap.TYPE_STRING:
				case ChannelMap.TYPE_UNKNOWN:
				case ChannelMap.TYPE_BYTEARRAY:
					log.error("Got byte array type for channel " + channelName + ". Don't know how to handle.");
					break;
			}
			
      timeSeriesData.stopAdd();
      
			chart.setNotify(true);
			chart.fireChartChanged();
		} catch (Exception e) {
			log.error("Problem plotting data for channel " + channelName + ".");
			e.printStackTrace();
		}
	}
	
	public void postTime(double time) {
		super.postTime(time);
		
		if (xyMode) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          postDataXY(channelMap, cachedChannelMap);
        }
      });
		} else {
			setTimeAxis();
		}		
	}

	private void postDataXY(ChannelMap channelMap, ChannelMap cachedChannelMap) {
		if (!xyMode) {
			log.error("Tried to post X vs. Y data when not in xy mode.");
			return;
		}
		
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
		//Check to see if we have 2 channels for x vs. y mode
		if (channels.size() != 2) {
			return;
		}

		Object[] channelsArray = channels.toArray();
		String xChannelName = (String)channelsArray[0];
		String yChannelName = (String)channelsArray[1];

		//get the channel indexes for the  x and y channels
		int xChannelIndex = channelMap.GetIndex(xChannelName);
		int yChannelIndex = channelMap.GetIndex(yChannelName);

    int firstXChannelIndex = -1;    
    
		//return if this channel map doesn't have data for the y channel
		if(yChannelIndex == -1) {
      return;
    } else if (xChannelIndex == -1) {
      //see if we cached data for the x channel
      firstXChannelIndex = (cachedChannelMap==null) ?
          -1 :
          cachedChannelMap.GetIndex(xChannelName);
      if (firstXChannelIndex == -1) {
        cachedChannelMap = null;
        return;
      }
		}
				
		try {
			//TODO make sure data is at the same timestamp
			double[] times = channelMap.GetTimes(yChannelIndex); //FIXME go over all channel times

			int startIndex = lastXYDataIndex + 1;
			int endIndex = startIndex;
			if (startIndex < times.length) {
				for (int i=times.length-1; i>startIndex; i--) {
					if (times[i] <= time) {
						endIndex = i;
						break;
					}
				}			
			} else {
				//no more data in channel map for us to display
				return;
			}

			lastXYDataIndex = endIndex;
			
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
      FastXYSeries xySeriesData = (FastXYSeries)dataCollection.getSeries(0);
			
			//FIXME assume data of same type
			int typeID = channelMap.GetType(yChannelIndex);
					
			chart.setNotify(false);
      
      xySeriesData.startAdd(times.length);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:
					double[] xDoubleData = firstXChannelIndex == -1?
					    channelMap.GetDataAsFloat64(xChannelIndex) :
              cachedChannelMap.GetDataAsFloat64(firstXChannelIndex);
					double[] yDoubleData = channelMap.GetDataAsFloat64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xDoubleData[i], yDoubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] xFloatData = firstXChannelIndex == -1?
              channelMap.GetDataAsFloat32(xChannelIndex) :
              cachedChannelMap.GetDataAsFloat32(firstXChannelIndex);
					float[] yFloatData = channelMap.GetDataAsFloat32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xFloatData[i], yFloatData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT64:
					long[] xLongData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt64(xChannelIndex) :
              cachedChannelMap.GetDataAsInt64(firstXChannelIndex);
					long[] yLongData = channelMap.GetDataAsInt64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xLongData[i], yLongData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] xIntData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt32(xChannelIndex) :
              cachedChannelMap.GetDataAsInt32(firstXChannelIndex);
					int[] yIntData = channelMap.GetDataAsInt32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xIntData[i], yIntData[i]);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] xShortData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt16(xChannelIndex) :
              cachedChannelMap.GetDataAsInt16(firstXChannelIndex);
					short[] yShortData = channelMap.GetDataAsInt16(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xShortData[i], yShortData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT8:
					byte[] xByteData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt8(xChannelIndex) :
              cachedChannelMap.GetDataAsInt8(firstXChannelIndex);
					byte[] yByteData = channelMap.GetDataAsInt8(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xByteData[i], yByteData[i]);
					}
					break;					
				case ChannelMap.TYPE_BYTEARRAY:					
				case ChannelMap.TYPE_STRING:
				case ChannelMap.TYPE_UNKNOWN:
					log.error("Don't know how to handle data type for " + xChannelName + " and " + yChannelName + ".");
					break;
			}
      
      xySeriesData.stopAdd();
			
			chart.setNotify(true);
			chart.fireChartChanged();
      
      //make sure cached channel map can be freeded
      cachedChannelMap = null;
		} catch (Exception e) {
			log.error("Problem plotting data for channels " + xChannelName + " and " + yChannelName + ".");
			e.printStackTrace();
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
		
		final XYDataset dataCollectionInvokeLater = this.dataCollection;
		SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				  for (int i=0; i<dataCollection.getSeriesCount(); i++) {
					  if (xyMode) {
						  XYSeriesCollection dataCollection = (XYSeriesCollection)dataCollectionInvokeLater;
						  XYSeries data = dataCollection.getSeries(i);
						  data.clear();
					  } else {
						  TimeSeriesCollection dataCollection = (TimeSeriesCollection)dataCollectionInvokeLater;
						  TimeSeries data = dataCollection.getSeries(i);
						  data.clear();				
					  }
				  }
			  }
		});		
		
		log.info("Cleared data display.");
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
   * This is an optimizied version of the TimeSeries class. It adds methods to
   * support fast loading of large amounts of data. 
   */
  class FastTimeSeries extends TimeSeries {
    public FastTimeSeries(String name, Class timePeriodClass) {
      super(name, timePeriodClass);
    }

    /**
     * Signal that a number of items will be added to the series.
     * 
     * This increases the capacity of this series to ensure it hold at least the
     * number of elements specified plus the current number of elements.
     * 
     * @param items  the number of items to be added
     */
    public void startAdd(int items) {
      ((ArrayList)data).ensureCapacity(data.size()+items);
    }
    
    /**
     * Adds a data item to the series. If the time period is less than the last
     * time period, the item will not be added.
     *
     * @param period  the time period to add (<code>null</code> not permitted).
     * @param value  the new value.
     */
    public void add(RegularTimePeriod period, double value) {
      TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
      int count = getItemCount();
      if (count == 0) {
        data.add(item);
      } else {
        RegularTimePeriod last = getTimePeriod(count-1);
        if (period.compareTo(last) > 0) {
          data.add(item);
        }
      }      
    }
    
    /**
     * Signal that the adding of items has ended.
     * 
     * This fires a series changed event.
     */
    public void stopAdd() {
      removeAgedItems(false);

      fireSeriesChanged();
    }
  }
  
  /**
   * An optimized version of XYSeries. 
   */
  class FastXYSeries extends XYSeries {
    public FastXYSeries(Comparable key) {
      super(key, false, true);
    }
    
    /**
     * Signal that a number of items will be added to the series.
     * 
     * This increases the capacity of this series to ensure it hold at least the
     * number of elements specified plus the current number of elements.
     * 
     * @param items  the number of items to be added
     */
    public void startAdd(int items) {
      ((ArrayList)data).ensureCapacity(data.size()+items);
    }

    /**
     * Adds a data item to the series.
     *
     * @param x  the x value.
     * @param y  the y value.
     */    
    public void add(double x, double y) {
      data.add(new XYDataItem(x, y));
        
      if (getItemCount() > getMaximumItemCount()) {
        data.remove(0);
      }
    }    
    
    /**
     * Signal that the adding of items has ended.
     * 
     * This fires a series changed event.
     */
    public void stopAdd() {
      fireSeriesChanged();
    }    
  }
}