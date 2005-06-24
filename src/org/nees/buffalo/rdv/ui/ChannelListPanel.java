package org.nees.buffalo.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
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
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.StateListener;

import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.NodeTypeEnum;

/**
 * @author Jason P. Hanley
 */
public class ChannelListPanel extends JPanel implements TreeModel, TreeSelectionListener, DragGestureListener, DragSourceListener, MouseListener, MetadataListener, StateListener {

	static Log log = LogFactory.getLog(ChannelListPanel.class.getName());

	private DataPanelManager dataPanelManager;
	private RBNBController rbnb;
	
	private ChannelTree ctree;
	private ChannelMap cmap;
	
	private double startTime = -1;
	private double endTime = -1;
	
	private Object root;
	private ChannelTree.Node node;

	private Vector treeModelListeners = new Vector();

	private JTree tree;
	private JEditorPane infoTextArea;
  
  private JButton metadataUpdateButton;

 	private boolean showHiddenChannels = false;
	
	private static final String NEWLINE = "\r\n";

	public ChannelListPanel(DataPanelManager dataPanelManager, RBNBController rbnb) {
		super();
		
		this.dataPanelManager = dataPanelManager;
		this.rbnb = rbnb;
		
		root = "";
		cmap = new ChannelMap();
		ctree = ChannelTree.createFromChannelMap(cmap);
		
		initPanel();
	}
	
	private void initPanel() {
		setLayout(new BorderLayout());

		tree = new JTree(this);
    tree.putClientProperty(Options.TREE_LINE_STYLE_KEY, Options.TREE_LINE_STYLE_NONE_VALUE);
		tree.setExpandsSelectedPaths(true);
		tree.setCellRenderer(new ChannelTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
    tree.setBorder(new EmptyBorder(4, 4, 4, 4));
    JScrollPane treeView = new JScrollPane(tree);
    treeView.setBorder(null);
    
    JToolBar channelToolBar = createToolBar();
    
    SimpleInternalFrame treeViewFrame = new SimpleInternalFrame(
        DataViewer.getIcon("icons/channels.gif"),
        "Channels",
        channelToolBar,
        treeView);

		infoTextArea = new JEditorPane();
    infoTextArea.setEditable(false);
    infoTextArea.setContentType("text/html");
    infoTextArea.setMinimumSize(new Dimension(0, 0));
    infoTextArea.setBorder(new EmptyBorder(4, 4, 4, 4));
    
    SimpleInternalFrame infoViewFrame = new SimpleInternalFrame(
        DataViewer.getIcon("icons/properties.gif"),
        "Properties");
    infoViewFrame.setPreferredSize(new Dimension(150, 150));
    infoViewFrame.add(infoTextArea);
        
    JSplitPane splitPane = Factory.createStrippedSplitPane(
                             JSplitPane.VERTICAL_SPLIT,
                             treeViewFrame,
                             infoViewFrame,
                             0.65f);
    splitPane.setContinuousLayout(true);
    add(splitPane, BorderLayout.CENTER);
    setBorder(new EmptyBorder(8, 8, 8, 0));
		
		setPreferredSize(new Dimension(150, 0));
			
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_LINK, this);		
	}
  
  private JToolBar createToolBar() {
    JToolBar channelToolBar = new JToolBar();

    JButton button = new ToolBarButton(
        "icons/expandall.gif",
        "Expand channel list");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        expandTree();
      }
    });
    channelToolBar.add(button);

    button = new ToolBarButton(
        "icons/collapseall.gif",
        "Collapse channel list");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        collapseTree();
      }
    });
    channelToolBar.add(button);
    
    metadataUpdateButton = new ToolBarButton(
        "icons/refresh.gif",
        "Update channel list");
    metadataUpdateButton.setEnabled(false);
    metadataUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        rbnb.updateMetadata();
      }
    });
    channelToolBar.add(metadataUpdateButton);
    
    return channelToolBar;
  }
	
	public void channelListUpdated(ChannelMap newChannelMap) {
		ChannelMap oldChannelMap = cmap;
 		ChannelTree oldChannelTree = ctree;
 		cmap = newChannelMap;
 		ctree = ChannelTree.createFromChannelMap(cmap);
 		
 		if (!root.equals(rbnb.getRBNBConnectionString())) {
 				root = rbnb.getRBNBConnectionString();
 				fireRootChanged();
 				clearMetadata();
		} else {
			TreePath[] paths = tree.getSelectionPaths();
			
			boolean channelListChanged = false;
			
			String[] newChannelList = cmap.GetChannelList();
			String[] oldChannelList = oldChannelMap.GetChannelList();
	
			//find channels added
			Iterator newIterator = ctree.iterator();
			while (newIterator.hasNext()) {
				ChannelTree.Node node = (ChannelTree.Node)newIterator.next();
				NodeTypeEnum type = node.getType();
				if (type == ChannelTree.CHANNEL || type == ChannelTree.SOURCE) {
					if (oldChannelTree.findNode(node.getFullName()) == null) {
						log.info("Found new node: " + node.getFullName() + ".");
						channelListChanged = true;
						break;
					}
				}
			}
			
			if (!channelListChanged) {
				//find channels removed
				Iterator oldIterator = oldChannelTree.iterator();
				while (oldIterator.hasNext()) {
					ChannelTree.Node node = (ChannelTree.Node)oldIterator.next();
					NodeTypeEnum type = node.getType();
					if (type == ChannelTree.CHANNEL || type == ChannelTree.SOURCE) {
						if (ctree.findNode(node.getFullName()) == null) {
							log.info("Found deleted node: " + node.getFullName() + ".");
							channelListChanged = true;
							break;
						}
					}
				} 				
			}			
			
			if (channelListChanged) {
				tree.clearSelection();
				clearMetadata();
				fireRootChanged();
				
		 		if (paths != null) {
		 			for (int i=0; i<paths.length; i++) {
			 			Object o = paths[i].getLastPathComponent();
			 			if (o instanceof ChannelTree.Node) {
			 				ChannelTree.Node node = (ChannelTree.Node)o;
			 				selectNode(node.getFullName());
			 			} else if (o instanceof String) {
			 				selectRootNode();
			 			}
		 			}
		 		}
			}
		}
    
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object o = tp.getLastPathComponent();
      showMetadata(o);
    }
	}
	
	public void showHiddenChannels(boolean showHiddenChannels) {
		if (this.showHiddenChannels != showHiddenChannels) {
			this.showHiddenChannels = showHiddenChannels;
			fireRootChanged();
		}
	}
  	
 	private synchronized void clearChannelList() {
 		if (root.equals("")) {
 			return;
 		}
 		
 		log.info("Clearing channel list.");

 		root = "";
 		ctree = ChannelTree.createFromChannelMap(new ChannelMap());
  		
 		fireRootChanged();
  }
  
  private void expandTree() {
    for (int i=0; i<tree.getRowCount(); i++) {
      tree.expandRow(i);  
    }
  }
  
  private void collapseTree() {
    for (int i=tree.getRowCount()-1; i>=0; i--) {
      tree.collapseRow(i);  
    }
    tree.expandRow(0);
  }
 	 	
 	private void fireRootChanged() {
 		TreeModelEvent e = new TreeModelEvent(this, new Object[] {root});
  	for (int i = 0; i < treeModelListeners.size(); i++) {
  		((TreeModelListener)treeModelListeners.elementAt(i)).treeStructureChanged(e);
  	}		
 	}
 	
 	private void fireChannelAdded(Object[] path, Object child) {
 		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {0}, new Object[] {child});
 		for (int i = 0; i < treeModelListeners.size(); i++) {
 			((TreeModelListener)treeModelListeners.elementAt(i)).treeNodesInserted(e);
 		}
 	}
 	
 	private void fireChannelRemoved(Object[] path, Object child) {
 		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {0}, new Object[] {child});
 		for (int i = 0; i < treeModelListeners.size(); i++) {
  			((TreeModelListener)treeModelListeners.elementAt(i)).treeNodesRemoved(e);
  		}
  	}
	
 	private void selectRootNode() {
 		tree.addSelectionPath(new TreePath(root));
 	}
 	
 	private void selectNode(String nodeName) {
 		ChannelTree.Node node = ctree.findNode(nodeName);
 		if (node != null) {
 			int depth = node.getDepth();
 			Object[] path = new Object[depth+2];
 			path[0] = root;
 			for (int i=path.length-1; i>0; i--) {
 				path[i] = node;
 				node = node.getParent();
 			}
 			
 			tree.addSelectionPath(new TreePath(path));
 		}
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
			if (node.getType() == ChannelTree.SERVER || (node.getType() == ChannelTree.SOURCE && (showHiddenChannels || !node.getName().startsWith("_"))) || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
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
			if (node.getType() == ChannelTree.SERVER || (node.getType() == ChannelTree.SOURCE && (showHiddenChannels || !node.getName().startsWith("_"))) || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
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
			if (node.getType() == ChannelTree.SERVER || (node.getType() == ChannelTree.SOURCE && (showHiddenChannels || !node.getName().startsWith("_"))) || node.getType() == ChannelTree.CHANNEL || node.getType() == ChannelTree.FOLDER) {
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
		if (e.isAddedPath()) {
			TreePath treePaths = e.getPath();
			Object o = treePaths.getLastPathComponent();
			showMetadata(o);
		}		
	}
	
	private void showMetadata(Object o) {
		infoTextArea.setText("");
		
		if (o == null) {
			return;
		} else if (o.equals(root)) {
      StringBuffer s = new StringBuffer();
      s.append("<strong>" + rbnb.getRBNBHostName() + ":" + rbnb.getRBNBPortNumber() + "</strong><br>");
			s.append("<em>Data Server</em>");
      int sources = getChildCount(root);
      s.append("<p style=\"font-size: 10px\">" + sources + " Data Source");
      if (sources == 0 || sources > 1) {
        s.append("s");
      }
      s.append("</p>");
      infoTextArea.setText(s.toString());
		} else {
      StringBuffer s = new StringBuffer();
			node = (ChannelTree.Node)o;
			node = ctree.findNode(node.getFullName());
      String channelName = node.getFullName();
      if (node.getType() == ChannelTree.SOURCE) {
        s.append("<strong>" + channelName + "</strong><br>");        
        s.append("<em>Data Source</em>");
        
        int channels = getChildCount(o);
        s.append("<p style=\"font-size: 10px\">" + channels + " Channel");
        if (channels == 0 || channels > 1) {
          s.append("s");
        }
        s.append("</p>");        
      } else if (node.getType() == ChannelTree.CHANNEL) {
				int channelIndex = cmap.GetIndex(channelName);
				String mime = node.getMime();
				double start = cmap.GetTimeStart(channelIndex);
				double duration = cmap.GetTimeDuration(channelIndex);
				int size = node.getSize();
				String unit = ((Channel)rbnb.getChannel(channelName)).getUnit();

        s.append("<strong>" + channelName + "</strong>");
        if (unit != null) {
          s.append(" (" + unit + ")");
        }
        s.append("<br>");
        
        if (mime != null) {
          if (mime.equals("application/octet-stream")) {
            s.append("<em>Numeric Data</em><br>");
          } else if (mime.equals("image/jpeg")) {
            s.append("<em>JPEG Images</em><br>");
          } else if (mime.equals("video/jpeg")) {
            s.append("<em>JPEG Video</em><br>");
          }
        }
        
				s.append("<p style=\"font-size: 10px\">Starts at " + DataViewer.formatDateSmart(start) + "<br>");
				s.append("Lasts " + DataViewer.formatSeconds(duration) + "<br>");
				if (size != -1) {
					s.append(NEWLINE + "Size: " + DataViewer.formatBytes(size));
				}
        s.append("</p>");
			}
      infoTextArea.setText(s.toString());
		}
		
		infoTextArea.setCaretPosition(0);
	}
	
	private void clearMetadata() {
		infoTextArea.setText("");
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
					e.startDrag(DragSource.DefaultLinkDrop, new StringSelection(channelName), this);
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
						dataPanelManager.viewChannel(channelName);
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

	public void postState(int newState, int oldState) {
		if (newState == Player.STATE_DISCONNECTED || newState == Player.STATE_EXITING) {
			clearChannelList();
			clearMetadata();
      metadataUpdateButton.setEnabled(false);
		} else {
      metadataUpdateButton.setEnabled(true);
    }
	}
}
