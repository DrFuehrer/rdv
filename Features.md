RDV is a visualization environment for scientific and engineering data. Here are a few of it's key features.

  * [Support for local or remote data sources](#Local_or_Remote_Data_Sources.md)
  * [Synchronized data](#Synchronized_Data.md)
  * [Data visualization](#Data_Visualization.md)
  * [Event markers](#Event_Markers.md)
  * [Extensions](#Extensions.md)

If you want to do something RDV can't do, use the [feature request tracker](http://code.google.com/p/rdv/issues/list?q=label%3AType-Enhancement&can=2) to view current feature requests or to request a [new feature](http://code.google.com/p/rdv/issues/entry). Read the [documentation](Documentation.md) for more information on how to use these features. And take a look at how [other projects](ProjectsUsingRDV.md) are using RDV.

### [![](http://rdv.googlecode.com/svn/wiki/rdv-logo-small.png)](http://rdv.googlecode.com/svn/wiki/rdv.jnlp) [Run RDV ](http://rdv.googlecode.com/svn/wiki/rdv.jnlp) - _Requires [Java](http://www.java.com/getjava/)_ ###

## Local or Remote Data Sources ##

Data can be pulled into RDV in a number of different ways. RDV can connect to a [Data Turbine](http://code.google.com/p/dataturbine/) server and view streaming data that is either live or archived. Data can also be imported locally from remote data sources or the local computer. RDV supports importing numeric, image, and video data from the local file system and [NEEScentral](http://central.nees.org/).

## Synchronized Data ##

RDV takes data from many different sources (that may be sampled at different rates) and visualizes them in a time synchronized manner. This includes numeric, image, and video data. With RDV, you can view data at a specific time, playback data at a specific rate, and view live streaming data. All of this is done within the same interface with the ability to zoom-in on time ranges of interest.

## Data Visualization ##

Numeric data can be view in a timeseries plot or in an XY plot. The plots can view multiple channels at once and are fully configurable. The axes support auto scaling, zooming in or out, and can be set manually. Additional settings such as title, colors, and fonts can be configured.

Numeric data can also be displayed in a table with the ability to set thresholds and monitor minimum and maximum values.

Images and video can be viewed within RDV with the ability to zoom in and out.

[![](http://rdv.googlecode.com/svn/wiki/rdv-screenshot-plot.png)](http://code.google.com/p/rdv/wiki/Screenshots)
[![](http://rdv.googlecode.com/svn/wiki/rdv-screenshot-image.png)](http://code.google.com/p/rdv/wiki/Screenshots)
[![](http://rdv.googlecode.com/svn/wiki/rdv-screenshot-table.png)](http://code.google.com/p/rdv/wiki/Screenshots)

## Event Markers ##

Event markers are used to mark a time where something interesting has happened. Event markers are flexible and can contain a label and a description to classify the event. And when used on a remote data source, all other uses connected to that data source will see your event, so they can be used in a collaborative way.

[![](http://rdv.googlecode.com/svn/wiki/rdv-screenshot-eventmarker.png)](http://code.google.com/p/rdv/wiki/Screenshots)

## Extensions ##

RDV can be extended to support new types of data or visualize data in new ways. An extension is a way to add new functionality to RDV. See the [extensions page](Extensions.md) for more information on how to extend RDV.