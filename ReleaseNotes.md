## RDV 1.9 - _July 18, 2008_ ##

This release contains significant new features for viewing images and video, a
new dial visualization, and support for exporting MATLAB files. A number of
smaller improvements for plotting have been added in addition to a few bug
fixes.

### New features ###

  * Support for zooming and panning of an image. A clickable navigation image can be used to jump around parts of an image when zoomed in.
  * Filmstrip interface to view images sequentially.
  * Support for viewing a [thumbnail of an image](ThumbnailImages.md) to conserve bandwidth.
  * DialViz, a visualization extension to view a numeric data channel on a dial.
  * Export of data to the MATLAB MAT-file format.
  * Support for [port knocking authentication](PortKnockingAuthentication.md).

### Improvements ###

  * Cursors on an XY plot to indicate the current data point in a series.
  * Zooming out of a plot via the mouse will remember previous zoom levels.
  * Support for PNG and GIF images.
  * Ability to hide the legend in a plot.

### Bug fixes ###

  * Can't connect to RBNB server with circular references.
  * Children of shortcut servers aren't shown in channel list.
  * Auto adjusting the domain axis in a time series plot does not work correctly.

### Changes ###

  * Plots set to auto range, don't always include zero anymore.
  * The system look & feel is used now, so RDV should look more native to the platform it is running on.
  * All RDV class were moved to the "org.rdv" package.

## RDV 1.8 - _January 23, 2008_ ##

### New features ###

  * Import [OpenSees](http://opensees.berkeley.edu/) XML data files into RDV.
  * Import video into RDV from files residing on a local machine or in NEEScentral that have been stored in a zip file.

### Bug fixes ###
  * Disable the export menu if no source/channels are selected.

## RDV 1.7 - _September 14, 2007_ ##

### New features ###
  * Export video as a series of time stamped JPEG files.
  * Play MP3 audio from a streaming server.

### Bug fixes ###
  * Fix a bug when saving closed floating windows in configuration.