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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;

/**
* A data panel that turns RDV into t hybrid in that it is a DataTurbine source
 * that submits data.
 * @author Lawrence J. Miller
 */
public class EventMarkerSubmissionDataPanel extends AbstractDataPanel implements ActionListener {
  
  private JPanel panel;
  private JScrollPane scrollPane;
  
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
		// TODO create data component
    panel = new JPanel ();
		panel.setLayout (new BorderLayout ());
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
  } // postTime ()
  
  public void actionPerformed (ActionEvent e) {}
  
} // class
