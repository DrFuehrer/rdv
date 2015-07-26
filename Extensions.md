## Introduction ##

RDV contains an extension mechanism that can be used to support new types of data or to visualize data in new ways. All of the standard tools included in RDV for viewing numeric, image, video, and audio data are implemented as extensions. Use extensions to take advantage of RDV's ability to deliver data from local and remote sources through a common interface, and concentrate on the type of data and the visualization you want to implement.

## A Data Panel ##

_[DataPanel](http://code.google.com/p/rdv/source/browse/trunk/src/org/rdv/datapanel/DataPanel.java)_ is the interface that all extensions implement. Data panel's are managed by RDV and their main job is do _something_ with the data in channels RDV makes available. A data panel can (and usually does) have a UI component to display this data in some way. This UI component can be docked into the main area of RDV that contains all the data panels.

## AbstractDataPanel ##

_[AbstractDataPanel](http://code.google.com/p/rdv/source/browse/trunk/src/org/rdv/datapanel/AbstractDataPanel.java)_ is an abstract implementation of `DataPanel` and all of RDV's extensions currently use this just for the sheer convenience. It does most of the work of creating a data panel for you. It will:

  * Registers listeners and keeps track of posted data and times
  * Handles subscribing and unsubscribing from channels
  * Enables drag-and-drop support for adding channels
  * Docks the UI component in RDV's data panel container.
  * Manages the common toolbar on the top of all data panels
  * Enables detach and full screen modes

It should make your life a little easier, but you can still implement a data panel with the `DataPanel` interface if you need more flexibility.

## Implementing a data panel with AbstractDataPanel ##

Follow the steps below to create a basic data panel. It won't do much yet, but we'll extend it later to handle data.

  1. Create a class that extends `AbstractDataPanel`.
  1. Implement a no argument constructor.
  1. Initialize any UI components and call `setDataComponent(JComponent)` with your main UI component while in the constructor.
  1. Implement the following method: `public boolean supportsMultipleChannels()`. If you data panel can display data from multiple channels at once, return true, otherwise return false.

Below is a part of the class _[TemplateDataPanel](http://code.google.com/p/rdv/source/browse/trunk/src/org/rdv/datapanel/TemplateDataPanel.java)_. Use it as a starting point for you data panel. It contains stubs for these methods that you must implement and can be used as a template for your data panel.

```
/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

  /** the last time displayed */
  private double lastTimeDisplayed;

  /**
   * Create the data panel.
   */
  public TemplateDataPanel() {
    super();

    initDataComponent();
  }

  /**
   * Initialize the UI component and pass it too the abstract class.
   */
  private void initDataComponent() {
    // TODO create data component
    JComponent myComponent = null;
    setDataComponent(myComponent);
  }

  public boolean supportsMultipleChannels() {
    // TODO change if this data panel supports multiple channels
    return false;
  }
}
```

## Dealing with channels ##

Override the following methods if you need to do anything special when a channel is added or removed from your data panel. Most data panel's will need to do this to update the UI or to create data structures to deal with new channels.

```
/**
 * For use by subclasses to do any initialization needed when a channel has
 * been added.
 * 
 * @param channelName  the name of the channel being added
 */
protected void channelAdded(String channelName) {}
```

```
/**
 * For use by subclasses to do any cleanup needed when a channel has been
 * removed.
 * 
 * @param channelName  the name of the channel being removed
 */
protected void channelRemoved(String channelName) {}
```


## Getting data ##

Override the `postTime(double)` method to receive data. It will be called each time RDV updates the current time. The code below loops through each channel and sees if there is data available for it. If there is, you data panel should get this data and display it.

```
public void postTime(double time) {
  if (time < this.time) {
    lastTimeDisplayed = -1;

    // TODO clear data in your data component
  }

  super.postTime(time);

  if (channelMap == null) {
    //no data to display yet
    return;
  }

  //loop over all channels and see if there is data for them
  for (String channelName : channels) {
    int channelIndex = channelMap.GetIndex(channelName);

    //if there is data for channel, post it
    if (channelIndex != -1) {
      displayData(channelName, channelIndex);
    }
  }
}
```

This is the `displayData` method from `TemplateDataPanel` and it will look for the data that should be displayed at this time. This implementation is looking data of type `double`, but any other type of data can be supported.

```
private void displayData(String channelName, int channelIndex) {
  if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_FLOAT64) {
    return;
  }

  double[] times = channelMap.GetTimes(channelIndex);

  int startIndex = -1;

  // determine what time we should load data from
  double dataStartTime;
  if (lastTimeDisplayed == time) {
    dataStartTime = time - timeScale;
  } else {
    dataStartTime = lastTimeDisplayed;
  }

  for (int i = 0; i < times.length; i++) {
    if (times[i] > dataStartTime && times[i] <= time) {
      startIndex = i;
      break;
    }
  }

  //see if there is no data in the time range we are loooking at
  if (startIndex == -1) {
    return;
  }

  int endIndex = startIndex;

  for (int i = times.length - 1; i > startIndex; i--) {
    if (times[i] <= time) {
      endIndex = i;
      break;
    }
  }

  double[] datas = channelMap.GetDataAsFloat64(channelIndex);
  for (int i=startIndex; i<=endIndex; i++) {
    double time = times[i];
    double data = datas[i];
    
    // TODO display the data at this timestamp
  }


  lastTimeDisplayed = times[endIndex];
}
```

Put your code where the `TODO` is to display the data in your UI component.

## Saving and restoring state ##

The state of a data panel can be saved and later restored with a RDV configuration file.
This is handled with Java [Properties](http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html). `AbstractDataPanel` has a `Properties` instance available to subclasses under the `properties` variable. Use this to store the state of your data panel.

When a data panel is restored, the method `setProperty(String key, String value)` will be called for each property. Override this to restore the state of your data panel from these properties.

## Registering a data panel extension ##

A list a extension to register on application start-up is stored in the file _[config/extensions.xml](http://code.google.com/p/rdv/source/browse/trunk/config/extensions.xml)_. In it is an entry for each extension with the following attributes:

| **Name**      | **Description** |
|:--------------|:----------------|
| `id`          | The name of the extension class |
| `name`        | A user friendly name for the extension |
| `mimeTypes`   | A list of MIME types that the extension can handle (optional) |

`id` and `name` are required, and while multiple MIME types can be defined, the element is optional. The MIME type used for numeric data is _application/octet-stream_ by convention.

A sample extension entry looks like:

```
<extension>
	<id>org.rdv.datapanel.TemplateDataPanel</id>
	<name>Template Data Panel</name>
	<mimeTypes>
		<mimeType>application/octet-stream</mimeType>
	</mimeTypes>		
</extension>	
```

## Starting your data panel ##

Everything should be set. So run RDV and in the _Window_ menu there will be a menu item for your data panel. Click it to open it and it should run. You can also activate a context menu (right click) on a channel that has a MIME type that your data panel can handle and open it that way.