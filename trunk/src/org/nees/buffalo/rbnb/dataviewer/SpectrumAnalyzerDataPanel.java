/*
 * Created on Mar 24, 2005
 */
package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import jnt.FFT.RealDoubleFFT;
import jnt.FFT.RealDoubleFFT_Radix2;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class SpectrumAnalyzerDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(SpectrumAnalyzerDataPanel.class.getName());

	double sampleRate;
	int numberOfSamples;
	
	ArrayList inputData;
	
	double[] hanningWindow;
	boolean useHanningWindow;

	double lastTimeDisplayed;
	
	JPanel panel;
	
	JFreeChart chart;
	ChartPanel chartPanel;
	XYSeriesCollection xySeriesCollection;
	XYSeries xySeries;
	
	JTextField sampleRateTextField;
	JTextField dataPointsTextField;
	JCheckBox useHanningWindowCheckBox;
	
	public SpectrumAnalyzerDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
		
		sampleRate = 256;
		numberOfSamples = 512;
		
		inputData = new ArrayList(numberOfSamples);
		
		setHanningWindow();
		useHanningWindow = true;
		
		lastTimeDisplayed = -1;
		
		initComponents();
		
		setDataComponent(panel);
		setControlBar(true);
		setDropTarget(true);
	}
	
	private void initComponents() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		xySeries = new XYSeries("", false, true);
		xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(xySeries);
		chart = ChartFactory.createXYLineChart(null, "Frequency (Hz)", null, xySeriesCollection, PlotOrientation.VERTICAL, false, true, false);
		XYPlot xyPlot = (XYPlot)chart.getPlot();
		NumberAxis xAxis = (NumberAxis)xyPlot.getDomainAxis();
		xAxis.setRange(0, sampleRate/2);

		chart.setAntiAlias(false);
		chartPanel = new ChartPanel(chart, true);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(new JLabel("Sample rate: "));
		sampleRateTextField = new JTextField(Double.toString(sampleRate));
		sampleRateTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sampleRate = Double.parseDouble(sampleRateTextField.getText());
				} catch (NumberFormatException nfe) {
					sampleRateTextField.setText(Double.toString(sampleRate));
				}
				XYPlot xyPlot = (XYPlot)chart.getPlot();
				NumberAxis xAxis = (NumberAxis)xyPlot.getDomainAxis();
				xAxis.setRange(0, sampleRate/2);
				clearData();
			}
		});
		controlPanel.add(sampleRateTextField);
		
		controlPanel.add(new JLabel("Data Points: "));
		dataPointsTextField = new JTextField(Integer.toString(numberOfSamples));
		dataPointsTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					numberOfSamples = Integer.parseInt(dataPointsTextField.getText());
				} catch (NumberFormatException nfe) {
					dataPointsTextField.setText(Double.toString(numberOfSamples));
				}
				clearData();
				if (inputData.size() < numberOfSamples) {
					inputData.ensureCapacity(numberOfSamples);
				}
				setHanningWindow();
			}
		});
		controlPanel.add(dataPointsTextField);
		
		useHanningWindowCheckBox = new JCheckBox("Use hanning window", true);
		useHanningWindowCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useHanningWindow = !useHanningWindow;
			}
		});
		controlPanel.add(useHanningWindowCheckBox);
				
		panel.add(chartPanel, BorderLayout.CENTER);
		panel.add(controlPanel, BorderLayout.SOUTH);
		
		
	}

	public String[] getSupportedMimeTypes() {
		return new String[] {"application/octet-stream"};
	}

	public boolean supportsMultipleChannels() {
		return false;
	}
	
	public boolean addChannel(Channel channel) {
		if (!super.addChannel(channel)) {
			return false;
		}
		
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}

		return true;
	}
	
	public void postData(ChannelMap channelMap) {
		super.postData(channelMap);
	}

	public void postTime(double time) {
		super.postTime(time);

		if (channelMap == null) {
			//no data to display yet
			return;
		}		

		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				postTime(channelName, channelIndex);
			}
		}
	}

	private void postTime(String channelName, int channelIndex) {
		double[] data = channelMap.GetDataAsFloat64(channelIndex);
		double[] times = channelMap.GetTimes(channelIndex);

		int startIndex = -1;
		
		for (int i=0; i<times.length; i++) {
			if (times[i] > lastTimeDisplayed && times[i] <= time) {
				startIndex = i;
				break;
			}
		}
		
		//see if there is no data in the time range we are loooking at
		if (startIndex == -1) {
			return;
		}		

		int endIndex = startIndex;
		
		for (int i=times.length-1; i>startIndex; i--) {
			if (times[i] <= time) {
				endIndex = i;
				break;
			}
		}
				
		for (int i=startIndex; i<=endIndex; i++) {
			if (inputData.size() == numberOfSamples) {
				inputData.remove(0);
			}
			inputData.add(new Double(data[i]));
		}
		
		if (inputData.size() == numberOfSamples) {
			RealDoubleFFT fft = new RealDoubleFFT_Radix2(numberOfSamples);
			double[] inputDataArray = toArray(inputData);
			fft.transform(inputDataArray);
			double[] values = fft.toWraparoundOrder(inputDataArray);
			chart.setNotify(false);
			xySeries.clear();
			for (int i=0; i<numberOfSamples; i++) {
				double frequency = sampleRate*i/(numberOfSamples*2);
				xySeries.add(frequency, values[i]);
			}
			chart.setNotify(true);
			chart.fireChartChanged();
		}
		
		lastTimeDisplayed = times[endIndex];
	}
	
	private double[] toArray(ArrayList arrayList) {
		double[] array = new double[arrayList.size()];
		for (int i=0; i<arrayList.size(); i++) {
			Double d = (Double)arrayList.get(i);
			array[i] = d.doubleValue();
			if (useHanningWindow) {
				array[i] *= hanningWindow[i];
			}
		}
		return array;
	}
	
	private void setHanningWindow() {
	    double pi = Math.PI;
	    int m = numberOfSamples/2;
	    double r = pi/(m+1);
	    double[] w = new double[numberOfSamples];
        
        for (int n = -m; n < m; n++) {
          w[m + n] = 0.5d + 0.5d*Math.cos(n*r);
        }
		
		hanningWindow = w;
	}
	
	void clearData() {
		xySeries.clear();
		inputData.clear();
		lastTimeDisplayed = -1;
	}

	public String toString() {
		return "Spectrum Analyzer Data Panel";
	}
	

}