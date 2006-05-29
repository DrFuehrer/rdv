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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.rbnb.sapi.ChannelMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A Data Panel extension to display numeric data in a tabular form. Maximum and
 * minimum values are also displayed.
 * 
 * @author  Jason P. Hanley
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @since   1.3
 */
public class DigitalTabularDataPanel extends AbstractDataPanel implements TableModelListener {

	static Log log = LogFactory.getLog(DigitalTabularDataPanel.class.getName());
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
   * The scroll pane for the table
   */
  private JScrollPane tableScrollPane;
  /**
   * The cell renderer for the data
   * 
   * @since  1.3
   */
  private DoubleTableCellRenderer doubleCellRenderer;
  //ÊDOTOO get this from each row
  private DataTableCellRenderer dataCellRenderer;
  
  /**
   * The button to set decimal data rendering
   */
  private JToggleButton decimalButton;
  
  /**
   * The button to set engineering data renderering
   */
  private JToggleButton engineeringButton;

  /**
   * The check box menu item to control max/min column visibility
   */
  private JCheckBoxMenuItem showMaxMinMenuItem;
  
  /** LJM 060523
   * The check box menu item to control threshold column visibility
   */
  private JCheckBoxMenuItem showThresholdMenuItem;

  /**
   * The last time data was displayed in the UI
   * 
   * @since  1.2
   */
  double lastTimeDisplayed;

  /**
   * The minimum height (in pixels) for a row
   */
  private static final int MIN_ROW_HEIGHT = 12;
	
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
   * Initialize the container and add the header.
   * 
   * @since  1.2
   */
	private void initComponents() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    
    tableModel = new DataTableModel();
    table = new JTable(tableModel);
    
    // TODO
    table.setDragEnabled (true);
    
    table.getModel ().addTableModelListener (this);
    table.addMouseListener (
          new MouseAdapter () {
                public void mouseClicked (MouseEvent e) {
                   JTable src = (JTable)(e.getComponent());
                   log.debug ("^^^ CLICK!! from " + src.getSelectedRow () + "x" + src.getSelectedColumn () );
                } // mouseClicked ()
          }); // MouseListener
 
    // DOTOO get the rows and add from each one
    dataCellRenderer = new DataTableCellRenderer ();
    // table.getColumn ("Value").setCellRenderer (dataCellRenderer);
    doubleCellRenderer = new DoubleTableCellRenderer ();
    table.getColumn ("Value").setCellRenderer (dataCellRenderer);

    table.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateRowHeight();
      }
    });
    
    tableScrollPane = new JScrollPane(table);
    tableScrollPane.getViewport().setBackground(Color.white);
    
    mainPanel.add(tableScrollPane, BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BorderLayout());
    
    JPanel offsetsPanel = new JPanel();
    
    final JCheckBox useOffsetsCheckBox = new JCheckBox("Use Offsets");
    useOffsetsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boolean checked = ((JCheckBox)ae.getSource()).isSelected();
        tableModel.useOffsets(checked);
        table.repaint();
      }      
    });
    offsetsPanel.add(useOffsetsCheckBox);

    JButton takeOffsetsButton = new JButton("Take Offsets");
    takeOffsetsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tableModel.takeOffsets();
        tableModel.useOffsets(true);
        useOffsetsCheckBox.setSelected(true);
        table.repaint();
      }      
    });
    offsetsPanel.add(takeOffsetsButton);
    
    buttonPanel.add(offsetsPanel, BorderLayout.WEST);
    
    JPanel formatPanel = new JPanel();
    
    decimalButton = new JToggleButton("Decimal", true);
    decimalButton.setSelected(true);
    decimalButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useEngineeringRenderer(false);
      }
    });
    formatPanel.add(decimalButton);

    engineeringButton = new JToggleButton("Engineering");
    engineeringButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useEngineeringRenderer(true);
      }
    });
    formatPanel.add(engineeringButton);
    
    ButtonGroup formatButtonGroup = new ButtonGroup();
    formatButtonGroup.add(decimalButton);
    formatButtonGroup.add(engineeringButton);
    
    buttonPanel.add(formatPanel, BorderLayout.EAST);
    
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);    
    
    // popup menu for panel
    JPopupMenu popupMenu = new JPopupMenu();
    
    final JMenuItem copyMenuItem = new JMenuItem("Copy");
    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
        copyMenuItem.setEnabled(table.getSelectedRowCount() > 0);
      }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
      public void popupMenuCanceled(PopupMenuEvent arg0) {}
    });
    copyMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        TransferHandler.getCopyAction().actionPerformed(
            new ActionEvent(table, ae.getID(),ae.getActionCommand(),
                ae.getWhen(), ae.getModifiers()));
      }
    });
    popupMenu.add(copyMenuItem);
    
    popupMenu.addSeparator();
    
    JMenuItem printMenuItem = new JMenuItem("Print...");
    printMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          table.print(JTable.PrintMode.FIT_WIDTH);
        } catch (PrinterException pe) {}
      }      
    });
    popupMenu.add(printMenuItem);
    
    popupMenu.addSeparator();
    
    showMaxMinMenuItem = new  JCheckBoxMenuItem("Show max/min and threshold columns", false);
    showMaxMinMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setMaxMinVisible(showMaxMinMenuItem.isSelected());
      }      
    });
    popupMenu.add(showMaxMinMenuItem);
    
    // LJM 060523
    showThresholdMenuItem = new  JCheckBoxMenuItem ("Show threshold columns", false);
    showThresholdMenuItem.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent ae) {
        setThresholdVisible (showThresholdMenuItem.isSelected ());
      }      
    }); // actionListener
    // TODO
    //popupMenu.add (showThresholdMenuItem);

    // set component popup and mouselistener to trigger it
    mainPanel.setComponentPopupMenu(popupMenu);
    table.setComponentPopupMenu(popupMenu);
    mainPanel.addMouseListener(new MouseInputAdapter() {});
	}
  
  private void useEngineeringRenderer(boolean useEngineeringRenderer) {
    doubleCellRenderer.setShowEngineeringFormat(useEngineeringRenderer);
    table.repaint();
    if (useEngineeringRenderer) {
      engineeringButton.setSelected(true);
      properties.setProperty("renderer", "engineering");
    } else {
      decimalButton.setSelected(true);
      properties.remove("renderer");
    }
  }

  private void setMaxMinVisible(boolean maxMinVisible) {
    if (tableModel.getMaxMinVisibile() != maxMinVisible) {
      tableModel.setMaxMinVisible(maxMinVisible);
      showMaxMinMenuItem.setSelected(maxMinVisible);
      
      table.getColumn ("Value").setCellRenderer (dataCellRenderer);
      //table.getColumn ("Value").setCellRenderer (doubleCellRenderer);
      
      if (maxMinVisible) {
        table.getColumn("Min").setCellRenderer(doubleCellRenderer);
        table.getColumn("Max").setCellRenderer(doubleCellRenderer);

        properties.setProperty("maxMinVisible", "true");
      } else {
        properties.remove("maxMinVisible");
      }
    }
  }

  // LJM 060523
  private void setThresholdVisible (boolean thesholdVisible) {
     if (tableModel.getThresholdVisible () != thesholdVisible) {
       tableModel.setThresholdVisible (thesholdVisible);
       showMaxMinMenuItem.setSelected (thesholdVisible);
       
       //table.getColumn ("Value").setCellRenderer (dataCellRenderer);
       // table.getColumn ("Value").setCellRenderer (doubleCellRenderer);
       
       if (thesholdVisible) {
         table.getColumn ("Min Thresh").setCellRenderer (doubleCellRenderer);
         table.getColumn ("Max Thresh").setCellRenderer (doubleCellRenderer);

         properties.setProperty ("thresholdVisible", "true");
       } else {
         properties.remove ("thresholdVisible");
       }
     } // if
   } // setThresholdVisible ()
  
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
		
      String lowerThresholdString = (String)( lowerThresholds.get (channelName) );
      String upperThresholdString = (String)( upperThresholds.get (channelName) );
      
      // +/- Double.MAX_VALUE represents empty thresholds
      double lowerThresholdTemp = -1 * Double.MAX_VALUE;
      double upperThresholdTemp = Double.MAX_VALUE;
      
      // handle errors generated by daq - Unknown command 'list-lowerbounds'     
      if (lowerThresholdString != null) {
         try {
            lowerThresholdTemp = Double.parseDouble (lowerThresholdString);
         } catch (java.lang.NumberFormatException nfe) {
            log.warn ("Non-numeric lower threshold in metadata: " + lowerThresholdString);
         }
      } // if
      if (upperThresholdString != null) {
         try {
            upperThresholdTemp = Double.parseDouble (upperThresholdString);
         } catch (java.lang.NumberFormatException nfe) {
            log.warn ("Non-numeric upper threshold in metadata: " + upperThresholdString);
         }
      } // if
      
		tableModel.addRow (channelName, unit, lowerThresholdTemp, upperThresholdTemp);
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
      int rowHeight = tableScrollPane.getViewport().getHeight()/channels.size();
      if (rowHeight < MIN_ROW_HEIGHT) {
        rowHeight = MIN_ROW_HEIGHT;
      }
      table.setRowHeight(rowHeight);
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
      } // switch
      tableModel.updateData(channelName, data);
      this.flagThreshold (tableModel.getRowNumber (channelName));
     } // for 
	} // postDataTabular ()
   
   
  /** a method that will set the out of threshold flag by looking at the table's data value */
 public void flagThreshold (int dataRow) {
    // changed to access data model directly because the treshold columns may not be visible; must emulate getValueAt ()  
    DataRow theRowAtDataRow = tableModel.getRowAt (dataRow); 
    Object minThreshData = (theRowAtDataRow.minThresh == (-1 * Double.MAX_VALUE))? null : new Double (theRowAtDataRow.minThresh);
    Object maxThreshData = (theRowAtDataRow.minThresh == Double.MAX_VALUE)? null : new Double (theRowAtDataRow.maxThresh);
    
    Object dataData = tableModel.getValueAt (dataRow, tableModel.findColumn ("Value"));
    double loThresh;
    double hiThresh;
    double data;
    if (minThreshData!=null && maxThreshData!=null && dataData!=null) {    
       if (isADouble (minThreshData) && isADouble (maxThreshData) && isADouble (dataData)) {   
          loThresh = ((Double)minThreshData).doubleValue ();
          hiThresh = ((Double)maxThreshData).doubleValue ();
          data = ((Double)dataData).doubleValue ();
          if (data < loThresh) { // indicate it on the table
             log.debug("^^^ LOW " + data + " < " + loThresh);
          } else if (hiThresh < data) { // indicate it on the table
             log.debug("^^^ HI " + hiThresh + " < " + data);
          } else { // clear the cell
          } // if in threshold
       } // if doubles
    } // if not null
} // flagThreshold ()
   
 public boolean isADouble (Object isit) {
    return ( isit.getClass().isInstance (new Double (-1.0)) );
 } // isADouble ()
 
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key != null && value != null) {
      if (key.equals("renderer") && value.equals("engineering")) {
        useEngineeringRenderer(true);
      } else if (key.equals("maxMinVisible") && value.equals("true")) {
        setMaxMinVisible(true);
      // LJM 060523
      } else if (key.equals ("thresholdVisible") && value.equals ("true")) {
         setThresholdVisible (true);
      } // if
    } // if
  } // setProperty ()
	
	public String toString() {
		return "Tabular Data Panel";
	}
  
   /** XXX a method for the @see TableModelListener interface */
   public void tableChanged (TableModelEvent e) {
       int row = e.getFirstRow ();
       int column = e.getColumn ();
       TableModel model = (TableModel)e.getSource ();
       /* Object data = model.getValueAt (row, column);
       if (true) {
          log.debug ("^^^ tableChange at: " + Integer.toString (row) + ", " + Integer.toString (column));
          log.debug ("^^^ data: " + data);
       }
       */
   } // tableChanged ()
 
 /** an inner class to implement the data model in this design pattern */ 
  private class DataTableModel extends AbstractTableModel {
    private String[] columnNames = {
        "Name",
        "Value",
        "Unit",
        "Min",
        "Max",
        "Min Thresh",
        "Max Thresh"
        };
    
    private ArrayList rows;
    
    private boolean cleared;
    
    private boolean maxMinVisible;
    // LJM 060523
    private boolean thresholdVisible;
    
    private boolean useOffsets;
    
    public DataTableModel() {
      super();
      rows = new ArrayList();
      cleared = true;
      maxMinVisible = false;
      // LJM 060523
      thresholdVisible = false;
      useOffsets = false;
    }
    
    public boolean isCellEditable (int row, int col) {
       return (
             col == this.findColumn ("Min Thresh") ||
             col == this.findColumn ("Max Thresh")
             );
    } // isCellEditable ()
    
    public void addRow (String name, String unit, double lowerThresh, double upperThresh) {
      // TODO include thresholds
       DataRow dataRow = new DataRow (name, unit, lowerThresh, upperThresh);
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
    
    public void takeOffsets() {
      for (int i=0; i<rows.size(); i++) {
        DataRow dataRow = (DataRow)rows.get(i);
        dataRow.setOffset(dataRow.getData());
      }
      fireTableDataChanged();
    }
    
    public void useOffsets(boolean useOffsets) {
      if (this.useOffsets != useOffsets) {
        this.useOffsets = useOffsets;
        fireTableDataChanged();
      }
    }       

    /** a method that will get a data row from its index */
    public DataRow getRowAt (int rowdex) {
    //log.debug("RowIndex: " + rowdex);	
       return ( (DataRow)rows.get (rowdex) );
    }
    
    public int getRowCount() {
      return rows.size();
    }
    
    /** a method that will @return the row index number
      * given @param row name*/
    public int getRowNumber (String name) {
       DataRow dataRow = null;
       for (int i=0; i<rows.size (); i++) {
          dataRow = (DataRow)rows.get (i);
          if (dataRow.name.equals (name)) {
             return i;
          } // if
       } // for
       return -1; // name not found
    } // getRowNumber ()

    public int getColumnCount() {
      // LJM 060523
      int retval = columnNames.length;
      retval -= (maxMinVisible)?    0 : 2;
      retval -= (thresholdVisible)? 0 : 2;
      return retval;
    }
    
    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      if (col != 0 && cleared) {
        return new String();
     }
      
      if (row == -1) {
         return null;
      }
      
      DataRow dataRow = (DataRow)rows.get(row);
      String[] nameSplit = dataRow.getName ().split ("/");
      
      switch (col) {
        case 0:
           return ( nameSplit [nameSplit.length-1] );
        case 1:
          return dataRow.isCleared()? null : new Double(dataRow.getData() - (useOffsets?dataRow.getOffset():0));
        case 2:
          return dataRow.getUnit();
        case 3:
          return dataRow.isCleared()? null : new Double(dataRow.getMinimum() - (useOffsets?dataRow.getOffset():0));
        case 4:
          return dataRow.isCleared()? null : new Double(dataRow.getMaximum() - (useOffsets?dataRow.getOffset():0));
        case 5:
           return (dataRow.minThresh == (-1 * Double.MAX_VALUE))? null : new Double (dataRow.minThresh);
        case 6:
           return (dataRow.maxThresh == Double.MAX_VALUE)? null : new Double (dataRow.maxThresh); 
        default:
          return null;
      }
    }
    
    /** XXX a method that will update table data in response to user actions */
    public void setValueAt (Object value, int row, int col) {
        log.debug ("^^^ Setting value at " + row + "," + col + " to " + value);
        DataRow dataRow = (DataRow)rows.get (row);
        switch (col) {
        case 5: // "Min Thresh"
             try {
                dataRow.minThresh = Double.parseDouble ((String)value);
             } catch (Throwable e) {
                // log.debug ("^^^ Empty string in minThresh - breaking");
                break;
             }
             fireTableCellUpdated (row, col);
             flagThreshold (row);
             break;
          case 6: // "Max Thresh"
             try {
                dataRow.maxThresh = Double.parseDouble ((String)value);
             } catch (Throwable e) {
                // log.debug ("^^^ Empty string in maxThresh - breaking");
                break;
             }
             fireTableCellUpdated (row, col);
             flagThreshold (row);
             break;
          default:
            return; // do nothing
        } // switch
    } // setValueAt ()
    
    public boolean getMaxMinVisibile() {
      return maxMinVisible;
    }
    
    // LJM 060523
    public boolean getThresholdVisible () {
       return thresholdVisible;
     } // getThresholdVisible ()
      
    public void setMaxMinVisible(boolean maxMinVisible) {
      if (this.maxMinVisible != maxMinVisible) {
        this.maxMinVisible = maxMinVisible;
        this.thresholdVisible = maxMinVisible;
        fireTableStructureChanged();
      }
    }
    
    // LJM 060523
    public void setThresholdVisible (boolean thresholdVisible) {
       if (this.thresholdVisible != thresholdVisible) {
         this.thresholdVisible = thresholdVisible;
         fireTableStructureChanged();
       }
     } // setThresholdVisible ()
    
  }
  
  private class DataRow {
    String name;
    String unit;
    double value;
    double min;
    double max;
    double minThresh = -1 * Double.MAX_VALUE;
    double maxThresh = Double.MAX_VALUE;
    DataTableCellRenderer dataCellRenderer;
 
    double offset;
    boolean cleared;
    
    // TODO include thresholds here
    public DataRow(String name, String unit, double lowerThresh, double upperThresh) {
      this.name = name;
      this.unit = unit;
      this.minThresh = lowerThresh;
      this.maxThresh = upperThresh;
      this.dataCellRenderer = new DataTableCellRenderer ();
      clearData();
    }
    
    public String getName() {
      return name;
    }

    public String getUnit() {
      return unit;
    }
    
    public double getData() {
      return value;
    }    
    
    public void setData(double data) {
      value = data;
      min = cleared? data:Math.min(min, data);
      max = cleared? data:Math.max(max, data);
      cleared = false;
    }
    
    public DataTableCellRenderer getDataCellRenderer () {
       return this.dataCellRenderer;
    }
    
    public void setDataCellRenderer (DataTableCellRenderer newRenderer) {
       this.dataCellRenderer = newRenderer;
    }
    
    public double getMinimum() {
      return min;
    }
    
    public double getMaximum() {
      return max;
    }

    public double getOffset() {
      return offset;
    }

    public void setOffset(double offset) {
      this.offset = offset;
    }

    public void clearData() {
      cleared = true;
    }
    
    public boolean isCleared() {
      return cleared;
    }
    
    public boolean isOverHighThreshold () {
       return (this.maxThresh < this.value);
    } // isOverHighThreshold ()

    public boolean isUnderLowThreshold () {
       return (this.value < this.minThresh);
    } // isUnderLowThreshold () 
    
  } //  inner class DataRow
  
  private class DoubleTableCellRenderer extends DefaultTableCellRenderer {
    private boolean showEngineeringFormat;
    
    private DecimalFormat decimalFormatter;
    private DecimalFormat engineeringFormatter;
    
    public DoubleTableCellRenderer() {
      this(false);
    }
    
    public DoubleTableCellRenderer(boolean showEngineeringFormat) {
      super();
      
      this.showEngineeringFormat = showEngineeringFormat;
      
      setHorizontalAlignment(SwingConstants.RIGHT);

      decimalFormatter = new DecimalFormat("0.000000000");
      engineeringFormatter = new DecimalFormat("##0.000000000E0");
    }
    
    public void setValue(Object value) {
      if (value != null && value instanceof Number) {
        if (showEngineeringFormat) {
          setText(engineeringFormatter.format(value));
        } else {
          setText(decimalFormatter.format(value));
        }
      } else {
        super.setValue(value);
      }
    }

    public void setShowEngineeringFormat(boolean showEngineeringFormat) {
      this.showEngineeringFormat = showEngineeringFormat;
    }
    
    
  } // inner class DoubleTableCellRenderer
  
  /** an inner calss that renders the data value cell by coloring it when it is outside
   * of the threshold intervals
   */ 
  private class DataTableCellRenderer extends DoubleTableCellRenderer {
     public DataTableCellRenderer () {
        super ();
     }

     public Component getTableCellRendererComponent(JTable aTable, 
             Object aNumberValue, 
             boolean aIsSelected, 
             boolean aHasFocus, 
             int aRow, int aColumn) {  
		/* 
		* Implementation Note :
		* It is important that no "new" be present in this 
		* implementation (excluding exceptions):
		* if the table is large, then a large number of objects would be 
		* created during rendering.
		*/

		if (aNumberValue == null || !(aNumberValue instanceof Number)) 
			return this;
		
		Component renderer = super.getTableCellRendererComponent(aTable, 
		                           aNumberValue, 
		                           aIsSelected, 
		                           aHasFocus, 
		                           aRow, aColumn);
		
		Number numberValue = (Number)aNumberValue;
		
	    DataRow theRowAtDataRow = tableModel.getRowAt (aRow); 
	    Object minThreshData = (theRowAtDataRow.minThresh == (-1 * Double.MAX_VALUE))? null : new Double (theRowAtDataRow.minThresh);
	    Object maxThreshData = (theRowAtDataRow.minThresh == Double.MAX_VALUE)? null : new Double (theRowAtDataRow.maxThresh);

	    if (minThreshData == null || maxThreshData == null) {
			renderer.setBackground(Color.white);
	    	renderer.setForeground(Color.black);
	    	return this;
	    }
		if (numberValue.doubleValue() < (Double)minThreshData) {
			renderer.setBackground(Color.red);
		}
		else if (numberValue.doubleValue() > (Double)maxThreshData){
			renderer.setBackground(Color.yellow);
		}
		else {
			renderer.setBackground(Color.white);
		}
		return this;
	}
     
  } // inner class DataCellRenderer
  
  
  
} // class
