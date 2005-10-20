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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.VerticalAlignment;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JFreeChartDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());
	
	JFreeChart chart;
	ChartPanel chartPanel;
	XYDataset dataCollection;
	
	JPanel chartPanelPanel;
	
	final boolean xyMode;
	
	int lastXYDataIndex;
    
    static Title emptyTitle = new TextTitle(" ");

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
			chart = ChartFactory.createXYLineChart(null, null, null, dataCollection, PlotOrientation.VERTICAL, false, false, false);
			((NumberAxis)((XYPlot)chart.getPlot()).getDomainAxis()).setAutoRangeIncludesZero(true);
			((NumberAxis)((XYPlot)chart.getPlot()).getRangeAxis()).setAutoRangeIncludesZero(true);

		} else {
			dataCollection = new TimeSeriesCollection();
			chart = ChartFactory.createTimeSeriesChart(null, "Time", null, dataCollection, true, false, false);
			((NumberAxis)((XYPlot)chart.getPlot()).getRangeAxis()).setAutoRangeIncludesZero(true);
		}
		
		chart.setAntiAlias(false);
		chartPanel = new ChartPanel(chart, true);
        chartPanel.addMouseListener(new MouseAdapter() {
          public void mouseEntered(MouseEvent e) {
            chartPanel.setHorizontalAxisTrace(true);
            chartPanel.setVerticalAxisTrace(true);
          }
          public void mouseExited(MouseEvent e) {
            chartPanel.setHorizontalAxisTrace(false);
            chartPanel.setVerticalAxisTrace(false);
            chartPanel.repaint();
          }
        });

        /* ArrayList subTitles = new ArrayList(1);
        subTitles.add(emptyTitle);
        chart.setSubtitles(subTitles);

        chartPanel.addChartMouseListener(new ChartMouseListener() {
          public void chartMouseClicked(ChartMouseEvent e) {}
          public void chartMouseMoved(ChartMouseEvent e) {
            Title subTitle;
            if (e.getEntity() != null) {
              XYItemEntity xyEntity = (XYItemEntity)e.getEntity();
              double y = xyEntity.getDataset().getYValue(xyEntity.getSeriesIndex(), xyEntity.getItem());              
              if (xyMode) {
                double x = xyEntity.getDataset().getXValue(xyEntity.getSeriesIndex(), xyEntity.getItem());  
                subTitle = new TextTitle(x + ", " + y);
              } else {
                String channelName = (String)((TimeSeriesCollection)dataCollection).getSeries(xyEntity.getSeriesIndex()).getKey();
                subTitle = new TextTitle(channelName + ": " + y);
              }
            } else {
              subTitle = emptyTitle;
            }
            ArrayList subTitles = new ArrayList(1);
            subTitles.add(subTitle);
            chart.setSubtitles(subTitles);
          }         
        }); */

		chartPanelPanel = new JPanel();
		chartPanelPanel.setLayout(new BorderLayout());
		chartPanelPanel.add(chartPanel, BorderLayout.CENTER);
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
			TimeSeries data = new TimeSeries(seriesName, FixedMillisecond.class);
			data.setHistoryCount((int)(timeScale*1000*2));
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
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			//TODO add this functionality
		} else {
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			TimeSeries data = dataCollection.getSeries(seriesName);
			dataCollection.removeSeries(data);
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
  
  JComponent getTitleComponent() {
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
      return super.getTitleComponent();
    }
  }  
	
	private void setAxisName() {	
		if (xyMode) {
			Object[] channelsArray = channels.toArray();
			
			if (channels.size() == 1) {
				String channelName = (String)channelsArray[0];
				String seriesName = getSeriesName(channelName);
				((XYPlot)chart.getPlot()).getDomainAxis().setLabel(seriesName);
			} else if (channels.size() == 2) {
				String channelName = (String)channelsArray[1];
				String seriesName = getSeriesName(channelName);
				((XYPlot)chart.getPlot()).getRangeAxis().setLabel(seriesName);
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
				data.setHistoryCount((int)(timeScale*1000*2));
			}
		}
		
		if (!xyMode) {
			setTimeAxis();
		}		
	}
	
	public void postData(ChannelMap channelMap) {
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

		//return if this channel map doesn't have data for both the x and y channel
		if(xChannelIndex == -1 || yChannelIndex == -1) {
			return;
		}
				
		try {
			//TODO make sure data is at the same timestamp
			double[] times = channelMap.GetTimes(xChannelIndex); //FIXME go over all channel times

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
			int typeID = channelMap.GetType(xChannelIndex);
					
			chart.setNotify(false);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:
					double[] xDoubleData = channelMap.GetDataAsFloat64(xChannelIndex);
					double[] yDoubleData = channelMap.GetDataAsFloat64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xDoubleData[i], yDoubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] xFloatData = channelMap.GetDataAsFloat32(xChannelIndex);
					float[] yFloatData = channelMap.GetDataAsFloat32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xFloatData[i], yFloatData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT64:
					long[] xLongData = channelMap.GetDataAsInt64(xChannelIndex);
					long[] yLongData = channelMap.GetDataAsInt64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xLongData[i], yLongData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] xIntData = channelMap.GetDataAsInt32(xChannelIndex);
					int[] yIntData = channelMap.GetDataAsInt32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xIntData[i], yIntData[i]);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] xShortData = channelMap.GetDataAsInt16(xChannelIndex);
					short[] yShortData = channelMap.GetDataAsInt16(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						xySeriesData.add(xShortData[i], yShortData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT8:
					byte[] xByteData = channelMap.GetDataAsInt8(xChannelIndex);
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
		
		XYPlot xyPlot = (XYPlot)chart.getPlot();
		DateAxis dateAxis = (DateAxis)xyPlot.getDomainAxis();
		dateAxis.setRange((time-timeScale)*1000, time*1000);
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
	
	public String toString() {
		return "JFreeChart Data Panel";
	}
}