package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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

	public JFreeChartDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this(dataPanelContainer, player, false);
	}
		
	public JFreeChartDataPanel(DataPanelContainer dataPanelContainer, Player player, boolean xyMode) {
		super(dataPanelContainer, player);
		
		this.xyMode = xyMode;
		
		initChart();
		
		setDataComponent(chartPanelPanel);
		setControlBar(true);
		setDropTarget(true);
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
				
		chartPanelPanel = new JPanel();
		chartPanelPanel.setLayout(new BorderLayout());
		chartPanelPanel.add(chartPanel, BorderLayout.CENTER);
	}
		
	public String[] getSupportedMimeTypes() {
		return new String[] {"application/octet-stream"};
	}
	
	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public void addChannel(String channelName, String unit) {
		super.addChannel(channelName, unit);

		String seriesName = getSeriesName(channelName);
		
		log.debug("Adding channel: " + seriesName + ".");

		if (xyMode) {
			if (channels.size() == 1) {
				XYSeries data = new XYSeries(seriesName, false, true);
				((XYSeriesCollection)dataCollection).addSeries(data);
			} else if (channels.size() > 2) {
				return;
			}
		} else {
			TimeSeries data = new TimeSeries(seriesName, FixedMillisecond.class);
			data.setHistoryCount((int)(domain*1000));
			((TimeSeriesCollection)dataCollection).addSeries(data);
		}
		
		setAxisName();
	}
	
	public void removeChannel(String channelName) {
		String seriesName = getSeriesName(channelName);
		
		super.removeChannel(channelName);
		
		log.debug("Removing channel: " + channelName + ".");
		
		if (xyMode) {
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			//TODO add this functionality
		} else {
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			TimeSeries data = dataCollection.getSeries(seriesName);
			dataCollection.removeSeries(data);
		}
	}
	
	/* private void removeAllChannels() {
		player.unsubscribeAll(this);
		if (xyMode) {
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			dataCollection.removeAllSeries();
		} else {
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			dataCollection.removeAllSeries();
		}
		channels.clear();
		units.clear();
	} */
	
	String getTitle() {
		if (xyMode && channels.size() == 2) {
			Object[] channelsArray = channels.toArray();
			return channelsArray[0] + " vs. " + channelsArray[1];
		} else {
			return super.getTitle();
		}
	}
	
	private void setAxisName() {	
		if (xyMode) {
			Object[] channelsArray = channels.toArray();
			
			if (channels.size() == 1) {
				String channelName = (String)channelsArray[0];
				String seriesName = getSeriesName(channelName);
				((XYPlot)chart.getPlot()).getDomainAxis().setLabel(seriesName);
				//title = (String) channels.get(0);
			} else if (channels.size() == 2) {
				String channelName = (String)channelsArray[1];
				String seriesName = getSeriesName(channelName);
				((XYPlot)chart.getPlot()).getRangeAxis().setLabel(seriesName);
				//title = channels.get(0) + " vs. " + channels.get(1);
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
		
	public void setDomain(double domain) {
		super.setDomain(domain);
			
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			if (xyMode) {
				XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
				XYSeries data = dataCollection.getSeries(i);
				//TODO add correspoding code for XYSeries
				data.setMaximumItemCount((int)(256*domain*2));
			} else {
				TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
				TimeSeries data = dataCollection.getSeries(i);
				data.setHistoryCount((int)(domain*1000*2));
			}
		}
		
		if (!xyMode) {
			setTimeAxis();
		}		
	}
	
	public void postTime(double time) {
		super.postTime(time);
		
		if (!xyMode && chart != null) {
			setTimeAxis();
		}		
	}
	
	private void setTimeAxis() {
		((DateAxis)((XYPlot)chart.getPlot()).getDomainAxis()).setRange((time-domain)*1000, time*1000);
	}
		
	public void postData(ChannelMap channelMap) {
		postData(channelMap, -1, -1);
	}	

	public void postData(ChannelMap channelMap, double startTime, double duration) {
		if (xyMode) {
			postDataXY(channelMap, startTime, duration);
		} else {
			postDataTimeSeries(channelMap, startTime, duration);
		}
	}

	private void postDataTimeSeries(ChannelMap channelMap, double startTime, double duration) {
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postDataTimeSeries(channelMap, channelName, channelIndex, startTime, duration);
			}
		}
	}
	
	private void postDataTimeSeries(ChannelMap channelMap, String channelName, int channelIndex, double startTime, double duration) {		
		TimeSeries timeSeriesData = null;
		TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
		timeSeriesData = dataCollection.getSeries(getSeriesName(channelName));
		
		try {		
			double[] times = channelMap.GetTimes(channelIndex);

			TimeIndex index = getTimeIndex(times, startTime, duration);
			int startIndex = index.startIndex;
			int endIndex = index.endIndex;
			
			//see if there is no data in the time range we are loooking at
			if (startIndex == -1 || endIndex == -1) {
				return;
			}
			
			int typeID = channelMap.GetType(channelIndex);
			
			FixedMillisecond time;
			
			chart.setNotify(false);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:					
					double[] doubleData = channelMap.GetDataAsFloat64(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, doubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] floatData = channelMap.GetDataAsFloat32(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, floatData[i]);
					}
				break;					
				case ChannelMap.TYPE_INT64:
					long[] longData = channelMap.GetDataAsInt64(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, longData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] intData = channelMap.GetDataAsInt32(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, intData[i]);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] shortData = channelMap.GetDataAsInt16(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						timeSeriesData.add(time, shortData[i]);
					}
					break;					
				case ChannelMap.TYPE_INT8:					
					byte[] byteData = channelMap.GetDataAsInt8(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
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
			
			chart.setNotify(true);
			chart.fireChartChanged();
			
		} catch (Exception e) {
			log.error("Problem plotting data for channel " + channelName + ".");
			e.printStackTrace();
		}
	}

	private void postDataXY(ChannelMap channelMap, double startTime, double duration) {
		if (!xyMode) {
			log.error("Tried to post X vs. Y data when not in xy mode.");
			return;
		}
		
		//Check to see if we have 2 channels for x vs. y mode
		if (channels.size() != 2) {
			return;
		}

		Object[] channelsArray = channels.toArray();
		String xChannelName = (String)channelsArray[0];
		String yChannelName = (String)channelsArray[1];

		int xChannelIndex = -1;
		int yChannelIndex = -1;
		
		//get the channel indexs for the  x and y channels
		String[] dataChannels = channelMap.GetChannelList();
		for (int i=0; i<dataChannels.length; i++) {
			if (xChannelName.equals(dataChannels[i])) {
				xChannelIndex = channelMap.GetIndex(dataChannels[i]);
			} else if(yChannelName.equals(dataChannels[i])) {
				yChannelIndex = channelMap.GetIndex(dataChannels[i]);
			}
		}
		
		//return if this channel map doesn't have data for both the x and y channel
		if(xChannelIndex == -1 || yChannelIndex == -1) {
			return;
		}
				
		try {
			//TODO make sure data is at the same timestamp
			double[] times = channelMap.GetTimes(xChannelIndex); //FIXME go over all channel times

			TimeIndex index = getTimeIndex(times, startTime, duration);
			int startIndex = index.startIndex;
			int endIndex = index.endIndex;
			
			//see if there is no data in the time range we are loooking at
			if (startIndex == -1 || endIndex == -1) {
				return;
			}

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
			log.error("Problem plotting data for channels " + xChannelName + " and " + yChannelName + ": " + e.getMessage() + ".");
		}

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
		
		log.debug("Cleared data display.");
	}
}