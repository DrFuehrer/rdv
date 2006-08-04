/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
 * Copyright (c) 2005-2006 NEES Cyberinfrastructure Center
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

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
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;
import org.nees.buffalo.rdv.data.DataFileChannel;
import org.nees.buffalo.rdv.data.DataFileListener;
import org.nees.buffalo.rdv.data.DataFileReader;
import org.nees.buffalo.rdv.rbnb.Channel;

import com.rbnb.sapi.ChannelMap;

/**
 * A data panel to plot data time series and xy charts. 
 * 
 * @author Jason P. Hanley
 */
public class JFreeChartDataPanel extends AbstractDataPanel {	
  /**
   * The logger for this class.
   */
	static Log log = LogFactory.getLog(JFreeChartDataPanel.class.getName());
	
  /**
   * The chart.
   */
	JFreeChart chart;
  
  /**
   * The xy plot for this chart.
   */
  XYPlot xyPlot;
  
  /**
   * The domain (horizontal) axis that contains a value. This will be a number
   * axis for an xy plot or a date axis for a timeseries plot.
   */
  ValueAxis domainAxis;
  
  /**
   * The range (vertical) axis that contains a number.
   */
  NumberAxis rangeAxis;
  
  /**
   * The component that renderers the chart.
   */
	ChartPanel chartPanel;
  
  /**
   * The data set for the chart.
   */
	XYDataset dataCollection;
  
  /**
   * The legend for the series in the chart.
   */
  LegendTitle seriesLegend;
	
  /**
   * The container for the chart component.
   */
	JPanel chartPanelPanel;
	
  /**
   * A bit to indicate if we are plotting time series charts of x vs. y charts.
   */
	final boolean xyMode;
	
  /**
   * The timestamp for the last piece if data displayed.
   */
  double lastTimeDisplayed;
  
  /**
   * The number of local data series.
   */
  int localSeries;
  
  /**
   * A channel map used to cache the values of an xy data set when only one
   * channel has been added.
   */
  ChannelMap cachedChannelMap;
  
  /**
   * Plot colors for each series.
   */
  HashMap<String,Color> colors;
  
  /**
   * Colors used for the series.
   */
  final static Color[] seriesColors = {Color.decode("#FF0000"), Color.decode("#0000FF"),
                    Color.decode("#009900"), Color.decode("#FF9900"),
                    Color.decode("#9900FF"), Color.decode("#FF0099"),
                    Color.decode("#0099FF"), Color.decode("#990000"),
                    Color.decode("#000099"), Color.black};

  /**
   * The file chooser UI used to select a local data file.
   */
  JFileChooser chooser;
  
  /**
   * Constructs a chart data panel in time series mode.
   */
	public JFreeChartDataPanel() {
		this(false);
	}

  /**
   * Constructs a chart data panel.
   * 
   * @param xyMode  if true in x vs. y mode, otherwise in time series mode
   */
	public JFreeChartDataPanel(boolean xyMode) {
		super();
		
		this.xyMode = xyMode;
		
    lastTimeDisplayed = -1;
    
    colors = new HashMap<String,Color>();
		
		initChart();
		
		setDataComponent(chartPanelPanel);
	}
		
  /**
   * Create the chart and setup it's UI.
   */
	private void initChart() {
    XYToolTipGenerator toolTipGenerator;
    
		if (xyMode) {
			dataCollection = new XYTimeSeriesCollection();
      
      NumberAxis domainAxis = new NumberAxis();
      domainAxis.setAutoRangeIncludesZero(true);
      domainAxis.addChangeListener(new AxisChangeListener() {
        public void axisChanged(AxisChangeEvent ace) {
          boundsChanged();
        }        
      });
      this.domainAxis = domainAxis;
      
      toolTipGenerator = new StandardXYToolTipGenerator("{0}: {1} , {2}",
          new DecimalFormat(),
          new DecimalFormat());
		} else {
			dataCollection = new TimeSeriesCollection();
      
      domainAxis = new DateAxis();
      domainAxis.setLabel("Time");
            
      toolTipGenerator = new StandardXYToolTipGenerator("{0}: {1} , {2}",
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
          new DecimalFormat());
    }
    
    rangeAxis = new NumberAxis();
    rangeAxis.setAutoRangeIncludesZero(true);    
    rangeAxis.addChangeListener(new AxisChangeListener() {
      public void axisChanged(AxisChangeEvent ace) {
        boundsChanged();
      }        
    });    
    
    StandardXYItemRenderer renderer = new FastXYItemRenderer(StandardXYItemRenderer.LINES,
        toolTipGenerator);
    renderer.setDefaultEntityRadius(6);    
    
    xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
    
    chart = new JFreeChart(xyPlot);    
		chart.setAntiAlias(false);
    
    seriesLegend = chart.getLegend();
    chart.removeLegend();    

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
    
    if (xyMode) {
      popupMenu.add(new JPopupMenu.Separator());
      
      JMenuItem addLocalSeriesMenuItem = new JMenuItem("Add local series...");
      addLocalSeriesMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          addLocalSeries();
        }        
      });
      
      popupMenu.add(addLocalSeriesMenuItem);
    }

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
  
  /**
   * Add data from a local file as a series to this chart. This will ask the
   * user for the file name, and which channels to use.
   */
  private void addLocalSeries() {
    if (chooser == null) {
      chooser = new JFileChooser();
    }
    
    int returnVal = chooser.showOpenDialog(dataComponent);
    if(returnVal != JFileChooser.APPROVE_OPTION) {
      return;
    }
    
    File file = chooser.getSelectedFile();
    if (file == null || !file.isFile() || !file.exists()) {
      return;
    }
    
    DataFileReader reader;
    try {
      reader = new DataFileReader(file);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(dataComponent,
          e.getMessage(),
          "Problem reading data file",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    List<DataFileChannel> channels = reader.getChannels();
    if (channels.size() < 2) {
      JOptionPane.showMessageDialog(dataComponent,
          "There must be at least 2 channels in the data file",
          "Problem with data file",
          JOptionPane.ERROR_MESSAGE);      
      return;
    }
    
    DataFileChannel xChannel = (DataFileChannel)JOptionPane.showInputDialog(
        dataComponent,
        "Select the x channel:",
        "Add local channel",
        JOptionPane.PLAIN_MESSAGE,
        null,
        channels.toArray(),
        null);
    
    if (xChannel == null) {
      return;
    }
    
    DataFileChannel yChannel = (DataFileChannel)JOptionPane.showInputDialog(
        dataComponent,
        "Select the y channel:",
        "Add local channel",
        JOptionPane.PLAIN_MESSAGE,
        null,
        channels.toArray(),
        null);
    
    if (yChannel == null) {
      return;
    }
    
    String xChannelName = xChannel.getChannelName();
    if (xChannel.getUnit() != null) {
      xChannelName += " (" + xChannel.getUnit() + ")";
    }
    final int xChannelIndex = channels.indexOf(xChannel);
    
    String yChannelName = yChannel.getChannelName();
    if (yChannel.getUnit() != null) {
      yChannelName += " (" + yChannel.getUnit() + ")";
    }
    final int yChannelIndex = channels.indexOf(yChannel);
    
    String seriesName = xChannelName + " vs. " + yChannelName;
    
    final XYTimeSeries data = new XYTimeSeries(seriesName, FixedMillisecond.class);
    
    DataFileListener listener = new DataFileListener() {
      public void postDataSamples(double timestamp, double[] values) {
        FixedMillisecond time = new FixedMillisecond((long)(timestamp*1000));
        XYTimeSeriesDataItem dataItem = new XYTimeSeriesDataItem(time);
        if (values[xChannelIndex] != Double.NaN && values[yChannelIndex] != Double.NaN) {
          dataItem.setX(values[xChannelIndex]);
          dataItem.setY(values[yChannelIndex]);
        }
        data.add(dataItem, false);
      }        
    };
    
    try {
      reader.readData(listener);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    Color color = getLeastUsedColor();
    colors.put(seriesName, color);      
    
    ((XYTimeSeriesCollection)dataCollection).addSeries(data);
    localSeries++;
    
    setSeriesColors();
    
    updateTitle();
    
    updateLegend();
  }
  
  /**
   * Remove the local series from the chart.
   * 
   * @param seriesName  the name of the local series.
   */
  private void removeLocalSeries(String seriesName) {
    XYTimeSeries series = ((XYTimeSeriesCollection)dataCollection).getSeries(seriesName);
    
    if (series == null) {
      return;
    }
    
    localSeries--;
    ((XYTimeSeriesCollection)dataCollection).removeSeries(series);
    
    colors.remove(seriesName);
    setSeriesColors();
    
    updateTitle();
    
    updateLegend();
  }
  
  /**
   * Called when the bounds of an axis are changed. This updates the data panel
   * properties for these values.
   */
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
		
  /**
   * Indicates that this data panel can support multiple channels. This always
   * returns true.
   * 
   * @return  always true
   */
	public boolean supportsMultipleChannels() {
		return true;
	}
	
  /**
   * Called when a channel has been added.
   * 
   * @param channelName  the new channel
   */
  void channelAdded(String channelName) {
    String channelDisplay = getChannelDisplay(channelName);
    String seriesName = null;
    Color color = null;
    
    if (xyMode) {
      if (channels.size() % 2 == 0) {
        String firstChannelName = (String)channels.get(channels.size()-2);
        String firstChannelDisplay = getChannelDisplay(firstChannelName);
        seriesName = firstChannelDisplay + " vs. " + channelDisplay;
        
        color = getLeastUsedColor();
        
        XYTimeSeries data = new XYTimeSeries(seriesName, FixedMillisecond.class);
        data.setMaximumItemAge((int)(timeScale*1000));
        
        int position = dataCollection.getSeriesCount() - localSeries;
        ((XYTimeSeriesCollection)dataCollection).addSeries(position, data);
      }
    } else {
      seriesName = channelDisplay;
      
      color = getLeastUsedColor();
      
			TimeSeries data = new FastTimeSeries(seriesName, FixedMillisecond.class);
			data.setMaximumItemAge((int)(timeScale*1000));
			((TimeSeriesCollection)dataCollection).addSeries(data);
		}
    
    if (seriesName != null) {
      // find the least used color and set it
      colors.put(seriesName, color);
      setSeriesColors();      
    }
    
    updateTitle();    
    
    updateLegend();    
  }
  
  /**
   * Remove the channel from the data panel.
   * 
   * @param channelName  the channel to remove
   * @return             true if the channel was removed, false otherwise
   */
  public boolean removeChannel(String channelName) {
    if (xyMode) {
      if (!channels.contains(channelName)) {
        return false;
      }
      
      int channelIndex = channels.indexOf(channelName);
      
      String firstChannel, secondChannel;
      if (channelIndex % 2 == 0) {
        firstChannel = channelName;
        if (channelIndex+1 < channels.size()) {
          secondChannel = (String)channels.get(channelIndex+1);
        } else {
          secondChannel = null;
        }
      } else {
        firstChannel = (String)channels.get(channelIndex-1);
        secondChannel = channelName;
      }
      
      rbnbController.unsubscribe(firstChannel, this);
      channels.remove(firstChannel);

      if (secondChannel != null) {
        rbnbController.unsubscribe(secondChannel, this);
        channels.remove(secondChannel);
        
        String firstChannelDisplay = getChannelDisplay(firstChannel);
        String secondChannelDisplay = getChannelDisplay(secondChannel);
        String seriesName = firstChannelDisplay + " vs. " + secondChannelDisplay;
        
        XYTimeSeriesCollection dataCollection = (XYTimeSeriesCollection)this.dataCollection;
        XYTimeSeries data = dataCollection.getSeries(seriesName);
        dataCollection.removeSeries(data);
        
        colors.remove(seriesName);
      }
      
      channelRemoved(channelName);
      
      return true;
    } else {
      return super.removeChannel(channelName);
    }
  }
	
  /**
   * Called when a channel has been removed.
   * 
   * @param  the name of the channel that was removed
   */
  void channelRemoved(String channelName) {
		if (!xyMode) {
      String channelDisplay = getChannelDisplay(channelName);
      
			TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
			TimeSeries data = dataCollection.getSeries(channelDisplay);
			dataCollection.removeSeries(data);
      
      colors.remove(channelDisplay);
		}
    
    setSeriesColors();
    
    updateTitle();
    
    updateLegend();
  }
  
  /**
   * Return a color that is least used.
   * 
   * @return            the color
   */
  private Color getLeastUsedColor() {
    int usage = -1;
    Color color = null;
    for (int i=0; i<seriesColors.length; i++) {
      int seriesUsingColor = getSeriesUsingColor(seriesColors[i]);
      if (usage == -1 || seriesUsingColor < usage) {
        usage = seriesUsingColor;
        color = seriesColors[i]; 
      }
    }

    return color;
  }
  
  /**
   * Count the number of series using the specified color for their series
   * plot.
   * 
   * @param color          the color to find
   * @return               the number of series using this color
   */
  private int getSeriesUsingColor(Color color) {
    if (color == null) {
      return 0;
    }
    
    int count = 0;
    
    for (int i=0; i<dataCollection.getSeriesCount(); i++) {
      Paint p = xyPlot.getRenderer().getSeriesPaint(i);
      if (p.equals(color)) {
        count++;
      }
    }
    
    return count;
  }
  
  /**
   * Set the color for all the series.
   */
  private void setSeriesColors() {
    for (int i=0; i<dataCollection.getSeriesCount(); i++) {
      String series = (String)dataCollection.getSeriesKey(i);
      xyPlot.getRenderer().setSeriesPaint(i, colors.get(series));
    }
  }
  
  /**
   * Update the legend and axis labels based on the series being viewed.
   */
  private void updateLegend() {
    int series = dataCollection.getSeriesCount();
    int chans = channels.size();
    
    if (xyMode) {
      if (series == 0 && chans == 1) {
        domainAxis.setLabel((String)channels.get(0));
        rangeAxis.setLabel(null);
      } else if (series == 1 && chans == 0) {
        XYTimeSeries xySeries = ((XYTimeSeriesCollection)dataCollection).getSeries(0); 
        String seriesName = (String)xySeries.getKey();
        String[] channelNames = seriesName.split(" vs. ");
        if (channelNames.length == 2) {
          domainAxis.setLabel(channelNames[0]);
          rangeAxis.setLabel(channelNames[1]);
        }
      } else if (series == 1 && chans == 2) {
        domainAxis.setLabel((String)channels.get(0));
        rangeAxis.setLabel((String)channels.get(1));
      } else {
        domainAxis.setLabel(null);
        rangeAxis.setLabel(null);        
      }      
    } else {
      if (series == 1) {
        rangeAxis.setLabel((String)channels.get(0));
      } else {
        rangeAxis.setLabel(null);
      }
    }
    
    if (series >= 2) {
      if (chart.getLegend() == null) {
        chart.addLegend(seriesLegend);
      }
    } else {
      if (chart.getLegend() != null) {
        seriesLegend = chart.getLegend();
      }
      chart.removeLegend();      
    }    
  }
	
  /**
   * Get the title of this data panel. This overides the super class
   * implementation to deal with x vs. y plots.
   * 
   * @return  the title of the data panel
   */
	String getTitle() {
		if (xyMode) {
      int remoteSeries = dataCollection.getSeriesCount()-localSeries;
      
      String title = new String();
			Iterator i = channels.iterator();
      while (i.hasNext()) {
        String firstChannel = (String)i.next();
        title += firstChannel;
        if (i.hasNext()) {
          String secondChannel = (String)i.next();
          title += " vs. " + secondChannel;
          if (i.hasNext() || localSeries > 0) {
            title += ", ";
          }          
        }
      }
      
      for (int j=remoteSeries; j<remoteSeries+localSeries; j++) {
        String seriesName = (String)dataCollection.getSeriesKey(j);
        title += seriesName;
        if (j<remoteSeries+localSeries-1) {
          title += ", ";
        }
      }      

      return title;
		} else {
			return super.getTitle();
		}
	}
  
  /**
   * Get the component to display the channels in the header of the data panel.
   * This overides the super class implementation to deal with x vs. y plots.
   * 
   * @return  the component displaying the channels for the data panel
   */
  JComponent getChannelComponent() {
    if (xyMode) {
      int remoteSeries = dataCollection.getSeriesCount()-localSeries;

      if (channels.size() == 0 && localSeries == 0) {
        return null;
      }
      
      JPanel titleBar = new JPanel();
      titleBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      titleBar.setOpaque(false);
      
      if (showChannelsInTitle) {
        Iterator i = channels.iterator();
        while (i.hasNext()) {
          String firstChannel = (String)i.next();
          String series = firstChannel;
          if (i.hasNext()) {
            series += " vs. " + (String)i.next();
          }
          titleBar.add(new ChannelTitle(series, firstChannel));
        }
        
        for (int j=remoteSeries; j<remoteSeries+localSeries; j++) {
          String seriesName = (String)dataCollection.getSeriesKey(j);
          titleBar.add(new LocalChannelTitle(seriesName));
        }
      }
      
      return titleBar;
    } else {
      return super.getChannelComponent();
    }
  }
  
  /**
   * A channel title component for local channels.
   */
  class LocalChannelTitle extends ChannelTitle {
    /**
     * Create a local channel title.
     * 
     * @param seriesName  the name of the series
     */
    public LocalChannelTitle(String seriesName) {
      super(seriesName, seriesName);
    }

    /**
     * Return an actionlistener to remove this series.
     */
    protected ActionListener getActionListener(final String seriesName, final String channelName) {
      return new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          removeLocalSeries(seriesName);
        }
      };
    }    
  }
	
  /**
   * Get the string for this channel to display in the UI. This will show the
   * channel units if there are any.
   *  
   * @param channelName  the name of the channel
   * @return             the string to display the channel in the UI
   */
  private String getChannelDisplay(String channelName) {
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
		
  /**
   * Called when the time scale changes. This updates the maximum age of the
   * dataset.
   * 
   * @param newTimeScale  the new time scale
   */
	public void timeScaleChanged(double newTimeScale) {
		super.timeScaleChanged(newTimeScale);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int series = dataCollection.getSeriesCount();
        if (xyMode) {
          series -= localSeries;
        }
        
    		for (int i=0; i<series; i++) {
    			if (xyMode) {
    				XYTimeSeriesCollection xyTimeSeriesCollection = (XYTimeSeriesCollection)dataCollection;
    				XYTimeSeries data = xyTimeSeriesCollection.getSeries(i);
            data.setMaximumItemAge((int)(timeScale*1000));
    			} else {
    				TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection)dataCollection;
    				TimeSeries data = timeSeriesCollection.getSeries(i);
    				data.setMaximumItemAge((int)(timeScale*1000));
    			}
    		}
        
        if (!xyMode) {
          setTimeAxis();
        }        
      }
    });		
	}
  
  /**
   * Posts new data to the data panel.
   * 
   * @param channelMap  the channel map with the new data
   */
	public void postData(final ChannelMap channelMap) {
    cachedChannelMap = this.channelMap;
    
		super.postData(channelMap);
		
		if (!xyMode) {
			SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
			    postDataTimeSeries(channelMap);
			  }
			});
		}
	}

  /**
   * Posts the data in the channel map when in time series mode.
   * 
   * @param channelMap  the channel map with the new data
   */
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
	
  /**
   * Posts the data in the channel map to the specified channel when in time
   * seires mode.
   * 
   * @param channelMap    the channel map containing the new data
   * @param channelName   the name of the channel to post data to
   * @param channelIndex  the index of the channel in the channel map
   */
	private void postDataTimeSeries(ChannelMap channelMap, String channelName, int channelIndex) {
		TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
    FastTimeSeries timeSeriesData = (FastTimeSeries)dataCollection.getSeries(getChannelDisplay(channelName));
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
	
  /**
   * Posts a new time. This pulls data out of a posted channel map when in x vs.
   * y mode.
   * 
   * @param time  the new time
   */
	public void postTime(double time) {
		super.postTime(time);
		
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (xyMode) {
          postDataXY(channelMap, cachedChannelMap);
        }
        
        setTimeAxis();
      }
    });
	}
  
  /**
   * Posts the data in the channel map when in x vs. y mode.
   * 
   * @param channelMap        the new channel map
   * @param cachedChannelMap  the cached channel map
   */
  private void postDataXY(ChannelMap channelMap, ChannelMap cachedChannelMap) {
    //loop over all channels and see if there is data for them
    int seriesCount = dataCollection.getSeriesCount()-localSeries;
    for (int i=0; i<seriesCount; i++) {
      postDataXY(channelMap, cachedChannelMap, i);
    }
    
    lastTimeDisplayed = time;
  }  

  /**
   * Posts the data in the channel map to the specified channel when in x vs. y
   * mode.
   * 
   * @param channelMap        the new channel map
   * @param cachedChannelMap  the cached channel map
   * @param series            the index of the series
   */
	private void postDataXY(ChannelMap channelMap, ChannelMap cachedChannelMap, int series) {
		if (!xyMode) {
			log.error("Tried to post X vs. Y data when not in xy mode.");
			return;
		}
		
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
		Object[] channelsArray = channels.toArray();
		String xChannelName = (String)channelsArray[series*2];
		String yChannelName = (String)channelsArray[series*2+1];

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
			
			XYTimeSeriesCollection dataCollection = (XYTimeSeriesCollection)this.dataCollection;
      XYTimeSeries xySeriesData = (XYTimeSeries)dataCollection.getSeries(series);
			
			//FIXME assume data of same type
			int typeID = channelMap.GetType(yChannelIndex);
      
      FixedMillisecond time;
					
			chart.setNotify(false);
      
      xySeriesData.startAdd(times.length);
			
			switch (typeID) {
				case ChannelMap.TYPE_FLOAT64:
					double[] xDoubleData = firstXChannelIndex == -1?
					    channelMap.GetDataAsFloat64(xChannelIndex) :
              cachedChannelMap.GetDataAsFloat64(firstXChannelIndex);
					double[] yDoubleData = channelMap.GetDataAsFloat64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xDoubleData[i], yDoubleData[i], false);
					}
					break;
				case ChannelMap.TYPE_FLOAT32:
					float[] xFloatData = firstXChannelIndex == -1?
              channelMap.GetDataAsFloat32(xChannelIndex) :
              cachedChannelMap.GetDataAsFloat32(firstXChannelIndex);
					float[] yFloatData = channelMap.GetDataAsFloat32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xFloatData[i], yFloatData[i], false);
					}
					break;					
				case ChannelMap.TYPE_INT64:
					long[] xLongData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt64(xChannelIndex) :
              cachedChannelMap.GetDataAsInt64(firstXChannelIndex);
					long[] yLongData = channelMap.GetDataAsInt64(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xLongData[i], yLongData[i], false);
					}
					break;
				case ChannelMap.TYPE_INT32:
					int[] xIntData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt32(xChannelIndex) :
              cachedChannelMap.GetDataAsInt32(firstXChannelIndex);
					int[] yIntData = channelMap.GetDataAsInt32(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xIntData[i], yIntData[i], false);
					}
					break;
				case ChannelMap.TYPE_INT16:
					short[] xShortData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt16(xChannelIndex) :
              cachedChannelMap.GetDataAsInt16(firstXChannelIndex);
					short[] yShortData = channelMap.GetDataAsInt16(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xShortData[i], yShortData[i], false);
					}
					break;					
				case ChannelMap.TYPE_INT8:
					byte[] xByteData = firstXChannelIndex == -1?
              channelMap.GetDataAsInt8(xChannelIndex) :
              cachedChannelMap.GetDataAsInt8(firstXChannelIndex);
					byte[] yByteData = channelMap.GetDataAsInt8(yChannelIndex);
					for (int i=startIndex; i<=endIndex; i++) {
            time = new FixedMillisecond((long)(times[i]*1000));
						xySeriesData.add(time, xByteData[i], yByteData[i], false);
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
	
  /**
   * Sets the time axis to display within the current time and time scale. This
   * assumes it is called in the event dispatch thread.
   */
	private void setTimeAxis() {
		if (chart == null) {
			log.warn("Chart object is null. This shouldn't happen.");
			return;
		}

    if (xyMode) {
      XYTimeSeriesCollection xyTimeSeriesCollection = (XYTimeSeriesCollection)dataCollection;
      int series = dataCollection.getSeriesCount()-localSeries;
      for (int i=0; i<series; i++) {
        XYTimeSeries data = xyTimeSeriesCollection.getSeries(i);
        data.removeAgedItems((long)(time*1000));
      }
    } else {
      domainAxis.setRange((time-timeScale)*1000, time*1000);      
    }
	}	
	
  /**
   * Removes all data from all the series.
   */
	void clearData() {
		if (chart == null) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
          lastTimeDisplayed = -1;
          
          int series = dataCollection.getSeriesCount();
          if (xyMode) {
            series -= localSeries;
          }          

				  for (int i=0; i<series; i++) {
					  if (xyMode) {
						  XYTimeSeriesCollection xyTimeSeriesDataCollection = (XYTimeSeriesCollection)dataCollection;
						  XYTimeSeries data = xyTimeSeriesDataCollection.getSeries(i);
						  data.clear();
					  } else {
						  TimeSeriesCollection timeSeriesDataCollection = (TimeSeriesCollection)dataCollection;
						  TimeSeries data = timeSeriesDataCollection.getSeries(i);
						  data.clear();				
					  }
				  }
			  }
		});		
		
		log.info("Cleared data display.");
	}
  
  /**
   * Sets properties for the data panel.
   * 
   * @param key    the key for the property
   * @param value  the value for the property
   */
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
	
  /**
   * Get the name of this data panel.
   */
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
  public class FastTimeSeries extends TimeSeries {
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
   * Represents one timestamped (x,y) data item.
   */
  public class XYTimeSeriesDataItem implements Cloneable, Comparable, Serializable {
    private static final long serialVersionUID = 941152355835111553L;

    /** The time period */
    private final RegularTimePeriod period;
    
    /** The x value */
    private Number x;
    
    /** The y value */
    private Number y;
    
    /**
     * Construct a new data item with the time.
     * 
     * @param period  the time for the data
     */
    public XYTimeSeriesDataItem(RegularTimePeriod period) {
      this(period, null, null);
    }
    
    /**
     * Construct a new data item with timestamped (x,y) values.
     * 
     * @param period  the time for the data
     * @param x       the x value
     * @param y       the y value
     */
    public XYTimeSeriesDataItem(RegularTimePeriod period, Number x, Number y) {
      if (period == null) {
        throw new IllegalArgumentException("Null 'period' argument.");   
      }
      
      this.period = period;
      this.x = x;
      this.y = y;
    }
    
    /**
     * Get a copy of this data item.
     */
    public Object clone() {
      return new XYTimeSeriesDataItem(period, x.doubleValue(), y.doubleValue());
    }

    /**
     * Compare this data item to another. This only compares the time of the
     * data item.
     * 
     * @param o  the data item to compare to
     * @return   an integer indicating the order of this data item relative to
     *           the other
     */
    public int compareTo(Object o) {
      if (o instanceof XYTimeSeriesDataItem) {
        XYTimeSeriesDataItem d = (XYTimeSeriesDataItem)o;
        return period.compareTo(d.getPeriod());
      } else {
        return 1;
      }
    }
    
    /**
     * Test if this object is equal to another.
     * 
     * @return  true if the object is equal, false otherwise
     */
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      
      if (o instanceof XYTimeSeriesDataItem) {
        XYTimeSeriesDataItem d = (XYTimeSeriesDataItem)o;
        return period.equals(d.getPeriod()) && x.equals(d.getX()) && y.equals(d.getY());
      }
      
      return false;
    }
    
    /**
     * Get the hash code for this object.
     */
    public int hashCode() {
      int result;
      result = period.hashCode();
      result = 29 * result + (x != null ? x.hashCode() : 0);
      result = 29 * result + (y != null ? y.hashCode() : 0);
      return result;
    }
    
    /**
     * Get the time period for this data item.
     * 
     * @return  the time period
     */
    public RegularTimePeriod getPeriod() {
      return period;
    }
    
    /**
     * Get the x value.
     * 
     * @return  the x value
     */
    public Number getX() {
      return x;
    }
    
    /**
     * Set the x value.
     * 
     * @param x  the new value of x
     */
    public void setX(Number x) {
      this.x = x;
    }
    
    /**
     * Get the y value.
     * 
     * @return  the y value
     */
    public Number getY() {
      return y;
    }
    
    /**
     * Set the y value
     * @param y  the new value of y
     */
    public void setY(Number y) {
      this.y = y;
    }
    
    /**
     * Set the x and y value.
     * 
     * @param x  the new value of x
     * @param y  the new value of y
     */
    public void setValue(Number x, Number y) {
      this.x = x;
      this.y = y;
    }
  }
  
  /**
   * A sequence of (x,y) data items that are timestamped. Only one data item may
   * is allowed per time.
   */
  public class XYTimeSeries extends Series implements Serializable {
    private static final long serialVersionUID = -5092511186726301050L;

    /**
     * The time period of the series.
     */
    private Class timePeriodClass;
    
    /**
     * The list of data items.
     */
    private ArrayList<XYTimeSeriesDataItem> data;
    
    /**
     * The old age of a data item relative to the newest one or a given time.
     */
    private long maximumItemAge;

    /**
     * Creates an empty series.
     * 
     * @param name             the name of the series
     * @param timePeriodClass  the time period
     */
    public XYTimeSeries(String name, Class timePeriodClass) {
      super(name);
      
      this.timePeriodClass = timePeriodClass;
      
      data = new ArrayList<XYTimeSeriesDataItem>();
      maximumItemAge = Long.MAX_VALUE;
    }
    
    /**
     * Get the time period for this series.
     * 
     * @return  the class of the time period
     */
    public Class getTimePeriodClass() {
      return timePeriodClass;
    }
    
    /**
     * Call to optimize the addition of large amount of data items.
     * 
     * @param items  the number of item that will be added.
     */
    public void startAdd(int items) {
      data.ensureCapacity(data.size()+items);
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
    
    /**
     * Add a data item and notify series change listeners.
     * 
     * @param period  the period of the data item
     * @param x       the x value of the data item
     * @param y       the y value of the data item.
     */
    public void add(RegularTimePeriod period, Number x, Number y) {
     add(period, x, y, true); 
    }

    /**
     * Add a data item. Optionally notify series change listeners.
     * 
     * @param period  the period of the data item
     * @param x       the x value of the data item
     * @param y       the y value of the data item
     * @param notify  if true notify series change listeners
     */
    public void add(RegularTimePeriod period, Number x, Number y, boolean notify) {
      add(new XYTimeSeriesDataItem(period, x, y), notify);
    }
    
    /**
     * Add the data item and notify series change listeners.
     * 
     * @param item  the data item to add
     */
    public void add(XYTimeSeriesDataItem item) {
      add(item, true);
    }
    
    /**
     * Add the data item. Optionally notify series change listeners.
     * 
     * @param item    the data item to add
     * @param notify  if true notify series change listeners
     */
    public void add(XYTimeSeriesDataItem item, boolean notify) {
      if (item == null) {
        throw new IllegalArgumentException("Null 'item' argument.");
      }
      
      if (!item.getPeriod().getClass().equals(timePeriodClass)) {
        throw new SeriesException("Invalid time period class for this series.");
      }
      
      boolean added = false;
      int count = getItemCount();
      if (count == 0) {
        data.add(item);
        added = true;
      } else {
        RegularTimePeriod last = this.getDataItem(count-1).getPeriod();
        if (item.getPeriod().compareTo(last) > 0) {
          data.add(item);
          added = true;
        }
      }
      
      if (added) {
        if (notify) {
          fireSeriesChanged();
        }
      }
    }
    
    /**
     * Update the values at the time period.
     * 
     * @param period  the time period to update
     * @param x       the new x value
     * @param y       the new y value
     */
    public void update(RegularTimePeriod period, Number x, Number y) {
      XYTimeSeriesDataItem item = getDataItem(period);
      if (item != null) {
        item.setValue(x, y);
        
        fireSeriesChanged();
      } else {
        throw new SeriesException("Period does not exist.");
      }
    }
    
    /**
     * Delete the data item at the time period.
     * 
     * @param period  the time period at which to delete the data item
     */
    public void delete(RegularTimePeriod period) {
      int index = getIndex(period);
      data.remove(index);
      fireSeriesChanged();
    }

    /**
     * Remove all data items from the series.
     */
    public void clear() {
      if (data.size() > 0) {
        data.clear();
        fireSeriesChanged();
      }
    }
    
    /**
     * Get the data item at the specified index.
     * 
     * @param index  the index at which the data item is located
     * @return       the data item, or null if there is none at the index
     */
    public XYTimeSeriesDataItem getDataItem(int index) {
      return data.get(index);
    }
    
    /**
     * Get the data item at the specified time.
     * 
     * @param period  the time of the data item
     * @return        the data item or null if there is no data item at the time
     */
    public XYTimeSeriesDataItem getDataItem(RegularTimePeriod period) {
      int index = getIndex(period);
      if (index >= 0) {
        return data.get(index);
      } else {
        return null;
      }
    }
    
    /**
     * Get the index of the data item at the specified time.
     * 
     * @param period  the time to look for the data item
     * @return        the index of the data item, or a negative number if not
     */
    public int getIndex(RegularTimePeriod period) {
      if (period == null) {
        throw new IllegalArgumentException("Null 'period' argument");
      }
      
      XYTimeSeriesDataItem dummy = new XYTimeSeriesDataItem(period);
      return Collections.binarySearch(data, dummy);
    }
    
    /**
     * Get the number of data items in this series.
     * 
     * @return  the number of data items in this series
     */
    public int getItemCount() {
      return data.size();
    }
    
    /**
     * Get a read-only list of the data item in this series.
     * 
     * @return  a list of data item in this series
     */
    public List<XYTimeSeriesDataItem> getItems() {
      return Collections.unmodifiableList(data);
    }
    
    /**
     * Get the maximum ago of a data item.
     * 
     * @return  the maximum age
     */
    public long getMaximumItemAge() {
      return maximumItemAge;
    }
    
    /**
     * Set the maximum ago of a data item.
     * 
     * @param periods  the maximum age
     */
    public void setMaximumItemAge(long periods) {
      if (periods < 0) {
        throw new IllegalArgumentException("Negative 'periods' argument.");
      }
      maximumItemAge = periods;
      removeAgedItems();       
    }
    
    /**
     * Remove data items that exceed the maximum age and notify series change
     * listeners if any data items are aged.
     */
    public void removeAgedItems() {
      removeAgedItems(true);
    }
    
    /**
     * Remove data items that exceed the maximum age. Optionally notify series
     * change listeners if any data items are aged.
     * 
     * @param notify  if true notify series change listeners
     */
    public void removeAgedItems(boolean notify) {
      int items = getItemCount(); 
      if (items > 0) {
        removeAgedItems(getDataItem(items-1).getPeriod().getSerialIndex(), notify);
      }
    }
    
    /**
     * Remove data items that exceed the maximum age starting at the given time.
     * Notify series change listeners if any data items are aged.
     * 
     * @param latest  the time to start at
     */
    public void removeAgedItems(long latest) {
      removeAgedItems(latest, true);
    }
    
    /**
     * Remove data items that exceed the maximum age starting at the given time.
     * Optionally notify series change listeners if any data items are aged.
     * 
     * @param latest  the time to start at
     * @param notify  if true notify series change listenerss
     */
    public void removeAgedItems(long latest, boolean notify) {
      boolean removed = false;
      
      long minimumItemAge = latest - maximumItemAge;
      while (getItemCount() > 0 && getDataItem(0).getPeriod().getSerialIndex() < minimumItemAge) {
        data.remove(0);
        removed = true;
      }
      
      if (notify && removed) {
        fireSeriesChanged();
      }
    }
  }
  
  /**
   * A collection of XYTimeSeries objects that form a dataset. 
   */
  public class XYTimeSeriesCollection extends AbstractXYDataset implements Serializable {
    private static final long serialVersionUID = 4352896561682820035L;
    
    /** List of series */
    private List<XYTimeSeries> data;
    
    /**
     * Create an empty dataset.
     */
    public XYTimeSeriesCollection() {
      super();
      
      data = new ArrayList<XYTimeSeries>();
    }

    /**
     * Get the number of data series in this collection
     * 
     * @return  the number of data series
     */
    public int getSeriesCount() {
      return data.size();
    }
    
    /**
     * Get a list of all data series in this collection. This list is read-only.
     * 
     * @return  a list of data series
     */
    public List<XYTimeSeries> getSeries() {
      return Collections.unmodifiableList(this.data);
    }    
    
    /**
     * Get the data series at the specified index.
     * 
     * @param series  the index of the series
     * @return        if found, the series, null otherwise
     */
    public XYTimeSeries getSeries(int series) {
      if ((series < 0) || (series >= getSeriesCount())) {
        throw new IllegalArgumentException("The 'series' argument is out of bounds (" + series + ").");
      }

      return data.get(series);      
    }
    
    /**
     * Get the data series with the specified key.
     * 
     * @param key  the key to the data series
     * @return     if foud, the data series, null otherwise
     */
    public XYTimeSeries getSeries(String key) {
      for (XYTimeSeries xyTimeSeries : data) {
        Comparable k = xyTimeSeries.getKey();
        if (k != null && k.equals(key)) {
          return xyTimeSeries;
        }
      }
      
      return null;
    }

    /**
     * Get the key for the specified series index.
     * @param key  the index to the data series
     * @return     if found, the key for the series, null otherwise
     */
    public Comparable getSeriesKey(int series) {
      return getSeries(series).getKey();
    }
    
    /**
     * Add the series to the collection.
     * 
     * @param series  the series to add
     */
    public void addSeries(XYTimeSeries series) {
      addSeries(getSeriesCount(), series);
    }
    
    /**
     * Add the series to the collection at the specified index.
     * 
     * @param index   the index at which to add the series
     * @param series  the series to add
     */
    public void addSeries(int index, XYTimeSeries series) {
      if (series == null) {
        throw new IllegalArgumentException("Null 'series' argument.");
      }
      data.add(index, series);
      series.addChangeListener(this);
      fireDatasetChanged();      
    }
    
    /**
     * Remove the series from the collection.
     * 
     * @param series  the series to remove
     */
    public void removeSeries(XYTimeSeries series) {
      if (series == null) {
        throw new IllegalArgumentException("Null 'series' argument.");
      }
      data.remove(series);
      series.removeChangeListener(this);
      fireDatasetChanged();      
    }
    
    /**
     * Remove the series, specified by the, index from the collection.
     * 
     * @param index  the index of the series
     */
    public void removeSeries(int index) {
      XYTimeSeries series = getSeries(index);
      if (series != null) {
        removeSeries(series);
      }
    }

    /**
     * Get the number of data items in the specified series
     * 
     * @param series  the index to the data series
     */
    public int getItemCount(int series) {
      return getSeries(series).getItemCount();
    }

    /**
     * Get the x value of the series at the specified data item.
     * 
     * @param series  the index of the data series
     * @param item    the index of the data item
     * @return        the x value
     */
    public Number getX(int series, int item) {
      XYTimeSeries xyTimeSeries = getSeries(series);
      return xyTimeSeries.getDataItem(item).getX();
    }

    /**
     * Get the x yalue of the series at the specified data item.
     * 
     * @param series  the index of the data series
     * @param item    the index of the data item
     * @return        the y value
     */
    public Number getY(int series, int item) {
      XYTimeSeries xyTimeSeries = getSeries(series);
      return xyTimeSeries.getDataItem(item).getY();
    }
  }
}