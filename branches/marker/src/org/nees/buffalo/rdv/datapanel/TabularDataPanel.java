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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

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
   * The scroll pane for the table
   */
  private JScrollPane tableScrollPane;
  /**
   * The cell renderer for the data
   * 
   * @since  1.3
   */
  private DoubleTableCellRenderer doubleCellRenderer;  
  
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
   * The maximum number of channels allowed in this data panel;
   */
  private static final int MAX_CHANNELS = 75;
	
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
    doubleCellRenderer = new DoubleTableCellRenderer();
    table.getColumn("Value").setCellRenderer(doubleCellRenderer);

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
    
    showMaxMinMenuItem = new  JCheckBoxMenuItem("Show max/min columns", false);
    showMaxMinMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setMaxMinVisible(showMaxMinMenuItem.isSelected());
      }      
    });
    popupMenu.add(showMaxMinMenuItem);    

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
      
      table.getColumn("Value").setCellRenderer(doubleCellRenderer);
      
      if (maxMinVisible) {
        table.getColumn("Min").setCellRenderer(doubleCellRenderer);
        table.getColumn("Max").setCellRenderer(doubleCellRenderer);

        properties.setProperty("maxMinVisible", "true");
      } else {
        properties.remove("maxMinVisible");
      }
    }
  }

	void clearData() {
    tableModel.clearData();
    lastTimeDisplayed = -1;
	}

	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public boolean addChannel(String channelName) {
    if (channels.size() > MAX_CHANNELS) {
      return false;
    }
    
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
      }

      tableModel.updateData(channelName, data);
    }
	}
  
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key != null && value != null) {
      if (key.equals("renderer") && value.equals("engineering")) {
        useEngineeringRenderer(true);
      } else if (key.equals("maxMinVisible") && value.equals("true")) {
        setMaxMinVisible(true);
      }
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
    
    private boolean maxMinVisible;
    
    private boolean useOffsets;
    
    public DataTableModel() {
      super();
      rows = new ArrayList();
      cleared = true;
      maxMinVisible = false;
      useOffsets = false;
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

    public int getRowCount() {
      return rows.size();
    }

    public int getColumnCount() {
      return columnNames.length-(maxMinVisible?0:2);
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
          return dataRow.getName();  
        case 1:
          return dataRow.isCleared()? null : new Double(dataRow.getData() - (useOffsets?dataRow.getOffset():0));
        case 2:
          return dataRow.getUnit();
        case 3:
          return dataRow.isCleared()? null : new Double(dataRow.getMinimum() - (useOffsets?dataRow.getOffset():0));
        case 4:
          return dataRow.isCleared()? null : new Double(dataRow.getMaximum() - (useOffsets?dataRow.getOffset():0));
        default:
          return null;
      }
    }
    
    public boolean getMaxMinVisibile() {
      return maxMinVisible;
    }
      
    public void setMaxMinVisible(boolean maxMinVisible) {
      if (this.maxMinVisible != maxMinVisible) {
        this.maxMinVisible = maxMinVisible;
        fireTableStructureChanged();
      }
    }    
  }
  
  private class DataRow {
    String name;
    String unit;
    double value;
    double min;
    double max;
    double offset;
    boolean cleared;
    
    
    public DataRow(String name, String unit) {
      this.name = name;
      this.unit = unit;
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
  }
  
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
  }
}
