/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataPanelManager;
import org.rdv.DataViewer;
import org.rdv.Extension;
import org.rdv.action.ActionFactory;
import org.rdv.rbnb.Channel;
import org.rdv.rbnb.MetadataListener;
import org.rdv.rbnb.Player;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBUtilities;
import org.rdv.rbnb.StateListener;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.NodeTypeEnum;

import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A panel that contains the channels in a tree.
 * 
 * @author Jason P. Hanley
 */
public class ChannelListPanel extends JPanel implements MetadataListener, StateListener {

  /** serialization version identifier */
  private static final long serialVersionUID = -5091214984427475802L;

	static Log log = LogFactory.getLog(ChannelListPanel.class.getName());

	private DataPanelManager dataPanelManager;
	private RBNBController rbnb;
	
  private List<ChannelSelectionListener> channelSelectionListeners;
  
  private JTextField filterTextField;
  private JButton clearFilterButton;

	private JTree tree;
  private ChannelTreeModel treeModel;
  
  private JButton metadataUpdateButton;
  
	public ChannelListPanel(DataPanelManager dataPanelManager, RBNBController rbnb) {
		super();
		
		this.dataPanelManager = dataPanelManager;
		this.rbnb = rbnb;
		
    channelSelectionListeners = new ArrayList<ChannelSelectionListener>();
		
		initPanel();
	}
	
  /**
   * Create the main UI panel.
   */
	private void initPanel() {
		setLayout(new BorderLayout());
    setMinimumSize(new Dimension(130, 27));

    JComponent filterComponent = createFilterPanel();
    JComponent treePanel = createTreePanel();
    
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(filterComponent, BorderLayout.NORTH);
    mainPanel.add(treePanel, BorderLayout.CENTER);
    
    JToolBar channelToolBar = createToolBar();
    
    SimpleInternalFrame treeFrame = new SimpleInternalFrame(
        DataViewer.getIcon("icons/channels.gif"),
        "Channels",
        channelToolBar,
        mainPanel);        
    add(treeFrame, BorderLayout.CENTER);
	}
  
  /**
   * Create the UI panel that contains the controls to filter the channel list.
   * 
   * @return  the UI component dealing with filtering
   */
  private JComponent createFilterPanel() {
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BorderLayout(5, 5));
    filterPanel.setBackground(Color.white);
    filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JLabel filterIconLabel = new JLabel(DataViewer.getIcon("icons/filter.gif"));
    filterPanel.add(filterIconLabel, BorderLayout.WEST);
    
    filterTextField = new JTextField();
    filterTextField.setToolTipText("Enter text here to filter the channel list");
    filterTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        treeModel.setFilter(filterTextField.getText());
      }
      public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
      public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
    });
    filterPanel.add(filterTextField, BorderLayout.CENTER);
    
    Action focusFilterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        filterTextField.requestFocusInWindow();
        filterTextField.selectAll();
      }
    };
    
    int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    filterTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier), "focusFilter");
    filterTextField.getActionMap().put("focusFilter", focusFilterAction);
    
    Action cancelFilterAction = new AbstractAction(null, DataViewer.getIcon("icons/cancel.gif")) {
      public void actionPerformed(ActionEvent e) {
        treeModel.setFilter(null);
      }
    };
    cancelFilterAction.putValue(Action.SHORT_DESCRIPTION, "Cancel filter");
    
    filterTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelFilter");
    filterTextField.getActionMap().put("cancelFilter", cancelFilterAction);    
    
    clearFilterButton = new JButton(cancelFilterAction);
    clearFilterButton.setBorderPainted(false);
    clearFilterButton.setVisible(false);
    filterPanel.add(clearFilterButton, BorderLayout.EAST);
    
    return filterPanel;
  }
  
  /**
   * Create the channel tree panel.
   * 
   * @return  the component containing the channel tree
   */
  private JComponent createTreePanel() {
    treeModel = new ChannelTreeModel();
    treeModel.addPropertyChangeListener(new FilterPropertyChangeListener());
    
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setExpandsSelectedPaths(true);
    tree.setCellRenderer(new ChannelTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.addTreeSelectionListener(new ChannelTreeSelectionListener());
    tree.addMouseListener(new ChannelTreeMouseListener());
    tree.setBorder(new EmptyBorder(0, 5, 5, 5));
    
    JScrollPane treeView = new JScrollPane(tree);
    treeView.setBorder(null);
    treeView.setViewportBorder(null);
    
    tree.setDragEnabled(true);
    tree.setTransferHandler(new ChannelTransferHandler());
    
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(tree,
      DnDConstants.ACTION_LINK,
      new ChannelDragGestureListener());    

    return treeView;
  }
  
  /**
   * Create the tool bar.
   * 
   * @return  the tool bar for the channel list panel
   */
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
  
	public void channelTreeUpdated(final ChannelTree newChannelTree) {
 		ChannelTree ctree = newChannelTree;
    
    TreePath[] paths = tree.getSelectionPaths();
    
    boolean structureChanged = treeModel.setChannelTree(ctree);
		if (structureChanged) {
			tree.clearSelection();
			
	 		if (paths != null) {
	 			for (int i=0; i<paths.length; i++) {
		 			Object o = paths[i].getLastPathComponent();
		 			if (o instanceof ChannelTree.Node) {
		 				ChannelTree.Node node = (ChannelTree.Node)o;
		 				selectNode(node.getFullName());
		 			} else {
		 				selectRootNode();
		 			}
	 			}
	 		}
    }
	}

  private class FilterPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent pce) {
      if (!pce.getPropertyName().equals("filter")) {
        return;
      }
      
      String filterText = (String)pce.getNewValue();

      if (filterText.compareToIgnoreCase(filterTextField.getText()) != 0) {
        filterTextField.setText(filterText);
      }
      
      clearFilterButton.setVisible(filterText.length() > 0);
      
      if (filterText.length() > 0) {
        expandTree();
      }      
    }    
  }
  
  public List<String> getSelectedChannels() {
    ArrayList<String> selectedChannels = new ArrayList<String>();
    
    TreePath[] selectedPaths = tree.getSelectionPaths();
    if (selectedPaths != null) {
      for (int i=0; i<selectedPaths.length; i++) {
        if (selectedPaths[i].getLastPathComponent() != treeModel.getRoot()) {
          ChannelTree.Node selectedNode = (ChannelTree.Node)selectedPaths[i].getLastPathComponent();
          NodeTypeEnum type = selectedNode.getType();
          if (type == ChannelTree.SOURCE) {
            selectedChannels.addAll(RBNBUtilities.getChildChannels(selectedNode, treeModel.isHiddenChannelsVisible()));
          } else if (type == ChannelTree.CHANNEL) {
            selectedChannels.add(selectedNode.getFullName());
          }
        }
      }
    }
    
    return selectedChannels;
  }
  
  public void addChannelSelectionListener(ChannelSelectionListener e) {
    channelSelectionListeners.add(e);
  }
  
  public void removeChannelSelectionListener(ChannelSelectionListener e) {
    channelSelectionListeners.remove(e);
  }
  
  private void fireChannelSelected(Object o) {
    boolean isRoot = (o == treeModel.getRoot());
    int children = treeModel.getChildCount(o);
    String channelName = null;
    if (!isRoot) {
      ChannelTree.Node node = (ChannelTree.Node)o;
      channelName = node.getFullName();
    }
      
    ChannelSelectionEvent cse = new ChannelSelectionEvent(channelName, children, isRoot);
    
    Iterator i = channelSelectionListeners.iterator();
    while (i.hasNext()) {
      ChannelSelectionListener csl = (ChannelSelectionListener)i.next();
      csl.channelSelected(cse);
    }
  }
  
  private void fireNoChannelsSelected() {
    Iterator i = channelSelectionListeners.iterator();
    while (i.hasNext()) {
      ChannelSelectionListener csl = (ChannelSelectionListener)i.next();
      csl.channelSelectionCleared();
    }    
  }
  
  public boolean isShowingHiddenChannles() {
    return treeModel.isHiddenChannelsVisible();
  }
	
	public void showHiddenChannels(boolean showHiddenChannels) {
    treeModel.setHiddenChannelsVisible(showHiddenChannels);
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
  }
 	 	
 	private void selectRootNode() {
 		tree.addSelectionPath(new TreePath(treeModel.getRoot()));
 	}
 	
 	private void selectNode(String nodeName) {
 		ChannelTree.Node node = treeModel.getChannelTree().findNode(nodeName);
 		if (node != null) {
 			int depth = node.getDepth();
 			Object[] path = new Object[depth+2];
 			path[0] = treeModel.getRoot();
 			for (int i=path.length-1; i>0; i--) {
 				path[i] = node;
 				node = node.getParent();
 			}
 			
 			tree.addSelectionPath(new TreePath(path));
 		}
 	}
  
  public class ChannelTreeSelectionListener implements TreeSelectionListener {
    public void valueChanged(TreeSelectionEvent e) {
      TreePath selectedPath = e.getNewLeadSelectionPath();
      if (selectedPath == null) {
        fireNoChannelsSelected();
      } else {
        Object o = selectedPath.getLastPathComponent();
        fireChannelSelected(o);
      }
    }
    
  }
  
	public Transferable createChannelsTransferable() {
    List<String> channels = getSelectedChannels();
    
    if (channels.size() == 0) {
      return null;
    }
    
    return new ChannelListTransferable(channels); 
  }
  
  private class ChannelTransferHandler extends TransferHandler {

    /** serialization version identifier */
    private static final long serialVersionUID = 5965378439143524577L;

    public int getSourceActions(JComponent c) {
      return DnDConstants.ACTION_LINK;
    }
    
    protected Transferable createTransferable(JComponent c) {
      return createChannelsTransferable();
    }
  }
  
  private class ChannelDragGestureListener implements DragGestureListener {	
  	public void dragGestureRecognized(DragGestureEvent dge) {
      Transferable transferable = createChannelsTransferable();
      if (transferable == null) {
        return;
      }
      
      try {
        dge.startDrag(DragSource.DefaultLinkDrop, transferable);
      } catch (InvalidDnDOperationException e) {}
  	}
  }
  
  private class ChannelTreeMouseListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        handleDoubleClick(e);
      }
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handlePopup(e); 
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handlePopup(e); 
      }  
    }    
  }
  
  private void handleDoubleClick(MouseEvent e) {
    TreePath treePath = tree.getSelectionPath();
    if (treePath == null) {
      return;
    }
    
    Object o = treePath.getLastPathComponent();
    if (o != treeModel.getRoot()) {
      ChannelTree.Node node = (ChannelTree.Node)o;
      if (node.getType() == ChannelTree.CHANNEL) {
        String channelName = node.getFullName();
        viewChannel(channelName);
      }
    }
  }
  
  private void handlePopup(MouseEvent e) {    
    TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());        
    if (treePath == null) {
      return;
    }
    
    // select only the node under the mouse if it is not already selected
    if (!tree.isPathSelected(treePath)) {
      tree.setSelectionPath(treePath);
    }
    
    JPopupMenu popup = null;
    
    Object o = treePath.getLastPathComponent();
    if (o == treeModel.getRoot()) {
      popup = getRootPopup();
    } else {
      ChannelTree.Node node = (ChannelTree.Node)o;
      
      if (node.getType() == ChannelTree.SOURCE) {
        popup = getSourcePopup(node);
      } else if (node.getType() == ChannelTree.CHANNEL) {
        popup = getChannelPopup(node);
      }
    }
    
    if (popup != null && popup.getComponentCount() > 0) {
      popup.show(tree, e.getX(), e.getY());
    }
  } 
  
  private JPopupMenu getRootPopup() {
    JPopupMenu popup = new JPopupMenu();
    
    JMenuItem menuItem = new JMenuItem("Import data...", DataViewer.getIcon("icons/import.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ActionFactory.getInstance().getDataImportAction().importData();
      }
    });
    popup.add(menuItem);
    
    menuItem = new JMenuItem("Export data...", DataViewer.getIcon("icons/export.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ActionFactory.getInstance().getDataExportAction().exportData(
            RBNBUtilities.getAllChannels(treeModel.getChannelTree(),
                                         treeModel.isHiddenChannelsVisible()));
      }
    });
    popup.add(menuItem);
    
    
    return popup;
  }
  
  private JPopupMenu getSourcePopup(final ChannelTree.Node source) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem;
    
    final String sourceName = source.getFullName();

    List extensions = getExtensionsForSource(source);          
    Iterator i = extensions.iterator();
    while (i.hasNext()) {
      final Extension extension = (Extension)i.next();
      
      menuItem = new JMenuItem("View source with " + extension.getName());
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          List channels = RBNBUtilities.getSortedChildren(source, treeModel.isHiddenChannelsVisible());
          viewChannels(channels, extension);
        }                
      });
      popup.add(menuItem);
    }
    
    if (extensions.size() > 0) {
      popup.addSeparator();
    }          
     
    if (dataPanelManager.isSourceSubscribed(sourceName)) {
      menuItem = new JMenuItem("Unsubscribe from source");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          dataPanelManager.unsubscribeSource(sourceName, true);
        }
      });
      popup.add(menuItem);
      popup.addSeparator();
    }
    
    menuItem = new JMenuItem("Export data source...", DataViewer.getIcon("icons/export.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ActionFactory.getInstance().getDataExportAction().exportData(
            RBNBUtilities.getChildChannels(source,
                                           treeModel.isHiddenChannelsVisible()));
      }
    });
    popup.add(menuItem);          
    
    return popup;
  }
  
  private JPopupMenu getChannelPopup(ChannelTree.Node channel) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem;

    final List<ChannelTree.Node> selectedChannels = new ArrayList<ChannelTree.Node>();   
 
    TreePath[] selectedNodes = tree.getSelectionPaths();
    if (selectedNodes != null) {
      for (int i=0; i<selectedNodes.length; i++) {
        if (selectedNodes[i].getLastPathComponent() != treeModel.getRoot()) {
          ChannelTree.Node selectedNode = (ChannelTree.Node)selectedNodes[i].getLastPathComponent();
          NodeTypeEnum type = selectedNode.getType();
          if (type == ChannelTree.CHANNEL) {
            selectedChannels.add(selectedNode);
          }
        }
      }
    }
        
    final String channelName = channel.getFullName();
    final Channel channelMetadata = rbnb.getMetadataManager().getChannel(channelName);

    ArrayList extensions = dataPanelManager.getExtensions(channelName);
    final Extension defaultExtension = dataPanelManager.getDefaultExtension(channelName);
    if (defaultExtension != null) {
      menuItem = new JMenuItem("View with " + defaultExtension.getName());
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          viewChannels(selectedChannels, defaultExtension);
        }                
      });            
      popup.add(menuItem);
      popup.addSeparator();
      
      if (extensions.size() > 1) {
        Iterator i = extensions.iterator();
        while (i.hasNext()) {
          final Extension extension = (Extension)i.next();
          if (extension != defaultExtension) {
            menuItem = new JMenuItem("View with " + extension.getName());
            menuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                viewChannels(selectedChannels, extension);
              }                
            });
            popup.add(menuItem);
          }
        }
      
        popup.addSeparator();
      }      
    }
    
    if (dataPanelManager.isChannelSubscribed(channelName)) {
      menuItem = new JMenuItem("Unsubscribe from channel");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          dataPanelManager.unsubscribeChannel(channelName, true);
        }            
      });
      popup.add(menuItem);
      popup.addSeparator();
    }

    String mime = channelMetadata.getMetadata("mime");
    if (mime.equals("application/octet-stream")) {
      String plural = selectedChannels.size()==1?"":"s";
      menuItem = new JMenuItem("Export channel" + plural + "...", DataViewer.getIcon("icons/export.gif"));
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          List<String> selectedChannelsAsString = new ArrayList<String>();
          for (ChannelTree.Node node : selectedChannels) {
            selectedChannelsAsString.add(node.getFullName());
          }
          ActionFactory.getInstance().getDataExportAction().exportData(
              selectedChannelsAsString);
        }
      });
      popup.add(menuItem);
    } else {
      popup.remove(popup.getComponentCount()-1);
    }
  
    return popup;    
  }
  
  private void viewChannel(final String channelName) {
    log.debug("viewChannel() - start.. channleName: " + channelName);
    new Thread() {
      public void run() {
        dataPanelManager.viewChannel(channelName);
      }
    }.start();
  }
  
  private void viewChannels(final List channels, final Extension extension) {
    new Thread() {
      public void run() {
        dataPanelManager.viewChannels(channels, extension);
      }
    }.start();
  }
  
  private List getExtensionsForSource(ChannelTree.Node source) {
    List<Extension> extensions = new ArrayList<Extension>();
    
    List children = RBNBUtilities.getSortedChildren(source, treeModel.isHiddenChannelsVisible());
    Iterator it = children.iterator();
    while (it.hasNext()) {
      String channelName = ((ChannelTree.Node)it.next()).getFullName();
      ArrayList channelExtensions = dataPanelManager.getExtensions(channelName);
      for (int i=0; i<channelExtensions.size(); i++) {
        Extension e = (Extension)channelExtensions.get(i);
        if (!extensions.contains(e)) {
          extensions.add(e);
        }
      }
    }
    
    return extensions;
  }
  
	public void postState(int newState, int oldState) {
		if (newState == Player.STATE_DISCONNECTED || newState == Player.STATE_EXITING) {
			metadataUpdateButton.setEnabled(false);
      filterTextField.setEnabled(false);
      clearFilterButton.setEnabled(false);
      tree.setEnabled(false);
		} else {
			metadataUpdateButton.setEnabled(true);
      filterTextField.setEnabled(true);
      clearFilterButton.setEnabled(true);
      tree.setEnabled(true);
		}
	}
}