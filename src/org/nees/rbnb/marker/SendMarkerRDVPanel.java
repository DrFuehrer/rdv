package org.nees.rbnb.marker;

/**
* 
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050916
 *
 * Copyright (c) 2005, 2006, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $HeadURL$ 
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.buffalo.rdv.rbnb.ConnectionListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.nees.rbnb.DataTurbine;
import org.nees.rbnb.marker.MarkerFieldDialog;
import org.nees.rbnb.marker.NeesEventRDVTimelinePanel;
import org.nees.rbnb.marker.NeesEvent;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;
import com.rbnb.sapi.SAPIException;

public class SendMarkerRDVPanel extends JPanel implements ActionListener, ConnectionListener
{
  private static Log log = LogFactory.getLog (SendMarkerRDVPanel.class.getName ());
  private boolean debugging = false;
  protected DataTurbine myTurban;
  private JOptionPane turbanPane;
  private static final int FRAME_HEIGHT = 100, FRAME_WIDTH = 360;
  private JButton sendMarkerButton;
  // private JTextArea markerDisplayArea;
  /** variables to keep track of the text entry field GUI elements, which are
    * generated dynamically from the NeesEvent */
  private Box[] fieldBoxes;
  private JTextField[] jtextFields;
  private JLabel[] jlabels;
  
  /** @see org.nees.rbnb.marker.NeesEvent */
  protected NeesEvent myEvent;
  protected String rbnbServerName;
  /** A variable that has the name of the channel to create on the rbnb source
    to which to submit markers. */
  private String rbnbSourceName = "_Events";
  private String rbnbChannel = "EventsChannel";
  public boolean archiveRbnbData = true;
  public static String RDVsourceLabel = SendMarkerRDVPanel.class.getName () +
    " from RDV";
  /** rdv's rbnb controller @see org.nees.buffalo.rdv.rbnb.RBNBController */
  protected RBNBController rdvRbnb;
  /** a variable to he=]old a reference to rdv's ApplicationFrame */
  private JFrame rdvFrame;
  /** An event marker timeline 
    * @see org.nees.rbnb.marker.NeesEventRDVTimelinePanel */
  NeesEventRDVTimelinePanel timeLine;
  
  /***************************************************************************/       
  /** Object constructor; noisy with the GUI construction, which is verbose. */
  public SendMarkerRDVPanel (NeesEvent anEvent, RBNBController rbnb, NeesEventRDVTimelinePanel tl, JFrame frame) {
    this.rdvRbnb = rbnb;
    this.timeLine = tl;
    this.rbnbServerName = rdvRbnb.getRBNBHostName ();
    this.rdvFrame = frame;
    this.myTurban = new DataTurbine (this.rbnbSourceName);
    try {
      setRbnbServer (this.rbnbServerName);
      log.info ("Opened RBNB connection.");
    } catch (SAPIException sapie) {
      log.error ("Problem opening the turbine: " + this.rbnbServerName + sapie);
    }
    catch (Exception e) {
      e.printStackTrace ();
    }
    myEvent = (anEvent != null)? anEvent : new NeesEvent (this.rbnbChannel);
    myEvent.setProperty ("type", "other");
    
    /* Add in a hook for ctrl-c's and other abrupt death. */
    Runtime.getRuntime ().addShutdownHook (new Thread () {
      public void run () {
        try {
          sendClosingMarker ();
          closeTurbine ();
          log.debug ("Shutdown hook.");
        } catch (IOException ioe) {
          log.error ("I/O problem sending closing marker." + ioe);
        } catch (TransformerException te) {
          log.error ("XML problem sending closing marker." + te);
        } catch (Exception e) {
          log.error ("Unexpected error closing DataTurbine.");
        }
      }
    });
    
    this.setBorder (BorderFactory.createEtchedBorder ());
    
    this.addMouseListener (new MouseAdapter () {
      public void mouseClicked  (MouseEvent e) {}
      public void mouseEntered	(MouseEvent e) {}
      public void mouseExited	  (MouseEvent e) {}
      public void mousePressed	(MouseEvent e) {}
    }
                           );
    ////////////////////////////////////////////////////////////////////////////
    // Construct the GUI
    Box sendButtonBox = Box.createHorizontalBox ();
    sendMarkerButton = new JButton ("Create Event Marker");
    sendMarkerButton.addActionListener (this);
    sendMarkerButton.setActionCommand ("sendMarker");
    sendButtonBox.add (Box.createHorizontalStrut (80));
    sendButtonBox.add (sendMarkerButton);
    sendButtonBox.add (Box.createHorizontalStrut (80));
    
    /* List of field entries from the XML schema that will be a stack of
      label/textArea pairs. */
    Box layoutFieldBox = Box.createVerticalBox ();
    // loop thru fields 
    fieldBoxes = new Box[NeesEvent.markerFieldNames.length];
    ArrayList jtextFieldsTmp = new ArrayList ();
    ArrayList jlabelsTmp = new ArrayList ();
    for (int i=0; i<NeesEvent.markerFieldNames.length; i++) {
      /* LJM 060327 - this if clips out fields; will use this until a spec for
      * desired fields is put forth by use case. */ 
      if ( NeesEvent.markerFieldNames[i].equals ("content") ||
           NeesEvent.markerFieldNames[i].equals ("label") ||
           NeesEvent.markerFieldNames[i].equals ("source") ||
           NeesEvent.markerFieldNames[i].equals ("timestamp") ||
           NeesEvent.markerFieldNames[i].equals ("type")
           ) continue;
      fieldBoxes[i] = Box.createHorizontalBox ();
      JLabel jlabelTmp = new JLabel (NeesEvent.markerFieldNames[i]);
      fieldBoxes[i].add (jlabelTmp);
      jlabelsTmp.add (jlabelTmp);
      fieldBoxes[i].add (Box.createHorizontalStrut (11));
      JTextField markerFieldTmp = new JTextField (NeesEvent.markerFieldNames[i]);
      markerFieldTmp.addActionListener (this);
      fieldBoxes[i].add (markerFieldTmp);
      jtextFieldsTmp.add (markerFieldTmp);
      layoutFieldBox.add (fieldBoxes[i]);
    } // for
    
    // turn the ArrayLists into arrays 
    Object[] jtextFieldsTmpArray = jtextFieldsTmp.toArray ();
    this.jtextFields = new JTextField[jtextFieldsTmpArray.length];
    for (int i=0; i<jtextFieldsTmpArray.length; i++) {
      jtextFields[i] = (JTextField)jtextFieldsTmpArray[i];
    } // for
    Object[] jlabelTmpArray = jlabelsTmp.toArray ();
    this.jlabels = new JLabel[jlabelTmpArray.length];
    for (int i=0; i<jlabelTmpArray.length; i++) {
      jlabels[i] = (JLabel)jlabelTmpArray[i];
    } // for
    
    this.add (layoutFieldBox);
    layoutFieldBox.add (sendButtonBox);
    
    try {
      sendOpeningMarker ();
    } catch (IOException ioe) {
      log.error ("I/O problem sending opening marker." + ioe);
    } catch (TransformerException te) {
      log.error ("XML problem sending opening marker." + te);
    }
  } // constructor ()
    ////////////////////////////////////////////////////////////////////////////////
  
  /** @see org.nees.buffalo.rdv.rbnb.ConnectionListener */
  public void connecting () {}
  
  /** @see org.nees.buffalo.rdv.rbnb.ConnectionListener */
  public void connected () {
    if (this.rbnbServerName.compareTo (this.rdvRbnb.getRBNBHostName ()) != 0) {
      // then the DataTurbine server has changed; we need to change to keep with it
      try {
        changeTurbine (this.rdvRbnb.getRBNBHostName ());
      } catch (SAPIException sae) {
        log.error ("Couldn't change RBNB servers: " + sae);
      }
      log.info ("notified about connection to \"" +
                this.rdvRbnb.getRBNBHostName () + "\" in listener.");
    } // if there's a new RBNB
  } // connected ()
  
  /** @see org.nees.buffalo.rdv.rbnb.ConnectionListener */
  public void connectionFailed () {}	
  
  /** A method that @return if this has a connected @see org.nees.rbnb.DataTurbine */
  public boolean isConnected () {
    return this.myTurban.isConnected ();
  }
  
  /**
    * A method that resets the DataTurbine server to which this is connected to
   * @param the new DataTurbine server.
   */
  public void setRbnbServer (String serverName) throws SAPIException, Exception {
    if (this.myTurban.isConnected ()) {
      this.closeTurbine ();
    }
    this.rbnbServerName = serverName;
    this.myTurban.setServerName (this.rbnbServerName);
    this.myTurban.open ();
  } // setRbnbServer ()
  
  /** A method to mark the beginning of the sumbission session.
    */
  public void sendOpeningMarker () throws IOException, TransformerException {
    if (! this.isConnected ()) { return; }
    NeesEvent openingMarker = new NeesEvent (this.rbnbChannel);
    openingMarker.setProperty ("source", RDVsourceLabel);
    openingMarker.setProperty ("label", "Connection Flag");
    openingMarker.setProperty ("type", "annotation");
    openingMarker.setProperty ("content", "Marker Submission Client Connected.");
    openingMarker.setProperty ("timestamp",""+(((double)(System.currentTimeMillis ())) / 1000.0));
    this.myTurban.putMarker (openingMarker, openingMarker.rbnbChannel);
    log.info ("Submitted opening marker.");
    if (this.rdvRbnb != null) {
      this.rdvRbnb.updateMetadata ();
    }
  } // sendOpeningMarker ()
  
  
  /** A method to mark the end of the sumbission session.
    * stubbed until a use case indicates that this is a desireable feature.
  */
  public void sendClosingMarker () throws IOException, TransformerException {
    /* Stuff a special a marker and send it. */
    /* NeesEvent closingMarker = new NeesEvent (this.rbnbChannel);
    closingMarker.setProperty ("source", RDVsourceLabel);
    closingMarker.setProperty ("label", "Disconnection Flag");
    closingMarker.setProperty ("type", "annotation");
    closingMarker.setProperty ("content", "Marker Submission Client Disconnected.");
    closingMarker.setProperty ("timestamp",""+(((double)(System.currentTimeMillis ())) / 1000.0));
    
    try {
      this.myTurban.putMarker (
                               closingMarker.toEventXmlString (), closingMarker.rbnbChannel
                               );
      log.info ("Submitted closing marker.");
    } catch (IOException ioe) {
      log.error ("IO error displaying to the GUI\n" + ioe);
      throw ioe;
    } catch (TransformerException te) {
      log.error ("XML Error forming the XML doc\n" + te);
      throw te;
    }*/
  } // sendClosingMarker ()
  
  
  /** A method to change the connection of this instance to @param an new
    * DataTurbine server. */
  public void changeTurbine (String newTurbine) throws SAPIException {
    DataTurbine tempTurban = new DataTurbine (rbnbSourceName);
    tempTurban.setServerName (newTurbine);
    try {
      tempTurban.open ();
    } catch (SAPIException sae) {
      throw sae;
    }
    if (this.myTurban != null &&
        tempTurban != null &&
        tempTurban.isConnected ())
    {
      this.closeTurbine ();
      this.myTurban = tempTurban;
      log.info ("DataTurbine server changed from " +
                this.rbnbServerName + " to " + 
                newTurbine);
      
      this.rbnbServerName = newTurbine;
      try {
        this.sendOpeningMarker ();
      } catch (IOException ioe) {
        log.error ("Trouble talking to the new DataTurbine.");
      } catch (TransformerException te) {
        log.error ("Trouble talking to the new DataTurbine.");
      }
    } else {
      log.debug ("DataTurbine server changed from " +
                 this.rbnbServerName + " to " + 
                 newTurbine + " requested, but " +
                 newTurbine + " isn't connected.");
    } // else
  } // changeTurbine ()
  
  
  /** A method to close the current DataTurbine connection. 
    * Throws a generic Exception as the RBNB close method 
    * is spec'ed not to throw anything, but this code prepares
    * for the unexpected.*/
  public void closeTurbine () {
    try {
      if (this.archiveRbnbData) {
        this.myTurban.closeAndKeep ();
        log.info ("Closed DataTurbine with cache and archive.");
      } else {
        this.myTurban.close ();
        log.info ("Closed DataTurbine without cache and archive.");
      }
    } catch (Exception ex) {
      log.error ("Error closing turbine: " + ex);
    }
    this.rbnbServerName = "";
  } // closeTurbine ()
  
  
  /** A stubbed do-nothing main method */
  public static void main (String[] args) {
    System.out.println (SendMarkerRDVPanel.class.getName () + " is not " +
                        "runnable and is designed to be used as an RDV GUI " +
                        "component.");
    System.exit (2);
  } // main()
  
  
  /** An object destructor method. */
  protected void finalize () {
    try {
      sendClosingMarker ();
    } catch (IOException ioe) {
      log.error ("I/O problem sending closing marker." + ioe);
    } catch (TransformerException te) {
      log.error ("XML problem sending closing marker." + te);
    }
    try {
      this.closeTurbine ();
    } catch (Exception e) {
      log.error ("Error closing turbine: " + e);
    }
  } // finalize ()
  
  
  /**
    * A method to handle mouse-generated events (send marker button)
   */
  public void actionPerformed (ActionEvent e) {
    String commands[] = { "sendMarker" };
    int index;
    
    for (index=0; index<commands.length; index++) {
      if (e.getActionCommand ().equals (commands[index])) {
        break;
      }
    } // for
    
    switch (index) {
      
      case 0:
        double markerCreateTime = (double)(System.currentTimeMillis ()) / 1000.0;
        String markerLabel = null;
        String markerContent = null;
        MarkerFieldDialog markerDialog = null;
        
        // if rdv isn't in real-time mode, then put it into rt mode
        if (this.rdvRbnb.getState () != org.nees.buffalo.rdv.rbnb.Player.STATE_MONITORING) {
          String rtPrompt = "RDV must be in real-time monitor mode for Event Markers to register.\nShall RDV be put into real-time monitor mode to proceed? (No cancels event marker submission)";
          int putInRT = JOptionPane.showConfirmDialog (this,
                                                       rtPrompt,
                                                       "Activate RDV Real-Time Mode?",
                                                       JOptionPane.YES_NO_OPTION,
                                                       JOptionPane.PLAIN_MESSAGE,
                                                       null);
          // if the user selected yes, do it
          if (putInRT == JOptionPane.YES_OPTION) {
            this.rdvRbnb.monitor ();
          } else if (putInRT == JOptionPane.NO_OPTION) { // if no, then cancel
            break;
          }
        } // if not in real time
          
        // Get the marker fields from the user
          if (markerDialog == null) {
            markerDialog =
              new MarkerFieldDialog (this.rdvFrame,
                                 markerCreateTime,
                                 this);
          } else {
            markerDialog.setVisible (true);
          }
      break;
        
    } // switch
  } // actionPerformed ()
  
} // class

/* Copyright Notice:
*
* Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*    * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the 
* documentation and/or other materials provided with the distribution.
*    * Neither the name of the San Diego Supercomputer Center nor the names of
* its contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
                         * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
                         * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
