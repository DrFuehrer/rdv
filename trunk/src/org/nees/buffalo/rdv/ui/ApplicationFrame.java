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

package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.Extension;
import org.nees.buffalo.rdv.rbnb.ConnectionListener;
import org.nees.buffalo.rdv.rbnb.MessageListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;

/**
 * Main frame fro the application
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class ApplicationFrame extends JFrame implements MessageListener, ConnectionListener, StateListener {
	
	static Log log = LogFactory.getLog(ApplicationFrame.class.getName());
	
	private DataViewer dataViewer;
	private RBNBController rbnb;
	private DataPanelManager dataPanelManager;
	
 	private BusyDialog busyDialog;
	
	private JFrame frame;
	private GridBagConstraints c;
	private JMenuBar menuBar;
	private ChannelListPanel channelListPanel;
  private MetadataPanel metadataPanel;
  private JSplitPane leftPanel;
	private JPanel rightPanel;
	private ControlPanel controlPanel;
	private StatusPanel statusPanel;
	private DataPanelContainer dataPanelContainer;
 	private JSplitPane splitPane;
 	
	private AboutDialog aboutDialog;
	private RBNBConnectionDialog rbnbConnectionDialog;

 	private Action fileAction;
 	private Action connectAction;
 	private Action disconnectAction;
 	private Action importAction;
  private Action exportAction;
 	private Action exitAction;
 	
 	private Action controlAction;
 	private Action realTimeAction;
 	private Action playAction;
 	private Action pauseAction;
 	private Action beginningAction;
 	private Action endAction;
 	private Action updateChannelListAction;
 	private Action dropDataAction;
 	
 	private Action viewAction;
 	private Action showChannelListAction;
  private Action showMetadataPanelAction;
 	private Action showControlPanelAction;
 	private Action showStatusPanelAction;
 	private Action dataPanelAction;
 	private Action dataPanelHorizontalLayoutAction;
 	private Action dataPanelVerticalLayoutAction;
 	private Action showHiddenChannelsAction;
 	private Action fullScreenAction;
 	
 	private Action windowAction;
 	private Action closeAllDataPanelsAction;
 	
 	private Action helpAction;
 	private Action aboutAction;
  
  private JLabel throbber;
  private Icon throbberStop;
  private Icon throbberAnim;
 		
	public ApplicationFrame(DataViewer dataViewer, RBNBController rbnb, DataPanelManager dataPanelManager) {
		super();
		
		this.dataViewer = dataViewer;
		this.rbnb = rbnb;
		this.dataPanelManager = dataPanelManager;
		
		busyDialog = null;
		
		initFrame();
	}
	
	private void initFrame() {
		frame = this;
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dataViewer.exit();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
		frame.getContentPane().setLayout(new BorderLayout());
    
    frame.setBounds(0, 0, 800, 600);

		frame.setTitle("RDV");
		
		c = new GridBagConstraints();

 		initActions();	
 		initMenuBar();
    
    initChannelListPanel();
    initMetadataPanel();
		initLeftPanel();
    
		initRightPanel();
		initControls();
		initDataPanelContainer();		
		initStatus();
    
		initSplitPane();
    
    channelListPanel.addChannelSelectionListener(metadataPanel);
		
		rbnb.addSubscriptionListener(controlPanel);
		
		rbnb.addTimeListener(controlPanel);
		rbnb.addTimeListener(statusPanel);
		
		rbnb.addStateListener(channelListPanel);
		rbnb.addStateListener(statusPanel);
		rbnb.addStateListener(controlPanel);
    rbnb.addStateListener(this);

		rbnb.getMetadataManager().addMetadataListener(channelListPanel);
    rbnb.getMetadataManager().addMetadataListener(metadataPanel);
		rbnb.getMetadataManager().addMetadataListener(controlPanel);
		
		rbnb.addPlaybackRateListener(statusPanel);
		
  	rbnb.addTimeScaleListener(statusPanel);
  	
  	rbnb.addMessageListener(this);
  	
  	rbnb.addConnectionListener(this);

 		frame.setVisible(true);
	}
  	
 	private void initActions() {
 		fileAction = new DataViewerAction("File", "File Menu", KeyEvent.VK_F);
 		
 		connectAction = new DataViewerAction("Connect", "Connect to RBNB server", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				if (rbnbConnectionDialog == null) {
 					rbnbConnectionDialog = new RBNBConnectionDialog(frame, rbnb, dataPanelManager);
 				} else {
 					rbnbConnectionDialog.setVisible(true);
 				}			
 			}			
 		};
 		
 		disconnectAction = new DataViewerAction("Disconnect", "Disconnect from RBNB server", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelManager.closeAllDataPanels();
 				rbnb.disconnect();
 			}			
 		};
 		
 		importAction = new DataViewerAction("Import", "Import local data to RBNB server", KeyEvent.VK_I, "icons/import.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				showImportDialog();
 			}			
 		};
        
    exportAction = new DataViewerAction("Export", "Export data on server to local computer", KeyEvent.VK_E, "icons/export.gif") {
        public void actionPerformed(ActionEvent ae) {
            
        }
    };
 
 		exitAction = new DataViewerAction("Exit", "Exit RDV", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				dataViewer.exit();
 			}			
 		};
 		
 		controlAction = new DataViewerAction("Control", "Control Menu", KeyEvent.VK_C);
 
 		realTimeAction = new DataViewerAction("Real Time", "View data in real time", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "icons/rt.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.monitor();
 			}			
 		};
 		
 		playAction = new DataViewerAction("Play", "Playback data", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "icons/play.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.play();
 			}			
 		};
 
 		pauseAction = new DataViewerAction("Pause", "Pause data display", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "icons/play.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.pause();
 			}			
 		};
 
 		beginningAction = new DataViewerAction("Go to beginning", "Move the location to the start of the data", KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "icons/begin.gif") {
 			public void actionPerformed(ActionEvent ae) {
				controlPanel.setLocationBegin();
 			}			
 		};
 
 		endAction = new DataViewerAction("Go to end", "Move the location to the end of the data", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "icons/end.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				controlPanel.setLocationEnd();
 			}			
 		};
 
 		updateChannelListAction = new DataViewerAction("Update Channel List", "Update the channel list", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "icons/refresh.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.updateMetadata();
 			}			
 		};
 
 		dropDataAction = new DataViewerAction("Drop Data", "Drop data if plaback can't keep up with data rate", KeyEvent.VK_D, "icons/drop_data.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				rbnb.dropData(menuItem.isSelected());		
 			}			
 		};
 		
 		viewAction = new DataViewerAction("View", "View Menu", KeyEvent.VK_V);

 		showChannelListAction = new DataViewerAction("Show Channels", "", KeyEvent.VK_L, "icons/channels.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
        channelListPanel.setVisible(menuItem.isSelected());
        leftPanel.resetToPreferredSizes();
 			}			
 		};
    
    showMetadataPanelAction = new DataViewerAction("Show Properties", "", KeyEvent.VK_P, "icons/properties.gif") {
      public void actionPerformed(ActionEvent ae) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
        metadataPanel.setVisible(menuItem.isSelected());
        leftPanel.resetToPreferredSizes();
      }     
    };    
 
 		showControlPanelAction = new DataViewerAction("Show Control Panel", "", KeyEvent.VK_C, "icons/control.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				controlPanel.setVisible(menuItem.isSelected());
 			}			
 		};
 
 		showStatusPanelAction = new DataViewerAction("Show Status Panel", "", KeyEvent.VK_S, "icons/info.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				statusPanel.setVisible(menuItem.isSelected());
 			}			
 		};

 		dataPanelAction = new DataViewerAction("Data Panel", "Data Panel Sub-Menu", KeyEvent.VK_D);
 		
		dataPanelHorizontalLayoutAction = new DataViewerAction("Horizontal Layout", "", -1, "icons/horizontal.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelContainer.setLayout(DataPanelContainer.HORIZONTAL_LAYOUT);
 			}			
 		}; 		
 		
		dataPanelVerticalLayoutAction = new DataViewerAction("Vertical Layout", "", -1, "icons/vertical.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelContainer.setLayout(DataPanelContainer.VERTICAL_LAYOUT);
 			}			
 		}; 		
 		
 		showHiddenChannelsAction = new DataViewerAction("Show Hidden Channels", "", KeyEvent.VK_H, "icons/hidden.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				boolean selected = menuItem.isSelected();
 				channelListPanel.showHiddenChannels(selected);
 			}			
 		};

 		fullScreenAction = new DataViewerAction("Full Screen", "", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 		        if (menuItem.isSelected()) {
 		        	if (enterFullScreenMode()) {
 		        		menuItem.setSelected(true);
 		        	} else {
 		        		menuItem.setSelected(false);
 		        	}
 		        } else {
 		        	leaveFullScreenMode();
 		        	menuItem.setSelected(false);
 		        }
 			}			
 		};
 		
 		windowAction = new DataViewerAction("Window", "Window Menu", KeyEvent.VK_W);
 		
 		closeAllDataPanelsAction = new DataViewerAction("Close all data panels", "", KeyEvent.VK_C, "icons/closeall.gif") {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelManager.closeAllDataPanels();				
 			}			
 		};
 		
 		helpAction = new DataViewerAction("Help", "Help Menu", KeyEvent.VK_H);
 
 		aboutAction = new DataViewerAction("About RDV", "", KeyEvent.VK_A) {
 			public void actionPerformed(ActionEvent ae) {
 				if (aboutDialog == null) {
 					aboutDialog = new AboutDialog(frame);
 				} else {
 					aboutDialog.setVisible(true);
 				}				
 			}			
 		};
 
 	}
 	
  private void initMenuBar() {
  	menuBar = new JMenuBar();
    menuBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
  
  	JMenuItem menuItem;
  		
 		JMenu fileMenu = new JMenu(fileAction);
  		
 		menuItem = new JMenuItem(connectAction);
 		fileMenu.add(menuItem);
 
 		menuItem = new JMenuItem(disconnectAction);
 		fileMenu.add(menuItem);
 		
 		fileMenu.addSeparator();	
 		
    menuItem = new JMenuItem(importAction);
 		fileMenu.add(menuItem);

    menuItem = new JMenuItem(exportAction);
    fileMenu.add(menuItem);
        
 		fileMenu.addSeparator();
 		
 		menuItem = new JMenuItem(exitAction);
  	fileMenu.add(menuItem);
  		
  	menuBar.add(fileMenu);
  		
 		JMenu controlMenu = new JMenu(controlAction);
 
 		menuItem = new JMenuItem(realTimeAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new JMenuItem(playAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new JMenuItem(pauseAction);
 		controlMenu.add(menuItem);
 		
 		controlMenu.addSeparator();
 
 		menuItem = new JMenuItem(beginningAction);
 		controlMenu.add(menuItem);
 
 		menuItem = new JMenuItem(endAction);
 		controlMenu.add(menuItem);
 		
 		menuBar.add(controlMenu);
 		
 		controlMenu.addSeparator();
 		
 		menuItem = new JMenuItem(updateChannelListAction);
 		controlMenu.add(menuItem);
 		
 		controlMenu.addSeparator();
 		
 		menuItem = new JCheckBoxMenuItem(dropDataAction);
 		menuItem.setSelected(true);
 		controlMenu.add(menuItem);
 		
 		JMenu viewMenu = new JMenu(viewAction);
 		
 		menuItem = new JCheckBoxMenuItem(showChannelListAction);
 		menuItem.setSelected(true);
 		viewMenu.add(menuItem);
    
    menuItem = new JCheckBoxMenuItem(showMetadataPanelAction);
    menuItem.setSelected(true);
    viewMenu.add(menuItem);   
 		
 		menuItem = new JCheckBoxMenuItem(showControlPanelAction);
 		menuItem.setSelected(true);
 		viewMenu.add(menuItem);
 		
 		menuItem = new JCheckBoxMenuItem(showStatusPanelAction);
 		menuItem.setSelected(true);
 		viewMenu.add(menuItem);
 		
 		viewMenu.addSeparator();
 		
 		JMenu dataPanelSubMenu = new JMenu(dataPanelAction);
 		
 		ButtonGroup dataLanelLayoutGroup = new ButtonGroup();
 		
 		menuItem = new JRadioButtonMenuItem(dataPanelHorizontalLayoutAction);
 		menuItem.setSelected(true);
 		dataPanelSubMenu.add(menuItem);
 		dataLanelLayoutGroup.add(menuItem);
 		
 		menuItem = new JRadioButtonMenuItem(dataPanelVerticalLayoutAction);
 		dataPanelSubMenu.add(menuItem);
 		dataLanelLayoutGroup.add(menuItem);
 		
 		viewMenu.add(dataPanelSubMenu);

 		viewMenu.addSeparator();
 		
 		menuItem = new JCheckBoxMenuItem(showHiddenChannelsAction);
 		menuItem.setSelected(false);
 		viewMenu.add(menuItem);

 		viewMenu.addSeparator();
 		
 		menuItem = new JCheckBoxMenuItem(fullScreenAction);
 		menuItem.setSelected(false);
 		viewMenu.add(menuItem);
 		
 		menuBar.add(viewMenu);
 		
 		JMenu windowMenu = new JMenu(windowAction);
  		
 		ArrayList extensions = dataPanelManager.getExtensions();
 		for (int i=0; i<extensions.size(); i++) {
 			final Extension extension = (Extension)extensions.get(i);
	 		Action action = new DataViewerAction("Add " + extension.getName(), "", KeyEvent.VK_J) {
	 			public void actionPerformed(ActionEvent ae) {
	 				try {
						dataPanelManager.createDataPanel(extension);
					} catch (Exception e) {
						log.error("Unable to open data panel provided by extension " + extension.getName() + " (" + extension.getID() + ").");
						e.printStackTrace();
					}
	 			}			
	 		};
	 		
	  		menuItem = new JMenuItem(action);
	  		windowMenu.add(menuItem);
 		}
  		
 		windowMenu.addSeparator();
 		
 		menuItem = new JMenuItem(closeAllDataPanelsAction);
  		windowMenu.add(menuItem);
  		
  		menuBar.add(windowMenu);
  		
 		JMenu helpMenu = new JMenu(helpAction);
  
 		menuItem = new JMenuItem(aboutAction);
  		helpMenu.add(menuItem);		
  		
  		menuBar.add(helpMenu);
      
    menuBar.add(Box.createHorizontalGlue());
    throbberStop = new ImageIcon(getClass().getClassLoader().getResource("icons/throbber.png"));
    throbberAnim = new ImageIcon(getClass().getClassLoader().getResource("icons/throbber_anim.gif"));
    throbber = new JLabel(throbberStop);
    throbber.setBorder(new EmptyBorder(0,0,0,4));
    menuBar.add(throbber, BorderLayout.EAST);
		
		frame.setJMenuBar(menuBar);
	}
  			
	private void initChannelListPanel() {
		channelListPanel = new ChannelListPanel(dataPanelManager, rbnb, this);
		channelListPanel.setMinimumSize(new Dimension(0, 0));
		
		log.info("Created channel list panel.");
	}
  
  private void initMetadataPanel() {
    metadataPanel = new MetadataPanel(rbnb);
    
    log.info("Created metadata panel");
  }
  
  private void initLeftPanel() {
    leftPanel = Factory.createStrippedSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        channelListPanel,
        metadataPanel,
        0.65f);
    leftPanel.setContinuousLayout(true);
    leftPanel.setBorder(new EmptyBorder(8, 8, 8, 0));
    
    log.info("Created left panel");
  }
	
	private void initRightPanel() {
		rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(0, 0));
		rightPanel.setLayout(new GridBagLayout());
	}
		
	private void initControls() {
		controlPanel = new ControlPanel(rbnb);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(8,0,0,8);
		c.anchor = GridBagConstraints.NORTHWEST;
		rightPanel.add(controlPanel, c);
		
		log.info("Added control panel.");
	}
	
	private void initDataPanelContainer() {
		dataPanelContainer = dataPanelManager.getDataPanelContainer();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(8,0,8,8);
		c.anchor = GridBagConstraints.NORTHWEST;
		rightPanel.add(dataPanelContainer, c);
		
		log.info("Added data panel container.");
	}
	
	private void initStatus() {
		statusPanel = new StatusPanel();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(0,0,8,8);
		c.anchor = GridBagConstraints.NORTHWEST;
		rightPanel.add(statusPanel, c);
		
		log.info("Added status panel.");		
	}
	
	private void initSplitPane() {
    splitPane = Factory.createStrippedSplitPane(
                  JSplitPane.HORIZONTAL_SPLIT,
                  leftPanel,
                  rightPanel,
                  0.2f);
		splitPane.setContinuousLayout(true);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
	}
	
 	private boolean enterFullScreenMode() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == null) {
				log.info("Switching to full screen mode.");
		
				setVisible(false);
		
				try {
					device.setFullScreenWindow(this);
				} catch (InternalError e) {
					log.error("Failed to switch to full screen exclusive mode.");
					e.printStackTrace();
					
					setVisible(true);
					return false;
				}
		
				dispose();
				setUndecorated(true);
				setVisible(true);
				requestFocus();
				
				return true;
			}
		}
		
		log.warn("No screens available or full screen exclusive mode is unsupported on your platform.");
    
    postError("Full screen mode is not supported on your platform.");
		
		return false;
 	}
 	
 	private void leaveFullScreenMode() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == this) {
				log.info("Leaving full screen mode.");
	
				setVisible(false);
				device.setFullScreenWindow(null);
				dispose();
				setUndecorated(false);
				setVisible(true);
				
				break;
			}
		}
	}
 	
	public void postError(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void postStatus(String statusMessage) {
		JOptionPane.showMessageDialog(this, statusMessage, "Error", JOptionPane.INFORMATION_MESSAGE);
	}
  
  public void showImportDialog() {
    showImportDialog(null);
  }
  
  public void showImportDialog(String sourceName) {
    new ImportDialog(frame, rbnb, sourceName);
  }
 	
 	class DataViewerAction extends AbstractAction {
 		boolean selected = false;
 		
 		public DataViewerAction(String text) {
 			this(text, null, -1, null, null);
 		}
 
 		public DataViewerAction(String text, String desc) {
 			this(text, desc, -1, null, null);
 		}
 		
		public DataViewerAction(String text, int mnemonic) {
 			this(text, null, mnemonic, null, null);
 		}
 		
 		public DataViewerAction(String text, String desc, int mnemonic) {
 			this(text, desc, mnemonic, null, null);
 		}
        
    public DataViewerAction(String text, String desc, int mnemonic, String iconFileName) {
      this(text, desc, mnemonic, null, iconFileName);
    }      
 		
 		public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator) {
 			this(text, desc, mnemonic, accelerator, null);
		}

    public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator, String iconFileName) {
 	    super(text);
 	    putValue(SHORT_DESCRIPTION, desc);
 	    putValue(MNEMONIC_KEY, new Integer(mnemonic));
 	    putValue(ACCELERATOR_KEY, accelerator);
 	    putValue(SMALL_ICON, DataViewer.getIcon(iconFileName));
 	  }
 	    
 		public void actionPerformed(ActionEvent ae) {}
 		
 		public boolean isSelected() {
 			return selected;
 		}
 		
 		public void setSelected(boolean selected) {
 			this.selected = selected;
 		}
 	}

 	public void connecting() {
 		busyDialog = new BusyDialog(this);
 		busyDialog.start();
    startThrobber();
	}

	public void connected() {
		busyDialog.close();
		busyDialog = null;
    stopThrobber();
	}

	public void connectionFailed() {
		busyDialog.close();
		busyDialog = null;
    stopThrobber();
	}
    
  public void postState(int newState, int oldState) {
    if (newState == Player.STATE_DISCONNECTED) {
      controlAction.setEnabled(false);
      disconnectAction.setEnabled(false);
      importAction.setEnabled(false);
      exportAction.setEnabled(false);
    } else if (newState != Player.STATE_EXITING) {
      controlAction.setEnabled(true);
      disconnectAction.setEnabled(true);
      importAction.setEnabled(true);
      exportAction.setEnabled(true);
    }
    
    if (newState == Player.STATE_LOADING || newState == Player.STATE_PLAYING || newState == Player.STATE_MONITORING) {
      startThrobber(); 
    } else {
      stopThrobber();
    }
  }
  
  private void startThrobber() {
    throbber.setIcon(throbberAnim);
  }
  
  private void stopThrobber() {
    throbber.setIcon(throbberStop);
  }  
}
