## Introduction ##

An image channel can have a thumbnail image channel associated with it. This is done using the `ThumbNailPlugIn` distributed with Data Turbine. When this plugin is started, RDV can use it to display a thumbnail image instead of a full resolution image. This can be used to save bandwidth when you are not interested in the full resolution image

## Starting the Thumbnail Plugin ##

The thumbnail plugin can be started in two different ways. Via Web Turbine or via the command line. Both take the same set of options.

### Plugin Options ###

The following options can be passed to plugin when starting it.

| **Description**             | **Command line** | **Default**      |
|:----------------------------|:-----------------|:-----------------|
| RBNB Server Address         | -a               | localhost:3333   |
| PlugIn Name                 | -n               | ThumbNail        |
| JPEG Source/Channel         | -c               |                  |
| Image Scale Factor (0-4)    | -s               | 0.5              |
| Image Quality (0-1)         | -q               | 0.5              |
| Maximum Number of Images    | -M               | 10               |

If no source or channel is specified, the plugin will thumbnail all image channels. To limit the thumbnailing to specific sources or channels, specify a source or channel.

### Starting Via Web Turbine ###

To start the plugin via Web Turbine, go to the Web Turbine web site for your Data Turbine server (http://hostname/webTurbine/) and click `Thumb Nail PlugIn`. From here you can set the various options for the plugin. The plugin name needs to be changed to `_Thumbnails` because this is what RDV is hard coded to look for. The other parameters can be tweaked for the desired usage. Click `Start PlugIn` to start the plugin.

### Starting Via the Command Line ###

To start the plugin via the command line, replace `$RBNB_HOME` with the root Data Turbine directory, and execute the following command:

> `java -cp $RBNB_HOME/bin/rbnb.jar:$RBNB_HOME/apache-tomcat-5.5.12/webapps/webTurbine/WEB-INF/classes/ ThumbNailPlugIn -n _Thumbnails`

The other plugins options listed above may be used, but they are optional.

## Viewing Thumbnails in RDV ##

Once the plugin is started, RDV will automatically detect that the plugin is running. To switching to viewing a thumbnail, right click the image to bring up the popup menu and select `Use thumbnail image`.

![http://rdv.googlecode.com/svn/wiki/use-thumbnail-image.png](http://rdv.googlecode.com/svn/wiki/use-thumbnail-image.png)