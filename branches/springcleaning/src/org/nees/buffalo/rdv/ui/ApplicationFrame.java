/*
 * Created on Apr 1, 2005
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.datapanel.DataPanel;
import org.nees.buffalo.rdv.datapanel.JFreeChartDataPanel;
import org.nees.buffalo.rdv.datapanel.JPEGDataPanel;
import org.nees.buffalo.rdv.datapanel.SpectrumAnalyzerDataPanel;
import org.nees.buffalo.rdv.datapanel.StringDataPanel;
import org.nees.buffalo.rdv.datapanel.TabularDataPanel;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.RBNBController;

/**
 * Main frame fro the application
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class ApplicationFrame extends JFrame {
	
	static Log log = LogFactory.getLog(ApplicationFrame.class.getName());
	
	private DataViewer dataViewer;
	private RBNBController rbnb;
	private DataPanelManager dataPanelManager;
	
	private JFrame frame;
	private GridBagConstraints c;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private ChannelListPanel channelListPanel;
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
 	private Action showControlPanelAction;
 	private Action showStatusPanelAction;
 	private Action dataPanelAction;
 	private Action dataPanelHorizontalLayoutAction;
 	private Action dataPanelVerticalLayoutAction;
 	private Action showHiddenChannelsAction;
 	private Action fullScreenAction;
 	
 	private Action windowAction;
 	private Action addJPEGDataPanelAction;
 	private Action addTimeSeriesChartDataPanelAction;
 	private Action addXYChartDataPanelAction;
 	private Action addStringDataPanelAction;
 	private Action addTabularDataPanelAction;
 	private Action addSpectrumAnalyzerDataPanelAction;
 	private Action closeAllDataPanelsAction;
 	
 	private Action helpAction;
 	private Action aboutAction;
	
	public ApplicationFrame(DataViewer dataViewer, RBNBController rbnb, DataPanelManager dataPanelManager) {
		super();
		
		this.dataViewer = dataViewer;
		this.rbnb = rbnb;
		this.dataPanelManager = dataPanelManager;
		
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

		frame.setTitle("RDV");
		
		c = new GridBagConstraints();

 		initActions();	
 		initMenuBar();
		initChannelListPanel();
		initRightPanel();
		initControls();
		initDataPanelContainer();		
		initStatus();
		initSplitPane();
		
		rbnb.addSubscriptionListener(controlPanel);
		
		rbnb.addTimeListener(controlPanel);
		rbnb.addTimeListener(statusPanel);
		
		rbnb.addStateListener(channelListPanel);
		rbnb.addStateListener(statusPanel);
		rbnb.addStateListener(controlPanel);

		rbnb.addMetadataListener(channelListPanel);
		rbnb.addMetadataListener(controlPanel);
		
		controlPanel.addPlaybackRateListener(rbnb);
		controlPanel.addPlaybackRateListener(statusPanel);
		
		controlPanel.addTimeScaleListener(rbnb);
  		controlPanel.addTimeScaleListener(statusPanel);
   
 		frame.pack();
  		frame.setVisible(true);
	}
  	
 	private void initActions() {
 		fileAction = new DataViewerAction("File", "File Menu", KeyEvent.VK_F);
 		
 		connectAction = new DataViewerAction("Connect", "Connect to RBNB server", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				if (rbnbConnectionDialog == null) {
 					rbnbConnectionDialog = new RBNBConnectionDialog(frame, rbnb);
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
 		
 		importAction = new DataViewerAction("Import", "Import local data to RBNB server", KeyEvent.VK_I) {
 			public void actionPerformed(ActionEvent ae) {
 				new RBNBImportDialog(frame, rbnb);
 			}			
 		}; 		
 
 		exitAction = new DataViewerAction("Exit", "Exit RDV", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				dataViewer.exit();
 			}			
 		};
 		
 		controlAction = new DataViewerAction("Control", "Control Menu", KeyEvent.VK_C);
 
 		realTimeAction = new DataViewerAction("Real Time", "View data in real time", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.monitor();
 			}			
 		};
 		
 		playAction = new DataViewerAction("Play", "Playback data", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.play();
 			}			
 		};
 
 		pauseAction = new DataViewerAction("Pause", "Pause data display", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.pause();
 			}			
 		};
 
 		beginningAction = new DataViewerAction("Go to beginning", "Move the location to the start of the data", KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0)) {
 			public void actionPerformed(ActionEvent ae) {
				controlPanel.setLocationBegin();
 			}			
 		};
 
 		endAction = new DataViewerAction("Go to end", "Move the location to the end of the data", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_END, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				controlPanel.setLocationEnd();
 			}			
 		};
 
 		updateChannelListAction = new DataViewerAction("Update Channel List", "Update the channel list", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)) {
 			public void actionPerformed(ActionEvent ae) {
 				rbnb.updateMetadataBackground();
 			}			
 		};
 
 		dropDataAction = new DataViewerAction("Drop Data", "Drop data if plaback can't keep up with data rate", KeyEvent.VK_D) {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				rbnb.dropData(menuItem.isSelected());		
 			}			
 		};
 		
 		viewAction = new DataViewerAction("View", "View Menu", KeyEvent.VK_V);
 
 		showChannelListAction = new DataViewerAction("Show Channel List", "", KeyEvent.VK_L) {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				boolean selected = menuItem.isSelected();
 				if (selected) {
 					int dividerLocation = splitPane.getLastDividerLocation();
 					if (dividerLocation <= 1) {
 						dividerLocation = 150;
 					}
 					splitPane.setDividerLocation(dividerLocation);
 				} else {
 					splitPane.setDividerLocation(0);
 				} 				
 			}			
 		};
 
 		showControlPanelAction = new DataViewerAction("Show Control Panel", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				controlPanel.setVisible(menuItem.isSelected());
 			}			
 		};
 
 		showStatusPanelAction = new DataViewerAction("Show Status Panel", "", KeyEvent.VK_S) {
 			public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				statusPanel.setVisible(menuItem.isSelected());
 			}			
 		};

 		dataPanelAction = new DataViewerAction("Data Panel", "Data Panel Sub-Menu", KeyEvent.VK_D);
 		
		dataPanelHorizontalLayoutAction = new DataViewerAction("Horizontal Layout") {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelContainer.setLayout(DataPanelContainer.HORIZONTAL_LAYOUT);
 			}			
 		}; 		
 		
		dataPanelVerticalLayoutAction = new DataViewerAction("Vertical Layout") {
 			public void actionPerformed(ActionEvent ae) {
 				dataPanelContainer.setLayout(DataPanelContainer.VERTICAL_LAYOUT);
 			}			
 		}; 		
 		
 		showHiddenChannelsAction = new DataViewerAction("Show Hidden Channels", "", KeyEvent.VK_H) {
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
 		
 		addJPEGDataPanelAction = new DataViewerAction("Add Video/Photo Data Panel", "", KeyEvent.VK_J) {
 			public void actionPerformed(ActionEvent ae) {
 				JPEGDataPanel panel = new JPEGDataPanel(dataPanelContainer, rbnb);
 				dataPanelManager.addDataPanel(panel);
 			}			
 		};
 		
 		addTimeSeriesChartDataPanelAction = new DataViewerAction("Add Time Series Data Panel", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				JFreeChartDataPanel panel = new JFreeChartDataPanel(dataPanelContainer, rbnb);
 				dataPanelManager.addDataPanel(panel);
 			}			
 		};
 		
 		addXYChartDataPanelAction = new DataViewerAction("Add XY Data Panel", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				JFreeChartDataPanel panel = new JFreeChartDataPanel(dataPanelContainer, rbnb, true);
 				dataPanelManager.addDataPanel(panel);
 			}			
 		};
 
 		addStringDataPanelAction = new DataViewerAction("Add Text Data Panel", "", KeyEvent.VK_S) {
 			public void actionPerformed(ActionEvent ae) {
 				StringDataPanel panel = new StringDataPanel(dataPanelContainer, rbnb);
 				dataPanelManager.addDataPanel(panel);
 			}			
 		};
 		
 		addTabularDataPanelAction = new DataViewerAction("Add Tabular Data Panel", "", KeyEvent.VK_S) {
 			public void actionPerformed(ActionEvent ae) {
 				TabularDataPanel panel = new TabularDataPanel(dataPanelContainer, rbnb);
 				dataPanelManager.addDataPanel(panel);
 			}			
 		};
 		
 		addSpectrumAnalyzerDataPanelAction = new DataViewerAction("Add Spectrum Analyzer Data Panel", "", KeyEvent.VK_A) {
 			public void actionPerformed(ActionEvent ae) {
 				SpectrumAnalyzerDataPanel panel = new SpectrumAnalyzerDataPanel(dataPanelContainer, rbnb);
 				dataPanelManager.addDataPanel(panel);
 			}
 		};
 
 		closeAllDataPanelsAction = new DataViewerAction("Close all data panels", "", KeyEvent.VK_C) {
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
  
  		JMenuItem menuItem;
  		
 		JMenu fileMenu = new JMenu(fileAction);
  		
 		menuItem = new JMenuItem(connectAction);
 		fileMenu.add(menuItem);
 
 		menuItem = new JMenuItem(disconnectAction);
 		fileMenu.add(menuItem);
 		
 		fileMenu.addSeparator();	
 		
 		menuItem = new JMenuItem(importAction);
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
  		
 		menuItem = new JMenuItem(addJPEGDataPanelAction);
 		windowMenu.add(menuItem);
 		
 		menuItem = new JMenuItem(addTimeSeriesChartDataPanelAction);
  		windowMenu.add(menuItem);
  
 		menuItem = new JMenuItem(addXYChartDataPanelAction);
  		windowMenu.add(menuItem);
  		
 		menuItem = new JMenuItem(addStringDataPanelAction);
  		windowMenu.add(menuItem);
  		
  		menuItem = new JMenuItem(addTabularDataPanelAction);
  		windowMenu.add(menuItem);

 		menuItem = new JMenuItem(addSpectrumAnalyzerDataPanelAction);
  		windowMenu.add(menuItem);
  		
 		windowMenu.addSeparator();
 		
 		menuItem = new JMenuItem(closeAllDataPanelsAction);
  		windowMenu.add(menuItem);
  		
  		menuBar.add(windowMenu);
  		
 		JMenu helpMenu = new JMenu(helpAction);
  
 		menuItem = new JMenuItem(aboutAction);
  		helpMenu.add(menuItem);		
  		
  		menuBar.add(helpMenu);
		
		frame.setJMenuBar(menuBar);
	}
			
	private void initChannelListPanel() {
		channelListPanel = new ChannelListPanel(this, rbnb);
		channelListPanel.setMinimumSize(new Dimension(0, 0));
		
		log.info("Created channel tree with initial channel list.");
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
		c.insets = new java.awt.Insets(10,10,5,10);
		c.anchor = GridBagConstraints.NORTHWEST;
		rightPanel.add(controlPanel, c);
		
		log.info("Added control panel.");
	}
	
	private void initDataPanelContainer() {
		dataPanelContainer = new DataPanelContainer();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(5,10,5,10);
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
		c.insets = new java.awt.Insets(5,10,10,10);
		c.anchor = GridBagConstraints.NORTHWEST;
		rightPanel.add(statusPanel, c);
		
		log.info("Added status panel.");		
	}
	
	private void initSplitPane() {
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, channelListPanel, rightPanel);
		splitPane.setResizeWeight(0);
		splitPane.setOneTouchExpandable(true);
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
 	
	public boolean viewChannel(String channelName) {
		Channel channel = rbnb.getChannel(channelName);
		if (channel != null) {
			viewChannel(channel);
			return true;
		} else {
			return false;
		}
	}
		
	public void viewChannel(Channel channel) {
		String channelName = channel.getName();
		String mime = channel.getMimeType();
		
		log.info("Cretaing data panel for channel " + channelName + ".");
		
		DataPanel panel = null;
		if (mime == null) {
			log.warn("Unknown data type for channel " + channelName + ".");
			if (channelName.endsWith(".jpg")) {
				panel = new JPEGDataPanel(dataPanelContainer, rbnb);
			} else {
				panel = new JFreeChartDataPanel(dataPanelContainer, rbnb);
			}
		} else if (mime.equals("image/jpeg")) {		
			panel = new JPEGDataPanel(dataPanelContainer, rbnb);
		} else if (mime.equals("application/octet-stream")) {		
			panel = new JFreeChartDataPanel(dataPanelContainer, rbnb);
		} else  if (mime.equals("text/plain")) {
			panel = new StringDataPanel(dataPanelContainer, rbnb);
		} else {
			log.error("Unsupported data type for channel " + channelName + ".");
			return;
		}
		
		try {
			panel.setChannel(channel);	
		} catch (Exception e) {
			log.error("Failed to add chanel to data panel.");
			e.printStackTrace();
			return;
		}
		
		dataPanelManager.addDataPanel(panel);
		dataPanelContainer.addDataPanel(panel);
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
 		
 		public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator) {
 			this(text, desc, mnemonic, accelerator, null);
		}
 		
 	    public DataViewerAction(String text, String desc, int mnemonic, KeyStroke accelerator, ImageIcon icon) {
 	        super(text, icon);
 	        putValue(SHORT_DESCRIPTION, desc);
 	        putValue(MNEMONIC_KEY, new Integer(mnemonic));
 	        putValue(ACCELERATOR_KEY, accelerator);
 	    }
 	    
 		public void actionPerformed(ActionEvent ae) {}
 		
 		public boolean isSelected() {
 			return selected;
 		}
 		
 		public void setSelected(boolean selected) {
 			this.selected = selected;
 		}
 	} 	
}
