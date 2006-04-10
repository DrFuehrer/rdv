package org.nees.rbnb.marker;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;
import javax.xml.transform.TransformerException;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.DataTurbine;
import org.nees.rbnb.marker.NeesEvent;
import com.rbnb.sapi.SAPIException;

/**
 * 
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Terry E. Weymouth <weymouth@umich.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050916
 *
 * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 */
public class SendMarkerGUI extends JFrame implements ActionListener
{
   private static Log log = LogFactory.getLog (SendMarkerGUI.class.getName ());
   private boolean hasFakeChannel = false;
   private boolean debugging = false;
   private String rbnbSourceName = "Events_Debug";
   private DataTurbine myTurban;
   private JOptionPane turbanPane;
   private Container c;
   private final int FRAME_HEIGHT = 500, FRAME_WIDTH = 450;
   
   private JPanel mainPanel;
   private Box mainPanelBox;
   private JMenuBar menuBar;
      private JMenu fileMenu;
         private JMenuItem quitFileMenuItem;
      private JMenu dtMenu;
         private JMenuItem dtConnectMenuItem;
         private JMenuItem dtDisconnectMenuItem;
         private JMenuItem dtChannelMenuItem;
         private JMenuItem dtSendMarkerMenuItem;
   private JLabel rbnbServerLabel;
   private JButton sendMarkerButton;
   private JCheckBox keepAliveCheckBox;
   private JTextArea markerDisplayArea;
   /** variables to keep track of the text entry field GUI elements, which are
     * generated dynamically from the NeesEvent */
   private Box[] fieldBoxes;
   private JTextField[] jtextFields;
   private JLabel[] jlabels;
   
   /** @see org.nees.rbnb.marker.NeesEvent */
   private NeesEvent myEvent;
   private FakeNeesEventChannel myEventChannel;
   private String rbnbServerName = "localhost:3333";
   /** A variable that has the name of the channel to create on the rbnb source
      to which to submit markers. */
   private String rbnbChannel = "EventsChannel_Debug";
   private boolean archiveRbnbData = true;
   
   /***************************************************************************/       
   /** Object constructor; noisy with the GUI construction, which is verbose. */
   public SendMarkerGUI (NeesEvent anEvent) {
      super ("DataTurbine Event Marker Submission Utility");
      setWindowListener ();
      if (this.hasFakeChannel) {
         setEventChannel ("fakeChannel");
         setEventChannelListener ();
      } else { // want to connect to DataTurbine
         /** @see org.nees.rbnb.DataTurbine */
         this.myTurban = new DataTurbine (this.rbnbSourceName);
         try {
            this.myTurban.open ();
            log.info ("Opened RBNB connection.");
         } catch (SAPIException sapie) {
            log.error ("Problem opening the turbine: this.rbnbServerName" + sapie);
         }
      }
      myEvent = (anEvent != null)? anEvent : new NeesEvent (this.rbnbChannel);
      myEvent.setProperty ("type", "other");
      myEvent.setProperty ("source", SendMarkerGUI.class.getName ());
      c = getContentPane ();
      mainPanel = new JPanel () {
         public void paintComponent (Graphics g) {
            super.paintComponent (g);
         } // paintComponent ()
      }; // JPanel ()
      mainPanel.setBackground (NeesEvent.danielBlue);
      
      /* Add in a hook for ctrl-c's and other abrupt death. */
      Runtime.getRuntime ().addShutdownHook (new Thread () {
         public void run () {
            try {
               sendClosingMarker ();
               myTurban.closeAndKeep ();
               log.info ("Shutdown hook.");
            } catch (IOException ioe) {
               log.error ("I/O problem sending closing marker." + ioe);
            } catch (TransformerException te) {
               log.error ("XML problem sending closing marker." + te);
            } catch (Exception e) {
               log.error ("Unexpected error closing DataTurbine.");
            }
         }
      });
      
      /******* set up the menus *******/
      menuBar = new JMenuBar ();
      setJMenuBar (menuBar);
         fileMenu = new JMenu ("File");
         fileMenu.setMnemonic (KeyEvent.VK_F);
            quitFileMenuItem = new JMenuItem ("Quit");
            quitFileMenuItem.setMnemonic (KeyEvent.VK_Q);
            quitFileMenuItem.addActionListener (this);
            quitFileMenuItem.setActionCommand ("quit");
            fileMenu.add (quitFileMenuItem);
         dtMenu = new JMenu ("DataTurbine");
         dtMenu.setMnemonic (KeyEvent.VK_D);
            dtConnectMenuItem = new JMenuItem ("Connect");
            dtConnectMenuItem.setMnemonic (KeyEvent.VK_C);
            dtConnectMenuItem.addActionListener (this);
            dtConnectMenuItem.setActionCommand ("dtConnect");
            
            dtDisconnectMenuItem = new JMenuItem ("Disconnect");
            dtDisconnectMenuItem.setMnemonic (KeyEvent.VK_S);
            dtDisconnectMenuItem.addActionListener (this);
            dtDisconnectMenuItem.setActionCommand ("dtDisconnect");            
         
            dtChannelMenuItem = new JMenuItem ("Set Channel Name");
            dtChannelMenuItem.setMnemonic (KeyEvent.VK_H);
            dtChannelMenuItem.addActionListener (this);
            dtChannelMenuItem.setActionCommand ("dtChannel"); 
            
            dtSendMarkerMenuItem = new JMenuItem ("Send Marker");
            dtSendMarkerMenuItem.setMnemonic (KeyEvent.VK_M);
            dtSendMarkerMenuItem.addActionListener (this);
            dtSendMarkerMenuItem.setActionCommand ("sendMarker");
            
            dtMenu.add (dtConnectMenuItem);
            dtMenu.add (dtDisconnectMenuItem);
            dtMenu.add (dtChannelMenuItem);
            dtMenu.add (dtSendMarkerMenuItem);
            
      menuBar.add (fileMenu);
      menuBar.add (dtMenu);
      /******* end menus *******/
      
      turbanPane = new JOptionPane ();
      turbanPane.setInitialValue ("localhost:3333");
      
      mainPanel.setBorder (BorderFactory.createEtchedBorder ());
      
      mainPanel.addMouseListener (new MouseAdapter () {
         public void mouseClicked   (MouseEvent e) {}
         public void mouseEntered	(MouseEvent e) {}
         public void mouseExited		(MouseEvent e) {}
         public void mousePressed	(MouseEvent e) {}
      }
                                        );
      // Construct the GUI
      Box serverLabelBox = Box.createHorizontalBox ();
      rbnbServerLabel = (this.hasFakeChannel)? new JLabel ("Fake Channel") : new JLabel (this.rbnbServerName);
      serverLabelBox.add (new JLabel ("Connected to DataTurbine Server: "));
      if (! this.myTurban.isConnected ()) {
         rbnbServerLabel.setText ("Not Connected."); 
      }
      serverLabelBox.add (rbnbServerLabel);
      Box sendButtonBox = Box.createHorizontalBox ();
      sendMarkerButton = new JButton ("Send Marker into channel \"" + myEvent.rbnbChannel + "\"");
      sendMarkerButton.addActionListener (this);
      sendMarkerButton.setActionCommand ("sendMarker");
      sendButtonBox.add (sendMarkerButton);
      
      // List of field entries from the dtd
      Box layoutFieldBox = Box.createVerticalBox ();
      layoutFieldBox.add (serverLabelBox);
      
      // fields go here
      Box eventTypeBox = Box.createHorizontalBox ();
         JLabel eventTypeLabel = new JLabel ("type");
         JComboBox eventTypeComboBox = new JComboBox (NeesEvent.eventTypes);
         
         eventTypeComboBox.addActionListener (new ActionListener() {
            public void actionPerformed (ActionEvent e) {
               JComboBox cb = (JComboBox)e.getSource ();
               myEvent.setProperty ("type", (String)cb.getSelectedItem ());
            }
         });
         eventTypeBox.add (eventTypeLabel);
         eventTypeBox.add (Box.createHorizontalStrut (11));
         eventTypeBox.add (eventTypeComboBox);
         
      // loop thru fields //////////////////////////////////////////////////////
      fieldBoxes = new Box[NeesEvent.markerFieldNames.length];
      Vector jtextFieldsTmp = new Vector ();
      Vector jlabelsTmp = new Vector ();
      for (int i=0; i<NeesEvent.markerFieldNames.length; i++) {
         if ( NeesEvent.markerFieldNames[i].equals ("timestamp") ||
              NeesEvent.markerFieldNames[i].equals ("type")) continue;
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
      }
      
      Object[] jtextFieldsTmpArray = jtextFieldsTmp.toArray ();
      this.jtextFields = new JTextField[jtextFieldsTmpArray.length];
      for (int i=0; i<jtextFieldsTmpArray.length; i++) {
         jtextFields[i] = (JTextField)jtextFieldsTmpArray[i];
      }
      Object[] jlabelTmpArray = jlabelsTmp.toArray ();
      this.jlabels = new JLabel[jlabelTmpArray.length];
      for (int i=0; i<jlabelTmpArray.length; i++) {
         jlabels[i] = (JLabel)jlabelTmpArray[i];
      }
      
      layoutFieldBox.add (eventTypeBox);
      // end field list
      
    Box textAreaBox = Box.createHorizontalBox ();
	     markerDisplayArea = new JTextArea ();
        markerDisplayArea.setBorder (BorderFactory.createEtchedBorder ());
        markerDisplayArea.setEditable (false);
        textAreaBox.add (markerDisplayArea);
	  
    layoutFieldBox.add (sendButtonBox);
    Box sentBox = Box.createHorizontalBox ();
    sentBox.add (new JLabel ("Last Marker Sent"));
    layoutFieldBox.add (sentBox);
    layoutFieldBox.add (textAreaBox);
    mainPanel.add (layoutFieldBox);
      
    mainPanelBox = Box.createVerticalBox ();
    mainPanelBox.add (mainPanel);
    c.add (mainPanelBox);
    setSize (FRAME_WIDTH, FRAME_HEIGHT);
    this.setResizable (true);
    try {
       sendOpeningMarker ();
    } catch (IOException ioe) {
       log.error ("I/O problem sending opening marker." + ioe);
    } catch (TransformerException te) {
       log.error ("XML problem sending opening marker." + te);
    }
    show (); 
   } // constructor ()   

   /** A method to mark the beginning of the sumbission session. */
   public void sendOpeningMarker () throws IOException, TransformerException {
      /* Stuff a special a marker and send it upon. */
      NeesEvent openingMarker = new NeesEvent (this.rbnbChannel);
      openingMarker.setProperty ("source", SendMarkerGUI.class.getName ());
      openingMarker.setProperty ("type", "annotation");
      openingMarker.setProperty ("content", "Marker Submission Client Connected.");
      openingMarker.setProperty ("timestamp",""+(((double)(System.currentTimeMillis ())) / 1000.0));
      
      if (this.hasFakeChannel) {
         myEventChannel.postEvent (openingMarker);
      } else { // we are sending to a real DataTurbine
         try {
            this.myTurban.putMarker (openingMarker, openingMarker.rbnbChannel);
            markerDisplayArea.setText (openingMarker.toEventXmlString ());
            log.info ("Submitted opening marker.");
         } catch (IOException ioe) {
            log.error ("IO error displaying to the GUI\n" + ioe);
            throw ioe;
         } catch (TransformerException te) {
            log.error ("XML Error forming the XML doc\n" + te);
            throw te;
         }
      } // else
   } // sendOpeningMarker ()
   
   /** A method to mark the end of the sumbission session. */
   public void sendClosingMarker () throws IOException, TransformerException {
      /* Stuff a special a marker and send it. */
      NeesEvent closingMarker = new NeesEvent (this.rbnbChannel);
      closingMarker.setProperty ("source", SendMarkerGUI.class.getName ());
      closingMarker.setProperty ("type", "annotation");
      closingMarker.setProperty ("content", "Marker Submission Client Disconnected.");
      closingMarker.setProperty ("timestamp",""+(((double)(System.currentTimeMillis ())) / 1000.0));
      
      if (this.hasFakeChannel) {
         myEventChannel.postEvent (closingMarker);
      } else { // we are sending to a real DataTurbine
         try {
            this.myTurban.putMarker (closingMarker, closingMarker.rbnbChannel);
            markerDisplayArea.setText (closingMarker.toEventXmlString ());
            log.info ("Submitted closing marker.");
         } catch (IOException ioe) {
            log.error ("IO error displaying to the GUI\n" + ioe);
            throw ioe;
         } catch (TransformerException te) {
            log.error ("XML Error forming the XML doc\n" + te);
            throw te;
         }
      } // else
   } // sendClosingMarker ()
   
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
         log.info ("Closed DataTurbine.");
      }
   } catch (Exception ex) {
      log.error ("Error closing turbine: " + ex);
   }
   // indicate disconnected in UI in any case
   if (! this.myTurban.isConnected ()) {
      rbnbServerLabel.setText ("Not Connected.");
   }
   
   } // closeTurbine ()
   
   /** A method to handle the main GUI frame. */
   private void setWindowListener () {
      final SendMarkerGUI app = this;
      app.addWindowListener (new WindowAdapter () {
         public void windowClosing (WindowEvent e) {
            System.exit (0);
         }
         public void windowActivated (WindowEvent e) {
            app.getContentPane ().repaint ();
            app.getJMenuBar ().repaint ();
         }
      }
                             );
   }
   /** A method to handle the synthetic DataTurbine channel. */
   private void setEventChannel (String server) {
       if (myEventChannel != null) myEventChannel.disconnect ();
       myEventChannel = new FakeNeesEventChannel (server);
       myEventChannel.reconnect ();
   }
   
   /** A method to handle the synthetic DataTurbine channel. */
   private void setEventChannelListener() {
       if (myEventChannel == null) return;
       myEventChannel.addListener (new NeesEventListener () {
          public void performEventAction (NeesEvent ev) {
              reportEvent (ev);
          }
       });
   }
   
   /** A method to display an event being processed by the program. */
   private void reportEvent (NeesEvent ev)
   {
     try {
       markerDisplayArea.setText (ev.toEventXmlString ());
       if ( ((String)(ev.getProperty ("type"))).compareToIgnoreCase ("start") == 0 ) {
         markerDisplayArea.setBackground (NeesEvent.startColor);
       } else if ( ((String)(ev.getProperty ("type"))).compareToIgnoreCase ("stop") == 0 ) {
         markerDisplayArea.setBackground (NeesEvent.stopColor);
       } else {
         markerDisplayArea.setBackground (NeesEvent.noteColor);
       }
     } catch (IOException e) {
       e.printStackTrace ();
     } catch (TransformerException e) {
       e.printStackTrace  ();
     }
     markerDisplayArea.repaint();       
   }
   
/******************************************************************************/      
   public static void main (String[] args) {
      SendMarkerGUI app = new SendMarkerGUI (null);
      //////////////////////////////////////////////////////////////////////////
      /* command-line argument handling */
      Options opts = new Options ();
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = null;
      
      opts.addOption ("a", false, "about");
      opts.addOption ("d", false, "enable debug output");
      opts.addOption ("f", false, "enable fake channel to act as source to");
      opts.addOption ("n", true, "DataTurbine source name to register");
      opts.addOption ("c", true, "Channel name to register in the '-n' source");
      opts.addOption ("r", true, "DataTurbine server name to connect to as a source." + 
                                  " Default is to use a fake channel to act as a source for.");
      opts.addOption ("s", false, "Ask DataTurbine to archive this data.");
      
      try {
         cmd = parser.parse (opts, args); 
      } catch (ParseException pe) {
         HelpFormatter formatter = new HelpFormatter ();
         formatter.printHelp ("SendMarkerGUI", opts);
         System.exit (0);
      }
      
      if (cmd.hasOption ("a")) {
         System.out.println ("About: this is a GUI program that generates " +
                             "and submits an event marker to either " +
                             "dataTurbine or a synthetic data queue.");
         System.exit (0);
      } if (cmd.hasOption ("c")) {
         app.rbnbChannel = (cmd.getOptionValue ("c"));
      } if (cmd.hasOption ("d")) {
         app.debugging = true;
      } if (cmd.hasOption ("f")) {
         app.hasFakeChannel = true;
      } if (cmd.hasOption ("n")) {
         app.rbnbSourceName = (cmd.getOptionValue ("n")); 
      } if (cmd.hasOption ("r")) {
         app.rbnbServerName = (cmd.getOptionValue ("r"));
         app.hasFakeChannel = false;
      } if (cmd.hasOption ("s")) {
         app.archiveRbnbData = true;
      }
      
      /* End of command-line handling */
      //////////////////////////////////////////////////////////////////////////
   } // main()

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
   
   /***************************************************************************/ 
   public void actionPerformed (ActionEvent e) {
         String commands[] = {"quit", "dtConnect", "dtDisconnect", "dtChannel", "sendMarker"};
         int index;
      
      for (index=0; index<commands.length; index++) {
         if (e.getActionCommand ().equals (commands[index])) {
            break;
         }
      }
            
      switch (index) {
         case 0: // quit
            try {
               sendClosingMarker ();
            } catch (IOException ioe) {
               log.error ("I/O problem sending closing marker." + ioe);
            } catch (TransformerException te) {
               log.error ("XML problem sending closing marker." + te);
            }
            System.exit (0); break;
         case 1: // dtConnect - connnect to rbnb server
            if (this.hasFakeChannel) {
               myEventChannel.reconnect();
            } else { // real turbine
               DataTurbine tempTurban = null;
               String tempRbnbServerName;
               tempRbnbServerName = turbanPane.showInputDialog (
                  "Please input the DataTurbine server to\n" +
                  "which to connect in the format host:port");
               try {
                  tempTurban = new DataTurbine (rbnbSourceName);
                  tempTurban.setServerName (tempRbnbServerName);
                  tempTurban.open ();
                  if (this.myTurban != null &&
                      tempTurban != null &&
                      tempTurban.isConnected ())
                  {
                     this.myTurban = tempTurban;
                     log.info ("DataTurbine server changed from " +
                               this.rbnbServerName + " to " + 
                               tempRbnbServerName);
                     this.rbnbServerName = tempRbnbServerName;
                     this.sendOpeningMarker ();
                     this.rbnbServerLabel.setText (this.rbnbServerName);
                  } else {
                     log.debug ("DataTurbine server changed from " +
                                this.rbnbServerName + " to " + 
                                tempRbnbServerName + " requested, but " +
                                tempRbnbServerName + " isn't connected.");
                  }
               } catch (Exception exe) {
                  log.error ("Problem opening the new DataTurbine " + exe);
               }
            } // else
            break;
         case 2: // dtDisconnect - disconnnect from the rbnb server
            if (this.hasFakeChannel) {
               myEventChannel.disconnect();
               log.info ("Disconnected from fake channel");
            } else {
               try {
                  sendClosingMarker ();
               } catch (IOException ioe) {
                  log.error ("I/O problem sending closing marker." + ioe);
               } catch (TransformerException te) {
                  log.error ("XML problem sending closing marker." + te);
               }
               this.closeTurbine ();
            } // else
            break;
         case 3: // dtChannel - set a new channel name from user input
             String tmpRbnbChannel = turbanPane.showInputDialog (
               "Please enter the new DataTurbine channel name to send " + 
               "markers into.\nThe current value is \"" +  this.rbnbChannel + "\""
                                                                 );
            this.rbnbChannel = (tmpRbnbChannel != null)?tmpRbnbChannel:rbnbChannel;
            myEvent.rbnbChannel = this.rbnbChannel;
            sendMarkerButton.setText ("Send Marker into channel \"" +
                                      myEvent.rbnbChannel + "\"");
           markerDisplayArea.setText ("");
           markerDisplayArea.repaint ();
         break;
         case 4: // sendMarker - send a marker to the rbnb server
            for (int i=0; i<this.jlabels.length; i++) {
               myEvent.setProperty (this.jlabels[i].getText (), this.jtextFields[i].getText ());
            } // for
           myEvent.setProperty ("timestamp",""+(((double)(System.currentTimeMillis ())) / 1000.0));
            if (this.hasFakeChannel) {
               myEventChannel.postEvent (myEvent);
            } else { // we are connected to a real DataTurbine
               try {
                  this.myTurban.putMarker (myEvent, myEvent.rbnbChannel);
                  reportEvent (myEvent);
               } catch (Exception ex) {
                  log.error ("Error putting the XML into the Turbine: " + ex);
               }
            }
            break;
         }
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
