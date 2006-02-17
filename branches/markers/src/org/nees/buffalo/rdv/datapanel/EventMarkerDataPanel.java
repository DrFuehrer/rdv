// LJM 051129 - choking on panel and dataComponent
/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rdv.datapanel;
import java.util.Iterator;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

// LJM 051129
import org.nees.rbnb.marker.NeesEvent;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;

/**
 * An RDV data panel that will handle XML-based event marker objects from a text
 * channel in DataTurbine and render them particularly to their event type. 
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class EventMarkerDataPanel extends AbstractDataPanel {
  
	/**
	 * The UI component to display the data in
	 */
	//private JComponent dataComponent;
	private double lastTimeDisplayed;
  
  // LJM 051021 - lifted from StringDataPanel
  private JPanel panel;
	private JEditorPane messages;
	private JScrollPane scrollPane;
  private StringBuffer messageBuffer;
  // LJM
  private static Log log = LogFactory.getLog (EventMarkerDataPanel.class.getName ());
  
	/**
    * Initialize the object and UI
	 */
	public EventMarkerDataPanel () {
		super ();
    messageBuffer = new StringBuffer ();
		initDataComponent ();
    setDataComponent (panel);
	}
	
	/**
    * Initialize the UI component and pass it too the abstract class.
	 */
	private void initDataComponent () {
		// TODO create data component
    // LJM 051021 - lifted from StringDataPanel
    panel = new JPanel ();
		panel.setLayout (new BorderLayout ());
		messages = new JEditorPane ();
		messages.setEditable (false);
    messages.setContentType ("text/plain");
		scrollPane = new JScrollPane (messages,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add (scrollPane, BorderLayout.CENTER);
		//setDataComponent (panel);
	}
  
	void clearData() {
		// TODO clear your data display
    // LJM 051021
    messages.setText (null);
    messageBuffer.delete (0, messageBuffer.length ());
    messages.setBackground (Color.white);
    messages.setFont (new Font ("Dialog", Font.PLAIN, 12));
		lastTimeDisplayed = -1;
	}
  
	public boolean supportsMultipleChannels () {
		// TODO change if this data panel supports multiple channels
		return false;
	}
  
	/** LJM 051129
    * A method to manage the graphical display of Event Markers.
    */
  public void postTime (double time) {
		super.postTime (time);
		
		//loop over all channels and see if there is data for them
		Iterator it = channels.iterator ();
    int channelIndex = -1;
		while (it.hasNext ()) {
			String channelName = (String)it.next ();
			if (channelMap != null ) {
        channelIndex = channelMap.GetIndex (channelName);
      }
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				// TODO display the data in your data component
        // LJM 051021 copied from StringDataPanel.java, line 148
        String[] data = channelMap.GetDataAsString (channelIndex);
        NeesEvent[] eventData = new NeesEvent[data.length];
        double[] times = channelMap.GetTimes (channelIndex);
        
        int startIndex = -1;
        
        for (int i=0; i<times.length; i++) {
          if (times[i] > lastTimeDisplayed && times[i] <= time) {
            startIndex = i;
            break;
          } // if
        } // for
        
        // if there is no data in the time range we are looking at
        if (startIndex == -1) {
          return;
        } // if	
        
        int endIndex = startIndex;
        
        for (int ii=times.length-1; ii>startIndex; ii--) {
          if (times[ii] <= time) {
            endIndex = ii;
            break;
          } // if
        } // for
          // LJM actually display the marker
        StringBuffer messageBuffer = new StringBuffer ();
        for (int i=startIndex; i<=endIndex; i++) {
          eventData[i] = new NeesEvent ();
          try {
            eventData[i].setFromEventXml (data[i]);
          } catch (TransformerException te) { 
            log.error ("Java XML Error\n" + te);
            te.printStackTrace ();
          } catch (InvalidPropertiesFormatException ipfe) {
            log.error ("Java XML Error\n" + ipfe);
            ipfe.printStackTrace ();
          } catch (IOException ioe) {
            log.error ("Java IO Error\n" + ioe);
            ioe.printStackTrace ();
          }
          try {
            if ( eventData[i].getProperty ("type") != null &&
                 ((String)(eventData[i].getProperty ("type"))).compareToIgnoreCase ("start") == 0 
                 ) {
              messageBuffer.append ("start\n\n");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) );
              messages.setBackground (NeesEvent.startColor);
              log.info ("Got a START marker.");
              messages.setFont (new Font ("Dialog", Font.BOLD, 24));
              
            } else if ( eventData[i].getProperty ("type") != null &&
                        ((String)(eventData[i].getProperty ("type"))).compareToIgnoreCase ("stop") == 0 
                        ) {
              messageBuffer.append ("Stop\n\n");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) );
              messages.setBackground (NeesEvent.stopColor);
              log.info ("Got a STOP marker.");
              messages.setFont (new Font ("Dialog", Font.BOLD, 24));
            } else { 
              String markerXml = eventData[i].toEventXmlString ();
              messageBuffer.append ("--\n");
              messageBuffer.append ("Label: ");
              messageBuffer.append ( (String)(eventData[i].getProperty ("label")) + "\n" );
              messageBuffer.append ("Content: ");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) + "\n" );
              messageBuffer.append ("Source: ");
              messageBuffer.append ( (String)(eventData[i].getProperty ("source")) );
              
              messages.setBackground (NeesEvent.noteColor);
              messages.setFont (new Font ("Dialog", Font.PLAIN, 12));
            }
            
            messageBuffer.append ("\nTime Marker Generated:\n");
            messageBuffer.append ( (String)(eventData[i].getProperty ("timestamp")) + "\n");
            messageBuffer.append ( DataViewer.formatDate (Double.parseDouble (eventData[i].getProperty ("timestamp"))) );
            messageBuffer.append ("\nTime Marker Received:\n");
            messageBuffer.append ( DataViewer.formatDate (times[i]) );
                                                          
            messageBuffer.append ("\n--\n");
          
          } catch (IOException ioe) {
            messageBuffer.append ("Java IO Error\n" + ioe);
          } catch (TransformerException te) {
            messageBuffer.append ("Java XML Display Error\n" + te);
          }
        } // for
        messages.setText (messageBuffer.toString ());
      } // if
		} // while
	} // postTime ()
} // class
