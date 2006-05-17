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
 * $URL: https://svn.nees.org/svn/telepresence/RDV/trunk/src/org/nees/buffalo/rdv/datapanel/TabularDataPanel.java $
 * $Revision: 1361 $
 * $Date: 2006-04-24 21:17:05 +0000 (Mon, 24 Apr 2006) $
 * $Author: ljmiller $
 */

package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.rbnb.sapi.ChannelMap;

/**
 * A Data Panel extension to display numeric data in a tabular form. Maximum and
 * minimum values are also displayed. The design of this datapanel is motivated
 * by the "Digital Display" program of the UMN MAST DAQ system.
 * 
 * @author  Jason P. Hanley
 * @author  Lawrence J. Miller <ljmiller@sdsc.edu>
 * @since   1.3
 */
public class DigitalTabularDataPanel extends AbstractDataPanel implements TableModelListener {

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
	public DigitalTabularDataPanel() {
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
    // XXX
    table.getModel ().addTableModelListener (this);
    table.addMouseListener (
          new MouseAdapter () {
                public void mouseClicked (MouseEvent e) {
                   JTable src = (JTable)(e.getComponent());
                   log.debug ("^^^ CLICK!! from " + src.getSelectedRow () + "x" + src.getSelectedColumn () );
                } // mouseClicked ()
          }); // MouseListener
    table.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateRowHeight();
      }
    });
    // XXX
    setUpUnitsColumn (table, table.getColumnModel ().getColumn (5));
    
    /*JScrollPane scrollPane = new JScrollPane (table);
    mainPanel.add (scrollPane);*/
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
      
      // XXX try color changes here
      int dataCellRowNumber = tableModel.getRowNumber (channelName);
      Object col1data = tableModel.getValueAt (dataCellRowNumber, 1);
      Object col3data = tableModel.getValueAt (dataCellRowNumber, 3);
      double loThresh, hiThresh;
      
      DefaultTableCellRenderer dataCellRenderer = (DefaultTableCellRenderer)( table.getCellRenderer (dataCellRowNumber, 2) );
      
      if ( col1data.getClass().isInstance (new Double (-1.0)) ) {
         log.debug ("^^^ col1's a Double");
         loThresh = ( (Double)(tableModel.getValueAt (dataCellRowNumber, 1)) ).doubleValue ();
         if (data < loThresh) { // set the cell color
            dataCellRenderer.setBackground (Color.red);
         } else { // clear the cell color
            dataCellRenderer.setBackground (null);
         }
      }
      
      if (col3data.getClass().isInstance (new Double (-1.0)) )  {
         log.debug ("^^^ col3's a Double");
         hiThresh = ( (Double)(tableModel.getValueAt (dataCellRowNumber, 3)) ).doubleValue ();
         if (hiThresh < data) { // set the cell color
            dataCellRenderer.setBackground (Color.red);
         } else { //  clear the cell color
            dataCellRenderer.setBackground (null);
         }
      }
      tableModel.updateData (channelName, data);
    }
	}
	
	public String toString() {
		return "Digital Data Panel";
	}
  
   
   /** method for the @see TableModelListener interface */
   public void tableChanged (TableModelEvent e) {
       int row = e.getFirstRow ();
       int column = e.getColumn ();
       TableModel model = (TableModel)e.getSource ();
       Object data = model.getValueAt (row, column);
       // String columnName = model.getColumnName (column);
       if (true) /*column==1 || column==3)*/ {
          log.debug ("^^^ tableChange at: " + Integer.toString (row) + ", " + Integer.toString (column));
          log.debug ("^^^ data: " + data);
       }
   } // tableChanged ()
 
   
 /** XXX a method to implement a custom cell editor or the units column */
 private void setUpUnitsColumn (JTable table, TableColumn unitsColumn)
 {
    String[] unitTypes = new String[] {"engineering", "decimal", "other"};
    JComboBox comboBox = new JComboBox();
    for (int i=0; i<unitTypes.length; i++) {
       comboBox.addItem (unitTypes[i]);
    } //for
    unitsColumn.setCellEditor (new DefaultCellEditor (comboBox));
 } // setUpUnitsColumn ()

 
 /** XXX a method that will flag cell contents that are outside of threshold ranges */
 private void setUpDataColumn (JTable table, TableColumn dataColumn)
 {
    /*TableCellRenderer dataCellRenderer = dataColumn.getCellRenderer ();
    // XXX change color for threshold
    /*if (data < dataRow.minThresh || dataRow.maxThresh < data) {
       dataRow.getCellRenderer.setBackground (Color.red);
    }*/
 } // setUpDataColumn ()
 
 
  /** An innner class to implement a custom data madel for this JTable */ 
  private class DataTableModel extends AbstractTableModel {
    private String[] columnNames = {
        "Name",
        "Min Threshold",
        "Value",
        "Max Threshold",
        "Unit",
        "Unit Rpresentation",
        "Min",
        "Max"};
    
    private ArrayList rows;
    
    private boolean cleared;

    public boolean isCellEditable (int row, int col) {
       return (col==1 || col==3 || col==5); 
    }
    
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
    
    /** XXX a method that will @return the row index number
     * given @param row name*/
    public int getRowNumber (String name) {
       DataRow dataRow = null;
       for (int i=0; i<rows.size(); i++) {
          dataRow = (DataRow)rows.get(i);
          if (dataRow.name.equals(name)) {
             return i;
          } // if
       } // for
       return -1; // name not found
    } // getRowNumber ()
    
    
    /** A method that will update table data in response to user actions */
    public void setValueAt (Object value, int row, int col) {
        log.debug ("^^^ Setting value at " + row + "," + col + " to " + value);
        //data[row][col] = value;
        // get the right row and then jump to the right column
        DataRow dataRow = (DataRow)rows.get (row);
        switch (col) {
          case 1:
             dataRow.minThresh = Double.parseDouble ((String)value);
             fireTableCellUpdated (row, col);
             // XXX change color for thresholds
             break;
          case 3:
             dataRow.maxThresh = Double.parseDouble ((String)value);
             fireTableCellUpdated (row, col);
             break;
          case 5:
             dataRow.unitRepresentation = (String)value;
             fireTableCellUpdated (row, col);
             break;
          default:
            return; // do nothing
        } // switch
    } // setValueAt ()
    
    
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
           return new Double (dataRow.minThresh);
        case 2:
          return new Double(dataRow.value);
        case 3:
           return new Double (dataRow.maxThresh);
        case 4:
          return dataRow.unit;
        case 5:
           return dataRow.unitRepresentation;
        case 6:
          return new Double(dataRow.min);
        case 7:
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
    double minThresh = -1;
    double maxThresh = -1;
    String unitRepresentation = "decimal";
    
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
    
    public boolean isOverHighThreshold () {
       return (this.maxThresh < this.value);
    } // isOverHighThreshold ()

    public boolean isUnderLowThreshold () {
       return (this.value < this.minThresh);
    } // isUnderLowThreshold ()
  }
}
