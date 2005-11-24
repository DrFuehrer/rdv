/*
 * Created on Feb 7, 2005
 */
package org.nees.buffalo.rdv.datapanel;

import org.nees.rbnb.marker.NeesEvent;
import java.util.Iterator;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class EventMarkerDataPanel extends AbstractDataPanel {

	/**
	 * The UI component to display the data in
	 */
	JComponent dataComponent;
	double lastTimeDisplayed;
    
    // LJM 051021 - lifted from StringDataPanel
   JPanel panel;
	JEditorPane messages;
	JScrollPane scrollPane;
   StringBuffer messageBuffer;

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
      messages.setContentType ("text/html");
		scrollPane = new JScrollPane (messages,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add (scrollPane, BorderLayout.CENTER);
		
		setDataComponent (dataComponent);
	}

	void clearData() {
		// TODO clear your data display
      // LJM 051021
      messages.setText(null);
      messageBuffer.delete(0, messageBuffer.length());
		lastTimeDisplayed = -1;
	}

	public boolean supportsMultipleChannels() {
		// TODO change if this data panel supports multiple channels
		return false;
	}

	public void postTime(double time) {
		super.postTime(time);
		
		//loop over all channels and see if there is data for them
		Iterator it = channels.iterator();
		while (it.hasNext()) {
			String channelName = (String)it.next();
			int channelIndex = channelMap.GetIndex(channelName);
			
			//if there is data for channel, post it
			if (channelIndex != -1) {
				// TODO display the data in your data component
            // LJM 051021 copied from StringDataPanel.java, line 148
            String[] data = channelMap.GetDataAsString(channelIndex);
            NeesEvent[] eventData = new NeesEvent[data.length];
            double[] times = channelMap.GetTimes(channelIndex);

            int startIndex = -1;
		
            for (int i=0; i<times.length; i++) {
               if (times[i] > lastTimeDisplayed && times[i] <= time) {
               startIndex = i;
               break;
               } // if
            } // for
		
            //see if there is no data in the time range we are loooking at
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
            // LJM here
            StringBuffer messageBuffer = new StringBuffer ();
            for (int i=startIndex; i<=endIndex; i++) {
               eventData[i] = new NeesEvent ();
               try {
                  eventData[i].setFromEventXml (data[i]);
               } catch (Exception e) { e.printStackTrace ();}
               messageBuffer.append("DataTurbineTime: " + times[i] +
                                       eventData[i] + "\n");
            } // for
            messages.setText(messageBuffer.toString());
            
         } // if
		} // while
	} // postTime ()
} // class
