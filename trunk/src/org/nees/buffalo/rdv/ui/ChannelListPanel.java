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
 * $URL: svn+ssh://jphanley@code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/ui/ChannelListPanel.java $
 * $Revision: 477 $
 * $Date: 2006-04-05 18:19:16 -0400 (Wed, 05 Apr 2006) $
 * $Author: jphanley $
 */

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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
import org.nees.buffalo.rdv.Extension;
import org.nees.buffalo.rdv.rbnb.MetadataListener;
import org.nees.buffalo.rdv.rbnb.Player;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.buffalo.rdv.rbnb.RBNBUtilities;
import org.nees.buffalo.rdv.rbnb.StateListener;

import com.jgoodies.looks.Options;
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
  private ApplicationFrame frame;
	
	private ChannelTree ctree;
	
	private Object root;
  private final static Object EMPTY_ROOT = new Object();
	private ChannelTree.Node node;

	private ArrayList treeModelListeners;  
  private ArrayList channelSelectionListeners; 

	private JTree tree;
  private SimpleInternalFrame treeFrame;
  
 	private boolean showHiddenChannels = false;
  
  private JButton metadataUpdateButton;

	public ChannelListPanel(DataPanelManager dataPanelManager, RBNBController rbnb, ApplicationFrame frame) {
		super();
		
		this.dataPanelManager = dataPanelManager;
		this.rbnb = rbnb;
    this.frame = frame;
		
		root = EMPTY_ROOT;
    
		ctree = ChannelTree.EMPTY_TREE;
    
    treeModelListeners = new ArrayList();
    channelSelectionListeners = new ArrayList();
		
		initPanel();
    
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_LINK, this);    
	}
	
	private void initPanel() {
    setBorder(null);
		setLayout(new BorderLayout());
    setMinimumSize(new Dimension(130, 27));

    JComponent treeView = createTree();
    JToolBar channelToolBar = createToolBar();
    
    treeFrame = new SimpleInternalFrame(
        DataViewer.getIcon("icons/channels.gif"),
        "Channels",
        channelToolBar,
        treeView);        
    add(treeFrame, BorderLayout.CENTER);
	}
  
  private JComponent createTree() {
    tree = new JTree(this);
    tree.setRootVisible(true);
    tree.setShowsRootHandles(false);
    tree.putClientProperty(Options.TREE_LINE_STYLE_KEY, Options.TREE_LINE_STYLE_NONE_VALUE);
    tree.setExpandsSelectedPaths(true);
    tree.setCellRenderer(new ChannelTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    tree.addMouseListener(this);
    tree.setBorder(new EmptyBorder(4, 4, 4, 4));
    
    JScrollPane treeView = new JScrollPane(tree);
    treeView.setBorder(null);
    
    return treeView;
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
  
	public void channelTreeUpdated(ChannelTree newChannelTree) {
 		ChannelTree oldChannelTree = ctree;
 		ctree = newChannelTree;
 		
 		if (!root.equals(rbnb.getRBNBConnectionString())) {
 				root = rbnb.getRBNBConnectionString();
 				fireRootChanged();
 				fireNoChannelsSelected();
		} else {
			TreePath[] paths = tree.getSelectionPaths();
			
			boolean channelListChanged = false;
			
			//find channels added
			Iterator newIterator = ctree.iterator();
			while (newIterator.hasNext()) {
				ChannelTree.Node node = (ChannelTree.Node)newIterator.next();
				NodeTypeEnum type = node.getType();
				if (type == ChannelTree.CHANNEL || type == ChannelTree.FOLDER ||
                    type == ChannelTree.SERVER || type == ChannelTree.SOURCE ||
                    type == ChannelTree.PLUGIN) {
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
					if (type == ChannelTree.CHANNEL || type == ChannelTree.FOLDER ||
                        type == ChannelTree.SERVER || type == ChannelTree.SOURCE ||
                        type == ChannelTree.PLUGIN) {
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
				fireNoChannelsSelected();
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
      fireChannelSelected(o);
    }
	}
  
  public ArrayList getSelectedChannels() {
    ArrayList selectedChannels = new ArrayList();
    
    TreePath[] selectedPaths = tree.getSelectionPaths();
    if (selectedPaths != null) {
      for (int i=0; i<selectedPaths.length; i++) {
        if (selectedPaths[i].getLastPathComponent() != root) {
          ChannelTree.Node selectedNode = (ChannelTree.Node)selectedPaths[i].getLastPathComponent();
          NodeTypeEnum type = selectedNode.getType();
          if (type == ChannelTree.SOURCE) {
            selectedChannels.addAll(RBNBUtilities.getChildChannels(selectedNode, showHiddenChannels));
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
    boolean isRoot = (o == root);
    int children = getChildCount(o);
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
    return showHiddenChannels;
  }
	
	public void showHiddenChannels(boolean showHiddenChannels) {
		if (this.showHiddenChannels != showHiddenChannels) {
			this.showHiddenChannels = showHiddenChannels;
			fireRootChanged();
		}
	}
  	
 	private synchronized void clearChannelList() {
 		if (root.equals(EMPTY_ROOT)) {
 			return;
 		}
 		
 		log.info("Clearing channel list.");

 		root = EMPTY_ROOT;
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
  }
 	 	
 	private void fireRootChanged() {
 		TreeModelEvent e = new TreeModelEvent(this, new Object[] {root});
  	for (int i = 0; i < treeModelListeners.size(); i++) {
  		((TreeModelListener)treeModelListeners.get(i)).treeStructureChanged(e);
  	}		
 	}
 	
 	private void fireChannelAdded(Object[] path, Object child) {
 		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {0}, new Object[] {child});
 		for (int i = 0; i < treeModelListeners.size(); i++) {
 			((TreeModelListener)treeModelListeners.get(i)).treeNodesInserted(e);
 		}
 	}
 	
 	private void fireChannelRemoved(Object[] path, Object child) {
 		TreeModelEvent e = new TreeModelEvent(this, path, new int[] {0}, new Object[] {child});
 		for (int i = 0; i < treeModelListeners.size(); i++) {
  			((TreeModelListener)treeModelListeners.get(i)).treeNodesRemoved(e);
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
    return getSortedChildren(n).get(index);
	}

	public int getChildCount(Object n) {
    return getSortedChildren(n).size();
  }

	public boolean isLeaf(Object n) {	
    boolean isLeaf = false;
    if (n != root) {
      node = (ChannelTree.Node)n;
      if (node.getType() == ChannelTree.CHANNEL) {
        isLeaf = true;
      }
    }
		return isLeaf;
	}

	public void valueForPathChanged(TreePath path, Object n) {}

	public int getIndexOfChild(Object n, Object child) {
    return getSortedChildren(n).indexOf(n);
	}
  
  private List getSortedChildren(Object n) {
    if (n == root) {
      return RBNBUtilities.getSortedChildren(ctree, showHiddenChannels);
    } else {
      node = (ChannelTree.Node)n;
      return RBNBUtilities.getSortedChildren(node, showHiddenChannels);
    }
  }

	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		if (e.isAddedPath()) {
			TreePath treePaths = e.getPath();
			Object o = treePaths.getLastPathComponent();
			fireChannelSelected(o);
		}		
	}
	
	public void dragGestureRecognized(DragGestureEvent e) {
		Point p = e.getDragOrigin();
		TreePath treePath = tree.getPathForLocation((int)p.getX(), (int)p.getY());
		if (treePath != null) {
			Object o = treePath.getLastPathComponent();
			if (o != root) {
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

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

  private void handleDoubleClick(MouseEvent e) {
    TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
    if (treePath != null) {
      Object o = treePath.getLastPathComponent();
      if (o != root) {
        ChannelTree.Node node = (ChannelTree.Node)o;
        if (node.getType() == ChannelTree.CHANNEL) {
          String channelName = node.getFullName();
          dataPanelManager.viewChannel(channelName);
        }
      }
    }    
  }
  
  private void handlePopup(MouseEvent e) {
    TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
    tree.setSelectionPath(treePath);
    if (treePath != null) {
      JPopupMenu popup = null;
      
      Object o = treePath.getLastPathComponent();
      if (o == root) {
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
  }
  
  private JPopupMenu getRootPopup() {
    JPopupMenu popup = new JPopupMenu();
    
    JMenuItem menuItem = new JMenuItem("Import data...", DataViewer.getIcon("icons/import.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        frame.showImportDialog();
      }
    });
    popup.add(menuItem);
    
    menuItem = new JMenuItem("Export data...", DataViewer.getIcon("icons/export.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        frame.showExportDialog(RBNBUtilities.getAllChannels(ctree, showHiddenChannels));
      }
    });

    popup.add(menuItem);
    
    
    return popup;
  }
  
  private JPopupMenu getSourcePopup(final ChannelTree.Node source) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem;
    
    final String sourceName = source.getFullName();

    List extensions = getExtensionsForSource(node);          
    Iterator i = extensions.iterator();
    while (i.hasNext()) {
      final Extension extension = (Extension)i.next();
      
      menuItem = new JMenuItem("View source with " + extension.getName());
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          List channels = RBNBUtilities.getSortedChildren(source, showHiddenChannels);
          dataPanelManager.viewChannels(channels, extension);
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
    
    menuItem = new JMenuItem("Import data to source...", DataViewer.getIcon("icons/import.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        frame.showImportDialog(sourceName);
      }
    });
    // LJM disabled for the NEESit 1.3 release
    // popup.add(menuItem);
    menuItem = new JMenuItem("Export data source...", DataViewer.getIcon("icons/export.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        frame.showExportDialog(RBNBUtilities.getChildChannels(source, showHiddenChannels));
      }
    });
    popup.add(menuItem);          
    
    return popup;
  }
  
  private JPopupMenu getChannelPopup(ChannelTree.Node channel) {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem;
    
    final String channelName = channel.getFullName();
    
    ArrayList extensions = dataPanelManager.getExtensions(channelName);
    final Extension defaultExtension = dataPanelManager.getDefaultExtension(channelName);
    if (defaultExtension != null) {
      menuItem = new JMenuItem("View with " + defaultExtension.getName());
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          dataPanelManager.viewChannel(channelName, defaultExtension);
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
                dataPanelManager.viewChannel(channelName, extension);
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
    
    menuItem = new JMenuItem("Export channel...", DataViewer.getIcon("icons/export.gif"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ArrayList channel = new ArrayList();
        channel.add(channelName);
        frame.showExportDialog(channel);
      }
    });
    popup.add(menuItem);
    
    return popup;    
  }
  
  private List getExtensionsForSource(ChannelTree.Node source) {
    List extensions = new ArrayList();
    List children = RBNBUtilities.getSortedChildren(source, showHiddenChannels);
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
  
	private class ChannelTreeCellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			if (value != root) {
        ChannelTree.Node node = (ChannelTree.Node)value;
        
        ChannelTree.NodeTypeEnum type = node.getType();
        String mime = RBNBUtilities.fixMime(node.getMime(), node.getFullName());
        
				setText(node.getName());
        if (type == ChannelTree.SERVER) {
          setIcon(DataViewer.getIcon("icons/server.gif"));
        } else if (type == ChannelTree.FOLDER ||
                   type == ChannelTree.SOURCE ||
                   type == ChannelTree.PLUGIN) {
          if (expanded) {
            setIcon(DataViewer.getIcon("icons/folder_open.gif"));
          } else {
            setIcon(DataViewer.getIcon("icons/folder.gif"));
          }
        } else if (type == ChannelTree.CHANNEL) {
          if (mime.equals("application/octet-stream")) {
            setIcon(DataViewer.getIcon("icons/data.gif"));
          } else if (mime.equals("image/jpeg") || mime.equals("video/jpeg")) {
            setIcon(DataViewer.getIcon("icons/jpeg.gif"));
          } else if (mime.equals("text/plain")) {
            setIcon(DataViewer.getIcon("icons/text.gif"));
          } else {
            setIcon(DataViewer.getIcon("icons/file.gif"));
          }
        }
 			} else if (value.equals(EMPTY_ROOT)) {
 				setIcon(null);
        setText(null);
			} else {
        setIcon(DataViewer.getIcon("icons/server.gif"));
      }

			return c;
		}
		
	}

	public void postState(int newState, int oldState) {
		if (newState == Player.STATE_DISCONNECTED || newState == Player.STATE_EXITING) {
			clearChannelList();
			fireNoChannelsSelected();
      metadataUpdateButton.setEnabled(false);
		} else {
      metadataUpdateButton.setEnabled(true);
    }
	}
}
