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
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JFreeChartDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {
	
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());

	Vector channels;
	
	JFreeChart chart;
	ChartPanel chartPanel;
	TimeSeriesCollection dataCollection;
	
	ChartPanelPanel panel;
	
	JFrame frame;
	boolean attached;
	
	double domain;
	
	DataPanelContainer dataPanelContainer;
	Player player;

	public JFreeChartDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
		
		channels = new Vector();
		attached = true;
		domain = 1;

		initChart();
		
		player.addStateListener(this);
		player.addTimeListener(this);
			
		new DropTarget(chartPanel, DnDConstants.ACTION_LINK, this);
	}
		
	private void initChart() {
		dataCollection = new TimeSeriesCollection();		
		chart = ChartFactory.createTimeSeriesChart("Empty", "t", "x", dataCollection, false, false, false);
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
		
		channels.add(channelName);
		setTitle();
		
		TimeSeries data = new TimeSeries(channelName, FixedMillisecond.class);
		data.setHistoryCount((int)(domain*1000));
		dataCollection.addSeries(data);
		
		player.subscribe(channelName, this);
	}
	
	public void removeChannel(String channelName) {
		log.debug("Removing channel: " + channelName + ".");
		
		channels.remove(channelName);
		setTitle();
		
		TimeSeries data = dataCollection.getSeries(channelName);
		dataCollection.removeSeries(data);
		player.unsubscribe(channelName, this);
	}
	
	private void removeAllChannels() {
		player.unsubscribeAll(this);
		dataCollection.removeAllSeries();
		channels.clear();
	}
	
	private void setTitle() {
		String title = "";
		for(int i=0; i < channels.size(); i++) {
			title += channels.get(i) + (i==channels.size()-1?"" : ", ");
		}
		
		chart.setTitle(title);
		
		if (!attached) {
			frame.setTitle(title);
		}

	}
		
	public void setDomain(double domain) {
		this.domain = domain;
		TimeSeries data;	
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			data = dataCollection.getSeries(i);
			data.setHistoryCount((int)(domain*1000));
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
		
	public void postData(ChannelMap channelMap, int channelIndex, String channelName) {
		postData(channelMap, channelIndex, channelName, -1, -1);
	}	

	public void postData(ChannelMap channelMap, int channelIndex, String channelName, double startTime, double duration) {
		if (channelName == null) {
			channelName = (String)channels.get(channelIndex);
		}
			
		TimeSeries data = dataCollection.getSeries(channelName);
		
		try {		
			double[] times = channelMap.GetTimes(channelIndex);
			int startIndex = -1;
			int endIndex = -1;

			if (startTime != -1 && duration != -1) {
				for (int i=0; i<times.length; i++) {
					if (times[i] >= startTime) {
						startIndex = i;
						break;
					}
				}
				
				if (startIndex == -1) {
					return;
				}
				
				double endTime = startTime + duration;
				for (int i=startIndex; i<times.length; i++) {
					if (times[i] < endTime) {
						endIndex = i;
					} else {
						break;
					}
				}
				
				if (endIndex == -1) {
					return;
				}
			} else {
				startIndex = 0;
				endIndex = times.length-1;
			}
			
			int typeID = channelMap.GetType(channelIndex);
			
			FixedMillisecond time;
			
			chart.setNotify(false);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:
					double[] doubleData = channelMap.GetDataAsFloat64(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, doubleData[i]);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] floatData = channelMap.GetDataAsFloat32(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, floatData[i]);
					}
					break;
				case ChannelMap.TYPE_INT64:
					long[] longData = channelMap.GetDataAsInt64(channelIndex);
				for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, longData[i]);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] intData = channelMap.GetDataAsInt32(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, intData[i]);
					}
					break;				
				case ChannelMap.TYPE_INT16:
					short[] shortData = channelMap.GetDataAsInt16(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, shortData[i]);
					}
					break;
				case ChannelMap.TYPE_INT8:
					byte[] byteData = channelMap.GetDataAsInt8(channelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
						time = new FixedMillisecond((long)(times[i]*1000));
						data.add(time, byteData[i]);
					}
					break;
				case ChannelMap.TYPE_STRING:
					log.error("Got string type of data for channel " + channelName + ". Don't know how to handle.");
					break;
				case ChannelMap.TYPE_UNKNOWN:
					log.error("Got unknown type of data for channel " + channelName + ".");
					break;
				case ChannelMap.TYPE_BYTEARRAY:
					log.error("Got byte array type for channel " + channelName + ". Don't know how to handle.");
					break;
			}
			
			chart.setNotify(true);
			chart.fireChartChanged();
			
		//} catch (SeriesException se) {
		//	log.error("Received duplicate data: " + se.getMessage() + ".");
		} catch (Exception e) {
			log.error("Problem plotting data for channel: " + channelName + ": " + e.getMessage() + ".");
		}

	}

	private void clearData() {
		TimeSeries data;	
		for (int i=0; i<dataCollection.getSeriesCount(); i++) {
			data = dataCollection.getSeries(i);
			data.clear();
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
