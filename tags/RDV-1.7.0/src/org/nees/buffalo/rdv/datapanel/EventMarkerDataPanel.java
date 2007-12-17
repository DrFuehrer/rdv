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

package org.nees.buffalo.rdv.datapanel;

import java.util.Iterator;
import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

// LJM
import org.nees.rbnb.marker.EventMarker;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
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
  
  // LJM lifted from StringDataPanel
  private JPanel panel;
	private JEditorPane messages;
	private JScrollPane scrollPane;
  private StringBuffer messageBuffer;
  // LJM
  private static Log log = LogFactory.getLog (EventMarkerDataPanel.class.getName ());
  
  protected static final Color startColor = Color.green;
  protected static final Color stopColor = Color.red;
  protected static final Color noteColor = Color.white;
  
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
    * Initialize the UI component and pass it to the abstract class.
	 */
	private void initDataComponent () {
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
    messages.setText (null);
    messageBuffer.delete (0, messageBuffer.length ());
    messages.setBackground (Color.white);
    messages.setFont (new Font ("Dialog", Font.PLAIN, 12));
		lastTimeDisplayed = -1;
	}
  
	public boolean supportsMultipleChannels () {
		return false;
	}
  
	/**
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
        String[] data = channelMap.GetDataAsString (channelIndex);
        EventMarker[] eventData = new EventMarker[data.length];
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
          eventData[i] = new EventMarker ();
          try {
            eventData[i].setFromEventXml (data[i]);
          } catch (InvalidPropertiesFormatException ipfe) {
            log.error ("Java XML Error\n" + ipfe);
            ipfe.printStackTrace ();
          } catch (IOException ioe) {
            log.error ("Java IO Error\n" + ioe);
            ioe.printStackTrace ();
          }
          try {
            if ( eventData[i].getProperty ("type") != null &&
                 ((String)(eventData[i].getProperty ("type"))).compareToIgnoreCase ("start") == 0 ) {
              messageBuffer.append ("start\n\n");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) );
              messages.setBackground (startColor);
              log.info ("Got a START marker.");
              messages.setFont (new Font ("Dialog", Font.BOLD, 24));
              
            } else if ( eventData[i].getProperty ("type") != null &&
                        ((String)(eventData[i].getProperty ("type"))).compareToIgnoreCase ("stop") == 0 ) {
              messageBuffer.append ("Stop\n\n");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) );
              messages.setBackground (stopColor);
              log.info ("Got a STOP marker.");
              messages.setFont (new Font ("Dialog", Font.BOLD, 24));
            } else { 
              String markerXml = eventData[i].toEventXmlString ();
              //messageBuffer.append ("--\n");
              
              messageBuffer.append ("Label: ");
              messageBuffer.append ( (String)(eventData[i].getProperty ("label")) + "\n" );
              messages.setBackground (noteColor);
              messages.setFont (new Font ("Dialog", Font.PLAIN, 12));
            
              messageBuffer.append ("Content: ");
              messageBuffer.append ( (String)(eventData[i].getProperty ("content")) + "\n" );
              messages.setBackground (noteColor);
              messages.setFont (new Font ("Dialog", Font.PLAIN, 12));
            
            }
            
            messageBuffer.append ("\nTime Marker Generated:\n");
            messageBuffer.append ( DataViewer.formatDate (Double.parseDouble (eventData[i].getProperty ("timestamp"))) );
            /*messageBuffer.append ("\nTime Marker Received:\n");
            messageBuffer.append ( DataViewer.formatDate (times[i]) );*/
            
            //messageBuffer.append ("\n--\n");
          
          } catch (IOException ioe) {
            messageBuffer.append ("Java IO Error\n" + ioe);
          }
        } // for
        messages.setText (messageBuffer.toString ());
      } // if
		} // while
	} // postTime ()
} // class