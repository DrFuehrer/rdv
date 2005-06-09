/*
 * Created on Mar 23, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rbnb.sapi.ChannelMap;

/**
 * A Data Panel extension to display numeric data in a tabular form. Maximum and
 * minimum values are also displayed.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class TabularDataPanel extends AbstractDataPanel {

  /**
   * The container for all the channel rows.
   * 
   * @since  1.2
   */
	private JPanel tabularDataPanel;
    
  /**
   * The layout manager for the container.
   * 
   * @since  1.2
   */
	private GridLayout gridLayout;
	
  /**
   * A hshtable to lookup the TabularDataRow for a channel.
   * 
   * @since  1.2
   */
	private Hashtable rows;

  /**
   * The last time data was displayed in the UI
   * 
   * @since  1.2
   */
  double lastTimeDisplayed;

	
  /**
   * Initialize the data panel.
   * 
   * @since  1.2
   */
	public TabularDataPanel() {
    super();
    
    rows = new Hashtable();
    
    lastTimeDisplayed = -1;
    
    initComponents();
    
    setDataComponent(tabularDataPanel);
	}
	
  /**
   * Initialize the container and adds the header.
   * 
   * @since  1.2
   */
	private void initComponents() {
		tabularDataPanel = new JPanel();
		gridLayout = new GridLayout(1, 1);
		tabularDataPanel.setLayout(gridLayout);
    TabularDataRow header = new TabularDataRowHeader();
    tabularDataPanel.add(header);
	}

	void clearData() {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			TabularDataRow row = (TabularDataRow)rows.get(channelName);
			row.clearData();
		}
    lastTimeDisplayed = -1;
	}

	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public boolean addChannel(String channelName) {
		if (!super.addChannel(channelName)) {
			return false;
		}
			
		String labelText = channelName;
		
		String unit = (String)units.get(channelName);
		if (unit != null) {
			labelText += "(" + unit + ")";
		}
		
		gridLayout.setRows(gridLayout.getRows()+1);
		TabularDataRow row = new TabularDataRow(labelText);
		tabularDataPanel.add(row);
		rows.put(channelName, row);
		
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
		TabularDataRow row = (TabularDataRow)rows.get(channelName);
		rows.remove(channelName);
		tabularDataPanel.remove(row);
		
		return true;
	}
	
	public void postTime(double time) {
		super.postTime(time);
		
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
    double lastTime = lastTimeDisplayed;
        
		//loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);

      //if there is data for channel, post it
			if (channelIndex != -1) {
        double channelLastTime = postDataTabular(channelName, channelIndex);
        if (channelLastTime != -1) {
        	Math.max(lastTime, channelLastTime);
        }
			}
		}
        
    lastTimeDisplayed = lastTime;
	}
	
	private double postDataTabular(String channelName, int channelIndex) {
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
        return -1;
    }       
    
    int endIndex = startIndex;
    
    for (int i=times.length-1; i>startIndex; i--) {
        if (times[i] <= time) {
            endIndex = i;
            break;
        }
    }
            
    for (int i=startIndex; i<=endIndex; i++) {
      double data;
    
      int typeID = channelMap.GetType(channelIndex);
    
      switch (typeID) {
      case ChannelMap.TYPE_FLOAT64:					
      	data = channelMap.GetDataAsFloat64(channelIndex)[i];
    	  break;
      case ChannelMap.TYPE_FLOAT32:
      	data = channelMap.GetDataAsFloat32(channelIndex)[i];
      	break;					
      case ChannelMap.TYPE_INT64:
      	data = channelMap.GetDataAsInt64(channelIndex)[i];
      	break;
      case ChannelMap.TYPE_INT32:
      	data = channelMap.GetDataAsInt32(channelIndex)[i];
      	break;
      case ChannelMap.TYPE_INT16:
      	data = channelMap.GetDataAsInt16(channelIndex)[i];
      	break;					
      case ChannelMap.TYPE_INT8:					
      	data = channelMap.GetDataAsInt8(channelIndex)[i];
      	break;					
      case ChannelMap.TYPE_STRING:
      case ChannelMap.TYPE_UNKNOWN:
      case ChannelMap.TYPE_BYTEARRAY:
      default:
      	return -1;
      }

      TabularDataRow row = (TabularDataRow)rows.get(channelName);
      row.setData(data);
    }
    
    return times[endIndex];
	}
	
	public String toString() {
		return "Tabular Data Panel";
	}
	
	class TabularDataRow extends JPanel {
		JLabel label;
		JLabel data;
		JLabel min;
		JLabel max;
		
		double minData;
		double maxData;
		
		boolean gotData;
        
		public TabularDataRow(String labelText) {
			this(labelText, null);
		}
			
		public TabularDataRow(String labelText, String dataText) {
			label = new JLabel(labelText);
			data = new JLabel(dataText);
			min = new JLabel();
			max = new JLabel();
			
			setLayout(new GridLayout(1,4));
			add(label);
			add(data);
			add(min);
			add(max);
			
			gotData = false;
		}
		
		public void setLabel(String labelText) {
			label.setText(labelText);
		}
		
		public void setData(double value) {
			if (!gotData) {
				gotData = true;
				minData = maxData = value;
			} else {
				minData = Math.min(minData, value);
				maxData = Math.max(maxData, value);
			}
			
			String dataText = Double.toString(value);
			data.setText(dataText);
			min.setText(Double.toString(minData));
			max.setText(Double.toString(maxData));
		}
		
    public void clearData() {
			gotData = false;
			data.setText(null);
			min.setText(null);
			max.setText(null);
		}
 	}
    
    public class TabularDataRowHeader extends TabularDataRow {
    	public TabularDataRowHeader() {
    		super("Channel Name");
        data.setText("Data");
        min.setText("Minimum");
        max.setText("Maximum");
    	}
    }
}
