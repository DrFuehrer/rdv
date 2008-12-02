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

package org.rdv.viz.spectrum;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.data.Channel;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.ui.SpringUtilities;

import com.rbnb.sapi.ChannelMap;

/**
 * A spectrum analyzer.
 * 
 * @author Jason P. Hanley
 */
public class SpectrumViz extends AbstractDataPanel {

  /** the logger for this class */
  static Log log = LogFactory.getLog(SpectrumViz.class.getName());

  /** the data panel property for the sample rate */
  private static final String DATA_PANEL_PROPERTY_SAMPLE_RATE = "sampleRate";

  /** the data panel property for the number of samples */
  private static final String DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES = "numberOfSamples";

  /** the data panel property for the window function */
  private static final String DATA_PANEL_PROPERTY_WINDOW_FUNCTION = "windowFunction";

  /** the data panel property for the segment size */
  private static final String DATA_PANEL_PROPERTY_SEGMENT_SIZE = "segmentSize";

  /** the data panel property for the overlap */
  private static final String DATA_PANEL_PROPERTY_OVERLAP = "overlap";

  /** the data panel property to control the visiblity of the properties panel */
  private static final String DATA_PANEL_PROPERTY_SHOW_PROPERTIES = "showProperties";

  /** the time for the last data point displayed */
  private double lastTimeDisplayed;

  /** the main panel */
  private JPanel panel;

  /** the spectrum analyzer panel */
  private SpectrumAnalyzerPanel spectrumAnalyzerPanel;

  /** the properties panel */
  private JPanel propertiesPanel;

  /** the sample rate text field */
  private JTextField sampleRateTextField;

  /** the number of samples text field */
  private JTextField numberOfSamplesTextField;

  /** the window function combo box */
  private JComboBox windowFunctionComboBox;

  /** the segment size text field */
  private JTextField segmentSizeTextField;;

  /** the overlap text field */
  private JTextField overlapTextField;

  /** the show properties menu item */
  private JCheckBoxMenuItem showPropertiesMenuItem;
	
  /**
   * Creates the spectrum analyzer data panel.
   */
	public SpectrumViz() {
		super();
				
		lastTimeDisplayed = -1;
		
		initComponents();
		
		setDataComponent(panel);
	}
	
	/**
	 * Initializes the UI components.
	 */
	private void initComponents() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		initSpectrumAnalyzerPanel();
		
		initPropertiesPanel();
		
		initPopupMenu();
	}
	
	/**
	 * Initializes the spectrum analyzer panel.
	 */
	private void initSpectrumAnalyzerPanel() {
	  spectrumAnalyzerPanel = new SpectrumAnalyzerPanel();
    spectrumAnalyzerPanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        hanelSpectrumAnalyzerPanelPropertyChanges(pce);
      }     
    });
	    
    panel.add(spectrumAnalyzerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Initializes the properties panel.
	 */
	private void initPropertiesPanel() {
    propertiesPanel = new JPanel();
    propertiesPanel.setLayout(new SpringLayout());
    propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
    
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        handlePropertiesUpdate((Component) ae.getSource());
      }
    };
    
    FocusAdapter focusListener = new FocusAdapter() {
      public void focusLost(FocusEvent fe) {
        handlePropertiesUpdate(fe.getComponent());
      }
    };
    
    propertiesPanel.add(new JLabel("Sample rate: "));
    sampleRateTextField = new JTextField(Double.toString(spectrumAnalyzerPanel.getSampleRate()));
    sampleRateTextField.addActionListener(actionListener);
    sampleRateTextField.addFocusListener(focusListener);
    propertiesPanel.add(sampleRateTextField);
    
    propertiesPanel.add(new JLabel("Number of points: "));
    numberOfSamplesTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples()));
    numberOfSamplesTextField.addActionListener(actionListener);
    numberOfSamplesTextField.addFocusListener(focusListener);
    propertiesPanel.add(numberOfSamplesTextField);
    
    propertiesPanel.add(new JLabel("Window: "));
    Object[] windowTypes = EnumSet.allOf(WindowFunction.class).toArray();
    windowFunctionComboBox = new JComboBox(windowTypes);
    windowFunctionComboBox.setSelectedItem(spectrumAnalyzerPanel.getWindowFunction());
    windowFunctionComboBox.addActionListener(actionListener);
    propertiesPanel.add(windowFunctionComboBox);
    
    propertiesPanel.add(new JLabel("Size: "));
    segmentSizeTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getSegmentSize()));
    segmentSizeTextField.addActionListener(actionListener);
    segmentSizeTextField.addFocusListener(focusListener);
    propertiesPanel.add(segmentSizeTextField);
    
    propertiesPanel.add(new JLabel("Overlap: "));
    overlapTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getOverlap()));
    overlapTextField.addActionListener(actionListener);
    overlapTextField.addFocusListener(focusListener);
    propertiesPanel.add(overlapTextField);
    
    SpringUtilities.makeCompactGrid(propertiesPanel, 5, 2, 5, 5, 5, 5);
    
    panel.add(propertiesPanel, BorderLayout.EAST);
	}
	
	/**
	 * Initializes the popup menu. This takes the popup menu from the chart and
	 * adds on to it.
	 */
	private void initPopupMenu() {
	  JPopupMenu popupMenu = spectrumAnalyzerPanel.getPopupMenu();
	  
	  JMenuItem propertiesMenuItem = (JMenuItem) popupMenu.getComponent(0);
	  propertiesMenuItem.setText("Chart Properties...");
	  
	  popupMenu.add(new JPopupMenu.Separator());
	  
	  showPropertiesMenuItem = new JCheckBoxMenuItem("Show properties", true);
	  showPropertiesMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setPropertiesVisible(showPropertiesMenuItem.isSelected());
      }
	  });
	  popupMenu.add(showPropertiesMenuItem);
	  
	  panel.setComponentPopupMenu(popupMenu);
	}
	
	/**
	 * Handle events from the properties panel.
	 * 
	 * @param source  the source of the event
	 */
	private void handlePropertiesUpdate(Component source) {
	  if (source == null) return;
	  
	  if (source.equals(sampleRateTextField)) {
	    handleSampleRateUpdate();
	  } else if (source.equals(numberOfSamplesTextField)) {
	    handleNumberOfSamplesUpdate();
	  } else if (source.equals(windowFunctionComboBox)) {
	    handleWindowFunctionUpdate();
	  } else if (source.equals(segmentSizeTextField)) {
	    handleSegmentSizeUpdate();
	  } else if (source.equals(overlapTextField)) {
	    handleOverlapUpdate();
	  }
	}
	
	/**
	 * Handles events from the sample rate component.
	 */
	private void handleSampleRateUpdate() {
    try {
      double sampleRate = Double.parseDouble(sampleRateTextField.getText());
      spectrumAnalyzerPanel.setSampleRate(sampleRate);
    } catch (Exception e) {
      sampleRateTextField.setText(Double.toString(spectrumAnalyzerPanel.getSampleRate()));
      return;
    }
	}
	
	/**
	 * Handles events from the number of samples component.
	 */
	private void handleNumberOfSamplesUpdate() {
    try {
      int numberOfSamples = Integer.parseInt(numberOfSamplesTextField.getText());
      spectrumAnalyzerPanel.setNumberOfSamples(numberOfSamples);
    } catch (Exception e) {
      numberOfSamplesTextField.setText(Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples()));
      return;
    }
	}
	
	/**
	 * Handles events from the window function component.
	 */
	private void handleWindowFunctionUpdate() {
	  WindowFunction windowFunction = (WindowFunction)windowFunctionComboBox.getSelectedItem();
	  spectrumAnalyzerPanel.setWindowFunction(windowFunction);
	}
	
	/**
	 * Handles events from the segment size component.
	 */
	private void handleSegmentSizeUpdate() {
    try {
      int segmentSize = Integer.parseInt(segmentSizeTextField.getText());
      spectrumAnalyzerPanel.setSegmentSize(segmentSize);
    } catch (Exception e) {
      segmentSizeTextField.setText(Integer.toString(spectrumAnalyzerPanel.getSegmentSize()));
      return;
    }
	}
	
	/**
	 * Handles events from the overlap component.
	 */
	private void handleOverlapUpdate() {
    try {
      int overlap = Integer.parseInt(overlapTextField.getText());
      spectrumAnalyzerPanel.setOverlap(overlap);
    } catch (Exception e) {
      overlapTextField.setText(Integer.toString(spectrumAnalyzerPanel.getOverlap()));
      return;
    }
	}
	
	/**
	 * Hanles property change events from the spectrum analyzer panel.
	 * 
	 * @param pce  the property change event
	 */
	private void hanelSpectrumAnalyzerPanelPropertyChanges(PropertyChangeEvent pce) {
    String propertyName = pce.getPropertyName();
    if (propertyName.equals(SpectrumAnalyzerPanel.SAMPLE_RATE_PROPERTY)) {
      String sampleRate = Double.toString(spectrumAnalyzerPanel.getSampleRate());
      sampleRateTextField.setText(sampleRate);
      properties.setProperty(DATA_PANEL_PROPERTY_SAMPLE_RATE, sampleRate);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.NUMBER_OF_SAMPLES_PROPERTY)) {
      String numberOfSamples = Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples());
      numberOfSamplesTextField.setText(numberOfSamples);
      properties.setProperty(DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES, numberOfSamples);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.WINDOW_FUNCTION_PROPERTY)) {
      WindowFunction windowFunction = spectrumAnalyzerPanel.getWindowFunction();
      windowFunctionComboBox.setSelectedItem(windowFunction);
      String windowFunctionString = windowFunction.toString();
      properties.setProperty(DATA_PANEL_PROPERTY_WINDOW_FUNCTION, windowFunctionString);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.SEGMENT_SIZE_PROPERTY)) {
      String segmentSize = Integer.toString(spectrumAnalyzerPanel.getSegmentSize());
      segmentSizeTextField.setText(segmentSize);
      properties.setProperty(DATA_PANEL_PROPERTY_SEGMENT_SIZE, segmentSize);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.OVERLAP_PROPERTY)) {
      String overlap = Integer.toString(spectrumAnalyzerPanel.getOverlap());
      overlapTextField.setText(overlap);
      properties.setProperty(DATA_PANEL_PROPERTY_OVERLAP, overlap);
    }
	}
	
	/**
	 * Gets the properties panel visibility.
	 * 
	 * @return  true if visible, false otherwise
	 */
  public boolean isPropertiesVisible() {
    return propertiesPanel.isVisible();
  }
  
  /**
   * Sets the properties panel visibility.
   * 
   * @param visible  true to make the panel visible, false to make it invisible
   */
  public void setPropertiesVisible(boolean visible) {
    propertiesPanel.setVisible(visible);
    showPropertiesMenuItem.setSelected(visible);
    
    if (visible) {
      properties.remove(DATA_PANEL_PROPERTY_SHOW_PROPERTIES);
    } else {
      properties.setProperty(DATA_PANEL_PROPERTY_SHOW_PROPERTIES, "false");
    }
  }

	public boolean supportsMultipleChannels() {
		return false;
	}

	@Override
  protected void channelAdded(String channelName) {
	  Channel channel = rbnbController.getChannel(channelName);
	  if (channel == null) {
	    return;
	  }
	  
	  // get the sample rate and send it to the spectrum analzyer panel
	  String sampleRateString = channel.getMetadata("sampleRate");
	  if (sampleRateString == null) {
	    return;
	  }
	  
	  double sampleRate;
	  try {
	    sampleRate = Double.parseDouble(sampleRateString);
	  } catch (NumberFormatException e) {
	    return;
	  }
	  
	  if (sampleRate > 0) {
	    spectrumAnalyzerPanel.setSampleRate(sampleRate);
	    
	    sampleRateTextField.setEnabled(false);
	  }
  }

  @Override
  protected void channelRemoved(String channelName) {
    clearData();
    
    sampleRateTextField.setEnabled(true);
  }

  @Override
	public void postData(ChannelMap channelMap) {
    this.channelMap = channelMap;
	}

	@Override
	public void postTime(double time) {
    if (time < this.time) {
      clearData();
    }

		super.postTime(time);

		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
    //loop over all channels and see if there is data for them
    for (String channelName : channels) {
      int channelIndex = channelMap.GetIndex(channelName);
      
      //if there is data for channel, post it
      if (channelIndex != -1) {
        postTime(channelName, channelIndex);
      }
    }
	}

	/**
	 * Looks through the data for the current time range and post it if any is
	 * dound.
	 * 
	 * @param channelName   the name for the channel
	 * @param channelIndex  the index for the channel
	 */
	private void postTime(String channelName, int channelIndex) {
	  // FIXME handle all data types
	  if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_FLOAT64) {
	    return;
	  }
	  
		double[] times = channelMap.GetTimes(channelIndex);

		int startIndex = -1;
		
    // determine what time we should load data from
    double dataStartTime;
    if (lastTimeDisplayed == time) {
      dataStartTime = time-timeScale;
    } else {
      dataStartTime = lastTimeDisplayed;
    }

    for (int i=0; i<times.length; i++) {
        if (times[i] > dataStartTime && times[i] <= time) {
            startIndex = i;
            break;
        }
    }
    
    //see if there is no data in the time range we are looking at
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

    double[] data = channelMap.GetDataAsFloat64(channelIndex);
    postData(data, startIndex, endIndex);
				
		lastTimeDisplayed = times[endIndex];
	}
	
	/**
	 * Post the data to the spectrum analyzer. This will method will queue the
	 * post on the EDT thread and return immediately.
	 * 
	 * @param data        the data to post
	 * @param startIndex  the start index of the data
	 * @param endIndex    the end index of the data
	 */
	private void postData(final double[] data, final int startIndex, final int endIndex) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        spectrumAnalyzerPanel.addData(data, startIndex, endIndex);
      }
      
    });
	}
	
	/**
	 * Clear the current stored data. This will queue a call on the EDT to clear
	 * the data in the spectrum analyzer and return immediatly.
	 */
	private void clearData() {
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      spectrumAnalyzerPanel.clearData();
	    }
	  });
	  
		lastTimeDisplayed = -1;
	}
	
  @Override
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
    
    properties.setProperty(key, value);
    
    if (key.equals(DATA_PANEL_PROPERTY_SAMPLE_RATE)) {
      try {
        double sampleRate = Double.parseDouble(value);
        spectrumAnalyzerPanel.setSampleRate(sampleRate);
      } catch (Exception e) {
        log.warn("Unable to set sample rate: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES)) {
      try {
        int numberOfSamples = Integer.parseInt(value);
        spectrumAnalyzerPanel.setNumberOfSamples(numberOfSamples);
      } catch (Exception e) {
        log.warn("Unable to set number of samples: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_WINDOW_FUNCTION)) {
      try {
        WindowFunction windowFunction = WindowFunction.valueOf(value);
        spectrumAnalyzerPanel.setWindowFunction(windowFunction);
      } catch (Exception e) {
        log.warn("Unable to set window function: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_SEGMENT_SIZE)) {
      try {
        int segmentSize = Integer.parseInt(value);
        spectrumAnalyzerPanel.setSegmentSize(segmentSize);
      } catch (Exception e) {
        log.warn("Unable to set segment size: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_OVERLAP)) {
      try {
        int overlap = Integer.parseInt(value);
        spectrumAnalyzerPanel.setOverlap(overlap);
      } catch (Exception e) {
        log.warn("Unable to set overlap: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_SHOW_PROPERTIES) && !Boolean.parseBoolean(value)) {
      setPropertiesVisible(false);
    }
  }

	@Override
	public String toString() {
		return "Spectrum Analyzer";
	}
	
}