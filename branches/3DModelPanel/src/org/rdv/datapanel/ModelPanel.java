/*	ModelPanel.java
*	Subclass of AbstractDataPanel
*
*	Copyright (c) 2007 Andrew Lindberg
*
* 	Permission is hereby granted, free of charge, to any person obtaining a copy
* 	of this software and associated documentation files (the "Software"), to deal
* 	in the Software without restriction, including without limitation the rights
* 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* 	copies of the Software, and to permit persons to whom the Software is
* 	furnished to do so, subject to the following conditions:
*
* 	The above copyright notice and this permission notice shall be included in
* 	all copies or substantial portions of the Software.
*
* 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* 	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* 	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* 	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* 	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* 	SOFTWARE.
* 	Author: Andrew Lindberg
* 	Date: July 31, 2007
*/

package org.rdv.datapanel;

import org.rdv.data.Channel;
import org.rdv.ui.MessagePopup;
import com.rbnb.sapi.ChannelMap;

import java.io.*;
import java.lang.Math;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.vecmath.*;
import javax.media.j3d.*;
import javax.media.j3d.BoundingSphere;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.picking.*;


/**
 * A data panel to display a 3D model of a structure in real time.
 * @author Andrew Lindberg
 *
 */
public class ModelPanel extends AbstractDataPanel
{
	/** The parent ModelPanel **/
	ModelPanel mpanel = this;
	/** The representation of the structure and current displacements */
	StructureModel model;
	/**	Loader to read model definition from file. */
	SModelLoader loader;
	/**	Component that renders the 3D model.*/
	ModelViewer mview;
	/** Component that contains the 3D pane and the toolbars. */
	ViewPanel viewpanel;
	/** Editor to modify live models */
	ModelEditor editor;
	/** Mouse event handler for interacting with the 3D model. */
	MousePicker mpicker;

	/** Last posted time */
	double lastPostedTime;
	
	/** actionLinks */
	// matches channel names to a set of NodeAxisPairs 
	HashMap<String,HashSet<NodeAxisPair>> actionLinks;
	
	// constants for axes numbers in NodeAxisPairs
	static final int X_AXIS = 0;
	static final int Y_AXIS = 1;
	static final int Z_AXIS = 2;
	static final int X_ROT = 3;
	static final int Y_ROT = 4;
	static final int Z_ROT = 5;
	static final int STR = 6; // stress/strain "axis"
	// constants for bound edges
	static final int MIN = 0;
	static final int MAX = 1;
	
	
	/**
	 * Initializes the Model Panel. Sets up the 3D environment and controls.
	 */
	public ModelPanel()
	{
		super();
		actionLinks = new HashMap<String,HashSet<NodeAxisPair>>();
		model = new StructureModel();
		loader = new SModelLoader();
		editor = new ModelEditor();
		mview = new ModelViewer();
		viewpanel = new ViewPanel();
		
		setDataComponent((JPanel)(viewpanel));
	}

	/**
	 * Links the channel to the currently selected node and axis from channel linker.
	 * Defers to addChannel(String channelname, String node, int axis)
	 * @param	channelname		name of the channel to add
	 */
	public boolean addChannel(String channelname)
	{
		return addChannel(channelname,editor.selectednode,editor.selectedaxis);
	}
	
	/**
	 * Adds a channel to the panel's subscriber list. Links the channel with the
	 * sepcified node and axis.
	 * @param	channelname		name of the channel to add
	 * @param	node		    name of the node to link the channel to
	 * @param	axis		node axis to link the channel to
	 * @return	<code>true</code> if the channel was successfully linked,
	 * <code>false</code> otherwise
	 */
	public boolean addChannel(String channelname, String node, int axis)
	{
		// check that channel exists
		Channel channel = rbnbController.getChannel(channelname);
		if (model.nodes.isEmpty())
		{ // no model to link against, shouldn't happen
			return false;
		}
		else if (channel == null || node.length() == 0 || axis < 0)
		{ // some param is invalid, also shouldn't happen
			return false;
		}
		else if (channels.contains(channelname))
		{
			// if we are already subscribed, but we want a new node
			// connected to the channel:
			// just call channelAdded and bypass the other steps
			channelAdded(channelname,node,axis);
			return true;
		}
		else
		{
			// totally new channel: link and subscribe
			channels.add(channelname);
			updateTitle();
			channelAdded(channelname,node,axis);
			rbnbController.subscribe(channelname, this);
		}
		return true;
	}
	
	/**
	 * Overridden from AbstractDataPanel to do extra work.
	 * Performs the actual linking of channel to a node and axis.
	 */
	void channelAdded(String channelname,String node, int axis)
	{
		if( !actionLinks.containsKey(channelname) )
		{
			actionLinks.put(channelname,new HashSet<NodeAxisPair>());
		}
		// add an MotionNode to the set
		unlink(node,axis);
		link(node,axis,channelname);
  	editor.updatescreen();
	}
	
	/* link the index and axis to the given channel name*/
	protected void link(String node, int axis, String channelname)
	{
		actionLinks.get(channelname).add(new NodeAxisPair(node,axis));
	}
	/* unlink any channels from the given index/axis pair*/
	protected void unlink(String node, int axis)
	{
		String cname = getChannel(node,axis);
		if(cname != "--")
		{
		  Iterator<NodeAxisPair> napairs = actionLinks.get(cname).iterator();
		  while(napairs.hasNext())
		  {
		    NodeAxisPair next = napairs.next();
		    if(next.axis == axis && next.nodename.equals(node))
		    {
		      napairs.remove();
		    }
		  }
		}
	}
	
	/* 
	 * Check if the channel has any links, unsubscribe if none.
	 */
	protected void checkLinks(String channelname)
	{
	  if( !channelname.equals("--") && actionLinks.get(channelname).isEmpty() )
	  {
	    super.removeChannel(channelname);
	    // this will unsubscribe and then call channelRemoved (remove from actionLinks)
	  }
	}
	
	/**
	 * Overridden from AbstractDataPanel to do extra work.
	 * Unlinks a channel from all node and axes with which it may be associated.
	 */
	protected void channelRemoved(String channelname)
	{
		actionLinks.remove(channelname);
		editor.updatescreen();
	}
	
	/**
	 * Returns whether or not multiple channels are supported. Always <code>true</code> for ModelPanel
	 */
	public boolean supportsMultipleChannels()
	{
		return true;
	}
	
	public void postData(ChannelMap channelMap)
	{
		this.channelMap = channelMap;
	}
	
	// overridden, leave parent's javadoc intact and re-vamp if needed
	public void postTime(double time)
	{
		// PostData drops in new data, postTime is called
		// repeatedly on those data points, iterate through them
		HashMap<String,Point3d> disp_updates = new HashMap<String,Point3d>();
		HashMap<String,Point3d> rot_updates = new HashMap<String,Point3d>();
		HashMap<String,Double> str_updates = new HashMap<String,Double>();
		
		// i iterates over subscribed channels
		Iterator<String> i = channels.iterator();
		while (i.hasNext() && channelMap != null )
		{
			String channelName = (String)i.next();
			int channelIndex = channelMap.GetIndex(channelName);
			if (channelIndex != -1) // if this channel has new data
			{
				double [] times = channelMap.GetTimes(channelIndex);
				int timeIndex = 0;
				if (times.length > 0 && time > mpanel.time )
				{
					for ( timeIndex = 0; times[timeIndex] < time && timeIndex < times.length-1 ; timeIndex++)
					{}
				}
				int typeID = channelMap.GetType(channelIndex);
				double data;
				switch (typeID)
				{
					case ChannelMap.TYPE_FLOAT64:
						data = channelMap.GetDataAsFloat64(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_FLOAT32:
						data = channelMap.GetDataAsFloat32(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_INT64:
						data = channelMap.GetDataAsInt64(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_INT32:
						data = channelMap.GetDataAsInt32(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_INT16:
						data = channelMap.GetDataAsInt16(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_INT8:
						data = channelMap.GetDataAsInt8(channelIndex)[timeIndex];
						break;
					case ChannelMap.TYPE_STRING:
					case ChannelMap.TYPE_UNKNOWN:
					case ChannelMap.TYPE_BYTEARRAY:
					default:
						return;
				} // switch
			
				// anodes iterates over all the MotionNodes linked to this channel
				Iterator<NodeAxisPair> anodes = actionLinks.get(channelName).iterator();
				while(anodes.hasNext())
				{
					Point3d disp = new Point3d();
					Point3d rot = new Point3d();
					NodeAxisPair nap = anodes.next();
					switch ( nap.axis ) // switch on axis
					{
						case X_AXIS:
							disp.set(data,0.0,0.0);
							break;
						case Y_AXIS:
							disp.set(0.0,data,0.0);
							break;
						case Z_AXIS:
							disp.set(0.0,0.0,data);
							break;
						case X_ROT:
							rot.set(data,0.0,0.0);
							break;
						case Y_ROT:
							rot.set(0.0,data,0.0);
							break;
						case Z_ROT:
							rot.set(0.0,0.0,data);
							break;
						case STR:
							str_updates.put(nap.nodename,data);
							break;
						default:
							break;
					} // end switch
					if(disp_updates.containsKey(nap.nodename))
					{
						disp_updates.get(nap.nodename).add(disp);
					}
					else
					{
						disp_updates.put(nap.nodename, disp);
					}
					if(rot_updates.containsKey(nap.nodename))
					{
						rot_updates.get(nap.nodename).add(rot);
					}
					else
					{
						rot_updates.put(nap.nodename, rot);
					}
				}// end while anodes
			} // end if channelIndex !=1
		} // while channel.hasNext()  (subscribed channels)
		
		// now walk updates and push to model and then call mview.update()
		Iterator<Map.Entry<String,Point3d>> iter = disp_updates.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<String,Point3d> entry = iter.next();
			model.moveNode(entry.getKey(), entry.getValue(), true);
		}
		iter = rot_updates.entrySet().iterator();
		while(iter.hasNext())
		{
			Map.Entry<String,Point3d> entry = iter.next();
			model.rotateNode(entry.getKey(), entry.getValue());
		}
		Iterator<Map.Entry<String,Double>> iter2 = str_updates.entrySet().iterator();
		while(iter2.hasNext())
		{
			Map.Entry<String,Double> entry = iter2.next();
			model.colorNode(entry.getKey(),entry.getValue());
		}
		// update screen
		mview.update();
		// update time
		this.time = time;
	}
	
	/**
	 * Destroys the current model definition and unlinks all channels.
	 */
	public void destroyModel()
	{
		mview.destroyModelBranch();
		editor.deselectAll(); // deselect all
		actionLinks.clear();
		model.nodes.clear();
		model.members.clear();
		removeAllChannels();
	}

	/**
	 * Loads the channel links defined for the model from the definition file.
	 */
	public void loadchannels()
	{
		HashMap<String,ArrayList<String>> channelmaps = (HashMap<String,ArrayList<String>>) loader.parseChannels();
		Iterator<String> iter = channelmaps.keySet().iterator();
		while(iter.hasNext())
		{
			String key = iter.next();
			ArrayList<String> chans = channelmaps.get(key);
			for(int j=0;j<chans.size();j++)
			{
				if(chans.get(j) != "--" && chans.get(j) != "")
				{
					addChannel((String)chans.get(j),key,j);
				}
			}
		}
	}
	
	// find the channel linked to the specified node axis pair
	private String getChannel(String node, int axis)
	{
		boolean found = false;
		Set<String> s = actionLinks.keySet();
		Iterator<String> i = s.iterator();
		String channel = "--";
		while(!found && i.hasNext())
		{
			String channame = i.next();
			Iterator <NodeAxisPair> j = actionLinks.get(channame).iterator();
			while(j.hasNext() && !found)
			{
				NodeAxisPair n = j.next(); 
				if( n.nodename.equals(node) && n.axis == axis )
				{
					found = true;
					channel = channame;
				}
			}
		}
		return channel;
	}
	
	// save model without channels
	public void saveModel()
	{
		saveModel(false);
	}
	
	/**
	 * Prompts for a filename and saves the current model with or without currently linked channels
	 * @param savechannels if <code>true</code> save currently linked channels, otherwise save model only
	 */
	public void saveModel(boolean savechannels)
	{
		if( model.nodeCount() > 0 )
		{
		  // get a file name
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showSaveDialog(dataComponent);
			if(returnVal != JFileChooser.APPROVE_OPTION) 
			{	return;	}
			File tempfile = chooser.getSelectedFile();
			if (tempfile == null ) 
		    {	return;	}
			else
			{ // write the file
					try{
				tempfile.createNewFile();
				BufferedWriter output = new BufferedWriter(new FileWriter(tempfile));
				output.write("# Model: "+tempfile);
				output.newLine();
				output.write("# Saved: "+new Date().toString());
				output.newLine();
				output.write("# Begin Node section");
				output.newLine();
				Iterator<StructureModel.SNode> iter = model.nodes.values().iterator();
				StructureModel.SNode next;
				while(iter.hasNext())
				{
					next = iter.next();
					if( !(next instanceof StructureModel.ScaleNode) )
					{
						output.write(next.name);
						output.write(" ");
						output.write(next.position.toString());
						output.write(" ");
						if( savechannels )
						{
							String dispchans = String.format("[%1$s,%2$s,%3$s]", getChannel(next.name,X_AXIS), getChannel(next.name,Y_AXIS), getChannel(next.name,Z_AXIS));
							if( !(dispchans.equals("[--,--,--]")) )
							{
								output.write(dispchans);
								output.write(" ");
							}
							String rotchans = String.format("{%1$s,%2$s,%3$s}", getChannel(next.name,X_ROT), getChannel(next.name,Y_ROT), getChannel(next.name,Z_ROT));
							if( !(rotchans.equals("{--,--,--}")) )
							{
								output.write(rotchans);
								output.write(" ");
							}
						}
						output.write( colortohex( next.color ) );
						output.newLine();
					}
				}
				output.write("# End Node section");
				output.newLine();
				output.write("===");
				output.newLine();
				output.write("# Begin Member section");
				output.newLine();
				Iterator<StructureModel.SMember> memberiter = model.members.values().iterator();
				
				while( memberiter.hasNext() )
				{
					StructureModel.SMember m = memberiter.next();
					output.write(m.nodes[0]);
					output.write(" ");
					output.write(m.nodes[1]);
					output.write(" ");
					if( m.curveType == StructureModel.SMember.CUBIC )
					{
						output.write("[cubic]");
						output.write(" ");
					}
					output.write( colortohex( m.color ) );
					output.newLine();
				}
				output.write("# End Member section");
				output.newLine();
				output.write("===");
				output.newLine();
				output.write("# Begin ScaleNode section");
				output.newLine();
				iter = model.nodes.values().iterator(); // reset iterator
				while(iter.hasNext())
				{
					next = iter.next();
					if( next instanceof StructureModel.ScaleNode )
					{
						StructureModel.ScaleNode sn = (StructureModel.ScaleNode)next;
						output.write(sn.name);
						output.write(" ");
						output.write(sn.position.toString());
						output.write(" ");
						String values = String.format("<%1$s,%2$s,%3$s>", sn.lower, sn.middle, sn.upper);
						output.write(values);
						output.write(" ");
						String colors = String.format("<%1$s,%2$s,%3$s,%4$s>", colortohex(sn.lowercolor), colortohex(sn.middlecolor), colortohex(sn.uppercolor), colortohex(sn.failcolor) );
						output.write(colors);
						output.write(" ");
						if( savechannels )
						{
							String strchans = String.format("{%1$s}", getChannel(sn.name,STR));
							if( !(strchans.equals("{--}")) )
							{
								output.write(strchans);
								output.write(" ");
							}
							String dispchans = String.format("[%1$s,%2$s,%3$s]", getChannel(sn.name,X_AXIS), getChannel(sn.name,Y_AXIS), getChannel(sn.name,Z_AXIS));
							if( !(dispchans.equals("[--,--,--]")) )
							{
								output.write(dispchans);
								output.write(" ");
							}
						}
						output.newLine();
					}
				}
				output.write("# End ScaleNode section");
				output.newLine();
				output.write("===");
				output.newLine();
				if( model.center_defined )
				{
					output.write("Center: "+model.centerpoint );
					output.newLine();
				}
				output.close();
					}
					catch (IOException e)
					{
						popupError("IO Error saving to file: "+tempfile );
					}
			}
		}
		else
		{ // if there is no live model
			popupError("No model to save.");
		}
	}
	
	// return Color3f as #xxxxxx hex string
	private String colortohex(Color3f c)
	{
		String color = String.format("#%1$02x%2$02x%3$02x",c.get().getRed(),c.get().getGreen(),c.get().getBlue());
		return color;
	}
	
	/**
	 * Displays an error message in a popup window. Also echo error message to stdout.
	 * @param msg	The error message to display.
	 */
	public void popupError(String msg)
	{
		MessagePopup popup = MessagePopup.getInstance();
		popup.showError(msg);
		System.out.println(msg);
	}
	
	// ---------------------------- //
	// --- Begin Nested Classes --- //
	// ---------------------------- //

	// StructureModel represents the model: nodes and members
	/**
	 * Encapsulates the definition of a model. Nodes and their positions and displacements,
	 * members and their endpoints and deformation types.
	 */
	class StructureModel
	{
		/** The list of nodes in the <code>StructureModel</code>. */
		private LinkedHashMap<String,SNode> nodes;
		/** The list of members in the <code>StructureModel</code>. */
		private LinkedHashMap<Integer,SMember> members;
		
		/** The center point of the model */
		private Point3d centerpoint = new Point3d(0.0,0.0,0.0);
		/** Did the model define the center or are we calculating it */
		private boolean center_defined = false;
		
		/**
		*	Constructs an empty <code>StructureModel</code>.
		*/
		public StructureModel()
		{
			nodes = new LinkedHashMap<String,SNode>();
			members = new LinkedHashMap<Integer,SMember>();
		}

		/**
		*	Adds a node to the <code>StructureModel</code>. Every node must have a unique name, and
		*	are required to be at unique locations. Throws NodeExistsException
		*	@param s        the name of the node being added
		* @param p    		the location of the node being added
		*	@param c		    the color of the node being added
		*	@see	#addNode(String,Point3d)
		*/
		public void addNode(String s, Point3d p, Color3f c) throws ModelException
		{
			if( this.containsNode(s) )
			{
				throw new ModelException("Node with name: "+s+" already exists in model.");
			}
			else if( this.containsNode(p) )
			{
				throw new ModelException("Node exists at point: "+p);
			}
			else if( !s.matches("[\\w_]+") )
			{
				throw new ModelException("Invalid characters in node name: "+s);
			}
			else
			{
				nodes.put(s,new SNode(s,p,c));
			}
		}

		/**
		*	Adds a node to the <code>StructureModel</code> with a default color (gray).
		*	@param name        the name of the node being added
		* @param point	     	the location of the node being added
		*	@see 	#addNode(String,Point3d,Color3f)
		*/
		public void addNode(String name, Point3d point) throws ModelException
		{
			addNode(name,point,null);
		}
		
		public void addScaleNode(String s, Point3d p, float lower, float middle, float upper, Color3f lowercolor, Color3f middlecolor, Color3f uppercolor, Color3f failcolor)
			throws ModelException
		{
			if ( this.containsNode(s) )
			{
				throw new ModelException("Node with name: "+s+" already exists in model.");
			}
			else if( this.containsNode(p) )
			{
				throw new ModelException("Node exists at point: "+p);
			}
			else if( !s.matches("[\\w_]+") )
			{
				throw new ModelException("Invalid characters in node name: "+s);
			}
			else if( lower > middle || middle > upper )
			{
				throw new ModelException("Threshold values must be in increasing order. "+lower+" "+middle+" "+upper);
			}
			else
			{
				nodes.put(s,new ScaleNode(s,p,lower,middle,upper,lowercolor,middlecolor,uppercolor,failcolor));
			}
		}
		

		/**
		*	Defines a new linear member in the <code>StructureModel</code> by its two end nodes with a default color.
		*	@param NodeOne		the name of the node at one end of the member
		*	@param NodeTwo		the name of the node at the other end of the member
		*	@see	#defineMember(String, String, int, Color3f)
		*/
		public void defineMember(String NodeOne, String NodeTwo) throws ModelException
		{
			defineMember(NodeOne,NodeTwo,StructureModel.SMember.LINEAR);
		}

		/**
		*	Defines a new linear member in the <code>StructureModel</code> by its two end nodes.
		*	@param NodeOne		the String of the node at one end of the member
		*	@param NodeTwo		the String of the node at the other end of the member
		*	@param color		  the color of the member
		*/
		public void defineMember(String NodeOne, String NodeTwo, Color3f color) throws ModelException
		{
			defineMember(NodeOne,NodeTwo, StructureModel.SMember.LINEAR, color);
		}

		/**
		*	Defines a new member in the <code>StructureModel</code> by its two end nodes
		*	 and deformation type with a default color.
		*	@param NodeOne		the name of the node at one end of the member
		*	@param NodeTwo		the name of the node at the other end of the member
		*	@param type			<code>int</code> that defines the type of deformation 
		*	this member undergoes
		*/
		public void defineMember(String NodeOne, String NodeTwo, int type) throws ModelException
		{
			defineMember(NodeOne, NodeTwo, type, null);
		}

		/**
		*	Defines a new member in the <code>StructureModel</code> by its two end nodes
		*	 and deformation type with a specific color.
		*	@param NodeOne		the node of the node at one end of the member
		*	@param NodeTwo		the ndoe of the node at the other end of the member
		*	@param type			<code>int</code> that defines the type of deformation this
		*	 member undergoes
		*	@param color		the color of the member
		*/
		public void defineMember(String NodeOne, String NodeTwo, int type, Color3f color) throws ModelException
		{
			if( NodeOne.compareTo(NodeTwo) > 0 )
			{
				String tmp = NodeOne;
				NodeOne = NodeTwo;
				NodeTwo = tmp;
			}
			if (NodeOne.compareTo(NodeTwo) == 0)
			{
				throw new ModelException("Must specify two distinct nodes to connect.");
			}
			else if( nodes.get(NodeOne) instanceof StructureModel.ScaleNode || nodes.get(NodeTwo) instanceof StructureModel.ScaleNode )
			{
				throw new ModelException("May not define members using stress/strain nodes.");
			}
			else if( this.containsMember(NodeOne,NodeTwo))
			{
				throw new ModelException("Member between nodes: "+NodeOne+" and "+ NodeTwo+" already exists in model.");
			}
			else
			{
				members.put(NodeOne.hashCode()*NodeTwo.hashCode(),new StructureModel.SMember(NodeOne, NodeTwo, type, color));
			}
		}
		
		/**
		 * Remove the node <code>name</code> from the model.
		 * Also remove any members that are connected to <code>name</code>.
		 * @param name		the name of the node to remove
		 */
		public void removeNode(String name)
		{
			Iterator<SMember>  m = members.values().iterator();
			while( m.hasNext() )
			{
				SMember nextmember = m.next();
				if( nextmember.nodes[0].equals(name) || nextmember.nodes[1].equals(name) )
				{
					m.remove();
				}
			}
			nodes.remove(name);
		}
		
		/**
		 * Remove the specified member
		 * @param member  the member index to remove (computed as nodeOne.hashCode()*nodeTwo.hashCode() )
		 */
		public void removeMember(int member)
		{
			members.remove( member );
		}
		
		/**
		 * Find the maximum or minimum values in the set of nodes along an axis.
		 * @param extreme	either ModelPanel.MAX or .MIN
		 * @param axis		one of ModelPanel.X_AXIS, .Y_AXIS or .Z_AXIS
		 * @return			a double of the extreme value for the specified axis
		 */
		public double getAxisExtreme(int extreme, int axis)
		{
			TreeSet<Double> sort = new TreeSet<Double>();
			Iterator<String> nodeiter = model.nodes.keySet().iterator();
			while(nodeiter.hasNext())
			{
				double values[] = new double[3];
				nodes.get(nodeiter.next()).getBasePosition().get(values);
				sort.add(values[axis]);
			}
			return (sort.isEmpty() ? 0 : (extreme == MAX ? sort.last() : sort.first() ) );
			
		}
		
		/**
		 * Returns the maximum height (z-axis parallel distance) of the model.
		 * @return	the maximum height (z-axis parallel distance) in the
		 * model as a <code>double</code>
		 */
		public double getHeight()
		{ 
			return getAxisExtreme(MAX,Z_AXIS)-getAxisExtreme(MIN,Z_AXIS);
		}
		
		/**
		 * Returns the maximum depth (y-axis parallel distance) of the model.
		 * @return	the maximum depth (y-axis parallel distance) in the
		 * model as a <code>double</code>
		 */
		public double getDepth()
		{ 
			return getAxisExtreme(MAX,Y_AXIS)-getAxisExtreme(MIN,Y_AXIS);
		}
		
		/**
		 * Returns the length of the longest diagonal in the model.
		 * @return	the length of the longest diagonal in the model
		 * as a <code>double</code>
		*/
		public double getMaximumSize()
		{ 
			double maxDistance = 0.0;
			Object[] nodes =  model.nodes.values().toArray();
			for( int i = 0; i < nodes.length; i++ )
			{
				StructureModel.SNode node = (StructureModel.SNode) nodes[i];
				for( int j = i; j < nodes.length; j++)
				{
					maxDistance = Math.max(maxDistance, node.position.distance(((StructureModel.SNode)nodes[j]).position));
				}
			}
			return maxDistance;
		}
		
		/**
		 * Returns the center point of the model. This is either set in the model
		 * file or otherwise computed from the model's dimensions.
		 * @return	the center point of the model as a <code>Point3d</code>
		*/
		public Point3d getCenter()
		{
			Point3d center = new Point3d();
			if( center_defined )
			{
				center.set(centerpoint);
			}
			else
			{
				center.setX(getAxisExtreme(MIN,X_AXIS) + (getAxisExtreme(MAX,X_AXIS)-getAxisExtreme(MIN,X_AXIS))/2 );
				center.setY(getAxisExtreme(MIN,Y_AXIS) + (getAxisExtreme(MAX,Y_AXIS)-getAxisExtreme(MIN,Y_AXIS))/2 );
				center.setZ(getAxisExtreme(MIN,Z_AXIS) + (getAxisExtreme(MAX,Z_AXIS)-getAxisExtreme(MIN,Z_AXIS))/2 );
			}
			return center;
		}

		/**
     * Returns <code>true</code> if the model contains a node by this name.
     * @param name the name to check for existence
     * @return  <code>true</code> if the model contains a node by this name
     */
		public boolean containsNode(String name)
		{
			return nodes.containsKey(name);	
		}
		/**
		 * Returns <code>true</code> if the model contains a node at this location.
		 * @param p the Point3d to check for the presence of a node
		 * @return  <code>true</code> if the model contains a node at this point
		 */
		public boolean containsNode(Point3d p)
		{
			boolean found = false;
			Iterator<String> names = nodes.keySet().iterator();
			while( !found && names.hasNext() )
			{
				if( nodes.get(names.next()).position.equals(p) )
				{	found = true;	}
			}
			return found;
		}
		
		/**
		 * Return true if the model already contains a member connecting these nodes
		 * @param one		the name of the first node in the member
		 * @param two		the name of the second node in the member
		 */
		public boolean containsMember(String one, String two)
		{
			return members.containsKey(one.hashCode()*two.hashCode());
		}
		
		/**
		* Reports how many nodes are in the <code>StructureModel</code>
		*	@return the number of nodes in the <code>StructureModel</code> as an <code>int</code>
		*/
		public int nodeCount()
		{
			return nodes.size();
		}

		/**
		*	Gets the number of members defined in the <code>StructureModel</code>.
		*	@return	the number of members in the <code>StructureModel</code> as
		*	an <code>int</code>
		*/
		public int memberCount()
		{
			return members.size();
		}

		/**
		* Moves the referenced node. A relative movement means that the supplied Point3d
		* should be treated as a displacement from the node's base position (not the
		* node's currently displaced position). An absolute movement changes the node's 
		* base position to the <code>Point3d</code> supplied and zeroes its displacement.
		*	@param name		the name of the node to move
		* @param p 			the Point3d to move the node to or displace it by
		*	@param relative 	<code>true</code> if this is a relative movement, <code>false</code> if this an absolute movement
		*/
		public void moveNode(String name, Point3d p, boolean relative)
		{
			SNode node = nodes.get(name);
			if (relative)
			{
				node.displacement.set(p);
			}
			else
			{
				node.position.set(p);
				node.displacement.set(0.0,0.0,0.0);
			}
		}

		/**
		 * Rotates the referenced node. Rotations should be kept between -180 and 180 on each axis.
		 * @param name		the name of the node to rotate
		 * @param p			the rotation to apply, around each axis
		 */
		public void rotateNode(String name, Point3d p)
		{
			SNode node = nodes.get(name);
			node.rotation.set(p);
		}

		/**
		 * Colors the Stress/Strain node based on the value of <code>data</code>.
		 * @param name	the name of the node to color
		 * @param data	the value to use in color interpolation computation
		 */
		public void colorNode(String name, double data)
		{
			ScaleNode node = (ScaleNode)nodes.get(name);
			node.interpolatecolor((float)data);
		}

		//----- Begin Nested Classes within StructureModel ---//
		class SNode
		{
			String name;
			Color3f color;
			Point3d position,displacement = new Point3d();
			Point3d rotation = new Point3d();
			public SNode(String s, Point3d p, Color3f c)
			{
				position = p;
				if (c == null)
				{	color = new Color3f(0.0f, 1.0f, 1.0f);	}
				else
				{	color = c;	}
				name = s;
			}
			public Point3d getPosition()
			{
				Point3d p = new Point3d(position);
				p.add(displacement);
				return p;
			}
			public Point3d getRotation()
			{
				return new Point3d(rotation);
			}
			public Point3d getBasePosition()
			{
				return new Point3d(position);
			}
			public void setColor(Color3f c)
			{
				color = c;
			}
		}

		class SMember
		{
			// package accessible variables
			String nodes[] = {"",""};
			Point3d ctrlptone, ctrlpttwo; 
			int curveType;
			Color3f color;

			public static final int LINEAR = 0;
			public static final int CUBIC = 1;

			SMember(String one, String two, int type, Color3f c)
			{
				if( one.compareTo(two) > 0) // ensure lexographic order for later searching...
				{	String tmp = one;
					one = two;
					two = tmp;
				}
				nodes[0] = one;
				nodes[1] = two;
				curveType = type;
				if( c != null )
				{ color = c;	}
				else
				{ c = new Color3f(Color.LIGHT_GRAY);	}
			}
			
		}// end of SMember class
		class ScaleNode extends SNode
		{
			float upper, lower, middle;
			Color3f uppercolor, lowercolor, middlecolor, failcolor;
			
			ScaleNode(String s, Point3d position, float lower, float middle, float upper, Color3f lowercolor, Color3f middlecolor, Color3f uppercolor, Color3f failcolor)
			{
				super(s,position,middlecolor);
				this.upper = upper;
				this.lower = lower;
				this.middle = middle;
				this.lowercolor = lowercolor;
				this.uppercolor = uppercolor;
				this.middlecolor = middlecolor;
				this.failcolor = failcolor;
				
			}

			void interpolatecolor(float data)
			{				
				if(data>upper || data<lower) //failcolor if out of range
				{	this.color = new Color3f(failcolor);	}
				else if (data >= middle)
				{
					float alpha = (data-middle)/(upper-middle);
				//	System.out.println("Between: "+uppercolor+" and "+middlecolor+"at ratio: "+alpha );
					this.color = new Color3f( ((1-alpha)*middlecolor.x + alpha*uppercolor.x) , ((1-alpha)*middlecolor.y + alpha*uppercolor.y) , ((1-alpha)*middlecolor.z + alpha*uppercolor.z) ); 
				}
				else if (data < middle)
				{
					float alpha = 1-(data-lower)/(middle-lower);
				//	System.out.println("Between: "+middlecolor+" and "+lowercolor+"at ratio: "+alpha );
					this.color = new Color3f( ((1-alpha)*middlecolor.x + alpha*lowercolor.x) , ((1-alpha)*middlecolor.y + alpha*lowercolor.y) , ((1-alpha)*middlecolor.z + alpha*lowercolor.z) );
				}	
			}
		}
		
		class ModelException extends Exception
		{
			String errormsg;
			ModelException(String msg)
			{
				errormsg = msg;
			}
		}
		//---- End Nested Classes within StructureModel---//
	}// end of StructureModel

	
	/**
	 * This class is responsible for loading a model definition from file
	 * and constructing a {@link StructureModel} based on it.
	 * It is also responsible for reading defined channel linkings from file.
	 */
	class SModelLoader extends JPanel
	{
		/* The file descriptor for the model definition */
		private File fileds;
		private BufferedReader reader;
		
		/**
		*  Constructs an SModelLoader linked to the file with name <code>f</code>.
		*  @param	f	the filename of the model file to which this loader is linked
		*/
		public SModelLoader( String f )
		{
			if (f != null)
			{
				fileds = new File(f);
			}
		}

		/**
		*  Constructs an default SModelLoader.
		*/
		public SModelLoader()
		{
			this(null);
		}

		/**
		*  Links this SModelLoader to the File <code>f</code>.
		*  @param f		the File object to link
		*/
		public void setFile(File f)
		{
			fileds = f;
		}
		
		/**
		*  Links this SModelLoader to the filename <code>f</code>.
		*  @param f		the filename to link
		*/
		public void setFile(String f)
		{
			fileds = new File(f);
		}
		
		/**
		*  Parses the file associated with this SModelLoader and adds the described
		*  nodes and members into the StructureModel associated with this
		*  SModelLoader.
		*/
		public void processModelFile()
		{
			model = new StructureModel(); // reset model
			openFile();
			parseNodes();
			closeFile();
			// open/close is inefficient but cleaner for the parser.
			
			// only bother to look for members if we have nodes.
			if(!model.nodes.isEmpty())
			{
				openFile();
				parseMembers();
				closeFile();
			}
		
			// also only look for a center if the model has size
			if(!model.nodes.isEmpty())
			{
				openFile();
				parseCenter();
				closeFile();
			}
			// look for stress/strain nodes (we can always look for these, could make a model with only these...)
			if(true)
			{
				openFile();
				parseScaleNodes();
				closeFile();
			}
		}// end buildModel()

	 // open the associated file for parsing
		private void openFile()
		{		try{
		  if(fileds != null)
		  {
		    reader = new BufferedReader(new FileReader(fileds));  
		  }
		  else
		  {
		    System.out.println("No model file to load channels from");
		  }
				}
				catch(FileNotFoundException e)
				{
					System.out.println("Could not find file: "+fileds.toString());
				}
		}
		
		// Closes the associated file.
		private void closeFile()
		{
				try{
			if( reader != null )
			{   reader.close();  }
				}
				catch(IOException e)
				{
					System.out.println("Failure closing file: "+fileds.toString());
				}
		}

		// Get the nodes from the model definition file
		private void parseNodes()
		{
			String name;
			Point3d pos;
			Color3f color = new Color3f(Color.LIGHT_GRAY); // default color
			// reworking of the parsing method again.
			// check for minimum validity and handle internals later
			boolean done = false;
				try
				{
			String line;
			while(!done && reader.ready())
			{
				line = reader.readLine();
				while( line.length() == 0 || line.trim().startsWith("#"))
				{ // skip blanks and comments
					line = reader.readLine();
				}
				//minimum requirements for node: name and (  )
				Pattern nodepattern = Pattern.compile("([\\w_]+)\\s*\\(([^\\)]+)\\).*?");
				Matcher m1 = nodepattern.matcher(line);
				if( m1.matches() )
				{ // possible valid node line 
					name = m1.group(1);
					String point = m1.group(2);
					pos = buildPoint(point);
					// color is the last set of word chars or alphanumeric preceded by # on the line, preceded by anything
					Pattern cpattern = Pattern.compile(".*?([\\w#\\d]+)$");
					Matcher m2 = cpattern.matcher(line);
					if( m2.matches() )
					{
						color = parseColor(m2.group(1));
					}
					// create the node
					model.addNode(name, pos, color);
				}
				else if( line.contains("===") )
				{
					done = true;
				}
				else
				{
					mpanel.popupError("Invalid node format:\n"+line);
					mpanel.destroyModel();
					done = true;
				}
			}
				}
				catch(PointFormatException e)
				{
					mpanel.popupError("Invalid point format:\n"+e.errorline);
					mpanel.destroyModel();
				}
				catch(StructureModel.ModelException e)
				{
					mpanel.popupError(e.errormsg);
					mpanel.destroyModel();
				}
				catch (IOException e)
				{
					System.out.println("Error parsing file: "+fileds.toString());
				}
		}
		
		// Get members from the associated file.
		private void parseMembers()
		{
			Pattern memberpattern = Pattern.compile("([\\w]+)\\s+([\\w]+)((?:[^\\[]*)([^\\]]+)(?:\\]))?\\s*(#?[\\w]+)?\\s*");
			boolean done = false, valid = true;
				try{
			if(reader.ready())
			{
				valid = advanceSections(1); // get to member section
				//System.out.println("advanced?: "+valid);
				while( !done && reader.ready() && valid ) // otherwise start parsing members
				{
					String line = reader.readLine();
					//System.out.println("Member line: "+line);
					while((line.trim().length() == 0 || line.trim().charAt(0) == '#') && reader.ready())
					{ line = reader.readLine();	} // skip comments and blank lines
					//done if eof or we find another === break
					done = ( !reader.ready() || line.contains("===") );
					if(!line.contains("===")) // if this is still a valid line, process it
					{
						Matcher m = memberpattern.matcher(line);
						if( m.matches() ) // valid member line
						{
							if (model.containsNode(m.group(1)) && model.containsNode(m.group(2)) ) 
							{
								model.defineMember(	m.group(1),
													m.group(2),
													parseMemberType(m.group(4)),
													parseColor(m.group(5)) );
							}
							else
							{
								mpanel.popupError("Invalid node in line:\n"+line);
								mpanel.destroyModel();
								valid = false;
								done = true;
							}
						}
						else
						{
							mpanel.popupError("Invalid member line:\n"+line);
							mpanel.destroyModel();
							valid = false;
							done = true;
						}
					}
				}
			}
				}
				catch(StructureModel.ModelException e)
				{
					mpanel.popupError(e.errormsg);
					mpanel.destroyModel();
				}
				catch(IOException e)
				{
					System.out.println("IO Error while parsing file.");
				}
		}
		
		// Get the defined center from the associated file, if it exists.
		private void parseCenter()
		{
			// default to calculated centerpoint
			boolean done = false;
				try{
			String line = reader.readLine();
			boolean valid = advanceSections(3); // get to center section
			
			if(!done && valid) // got three sections in, and file is still valid (no EOF)
			{
				// we are now at the start of the third === section
				Pattern centerpattern = Pattern.compile("(?:[Cc][Ee][Nn][Tt][Ee][Rr])(?:[^\\(]*)([^\\)]+)(?:[\\s\\)]*)");
				while(reader.ready() && !done)
				{
					line = reader.readLine();
					while(( line.length() == 0 || line.trim().charAt(0) == '#') && reader.ready())
					{ line = reader.readLine(); } // skip '#' denoted comments and empty lines
					Matcher cm = centerpattern.matcher(line);
					if( cm.matches() )
					{
						done = true;
						// update if we find a defined centerpoint
							try{
						model.centerpoint = new Point3d(buildPoint(cm.group(1).trim().substring(1)));
						model.center_defined = true;
							}
							catch(PointFormatException e)
							{
								done = true;
								mpanel.popupError("Invalid point format in line:\n"+line);
								mpanel.destroyModel();
							}
					}
					else if (!reader.ready())
					{
						done = true;
					}
				}
			}
				}
				catch(IOException e)
				{ System.out.println("Error reading from file.");}
		}
		
		// Parses the model definition file for stress/strain nodes and adds them to the StructureModel
		private void parseScaleNodes()
		{
				try{
			String line = reader.readLine();
			boolean valid = advanceSections(2); // get to scale node section
			boolean done = false;
			// we are now at the start of the second === section
			
			// pattern is: name (point) <values> <colors>? 
			while( !done && valid && reader.ready() )
			{
				Pattern snpattern = Pattern.compile("([\\w]+)(?:[^\\(]*)\\(([^\\)]*)(?:[^<]*<)([^>]*)>(.*?)");
				while(reader.ready() && !done)
				{
					line = reader.readLine();
					while(( line.length() == 0 || line.trim().charAt(0) == '#') && reader.ready())
					{ line = reader.readLine();	} // skip '#' denoted comments and empty lines
					Matcher sn = snpattern.matcher(line);
					if( sn.matches() )
					{
							try{
						// defaults
						Color3f midcolor = parseColor("white");
						Color3f uppercolor = parseColor("blue");
						Color3f lowercolor = parseColor("cyan");
						Color3f failcolor = parseColor("red");
						
						Pattern colorpattern = Pattern.compile("(?:[^<]*<)([^,]+),([^,]+),([^,]+),([^>]+)>.*?");
						Matcher m = colorpattern.matcher( sn.group(4) );
						if (m.matches() )
						{
							if( m.group(1) != null )
							{ lowercolor	= parseColor(m.group(1).trim() );	}
							if( m.group(2) != null )
							{ midcolor		= parseColor(m.group(2).trim() );	}
							if( m.group(3) != null )
							{ uppercolor	= parseColor(m.group(3).trim() );	}
							if( m.group(1) != null )
							{ failcolor		= parseColor(m.group(4).trim() );	}
						}
						else if( (sn.group(4).contains("<") || sn.group(4).contains(">")) && !m.matches() )
						{
							mpanel.popupError("Invalid colors format for node: "+sn.group(1)+". Using defaults." );
						}
						Point3d valuepoint = buildPoint(sn.group(3).trim());
						if( (valuepoint.x >= valuepoint.y) || (valuepoint.y >= valuepoint.z) )
						{
							done = true;
							mpanel.popupError("Threshold values must be in increasing order:\n"+line);
							mpanel.destroyModel();
						}
						else
						{
							String name = sn.group(1);
							model.addScaleNode(name, buildPoint(sn.group(2).trim()), (float)valuepoint.x, (float)valuepoint.y, (float)valuepoint.z, lowercolor, midcolor, uppercolor, failcolor);
							
						}
							}
							catch(StructureModel.ModelException e)
							{
								mpanel.popupError(e.errormsg);
								mpanel.destroyModel();
							}
							catch(PointFormatException e)
							{
								done = true;
								mpanel.popupError("Invalid point format in line:\n"+line);
								mpanel.destroyModel();
							}
					}
					else if ( line.contains("===") || line.length() == 0 )
					{ // end of section
						done = true;
					}
					else
					{
						done = true;
						mpanel.popupError("Invalid ScaleNode format in line:\n"+line);
						mpanel.destroyModel();
					}
				}
			}	
				}
				catch(IOException e)
				{ System.out.println("Error reading from file.");}
		}
		
		/**
		 * Parses the model definition file for channel links and returns them.
		 * @return	<code>HashMap</code> of channel links whose keys are node indices
		 * (<code>Integer</code>s) and values are <code>ArrayList</code>s of
		 * channelnames (<code>String</code>s).
		 */
		HashMap<String,ArrayList<String>> parseChannels()
		{
		  boolean valid = true, nodesection = true;
		  
		  // new plan, parse every line for either node or scalenode compliance, feed to appropriate channel parser
			// if anything fails, abort before we return the list
		  openFile();
			
			// minimum requirements for node: name[whitespace](  )
			//  and we already know that part is valid or the model wouldn't be loaded
			Pattern nodepattern = Pattern.compile("^([\\w_]+)\\s*\\(([^\\)]+)\\).*?");
			
			// minimum pattern for scalenode: name[whitespace](point)[whitespace]<values>
			//  and again we already know its valid
			Pattern snpattern = Pattern.compile("^([\\w_]+)(?:[^\\(]*)\\(([^\\)]*)(?:[^<]*<)([^>]*)>.*?");
			
			// and then a pattern to look for each type of channel pattern
			Pattern sqbrace = Pattern.compile("[^\\[]+\\[([^,]+),([^,]+),([^\\]]+)\\].*?");
			Pattern curbrace = Pattern.compile("[^\\{]+\\{([^,]+),([^,]+),([^\\}]+)\\}.*?");
			Pattern curbracesingle = Pattern.compile("[^\\{]+\\{([^\\}]+)\\}.*?");
			
			// HashMap to store found channels in
			HashMap<String,ArrayList<String>> channels = new HashMap<String,ArrayList<String>>();
			
				try
				{
			String line;
			boolean done = false;
			
			while( reader != null && reader.ready() && !done  && valid )
			{
				line = reader.readLine();
				while( reader.ready() && (line.length() == 0 || line.trim().charAt(0) == '#'))
				{	line = reader.readLine();	} // skip comments and blank lines
				if ( line.contains("===") )
				{
					nodesection = false;
				}
				Matcher m1 = nodepattern.matcher(line);
				Matcher m2 = snpattern.matcher(line);
				ArrayList<String> chans = new ArrayList<String>();
				if( m1.matches() && nodesection )
				{
					Matcher ma = sqbrace.matcher(line);
					Matcher mb = curbrace.matcher(line);
					// displacement channels
					if( ma.matches() && mb.matches() )
					{ // format matches, now ensure channels exist, otherwise error
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( ma.group(i).trim().equals("--") )
							{
								chans.add(i-1,"--"); // blank channel
							}
							else if( rbnbController.getChannel(ma.group(i).trim()) != null )
							{
								chans.add(i-1, ma.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+ma.group(i).trim()+" does not exist. (Node: "+m1.group(1).trim()+")");
								valid = false;
							}
						}
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( mb.group(i).trim().equals("--") )
							{
								chans.add(i+2,"--"); // blank channel
							}
							else if( rbnbController.getChannel(mb.group(i).trim()) != null )
							{
								chans.add(i+2, mb.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+mb.group(i).trim()+" does not exist. (Node: "+m1.group(1).trim()+")");
								valid = false;
							}
						}	
					}
					else if ( ma.matches() ) // just displacemet
					{
						// format matches, now ensure channels exist, otherwise error
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( ma.group(i).trim().equals("--") )
							{
								chans.add(i-1,"--"); // blank channel
							}
							else if( rbnbController.getChannel(ma.group(i).trim()) != null )
							{
								chans.add(i-1, ma.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+ma.group(i).trim()+" does not exist. (Node: "+m1.group(1).trim()+")");
								valid = false;
							}
						}
						if ( (line.contains("{") || line.contains("}")) && ! mb.matches() )
						{
							mpanel.popupError("Invalid rotation channel format. (Node: "+m1.group(1).trim()+")");
							valid = false;
						}
					}
					else if( mb.matches() ) // just rotation
					{ // format matches, now ensure channels exist, otherwise error
						chans.add( 0, "--" );
						chans.add( 1, "--" );
						chans.add( 2, "--" );
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( mb.group(i).trim().equals("--") )
							{
								chans.add(i+2,"--"); // blank channel
							}
							else if( rbnbController.getChannel(mb.group(i).trim()) != null )
							{
								chans.add(i+2, mb.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+mb.group(i).trim()+" does not exist. (Node: "+m1.group(1).trim()+")");
								valid = false;
							}
						}
						if ( (line.contains("[") || line.contains("]")) && ! ma.matches() )
						{
							mpanel.popupError("Invalid displacement channel format. (Node: "+m1.group(1).trim()+")");
							valid = false;
						}
					}
					else if ( ( line.contains("[")  || line.contains("]") ) && !ma.matches() )
					{
						popupError("Invalid displacement channel format for "+m1.group(1).trim() );
						valid = false;
					}
					else if ( ( line.contains("{")  || line.contains("}") )&& !mb.matches() )
					{
						popupError("Invalid rotation channel format for "+m1.group(1).trim() );
						valid = false;
					}
					if(valid)
					{ channels.put(m1.group(1).trim(), chans ); }
				}
				else if ( m2.matches() ) // scalenode
				{
					Matcher ma = sqbrace.matcher(line);
					Matcher mb = curbracesingle.matcher(line);
					if( ma.matches() && mb.matches() ) // both channel types
					{ // format matches, now ensure channels exist, otherwise error
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( ma.group(i).trim().equals("--") )
							{
								chans.add(i-1,"--"); // blank channel
							}
							else if( rbnbController.getChannel(ma.group(i).trim()) != null )
							{
								chans.add(i-1, ma.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+ma.group(i).trim()+" does not exist. (Node: "+m2.group(1).trim()+")");
								valid = false;
							}
						}
						if ( valid )
						{
							chans.add( 3, "--" );
							chans.add( 4, "--" );
							chans.add( 5, "--" );
							if( mb.group(1).trim().equals("--") )
							{
								chans.add(6,"--"); // blank channel
							}
							else if( rbnbController.getChannel(mb.group(1).trim()) != null )
							{
								chans.add(6, mb.group(1).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+mb.group(1).trim()+" does not exist. (Node: "+m2.group(1).trim()+")");
								valid = false;
							}
						}
					}
					else if ( ma.matches() ) // only displacement channels
					{
						for (int i = 1; i <= 3 && valid ; i++ )
						{ // ensure each channel exists or is --
							if( ma.group(i).trim() == "--" )
							{
								chans.add(i-1,"--"); // blank channel
							}
							else if( rbnbController.getChannel(ma.group(i).trim()) != null )
							{
								chans.add(i-1, ma.group(i).trim());
							}
							else
							{
								mpanel.popupError("Channel: "+ma.group(i).trim()+" does not exist. (Node: "+m2.group(1).trim()+")");
								valid = false;
							}
						}
					}
					else if (mb.matches() ) // only stress/strain channel
					{
						for (int i = 0; i < 6; i++ )
						{	chans.add( i, "--" );	} // pre-fill first six channels
						if( mb.group(1).trim() == "--" )
						{
							chans.add(6,"--"); // blank channel
						}
						else if( rbnbController.getChannel(mb.group(1).trim()) != null )
						{
							chans.add(6, mb.group(1).trim());
						}
						else
						{
							mpanel.popupError("Channel: "+mb.group(1).trim()+" does not exist. (Node: "+m2.group(1).trim()+")");
							valid = false;
						}
					}
					else if ( (line.contains("[") || line.contains("]") ) && !ma.matches() )
					{
						popupError("Invalid displacement channel format for "+m2.group(1).trim() );
						valid = false;
					}
					else if ( ( line.contains("{")  || line.contains("}") ) && !mb.matches() )
					{
						popupError("Invalid stress/strain channel format for "+m1.group(1).trim() );
						valid = false;
					}
					if( valid )
					{	channels.put(m2.group(1).trim(), chans );	}
				}
			}
			closeFile();
				}
				catch( IOException e )
				{
					System.out.println("IOError parsing channels.");
				}
			if( !valid ) // don't setup anything if a part of the config was invalid.
			{	channels.clear(); }
			return channels;
		}
		
		
		// helper functions for parsing the model file
		// Try to skip a number of sections, report succeed or fail.
		private boolean advanceSections(int numsections)
		{
			boolean done = false;
			boolean succeed = true;
				try{
			String line = reader.readLine();
			int breakcount = 0; // count === occurrences to find scale node section
			while(breakcount < numsections && !done && reader.ready())
			{
				line = reader.readLine();
				if(!reader.ready()) // if we hit EOF, done and fail
				{ done = true; succeed = false; }
				else if(line.equals("==="))
				{
					breakcount++;
				}
			}
				}
				catch(IOException e)
				{
					System.out.println("IOError skipping sections.");
				}
			return succeed;
		}

		// build a Poitn3d from a String: x,y,z
		private Point3d buildPoint(String point) throws PointFormatException
		{
			// ensure this is valid format. otherwise throw exception
			if (!point.matches("\\s*-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?\\s*,\\s*-?\\d+(\\.\\d+)?\\s*"))
			{ throw new PointFormatException("Invalid point format: ("+point+")"); }
			else
			{	
				double x,y,z;
				String[] s = point.split(",");
				x = Double.parseDouble(s[0]);
				y = Double.parseDouble(s[1]);
				z = Double.parseDouble(s[2]);
				return new Point3d(x,y,z);
			}
		}
		
		// build a Color3f from an ansi name or #xxxxxx String
		private Color3f parseColor(String line)
		{
			String color ="";
			if(line != null)
			{ color = line.trim(); }
			Color3f c = new Color3f(Color.LIGHT_GRAY); // setting default
			if( color.length() == 0)
			{ System.out.println("No color supplied, using default."); }
			else if(color.charAt(0) == '#')
			{		try
					{
				c.set(Color.decode(color));
					}
					catch(NumberFormatException e)
					{
						System.out.println("Invalid color ("+color+") in config file, using default.");
						c.set(Color.LIGHT_GRAY);
					}
			}
			else if(color.equalsIgnoreCase("blue"))
			{	c.set(Color.BLUE); }
			else if(color.equalsIgnoreCase("green"))
			{	c.set(Color.GREEN);	}
			else if(color.equalsIgnoreCase("red"))
			{	c.set(Color.RED);	}
			else if(color.equalsIgnoreCase("yellow"))
			{	c.set(Color.YELLOW);	}
			else if(color.equalsIgnoreCase("cyan"))
			{	c.set(Color.CYAN);	}
			else if(color.equalsIgnoreCase("pink"))
			{	c.set(Color.PINK);	}
			else if(color.equalsIgnoreCase("white"))
			{	c.set(Color.WHITE);	}
			else if(color.equalsIgnoreCase("black"))
			{	c.set(Color.BLACK);	}
			else if(color.equalsIgnoreCase("magenta"))
			{	c.set(Color.MAGENTA);	}
			else if(color.equalsIgnoreCase("orange"))
			{	c.set(Color.ORANGE);	}
			else if(color.equalsIgnoreCase("gray"))
			{	c.set(Color.LIGHT_GRAY);	}
			else
			{ System.out.println("Invalid color ("+color+") in config file, using default."); }
			return c;
		}
		
		// get member type from String 
		private int parseMemberType(String line)
		{
			int type = 0; // default to LINEAR
			if(line != null && line.trim().substring(1).equalsIgnoreCase("CUBIC"))
			{
				type = StructureModel.SMember.CUBIC;
			} // otherwise its LINEAR, which we already set
			return type;
		}
	} // end of SModelLoader

	
	/**
	*	ModelViewer is responsible for drawing the wireframe model of the
	*	associated {@link StructureModel} and rendering it into a canvas on screen. It is
	*	call driven, so must be told to update itself, which causes it to re-read
	*	values from the model and redraw the canvas.
	*/
	class ModelViewer extends JPanel
	{
		private SimpleUniverse suni;
		
		// branches for sync control
		private BranchGroup modelBranch; // holds top-level TGs --> bridgeBranch
		private BranchGroup bridgeBranch; // holds node and member branches
		private BranchGroup axesBranch; // holds axes
		
		// for setting up the initial view
		private Transform3D cameraTrans = new Transform3D();
		private Transform3D cameraorig;
		
		private Transform3D modelTrans = new Transform3D();
		private Transform3D modelorig;
	
		private Transform3D tiltzoomorig = new Transform3D();
		private Transform3D rotatetranslateorig = new Transform3D();

		private TransformGroup modelPlacementTG = new TransformGroup(); // used for initial positioning
		private TransformGroup rotatetranslateTG = new TransformGroup(); // used for tilt control via API methods (screen oriented)
		private TransformGroup tiltzoomTG = new TransformGroup();  // used for mouse and most transforms via API methods (model-centered)

		private MouseRotate mouseSpin = new MouseRotate();
   	private MouseZoom mouseSize = new MouseZoom();
   	private MouseTranslate mouseMove = new MouseTranslate();

		// where the nodes live
		//mappings of node names to nodetransforms and nodeshapes		 
		private HashMap<String,TransformGroup> nodetransformmap;
		private HashMap<String,Sphere> nodeshapemap;

		//  where the members' LineArrays live
		private HashMap<Integer,Shape3D> membermap;
		
		// BranchGroups for connecting nodes and members during sync.
		private BranchGroup nodebranch;
		private BranchGroup memberbranch;
		
		private Color3f highlightcolor = new Color3f(1.0f,0.7f,0.2f);
		private String highlightnode = "";
		private int highlightmember = 0;
		private Canvas3D canvas;
		private PickCanvas pcanvas;


		/**
		*	Constructs a new <code>ModelViewer</code>
		*/
		public ModelViewer()
		{
			nodetransformmap = new HashMap<String,TransformGroup>();
			nodeshapemap = new HashMap<String,Sphere>();
			membermap = new HashMap<Integer,Shape3D>();
			
			setLayout(new BorderLayout());
			modelBranch = new BranchGroup();
			bridgeBranch = new BranchGroup();
			bridgeBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			bridgeBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			bridgeBranch.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
			bridgeBranch.setBoundsAutoCompute(true);
		
			tiltzoomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    tiltzoomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

	    rotatetranslateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    rotatetranslateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    
	    modelPlacementTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    modelPlacementTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    
	    mouseSpin.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
	    mouseSize.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
	    mouseMove.setCapability(Behavior.ALLOW_BOUNDS_WRITE);

	    // from java's ColorCube tutorial
			// set up Mouse Controls for zoom/spin/rotate

	    // bounds were 10000.0 ... check how big they actually need to be....? pixels?
	    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);

   		//Setup and apply rotation behavior
   		mouseSpin.setFactor(mouseSpin.getXFactor()*0.6);
			mouseSpin.setTransformGroup(rotatetranslateTG);
			modelBranch.addChild(mouseSpin);
			mouseSpin.setSchedulingBounds(bounds);

			//Setup and apply zoom behavior
			mouseSize.setTransformGroup(tiltzoomTG);
			modelBranch.addChild(mouseSize);
			mouseSize.setSchedulingBounds(bounds);
   		//Setup and apply translate behavior
			mouseMove.setTransformGroup(rotatetranslateTG);
			modelBranch.addChild(mouseMove);
			mouseMove.setSchedulingBounds(bounds);
			
			// nest view control groups.
			// tiltzoom --> rotate --> local placement and orientation
			modelBranch.addChild(tiltzoomTG);
			tiltzoomTG.addChild(rotatetranslateTG);
			rotatetranslateTG.addChild(modelPlacementTG);
			modelPlacementTG.addChild(bridgeBranch);
		    
			canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
			add(canvas,BorderLayout.CENTER );

			suni = new SimpleUniverse(canvas);
			suni.getViewingPlatform().setNominalViewingTransform();
			
			/* Following DepthBufferFreeze is magic to make lines render with correct occlusion.
			 * Actually, due to the way the underlying OpenGL/hardware processes decide to render
			 * things, opaque and antialiased objects are rendered on different passes and the 
			 * hardware buffered is updated in specific ways. In general, this causes the Z-order
			 * of antialiased objects to effectively be ignored, the following freeze prevents this,
			 * probably sacrificing some amount of computation overhead to do so, but it gives us what
			 * we want here (correctly rendered member depth) and I don't see the potential performance
			 * hit causing a problem
			 */
			suni.getViewer().getView().setDepthBufferFreezeTransparent(false);
			suni.addBranchGraph(modelBranch);
			
			// setup mouse picker
			pcanvas = new PickCanvas(canvas,modelBranch);
			mpicker = new MousePicker(pcanvas);
			
			nodebranch = new BranchGroup();
			memberbranch = new BranchGroup();
			nodebranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			nodebranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			nodebranch.setCapability(BranchGroup.ALLOW_DETACH);
			memberbranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			memberbranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);	
			memberbranch.setCapability(BranchGroup.ALLOW_DETACH);
			
			axesBranch = createAxes();
			
			bridgeBranch.addChild(nodebranch);
			bridgeBranch.addChild(memberbranch);
			bridgeBranch.addChild(axesBranch);
		} // end constructor

		
		/**
		 * This synchronizes the <code>ModelViewer</code> scenegraph object with the <code>StructureModel</code>
		 */
		public void sync()
		{
			// detach for editing
			nodebranch.detach();
			memberbranch.detach();
			
			Iterator<String> modelnodes = model.nodes.keySet().iterator();
			String nextnode;
			while(modelnodes.hasNext())
			{
				nextnode = modelnodes.next();
				if( !nodeshapemap.containsKey(nextnode) ) // can check either, sync must keep both maps synced in tandem
				{
					float radius = (float)Math.max(model.getMaximumSize()/75.0, 0.01);
					Sphere sphere = new Sphere(radius);
					sphere.getShape().setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
					Appearance ap = new Appearance();
					ap.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
					ap.setColoringAttributes(new ColoringAttributes(model.nodes.get(nextnode).color, ColoringAttributes.SHADE_FLAT));
					sphere.setAppearance(ap);
					TransformGroup tg = new TransformGroup();
					tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
					Transform3D tr = new Transform3D();
					tr.set(new Vector3d(model.nodes.get(nextnode).position));
					tg.setTransform(tr);
					tg.addChild(sphere);
           
					nodebranch.addChild(tg);
					nodetransformmap.put(nextnode, tg);
					nodeshapemap.put(nextnode, sphere);
				}
			}
			// now remove anything from the mview maps that is no longer in the model
			Iterator<String> mviewnodes = nodeshapemap.keySet().iterator();
			while( mviewnodes.hasNext() )
			{
				nextnode = mviewnodes.next();
				if( !model.containsNode(nextnode) )
				{
					// remove transformgroup from branch
					nodebranch.removeChild(nodetransformmap.get(nextnode));
					// remove from mview maps
					nodetransformmap.remove(nextnode);
					mviewnodes.remove(); // remove last pop from nodeshapemap via iterator
				}
			}
			
			// member syncing
			Appearance lineAppearance = new Appearance();
			lineAppearance.setLineAttributes(new LineAttributes(2, LineAttributes.PATTERN_SOLID,true));
			Iterator<Integer> modelmembers = model.members.keySet().iterator();
			Integer nextmember;
			while( modelmembers.hasNext() )
			{
				nextmember = modelmembers.next();
				if( !membermap.containsKey(nextmember) )
				{
					LineStripArray l;
					int format = GeometryArray.COORDINATES | GeometryArray.COLOR_3;
					int[] longstripcount = { 40 }; //(only ever one line)
					int[] shortstripcount = { 2 }; //(only ever one line)
					if( model.members.get(nextmember).curveType == StructureModel.SMember.LINEAR )
					{
						l = new LineStripArray(2,format,shortstripcount);
					}
					else // cubic
					{
						l = new LineStripArray(40,format,longstripcount);
					}
					l.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
					l.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
					l.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
					
					Shape3D lineShape = new Shape3D(l,lineAppearance);
					lineShape.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
					lineShape.setBoundsAutoCompute(true);
					lineShape.setCapability(Shape3D.ALLOW_BOUNDS_WRITE);
					
					lineShape.setPickable( (editor.getMode() == ModelEditor.MEMBER) ); // set Pickable based on current mode
					memberbranch.addChild(lineShape);
					membermap.put(nextmember,lineShape);
				}
			}
			Iterator<Integer> mviewmembers = membermap.keySet().iterator();
			while( mviewmembers.hasNext() )
			{
				nextmember = mviewmembers.next();
				if( !model.members.containsKey(nextmember) )
				{
					memberbranch.removeChild(membermap.get(nextmember));
					mviewmembers.remove(); // remove last pop from membermap via iterator
				}
			}
			
			bridgeBranch.addChild(nodebranch);
			bridgeBranch.addChild(memberbranch); // reattach
			update();
		}
				
		
		/**
		 * Update the viewing platform and transform to reflect the model's new size and shape
		 * for proper viewing and scaling. This method adjusts center and scaling, but does not
		 * change the model's orientation onscreen. See resetScene() for that.
		 */
		public void viewsync()
		{
			// update mouse action bounds
			double scale = model.getMaximumSize();
			BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10.0*scale);
			mouseSize.setBounds(bounds);
			mouseSize.setSchedulingBounds(bounds);
	    mouseSize.setFactor(scale/75);
			mouseMove.setBounds(bounds);
			mouseMove.setSchedulingBounds(bounds);
	    mouseMove.setFactor(scale/100);
			mouseSpin.setBounds(bounds);
			mouseSpin.setSchedulingBounds(bounds);
			
			// update view controls rotation bases
			// model is now upright vs. toward screen
			modelTrans.rotX(-Math.PI*1/2);
			/* Translate to the center of the model. Orientation is
			 * rotated versus screen. y <=> z
			*/
			// update model location
			Point3d modelcenter = model.getCenter();
			modelTrans.setTranslation(new Vector3d(-modelcenter.x,-modelcenter.z,modelcenter.y));
			
			modelPlacementTG.setTransform(modelTrans);
			modelorig = new Transform3D(modelTrans); // save for resetScene
					
			// rotate the initial viewpoint based on height vs. depth
			// tilt a low height, high depth model towards camera for better viewing
			double height = model.getHeight();
			double depth = model.getDepth();
			tiltzoomorig.rotX((1.0-(depth > height ? height/depth : 1.0))*Math.PI/4.0); // prep for resetScene
			tiltzoomTG.setTransform(tiltzoomorig); 
			
			// update viewingplatform location
			// zero nodes, one node, no height, large
			Point3d eye = new Point3d();
			double maxsize = model.getMaximumSize();
			if( maxsize > 0 ) 
			{
				Iterator<String> points = model.nodes.keySet().iterator();
				double sw = (double) canvas.getWidth(); // fov is based on sw, need to scale for fov relative to screen height
				double sh = (double) canvas.getHeight();
				double borderFactor = 1.1;
				double screenRatioAdjust = Math.max(1.0, sw/sh);
				BoundingSphere b = new BoundingSphere(modelcenter,maxsize/10.0);
				while( points.hasNext() )
				{
					Point3d np = model.nodes.get(points.next()).getBasePosition();
					b.combine(np);
				}
				eye.set(0,0,b.getRadius()*borderFactor*screenRatioAdjust / Math.tan(suni.getViewer().getView().getFieldOfView()/2) );
			}
			else // for zero or one nodes
			{
				eye.set(0.0,0,1.75);
			}
			
			// set clipping distances
			suni.getViewer().getView().setBackClipDistance(eye.z*2);
			suni.getViewer().getView().setFrontClipDistance(eye.z*2/500.0);
			
			cameraTrans.lookAt(eye, new Point3d(), new Vector3d(0,1,0));
			cameraTrans.invert();
			cameraorig = new Transform3D(cameraTrans);
			suni.getViewingPlatform().getViewPlatformTransform().setTransform(cameraTrans);
			// also take the mouse TG back to regular zoom (but leave translation/rotation as-is)
			Transform3D delta = new Transform3D();
			tiltzoomTG.getTransform(delta);
			delta.setScale(1.0);
			tiltzoomTG.setTransform(delta);
			
			// update axes scale
			axesBranch.detach();
			axesBranch = createAxes();
			bridgeBranch.addChild(axesBranch);
		}

		
		/**
		*	Update the canvas with new values from the model
		*	This is not optimized at all, so should only be called when there are
		*	new values in the model to avoid a performance penalty.
		*/
		public void update()
		{
			updateNodes();
			updateMembers();
		}

		
		/**
		 * Destroys the model currently being displayed
		 */
		public void destroyModelBranch()
		{
			bridgeBranch.removeAllChildren();
			nodebranch.removeAllChildren();
			memberbranch.removeAllChildren();
			if( !(nodetransformmap == null) )
			{	nodetransformmap.clear();	}
			if( !(nodeshapemap == null)  )
			{	nodeshapemap.clear();	}
			if( !(membermap == null) )
			{	membermap.clear();	}
			resetScene();
		}

		
		// axes scaled by (1/32) of max_size
		private BranchGroup createAxes()
		{
			double scale = (1.0/32.0)*model.getMaximumSize();
			float label_scale = (float)((1.0/16.0)*model.getMaximumSize());
			Matrix3f matrix = new Matrix3f(1,0,0,0,1,0,0,0,1);
			
			BranchGroup axes = new BranchGroup();
			Point3d mc = model.getCenter();
			TransformGroup tg = new TransformGroup(new Transform3D(matrix,new Vector3d(mc.x,mc.y,model.getAxisExtreme(ModelPanel.MIN, ModelPanel.Z_AXIS)),1.0));
			
			axes.addChild(tg);
			axes.setCapability(BranchGroup.ALLOW_DETACH);
			axes.setCapability(Group.ALLOW_CHILDREN_WRITE);
			axes.setCapability(Group.ALLOW_CHILDREN_READ);
			axes.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			Color3f blue = new Color3f(0.0f,0.0f,1.0f);
		
			LineArray x_axis = new LineArray(2,LineArray.COORDINATES|LineArray.COLOR_3);
			x_axis.setCoordinate(0,new Point3f((float)(-scale),0.0f,0.0f));
			x_axis.setCoordinate(1,new Point3f((float)(scale),0.0f,0.0f));
			x_axis.setColor(0,blue);
			x_axis.setColor(1,blue);
			tg.addChild(new Shape3D(x_axis));
		
			LineArray y_axis = new LineArray(2,LineArray.COORDINATES|LineArray.COLOR_3);
			y_axis.setCoordinate(0,new Point3f(0.0f,(float)(-scale),0.0f));
			y_axis.setCoordinate(1,new Point3f(0.0f,(float)(scale),0.0f));
			y_axis.setColor(0,blue);
			y_axis.setColor(1,blue);
			tg.addChild(new Shape3D(y_axis));
		
			LineArray z_axis = new LineArray(2,LineArray.COORDINATES|LineArray.COLOR_3);
			z_axis.setCoordinate(0,new Point3f(0.0f,0.0f,(float)(-scale)));
			z_axis.setCoordinate(1,new Point3f(0.0f,0.0f,(float)(scale)));
			z_axis.setColor(0,blue);
			z_axis.setColor(1,blue);
			tg.addChild(new Shape3D(z_axis));
		
			// labels
			Text2D x_label = new Text2D("x",new Color3f(1.0f,0.0f,0.0f),"SansSerif",150,java.awt.Font.BOLD);
			x_label.setRectangleScaleFactor(x_label.getRectangleScaleFactor()*label_scale);
			// was scale, -0.25,
			TransformGroup xtg = new TransformGroup(new Transform3D(matrix,new Vector3d(scale,0,0),1.0));
			xtg.addChild(x_label);
			tg.addChild(xtg);
			Text2D y_label = new Text2D("y",new Color3f(1.0f,0.0f,0.0f),"SansSerif",150,java.awt.Font.BOLD);
			y_label.setRectangleScaleFactor(y_label.getRectangleScaleFactor()*label_scale);
			// was -0.25,scale,
			TransformGroup ytg = new TransformGroup(new Transform3D(matrix,new Vector3d(0,scale,0),1.0));
			ytg.addChild(y_label);
			tg.addChild(ytg);
			Text2D z_label = new Text2D("z",new Color3f(1.0f,0.0f,0.0f),"SansSerif",150,java.awt.Font.BOLD);
			z_label.setRectangleScaleFactor(z_label.getRectangleScaleFactor()*label_scale);
			TransformGroup ztg = new TransformGroup();
			Transform3D z_trans = new Transform3D(new Matrix3f(-1,0,0,0,0,-1,0,-1,0), new Vector3d(0,0,1.5*scale),1.0);
			ztg.setTransform(z_trans);
			ztg.addChild(z_label);
			tg.addChild(ztg);
			// turn off picking for all axes shapes
			for (int i = 0; i < axes.numChildren() ; i++ )
			{
				axes.getChild(i).setPickable(false);
			}
			return axes;
		}


		private void updateNodes()
		{
			Sphere s = new Sphere((float)Math.max(model.getMaximumSize()/100.0,0.01));
			Geometry g = s.getShape().getGeometry();
			Iterator<String> nodes = model.nodes.keySet().iterator();
			while( nodes.hasNext() )
			{
				String nextnode = nodes.next();
				Transform3D tr = new Transform3D();
				Point3d newloc = new Point3d(model.nodes.get(nextnode).position);
				newloc.add(model.nodes.get(nextnode).displacement);
				tr.set(new Vector3d(newloc));
				nodetransformmap.get(nextnode).setTransform(tr);
				
				// Should look into a way to avoid updating _every_ node's color...
				//	Keep track of previously highlighted node?
				Appearance ap = nodeshapemap.get(nextnode).getAppearance();
				if( nextnode.equals(highlightnode) )
					ap.setColoringAttributes(new ColoringAttributes(highlightcolor,ColoringAttributes.SHADE_FLAT));
				else
					ap.setColoringAttributes(new ColoringAttributes(model.nodes.get(nextnode).color,ColoringAttributes.SHADE_FLAT));
				nodeshapemap.get(nextnode).getShape().setGeometry(g);
			}
		}

		private void updateMembers()
		{
			Iterator<Integer> members = model.members.keySet().iterator();
			while( members.hasNext() )
			{
				Integer nextmember = members.next();
				StructureModel.SMember curmember = model.members.get(nextmember);
				LineStripArray curline = (LineStripArray) membermap.get(nextmember).getGeometry();
				if(curmember.curveType == StructureModel.SMember.LINEAR)
				{
					curline.setCoordinate(0,(Point3d)model.nodes.get(curmember.nodes[0]).getPosition());
					curline.setCoordinate(1,(Point3d)model.nodes.get(curmember.nodes[1]).getPosition());
					Color3f c = curmember.color;
					if( nextmember == highlightmember )
					{
						c = highlightcolor;
					}
					curline.setColor(0,c);
					curline.setColor(1,c);
				}
				else if(curmember.curveType == StructureModel.SMember.CUBIC)
				{
					// calculate the control points
					StructureModel.SNode	nodeone = model.nodes.get(curmember.nodes[0]),
											nodetwo = model.nodes.get(curmember.nodes[1]);
					Point3d endone =	nodeone.getBasePosition(),
							endtwo =	nodetwo.getBasePosition();
					double cx,cy,cz, percentage = 0.4;
					// put cx,cy,cz at a percentage of the length along the unperturbed positions on endpoints
					// from nodeone's position
					cx = nodeone.getPosition().x + (percentage)*(endtwo.x-endone.x);
					cy = nodeone.getPosition().y + (percentage)*(endtwo.y-endone.y);
					cz = nodeone.getPosition().z + (percentage)*(endtwo.z-endone.z);
					// account for rotation
					
					Point3d controlone = rotatePoint(new Point3d(cx,cy,cz),nodeone.getPosition(),nodeone.getRotation());
					
					// repeat for control point two
					cx = nodetwo.getPosition().x + (percentage)*(endone.x-endtwo.x);
					cy = nodetwo.getPosition().y + (percentage)*(endone.y-endtwo.y);
					cz = nodetwo.getPosition().z + (percentage)*(endone.z-endtwo.z);
					Point3d controltwo = rotatePoint(new Point3d(cx,cy,cz),nodetwo.getPosition(),nodetwo.getRotation());
					// update endone and endtwo to displaced positions
					endone = nodeone.getPosition();
					endtwo = nodetwo.getPosition();
					// now we have the control points:
					// endone, controlone, controltwo, endtwo
					
					Color3f c = curmember.color;
					if( nextmember == highlightmember )
					{ c = highlightcolor; }
					int jtotal = curline.getVertexCount();
					for (int j = 0; j<jtotal; j++)
					{
						Point3d next = getBezPoint((double)j/(double)(jtotal-1),endone,controlone,controltwo,endtwo);
						curline.setCoordinate(j, next);
						curline.setColor(j,c);
					}
				}
			} // end while
		}

		/**
		 * Get the <code>String</code> name for the <code>Node</code> specified
		 * @param s the <code>Node</code> to find the name of
		 * @return  the name of the node as a <code>String</code>
		 */
		public String getNodeName(Node s)
		{
			Iterator<String> names = nodeshapemap.keySet().iterator();
			boolean found = false;
			String next = "";
			while( names.hasNext() && !found )
			{
				next = names.next();
				if( (Node)nodeshapemap.get(next) == (Node)s )
				{	found = true;	}
			}
			return next;
		}
		
		/**
		 * Get the <code>int</code> reference for the <code>Node</code> specified
		 * @param s the <code>Node</code> to find the name of
		 * @return the reference of the member as an <code>int</code>
		 */
		public int getMemberRef(Node s)
		{
			Iterator<Integer> refs = membermap.keySet().iterator();
			boolean found = false;
			Integer next = 0;
			while( refs.hasNext() && !found )
			{
				next = refs.next();
				if( (Node)membermap.get(next) == s )
				{	found = true;	}
			}
			return next;
		}
		
		// rotate a point about a center point on three axes
		private Point3d rotatePoint(Point3d p, Point3d center, Point3d rotation)
		{
			double xangle = rotation.x;
			double yangle = rotation.y;
			double zangle = rotation.z;
			p.sub(center);
			Transform3D xrot = new Transform3D();
			xrot.set(new Matrix3d(1,0,0,0,Math.cos(xangle),(-1)*Math.sin(xangle),0,Math.sin(xangle),Math.cos(xangle)));
			xrot.transform(p);
			Transform3D yrot = new Transform3D();
			yrot.set(new Matrix3d(Math.cos(yangle),0,Math.sin(yangle),0,1,0,(-1)*Math.sin(yangle),0,Math.cos(yangle)));
			yrot.transform(p);
			Transform3D zrot = new Transform3D();
			zrot.set(new Matrix3d(Math.cos(zangle),(-1)*Math.sin(zangle),0,Math.sin(zangle),Math.cos(zangle),0,0,0,1));
			zrot.transform(p);
			p.add(center);
			return p;
		}
		
		// compute a Bezier point along a curve
		private Point3d getBezPoint(double percent,Point3d end1, Point3d ctrl1, Point3d ctrl2, Point3d end2)
		{
			Point3d result = new Point3d();
			double x,y,z;
			x = end1.x*Bez1(percent) + ctrl1.x*Bez2(percent) + ctrl2.x*Bez3(percent) + end2.x*Bez4(percent);
			y = end1.y*Bez1(percent) + ctrl1.y*Bez2(percent) + ctrl2.y*Bez3(percent) + end2.y*Bez4(percent);
			z = end1.z*Bez1(percent) + ctrl1.z*Bez2(percent) + ctrl2.z*Bez3(percent) + end2.z*Bez4(percent);
			result.set(x, y, z);
			return result;
		}
				
		// Bezier functions for computing cubic curves
		private double Bez1(double percent)
		{
			return percent*percent*percent;
		}
		private double Bez2(double percent)
		{
			return 3*percent*percent*(1-percent);
		}
		private double Bez3(double percent)
		{
			return 3*percent*(1-percent)*(1-percent);
		}
		private double Bez4(double percent)
		{
			return (1-percent)*(1-percent)*(1-percent);
		}

		/**
		*	Rotates the displayed model around its own z-axis
		*	@param degrees	the number of degrees to rotate the model
		* @param relative whether this rotation is relative to current position or absolute (relative to initial position)
		*/
		public void rotateModel(float degrees, boolean relative)
		{
				Transform3D current = new Transform3D();
				Transform3D delta = new Transform3D();
				rotatetranslateTG.getTransform(current);
				if( ! relative ) // if not relative, reset rotation to zero before multiply
				{	current.rotY(0); } 
				delta.rotY(degrees/180*Math.PI);
				current.mul(delta);
				rotatetranslateTG.setTransform(current);
		}
		
		/**
		*	Tilts the displayed model around its own y-axis towards the viewer
		*	@param degrees	the number of degrees to rotate the model
		* @param relative whether this tilt is relative to current position or absolute (relative to initial position)
		*/
		public void tiltModel(float degrees, boolean relative)
		{
			Transform3D delta = new Transform3D();
			Transform3D current = new Transform3D();
			tiltzoomTG.getTransform(current);
			if( !relative )
			{	current.rotX(0);	}
			delta.rotX(degrees/180f*Math.PI);
			current.mul(delta);
			tiltzoomTG.setTransform(current);
		}
		
		/**
		*	Zooms the camera in or out
		*	@param	factor	scale percentage (>1.0 to zoom in, <1.0 to zoom out)
		* @param relative whether this zoom is relative to current position or absolute (relative to initial position)
		*/
		public void zoomCamera(float factor, boolean relative)
		{
			Transform3D delta = new Transform3D();
			tiltzoomTG.getTransform(delta);
			if( relative )
			{ delta.setScale(delta.getScale()*factor); }
			else
			{ delta.setScale(factor); }
			tiltzoomTG.setTransform(delta);
		}

		/**
		*	Resets the camera to its initial viewpoint.
		*/
		public void resetScene()
		{
			viewsync();
			tiltzoomTG.setTransform(tiltzoomorig);
			modelPlacementTG.setTransform(modelorig);
			rotatetranslateTG.setTransform(rotatetranslateorig);
			suni.getViewingPlatform().getViewPlatformTransform().setTransform(cameraorig);
		}

		/**
		*	Highlights the specified node in the model
		*	@param nodename the name of the node to highlight
		*/
		public void highlightNode(String nodename)
		{
			highlightnode = nodename;
			updateNodes();
		}
		
		/**
	    * Highlights the specified member in the model
	    * @param memberref the reference of the member to highlight
	    */
		public void highlightMember(int memberref)
		{
			highlightmember = memberref;
			updateMembers();
		}

		/**
		* Set the color used to highlight nodes or members.
		*	@param c 		the <code>Color3f</code> to user for highlighting
		*/
		public void setHighlightColor( Color3f c)
		{
			highlightcolor = c;
		}
	}// end ModelViewer
	

	/**
	*	This class groups the model canvas and the toolbars together into
	*	one component that can be used as the <code>dataComponent</code>
	*	for the {@link DataPanel} interface.
	*/
	class ViewPanel extends JPanel implements ActionListener
	{
		
		private float zoom_factor = 1;
		
		private JMenuBar menubar		= new JMenuBar();
		private JMenu modelmenu			= new JMenu("Model");
		private JMenu viewmenu			= new JMenu("View");
		private JMenu editmenu			= new JMenu("Edit");
		private JMenuItem open			= new JMenuItem("Open model");
		private JMenuItem close			= new JMenuItem("Close model");
		private JMenuItem frontview		= new JMenuItem("Front View");
		private JMenuItem topview		= new JMenuItem("Top View");
		private JMenuItem cornerview  = new JMenuItem("Corner View");
		private JMenuItem ccw15  = new JMenuItem("Rotate CCW 15°");
		private JMenuItem cw15  = new JMenuItem("Rotate CW 15°");
		private JMenuItem ccw90  = new JMenuItem("Rotate CCW 90°");
    private JMenuItem cw90  = new JMenuItem("Rotate CW 90°");
    
		private JMenuItem deselect		= new JMenuItem("Deselect All");
		private JMenuItem loadchans		= new JMenuItem("Load Channels");
		private JMenuItem zoomin		= new JMenuItem("Zoom In");
		private JMenuItem zoomout		= new JMenuItem("Zoom Out");
		private JMenuItem save			= new JMenuItem("Save Model");
		private JMenuItem savewchans	= new JMenuItem("Save Model with Channels");
		private JCheckBoxMenuItem editlinks   = new JCheckBoxMenuItem("Edit Channel Links");
		private JCheckBoxMenuItem editnodes		= new JCheckBoxMenuItem("Edit Nodes");
		private JCheckBoxMenuItem editmembers	= new JCheckBoxMenuItem("Edit Members");
		private JCheckBoxMenuItem editsnodes	= new JCheckBoxMenuItem("Edit Stress/Strain Nodes");
		private ButtonGroup editgroup			= new ButtonGroup();
		
		private JButton zoominbutton = new JButton("+");
		private JButton zoomoutbutton = new JButton("-");
		private JButton ccw90b   = new JButton("CCW");
    private JButton ccw15b       = new JButton("CCW");
    private JButton cw90b  = new JButton("CW");
    private JButton cw15b       = new JButton("CW");
    private JLabel fifteen = new JLabel("15°");
    private JLabel ninety = new JLabel("90°");
		
		private JPanel bottompane = new JPanel(new BorderLayout());
		private JPanel viewcontrols = new JPanel(new GridBagLayout());
		/** reference for edit pane */
		// should look into rebuilding this so that axis selection is replace by edit tools
		private JPanel editpanel;
		
		
		/**
		 * Constructs a new <code>ViewPanel</code>.
		 */
		public ViewPanel()
		{
			setLayout(new BorderLayout());
			modelmenu.add(open);
			modelmenu.add(close);
			modelmenu.add(loadchans);
			modelmenu.addSeparator();
			modelmenu.add(save);
			modelmenu.add(savewchans);

			
			viewmenu.add(frontview);
			viewmenu.add(topview);
			viewmenu.add(cornerview);
			viewmenu.addSeparator();
			viewmenu.add(deselect);
			viewmenu.addSeparator();
      viewmenu.add(zoomin);   zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(zoomout);  zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(ccw15);    ccw15.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(cw15);     cw15.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(ccw90);    ccw90.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK));
      viewmenu.add(cw90);     cw90.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK));
      
      
      editmenu.add(editlinks);
			editmenu.add(editnodes);
			editmenu.add(editmembers);
			editmenu.add(editsnodes);
			editgroup.add(editlinks);
			editgroup.add(editnodes);
			editgroup.add(editmembers);
			editgroup.add(editsnodes);
			editgroup.setSelected(editlinks.getModel(), true); // edit links selected by default
			
			modelmenu.getPopupMenu().setLightWeightPopupEnabled(false);
			viewmenu.getPopupMenu().setLightWeightPopupEnabled(false);
			editmenu.getPopupMenu().setLightWeightPopupEnabled(false);
			
			menubar.add(modelmenu);
			menubar.add(viewmenu);
			menubar.add(editmenu);
			
			// configure the buttons
			Insets inset = new Insets(1,3,1,3);
			zoominbutton.setFocusPainted(false);
			zoominbutton.setMargin(inset);
			zoomoutbutton.setFocusPainted(false);
			zoomoutbutton.setMargin(inset);
			ccw15b.setFocusPainted(false);
			ccw15b.setMargin(inset);
			cw15b.setFocusPainted(false);
			cw15b.setMargin(inset);
			ccw90b.setFocusPainted(false);
			ccw90b.setMargin(inset);
      cw90b.setFocusPainted(false);
      cw90b.setMargin(inset);
      // creating the view control button panel
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			viewcontrols.add(zoominbutton,c);
			c.gridx = 2;
			viewcontrols.add(zoomoutbutton,c);
			c.gridy = 1;
			c.gridx = 0;
			viewcontrols.add(ccw15b,c);
			c.gridx = 1;
			viewcontrols.add(fifteen,c);
			c.gridx = 2;
			viewcontrols.add(cw15b,c);
			c.gridx = 0;
			c.gridy = 2;
			viewcontrols.add(ccw90b,c);
      c.gridx = 1;
      viewcontrols.add(ninety,c);
      c.gridx = 2;
      viewcontrols.add(cw90b,c);
      
			
			add(menubar,BorderLayout.NORTH);
			add(mview, BorderLayout.CENTER);
			editpanel = editor.getAxisPanel();
			add(bottompane,BorderLayout.SOUTH);
			viewcontrols.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,2,0,0,Color.DARK_GRAY),BorderFactory.createEmptyBorder(1, 2, 1, 2)));
			bottompane.add(editpanel,BorderLayout.CENTER);
			bottompane.add(viewcontrols,BorderLayout.EAST);
						
			// adding listeners
			open.addActionListener(this);
			close.addActionListener(this);
			loadchans.addActionListener(this);
			save.addActionListener(this);
			savewchans.addActionListener(this);
			editnodes.addActionListener(this);
			editmembers.addActionListener(this);
			editsnodes.addActionListener(this);
			editlinks.addActionListener(this);
			zoomin.addActionListener(this);
			zoomout.addActionListener(this);
			frontview.addActionListener(this);
			topview.addActionListener(this);
			cornerview.addActionListener(this);
			deselect.addActionListener(this);
			zoominbutton.addActionListener(this);
			zoomoutbutton.addActionListener(this);
			cw15b.addActionListener(this);
      ccw15b.addActionListener(this);
      cw90b.addActionListener(this);
      ccw90b.addActionListener(this);
      ccw15.addActionListener(this);
      cw15.addActionListener(this);
      ccw90.addActionListener(this);
      cw90.addActionListener(this);
      
		}// end constructor
		
	/*
	 * Sets the editing panel to the passed panel and validates layouts and repaints screen as needed
	 */
		private void setEditPanel(JPanel newpanel)
		{
		  if( editpanel != null )
      {
        bottompane.remove(editpanel);
        bottompane.revalidate();
      }
		  editpanel = newpanel;
		  bottompane.add(editpanel,BorderLayout.CENTER);
		  bottompane.revalidate();
		  bottompane.repaint();
		}
		
		/**
		*	Handles the button presses and notifies the <code>ModelViewer</code> of what to do
		*	@param	e	the action event that occurred
		*/
		public void actionPerformed(ActionEvent e)
		{
			// check view buttons first
			Object source = e.getSource();
			if( source == close )
			{
				// reset into linking mode
			  setEditPanel(editor.getAxisPanel());
			  editgroup.setSelected(editlinks.getModel(), true);
				mpanel.destroyModel();
			}
			else if( source == open )
			{
				// launch the file select dialog
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(dataComponent);
				if(returnVal != JFileChooser.APPROVE_OPTION) 
				{	return;	}  // break out if user cancels
				File tempfile = chooser.getSelectedFile();
				if (tempfile == null || !tempfile.isFile() || !tempfile.exists()) 
			  {	return;	} // break out if file does not exist
				
				// reset into linking mode
        setEditPanel(editor.getAxisPanel());
        editgroup.setSelected(editlinks.getModel(), true);
				// and create new model
        mpanel.destroyModel();
				loader.setFile(tempfile);
				loader.processModelFile();
				mview.sync();
				mview.resetScene();
				
			}
			else if( source == save )
			{
				mpanel.saveModel();
			}
			else if( source == savewchans )
			{
				mpanel.saveModel(true);
			}
			else if( source == editnodes )
			{
			  setEditPanel(editor.getNodePanel());
			}
			else if( source == editmembers )
			{
			  setEditPanel(editor.getMemberPanel());
			}
			else if( source == editsnodes )
			{
			  setEditPanel(editor.getScaleNodePanel());
			}
			else if( source == editlinks )
			{
			  setEditPanel(editor.getAxisPanel());
			}
			else if(source == frontview)
			{
				mview.resetScene();
			}
			else if(source == topview)
			{
				mview.resetScene();
				mview.tiltModel(90, false);
			}
			else if(source == cornerview)
			{
				mview.resetScene();
				mview.rotateModel(-35, false); // reset and initial modification
				mview.tiltModel(20, true); // second modification must be relative
				mview.zoomCamera(0.9f, true);
			}
			else if(source == zoomin || source == zoominbutton)
			{
				mview.zoomCamera(1.1f, true);
			}
			else if(source == zoomout || source == zoomoutbutton)
			{
				mview.zoomCamera(0.9f, true);
			}
			else if(source == cw15b || source == cw15 )
      {
        mview.rotateModel(-15, true);
      }
      else if(source == ccw15b || source == ccw15 )
      {
        mview.rotateModel(15, true);
      }
      else if(source == ccw90b || source == ccw90 )
      {
        mview.rotateModel(90, true);
      }
      else if(source == cw90b || source == cw90)
      {
        mview.rotateModel(-90, true);
      }
			else if( source == deselect )
			{
				editor.deselectAll(); // deselect all
			}
			else if( source == loadchans)
			{
				// reset into linking mode
        setEditPanel(editor.getAxisPanel());
        editgroup.setSelected(editlinks.getModel(), true);
				mpanel.loadchannels();
			}
		} // end actionPerformed()
	}
	
	// controls which listeners are acting on the canvas
	//   either for editing of linking, etc
	/**
	 * Responsible for controlling the listeners in the view area for mouse selection of the model elements.
	 */
	class MousePicker
	{
		PickCanvas pcanvas;
		JPopupMenu axismenu,nodemenu,membermenu,nconnmenu;
		JLabel memberlabel, nodelabel, nconnlabel;
		MouseAdapter currentpicker;
		
		// node-axis picker
		MouseAdapter napick = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				Node p = getFinalPick(e,PickResult.PRIMITIVE);
				if( p != null )
				{
					editor.selectNode(mview.getNodeName(p));
					if(!(model.nodes.get(editor.selectednode) instanceof StructureModel.ScaleNode))
					{ // ensure stress/strain axis is not selected for non-ScaleNode
					 	if(editor.selectedaxis == STR)
					 	{	editor.selectedaxis = -1;	}
					} 
					else
					{// also ensure rotation axes are not selected for ScaleNodes
						if(editor.selectedaxis == X_ROT || editor.selectedaxis == Y_ROT || editor.selectedaxis == Z_ROT)
						{ editor.selectedaxis = -1;	}
					}
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						if((model.nodes.get(editor.selectednode) instanceof StructureModel.ScaleNode))
						{
							axismenu.getComponent(3).setEnabled(false);
							axismenu.getComponent(4).setEnabled(false);
							axismenu.getComponent(5).setEnabled(false);
							axismenu.getComponent(6).setEnabled(true);
						}
						else
						{
							axismenu.getComponent(3).setEnabled(true);
							axismenu.getComponent(4).setEnabled(true);
							axismenu.getComponent(5).setEnabled(true);
							axismenu.getComponent(6).setEnabled(false);
						}
						axismenu.show(pcanvas.getCanvas(), e.getX()+8, e.getY()+8);
					}
				}
				else
				{ // picked nothing --> deselect
					editor.deselectAll();
				}
				editor.updatescreen();
			}
		};
		
		// node picker
		MouseAdapter npick = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				Node s = getFinalPick(e,PickResult.PRIMITIVE);
				if( s != null && !(model.nodes.get(mview.getNodeName(s)) instanceof StructureModel.ScaleNode) )
				{
					String name = mview.getNodeName(s);
					editor.selectNode(name);
					if( e.getButton() == MouseEvent.BUTTON3  )
					{
						nodelabel.setText(name+": "+model.nodes.get(name).getBasePosition());
						nodemenu.show(pcanvas.getCanvas(),e.getX()+8,e.getY()+8);
					}
				}
				else
				{ // picked nothing --> deselect
					editor.deselectAll();
				}
				editor.updatescreen();
			}
		};
		
		// member picker
		MouseAdapter mpick = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				Node n = null;
				if( !e.isShiftDown() ) // regular click --> select members
				{
					n = getFinalPick(e,PickResult.SHAPE3D);
					if( n != null & mview.membermap.containsValue(n) )
					{
						int member = mview.getMemberRef(n);
						editor.selectMember(member);
						if( e.getButton() == MouseEvent.BUTTON3 )
						{
							memberlabel.setText(model.members.get(member).nodes[0]+" to "+model.members.get(member).nodes[1]);
							membermenu.show(pcanvas.getCanvas(),e.getX()+8,e.getY()+8);
						}
					}
				}
				else // shift click --> select nodes
				{
					n = getFinalPick(e,PickResult.PRIMITIVE);
					if( n!=null && !(model.nodes.get(mview.getNodeName(n)) instanceof StructureModel.ScaleNode) )
					{
						String name = mview.getNodeName(n);
						editor.selectNode(name);
						if( e.getButton() == MouseEvent.BUTTON3 )
						{
							nconnlabel.setText(name+": "+model.nodes.get(name).getBasePosition());
							nconnmenu.show(pcanvas.getCanvas(),e.getX()+8,e.getY()+8);
						}
					}
				}
				if( n == null ) // didn't get any valid picks
				{ editor.deselectAll();	}// deselect all
				editor.updatescreen();
			}
		};
		
		// scale node picker
		MouseAdapter snpick = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				Node s = getFinalPick(e,PickResult.PRIMITIVE);
				if( s != null && (model.nodes.get(mview.getNodeName(s)) instanceof StructureModel.ScaleNode))
				{
					String name = mview.getNodeName(s);
					editor.selectNode(name);
					if( e.getButton() == 3 )
					{
						nodelabel.setText(name+": "+model.nodes.get(name).getBasePosition());
						nodemenu.show(pcanvas.getCanvas(),e.getX()+8,e.getY()+8);
					}
				}
				else
				{ // picked nothing --> deselect
					editor.deselectAll();
				}
				editor.updatescreen();
			}
		};
		
		// axis menu listener
		MouseAdapter amlisten = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				editor.selectedaxis = axismenu.getComponentIndex((Component)e.getSource());
				editor.updatescreen();
			}
		};
		
		// node menu listener
		MouseAdapter nmlisten = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				model.removeNode(editor.selectednode);
				mview.sync();
				mview.viewsync();
				editor.updatescreen();
			}
		};
		
		// member menu listener
		MouseAdapter mmlisten = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				model.removeMember(editor.selectedmember);
				mview.sync();
				editor.updatescreen();
			}
		};
		
		// node connection menu listener
		MouseAdapter ncmlisten = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{ 
				editor.setMemberCreationNode(nconnmenu.getComponentIndex( (Component)e.getSource() ), editor.selectednode);
				editor.updatescreen();
			}
		};
		
		/**
		 * Creates a new <code>MousePicker</code> for controlling picking on <code>pc</code>
		 * @param pc  the <code>PickCanvas</code> that this <code>MousePicker</code> manages picking for.
		 */
		public MousePicker(PickCanvas pc)
		{
			this.pcanvas = pc;
			this.pcanvas.setMode(PickTool.GEOMETRY);
			pcanvas.setTolerance(6.0f); // 6 pixel tolerance
			axismenu = new JPopupMenu();
			axismenu.setLightWeightPopupEnabled(false);
			axismenu.add("X-Axis");
			axismenu.add("Y-Axis");
			axismenu.add("Z-Axis");
			axismenu.add("X Rotation Axis");
			axismenu.add("Y Rotation Axis");
			axismenu.add("Z Rotation Axis");
			axismenu.add("Stress/Strain");
			axismenu.getComponent(6).setEnabled(false);
			for (int i= 0; i < axismenu.getComponentCount(); i++)
			{
				axismenu.getComponent(i).addMouseListener(amlisten);
			}
			
			nodelabel = new JLabel("<nodename> <pos>");
			nodelabel.setFont(new Font("Sans Serif",Font.BOLD,14));
			nodemenu = new JPopupMenu();
			nodemenu.setLightWeightPopupEnabled(false);
			nodemenu.add(nodelabel);
			nodemenu.add("Remove");
			for (int i= 1; i < nodemenu.getComponentCount(); i++)
			{
				nodemenu.getComponent(i).addMouseListener(nmlisten);
			}
			
			memberlabel = new JLabel("<A to B>");
			memberlabel.setFont(new Font("Sans Serif",Font.BOLD,14));
			membermenu = new JPopupMenu();
			membermenu.setLightWeightPopupEnabled(false);
			membermenu.add(memberlabel);
			membermenu.add("Remove");
			for (int i= 1; i < membermenu.getComponentCount(); i++)
			{
				membermenu.getComponent(i).addMouseListener(mmlisten);
			}
			
			nconnlabel = new JLabel("<nodename> <pos>");
			nconnlabel.setFont(new Font("Sans Serif",Font.BOLD,14));
			nconnmenu = new JPopupMenu();
			nconnmenu.setLightWeightPopupEnabled(false);
			nconnmenu.add(nconnlabel);
			nconnmenu.add("Copy to Node 1");
			nconnmenu.add("Copy to Node 2");
			for (int i = 1; i < nconnmenu.getComponentCount(); i++)
			{
				nconnmenu.getComponent(i).addMouseListener(ncmlisten);
			}
		}
		
		/**
		 * Update the picking mode based on the new mode
		 * @param mode  the new mode that picking should be handled for, should be one of <code>ModelEditor.AXIS, .NODE, .MEMBER, .SNODE</code>
		 */
		public void updatePickMode(int mode)
		{
			pcanvas.getCanvas().removeMouseListener(currentpicker);
			switch( mode )
			{
				case 0: // editor.AXIS
					currentpicker = napick;
					break;
				case 1: // editor.NODE
					currentpicker = npick;
					break;
				case 2: // editor.MEMBER:
					currentpicker = mpick;
					break;
				case 3: // editor.SNODE:
					currentpicker = snpick;
					break;
			}
			pcanvas.getCanvas().addMouseListener(currentpicker);			
		}
		
		// dedude the result of picking based on the type of Node requested and the mouse location
		// from the pickAllSorted
		private Node getFinalPick(MouseEvent e, int type )
		{
			pcanvas.setShapeLocation(e);
			PickResult[] picks = pcanvas.pickAllSorted();
			Node p = null;
			if( picks != null )
			{
				int index = 1;
				p = picks[0].getNode(type);
				while(p == null && index < picks.length )
				{  // find the closest Node of requested type
					p = picks[index].getNode(type);
					index ++;
				}
			}
			return p;
		}
	}
	
	/**
	 * This class manages the editing of live models in the panel.
	 */
	class ModelEditor implements ActionListener
	{
		/* Currently selected node index, <code>-1</code>
		 * if no node is currently selected, otherwise
		 * between -1 and totalnodes.
		 */
		String selectednode = "";
		int selectedmember = 0;
		
		/* Currently selected axis, should be one of
		 * <code>ModelPanel.X_AXIS</code>,
		 * <code>ModelPanel.Y_AXIS</code>,
		 * <code>ModelPanel.Z_AXIS</code>,
		 * <code>ModelPanel.X_ROT</code>,
		 * <code>ModelPanel.Y_ROT</code>,
		 * <code>ModelPanel.Z_ROT</code>,
		 * <code>ModelPanel.STR</code>
		 * or -1 if none selected
		 */
		int selectedaxis = -1;
		
		// mode indices
		public static final int AXIS = 0,
								NODE = 1,
								MEMBER = 2,
								SNODE = 3;
		private int mode = AXIS;
		private JPanel axispane, nodepane, memberpane, snodepane;
		private JButton addnode, addmember, addscalenode;
		private JButton next_node		= new JButton(">");
		private JButton prev_node		= new JButton("<");
		private JButton next_axis		= new JButton(">");
		private JButton prev_axis		= new JButton("<");
		private JButton unlink      = new JButton("Unlink");
		
		private JTextField 	nodename, xpos, ypos,zpos,
							snname, snxpos, snypos, snzpos,
							snlowerval, snmiddleval, snupperval;
		private JComboBox	node1, node2, type, nodecolor,
							membercolor, 
							snlowercolor, snmiddlecolor, snuppercolor, snfailcolor;
		private JLabel p1,p2;
		private JLabel	apnodename		= new JLabel(" - ");
		private JLabel	apaxisname		= new JLabel(" - ");
		private JLabel	apchanname		= new JLabel("--");
		private Vector<String> basecolors;
		
		public ModelEditor()
		{
			GridBagLayout grid = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			basecolors = new Vector<String>();
			basecolors.add("blue");
			basecolors.add("green");
			basecolors.add("red");
			basecolors.add("yellow");
			basecolors.add("cyan");
			basecolors.add("pink");
			basecolors.add("magenta");
			basecolors.add("white");
			basecolors.add("gray");
			
			// axispane
			JLabel	axislabel = new JLabel("Axis: ");
			JLabel	nodelabel = new JLabel("Node: ");
			next_node.setFocusPainted(false);
			prev_node.setFocusPainted(false);
			next_axis.setFocusPainted(false);
			prev_axis.setFocusPainted(false);
			unlink.setFocusPainted(false);
			JLabel spacer = new JLabel();
			
			axispane = new JPanel(grid);
			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 0;
			c.gridy = 0;
			c.insets = new Insets(2,2,2,2);
			c.gridwidth = 8;
			c.fill = GridBagConstraints.BOTH;
			c.gridy = 1;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			axispane.add( prev_node, c );
			c.gridx = 1;
			axispane.add( next_node, c );
			c.gridx = 2;
			axispane.add( nodelabel, c );
			c.gridx = 3;
			axispane.add( apnodename, c );
			c.gridx = 4;
			c.weightx = 1.0;
			axispane.add(spacer,c);
			c.gridx = 5;
			c.weightx = 0;
			// second row
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 1;
			axispane.add( prev_axis, c );
			c.gridx = 1;
			axispane.add( next_axis, c );
			c.gridx = 2;
			axispane.add( axislabel, c );
			c.gridx = 3;
			axispane.add( apaxisname, c );
			c.gridx = 4;
			c.weightx = 1.0;
			axispane.add( apchanname, c );
			c.weightx = 0;
			c.gridx = 5;
			axispane.add(unlink,c);
			prev_axis.addActionListener(this);
			next_axis.addActionListener(this);
			prev_node.addActionListener(this);
			next_node.addActionListener(this);
			unlink.addActionListener(this);
		
			// nodepane fields
			nodepane = new JPanel(grid);
			addnode	= new JButton(" Add ");
			addnode.addActionListener(this);
			JLabel	namelabel	=	new JLabel("Name:");
			JLabel	positionlabel	=	new JLabel("Position: (x,y,z)");
			JLabel	nodecolorlabel	=	new JLabel("Color:");
			nodename	= new JTextField(10); nodename.setEditable(false); nodename.setText(findNext("node"));
			xpos	= new JTextField(6); xpos.setText("0");
			ypos	= new JTextField(6); ypos.setText("0");
			zpos	= new JTextField(6); zpos.setText("0");
			nodecolor = new JComboBox(basecolors); nodecolor.setLightWeightPopupEnabled(false);
			
			// layout of nodepane
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.LINE_START;
			c.gridy = 0;
			c.gridx = 0;
			// first row
			nodepane.add(namelabel,c);
			c.gridx = 1;
			c.gridwidth = 3;
			nodepane.add(positionlabel,c);
			c.gridx = 4;
			c.gridwidth = 1;
			nodepane.add(nodecolorlabel,c);
			// second row
			c.gridy = 1;
			c.gridx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.3;
			nodepane.add(nodename,c);
			c.gridx = 1;
			c.weightx = 0.3;
			nodepane.add(xpos,c);
			c.gridx = 2;
			nodepane.add(ypos,c);
			c.gridx = 3;
			nodepane.add(zpos,c);
			c.gridx = 4;
			c.weightx = 0.3;
			nodepane.add(nodecolor,c);
			c.gridy = 0;
			c.gridx = 5;
			c.weightx = 0.2;
			c.gridheight = 2;
			c.fill = GridBagConstraints.BOTH;
			nodepane.add(addnode,c);
			
			
			// memberpane fields
			memberpane	= new JPanel(grid);
			addmember	= new JButton(" Add ");
			JLabel nodetitle	= new JLabel("Nodes:");
			JLabel n1	= new JLabel("1:");
			JLabel n2	= new JLabel("2:");
			JLabel pos	= new JLabel("Position: (x,y,z)");
			p1   = new JLabel("(-,-,-)");
			p2   = new JLabel("(-,-,-)");
			JLabel typecolorlabel	= new JLabel("Type/Color:");
			node1		= new JComboBox();
			node1.addActionListener(this); node1.setLightWeightPopupEnabled(false);
			node2		= new JComboBox();
			node2.addActionListener(this); node2.setLightWeightPopupEnabled(false);
			String[] curves = {"Linear","Cubic"};
			type		= new JComboBox(curves); type.setLightWeightPopupEnabled(false);
			membercolor = new JComboBox(basecolors); 
			membercolor.setSelectedIndex(1); membercolor.setLightWeightPopupEnabled(false);
			
			c.weightx = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.NONE;
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 2;
			memberpane.add(nodetitle,c);
			c.gridwidth = 1;
			c.gridx = 2;
			memberpane.add(pos,c);
			c.gridx = 3;
			memberpane.add(typecolorlabel,c);
			c.gridy = 1;
			c.gridx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			memberpane.add(n1,c);
			c.gridx = 1;
			memberpane.add(node1,c);
			c.gridx = 2;
			memberpane.add(p1,c);
			c.gridx = 3;
			memberpane.add(type,c);
			c.gridy = 2;
			c.gridx = 0;
			memberpane.add(n2,c);
			c.gridx = 1;
			c.weightx = 0.5;
			memberpane.add(node2,c);
			c.gridx = 2;
			memberpane.add(p2,c);
			c.gridx = 3;
			memberpane.add(membercolor,c);
			c.gridx = 4;
			c.gridy = 0;
			c.gridheight = 3;
			c.weightx = 0.2;
			c.fill = GridBagConstraints.BOTH;
			memberpane.add(addmember,c);
			addmember.addActionListener(this);
			
			// snodepane setup
			snodepane = new JPanel(grid);
			addscalenode	= new JButton(" Add ");
			addscalenode.addActionListener(this);
			JLabel snnamelabel		= new JLabel("Name: ");
			JLabel snpositionlabel	= new JLabel("Position: (x,y,z)");
			JLabel sncolorlabel		= new JLabel("Colors: (Lo,Mid,Hi,Out)");
			JLabel snvaluelabel		= new JLabel("Values: (Lo,Mid,Hi)");
			
			snname		= new JTextField(10); snname.setText(findNext("snode")); snname.setEditable(false);
			snxpos		= new JTextField(6);
			snypos		= new JTextField(6);
			snzpos		= new JTextField(6);
			snupperval	= new JTextField(6);
			snmiddleval	= new JTextField(6);
			snlowerval	= new JTextField(6);
			snlowercolor= new JComboBox(basecolors); snlowercolor.setSelectedIndex(1); snlowercolor.setLightWeightPopupEnabled(false);
			snmiddlecolor= new JComboBox(basecolors); snmiddlecolor.setSelectedIndex(7); snmiddlecolor.setLightWeightPopupEnabled(false);
			snuppercolor= new JComboBox(basecolors); snuppercolor.setLightWeightPopupEnabled(false);
			snfailcolor	= new JComboBox(basecolors); snfailcolor.setSelectedIndex(2); snfailcolor.setLightWeightPopupEnabled(false);
			
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.LINE_END;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 0;
			snodepane.add(snnamelabel,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridwidth = 2;
			snodepane.add(snname,c);
			c.gridy = 1;
			c.gridx = 0;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			snodepane.add(snpositionlabel,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			snodepane.add(snxpos,c);
			c.gridx = 2;
			snodepane.add(snypos,c);
			c.gridx = 3;
			snodepane.add(snzpos,c);
			c.gridy = 2;
			c.gridx = 0;
			c.fill = GridBagConstraints.NONE;
			snodepane.add(snvaluelabel,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			snodepane.add(snlowerval,c);
			c.gridx = 2;
			snodepane.add(snmiddleval,c);
			c.gridx = 3;
			snodepane.add(snupperval,c);
			c.gridy = 3;
			c.gridx = 0;
			c.fill = GridBagConstraints.NONE;
			snodepane.add(sncolorlabel,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.1;
			c.gridx = 1;
			snodepane.add(snlowercolor,c);
			c.gridx = 2;
			snodepane.add(snmiddlecolor,c);
			c.gridx = 3;
			snodepane.add(snuppercolor,c);
			c.gridx = 4;
			snodepane.add(snfailcolor,c);
			c.weightx = 0;
			c.fill = GridBagConstraints.BOTH;
			c.gridy = 0;
			c.weighty = 0.2;
			c.gridheight = 3;
			snodepane.add(addscalenode,c);
		}
		
		/**
		 * Gets the <code>JPanel</code> for axis linking
		 * @return  a <code>JPanel</code> for axis linking
		 */
		public JPanel getAxisPanel()
		{
			setMode(AXIS);
			editor.updatescreen();
			return axispane;
		}
    
    /**
     * Gets the <code>JPanel</code> for node editing
     * @return  a <code>JPanel</code> for node editing
     */
		public JPanel getNodePanel()
		{
			setMode(NODE);
			nodename.setText(findNext("node"));
			return nodepane;
		}
		
    /**
     * Gets the <code>JPanel</code> for member editing
     * @return  a <code>JPanel</code> for member editing
     */
		public JPanel getMemberPanel()
		{
			setMode(MEMBER);
			Vector<String> nodes = new Vector<String>(mpanel.model.nodes.keySet());
			Iterator<String> iter = nodes.iterator();
			while( iter.hasNext() )
			{
				if( mpanel.model.nodes.get(iter.next()) instanceof StructureModel.ScaleNode )
				{	iter.remove();	} // remove scale nodes
			}
			node1.setModel(new DefaultComboBoxModel(nodes));
			node2.setModel(new DefaultComboBoxModel(nodes));
			// fill in position labels
			if( !model.nodes.isEmpty())
			{
				setMemberCreationNode(1,node1.getSelectedItem().toString());
				setMemberCreationNode(2,node2.getSelectedItem().toString());
			}
			return memberpane;
		}
		
    /**
     * Gets the <code>JPanel</code> for stress/strain node editing
     * @return  a <code>JPanel</code> for stress/strain node editing
     */
		public JPanel getScaleNodePanel()
		{
			setMode(SNODE);
			snname.setText(findNext("snode"));
			return snodepane;
		}
		
		/**
		 * Selects a node in the model for actions
		 * @param name  the name of the node to select
		 */
		public void selectNode( String name )
		{
			selectedmember = 0;
			selectednode = name;
		}
		
		/**
		 * Selects a member in the model for actions
		 * @param member  the reference of the member to select
		 */
		public void selectMember( int member )
		{
			selectednode = "";
			selectedaxis = -1;
			selectedmember = member;
		}
		
		/**
		 * Deselects any selected nodes, members or axes in the model.
		 */
		public void deselectAll()
		{
		  selectednode = "";
		  selectedaxis = -1;
		  selectedmember = 0;
		}
		
		/**
		 * Sets the editing mode.
		 * @param mode  Should be one of <code>ModelEditor.AXIS, .NODE, .MEMBER, .SNODE</code>.
		 */
		public void setMode(int mode)
		{
			
			if ( this.mode == MEMBER || mode == MEMBER )
			{ // if we are leaving or entering MEMBER mode:
				Iterator<Shape3D> iter = mview.membermap.values().iterator();
				while( iter.hasNext() )
				{
					Shape3D n = iter.next();
					n.setPickable((mode == MEMBER)); // set members pickable/not  
				}
			}
			mpicker.updatePickMode(mode);
			deselectAll(); // deselect everything
			mview.sync();   // update model for highlighting
			updatescreen(); // update text in panels
			if( (this.mode == MEMBER || this.mode == NODE || this.mode == SNODE) && mode == AXIS)
			{
			  if( model.nodeCount() > 0 )
			  {
  			  // prompt to save on leaving edit mode, IFF there is a model
  			  Object[] options  = {"Save","Save with channels","No"};
  			  int n = JOptionPane.showOptionDialog(
  			      dataComponent,
  			      "Would you like to save the edited model?",
  			      "Save Model?",
  			      JOptionPane.DEFAULT_OPTION, // option type
  			      JOptionPane.QUESTION_MESSAGE,
  			      null, // icon
  			      options,
  			      options[2]);
  			  if(n == 0)
  			  {
  			    saveModel();
  			  }
  			  else if( n == 1)
  			  {
  			    saveModel(true);
  			  }
			  }
			}
			this.mode = mode;
		}
		
		/**
		 * Returns the current editing mode.
		 * @return  an <code>int</code> representing the current editing mode.
		 */
		public int getMode()
		{
			return mode;
		}
		
		/**
		 * Sets the member editing panel's node selection box to the specified node.
		 * @param which which member editing panel node field to select in. Either 1 or 2.
		 * @param name  the name of the node to set as selected
		 */
		public void setMemberCreationNode(int which, String name)
		{
			if ( which == 1 )
			{
				node1.setSelectedItem(name);
				p1.setText(((StructureModel.SNode)model.nodes.get(name)).getBasePosition().toString());
			}
			else if ( which == 2 )
			{
				node2.setSelectedItem(name);
				p2.setText(((StructureModel.SNode)model.nodes.get(name)).getBasePosition().toString());
			}
		}
		
		/**
		 * Sets the axis linking panel's channel name
		 * @param chan  the name of the channel to set
		 */
		public void setChanName( String chan )
		{
			apchanname.setText(chan);
		}
		
		/**
		 * Updates the editing panel with most current selections/actions.
		 */
		public void updatescreen()
		{
			if(selectednode == "")
			{
				apnodename.setText(" - ");
			}
			else
			{
				apnodename.setText(selectednode);
			}
			if(selectedaxis == STR)
			{
				// check that we have a ScaleNode selected
				if(!(model.nodes.get(selectednode) instanceof StructureModel.ScaleNode))
				{	
					selectedaxis = -1;
				}
			}
			switch (selectedaxis)
			{
				case -1:
					apaxisname.setText(" - ");
					break;
				case 0:
					apaxisname.setText("X: ");
					break;
				case 1:
					apaxisname.setText("Y: ");
					break;
				case 2:
					apaxisname.setText("Z: ");
					break;
				case 3:
					apaxisname.setText("X rotation: ");
					break;
				case 4:
					apaxisname.setText("Y rotation: ");
					break;
				case 5:
					apaxisname.setText("Z rotation: ");
					break;
				case 6:
					apaxisname.setText("Strs/Strn: ");
					break;
				default:
					apaxisname.setText(" - ");
					break;
			}
			mview.highlightNode(selectednode);
			mview.highlightMember(selectedmember);
			apchanname.setText(mpanel.getChannel(selectednode,selectedaxis));
		}
		
		// find the next unused numbered name of the base name in the model
		private String findNext(String name)
		{
			int count = 1;
			while( model.containsNode(name+count) )
			{
				count++;
			}
			return name+count;
		}
		
		// override for implementing ActionListener
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if( source == addnode )
			{
					try
					{
				String name = nodename.getText();
				Point3d point = loader.buildPoint(xpos.getText()+","+ypos.getText()+","+zpos.getText());
				Color3f color = loader.parseColor((String)nodecolor.getSelectedItem());
				mpanel.model.addNode(name,point,color);
				mview.sync();
				mview.viewsync();
				//mview.resetScene(); // back to front view
				mview.update();
				nodename.setText(findNext("node"));
					}
					catch(StructureModel.ModelException ex)
					{
						popupError(ex.errormsg);
					}
					catch( PointFormatException ex)
					{
						popupError(ex.errorline);
					}
			}
			else if( source == addmember )
			{
					try
					{
				mpanel.model.defineMember((String)node1.getSelectedItem(),(String)node2.getSelectedItem(),(Integer)type.getSelectedIndex(),loader.parseColor((String)membercolor.getSelectedItem()));
				mview.sync();
				mview.update();
					}
					catch (StructureModel.ModelException ex)
					{
						popupError(ex.errormsg);
					}
			}
			else if( source == addscalenode )
			{
					try
					{
				String name = snname.getText();
				Point3d point = loader.buildPoint(snxpos.getText()+","+snypos.getText()+","+snzpos.getText());
				Color3f lowc = loader.parseColor((String)snlowercolor.getSelectedItem());
				Color3f midc = loader.parseColor((String)snmiddlecolor.getSelectedItem());
				Color3f highc = loader.parseColor((String)snuppercolor.getSelectedItem());
				Color3f failc = loader.parseColor((String)snfailcolor.getSelectedItem());
				float low = Float.parseFloat(snlowerval.getText());
				float mid = Float.parseFloat(snmiddleval.getText());
				float high = Float.parseFloat(snupperval.getText());
				mpanel.model.addScaleNode(name, point, low, mid, high, lowc, midc, highc, failc);
				mview.sync();
				mview.viewsync();
				mview.update();
				snname.setText(findNext("snode"));
					}
					catch(StructureModel.ModelException ex)
					{
						popupError(ex.errormsg);
					}
					catch( PointFormatException ex)
					{
						popupError(ex.errorline);
					}
					catch(NumberFormatException ex)
					{
						popupError("Invalid threshold value.");
					}
			}
			else if( source == node1 )
			{
				StructureModel.SNode s = model.nodes.get(node1.getSelectedItem().toString());
				Point3d p = s.getBasePosition();
				p1.setText(p.toString());
			}
			else if( source == node2 )
			{
				StructureModel.SNode s = model.nodes.get(node2.getSelectedItem().toString());
				Point3d p = s.getBasePosition();
				p2.setText(p.toString());
			}
			
			// and then linking buttons
			else if( model.nodeCount() > 0 )
			{
				if( source == next_node)
				{
					if( selectednode == "" )
					{
						selectednode = (String)model.nodes.keySet().toArray()[0];
					}
					else
					{
						// select next, loop to beginning
						Object[] nodearray = model.nodes.keySet().toArray();
						boolean found = false;
						int index;
						for( index = 0; index < nodearray.length && !found; index++ )
						{
							if( nodearray[index] == selectednode)
							{	found = true;	}
						}
						if(found)
						{
							// leavign for loop advances one further, now ensure wrap
							index = (index)% nodearray.length;
							selectednode = (String)nodearray[index];
							if( selectedaxis == STR ) // check if we have Strs/Strn axis selected
							{   // if so check if we are viewing a ScaleNode
								if(!(model.nodes.get(selectednode) instanceof StructureModel.ScaleNode) )
								{ // if not, deselect axis
									selectedaxis = -1;
								}
							}
							if ( model.nodes.get(selectednode) instanceof StructureModel.ScaleNode )
							{ // if we select a ScaleNode, make sure rotation axes are not selected
								if (selectedaxis > 2 && selectedaxis < 6 )
								{	selectedaxis = -1;	}
							}
						}
						else
						{
							selectednode = "";
							selectedaxis = -1;
						}
					}	
				}
				else if( source == prev_node )
				{
					if(selectednode == "")
					{
						selectednode = (String)model.nodes.keySet().toArray()[model.nodes.keySet().size()-1];
					}
					else
					{
						Object[] nodearray = model.nodes.keySet().toArray();
						boolean found = false;
						int index;
						for( index = 0; index < nodearray.length && !found; index++ )
						{
							if( nodearray[index] == selectednode)
							{	found = true;	}
						}
						if(found)
						{
							index = (index+nodearray.length-2)%nodearray.length;
							selectednode = (String) nodearray[index];
							if( selectedaxis == STR ) // check if we have Strs/Strn axis selected
							{   // if so check if we are viewing a ScaleNode
								if(!(model.nodes.get(selectednode) instanceof StructureModel.ScaleNode) )
								{ // if not, deselect axis
									selectedaxis = -1;
								}
							}
							if ( model.nodes.get(selectednode) instanceof StructureModel.ScaleNode )
							{ // if we select a ScaleNode, make sure rotation axes are not selected
								if (selectedaxis > Z_AXIS && selectedaxis < STR )
								{	selectedaxis = -1;	}
							}
						}
						else
						{
							selectednode = "";
							selectedaxis = -1;
						}
					}
				}
				else if( source == next_axis)
				{
					if(selectednode != "")
					{
						if(model.nodes.get(selectednode) instanceof StructureModel.ScaleNode)
						{
							do
							{	selectedaxis = (selectedaxis+1) % 7; }
							while (selectedaxis > 2 && selectedaxis < 6);
						}
						else
						{
							selectedaxis = (selectedaxis+1) % 6;
						}
					}
				}
				else if ( source == prev_axis)
				{
					if(selectednode != "")
					{
						if(model.nodes.get(selectednode) instanceof StructureModel.ScaleNode)
						{ 
							do
							{// wrap 6->2->1->0->6
								selectedaxis = (selectedaxis+6) % 7;	
							}while (selectedaxis >2 && selectedaxis < 6);
						}
						else
						{	// wrap 5-->0
							selectedaxis = (selectedaxis+5) % 6;
						}
					}
				}
				else if( source == unlink )
				{
				  String channel = mpanel.getChannel(selectednode, selectedaxis);
				  mpanel.unlink(selectednode, selectedaxis);
				  mpanel.checkLinks(channel);
				}
				updatescreen();
			}
		}
	}
	
	// helper class for realtime data mapping
	/**
	 * Helper class for channel subscriptions.
	 */
	class NodeAxisPair
	{
		String nodename;
		int axis;
		NodeAxisPair(String n, int a)
		{
			nodename = n;
			axis = a;
		}
	}
	/**
	 *  Helper class for exception handling
	 */
	class PointFormatException extends Exception
	{
		private String errorline;
		PointFormatException(String errorline)
		{
			this.errorline = errorline;
		}
	}
}
