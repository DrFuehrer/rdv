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
import java.awt.Container;
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
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

/**
 * A Data Panel extension to display numeric data in a tabular form. Maximum and
 * minimum values are also displayed.
 * 
 * @author  Jason P. Hanley
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
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
   * a variable that has the number of groupings of columns to display
   * @since  1.3
   */
  // LJM 060524
  private static final int DEFAULT_COLUMN_GROUP_COUNT = 2;
  private int columnGroupCount = DEFAULT_COLUMN_GROUP_COUNT;
  
  /**
   * The data models for the table
   * @since  1.3
   */
  private Vector tableModels;
  
  /**
   * The table
   * @since  1.3
   */
  private Vector tables;
  private Box panelBox;
  
  /**
   * The cell renderer for the data
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
   * The check box menu item to control threshold column visibility
   * this functionality is currently coupled to showMaxMinMenuItem
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
   * The maximum number of channels allowed in this data panel;
   */
  private static final int MAX_CHANNELS = 75;

  /**
   * Initialize the data panel.
   * 
   * @since  1.2
   */
	public DigitalTabularDataPanel () {
    super ();
    String inputMessage = "How many groupings of columns shall the tabular panel display?";
    String errorInputMessage = "Please specify the count of grouping of columns as an INTEGER";
    String inputTitle = "Column Group Display Count";
    boolean gotGoodInput = false;
    while (!gotGoodInput) {
       try {
          this.columnGroupCount = Integer.parseInt (
                JOptionPane.showInputDialog (this.dataComponent, inputMessage, inputTitle, JOptionPane.PLAIN_MESSAGE) 
                                                 );
          gotGoodInput = true;
       } catch (Exception e) {
          JOptionPane.showMessageDialog (this.dataComponent, errorInputMessage, inputTitle, JOptionPane.PLAIN_MESSAGE);
       }
    } //  while
    lastTimeDisplayed = -1;
    initComponents (); 
    setDataComponent (mainPanel);
	} // constructor ()
	
  /**
   * Initialize the container and add the header.
   * 
   * @since  1.2
   */
      	private void initComponents() {
      	   mainPanel = new JPanel();
      	   mainPanel.setLayout(new BorderLayout());
                
      	   panelBox      = Box.createHorizontalBox ();
      	   tableModels   = new Vector ();
      	   tables        = new Vector ();
      	   
      	   // a pair of temp variables to populate the arraylists
      	   DataTableModel   tableModel = null;
      	   JTable           table      = null;
         
      	   for (int i=0; i<columnGroupCount; i++) {
      	      tableModel = new DataTableModel ();
      	      table = new JTable (tableModel);
               
      	      // TODO DRAGNDROP
      	      table.setDragEnabled (true);
          
      	      table.getModel ().addTableModelListener (this);
      	      table.addMouseListener (
      	            new MouseAdapter () {
      	               public void mouseClicked (MouseEvent e) {
      	                  JTable src = (JTable)(e.getComponent());
      	                  //log.debug ("^^^ CLICK!! from " + src.getSelectedRow () + "x" + src.getSelectedColumn () );
      	               } // mouseClicked ()
      	            }); // MouseListener
       
            table.getColumn ("Value").setCellRenderer (new DataTableCellRenderer ());
      	      
            table.addComponentListener(new ComponentAdapter() {
      	         public void componentResized(ComponentEvent e) {
      	            updateRowHeight();
      	         }
      	      }); 
     
      	      tables.add (table);
      	      tableModels.add (tableModel);              
      	      panelBox.add (new JScrollPane (table));
      	      if (i!=0 || i!=columnGroupCount-1) {
      	         panelBox.add (Box.createHorizontalStrut (7));
      	      }
      	   } // for
      	   mainPanel.add (panelBox, BorderLayout.CENTER);
            
      	   log.debug ("+++ " +  tables.size () + " tables and " +  tableModels.size () + " tableModels");
            
          JPanel buttonPanel = new JPanel();
          buttonPanel.setLayout(new BorderLayout());
       
          JPanel offsetsPanel = new JPanel();
          
          final JCheckBox useOffsetsCheckBox = new JCheckBox("Use Offsets");
          useOffsetsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              boolean checked = ((JCheckBox)ae.getSource()).isSelected();
              
              for (int i=0; i<columnGroupCount; i++) {
                 ( (DataTableModel)tableModels.get (i) ).useOffsets (checked);
                 ( (JTable)tables.get (i) ).repaint ();
              } // for 
            }      
          });
          offsetsPanel.add (useOffsetsCheckBox);
      
          JButton takeOffsetsButton = new JButton ("Take Offsets");
          takeOffsetsButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent ae) {
               useOffsetsCheckBox.setSelected (true); // this moved from above repaint
               for (int i=0; i<columnGroupCount; i++) {
                  ( (DataTableModel)tableModels.get (i) ).takeOffsets ();
                  ( (DataTableModel)tableModels.get (i) ).useOffsets (true);
                  ( (JTable)tables.get (i) ).repaint ();
               } // for
            } // actionPerformed ()
          }); // actionlistener
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
     
               for (int i=0; i<columnGroupCount; i++) {
                  copyMenuItem.setEnabled( ( (JTable)tables.get (i) ).getSelectedRowCount() > 0 );
               } // for     
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
            public void popupMenuCanceled(PopupMenuEvent arg0) {}
          });
          
          popupMenu.add(copyMenuItem);
          
          popupMenu.addSeparator();
          
          JMenuItem printMenuItem = new JMenuItem("Print...");
          printMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            
               try {
                  
                  for (int i=0; i<columnGroupCount; i++) {
                         ( (JTable)tables.get (i) ).print (JTable.PrintMode.FIT_WIDTH);
                  } // for
              } catch (PrinterException pe) {}
            
            }      
          });
          popupMenu.add(printMenuItem);         
          popupMenu.addSeparator();
          
          showMaxMinMenuItem = new  JCheckBoxMenuItem ("Show max/min and threshold columns", false);
          showMaxMinMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              setMaxMinVisible(showMaxMinMenuItem.isSelected());
            }      
          });
          popupMenu.add(showMaxMinMenuItem);
          
          /*
          showThresholdMenuItem = new  JCheckBoxMenuItem ("Show threshold columns", false);
          showThresholdMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent ae) {
              setThresholdVisible (showThresholdMenuItem.isSelected ());
            }      
          }); // actionListener
          popupMenu.add (showThresholdMenuItem);
          */
          
          JMenuItem selectColumnGroupsMenuItem = new JMenuItem ("Select Number of Column Groupings");
          selectColumnGroupsMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
               
               if (columnGroupCount == DEFAULT_COLUMN_GROUP_COUNT) {
                  // TODO MULTI**
                  //clearAllComponents ();
                  log.debug ("*** columnGroupCount: " + columnGroupCount + " channel COUNT: " + channels.size ());
                  //initComponents ();
                  Iterator itrate = channels.iterator ();
                  while (itrate.hasNext ()) {
                     String aChannel = (String)itrate.next ();
                     addChannel (aChannel);
                  } // while
                  component.setContent (mainPanel);
                  component.repaint();
                  //setDataComponent (mainPanel);
               } else if (columnGroupCount == 1) {
                  log.debug ("*** columnGroupCount: " + columnGroupCount + " channel COUNT: " + channels.size ());
                  //initComponents ();
                  component.setContent (mainPanel);
                  //setDataComponent (mainPanel);       
               }
            }      
          });
          popupMenu.add (selectColumnGroupsMenuItem);
      
          // set component popup and mouselistener to trigger it
          mainPanel.setComponentPopupMenu(popupMenu);
          table.setComponentPopupMenu(popupMenu);
          mainPanel.addMouseListener(new MouseInputAdapter() {});
    
      	} // setupComponents ()
  
         
       /** TODO a method that will add @param some number of table to this panel */
       private void setTables (int numTables) {
          columnGroupCount = numTables;
          int currentTableCount = tables.size ();
          if (currentTableCount < numTables) {
             // add more tables
          } else if (numTables < currentTableCount) {
             for (int i=0; i<(currentTableCount-numTables); i++) {
                tables.remove (currentTableCount-1-i);
                tableModels.remove (currentTableCount-1-i);
             } // for
          } 
          
          
       } // setTables ()
         
      	/** a method that will clear all gui components from the panel display */
      	private void clearAllComponents () {
            panelBox.removeAll ();
            panelBox = null;
            tableModels.clear ();
            tables.clear ();
            mainPanel.removeAll ();
            mainPanel.setLayout (null);
            mainPanel.repaint ();
            
            mainPanel = new JPanel();
      	} // clearAllComponents ()
         
  private void useEngineeringRenderer(boolean useEngineeringRenderer) {
    doubleCellRenderer.setShowEngineeringFormat(useEngineeringRenderer);
    
    for (int i=0; i<columnGroupCount; i++) {
       ( (JTable)tables.get (i) ).repaint ();
    } // for
    
    if (useEngineeringRenderer) {
      engineeringButton.setSelected(true);
      properties.setProperty("renderer", "engineering");
    } else {
      decimalButton.setSelected(true);
      properties.remove("renderer");
    }
  }

  private void setMaxMinVisible(boolean maxMinVisible) {
     
     for (int i=0; i<columnGroupCount; i++) { 
        if (( (DataTableModel)tableModels.get (i) ).getMaxMinVisibile () != maxMinVisible) {
           ( (DataTableModel)tableModels.get (i) ).setMaxMinVisible (maxMinVisible);
           showMaxMinMenuItem.setSelected (maxMinVisible);
              
              ((JTable)tables.get (i)).getColumn ("Value").setCellRenderer (new DataTableCellRenderer ());
           
           if (maxMinVisible) {
              ((JTable)tables.get (i)).getColumn ("Min").setCellRenderer (doubleCellRenderer);
              ((JTable)tables.get (i)).getColumn ("Max").setCellRenderer (doubleCellRenderer);
   
              properties.setProperty ("maxMinVisible", "true");
           } else {
             properties.remove ("maxMinVisible"); 
           } // else
        } // if visible
     } // for
  } // setMaxMinVisible ()

  private void setThresholdVisible (boolean thesholdVisible) {
     
     for (int i=0; i<columnGroupCount; i++) { 
        if (( (DataTableModel)tableModels.get (i) ).getThresholdVisible () != thesholdVisible) {
           ( (DataTableModel)tableModels.get (i) ).setThresholdVisible (thesholdVisible);
           showMaxMinMenuItem.setSelected (thesholdVisible);
        
           // table.getColumn ("Value").setCellRenderer (dataCellRenderer);
           // table.getColumn ("Value").setCellRenderer (doubleCellRenderer);
        
           if (thesholdVisible) {
              ((JTable)tables.get (i)).getColumn ("Min Thresh").setCellRenderer (doubleCellRenderer);
              ((JTable)tables.get (i)).getColumn ("Max Thresh").setCellRenderer (doubleCellRenderer);

              properties.setProperty ("thresholdVisible", "true");
           } else {
              properties.remove ("thresholdVisible");
           }
        } // if
     } // for
   } // setThresholdVisible ()
  
	void clearData() {
	   for (int i=0; i<columnGroupCount; i++) { 
	      ( (DataTableModel)tableModels.get (i) ).clearData();
	   } // for
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
         //return false;
		}
			
		String labelText = channelName;
		
		String unit = (String)units.get(channelName);
		if (unit != null) {
			labelText += "(" + unit + ")";
		}
		
      for (int i=0; i<columnGroupCount; i++) {
      
      String lowerThresholdString = (String)( lowerThresholds.get (channelName) );
      String upperThresholdString = (String)( upperThresholds.get (channelName) );
      
      // +/- Double.MAX_VALUE represents empty thresholds
      double lowerThresholdTemp = -1 * Double.MAX_VALUE;
      double upperThresholdTemp = Double.MAX_VALUE;
      
      // handle errors generated by daq - "Unknown command 'list-lowerbounds'" 
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
         ( (DataTableModel)tableModels.get (i) ).addRow (channelName, unit, lowerThresholdTemp, upperThresholdTemp);
      } // for
		updateRowHeight();
		log.debug ("@@@ channel ADDED: " + channelName);
		return true;
	} // addChannel
	
	public boolean removeChannel(String channelName) {
		if (!super.removeChannel(channelName)) {
			return false;
		}
		
      for (int i=0; i<columnGroupCount; i++) { 
         ( (DataTableModel)tableModels.get (i) ).deleteRow (channelName);
      } // for
      
      updateRowHeight();
		return true;
	}
	
	/*
   // LJM 060602 do we need this method?
    void channelAdded(String channelName) {
       String unit = (String)units.get(channelName);
       tableModel.addRow(channelName, unit);
       updateRowHeight();
     } // channelAdded ()
    
    // LJM 060602 do we need this method?
    void channelRemoved(String channelName) {
       tableModel.deleteRow(channelName);
       updateRowHeight();
    } // channelRemoved ()
    */
  
  private void updateRowHeight() {
     for (int i=0; i<columnGroupCount; i++) { 
        ( (JTable)tables.get (i) ).setRowHeight (MIN_ROW_HEIGHT);
     } // for
  }
	
	public void postTime(double time) {
		super.postTime(time);
		
		if (channelMap == null) {
			// no data to display yet
			return;
		}
		
		// loop over all channels and see if there is data for them
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);

      // if there is data for channel, post it
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
    
    // see if there is no data in the time range we are loooking at
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
           
      for (int ii=0; ii<columnGroupCount; ii++) { 
         ( (DataTableModel)tableModels.get (ii) ).updateData (channelName, data);   
      } // for 
    } // for 
	} // postDataTabular ()
   
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
      } else if (key.equals ("thresholdVisible") && value.equals ("true")) {
         setThresholdVisible (true);
      } // if
    } // if
  } // setProperty ()
	
	public String toString() {
		return "Tabular Data Panel";
	}
  
   /** a method for the @see TableModelListener interface */
   public void tableChanged (TableModelEvent e) {
       int row = e.getFirstRow ();
       int column = e.getColumn ();
       TableModel model = (TableModel)e.getSource ();
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
    private boolean thresholdVisible; 
    private boolean useOffsets;
    
    public DataTableModel() {
      super();
      rows = new ArrayList();
      cleared = true;
      maxMinVisible = false;
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
      DataRow dataRow = new DataRow (name, unit, lowerThresh, upperThresh);
      rows.add(dataRow);
      int row = rows.size()-1;
      fireTableRowsInserted(row, row);
      log.debug ("!!! ADDed row: " + name);
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

    /** a method that will @return get a data row from its @param index */
    public DataRow getRowAt (int rowdex) {
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
                   break;
                }
                fireTableCellUpdated (row, col);
                break;
             case 6: // "Max Thresh"
                try {
                   dataRow.maxThresh = Double.parseDouble ((String)value);
                } catch (Throwable e) {
                   break;
                }
                fireTableCellUpdated (row, col);
                break;
             default:
               return; // do nothing
        } // switch
    } // setValueAt ()
    
    public boolean getMaxMinVisibile() {
      return maxMinVisible;
    }
    
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
 
    double offset;
    boolean cleared;
    
    public DataRow (String name, String unit, double lowerThresh, double upperThresh) {
      this.name = name;
      this.unit = unit;
      this.minThresh = lowerThresh;
      this.maxThresh = upperThresh;
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
    * of the threshold interval
    */ 
  private class DataTableCellRenderer extends DoubleTableCellRenderer {
     public DataTableCellRenderer () {
        super ();
     }
     
     public Component getTableCellRendererComponent (
                                                        JTable aTable, 
                                                        Object aNumberValue, 
                                                        boolean aIsSelected, 
                                                        boolean aHasFocus, 
                                                        int aRow,
                                                        int aColumn
                                                     ) {  
		/* 
		* Implementation Note :
		* It is important that no "new" be present in this 
		* implementation (excluding exceptions):
		* if the table is large, then a large number of objects would be 
		* created during rendering.
		*/
        if (aNumberValue == null || !(aNumberValue instanceof Number)) { 
           return this;
        }
		
        Component renderer = super.getTableCellRendererComponent (
                                                                  aTable, 
                                                                  aNumberValue, 
                                                                  aIsSelected, 
                                                                  aHasFocus, 
                                                                  aRow,
                                                                  aColumn
                                                               );
		
        Number numberValue = (Number)aNumberValue;
		
        DataRow theRowAtDataRow = ( (DataTableModel)aTable.getModel () ).getRowAt (aRow); 
        Object minThreshData = (theRowAtDataRow.minThresh == (-1 * Double.MAX_VALUE))? null : new Double (theRowAtDataRow.minThresh);
        Object maxThreshData = (theRowAtDataRow.minThresh == Double.MAX_VALUE)? null : new Double (theRowAtDataRow.maxThresh);

        if (minThreshData == null || maxThreshData == null) {
           renderer.setBackground (Color.white);
           renderer.setForeground (Color.black);
           return this;
        }
        if (numberValue.doubleValue () < (Double)minThreshData) {
           renderer.setBackground (Color.red);
        } else if (numberValue.doubleValue () > (Double)maxThreshData){
           renderer.setBackground (Color.yellow);
        } else {
           renderer.setBackground (Color.white);
        }
        return this;
     } // getTableCellRendererComponent ()   
  } // inner class DataCellRenderer
} // class
/*
// TODO ARRAYLIST
for (int i=0; i<columnGroupCount; i++) { 
      ( (DataTableModel)tableModels.get (i) ).
      ( (JTable)tables.get (i) ).
} // for
*/
