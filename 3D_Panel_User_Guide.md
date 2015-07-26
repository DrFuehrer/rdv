# 3D Model Panel User's Guide #
The following is a look at how to use the‭ ‬3D Model Panel as a part of the RDV program.‭ ‬It covers the usage of all the different modes that the 3D Model Panel is currently capable of as well as information of how to use and create model definition files. The 3D Model Panel uses Java3D for rendering, which is downloaded as part of the Web Start package.

## Starting 3D Model Panel and the Main Interface ##
Select "Add 3D Model Panel" from the "Window" menu in RDV to create a new 3D Model Panel within RDV.

![http://www.nees.lehigh.edu/wiki/images/3/3b/Add3DPanel.png](http://www.nees.lehigh.edu/wiki/images/3/3b/Add3DPanel.png)

You will be presented with the new 3D Model Panel layout. The center black pane is the 3D viewing area where the model will be drawn. Along the top is a menu for accessing the model file functions, view controls, and the new edit modes. Along the bottom are the controls for the current mode. In the bottom right corner are a set of buttons for controlling some aspects of the view.

![http://www.nees.lehigh.edu/wiki/images/5/58/3DPanelOverview.png](http://www.nees.lehigh.edu/wiki/images/5/58/3DPanelOverview.png)

## Menus and Onscreen Controls ##
### Model Menu ###
The "Model" menu will allow you to manage previously defined model definition files. To load a previously defined model file from disk, select the "Open Model" option from the "Model" menu and select the file in the dialog. You will then see the model loaded onscreen in the 3D Viewing area. If this model file also contains information about channel links, you can load them (if connected to the appropriate server or working offline with the appropriate archive file) by selecting the "Load Channels" option from the "Model" menu. This will create the links defined by the model file and will then be ready for playback or real time streaming data.

![http://www.nees.lehigh.edu/wiki/images/8/84/Modelmenu.png](http://www.nees.lehigh.edu/wiki/images/8/84/Modelmenu.png)

The second part of the "Model" menu will let you save the current model back to file. A model can be saved with or without the channels currently linked to it, if no channels are currently linked, both options will have the same result. Saving a model with channels will allow you to use the "Load Channels" option in the "Model" menu the next time you use the model.

### View Menu ###
The "View" menu provides several convenient options for controlling the model in the viewing area. The first section of the "View" menu is a selection of preset views. The first is "Front View" which is the default view of the model you will see when loading a model from file (or building it from within RDV, as seen later in the "Edit" section). The "Front View" of the model tries to give a good overview of the model from the front end, and so may tilt the model towards the camera if it is very deep (extends far away from the camera) instead of viewing the model directly from the front in all cases. The "Top View" will show the model from a birds eye view and the "Corner View" shows a slightly isometric view of the model from the front-right corner. Should the model ever become "lost" and not be visible onscreen due to mouse control or other reasons, these preset options should bring the model back into view.

![http://www.nees.lehigh.edu/wiki/images/3/36/Viewmenu.png](http://www.nees.lehigh.edu/wiki/images/3/36/Viewmenu.png)

The "View" menu also provides a way to deselect any selected element of the model, which can also be accomplished by clicking on a blank area in the viewing area.
The final section is a set of view controls. You will also notice that these menu entries have a keystroke listed next to them in the menu, which are the hot-key shortcuts for each options. These shortcut keys will allow you to quickly control the view of the model from the keyboard without needing to access the menu. These controls are also available in the bottom right corner of the 3D Model Panel as buttons.

### Edit Menu ###
![http://www.nees.lehigh.edu/wiki/images/a/a7/Editmenu.png](http://www.nees.lehigh.edu/wiki/images/a/a7/Editmenu.png)

The "Edit" menu allows you to edit or create new models from within RDV without the need to edit the model file directly. This can be used to create completely new models or edit existing models. To begin editing, select one of the "Edit ..." options from the "Edit" menu. This will change the bottom of the 3D Model Panel interface to the editing mode for the selected type of element. These are detailed individually below. Once you are done editing, select "Leave Edit Mode" from the "Edit" menu and you will be prompted to save your edited model. See [[Modes](#Editing.md)]

### View Controls ###
The bottom right corner of the 3D Model Panel contains a set of six buttons for controlling the view. the "+" and "-" buttons zoom the model in and out respectively. The next two rows of buttons rotate the model about its center in increments of 15 degrees or 90 degrees either clockwise or counterclockwise. These options are also available in the "View" menu and as shortcut keys (listed in the "View" menu).

## Mouse Controls ##
By using the mouse in the viewing area, you can manipulate the view of the model. Clicking and dragging with the left mouse button will rotate the model around its center. Clicking and dragging with the right mouse button will translate the model and clicking and dragging with the middle mouse button (wheel button) will zoom the model in and out.


## Editing Modes ##
Each editing mode provides a slightly different set of features for editing the selected element type. All the editing modes will take over the lower section of the 3D Model Panel and use it for input for the creation of new elements. By filling out the fields and clicking the "Add" button, you will add a new element to the model based on the current field values. Naming is done automatically according to a simple incremental scheme that will not overwrite existing elements, there are also restrictions on duplicity of elements (elements with the same name or in the same place are disallowed). If you specify an inappropriate value for any of the fields, the program should pop up a dialog window to inform you how to correct the values. The modes in detail follow.

### Edit Channel Links ###
The default editing mode for the 3D Model Panel is channel linking. This allows you to link channels from an archive file, or a live server, to nodes which will dictate their displacement. The lower portion of the screen will show a Node: field as well as an Axis: field. The buttons near each label will cycle through the available nodes in the current model or the available axes for the selected node respectively.

![http://www.nees.lehigh.edu/wiki/images/c/c1/Axiscontrols.png](http://www.nees.lehigh.edu/wiki/images/c/c1/Axiscontrols.png)

The selected node will be highlighted (in orange) in the viewing area and if there is a channel linked to the selected axis, its name will appear next to the axis name in the lower portion of the screen. Nodes can also be selected via left-clicking in the viewing area in this mode, and their name will appear in the lower portion of the screen. Additionally, right clicking on a node in the viewing area will access a pop up menu that can be used to select the desired axis for that node directly.

![http://www.nees.lehigh.edu/wiki/images/f/f2/Axis-popupmenu.png](http://www.nees.lehigh.edu/wiki/images/f/f2/Axis-popupmenu.png)

Once a node and an axis have been selected, dragging and dropping a channel from the channel list on the left into the viewing area will link the channel with the selected node and axis. Finally, the "Unlink" button in the lower right corner will remove any channel linked to the currently selected node and axis pair. (To unlink all the channels in the model, use the buttons in the title bar to remove all the channels, which will remove all links.)

### Edit Nodes ###
![http://www.nees.lehigh.edu/wiki/images/e/ed/Nodecontrols.png](http://www.nees.lehigh.edu/wiki/images/e/ed/Nodecontrols.png)

The node editing screen provides fields for creating new nodes by filling in position information and choosing a color. By right clicking on nodes in the viewing area, you will be presented with a menu allowing the removal of nodes.

![http://www.nees.lehigh.edu/wiki/images/6/63/Node-popupmenu.png](http://www.nees.lehigh.edu/wiki/images/6/63/Node-popupmenu.png)

Removing a node will also remove any members that are connected to the node. Not all the spheres in the model viewing area may be nodes, if you cannot select a node for removal while in this mode, it is likely a stress/strain mode, and you must use that mode to select it for removal.

### Edit Members ###
![http://www.nees.lehigh.edu/wiki/images/8/83/Membercontrols.png](http://www.nees.lehigh.edu/wiki/images/8/83/Membercontrols.png)

This mode provides a panel where two nodes may be selected via pull down menu, a deformation type may be specified (cubic or linear) and a color may be chosen, then clicking "Add" will create the new member in the model. In the viewing area, click to select a member, or right click to access a popup menu to remove a member.

![http://www.nees.lehigh.edu/wiki/images/7/70/Member-popupmenu.png](http://www.nees.lehigh.edu/wiki/images/7/70/Member-popupmenu.png)

Additionally, if the "Shift" key is held down, clicking will allow the selection of nodes, and Shift-right-clicking will access a popup menu that will provide a way to select a node as the No.1 or No.2 node in the member creation panel.

![http://www.nees.lehigh.edu/wiki/images/0/06/Alt-member-popupmenu.png](http://www.nees.lehigh.edu/wiki/images/0/06/Alt-member-popupmenu.png)

By selecting different nodes in this way for node No.1 and No.2, you can select the endpoints of a new member via mouse. Then click "Add" to create the member.

### Edit Stress/Strain Nodes ###
![http://www.nees.lehigh.edu/wiki/images/0/09/Snodecontrols.png](http://www.nees.lehigh.edu/wiki/images/0/09/Snodecontrols.png)

This editing mode is very similar to the Node editing mode. The creation panel has further options for stress/strain nodes which include the threshold values (lower limit, zero reference point and upper limit, from left to right) and corresponding colors below the value fields. Once the node's stress/strain axis is linked to a channel, as the channel approaches each limit, the node will change color between the zero point reference color and the corresponding limit color. The final color will be used if the channel data exceeds the limits on either end. The mouse behaves in a similar way to the Edit Nodes mode, and will allow removal only of stress/strain nodes.

## The Model Definition File ##

Below is a detailed example model definition file as well as an explanation of how the model definition file should be created.

To use this example, perform the following:
  1. Copy and paste the example model into a text editor and save as ExampleModel.txt
  1. Copy and paste the example input into a text editor and save as Example.txt
  1. Open RDV and work offline by going to File -> Work Offline
  1. Import the Example.txt file by going to File -> Import -> Import data file
  1. Open a 3D Model Panel using Window -> Add 3D Model Panel
  1. In the 3D Model Panel, select Model -> Open model and choose the ExampleModel.txt
  1. Click Model again and choose Load Channels.  Since the Model file has channels assigned to nodes already, this will load the channels automatically
  1. Rewind the RDV's player and click Play
  * 0-4   seconds: Motion in the +X direction
  * 4-8  seconds: Motion in the +Y direction
  * 8-12 seconds: Motion in the +Z direction
  * 12-16 seconds: Motion in -X-Y-Z directions back to center
  * 16-40 seconds: Positive strain to limit, returns to 0, Negative strain to limit, returns to 0.
  * 40-48 seconds: Rotation in the ±X direction at Node4 on the 1st floor

**Example Model File**
```
# This is an example file for the 3D Model Panel model definition file
#  to be used with the 3D Model Panel in RDV
# It should explain the layout and options available in the model definition
#  syntax.
#
# Comments are lines preceded by the # symbol and are ignored by the parser
#  It is recommended to make use of them to make notes about the model file
#  as the current format is rather verbose.
#  blanks lines are also ignored by the compiler, so feel free to seperate 
#  sections for clarity


# The model file definition currently consists of four sections:
# Nodes, Members, ScaleNodes (for Stress, Strain, Load) and Options


# The Node section defines the name, position and color of a node in the
# structure, as well as any channel connections.
# Only the name and position are required, all other sections are optional,
# however they must appear in the correct order:
# Examples (in the order they must appear on a line):
#	Name: 			alphanumeric and underscore allowed
#	Position:		( 1.03,  3 ,  -5.88)
#	Displacement Channels:	[ x channel, y channel, z channel ]
#	Rotation Channels:	{ x channel, y channel, z channel }
#	Color:			Colorname or Hex color as '#FFFFFF'
#
# If a channel group is used, all three channels must be listed,
# use '--' mark an axis as having no connection. 
#	IE for only X axis displacement:
#		[ x_channel_name, -- , -- ]
# Colors supported are:
#	red,orange,yellow,green,blue,cyan,pink,magenta,white,gray,black
# or hexadecimal colors in #XXXXXX format.
# See http://www.drewfoster.com/rgbhex/rgbhex.php for a quick
# explanation of hex color if you need/want.
#	Also, note that the interface uses orange to highlight selected nodes,
#	so it may be prudent to avoid orange for other things.
# Finally, an example:

# Node Section:
#ground floor
node_gnd_1	(  2,  2, 0)
node_gnd_2	( -2,  2, 0)	blue
node_gnd_3	( -2, -2, 0)	#0000FF
node_gnd_4	(  2, -2, 0)	green

#first floor
node_1st_1	(  2,  2, 3)	[Example/X_disp, Example/Y_disp, Example/Z_disp]  magenta
node_1st_2	( -2,  2, 3)	[Example/X_disp, Example/Y_disp, Example/Z_disp]  magenta
node_1st_3	( -2, -2, 3)	[Example/X_disp, Example/Y_disp, Example/Z_disp]  magenta
node_1st_4	(  2, -2, 3)	[Example/X_disp, Example/Y_disp, Example/Z_disp] {Example/X_rot,--,--} cyan

# Each section must be ended by a line containing only ===
# which delimits the sections for the program. 
#	Don't Forget Them!
#End the nodes section
===


# The next section defines members, connections between nodes.
# The format here is:
# 	Name of first node:	alphanumeric and underscore
#	Name of second node:	(the nodes need to exist in the Nodes section)
#	Type of member:		[cubic] or nothing (defaults to linear)
#	Color:			Same as for Nodes

# The type defines how the member will be drawn. A cubic member will use
# Bezier curves to approximate how a member would deform under the
# connected nodes' motion. If no type is specified, linear is assumed,
# a straight line is drawn between the nodes at all times.

# Member Section:
# connect the stories, some cubic:
node_gnd_1 node_1st_1 green
node_gnd_2 node_1st_2 green
node_gnd_3 node_1st_3 [cubic] yellow
node_gnd_4 node_1st_4 [cubic] yellow

# connect the floor
node_1st_1 node_1st_2 blue
node_1st_2 node_1st_3 #0000FF
node_1st_3 node_1st_4 blue
node_1st_4 node_1st_1 blue

#End the members section
===

# The next section defines stress/strain/load nodes. These node change color based
# on the channel data and determined limit values. They have many options on 
# the line, as follows:
#	Name: 		Same as with Nodes
#	Position: 	Same as with Nodes
#	Value Limits:	< lower limit, middle value, upper limit >
#	Limit Color:	< lower color, middle color, upper color, fail color >
#	Stress/Strain Channel:	{ Channel Name }
#	Displacement Channels:	Same as Nodes
# These nodes will change their color gradually between the middle and upper
# or lower limit colors based on the value of the data in the Stress/Strain
# channel. If the value goes beyond either limit, they will display the 
# 'fail color'. The upper and lower limits need not be symmetric around the 
# middle value.

node_str ( 0.1, -2, 2 )	< -2.9, 0, 2.8 > <green, white, yellow, red> { Example/strain } [Example/X_disp, Example/Y_disp, Example/Z_disp]

#End the strain/load section
===

# The final section defines other options, of which only 'center' is currently
# supported. This line will change the center of the model to the new value,
# and overrides the center computed by calculating the average of the model's
# extreme nodes.
# Note that the the current viewing tools do not account for dramatic changes
# made here, so shifting the center of the model dramatically from its computed
# center may necessitate some zooming and panning in the view window.
# The line takes the form:
#
#center (0,1,1)
#
```

**Example Input File**
```
"Active channels: X_disp, Y_disp, Z_disp, strain, X_rot"					
"Channel units: in, in, in, ustrain, rad"					
Start time: 2008-01-01T00:00:00.000Z					

Time	X_disp	Y_disp	Z_disp	strain	X_rot
0	0	0	0	0	0
0.2	0.1	0	0	0	0
0.4	0.2	0	0	0	0
0.6	0.3	0	0	0	0
0.8	0.4	0	0	0	0
1	0.5	0	0	0	0
1.2	0.6	0	0	0	0
1.4	0.7	0	0	0	0
1.6	0.8	0	0	0	0
1.8	0.9	0	0	0	0
2	1	0	0	0	0
2.2	1.1	0	0	0	0
2.4	1.2	0	0	0	0
2.6	1.3	0	0	0	0
2.8	1.4	0	0	0	0
3	1.5	0	0	0	0
3.2	1.6	0	0	0	0
3.4	1.7	0	0	0	0
3.6	1.8	0	0	0	0
3.8	1.9	0	0	0	0
4	2	0	0	0	0
4.2	2	0.1	0	0	0
4.4	2	0.2	0	0	0
4.6	2	0.3	0	0	0
4.8	2	0.4	0	0	0
5	2	0.5	0	0	0
5.2	2	0.6	0	0	0
5.4	2	0.7	0	0	0
5.6	2	0.8	0	0	0
5.8	2	0.9	0	0	0
6	2	1	0	0	0
6.2	2	1.1	0	0	0
6.4	2	1.2	0	0	0
6.6	2	1.3	0	0	0
6.8	2	1.4	0	0	0
7	2	1.5	0	0	0
7.2	2	1.6	0	0	0
7.4	2	1.7	0	0	0
7.6	2	1.8	0	0	0
7.8	2	1.9	0	0	0
8	2	2	0	0	0
8.2	2	2	0.1	0	0
8.4	2	2	0.2	0	0
8.6	2	2	0.3	0	0
8.8	2	2	0.4	0	0
9	2	2	0.5	0	0
9.2	2	2	0.6	0	0
9.4	2	2	0.7	0	0
9.6	2	2	0.8	0	0
9.8	2	2	0.9	0	0
10	2	2	1	0	0
10.2	2	2	1.1	0	0
10.4	2	2	1.2	0	0
10.6	2	2	1.3	0	0
10.8	2	2	1.4	0	0
11	2	2	1.5	0	0
11.2	2	2	1.6	0	0
11.4	2	2	1.7	0	0
11.6	2	2	1.8	0	0
11.8	2	2	1.9	0	0
12	2	2	2	0	0
12.2	1.9	1.9	1.9	0	0
12.4	1.8	1.8	1.8	0	0
12.6	1.7	1.7	1.7	0	0
12.8	1.6	1.6	1.6	0	0
13	1.5	1.5	1.5	0	0
13.2	1.4	1.4	1.4	0	0
13.4	1.3	1.3	1.3	0	0
13.6	1.2	1.2	1.2	0	0
13.8	1.1	1.1	1.1	0	0
14	1	1	1	0	0
14.2	0.9	0.9	0.9	0	0
14.4	0.8	0.8	0.8	0	0
14.6	0.7	0.7	0.7	0	0
14.8	0.6	0.6	0.6	0	0
15	0.5	0.5	0.5	0	0
15.2	0.4	0.4	0.4	0	0
15.4	0.3	0.3	0.3	0	0
15.6	0.2	0.2	0.2	0	0
15.8	0.1	0.1	0.1	0	0
16	0	0	0	0	0
16.2	0	0	0	0.1	0
16.4	0	0	0	0.2	0
16.6	0	0	0	0.3	0
16.8	0	0	0	0.4	0
17	0	0	0	0.5	0
17.2	0	0	0	0.6	0
17.4	0	0	0	0.7	0
17.6	0	0	0	0.8	0
17.8	0	0	0	0.9	0
18	0	0	0	1	0
18.2	0	0	0	1.1	0
18.4	0	0	0	1.2	0
18.6	0	0	0	1.3	0
18.8	0	0	0	1.4	0
19	0	0	0	1.5	0
19.2	0	0	0	1.6	0
19.4	0	0	0	1.7	0
19.6	0	0	0	1.8	0
19.8	0	0	0	1.9	0
20	0	0	0	2	0
20.2	0	0	0	2.1	0
20.4	0	0	0	2.2	0
20.6	0	0	0	2.3	0
20.8	0	0	0	2.4	0
21	0	0	0	2.5	0
21.2	0	0	0	2.6	0
21.4	0	0	0	2.7	0
21.6	0	0	0	2.8	0
21.8	0	0	0	2.9	0
22	0	0	0	3	0
22.2	0	0	0	2.9	0
22.4	0	0	0	2.8	0
22.6	0	0	0	2.7	0
22.8	0	0	0	2.6	0
23	0	0	0	2.5	0
23.2	0	0	0	2.4	0
23.4	0	0	0	2.3	0
23.6	0	0	0	2.2	0
23.8	0	0	0	2.1	0
24	0	0	0	2	0
24.2	0	0	0	1.9	0
24.4	0	0	0	1.8	0
24.6	0	0	0	1.7	0
24.8	0	0	0	1.6	0
25	0	0	0	1.5	0
25.2	0	0	0	1.4	0
25.4	0	0	0	1.3	0
25.6	0	0	0	1.2	0
25.8	0	0	0	1.1	0
26	0	0	0	1	0
26.2	0	0	0	0.9	0
26.4	0	0	0	0.8	0
26.6	0	0	0	0.7	0
26.8	0	0	0	0.6	0
27	0	0	0	0.5	0
27.2	0	0	0	0.4	0
27.4	0	0	0	0.3	0
27.6	0	0	0	0.2	0
27.8	0	0	0	0.1	0
28	0	0	0	0	0
28.2	0	0	0	-0.1	0
28.4	0	0	0	-0.2	0
28.6	0	0	0	-0.3	0
28.8	0	0	0	-0.4	0
29	0	0	0	-0.5	0
29.2	0	0	0	-0.6	0
29.4	0	0	0	-0.7	0
29.6	0	0	0	-0.8	0
29.8	0	0	0	-0.9	0
30	0	0	0	-1	0
30.2	0	0	0	-1.1	0
30.4	0	0	0	-1.2	0
30.6	0	0	0	-1.3	0
30.8	0	0	0	-1.4	0
31	0	0	0	-1.5	0
31.2	0	0	0	-1.6	0
31.4	0	0	0	-1.7	0
31.6	0	0	0	-1.8	0
31.8	0	0	0	-1.9	0
32	0	0	0	-2	0
32.2	0	0	0	-2.1	0
32.4	0	0	0	-2.2	0
32.6	0	0	0	-2.3	0
32.8	0	0	0	-2.4	0
33	0	0	0	-2.5	0
33.2	0	0	0	-2.6	0
33.4	0	0	0	-2.7	0
33.6	0	0	0	-2.8	0
33.8	0	0	0	-2.9	0
34	0	0	0	-3	0
34.2	0	0	0	-2.9	0
34.4	0	0	0	-2.8	0
34.6	0	0	0	-2.7	0
34.8	0	0	0	-2.6	0
35	0	0	0	-2.5	0
35.2	0	0	0	-2.4	0
35.4	0	0	0	-2.3	0
35.6	0	0	0	-2.2	0
35.8	0	0	0	-2.1	0
36	0	0	0	-2	0
36.2	0	0	0	-1.9	0
36.4	0	0	0	-1.8	0
36.6	0	0	0	-1.7	0
36.8	0	0	0	-1.6	0
37	0	0	0	-1.5	0
37.2	0	0	0	-1.4	0
37.4	0	0	0	-1.3	0
37.6	0	0	0	-1.2	0
37.8	0	0	0	-1.1	0
38	0	0	0	-1	0
38.2	0	0	0	-0.9	0
38.4	0	0	0	-0.8	0
38.6	0	0	0	-0.7	0
38.8	0	0	0	-0.6	0
39	0	0	0	-0.5	0
39.2	0	0	0	-0.4	0
39.4	0	0	0	-0.3	0
39.6	0	0	0	-0.2	0
39.8	0	0	0	-0.1	0
40	0	0	0	0	0
40.2	0	0	0	0	0.2
40.4	0	0	0	0	0.4
40.6	0	0	0	0	0.6
40.8	0	0	0	0	0.8
41	0	0	0	0	1
41.2	0	0	0	0	0.8
41.4	0	0	0	0	0.6
41.6	0	0	0	0	0.4
41.8	0	0	0	0	0.2
42	0	0	0	0	0
42.2	0	0	0	0	-0.2
42.4	0	0	0	0	-0.4
42.6	0	0	0	0	-0.6
42.8	0	0	0	0	-0.8
43	0	0	0	0	-1
43.2	0	0	0	0	-0.8
43.4	0	0	0	0	-0.6
43.6	0	0	0	0	-0.4
43.8	0	0	0	0	-0.2
44	0	0	0	0	0
44.2	0	0	0	0	0.2
44.4	0	0	0	0	0.4
44.6	0	0	0	0	0.6
44.8	0	0	0	0	0.8
45	0	0	0	0	1
45.2	0	0	0	0	0.8
45.4	0	0	0	0	0.6
45.6	0	0	0	0	0.4
45.8	0	0	0	0	0.2
46	0	0	0	0	0
46.2	0	0	0	0	-0.2
46.4	0	0	0	0	-0.4
46.6	0	0	0	0	-0.6
46.8	0	0	0	0	-0.8
47	0	0	0	0	-1
47.2	0	0	0	0	-0.8
47.4	0	0	0	0	-0.6
47.6	0	0	0	0	-0.4
47.8	0	0	0	0	-0.2
48	0	0	0	0	0
```

## Special: Installing Java3D ##
The WebStart version of RDV will download the needed Java3D binaries automatically. To run the 3D Model Panel from your own compiled version, you will need the Java3D libraries installed for compilation and use:

Java3D is available from the Java 3D Project at:
[Java 3D](https://java3d.dev.java.net/)
and installation instructions can be found [here](https://java3d.dev.java.net/#Documentation).

(Java3D requires a Java Runtime Environment to be installed.)