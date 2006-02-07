
/*
 * Created on Jan. 24, 2006
 */
package org.nees.buffalo.rdv.datapanel;
import java.util.Iterator;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/////////////////////////////////////////////////////////////////////////////LJM
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.rbnb.marker.NeesEvent;
import org.nees.rbnb.marker.SendMarkerRDVPanel;
/////////////////////////////////////////////////////////////////////////////LJM

/**
* A data panel that turns RDV into a hybrid in that it is a DataTurbine source
 * that submits data.
 * @author Lawrence J. Miller
 */
public class EventMarkerSubmissionDataPanel extends AbstractDataPanel implements ActionListener {
  
  private JPanel panel;
  private JScrollPane scrollPane;
  private String dtServer = "localhost";
  private String dtServerTemp;
  
  // LJM
  private static Log log = LogFactory.getLog (EventMarkerSubmissionDataPanel.class.getName ());
  
	/**
    * Initialize the object and UI
	 */
	public EventMarkerSubmissionDataPanel () {
		super ();
		initDataComponent ();
    setDataComponent (panel);
	} // constructor ()
	
	/**
    * Initialize the UI component and pass it too the abstract class.
	 */
	private void initDataComponent () {
    panel = new SendMarkerRDVPanel (null, dtServer);
		/*scrollPane = new JScrollPane (messages,
                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);*/
		//panel.add (scrollPane, BorderLayout.CENTER);
	}
  
	void clearData() {
		// TODO clear your data display
	}
  
	public boolean supportsMultipleChannels () {
		// TODO change if this data panel supports multiple channels
		return false;
	}
  
  public void postTime (double time) {
		super.postTime (time);
    this.dtServerTemp = rbnbController.getRBNBHostName ();
    if (this.dtServer.compareTo (this.dtServerTemp) != 0) {
      try {
        ((SendMarkerRDVPanel)panel).changeTurbine (dtServer);
      } catch (com.rbnb.sapi.SAPIException sae) {
        log.error ("Problem talking to DataTurbine.");
        sae.printStackTrace ();
      }
    } // if
    ((SendMarkerRDVPanel)panel).setRbnbLabel (dtServer);
  } // postTime ()
  
  public void actionPerformed (ActionEvent e) {}
  
} // class
