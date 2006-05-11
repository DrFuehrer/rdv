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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

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
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JFreeChartDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());
	
	JFreeChart chart;
  ValueAxis domainAxis;
  NumberAxis rangeAxis;
	ChartPanel chartPanel;
	XYDataset dataCollection;
  LegendTitle rangeLegend;
	
	JPanel chartPanelPanel;
	
	final boolean xyMode;
	
	int lastXYDataIndex;
  
  ChannelMap cachedChannelMap;
    
	public JFreeChartDataPanel() {
		this(false);
	}
		
	public JFreeChartDataPanel(boolean xyMode) {
		super();
		
		this.xyMode = xyMode;
		
		lastXYDataIndex = -1;
		
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
      StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES,
          toolTipGenerator);
      renderer.setDefaultEntityRadius(6);      
      
      XYPlot xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
      
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
      StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES,
          toolTipGenerator);
      renderer.setDefaultEntityRadius(6);

      XYPlot xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
      
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
		
		if (!super.addChannel(channelName)) {
			return false;
		}
		
		String seriesName = getSeriesName(channelName);
		
		log.info("Adding channel: " + seriesName + ".");

		if (xyMode) {
			if (channels.size() == 1) {
				XYSeries data = new XYSeries(seriesName, false, true);
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
      
			TimeSeries data = new TimeSeries(seriesName, FixedMillisecond.class);
			data.setMaximumItemAge((int)(timeScale*1000*2));
			((TimeSeriesCollection)dataCollection).addSeries(data);
		}
		
		setAxisName();
		
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		String seriesName = getSeriesName(channelName);
		
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
		log.info("Removing channel: " + channelName + ".");
		
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
		}
		
		return true;
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
		String unit = (String)units.get(channelName);
		if (unit != null) {
			seriesName += " (" + unit + ")";
		}
		return seriesName;
	}
		
	public void timeScaleChanged(double timeScale) {
		super.timeScaleChanged(timeScale);
			
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			if (xyMode) {
				XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
				XYSeries data = dataCollection.getSeries(i);
				//TODO add correspoding code for XYSeries
				data.setMaximumItemCount((int)(256*timeScale*2));
			} else {
				TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
				TimeSeries data = dataCollection.getSeries(i);
				data.setMaximumItemAge((int)(timeScale*1000*2));
			}
		}
		
		if (!xyMode) {
			setTimeAxis();
		}		
	}
	
	public void postData(ChannelMap channelMap) {
    cachedChannelMap = this.channelMap;
    
		super.postData(channelMap);
		
		if (xyMode) {
			lastXYDataIndex = -1;
		} else {
			postDataTimeSeries();
		}
	}

	private void postDataTimeSeries() {
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postDataTimeSeries(channelName, channelIndex);
			}
		}
	}
	
	private void postDataTimeSeries(String channelName, int channelIndex) {		
		TimeSeries timeSeriesData = null;
		TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
		timeSeriesData = dataCollection.getSeries(getSeriesName(channelName));
    if (timeSeriesData == null) {
      //FIXME why does this happen?
      log.error("We don't have a data collection to post this data.");
      return;
    }
		
		try {		
			double[] times = channelMap.GetTimes(channelIndex);
			
			int typeID = channelMap.GetType(channelIndex);
			
			FixedMillisecond time;
			
			chart.setNotify(false);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:					
					double[] doubleData = channelMap.GetDataAsFloat64(channelIndex);
					for (int i=0; i<doubleData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, doubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] floatData = channelMap.GetDataAsFloat32(channelIndex);
					for (int i=0; i<floatData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, floatData[i]);
					}
				break;					
				case ChannelMap.TYPE_INT64:
					long[] longData = channelMap.GetDataAsInt64(channelIndex);
					for (int i=0; i<longData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, longData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] intData = channelMap.GetDataAsInt32(channelIndex);
					for (int i=0; i<intData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, intData[i]);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] shortData = channelMap.GetDataAsInt16(channelIndex);
					for (int i=0; i<shortData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, shortData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT8:					
					byte[] byteData = channelMap.GetDataAsInt8(channelIndex);
					for (int i=0; i<byteData.length; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.addOrUpdate(time, byteData[i]);
					}
					break;					
				case ChannelMap.TYPE_STRING:
				case ChannelMap.TYPE_UNKNOWN:
				case ChannelMap.TYPE_BYTEARRAY:
					log.error("Got byte array type for channel " + channelName + ". Don't know how to handle.");
					break;
			}
			
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
			postDataXY();
		} else {
			setTimeAxis();
		}		
	}

	private void postDataXY() {
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
			
			XYSeries xySeriesData = null;
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			xySeriesData = dataCollection.getSeries(0);
			
			//FIXME assume data of same type
			int typeID = channelMap.GetType(yChannelIndex);
					
			chart.setNotify(false);
			
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
		
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			if (xyMode) {
				XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
				XYSeries data = dataCollection.getSeries(i);
				data.clear();
			} else {
				TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
				TimeSeries data = dataCollection.getSeries(i);
				data.clear();				
			}
		}
		
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
}