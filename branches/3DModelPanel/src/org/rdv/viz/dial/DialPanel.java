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

package org.rdv.viz.dial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DefaultValueDataset;
import org.rdv.util.EngineeringFormat;

/**
 * A JPanel for displaying a dial visualization.
 * 
 * @author  Jason P. Hanley
 * @see     DialModel
 */
public class DialPanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = 4279289359942253323L;
  
  /** the dial model */
  private final DialModel model;
  
  /** the dataset for the dial */
  private DefaultValueDataset dataset;
  
  /** the plot for the dial */
  private DialPlot plot;
  
  /** the indicator for the dial value */
  private DialValueIndicator dialValueIndicator;
  
  /** the text annotation for the channel name */
  private DialTextAnnotation dialTextAnnotation;
  
  /** the scale for the dial */
  private StandardDialScale dialScale;
  
  /** the formatter for the dial ticks */
  private EngineeringFormat engineeringFormat;
  
  /** the formatter for the dial value indicator */
  private EngineeringFormat engineeringFormatWithUnit;
  
  /** the text field for the lower bound of the dial */
  private JTextField lowerBoundTextField;

  /** the text field for the upper bound of the dial */
  private JTextField upperBoundTextField;

  /** the value for the critical minimum threshold */
  private double criticalMinimumThreshold;

  /** the value for the warning minimum threshold */
  private double warningMinimumThreshold;
  
  /** the value for the warning maximum threshold */
  private double warningMaximumThreshold;
  
  /** the  value for the critical maximum threshold */
  private double criticalMaxThreshold;
  
  /** the dial ranges for the thresholds */
  private StandardDialRange[] thresholdDialRanges = new StandardDialRange[4];
  
  /**
   * Creates the DialPanel with the default model.
   * 
   * @see  DialModel
   */
  public DialPanel() {
    this(new DialModel());
  }

  /**
   * Creates the DialPanel with the given DialModel.
   * 
   * @param model  the model for the dial panel
   */
  public DialPanel(DialModel model) {
    super();
    
    this.model = model;

    initDataset();

    initPanel();
    
    updateRange();
    
    initModelListener();
  }
  
  /**
   * Gets the model for this dial.
   * 
   * @return  the model for this dial
   */
  public DialModel getModel() {
    return model;
  }

  /**
   * Initializes the DialPlot dataset.
   */
  private void initDataset() {
    dataset = new DefaultValueDataset(model.getValue());
    dataset.addChangeListener(new DatasetChangeListener() {
      public void datasetChanged(DatasetChangeEvent arg0) {
        checkThresholds();
      }      
    });    
  }

  /**
   * Initializes the dial panel.
   */
  private void initPanel() {
    setLayout(new BorderLayout());
    
    JPanel chartPanel = createDialPanel();
    add(chartPanel, BorderLayout.CENTER);

    JPanel settingsPanel = createSettingsPanel();
    add(settingsPanel, BorderLayout.PAGE_END);    
  }
  
  /**
   * Creates the panel containing the dial.
   * 
   * @return  the dial panel
   */
  private JPanel createDialPanel() {
    plot = new DialPlot(dataset);
    
    plot.setDialFrame(new StandardDialFrame());

    engineeringFormat = new EngineeringFormat();
    engineeringFormatWithUnit = new EngineeringFormat();
    
    dialValueIndicator = new DialValueIndicator();
    dialValueIndicator.setOutlinePaint(Color.black);
    dialValueIndicator.setRadius(0.7);
    dialValueIndicator.setVisible(false);
    dialValueIndicator.setNumberFormat(engineeringFormatWithUnit);
    plot.addLayer(dialValueIndicator);

    dialTextAnnotation = new DialTextAnnotation("");
    dialTextAnnotation.setRadius(0.8);
    plot.addLayer(dialTextAnnotation);
    
    DialPointer dialPointer = new DialPointer.Pointer();
    dialPointer.setRadius(0.9);
    plot.addPointer(dialPointer);
    
    plot.setCap(new DialCap());
    
    dialScale = new BoundedDialScale();
    dialScale.setStartAngle(-120);
    dialScale.setExtent(-300);
    dialScale.setMinorTickCount(5);
    dialScale.setTickLabelFormatter(engineeringFormat);
    dialScale.setTickRadius(0.9);
    
    JFreeChart chart = new JFreeChart(plot);
    chart.removeLegend();   
    
    return new ChartPanel(chart);
  }
  
  /**
   * Creates the panel containing settings.
   * 
   * @return  the settings panel
   */
  private JPanel createSettingsPanel() {
    JPanel settingsPanel = new JPanel();
    settingsPanel.setLayout(new BorderLayout());
    settingsPanel.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
    
    lowerBoundTextField = new JTextField(6);
    lowerBoundTextField.setToolTipText("The minimum value for the dial");
    lowerBoundTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setRangeFromTextFields();
      }
    });
    settingsPanel.add(lowerBoundTextField, BorderLayout.LINE_START);
    
    upperBoundTextField = new JTextField(6);
    upperBoundTextField.setToolTipText("The maximum value for the dial");
    upperBoundTextField.setHorizontalAlignment(JTextField.TRAILING);
    upperBoundTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setRangeFromTextFields();
      }
    });
    settingsPanel.add(upperBoundTextField, BorderLayout.LINE_END);
    
    return settingsPanel;
  }

  private void initModelListener() {
    model.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("value")) {
          Number value = (Number)pce.getNewValue();
          dataset.setValue(value);
        } else if (pce.getPropertyName().equals("name")) {
          String name = (String)pce.getNewValue();
          dialValueIndicator.setVisible(name != null);
          if (name != null) {
            dialTextAnnotation.setLabel(name);
          }
        } else if (pce.getPropertyName().equals("unit")) {
          String unit = (String)pce.getNewValue();
          engineeringFormatWithUnit.setUnit(unit);
        } else if (pce.getPropertyName().equals("range")) {
          updateRange();
        } else if (pce.getPropertyName().equals("warningThreshold") ||
            pce.getPropertyName().equals("criticalThreshold")) {
          updateThresholdRanges();
        }
      }      
    });    
  }
  
  /**
   * Sets the range of the dial according to the range text fields.
   */
  private void setRangeFromTextFields() {
    double lowerBound;
    double upperBound;
    try {
      lowerBound = Double.parseDouble(lowerBoundTextField.getText());
      upperBound = Double.parseDouble(upperBoundTextField.getText());
      
      if (lowerBound >= upperBound) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      lowerBoundTextField.setText(engineeringFormat.format(model.getRange().getLowerBound()));
      upperBoundTextField.setText(engineeringFormat.format(model.getRange().getUpperBound()));
      return;
    }
    
    model.setRange(new Range(lowerBound, upperBound));    
  }
  
  /**
   * Updates the dial range.
   * 
   * @param range  the dial range
   */
  private void updateRange() {
    Range range = model.getRange();
    
    lowerBoundTextField.setText(engineeringFormat.format(model.getRange().getLowerBound()));
    upperBoundTextField.setText(engineeringFormat.format(model.getRange().getUpperBound()));
    
    dialScale.setLowerBound(range.getLowerBound());
    dialScale.setUpperBound(range.getUpperBound());
    
    double tickIncrement = range.getLength()/10;
    dialScale.setMajorTickIncrement(tickIncrement);
    
    plot.addScale(0, dialScale);
    
    updateThresholdRanges();
  }
  
  /**
   * Updates the threshold ranges.
   * 
   * @param range  the dial range
   */
  private void updateThresholdRanges() {
    Range range = model.getRange();
    double warningThresh = model.getWarningThreshold() * (range.getLength());
    double criticalThresh = model.getCriticalThreshold() * (range.getLength());

    criticalMinimumThreshold = range.getLowerBound() + criticalThresh;
    warningMinimumThreshold = range.getLowerBound() + warningThresh;
    warningMaximumThreshold = range.getUpperBound() - warningThresh;
    criticalMaxThreshold = range.getUpperBound() - criticalThresh;

    // remove previous dial ranges
    for (StandardDialRange dialRange : thresholdDialRanges) {
      if (dialRange != null) {
        plot.removeLayer(dialRange);
      }
    }
    
    thresholdDialRanges[0] = new StandardDialRange(range.getLowerBound(), criticalMinimumThreshold, Color.red);
    thresholdDialRanges[0].setInnerRadius(0);
    thresholdDialRanges[0].setOuterRadius(0.9);
    plot.addLayer(thresholdDialRanges[0]);

    thresholdDialRanges[1] = new StandardDialRange(criticalMinimumThreshold, warningMinimumThreshold, Color.yellow);
    thresholdDialRanges[1].setInnerRadius(0);
    thresholdDialRanges[1].setOuterRadius(0.9);
    plot.addLayer(thresholdDialRanges[1]);  
    
    thresholdDialRanges[2] = new StandardDialRange(warningMaximumThreshold, criticalMaxThreshold, Color.yellow);
    thresholdDialRanges[2].setInnerRadius(0);
    thresholdDialRanges[2].setOuterRadius(0.9);
    plot.addLayer(thresholdDialRanges[2]);
    
    thresholdDialRanges[3] = new StandardDialRange(criticalMaxThreshold, range.getUpperBound(), Color.red);
    thresholdDialRanges[3].setInnerRadius(0);
    thresholdDialRanges[3].setOuterRadius(0.9);
    plot.addLayer(thresholdDialRanges[3]);
    
    // make sure to do this after changing the dial ranges because it gets reset for some reason
    dialValueIndicator.setTemplateValue(-222.222e222);
    
    checkThresholds();
  }

  /**
   * Checks the dial value against the warning and critical threshold values. If
   * a threshold is reached, the dial value indicator will change color
   * according to the type of threshold reached.
   */
  private void checkThresholds() {
    Number numberValue = plot.getDataset().getValue();
    
    if (numberValue == null) {
      dialValueIndicator.setBackgroundPaint(Color.white);
    } else {
      double value = numberValue.doubleValue();
      
      if (value < criticalMinimumThreshold
          || value > criticalMaxThreshold) {
        dialValueIndicator.setBackgroundPaint(Color.red);
      } else if (value < warningMinimumThreshold
          || value > warningMaximumThreshold) {
        dialValueIndicator.setBackgroundPaint(Color.yellow);
      } else {
        dialValueIndicator.setBackgroundPaint(Color.white);
      }
    }
  }
  
}