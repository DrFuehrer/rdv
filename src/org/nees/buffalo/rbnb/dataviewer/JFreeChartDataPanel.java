package org.nees.buffalo.rbnb.dataviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
public class JFreeChartDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {
	
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());

	Vector channels;
	
	JFreeChart chart;
	ChartPanel chartPanel;
	XYDataset dataCollection;
	
	ChartPanelPanel panel;
	
	JFrame frame;
	boolean attached;
	
	double domain;
	
	DataPanelContainer dataPanelContainer;
	Player player;
	Number xValue, yValue;
	
	boolean xyMode;

	public JFreeChartDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this(dataPanelContainer, player, false);
	}
		
	public JFreeChartDataPanel(DataPanelContainer dataPanelContainer, Player player, boolean xyMode) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
		this.xyMode = xyMode;
		
		channels = new Vector();
		attached = true;
		domain = 1;

		initChart();
		
		player.addStateListener(this);
		player.addTimeListener(this);
			
		new DropTarget(chartPanel, DnDConstants.ACTION_LINK, this);
	}
		
	private void initChart() {
		if (xyMode) {
			dataCollection = new XYSeriesCollection();
			chart = ChartFactory.createXYLineChart(null, null, null, dataCollection, PlotOrientation.VERTICAL, false, false, false);
		} else {
			dataCollection = new TimeSeriesCollection();
			chart = ChartFactory.createTimeSeriesChart(null, "Time", null, dataCollection, true, false, false);
		}
		
		chart.setAntiAlias(false);
		chartPanel = new ChartPanel(chart, true);
		chartPanel.setPreferredSize(new Dimension(356,244));
		
		panel = new ChartPanelPanel(chartPanel);
		panel.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getX() >= panel.getWidth()-10 && e.getY() <= 12) {
						closePanel();
					} else {
						toggleDetach();
					}
				}
			}
		});

	}

	public JComponent getComponent() {
		return panel;
	}
	
	public void closePanel() {
		removeAllChannels();
		
		if (attached) {
			dataPanelContainer.removeDataPanel(this);
		} else  if (frame != null) {
			frame.setVisible(false);
			frame.getContentPane().remove(panel);
			frame.dispose();
			frame = null;			
		}
		
		player.removeStateListener(this);
		player.removeTimeListener(this);
	}
	
	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public void setChannel(String channelName) {
		removeAllChannels();
		addChannel(channelName);
	}

	public void addChannel(String channelName) {
		if (channels.contains(channelName)) return;
		
		log.debug("Adding channel: " + channelName + ".");

		if (xyMode) {
			if (channels.size() == 0) {
				XYSeries data = new XYSeries(channelName);
				((XYSeriesCollection)dataCollection).addSeries(data);
			} else if (channels.size() == 1) {
				
			} else {
				return;
			}
		} else {
			TimeSeries data = new TimeSeries(channelName, FixedMillisecond.class);
			data.setHistoryCount((int)(domain*1000));
			((TimeSeriesCollection)dataCollection).addSeries(data);
		}
		
		player.subscribe(channelName, this);
		
		channels.add(channelName);
		setTitle();
		
	}
	
	public void removeChannel(String channelName) {
		log.debug("Removing channel: " + channelName + ".");
		
		channels.remove(channelName);
		setTitle();
		if (xyMode) {
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			//TODO add this functionality
		} else {
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			TimeSeries data = dataCollection.getSeries(channelName);
			dataCollection.removeSeries(data);
			player.unsubscribe(channelName, this);
		}
	}
	
	private void removeAllChannels() {
		player.unsubscribeAll(this);
		if (xyMode) {
			XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
			dataCollection.removeAllSeries();
		} else {
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			dataCollection.removeAllSeries();
		}
		channels.clear();
	}
	
	private void setTitle() {	
		String title = "";
		
		if (xyMode && channels.size() == 2) {
			((XYPlot)chart.getPlot()).getDomainAxis().setLabel((String)channels.get(0));
			((XYPlot)chart.getPlot()).getRangeAxis().setLabel((String)channels.get(1));
			title = channels.get(0) + " vs " + channels.get(1);
		} else {
			for(int i=0; i < channels.size(); i++) {
				title += channels.get(i) + (i==channels.size()-1?"" : ", ");
			}			
		}
		
		if (!attached) {
			frame.setTitle(title);
		}

	}
		
	public void setDomain(double domain) {
		this.domain = domain;
			
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			if (xyMode) {
				XYSeriesCollection dataCollection = (XYSeriesCollection)this.dataCollection;
				XYSeries data = dataCollection.getSeries(i);
				// TODO add correspoding code for XYSeries
			} else {
				TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
				TimeSeries data = dataCollection.getSeries(i);
				data.setHistoryCount((int)(domain*1000));
			}
			
		}
	}
	
	public void postTime(double time) {}

	public void postState(int newState, int oldState) {
		switch (newState) {
			case Player.STATE_LOADING:
			case Player.STATE_MONITORING:
				clearData();
				break;
		}
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
		for (int i=0; i<channels.size(); i++) {
			String channelName = (String)channels.get(i);
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
		timeSeriesData = dataCollection.getSeries(channelName);
		
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

		String xChannelName = (String)channels.get(0);
		String yChannelName = (String)channels.get(1);

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
	
	class TimeIndex {
		public int startIndex;
		public int endIndex;
		
		public TimeIndex(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}
	
	public TimeIndex getTimeIndex(double[] times, double startTime, double duration) {
		int startIndex = -1;
		int endIndex = -1;

		if (startTime != -1 && duration != -1) {
			for (int i=0; i<times.length; i++) {
				if (times[i] >= startTime) {
					startIndex = i;
					break;
				}
			}
			
			if (startIndex != -1) {
				double endTime = startTime + duration;
				for (int i=startIndex; i<times.length; i++) {
					if (times[i] < endTime) {
						endIndex = i;
					} else {
						break;
					}
				}
			}
		} else {
			startIndex = 0;
			endIndex = times.length-1;
		}
		
		return new TimeIndex(startIndex, endIndex);
	}
	
	private void clearData() {
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
	
	public void toggleDetach() {
		if (attached) {
			detachPanel();
		} else {
			attachPanel();
		}
	}
	
	public void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(this);
		
		frame = new JFrame();
		setTitle();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attachPanel();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void attachPanel() {
		frame.setVisible(false);
		frame.getContentPane().remove(panel);
		frame.dispose();
		frame = null;			

		dataPanelContainer.addDataPanel(this);
		attached = true;	
	}
	

	public void dragEnter(DropTargetDragEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}
	
	public void dropActionChanged(DropTargetDragEvent e) {}
	
	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();
			if(e.isDataFlavorSupported(stringFlavor)) {
				String channelName = (String)tr.getTransferData(stringFlavor);
				e.acceptDrop(DnDConstants.ACTION_LINK);
				e.dropComplete(true);
				
				try {
					if (supportsMultipleChannels()) {
						addChannel(channelName);
					} else {
						setChannel(channelName);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				e.rejectDrop();
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}	
	}
	
	public void dragExit(DropTargetEvent e) {}

	public String[] getSupportedMimeTypes() {
		return new String[] {"application/octet-stream"};
	}
	
	class ChartPanelPanel extends JPanel {
		ChartPanel chartPanel;
		
		public ChartPanelPanel(ChartPanel chartPanel) {
			super();
			
			this.chartPanel = chartPanel;
			
			setBorder(new EtchedBorder());
			
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.ipadx = 0;
			c.ipady = 0;
			c.insets = new java.awt.Insets(0,0,0,0);
			c.anchor = GridBagConstraints.CENTER;		
			add(chartPanel, c);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawString("X", getWidth()-10, 12);
		}
		
		public void addMouseListener(MouseListener listener) {
			super.addMouseListener(listener);
			chartPanel.addMouseListener(listener);
		}
			
		public void setBackground(Color c) {
			super.setBackground(c);
			if (chartPanel != null) {
				chartPanel.setBackground(c);
			}
		}

	}
}
