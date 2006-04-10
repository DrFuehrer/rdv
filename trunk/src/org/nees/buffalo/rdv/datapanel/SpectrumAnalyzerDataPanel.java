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
 * $URL: svn+ssh://jphanley@code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/datapanel/SpectrumAnalyzerDataPanel.java $
 * $Revision: 371 $
 * $Date: 2005-12-26 13:11:21 -0500 (Mon, 26 Dec 2005) $
 * $Author: jphanley $
 */

package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	double[] inputData;
	int inputDataIndex;
	
	double[] window;
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
	
	public SpectrumAnalyzerDataPanel() {
		super();
		
		sampleRate = 256;
		numberOfSamples = 512;
		
		inputData = new double[numberOfSamples];
		inputDataIndex = 0;
		
		createWindow();
		useHanningWindow = true;
		
		lastTimeDisplayed = -1;
		
		initComponents();
		
		setDataComponent(panel);
	}
	
	private void initComponents() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		xySeries = new XYSeries("", false, false);
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
				if (inputData.length < numberOfSamples) {
					inputData = new double[numberOfSamples];
					inputDataIndex = 0;
				}
				createWindow();
			}
		});
		controlPanel.add(dataPointsTextField);
		
		useHanningWindowCheckBox = new JCheckBox("Use hanning window", true);
		useHanningWindowCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useHanningWindow = !useHanningWindow;
				plotSpectrum();
			}
		});
		controlPanel.add(useHanningWindowCheckBox);
				
		panel.add(chartPanel, BorderLayout.CENTER);
		panel.add(controlPanel, BorderLayout.SOUTH);	
	}

	public boolean supportsMultipleChannels() {
		return false;
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
			inputData[inputDataIndex++] = data[i];
			if (inputDataIndex == numberOfSamples) {
				inputDataIndex = 0;
			}
		}
		
		plotSpectrum();
		
		lastTimeDisplayed = times[endIndex];
	}
	
	private void plotSpectrum() {
		RealDoubleFFT fft = new RealDoubleFFT_Radix2(numberOfSamples);
		double[] inputDataArray = generateInputArray();
		fft.transform(inputDataArray);
		chart.setNotify(false);
		xySeries.clear();
		
		double period = sampleRate/numberOfSamples; 
    double frequency = 0;
		for (int i=0; i<=numberOfSamples/2; i++) {
      double datum = Math.pow(inputDataArray[i], 2);
			xySeries.add(frequency, datum);
      frequency += period;
		}
		
		chart.setNotify(true);
		chart.fireChartChanged();		
	}
	
	private double[] generateInputArray() {
		double[] array = new double[numberOfSamples];
		for (int i=0; i<numberOfSamples; i++) {
			int index = (inputDataIndex + i) % numberOfSamples;
			array[i] = inputData[index];
			if (useHanningWindow) {
				array[i] *= window[i];
			}
		}
		return array;
	}
	
	private void createWindow() {
	    double pi = Math.PI;
	    int m = numberOfSamples/2;
	    double r = pi/(m+1);
	    window = new double[numberOfSamples];
        for (int n = -m; n < m; n++) {
          window[m + n] = 0.5d + 0.5d*Math.cos(n*r);
        }
	}
	
	void clearData() {
		xySeries.clear();
		for (int i=0; i<inputData.length; i++) {
			inputData[i] = 0;
		}
		inputDataIndex = 0;
		lastTimeDisplayed = -1;
	}

	public String toString() {
		return "Spectrum Analyzer Data Panel";
	}
	

}
