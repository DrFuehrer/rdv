package org.nees.buffalo.rdv;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.datapanel.DataPanel2;
import org.nees.buffalo.rdv.datapanel.JFreeChartDataPanel;
import org.nees.buffalo.rdv.datapanel.JPEGDataPanel;
import org.nees.buffalo.rdv.datapanel.SpectrumAnalyzerDataPanel;
import org.nees.buffalo.rdv.datapanel.StringDataPanel;
import org.nees.buffalo.rdv.datapanel.TabularDataPanel;

/**
 * @author Jason P. Hanley
 */
public class DataViewer extends JFrame implements DomainListener {
	
	static Log log = LogFactory.getLog(DataViewer.class.getName());

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
 	
 	private boolean applet;
	
	private AboutDialog aboutDialog;
	private RBNBConnectionDialog rbnbConnectionDialog;
		
	private RBNBController rbnb;
 	private static String rbnbHostName;
 	private static int rbnbPort;
	
	private ArrayList dataPanels;

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

	public static final String DEFAULT_RBNB_HOST_NAME = "localhost";
	public static final int DEFAULT_RBNB_PORT = 3333;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
	
	public DataViewer() {
 		this(null, DEFAULT_RBNB_PORT, false);
 	}
  		
 	public DataViewer(String rbnbHostName) {
 		this(rbnbHostName, DEFAULT_RBNB_PORT, false);
	}

 	public DataViewer(String rbnbHostName, boolean applet) {
 		this(rbnbHostName, DEFAULT_RBNB_PORT, applet);
	} 	
 	
	public DataViewer(String rbnbHostName, int rbnbPort) {
		this(rbnbHostName, rbnbPort, false);
	}
	
	public DataViewer(String rbnbHostName, int rbnbPort, boolean applet) {
		super();
			
		DataViewer.rbnbHostName = rbnbHostName;
		DataViewer.rbnbPort = rbnbPort;
		this.applet = applet;

		init();
	}
	
	private void init() {
		frame = this;
		
		dataPanels = new ArrayList();
		
		initRBNB();
		
		initFrame();		
	}
	
	private void initRBNB() {
		rbnb = new RBNBController();
	}
		
	private void initFrame() {
		if (!applet) {
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					exit();
				}
			});
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
			frame.getContentPane().setLayout(new BorderLayout());
	
			frame.setTitle("RDV");
		}
		
		c = new GridBagConstraints();

 		initActions();	
		if (!applet) initMenuBar();
		initChannelListPanel();
		initRightPanel();
		initControls();
		initDataPanelContainer();		
		initStatus();
		initSplitPane();
		
		rbnb.addSubscriptionListener(channelListPanel);
		rbnb.addSubscriptionListener(controlPanel);
		
		rbnb.addTimeListener(controlPanel);
		rbnb.addTimeListener(statusPanel);
		
		rbnb.addStateListener(statusPanel);
		rbnb.addStateListener(controlPanel);
		
		channelListPanel.addChannelListListener(rbnb);
		channelListPanel.addChannelListListener(controlPanel);
		
		controlPanel.addTimeScaleListener(rbnb);
		controlPanel.addTimeScaleListener(statusPanel);
		
		controlPanel.addDomainListener(rbnb);
  		controlPanel.addDomainListener(statusPanel);
  		controlPanel.addDomainListener(this);
  
 		if (rbnbHostName != null) {
 			rbnb.connect();
 			channelListPanel.connect();
 		}
 
  		if (!applet) {
  			frame.pack();
  			frame.setVisible(true);
  		}
	}
  	
 	private void initActions() {
 		fileAction = new DataViewerAction("File", "File Menu", KeyEvent.VK_F);
 		
 		connectAction = new DataViewerAction("Connect", "Connect to RBNB server", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				if (rbnbConnectionDialog == null) {
 					rbnbConnectionDialog = new RBNBConnectionDialog((DataViewer)frame, rbnb, channelListPanel);
 				} else {
 					rbnbConnectionDialog.setVisible(true);
 				}			
 			}			
 		};
 		
 		disconnectAction = new DataViewerAction("Disconnect", "Disconnect from RBNB server", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				closeAllDataPanels();
 				rbnb.disconnect();
 				channelListPanel.disconnect();
 			}			
 		};
 		
 		importAction = new DataViewerAction("Import", "Import local data to RBNB server", KeyEvent.VK_I) {
 			public void actionPerformed(ActionEvent ae) {
 				new RBNBImportDialog(frame);
 			}			
 		}; 		
 
 		exitAction = new DataViewerAction("Exit", "Exit RDV", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)) {
 			public void actionPerformed(ActionEvent ae) {
 				exit();
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
 				channelListPanel.updateChannelListBackground();
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
 				addDataPanel(panel);
 			}			
 		};
 		
 		addTimeSeriesChartDataPanelAction = new DataViewerAction("Add Time Series Data Panel", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				JFreeChartDataPanel panel = new JFreeChartDataPanel(dataPanelContainer, rbnb);
 				addDataPanel(panel);
 			}			
 		};
 		
 		addXYChartDataPanelAction = new DataViewerAction("Add XY Data Panel", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				JFreeChartDataPanel panel = new JFreeChartDataPanel(dataPanelContainer, rbnb, true);
 				addDataPanel(panel);
 			}			
 		};
 
 		addStringDataPanelAction = new DataViewerAction("Add Text Data Panel", "", KeyEvent.VK_S) {
 			public void actionPerformed(ActionEvent ae) {
 				StringDataPanel panel = new StringDataPanel(dataPanelContainer, rbnb);
 				addDataPanel(panel);
 			}			
 		};
 		
 		addTabularDataPanelAction = new DataViewerAction("Add Tabular Data Panel", "", KeyEvent.VK_S) {
 			public void actionPerformed(ActionEvent ae) {
 				TabularDataPanel panel = new TabularDataPanel(dataPanelContainer, rbnb);
 				addDataPanel(panel);
 			}			
 		};
 		
 		addSpectrumAnalyzerDataPanelAction = new DataViewerAction("Add Spectrum Analyzer Data Panel", "", KeyEvent.VK_A) {
 			public void actionPerformed(ActionEvent ae) {
 				SpectrumAnalyzerDataPanel panel = new SpectrumAnalyzerDataPanel(dataPanelContainer, rbnb);
 				addDataPanel(panel);
 			}
 		};
 
 		closeAllDataPanelsAction = new DataViewerAction("Close all data panels", "", KeyEvent.VK_C) {
 			public void actionPerformed(ActionEvent ae) {
 				closeAllDataPanels();				
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
		channelListPanel = new ChannelListPanel(this);
		channelListPanel.setMinimumSize(new Dimension(0, 0));
		
		log.info("Created channel tree with initial channel list.");
	}
	
	private void initRightPanel() {
		rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(0, 0));
		rightPanel.setLayout(new GridBagLayout());
	}
		
	private void initControls() {
		controlPanel = new ControlPanel(this, rbnb);
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
		statusPanel = new StatusPanel(this);
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
	
	public boolean viewChannel(String channelName) {
		Channel channel = channelListPanel.getChannel(channelName);
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
		
		DataPanel2 panel = null;
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
		
		addDataPanel(panel);
	}
	
	private void addDataPanel(DataPanel2 dataPanel) {
		dataPanel.setDomain(controlPanel.getDomain());
		dataPanelContainer.addDataPanel(dataPanel);
		dataPanels.add(dataPanel);
	}
	
	protected void closeAllDataPanels() {
		DataPanel2 dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel2)dataPanels.get(i);
			dataPanel.closePanel();
		}
	}
	
	public void domainChanged(double domain) {
		DataPanel2 dataPanel;
		for (int i=0; i<dataPanels.size(); i++) {
			dataPanel = (DataPanel2)dataPanels.get(i);
			dataPanel.setDomain(domain);
		}		
	}
	
	public void setDomain(double domain) {
		controlPanel.setDomain(domain);
	}

	public void exit() {
		if (rbnb != null) {
			rbnb.exit();
		}
		log.info("Exiting.");
		if (!applet) System.exit(0);		
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
 	
 	public Player getPlayer() {
 		return rbnb;
 	}

	public static String getRBNBHostName() {
		return rbnbHostName;
	}
	
	public static void setRBNBHostName(String rbnbHostName) {
		DataViewer.rbnbHostName = rbnbHostName;
	}
	
	public static int getRBNBPort() {
		return rbnbPort;
	}
	
	public static void setRBNBPort(int rbnbPort) {
		DataViewer.rbnbPort = rbnbPort;
	}
		
	public static String formatDate(double date) {
		return DATE_FORMAT.format(new Date(((long)(date*1000))));
	}
	
	public static String formatDate(long date) {
		return DATE_FORMAT.format(new Date(date));
	}

	public static String formatSeconds(double seconds) {
		String secondsString;
		if (seconds < 1e-6) {
			secondsString = Double.toString(round(seconds*1000000000)) + " ns";
 		} else if (seconds < 1e-3) {
			secondsString = Double.toString(round(seconds*1000000)) + " us";
 		} else if (seconds < 1 && seconds != 0) {
			secondsString = Double.toString(round(seconds*1000)) + " ms";
 		} else if (seconds < 60) {
 		 	secondsString = Double.toString(round(seconds)) + " s";
 		} else if (seconds < 60*60) {
			secondsString = Double.toString(round(seconds/60)) + " m";
 		} else if (seconds < 60*60*24){
 			secondsString = Double.toString(round(seconds/(60*60))) + " h";
 		} else {
 			secondsString = Double.toString(round(seconds/(60*60*24))) + " d";
 		}
 		return secondsString;
 	}
 	
 	public static String formatBytes(int bytes) {
 		String bytesString;
 		if (bytes < 1024) {
 			bytesString = Integer.toString(bytes) + " B";
 		} else if (bytes < 1024*1024) {
 			bytesString = Double.toString(round(bytes/1024d)) + " KB";
 		} else if (bytes < 1024*1024*1024) {
 			bytesString = Double.toString(round(bytes/1024d)) + " MB";
 		} else {
 			bytesString = Double.toString(round(bytes/1024d)) + " GB";
 		}
 		return bytesString;
 	}
 	
 	public static float round(float f) {
 		return (long)(f*100)/100f;
 	}
 	 	
 	public static double round(double d) {
 		return (long)(d*100)/100d;
 	}

	public static void main(String[] args) {
		//enable dynamic layout during application resize
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (!toolkit.isDynamicLayoutActive()) {
			toolkit.setDynamicLayout(true);
		}
		
		Options options = new Options();
		Option hostNameOption = OptionBuilder.withArgName("host name")
											 .hasArg()
											 .withDescription("The host name of the RBNB server")
											 .withLongOpt("host")
											 .create('h');
		Option portNumberOption = OptionBuilder.withArgName("port number")
											   .hasArg()
											   .withDescription("The port number of the RBNB server")
											   .withLongOpt("port")
											   .create('p');
		Option channelsOption = OptionBuilder.withArgName("channels")
											 .hasArgs()
											 .withDescription("Channels to subscribe to")
											 .withLongOpt("channels")
											 .create('c');
		Option timeScaleOption = OptionBuilder.withArgName("time scale")
											  .hasArg()
											  .withDescription("The time scale in seconds")
											  .withLongOpt("time-scale")
											  .create('s');
		Option playOption = OptionBuilder.withDescription("Start playing back data") 
										 .withLongOpt("play")
										 .create();
		Option realTimeOption = OptionBuilder.withDescription("Start viewing data in real time") 
		 									 .withLongOpt("real-time")
											 .create();
		Option helpOption = new Option("?", "help", false, "Display usage");
		options.addOption(hostNameOption);
		options.addOption(portNumberOption);
		options.addOption(channelsOption);
		options.addOption(timeScaleOption);
		options.addOption(playOption);
		options.addOption(realTimeOption);			
		options.addOption(helpOption);
				
		String hostName = null;
		int portNumber = -1;
		String[] channels = null;
		double timeScale = -1;
		boolean play = false;
		boolean realTime = false;
		
		CommandLineParser parser = new PosixParser();
		
		try {
	    	CommandLine line = parser.parse(options, args);
	    	
	    	if (line.hasOption('?')) {
	    		HelpFormatter formatter = new HelpFormatter();
	    		formatter.printHelp("DataViewer", options, true);
	    		return;
	    	}
	    	
	    	if (line.hasOption('h')) {
	    		hostName = line.getOptionValue('h');
	    		log.info("Set host name to " + hostName + ".");
	    	}
	    	
	    	if (line.hasOption('p')) {
	    		String value = line.getOptionValue('p');
	    		portNumber = Integer.parseInt(value);
	    		log.info("Set port number to " + portNumber + ".");
	    	}
	    	
	    	if (line.hasOption('c')) {
	    		channels = line.getOptionValues('c');
	    	}
	    	
	    	if (line.hasOption('s')) {
	    		String value = line.getOptionValue('s');
	    		timeScale = Double.parseDouble(value);
	    	}
	    	
	    	if (line.hasOption("play")) {
	    		play = true;
	    	} else if (line.hasOption("real-time")) {
	    		realTime = true;
	    	}
		} catch( ParseException e) {
			log.error("Command line arguments invalid: " + e.getMessage());
			return;
	    }
		
		DataViewer dataViewer;
		if (hostName == null && portNumber != -1) {
			log.error("You must specify a host name if a port number is given.");
			return;
		} else if (hostName != null && portNumber == -1) {
			log.info("Starting data viewer connected to " + hostName + ".");
			dataViewer = new DataViewer(hostName);
		} else if (hostName != null && portNumber != -1) {
			log.info("Starting data viewer connected to " + hostName + ":" + portNumber + ".");
			dataViewer = new DataViewer(hostName, portNumber);
		} else {
			log.info("Starting data viewer in disconnected state.");
			dataViewer = new DataViewer();
		}
		
		if (timeScale != -1) {
			dataViewer.setDomain(timeScale);
		}
		
		if (channels != null && hostName != null) {
			for (int i=0; i<channels.length; i++) {
				String channel = channels[i];
				log.info("Viewing channel " + channel + ".");
				if (!dataViewer.viewChannel(channel)) {
					log.error("Failed to view channel " + channel + ".");
				}
			}
		}
		
		Player player = dataViewer.getPlayer();
		if (play) {
			log.info("Starting data playback.");
			player.play();
		} else if (realTime) {
			log.info("Viewing data in real time.");
			player.monitor();
		}
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
