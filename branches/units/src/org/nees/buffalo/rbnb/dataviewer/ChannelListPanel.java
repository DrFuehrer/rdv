package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * @author Jason P. Hanley
 */
public class ChannelListPanel extends JPanel implements TreeModel, TreeSelectionListener, DragGestureListener, DragSourceListener, MouseListener, PlayerSubscriptionListener {

	static Log log = LogFactory.getLog(ChannelListPanel.class.getName());

	private DataViewer dataViewer;
	private ChannelTree ctree;
	private ChannelMap cmap;
	private HashMap units;
	
	private double startTime = -1;
	private double endTime = -1;
	
	private Sink sink = null;
	private final String rbnbSinkName = "RBNBDataViewer";

	private Object root;
	private ChannelTree.Node node;

	private Vector treeModelListeners = new Vector();

	private JTree tree;
 	private JScrollPane treeView;
	private JTextArea infoTextArea;
	
	private ArrayList channelListListeners;

 	private boolean connected = false;
	
	private static final String NEWLINE = "\r\n";

	public ChannelListPanel(DataViewer dataViewer) {
		super();
		
		this.dataViewer = dataViewer;
		
		channelListListeners =  new ArrayList();
		
		root = "";
		ctree = ChannelTree.createFromChannelMap(new ChannelMap());

		units = new HashMap();
		
		initPanel();
	}
	
	private void initPanel() {
		setLayout(new BorderLayout());

		tree = new JTree(this);
		tree.setExpandsSelectedPaths(true);
		tree.setCellRenderer(new ChannelTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		
		treeView = new JScrollPane(tree);

		infoTextArea = new JTextArea(6, 5);
		JScrollPane infoView = new JScrollPane(infoTextArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeView, infoView);
		splitPane.setResizeWeight(0.75);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		add(splitPane, BorderLayout.CENTER);
			
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_LINK, this);		
	}

 	public void connect() {
 		if (!connected) {
 			connected = true;
 			updateChannelList();
 		} else {
 			connected = true;
 		}
 	}
 	
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	public void disconnect() {
 		if (connected) {
 			clearChannelList();
 		}
 	}
 	
 	public void reconnect() {
 		disconnect();
 		connect();
 	}

	public void updateChannelListBackground() {
 		if (!connected) return;		

		new Thread(new Runnable() {
			public void run() {
				updateChannelList();
			}
		}, "ChannelUpdate").start();	
	}

	public synchronized void updateChannelList() {
 		if (!connected) return;

		log.debug("Updating channel listing.");
		
		if (!initRBNB()) {
			closeRBNB();
			clearChannelList();
			return;
		}
		
		try {
			sink.RequestRegistration();
		} catch (SAPIException e) {
			log.error("Failed to request channel listing.");
			closeRBNB();
			clearChannelList();
			return;
		}

		ChannelMap oldChannelMap = cmap;
		try {
			cmap = sink.Fetch(0);
		} catch (SAPIException e) {
			log.error("Failed to fetch list of available channels.");
			closeRBNB();
			clearChannelList();
			return;
		}
		
		log.debug("Received list of available channels.");
						
		getUnits();
		
		closeRBNB();
		
		fireChannelListUpdated(cmap);
				
 		ChannelTree oldChannelTree = ctree;
 		ctree = ChannelTree.createFromChannelMap(cmap);
  		
 		if (oldChannelMap != null) {
 			if (!root.equals(DataViewer.getRBNBHostName() + ":" + DataViewer.getRBNBPort())) {
 				root = DataViewer.getRBNBHostName() + ":" + DataViewer.getRBNBPort();
 				fireRootChanged();
 			} else {
 				String[] newChannelList = cmap.GetChannelList();
 				String[] oldChannelList = oldChannelMap.GetChannelList();
  		
 				//find channels added
 				for (int i=0; i<newChannelList.length; i++) {
 					if (oldChannelTree.findNode(newChannelList[i]) == null) {
 						ArrayList path = new ArrayList();
 						ChannelTree.Node node = ctree.findNode(newChannelList[i]);
 						while (node != null) {
 							path.add(node);
 							node = node.getParent();
 						}
  
 						log.debug("Found added channel " + newChannelList[i]);
 						
 						fireRootChanged();
 						//fireChannelAdded(path.toArray());
 					}
 				}
  
 				//find channels removed
 				for (int i=0; i<oldChannelList.length; i++) {
 					if (ctree.findNode(oldChannelList[i]) == null) {
 						ArrayList path = new ArrayList();
 						ChannelTree.Node node = oldChannelTree.findNode(oldChannelList[i]).getParent();
 						while (node != null) {
 							path.add(node);
 							node = node.getParent();
 						}
  		
 						log.debug("Found removed channel " + oldChannelList[i]);
 						
 						fireChannelRemoved(path.toArray());
 					}
 				}	
 			}
 		} else {
 			root = DataViewer.getRBNBHostName() + ":" + DataViewer.getRBNBPort();
 			fireRootChanged();			
  		}
  	}
  	
 	private synchronized void clearChannelList() {
 		log.debug("Clearing channel list.");
 		
  		root = "";
  		ctree = ChannelTree.createFromChannelMap(new ChannelMap());
  		
 		fireRootChanged();
 		
 		connected = false;
 	}
 	
 	private void getUnits() { 		
		//subscribe to all units channels
		ChannelMap unitsChannelMap = new ChannelMap();
		try {
			unitsChannelMap.Add("_Units/*");
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		//get the latest unit information
		try {
			//sink.Request(unitsChannelMap, 0, Double.MAX_VALUE, "absolute");
			sink.Request(unitsChannelMap, 0, 0, "newest");
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		//fetch the unit channel data
		try {
			unitsChannelMap = sink.Fetch(-1);
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		closeRBNB();
		
		String[] channels = unitsChannelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			String parent = channelName.substring(channelName.lastIndexOf("/")+1);
			int channelIndex = unitsChannelMap.GetIndex(channelName);
			String[] data = unitsChannelMap.GetDataAsString(channelIndex);
			String newestData = data[data.length-1];
			String[] channelTokens = newestData.split("\t,");
			for (int j=0; j<channelTokens.length; j++) {
				String[] tokens = channelTokens[j].split("=");
				if (tokens.length == 2) {
					String channel = parent + "/" + tokens[0].trim();
					String unit = tokens[1].trim();
					log.debug("Got unit (" + unit + ") for channel " + channel + ".");
					units.put(channel, unit);
				} else {
					log.debug("Invalid unit string: " + channelTokens[j] + ".");
				}
			}
		}
 	}
 	
 	public String getUnit(String channel) {
 		return (String)units.get(channel);
 	}
 	
 	private void fireRootChanged() {
  		TreeModelEvent e = new TreeModelEvent(this, new Object[] {root});
 		for (int i = 0; i < treeModelListeners.size(); i++) {
 			((TreeModelListener)treeModelListeners.elementAt(i)).treeStructureChanged(e);
 		}		
 	}
 	
 	private void fireChannelAdded(Object[] path) {
 		TreeModelEvent e = new TreeModelEvent(this, path);
 		for (int i = 0; i < treeModelListeners.size(); i++) {
 			((TreeModelListener)treeModelListeners.elementAt(i)).treeNodesInserted(e);
 		}
 	}
 	
 	private void fireChannelRemoved(Object[] path) {
 		TreeModelEvent e = new TreeModelEvent(this, path);
 		for (int i = 0; i < treeModelListeners.size(); i++) {
  			((TreeModelListener)treeModelListeners.elementAt(i)).treeStructureChanged(e);
  		}
  	}
	
	private synchronized boolean initRBNB() {
		if (sink == null) {			
			sink = new Sink();
		}  else {
			return true;
		}
		
		try {
			sink.OpenRBNBConnection(DataViewer.getRBNBHostName() + ":" + DataViewer.getRBNBPort(), rbnbSinkName);
		} catch (SAPIException e) {
			log.error("Failed to connect to RBNB server.");
			return false;	
		}
		
		log.debug("Connected to RBNB server.");
		
		return true;
	}

	private synchronized boolean closeRBNB() {
		if (sink == null) return true;
			
		sink.CloseRBNBConnection();
		sink = null;

		log.debug("Connection to RBNB server closed.");
		
		return true;
	}
	
	private synchronized boolean reInitRBNB() {
		if (!closeRBNB()) {
			return false;
		}
		
		if (!initRBNB()) {
			return false;
		}
		
		return true;
	}
	
	
	public Object getRoot() {
		return root;
	}

	public Object getChild(Object n, int index) {
		Iterator it;
		
		if (n.equals(root)) {
			it = ctree.rootIterator();
		} else {
			node = (ChannelTree.Node)n;
			it = node.getChildren().iterator();
		}
		
		int i=0;
		while (i<=index && it.hasNext()) {
			node = (ChannelTree.Node)it.next();
			if (node.getType() == ChannelTree.SERVER || node.getType() == ChannelTree.SOURCE || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
				i++;
			}
		}
		
		return node;
	}

	public int getChildCount(Object n) {
		Iterator it;
		
		if (n.equals(root)) {
			it = ctree.rootIterator();
		} else {
			node = (ChannelTree.Node)n;
			it = node.getChildren().iterator();
		}
		
		int i=0;
		while (it.hasNext()) {
			node = (ChannelTree.Node)it.next();
			if (node.getType() == ChannelTree.SERVER || node.getType() == ChannelTree.SOURCE || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
				i++;
			}
		}		
		
		return i;
	}

	public boolean isLeaf(Object n) {	
		boolean isLeaf = (getChildCount(n) == 0) ? true : false;
		
		return isLeaf;
	}

	public void valueForPathChanged(TreePath path, Object n) {}

	public int getIndexOfChild(Object n, Object child) {
		Iterator it;
		
		if (n.equals(root)) {
			it = ctree.rootIterator();
		} else {
			node = (ChannelTree.Node)n;
			it = node.getChildren().iterator();
		}
		
		int i=0;
		while (it.hasNext()) {
			node = (ChannelTree.Node)it.next();
			if (node.getType() == ChannelTree.SERVER || node.getType() == ChannelTree.SOURCE || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
				if (node.equals(child)) {
					return i;
				}
			}
		}			

		return -1;		
	}

	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.addElement(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.removeElement(l);
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		infoTextArea.setText("");
		
		TreePath[] treePaths = e.getPaths();
		Object o;
		for (int i=0; i<treePaths.length; i++) {
			o = treePaths[i].getLastPathComponent();
			if (o.equals(root)) {
				if (e.isAddedPath(i)) {
					infoTextArea.append("Server: " + root);
				}
			} else {
				node = (ChannelTree.Node)o;
				if (e.isAddedPath(i)) {
					infoTextArea.append("Type: " + node.getType() + NEWLINE);
					infoTextArea.append("Name: " + node.getFullName() + NEWLINE);
					if (node.getType() == ChannelTree.CHANNEL) {
						infoTextArea.append("Mime: " + node.getMime() + NEWLINE);
						infoTextArea.append("Start: " + DataViewer.formatDate(node.getStart()) + NEWLINE);
						infoTextArea.append("Duriation: " + DataViewer.formatSeconds(node.getDuration()) + NEWLINE);
						infoTextArea.append("Size: " + DataViewer.formatBytes(node.getSize()));
						
						/* infoTextArea.append(NEWLINE + NEWLINE);
						String channelName = node.getFullName();
						int channelIndex = cmap.GetIndex(channelName);
						try {
							String[] metaData = cmap.GetDataAsString(channelIndex);					
							for (int j=0; j<metaData.length; j++) {
								infoTextArea.append(metaData[j] + NEWLINE);
							}
						} catch (ClassCastException cce) {
							log.warn("Failed to get metadata for channel " + channelName);
						} */
					}
				}
			}
			
			if (i == treePaths.length-1) {
				infoTextArea.append(NEWLINE + NEWLINE);				
			}
		}
		
		infoTextArea.setCaretPosition(0);
	}
	
	public void dragGestureRecognized(DragGestureEvent e) {
		Point p = e.getDragOrigin();
		TreePath treePath = tree.getPathForLocation((int)p.getX(), (int)p.getY());
		if (treePath != null) {
			Object o = treePath.getLastPathComponent();
			if (!o.equals(root)) {
				ChannelTree.Node node = (ChannelTree.Node)o;
				if (node.getType() == ChannelTree.CHANNEL) {
					String channelName = node.getFullName();
					String unit = getUnit(channelName);
					String data = channelName;
					if (unit != null) {
						data += "\t" + unit;
					}
					e.startDrag(DragSource.DefaultLinkDrop, new StringSelection(data), this);
				}
			}
		}
	}
	
	public void dragEnter(DragSourceDragEvent dsde) {}
	public void dragOver(DragSourceDragEvent dsde) {}
	public void dropActionChanged(DragSourceDragEvent dsde) {}
	public void dragExit(DragSourceEvent dse) {}
	public void dragDropEnd(DragSourceDropEvent dsde) {}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
			if (treePath != null) {
				Object o = treePath.getLastPathComponent();
				if (!o.equals(root)) {
					ChannelTree.Node node = (ChannelTree.Node)o;
					if (node.getType() == ChannelTree.CHANNEL) {
						String channelName = node.getFullName();
						String mime = node.getMime();
						String unit = (String)units.get(channelName);
						dataViewer.viewChannel(channelName, mime, unit);
					}
				}
			}
			
		}
	}
	
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	private class ChannelTreeCellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			if (!value.equals(root)) {
				ChannelTree.Node node = (ChannelTree.Node)value;
				setText(node.getName());
 			} else if (value.equals("")) {
 				setIcon(null);
			}
			return c;
		}
		
	}

	public void channelSubscribed(String channelName) {
		updateChannelListBackground();				
	}

	public void channelUnsubscribed(String channelName) {
		updateChannelListBackground();		
	}
	
	public void channelChanged(String unsubscribedChannelName, String subscribedChannelName) {
		updateChannelListBackground();
	}
	
	public void addChannelListListener(ChannelListListener listener) {
		channelListListeners.add(listener);
	}

	public void removeChannelListListener(ChannelListListener listener) {
		channelListListeners.remove(listener);
	}
	
	private void fireChannelListUpdated(ChannelMap channelMap) {
		ChannelListListener listener;
		for (int i=0; i<channelListListeners.size(); i++) {
			listener = (ChannelListListener)channelListListeners.get(i);
			listener.channelListUpdated(channelMap);
		}
	}
}
