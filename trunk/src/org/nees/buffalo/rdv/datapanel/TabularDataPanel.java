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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
   * The main panel
   * 
   * @since  1.3
   */
  private JPanel mainPanel;
  
  /**
   * The data model for the table
   * 
   * @since  1.3
   */
	private DataTableModel tableModel;
  
  /**
   * The table
   * 
   * @since  1.3
   */
  private JTable table;

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
    
    lastTimeDisplayed = -1;
    
    initComponents();
    
    setDataComponent(mainPanel);
	}
	
  /**
   * Initialize the container and adds the header.
   * 
   * @since  1.2
   */
	private void initComponents() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    
    tableModel = new DataTableModel();
    table = new JTable(tableModel);
    table.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateRowHeight();
      }
    });
    
    mainPanel.add(table.getTableHeader(), BorderLayout.NORTH);
    mainPanel.add(table, BorderLayout.CENTER);
	}
  

	void clearData() {
    tableModel.clearData();
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
		
		tableModel.addRow(channelName, unit);
    updateRowHeight();
    
		return true;
	}
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
		tableModel.deleteRow(channelName);
    updateRowHeight();
    
		return true;
	}
  
  private void updateRowHeight() {
    if (channels.size() > 0) {
      table.setRowHeight(table.getHeight()/channels.size());
    }
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
        postDataTabular(channelName, channelIndex);
			}
		}
        
    lastTimeDisplayed = time;
	}
	
	private void postDataTabular(String channelName, int channelIndex) {
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
      	return;
      }

      tableModel.updateData(channelName, data);
    }
	}
	
	public String toString() {
		return "Tabular Data Panel";
	}
  
  private class DataTableModel extends AbstractTableModel {
    private String[] columnNames = {
        "Name",
        "Value",
        "Unit",
        "Min",
        "Max"};
    
    private ArrayList rows;
    
    private boolean cleared;
    
    public DataTableModel() {
      super();
      rows = new ArrayList();
      cleared = true;
    }
    
    public void addRow(String name, String unit) {
      DataRow dataRow = new DataRow(name, unit);
      rows.add(dataRow);
      int row = rows.size()-1;
      fireTableRowsInserted(row, row);
    }
    
    public void deleteRow(String name) {
      for (int i=0; i<rows.size(); i++) {
        DataRow dataRow = (DataRow)rows.get(i);
        if (dataRow.name.equals(name)) {
          rows.remove(dataRow);
          this.fireTableRowsDeleted(i, i);
          break;
        }
      }
    }
    
    public void updateData(String name, double data) {
      cleared = false;
      for (int i=0; i<rows.size(); i++) {
        DataRow dataRow = (DataRow)rows.get(i);
        if (dataRow.name.equals(name)) {
          dataRow.setData(data);
          fireTableDataChanged();
          break;
        }
      }
    }
    
    public void clearData() {
      cleared = true;
      for (int i=0; i<rows.size(); i++) {
        DataRow dataRow = (DataRow)rows.get(i);
        dataRow.clearData();
      }
      fireTableDataChanged();
    }

    public int getRowCount() {
      return rows.size();
    }

    public int getColumnCount() {
      return columnNames.length;
    }
    
    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      if (col != 0 && cleared) {
        return new String();
      }
      
      DataRow dataRow = (DataRow)rows.get(row);
      switch (col) {
        case 0:
          return dataRow.name;  
        case 1:
          return new Double(dataRow.value);
        case 2:
          return dataRow.unit;
        case 3:
          return new Double(dataRow.min);
        case 4:
          return new Double(dataRow.max);
        default:
          return null;
      }
    }    
  }
  
  private class DataRow {
    String name;
    String unit;
    double value;
    double min;
    double max;
    
    public DataRow(String name, String unit) {
      this.name = name;
      this.unit = unit;
      clearData();
    }
    
    public void setData(double data) {
      value = data;
      min = min==-1? data:Math.min(min, data);
      max = max==-1? data:Math.max(max, data);
    }
    
    public void clearData() {
      value = 0;
      min = -1;
      max = -1;      
    }
  }
}
